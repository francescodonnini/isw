package io.github.francescodonnini.data;

import io.github.francescodonnini.collectors.ast.*;
import io.github.francescodonnini.data.pmd.CPDConsumer;
import io.github.francescodonnini.data.pmd.CPDFactory;
import io.github.francescodonnini.data.pmd.JavaLanguage;
import io.github.francescodonnini.data.pmd.PMDFactory;
import io.github.francescodonnini.model.JavaClass;
import io.github.francescodonnini.model.JavaMethod;
import io.github.francescodonnini.model.Release;
import io.github.francescodonnini.utils.FileUtils;
import io.github.francescodonnini.utils.GitUtils;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.cpd.CPDConfiguration;
import net.sourceforge.pmd.cpd.CpdAnalysis;
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

public class DataLoaderImpl implements ClassDataLoader, MethodDataLoader {
    private static final String JAVA_FILE_EXT = ".java";
    private final Logger logger = Logger.getLogger(DataLoaderImpl.class.getName());
    // projectPath è il percorso delle repository dove leggere i file da cui creare le entry per il dataset.
    private final Path  projectPath;
    private final Path reportsPath;
    private final Git git;
    private final TrackingIdService trackingId = new TrackingIdService();
    private final PMDConfiguration pmdConfig;
    private final CPDConfiguration cpdConfig;
    private final JavaMethodExtractorFactory factory;
    private final List<JavaClass> classes = new ArrayList<>();
    private final List<JavaMethod> methods = new ArrayList<>();
    private boolean dataLoaded = false;
    private final List<Release> releases;
    private final List<Integer> methodsPerRelease = new ArrayList<>();

    public DataLoaderImpl(
            JavaMethodExtractorFactory factory,
            List<Release> releases,
            Path projectPath,
            Path reportsPath) throws IOException {
        this.factory = factory;
        this.projectPath = projectPath;
        this.reportsPath = reportsPath;
        this.releases = releases;
        this.git = GitUtils.createGit(projectPath);
        pmdConfig = PMDFactory.create();
        cpdConfig = CPDFactory.create();
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
                loadData(commit, diffList, filter);

                if (lastRelease != null) {
                    logger.log(Level.INFO,
                            "A total of {0} has been read for release {1} (commit {2})",
                            new Object[] {
                                    methodsPerRelease.stream().mapToInt(i -> i).sum(),
                                    lastRelease,
                                    commit.getName()
                    });
                    methodsPerRelease.clear();
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
        var i = 0;
        for (var release : releases) {
            RevCommit last = null;
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

    private void loadData(RevCommit commit, List<DiffEntry> diffList, Predicate<Path> predicate) throws IOException {
        try (var walk = new TreeWalk(git.getRepository());
             var reader = git.getRepository().newObjectReader();
             var pmd = createPMDAnalysis(commit.getName());
             var cpd = CpdAnalysis.create(cpdConfig)) {
            walk.addTree(commit.getTree());
            walk.setRecursive(true);

            handleRenames(diffList);

            var files = new ArrayList<ParseContext>();
            while (walk.next()) {
                var path = Path.of(walk.getPathString());
                if (predicate.test(path)) {
                    prepareFile(walk, reader, commit, pmd, cpd)
                            .ifPresent(files::add);
                }
            }
            var lists = files.parallelStream()
                            .map(this::parseClass)
                            .filter(c -> !c.isEmpty())
                            .toList();
            var list = lists.stream()
                    .flatMap(Collection::stream)
                    .toList();
            parseCommit(list, commit, diffList);
            pmd.performAnalysis();
            cpd.performAnalysis(new CPDConsumer(list));
            addProgramData(list);
        }
    }

    private Optional<ParseContext> prepareFile(
            TreeWalk walk,
            ObjectReader reader,
            RevCommit commit,
            PmdAnalysis pmd,
            CpdAnalysis cpd) throws IOException {
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
        cpd.files().addFile(textFile);
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

    private void addProgramData(List<JavaClass> classList) {
        classList.stream()
                .filter(c -> !c.getMethods().isEmpty())
                .forEach(c -> {
            var methodList = c.getMethods();
            this.methods.addAll(methodList);
            classes.add(c);
            methodsPerRelease.add(methodList.size());
        });
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

    private List<JavaClass> parseClass(ParseContext ctx) {
        try {
            var extractor = factory.create();
            extractor.parse(ctx);
            return extractor.getClasses();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e, () -> "Error parsing file " + ctx.path());
            return List.of();
        }
    }

    private void parseCommit(List<JavaClass> classList, RevCommit commit, List<DiffEntry> diffList) {
        if (classList.isEmpty()) {
            return;
        }
        var index = classList.stream()
                .collect(Collectors.groupingBy(c -> c.getPath().toString()));
        for (var diff : diffList) {
            var path = diff.getNewPath();
            // Se il percorso del file modificato non è un file .java allora non è necessario analizzare
            // la modifica.
            if (path.endsWith(JAVA_FILE_EXT) && index.containsKey(path)) {
                var author = GitUtils.getAuthor(commit);
                for (var c : index.get(path)) {
                    author.ifPresent(c::setAuthor);
                }
            }
        }
    }
}
