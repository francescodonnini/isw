package io.github.francescodonnini.data;

import io.github.francescodonnini.collectors.ast.*;
import io.github.francescodonnini.model.JavaClass;
import io.github.francescodonnini.model.JavaMethod;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.renderers.CSVRenderer;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DataLoaderImpl implements ClassDataLoader, MethodDataLoader {
    private static final String JAVA_FILE_EXT = ".java";
    private final Logger logger = Logger.getLogger(DataLoaderImpl.class.getName());
    // repositoryPath è il percorso delle repository dove leggere i file da cui creare le entry per il dataset.
    private final String projectPath;
    private final Git git;
    private final LocalDate endTime;
    private final List<JavaClass> classes = new ArrayList<>();
    private final List<JavaMethod> methods = new ArrayList<>();
    private final JavaMethodExtractor extractor;
    private boolean dataLoaded = false;
    private final String reportsPath;

    public DataLoaderImpl(
            String projectPath,
            AbstractCounterFactoryImpl factory,
            LocalDate endTime,
            String reportsPath) throws IOException {
        this.projectPath = projectPath;
        this.git = createGit(projectPath);
        this.endTime = endTime;
        this.reportsPath = reportsPath;
        extractor = new JavaMethodExtractor(createCounters(factory));
    }

    private Git createGit(String projectPath) throws IOException {
        var repository = new FileRepositoryBuilder()
                .setGitDir(new File(projectPath, ".git"))
                .build();
        return new Git(repository);
    }

    private List<AbstractCounter> createCounters(AbstractCounterFactoryImpl factory) {
        return List.of(
                factory.build(CyclomaticComplexityCounter.class),
                factory.build(InputParametersCounter.class),
                factory.build(StatementsCounter.class),
                factory.build(ElseCounter.class),
                factory.build(NestingDepth.class));
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
            return new ArrayList<>(methods);
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
        try {
            // lista di commit effettuati non oltre endTime e ordinati rispetto alla data di commit.
            var commits = StreamSupport
                    .stream(git.log().call().spliterator(), false)
                    .filter(c -> !getCommitDate(c).isAfter(endTime))
                    .sorted(Comparator.comparingInt(RevCommit::getCommitTime))
                    .toList();
            logger.log(Level.INFO, "total commits: {0}", commits.size());
            int progress = 0;
            for (var commit : commits) {
                ++progress;
                logProgress(progress, commits.size());
                checkout(git, commit.getName());
                var susceptible = getTouchedFiles(commit);
                try (var pmd = createPmdAnalysis(commit.getName())) {
                    var parent = Path.of(projectPath);
                    listAllFiles(parent).stream()
                            .filter(this::isValidPath)
                            .filter(p -> susceptible.contains(p.toString()))
                            .forEach(p -> {
                                parseFile(p, commit);
                                pmd.files().addFile(parent.resolve(p));
                            });
                    pmd.performAnalysis();
                } catch (IOException e) {
                    logger.info(e.getMessage());
                }
            }
            dataLoaded = true;
        } finally {
            checkout(git, head);
        }
    }

    private PmdAnalysis createPmdAnalysis(String reportName) throws IOException {
        if (!reportName.endsWith(".csv")) {
            reportName += ".csv";
        }
        var renderer = new CSVRenderer();
        renderer.setWriter(Files.newBufferedWriter(Path.of(reportsPath, reportName)));
        var pmd = PmdAnalysis.create(getPmdConfig(reportName));
        pmd.addRenderer(renderer);
        return pmd;
    }

    private PMDConfiguration getPmdConfig(String reportName) {
        var config = new PMDConfiguration();
        config.setDefaultLanguageVersion(LanguageRegistry.PMD.getLanguageVersionById("java", "22"));
        config.setReportFile(Path.of(reportsPath, reportName));
        config.addRuleSet("rulesets/java/quickstart.xml");
        return config;
    }

    private void logProgress(int progress, int total) {
        if (progress % 100 == 0) {
            logger.log(Level.INFO, () -> "%d/%d (%.2f%%)".formatted(progress, total, progress * 100. / total));
        }
    }

    /**
     * Prende l'insieme dei file modificati da un commit
     * @param commit di cui si vuole sapere i file che ha modificato
     * @return una collezione di percorsi dei file toccati dal commit
     * @throws IOException se non è possibile recuperare l'insieme delle differenze tra due commit
     */
    private Set<String> getTouchedFiles(RevCommit commit) throws IOException {
        var df = new DiffFormatter(DisabledOutputStream.INSTANCE);
        df.setRepository(git.getRepository());
        df.setDetectRenames(true);
        var touchedFiles = new HashSet<String>();
        var diffs = df.scan(getParent(commit), commit.getTree());
        for (var diff : diffs) {
            var path = diff.getNewPath();
            // Se il percorso del file modificato non è un file .java allora non è necessario analizzare
            // la modifica.
            if (path.endsWith(JAVA_FILE_EXT)) {
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
        return path.endsWith(JAVA_FILE_EXT) && !path.endsWith("package-info.java");
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

    private LocalDate getCommitDate(RevCommit commit) {
        return getCommitTime(commit).toLocalDate();
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
        var index = classList.stream()
                .collect(Collectors.groupingBy(c -> c.getPath().toString()));
        var diffs = df.scan(getParent(commit), commit.getTree());
        for (var diff : diffs) {
            var oldPath = diff.getOldPath();
            var path = diff.getNewPath();
            // Se il percorso del file modificato non è un file .java allora non è necessario analizzare
            // la modifica.
            if (path.endsWith(JAVA_FILE_EXT) && index.containsKey(path)) {
                getAuthor(commit).ifPresent(author -> index.get(path).forEach(c -> c.setAuthor(author)));
                if (!oldPath.equals("/dev/null") && !oldPath.equals(path)) {
                    renameOldEntries(oldPath, path);
                }
            }
        }
    }

    private void renameOldEntries(String oldPath, String path) {
        classes
                .stream()
                .filter(c -> c.getPath().toString().equals(oldPath))
                .forEach(c -> updatePath(c, Path.of(path)));
    }

    private void updatePath(JavaClass c, Path path) {
        if (c.getOldPath().isEmpty()) {
            c.setOldPath(c.getPath());
        }
        c.setPath(path);
    }

    private RevTree getParent(RevCommit commit) {
        try {
            return commit.getParent(0).getTree();
        } catch (IndexOutOfBoundsException _) {
            logger.log(Level.INFO, "commit %s has no parent".formatted(commit));
            return null;
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
