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
import io.github.francescodonnini.workflow.Node;
import io.github.francescodonnini.workflow.WorkflowBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.FileNotFoundException;
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
    private static CsvJavaMethodApi localMethodApi;
    private static List<Release> releases;
    private static List<JavaClass> classes;
    private static List<JavaMethod> methods;
    private static List<Issue> issues;

    private static boolean loadReleases() {
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
        return true;
    }

    private static boolean loadProgramData() {
        try {
            var localClassApi = new CsvJavaClassApi(dataPath.resolve("classes.csv").toString());
            classes = localClassApi.getLocal();
            localMethodApi = new CsvJavaMethodApi(dataPath.resolve("methods.csv").toString(), classes);
            methods = localMethodApi.getLocal();
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "@loadProgramData: got error {}", e.getMessage());
            return false;
        }
    }

    private static boolean loadClasses() {
        try {
            var localClassApi = new CsvJavaClassApi(dataPath.resolve("classes-x.csv").toString());
            classes = localClassApi.getLocal();
            return true;
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "@loadClasses: got error {}", e.getMessage());
            return false;
        }
    }

    private static boolean collectProgramData() {
        try {
            var dataLoader = new DataLoaderImpl(projectPath, new AbstractCounterFactoryImpl(), reportsPath, releases);
            var localClassApi = new CsvJavaClassApi(dataPath.resolve("classes-x.csv").toString());
            var classApi = new JavaClassRepository(dataLoader, localClassApi, useCache);
            classes = classApi.getClasses();
            localMethodApi = new CsvJavaMethodApi(dataPath.resolve("methods-x.csv").toString(), classes);
            var methodApi = new JavaMethodRepository(dataLoader, localMethodApi, useCache);
            methods = methodApi.getMethods();
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "@loadProgramData: got error {}", e.getMessage());
            return false;
        }
    }

    private static boolean linkCodeSmells() {
        var linker = new CsvSmellLinker(reportsPath);
        linker.link(classes);
        try {
            localMethodApi.saveLocal(methods, dataPath.resolve("methods-x-with-smells.csv").toString());
            return true;
        } catch (IOException | CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
            logger.log(Level.SEVERE, "@linkCodeSmells: got error {}", e.getMessage());
            return false;
        }
    }

    private static boolean loadStinkyMethods() {
        try {
            var localMethodApi = new CsvJavaMethodApi(dataPath.resolve("methods-x-with-smells.csv").toString(), classes);
            methods = localMethodApi.getLocal();
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "@loadStinkyMethods: got error {}", e.getMessage());
            return false;
        }
    }

    private static boolean collectChangeMetrics() {
        var collector = new DiffCollector(releases, methods);
        methods = collector.collect();
        try {
            localMethodApi.saveLocal(methods, dataPath.resolve("methods-x-diffed.csv").toString());
            return true;
        } catch (IOException | CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
            logger.log(Level.SEVERE, "@collectChangeMetrics: got error {}", e.getMessage());
            return false;
        }
    }

    private static boolean doProportion() {
        try {
            var commits = getCommits(projectPath);
            var pattern = "%s-\\d+".formatted(projectName);
            var jiraIssueApi = new JiraIssueApi(projectName, pattern, restApi, releases, commits);
            var localIssueApi = new CsvIssueApi(dataPath.resolve("issues.csv").toString(), releases, commits);
            var issueApi = new IssueRepository(jiraIssueApi, localIssueApi, useCache);
            issues = issueApi.getIssues();
            var proportion = new Incremental(issues, releases);
            proportion.makeLabels();
            return true;
        } catch (IOException | GitAPIException e) {
            logger.log(Level.SEVERE, "@doProportion: got error {}", e.getMessage());
            return false;
        }
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

    private static boolean doLabelling() {
        try (var git = createGit(projectPath)) {
            var labelMaker = new LabelMakerImpl(git, issues, methods, releases);
            methods = labelMaker.makeLabels();
            localMethodApi.saveLocal(methods, dataPath.resolve("methods-x-labeled.csv").toString());
            return true;
        } catch (IOException | CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
            logger.log(Level.SEVERE, "@doLabelling: got error {}", e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) throws ConfigurationException, IOException {
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
    }

    private static void completeWorkflow() {
        new WorkflowBuilder()
                .addNode(Node.create("1", "loadReleases", Main::loadReleases))
                .addNode(Node.create("2", "loadClasses", List.of("1"), Main::loadClasses))
                .addNode(Node.create("3", "loadStinkyMethods", List.of("2"), Main::loadStinkyMethods))
                .addNode(Node.create("4", "collectChangeMetrics", List.of("3"), Main::collectChangeMetrics))
                // .addNode(Node.create("5", "doProportion", List.of("4"), Main::doProportion))
                // .addNode(Node.create("6", "doLabelling", List.of("5"), Main::doLabelling))
                .build()
                .execute();
    }
}
