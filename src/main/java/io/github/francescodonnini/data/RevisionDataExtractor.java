package io.github.francescodonnini.data;

import io.github.francescodonnini.data.pmd.JavaLanguage;
import io.github.francescodonnini.data.pmd.PMDFactory;
import io.github.francescodonnini.model.Release;
import io.github.francescodonnini.model.RevisionJavaClass;
import io.github.francescodonnini.utils.FileUtils;
import io.github.francescodonnini.utils.GitUtils;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.lang.document.FileId;
import net.sourceforge.pmd.lang.document.TextFile;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class RevisionDataExtractor {
    private static final String JAVA_FILE_EXT = ".java";
    private final Logger logger = Logger.getLogger(RevisionDataExtractor.class.getName());
    // projectPath è il percorso delle repository dove leggere i file da cui creare le entry per il dataset.
    private final Path  projectPath;
    private final Path reportsPath;
    private final Git git;
    private final TrackingIdService trackingId = new TrackingIdService();
    private final PMDConfiguration pmdConfig;
    private final JavaClassAnalyzerFactory factory;
    private final List<RevisionJavaClass> classes = new ArrayList<>();
    private boolean dataLoaded = false;
    private final List<Release> releases;
    private final List<Integer> classesPerRelease = new ArrayList<>();
    private int currentChangeSetSize = 0;
    private final Map<String, Integer> developersExperience = new HashMap<>();
    private final Map<Long, Map<String, Integer>> fileDevelopersExperience = new HashMap<>();

    public RevisionDataExtractor(
            JavaClassAnalyzerFactory factory,
            List<Release> releases,
            Path projectPath,
            Path reportsPath) throws IOException {
        this.factory = factory;
        this.projectPath = projectPath;
        this.reportsPath = reportsPath;
        this.releases = releases;
        this.git = GitUtils.createGit(projectPath);
        pmdConfig = PMDFactory.create();
    }

    public List<RevisionJavaClass> getRevisionClasses() throws DataLoaderException {
        try {
            lazyDataLoading();
            return new ArrayList<>(classes);
        } catch (GitAPIException | IOException e) {
            throw new DataLoaderException(e);
        }
    }

    private void lazyDataLoading() throws GitAPIException, IOException {
        if (!dataLoaded) {
            loadData();
            dataLoaded = true;
        }
    }

    private void loadData() throws IOException, GitAPIException {
        FileUtils.createDirectory(reportsPath);

        var endTime = releases.getLast().releaseDate();
        // lista di commit effettuati non oltre endTime e ordinati rispetto alla data di commit.
        var commits = StreamSupport
                .stream(git.log().call().spliterator(), false)
                .filter(c -> !GitUtils.getCommitDate(c).isAfter(endTime))
                .sorted(Comparator.comparingInt(RevCommit::getCommitTime))
                .toList();
        var lastCommitPerRelease = mapLastCommitPerRelease(commits);
        logger.log(Level.INFO, "total commits: {0}", commits.size());

        var filter = new PathPredicate();
        try (var df = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
            df.setRepository(git.getRepository());
            df.setDetectRenames(true);
            //  releaseChangeSet is the set of files which have been edited by at least one commit of a release.
            var progress = 0;
            for (var commit : commits) {
                ++progress;
                logProgress(progress, commits.size());

                var lastRelease = lastCommitPerRelease.get(commit.getName());

                // susceptible is the set of files touched by the current commit
                var diffList = getCommitDiffList(commit, df);
                var susceptible = getTouchedFiles(diffList);

                // if the current commit isn't the last one of a release, then we can skip the rest of the loop
                // if there aren't any files touched by the commit
                if (lastRelease == null && susceptible.isEmpty()) {
                    continue;
                }

                filter.setLastCommitPerRelease(lastRelease);
                filter.add(susceptible);
                loadData(commit, diffList, filter, df);

                if (lastRelease != null) {
                    logger.log(Level.INFO,
                            "A total of {0} has been read for release {1} (commit {2})",
                            new Object[] {
                                    classesPerRelease.stream().mapToInt(i -> i).sum(),
                                    lastRelease,
                                    commit.getName()
                    });
                    classesPerRelease.clear();
                    filter.reset();
                }
            }
        }
    }

    private List<DiffEntry> getCommitDiffList(RevCommit commit, DiffFormatter df) throws IOException {
        RevTree parent = null;
        if (commit.getParentCount() > 0) {
            parent = GitUtils.getParent(commit);
        }
        return df.scan(parent, commit.getTree());
    }

    private Map<String, Release> mapLastCommitPerRelease(List<RevCommit> sortedCommits) {
        var map = new HashMap<String, Release>();
        RevCommit last = null;
        var i = 0;
        for (var release : releases) {
            while (i < sortedCommits.size()) {
                var commit = sortedCommits.get(i);
                if (GitUtils.getCommitDate(commit).isAfter(release.releaseDate())) {
                    break;
                }
                last = commit;
                ++i;
            }
            if (last == null) {
                logger.log(Level.INFO, "no closing commit for release {0}", release);
            } else {
                map.put(last.getName(), release);
                logger.log(Level.INFO, "commit {0} is the last one for release {1}", new Object[] {last.getName().substring(0, 6), release});
            }
        }
        return map;
    }

    private void loadData(RevCommit commit,
                          List<DiffEntry> diffList,
                          Predicate<Path> predicate,
                          DiffFormatter df) throws IOException {
        var entropy = calculateEntropy(diffList, df);
        try (var walk = new TreeWalk(git.getRepository());
             var reader = git.getRepository().newObjectReader();
             var pmd = createPMDAnalysis(commit.getName())) {
            walk.addTree(commit.getTree());
            walk.setRecursive(true);

            handleRenames(diffList);

            var files = new ArrayList<ParseContext>();
            while (walk.next()) {
                var path = Path.of(walk.getPathString());
                if (predicate.test(path)) {
                    prepareFile(walk, reader, commit, pmd)
                            .ifPresent(files::add);
                }
            }
            currentChangeSetSize = files.size();
            var lists = files.parallelStream()
                            .map(this::parseClass)
                            .filter(c -> !c.isEmpty())
                            .toList();
            var list = lists.stream()
                    .flatMap(Collection::stream)
                    .toList();
            parseCommit(list, commit, diffList, entropy);
            pmd.performAnalysis();
            addProgramData(list);
        }
    }

    private double calculateEntropy(List<DiffEntry> diffList, DiffFormatter df) {
        if (diffList == null || diffList.isEmpty()) {
            return 0.0;
        }

        var changesPerFile = new HashMap<String, Integer>();
        var total = 0;
        for (var diff : diffList) {
            try {
                var fileChanges = df.toFileHeader(diff).toEditList()
                        .stream()
                        .mapToInt(e -> e.getLengthA() + e.getLengthB())
                        .sum();
                if (fileChanges > 0) {
                    changesPerFile.put(diff.getNewPath(), fileChanges);
                    total += fileChanges;
                }
            } catch (IOException e) {
                logger.log(Level.WARNING, "Cannot read edits for entry {}", diff.getNewPath());
            }
        }
        if (total == 0) {
            return 0.0;
        }

        var entropy = 0.0;
        for (var changes : changesPerFile.values()) {
            var p = (double) changes / total;
            entropy -= p * Math.log(p) / Math.log(2);
        }
        return entropy;
    }

    private Optional<ParseContext> prepareFile(
            TreeWalk walk,
            ObjectReader reader,
            RevCommit commit,
            PmdAnalysis pmd) throws IOException {
        var path = Path.of(walk.getPathString());
        var objectId = walk.getObjectId(0);
        var loader = reader.open(objectId);
        var content = new String(loader.getBytes(), StandardCharsets.UTF_8);
        if (AutoGenerated.isGenerated(content)) {
            return Optional.empty();
        }

        var textFile = TextFile
                .builderForCharSeq(content, FileId.fromPath(path), JavaLanguage.LANGUAGE_VERSION)
                .build();
        pmd.files().addFile(textFile);
        return Optional.of(new ParseContext(
                trackingId.getId(path),
                commit.getName(),
                projectPath,
                path,
                GitUtils.getCommitTime(commit),
                content));
    }


    private void handleRenames(List<DiffEntry> diffList) {
        for (var diff : diffList) {
            var oldPath = diff.getOldPath();
            var path = diff.getNewPath();
            if (path.endsWith(JAVA_FILE_EXT)
                && diff.getChangeType().equals(DiffEntry.ChangeType.RENAME)
                && !oldPath.equals("/dev/null")
                && !oldPath.equals(path)) {
                    trackingId.updateId(Path.of(oldPath), Path.of(path));
            }
        }
    }

    private void addProgramData(List<RevisionJavaClass> classList) {
        classes.addAll(classList);
        classesPerRelease.add(classList.size());
    }

    private PmdAnalysis createPMDAnalysis(String reportName) throws IOException {
        if (!reportName.endsWith(".csv")) {
            reportName += ".csv";
        }
        var reportPath = reportsPath.resolve(reportName);
        return PMDFactory.create(reportPath, pmdConfig);
    }

    private void logProgress(int progress, int total) {
        if (progress % 50 == 0) {
            logger.log(Level.INFO, () -> String.format("%d/%d (%.2f%%)", progress, total, (progress * 100.0) / total));
        }
    }

    /**
     * Prende l'insieme dei file modificati da un commit
     * @return una collezione di percorsi dei file toccati dal commit
     */
    private Set<String> getTouchedFiles(List<DiffEntry> diffList) {
        var touchedFiles = new HashSet<String>();
        for (var diff : diffList) {
            var path = diff.getNewPath();
            // Se il percorso del file modificato non è un file .java allora non è necessario analizzare
            // la modifica.
            if (path.endsWith(JAVA_FILE_EXT)) {
                touchedFiles.add(path);
            }
        }
        return touchedFiles;
    }

    private List<RevisionJavaClass> parseClass(ParseContext ctx) {
        try {
            var extractor = factory.create();
            var classList = extractor.run(ctx.path(), ctx.content());
            classList.forEach(c -> setContext(c, ctx));
            return classList;
        } catch (IOException e) {
            logger.log(Level.SEVERE, e, () -> "Error parsing file " + ctx.path());
            return List.of();
        }
    }

    private void setContext(RevisionJavaClass cls, ParseContext ctx) {
        cls.setTime(ctx.time());
        cls.setTrackingId(ctx.trackingId());
        cls.setCommit(ctx.commit());
        cls.getMetrics().setChangeSetSize(currentChangeSetSize);
    }

    private void parseCommit(List<RevisionJavaClass> classList,
                             RevCommit commit,
                             List<DiffEntry> diffList,
                             double entropy) {
        if (classList.isEmpty()) {
            return;
        }

        var index = classList.stream()
                .collect(Collectors.groupingBy(c -> c.getPath().toString()));
        var author = GitUtils.getAuthor(commit);
        var devExp = getDevExp(commit);
        for (var diff : diffList) {
            var path = diff.getNewPath();

            // Se il percorso del file modificato non è un file .java allora non è necessario analizzare
            // la modifica.
            if (path.endsWith(JAVA_FILE_EXT) && index.containsKey(path)) {
                var classesInFile = index.get(path);
                var fileId = classesInFile.getFirst().getTrackingId();
                var fileExp = getFileExp(commit, fileId);

                for (var c : index.get(path)) {
                    c.setCommitEntropy(entropy);
                    c.setDevExp(devExp);
                    c.setFileExp(fileExp);
                    author.ifPresent(c::setAuthor);
                }

                updateFileExp(commit, fileId, fileExp);
            }
        }
        updateDevExp(commit, devExp);
    }

    private int getDevExp(RevCommit commit) {
        var author = GitUtils.getAuthor(commit);
        if (author.isEmpty()) {
            return 0;
        }
        return developersExperience.getOrDefault(author.get(), 0);
    }

    private int getFileExp(RevCommit commit, long fileId) {
        var author = GitUtils.getAuthor(commit);
        if (author.isEmpty()) {
            return 0;
        }
        return fileDevelopersExperience
                .computeIfAbsent(fileId, unused -> new HashMap<>())
                .getOrDefault(author.get(), 0);
    }

    private void updateDevExp(RevCommit commit, int devExp) {
        var author = GitUtils.getAuthor(commit);
        if (author.isEmpty()) {
            return;
        }
        developersExperience.put(author.get(), devExp + 1);
    }

    private void updateFileExp(RevCommit commit, long fileId, int fileExp) {
        var author = GitUtils.getAuthor(commit);
        if (author.isEmpty()) {
            return;
        }

        fileDevelopersExperience
                .computeIfAbsent(fileId, unused -> new HashMap<>())
                .put(author.get(), fileExp + 1);
    }
}
