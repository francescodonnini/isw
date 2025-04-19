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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
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
            return new ArrayList<>(classes);
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
        var commits = StreamSupport
                .stream(git.log().call().spliterator(), false)
                .sorted(Comparator.comparingInt(RevCommit::getCommitTime))
                .toList();
        logger.log(Level.INFO, "total commits: {0}", commits.size());
        int progress = 0;
        for (var commit : commits) {
            ++progress;
            if (progress % 100 == 0) {
                logger.log(Level.INFO, "%d/%d (%f%%)".formatted(progress, commits.size(), progress * 100. / commits.size()));
            }
            checkout(git, commit.getName());
            var susceptibles = getTouchedFiles(commit);
            try {
                var files = listAllFiles(Path.of(projectPath)).stream()
                        .filter(this::isValidPath)
                        .collect(Collectors.toSet());
                files.stream()
                        .filter(path -> susceptibles.contains(path.toString()))
                        .forEach(path -> parseFile(path, commit));
            } catch (IOException e) {
                checkout(git, head);
            }
        }
        checkout(git, head);
        dataLoaded = true;
    }

    private List<String> getTouchedFiles(RevCommit commit) throws IOException {
        var df = new DiffFormatter(DisabledOutputStream.INSTANCE);
        df.setRepository(git.getRepository());
        df.setDetectRenames(true);
        var o = getParent(commit);
        if (o.isEmpty()) {
            return List.of();
        }
        var touchedFiles = new ArrayList<String>();
        var diffs = df.scan(o.get().getTree(), commit.getTree());
        for (var diff : diffs) {
            var path = diff.getNewPath();
            // Se il percorso del file modificato non è un file .java allora non è necessario analizzare
            // la modifica.
            if (path.endsWith(".java")) {
                touchedFiles.add(path);
            }
        }
        return touchedFiles;
    }

    private void checkout(Git git, String branch) throws GitAPIException {
        git.checkout().setName(branch).call();
    }

    private List<Path> listAllFiles(Path basePath) throws IOException {
        var remainingDirectories = new ArrayList<Path>();
        remainingDirectories.add(basePath);
        var files = new ArrayList<Path>();
        while (!remainingDirectories.isEmpty()) {
            var path = remainingDirectories.removeLast();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (Path entry : stream) {
                    if (Files.isDirectory(entry)) {
                        try {
                            if (!Files.isHidden(entry)) {
                                remainingDirectories.add(entry);
                            }
                        } catch (IOException e) {
                            logger.log(Level.INFO, "error reading file %s".formatted(entry), e);
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
            var ctx = new ParseContext(commit.getName(), Path.of(projectPath), file, getCommitTime(commit));
            var classList = parseClass(ctx);
            // Raccoglie informazioni sui metodi modificati dal commit 'commit'
            parseCommit(classList, commit);
            classList.forEach(c -> methods.addAll(c.getMethods()));
            classes.addAll(classList);
        } catch (IOException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }

    private LocalDateTime getCommitTime(RevCommit commit) {
        return commit.getAuthorIdent().getWhenAsInstant().atZone(commit.getAuthorIdent().getZoneId()).toLocalDateTime();
    }



    public List<JavaClass> parseClass(ParseContext ctx) throws IOException {
        try {
            extractor.parse(ctx);
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
                .collect(Collectors.groupingBy(c -> c.getPath().toString()));
        var diffs = df.scan(o.get().getTree(), commit.getTree());
        for (var diff : diffs) {
            var oldPath = diff.getOldPath();
            var path = diff.getNewPath();
            // Se il percorso del file modificato non è un file .java allora non è necessario analizzare
            // la modifica.
            if (path.endsWith(".java") && index.containsKey(path)) {
                getAuthor(commit).ifPresent(author -> index.get(path).forEach(c -> c.setAuthor(author)));
                if (!oldPath.equals("/dev/null") && !oldPath.equals(path)) {
                    index.get(path).forEach(c -> c.setOldPath(Path.of(oldPath)));
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
