package io.github.francescodonnini;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.github.francescodonnini.collectors.DiffCollector;
import io.github.francescodonnini.collectors.ast.AbstractCounterFactoryImpl;
import io.github.francescodonnini.config.IniSettings;
import io.github.francescodonnini.csv.*;
import io.github.francescodonnini.data.*;
import io.github.francescodonnini.jira.JiraIssueApi;
import io.github.francescodonnini.jira.JiraReleaseApi;
import io.github.francescodonnini.jira.JiraVersionApi;
import io.github.francescodonnini.jira.RestApi;
import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.JavaClass;
import io.github.francescodonnini.model.JavaMethod;
import io.github.francescodonnini.model.Release;
import io.github.francescodonnini.proportion.Incremental;
import io.github.francescodonnini.utils.FileUtils;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static boolean useCache;
    private static double dropFactor;
    private static String projectName ;
    private static Path dataPath;
    private static String projectPath;
    private static String reportsPath;
    private static final RestApi restApi = new RestApi();
    private static List<Release> releases;
    private static List<JavaClass> classes;
    private static List<JavaMethod> methods;
    private static List<Issue> issues;

    private static void loadReleases() {
        var localVersionApi = new CsvVersionApi(dataPath.resolve("versions.csv").toString());
        var jiraVersionApi = new JiraVersionApi(projectName, restApi);
        var versionApi = new VersionRepository(jiraVersionApi, localVersionApi, false);
        var jiraReleaseApi = new JiraReleaseApi(versionApi);
        var releaseApi = new ReleaseRepository(jiraReleaseApi, new CsvReleaseApi(dataPath.resolve("releases.csv").toString()), false);
        var allReleases = releaseApi.getReleases();
        if (dropFactor > 1 || dropFactor < 0) {
            throw new IllegalArgumentException("drop factor must be between 0 and 1");
        }
        var remainingReleases = (1 - dropFactor) * allReleases.size();
        releases = allReleases.subList(0, (int) remainingReleases);
        logger.log(Level.INFO, () -> String.format("selected %d/%d releases", releases.size(), allReleases.size()));
    }

    private static void loadProgramData() throws IOException {
        if (useCache) {
            var localClassApi = new CsvJavaClassApi(dataPath.resolve("classes.csv").toString());
            classes = localClassApi.getLocal();
            var localMethodApi = new CsvJavaMethodApi(dataPath.resolve("methods.csv").toString(), classes);
            methods = localMethodApi.getLocal();
        } else {
            var dataLoader = new DataLoaderImpl(projectPath, new AbstractCounterFactoryImpl(), reportsPath, releases);
            var localClassApi = new CsvJavaClassApi(dataPath.resolve("classes.csv").toString());
            var classApi = new JavaClassRepository(dataLoader, localClassApi, useCache);
            classes = classApi.getClasses();
            var localMethodApi = new CsvJavaMethodApi(dataPath.resolve("methods.csv").toString(), classes);
            var methodApi = new JavaMethodRepository(dataLoader, localMethodApi, useCache);
            methods = methodApi.getMethods();
        }
    }

    private static void linkCodeSmells() throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        var localMethodApi = new CsvJavaMethodApi(dataPath.resolve("stinky-methods.csv").toString(), classes);
        if (!useCache) {
            var linker = new CsvSmellLinker(reportsPath);
            linker.link(classes);
            localMethodApi.saveLocal(methods, dataPath.resolve("stinky-methods.csv").toString());
        }
        methods = localMethodApi.getLocal();
    }

    private static void collectChangeMetrics() throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        var localMethodApi = new CsvJavaMethodApi(dataPath.resolve("methods-cm.csv").toString(), classes);
        if (!useCache) {
            var collector = new DiffCollector(releases, methods);
            methods = collector.collect();
            localMethodApi.saveLocal(methods, dataPath.resolve("methods-cm.csv").toString());
        }
        methods = localMethodApi.getLocal();
    }

    private static void doProportion() throws GitAPIException, IOException {
        var commits = getCommits(projectPath);
        var pattern = "%s-\\d+".formatted(projectName);
        var jiraIssueApi = new JiraIssueApi(projectName, pattern, restApi, releases, commits);
        var localIssueApi = new CsvIssueApi(dataPath.resolve("issues.csv").toString(), releases, commits);
        var issueApi = new IssueRepository(jiraIssueApi, localIssueApi, useCache);
        issues = issueApi.getIssues();
        var proportion = new Incremental(issues, releases);
        proportion.makeLabels();
    }

    private static List<RevCommit> getCommits(String projectPath) throws IOException, GitAPIException {
        try (var git = createGit(projectPath)) {
            var commits = new ArrayList<RevCommit>();
            git.log().call().forEach(commits::add);
            return commits;
        }
    }

    private static Git createGit(String projectPath) throws IOException {
        var gitPath = Path.of(projectPath, ".git");
        try (var repository = new FileRepositoryBuilder()
                .setGitDir(gitPath.toFile())
                .build()) {
            return new Git(repository);
        }
    }

    private static void doLabelling() throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        try (var git = createGit(projectPath)) {
            var labelMaker = new LabelMakerImpl(git, issues, methods, releases);
            methods = labelMaker.makeLabels();
            var localMethodApi = new CsvJavaMethodApi(dataPath.resolve("labeled-methods.csv").toString(), classes);
            localMethodApi.saveLocal(methods, dataPath.resolve("labeled-methods.csv").toString());
        }
    }

    public static void main(String[] args) throws ConfigurationException, IOException, GitAPIException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        init(args);
        completeWorkflow();
    }

    private static void init(String[] args) throws ConfigurationException, IOException {
        if (args.length != 2) {
            System.exit(-1);
        }
        projectName = args[1].toUpperCase();
        // regex "<project name>-d+" ("%s-\\d+") è presente in tutti i commit che chiudono un ticket di JIRA
        var settings = new IniSettings(args[0]);
        useCache = false;
        dropFactor = settings.getDouble("dropFactor");
        reportsPath = Path.of(settings.getString("pmdReportsPath"), projectName).toString();
        FileUtils.createDirectory(reportsPath);
        projectPath = Path.of(settings.getString("gitBasePath"), projectName.toLowerCase()).toString();
        dataPath = Path.of(settings.getString("dataPath"), projectName);
        logger.log(Level.INFO, "project name: {0}", projectName);
        logger.log(Level.INFO, "project path: {0}", projectPath);
        logger.log(Level.INFO, "reports path: {0}", reportsPath);
        logger.log(Level.INFO, () -> String.format("dataset path: %s (use cache %s)", reportsPath, useCache));
        logger.log(Level.INFO, "drop  factor: {0}", dropFactor);
    }

    private static void completeWorkflow() throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, GitAPIException {
        loadReleases();
        loadProgramData();
        linkCodeSmells();
        collectChangeMetrics();
        doProportion();
        doLabelling();
    }
}
