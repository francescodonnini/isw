package io.github.francescodonnini.data;

import io.github.francescodonnini.collectors.ast.*;
import io.github.francescodonnini.model.JavaClass;
import io.github.francescodonnini.model.JavaMethod;
import io.github.francescodonnini.model.Release;
import io.github.francescodonnini.utils.FileUtils;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.cpd.CPDConfiguration;
import net.sourceforge.pmd.cpd.CpdAnalysis;
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
import java.util.function.Predicate;
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
    private final List<JavaClass> classes = new ArrayList<>();
    private final List<JavaMethod> methods = new ArrayList<>();
    private final JavaMethodExtractor extractor;
    private boolean dataLoaded = false;
    private final Path reportsPath;
    private final List<Release> releases;
    private final List<Integer> methodsPerRelease = new ArrayList<>();

    public DataLoaderImpl(
            AbstractCounterFactoryImpl factory, List<Release> releases, String projectPath,
            Path reportsPath) throws IOException {
        this.projectPath = projectPath;
        this.git = createGit(projectPath);
        this.reportsPath = reportsPath;
        extractor = new JavaMethodExtractor(createCounters(factory));
        this.releases = releases;
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
                factory.build(NestingDepth.class),
                factory.build(HalsteadComplexityCounter.class));
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
            FileUtils.createDirectory(reportsPath);
            var endTime = releases.getLast().releaseDate();
            // lista di commit effettuati non oltre endTime e ordinati rispetto alla data di commit.
            var commits = StreamSupport
                    .stream(git.log().call().spliterator(), false)
                    .filter(c -> !getCommitDate(c).isAfter(endTime))
                    .sorted(Comparator.comparingInt(RevCommit::getCommitTime))
                    .toList();
            var releaseChangeSet = new HashSet<String>();
            var lastCommitPerRelease = getLastCommitPerRelease(commits);
            logger.log(Level.INFO, "total commits: {0}", commits.size());
            var progress = 0;
            for (var commit : commits) {
                ++progress;
                logProgress(progress, commits.size());
                checkout(git, commit.getName());
                var lastRelease = lastCommitPerRelease.get(commit.getName());
                var susceptible = getTouchedFiles(commit);
                releaseChangeSet.addAll(susceptible);
                Predicate<String> filter = getPathFilter(lastRelease != null, susceptible, releaseChangeSet);
                loadData(commit, filter);
                if (lastRelease != null) {
                    logger.log(Level.INFO, "In the closing commit of release %s a total of %d methods has been read".formatted(lastRelease, methodsPerRelease.getLast()));
                    logger.log(Level.INFO, "A total of %d has been read for release %s (commit %s)".formatted(methodsPerRelease.stream().mapToInt(i -> i).sum(), lastRelease, commit.getName()));
                    methodsPerRelease.clear();
                    releaseChangeSet.clear();
                }
            }
            dataLoaded = true;
        } finally {
            checkout(git, head);
        }
    }

    private Predicate<String> getPathFilter(boolean isLastCommitOfRelease, Set<String> susceptible, Set<String> alreadyChecked) {
        if (isLastCommitOfRelease) {
            return p -> !alreadyChecked.contains(p);
        }
        return susceptible::contains;
    }

    private Map<String, Release> getLastCommitPerRelease(List<RevCommit> commits) {
        var map = new HashMap<String, Release>();
        for (var release : releases) {
            var o = commits.stream()
                    .filter(c -> !getCommitDate(c).isAfter(release.releaseDate()))
                    .max(Comparator.comparing(this::getCommitDate));
            if (o.isPresent()) {
                var commit = o.get();
                map.put(o.get().getName(), release);
                logger.log(Level.INFO, "commit %s is the last one for release %s".formatted(commit, release));
            } else {
                logger.log(Level.WARNING, "cannot find a closing commit for release %s", release);
            }
        }
        return map;
    }

    private void loadData(RevCommit commit, Predicate<String> pathFilter) {
        try (var pmd = createPmdAnalysis(commit.getName());
             var cpd = createCpdAnalysis()) {
            var parent = Path.of(projectPath);
            var classList = new ArrayList<JavaClass>();
            listAllFiles(parent).stream()
                    .filter(this::isValidPath)
                    .filter(p -> pathFilter.test(p.toString()))
                    .filter(p -> !isGenerated(parent.resolve(p), commit))
                    .forEach(p -> {
                        classList.addAll(parseFile(p, commit));
                        pmd.files().addFile(parent.resolve(p));
                        cpd.files().addFile(parent.resolve(p));
                    });
            parseCommit(classList, commit);
            pmd.performAnalysis();
            cpd.performAnalysis(new CPDConsumer(classList));
            addProgramData(classList);
        } catch (IOException e) {
            logger.info(e.getMessage());
        }
    }

    private void addProgramData(ArrayList<JavaClass> classList) {
        classList.stream().filter(c -> !c.getMethods().isEmpty()).forEach(c -> {
            var methods = c.getMethods();
            this.methods.addAll(methods);
            classes.add(c);
            methodsPerRelease.add(methods.size());
        });
    }

    private CpdAnalysis createCpdAnalysis() {
        var config = new CPDConfiguration();
        config.setMinimumTileSize(100);
        config.setDefaultLanguageVersion(LanguageRegistry.CPD.getLanguageVersionById("java", "22"));
        config.setIgnoreIdentifiers(true);
        config.setIgnoreLiterals(true);
        return CpdAnalysis.create(config);
    }

    private PmdAnalysis createPmdAnalysis(String reportName) throws IOException {
        if (!reportName.endsWith(".csv")) {
            reportName += ".csv";
        }
        var renderer = new CSVRenderer();
        renderer.setWriter(Files.newBufferedWriter(reportsPath.resolve(reportName)));
        var pmd = PmdAnalysis.create(getPmdConfig(reportName));
        pmd.addRenderer(renderer);
        return pmd;
    }

    private PMDConfiguration getPmdConfig(String reportName) {
        var config = new PMDConfiguration();
        config.setDefaultLanguageVersion(LanguageRegistry.PMD.getLanguageVersionById("java", "22"));
        config.setReportFile(reportsPath.resolve(reportName));
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

    private boolean isGenerated(Path path, RevCommit commit) {
        try (var lines = Files.lines(path)) {
            var b = lines.limit(50).anyMatch(this::isAutogeneratedComment);
            if (b) {
                logger.log(Level.INFO, "file %s is probably autogenerated (commit %s)".formatted(path, commit.getName().substring(0, 6)));
            }
            return b;
        } catch (IOException e) {
            logger.log(Level.INFO, "error reading file %s".formatted(path), e);
            return true;
        }
    }

    private boolean isAutogeneratedComment(String line) {
        line = line.toLowerCase();
        return line.contains("generated by the protocol buffer compiler")
                || line.contains("do not edit!")
                || line.contains("autogenerated by thrift")
                || line.contains("@generated");
    }

    private List<JavaClass> parseFile(Path file, RevCommit commit) {
        try {
            var ctx = new ParseContext(commit.getName(), Path.of(projectPath), file, getCommitTime(commit));
            return parseClass(ctx);
        } catch (IOException e) {
            logger.log(Level.INFO, e.getMessage());
            return List.of();
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
        if (classList.isEmpty()) {
            return;
        }
        var df = new DiffFormatter(DisabledOutputStream.INSTANCE);
        df.setRepository(git.getRepository());
        df.setDetectRenames(true);
        var index = classList.stream()
                .collect(Collectors.groupingBy(c -> c.getPath().toString()));
        var diffs = df.scan(getParent(commit), commit.getTree());
        for (var diff : diffs) {
            var oldPath = diff.getOldPath();
            var path = diff.getNewPath();
            // var edits = df.toFileHeader(diff).toEditList();
            // Se il percorso del file modificato non è un file .java allora non è necessario analizzare
            // la modifica.
            if (path.endsWith(JAVA_FILE_EXT) && index.containsKey(path)) {
                var author = getAuthor(commit);
                for (var c : index.get(path)) {
                    author.ifPresent(c::setAuthor);
                    // c.getMethods().removeIf(m -> !EditUtils.isTouched(m, edits));
                }
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
        } catch (IndexOutOfBoundsException e) {
            logger.log(Level.INFO, "commit %s has no parent".formatted(commit));
            return null;
        }
    }

    private Optional<String> getAuthor(RevCommit commit) {
        var author = commit.getAuthorIdent().getEmailAddress();
        if (author == null || author.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(author);
    }
}
