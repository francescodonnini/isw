package io.github.francescodonnini.data;

import io.github.francescodonnini.collectors.ast.AbstractCounter;
import io.github.francescodonnini.model.JavaClass;
import io.github.francescodonnini.model.JavaMethod;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DataLoaderImpl implements DataLoader {
    private final Logger logger = Logger.getLogger(DataLoaderImpl.class.getName());
    // repositoryPath è il percorso delle repository dove leggere i file da cui creare le entry per il dataset.
    private final String projectPath;
    private final Git git;
    // releases è la lista delle release da cui selezionare i file per le entry.
    private final JavaMethodExtractor extractor;
    private final List<JavaClass> classes = new ArrayList<>();
    private final List<JavaMethod> methods = new ArrayList<>();
    private boolean dataLoaded = false;

    public DataLoaderImpl(
            String projectPath,
            List<AbstractCounter> counters) throws IOException {
        this.projectPath = projectPath;
        this.git = createGit(projectPath);
        this.extractor = new JavaMethodExtractor(counters);
    }

    private Git createGit(String projectPath) throws IOException {
        var repository = new FileRepositoryBuilder()
                .setGitDir(new File(projectPath, ".git"))
                .build();
        return new Git(repository);
    }

    @Override
    public List<JavaClass> getClasses() throws DataLoaderException {
        try {
            lazyDataLoading();
            return classes;
        } catch (GitAPIException | IOException e) {
            throw new DataLoaderException(e);
        }
    }

    @Override
    public List<JavaMethod> getMethods() throws DataLoaderException {
        try {
            lazyDataLoading();
            return methods;
        } catch (GitAPIException | IOException e) {
            throw new DataLoaderException(e);
        }
    }

    private void lazyDataLoading() throws GitAPIException, IOException {
        if (!dataLoaded) {
            loadData();
        }
    }

    private void loadData() throws IOException, GitAPIException {
        var head = git.getRepository().getBranch();
        var commits = StreamSupport.stream(git.log().call().spliterator(), false)
                .toList();
        for (var commit : commits) {
            checkout(git, commit.getName());
            try {
                var files = listAllFiles(Path.of(projectPath)).stream()
                        .filter(this::isValidPath)
                        .toList();
                for (var path : files) {
                    parseFile(path, commit);
                }
            } catch (IOException e) {
                checkout(git, head);
            }
        }
        checkout(git, head);
        dataLoaded = true;
    }

    private void checkout(Git git, String branch) throws GitAPIException {
        git.checkout().setName(branch).call();
    }

    private static List<Path> listAllFiles(Path basePath) throws IOException {
        var paths = new ArrayList<Path>();
        paths.add(basePath);
        var files = new ArrayList<Path>();
        while (!paths.isEmpty()) {
            var path = paths.removeLast();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (Path entry : stream) {
                    if (Files.isDirectory(entry)) {
                        if (!Files.isHidden(entry)) {
                            paths.add(entry);
                        }
                    } else if (isJavaFile(entry.toString())) {
                        files.add(basePath.relativize(entry));
                    }
                }
            }
        }
        return files;
    }

    // Si vogliono selezionare solamente i file .java che non sono file di test.
    private boolean isValidPath(Path path) {
        return isJavaFile(path.toString());
    }


    private static boolean isJavaFile(String path) {
        return path.endsWith(".java") && !path.endsWith("package-info.java");
    }

    private void parseFile(Path file, RevCommit commit) {
        try {
            // clazz è l'entry point del file,
            var clazz = new JavaClass(commit.getName(), Path.of(projectPath), file, getCommitTime(commit));
            extractor.setClass(clazz);
            var classList = parseClass(clazz);
            // Raccoglie informazioni sui metodi modificati dal commit 'commit'
            parseCommit(classList, commit);
            classList.forEach(c -> methods.addAll(c.getMethods()));
        } catch (IOException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }

    private LocalDateTime getCommitTime(RevCommit commit) {
        return commit.getAuthorIdent().getWhenAsInstant().atZone(commit.getAuthorIdent().getZoneId()).toLocalDateTime();
    }

    private List<JavaClass> parseClass(JavaClass clazz) throws IOException {
        try {
            extractor.setClass(clazz);
            extractor.parse();
            return extractor.getClasses();
        } finally {
            extractor.reset();
        }
    }

    private void parseCommit(List<JavaClass> classList, RevCommit commit) throws IOException {
        var df = new DiffFormatter(DisabledOutputStream.INSTANCE);
        df.setRepository(git.getRepository());
        df.setDetectRenames(true);
        var o = getParent(commit);
        if (o.isEmpty()) {
            return;
        }
        var index = classList.stream()
                .map(c -> Map.entry(c.getPath().toString(), c))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        var diffs = df.scan(o.get().getTree(), commit.getTree());
        for (var diff : diffs) {
            var oldPath = diff.getOldPath();
            var path = diff.getNewPath();
            // Se il percorso del file modificato non è un file .java allora non è necessario analizzare
            // la modifica.
            if (path.endsWith(".java") && index.containsKey(path)) {
                getAuthor(commit).ifPresent(author -> index.get(path).setAuthor(author));
                if (!oldPath.equals("/dev/null") && !oldPath.equals(path)) {
                    index.get(path).setOldPath(Path.of(oldPath));
                    logger.log(Level.INFO, "%s > %s".formatted(oldPath, path));
                }
            }
        }
    }

    private Optional<RevCommit> getParent(RevCommit commit) {
        try {
            return Optional.ofNullable(commit.getParent(0));
        } catch (IndexOutOfBoundsException e) {
            logger.log(Level.INFO, "Commit %s has no parent".formatted(commit));
            return Optional.empty();
        }
    }

    private Optional<String> getAuthor(RevCommit commit) {
        var author = commit.getAuthorIdent().getEmailAddress();
        if (author == null || author.isEmpty() || author.equalsIgnoreCase("unknown@apache.org")) {
            return Optional.empty();
        }
        return Optional.of(author);
    }
}
