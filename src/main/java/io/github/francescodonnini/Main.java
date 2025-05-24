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

public class Main {
    private static String dataPath;
    private static RestApi restApi = new RestApi();

    public static void main(String[] args) throws ConfigurationException, IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, GitAPIException {
        if (args.length != 2) {
            System.exit(-1);
        }
        var projectName = args[1].toUpperCase();
        // regex "<project name>-d+" ("%s-\\d+") è presente in tutti i commit che chiudono un ticket di JIRA
        var settings = new IniSettings(args[0]);
        var useCache = false;
        var reportsPath = Path.of(settings.getString("pmdReportsPath"), projectName).toString();
        FileUtils.createDirectory(reportsPath.toString());
        var sourcePath = Path.of(settings.getString("gitBasePath"), projectName.toLowerCase());
        try (var git = createGit(Path.of(settings.getString("gitBasePath"), projectName.toLowerCase()))) {
            var restApi = new RestApi();
            var projectPath = Path.of(settings.getString("gitBasePath"), projectName.toLowerCase()).toString();
            dataPath = Path.of(settings.getString("dataPath"), projectName).toString();
            var releases = getReleases(projectName, 0.5, useCache);
            var factory = new DataLoaderImpl(projectPath, new AbstractCounterFactoryImpl(), releases.getLast().releaseDate(), reportsPath.toString());
            var localClassApi = new CsvJavaClassApi(Path.of(dataPath, "classes.csv").toString());
            var classApi = new JavaClassRepository(factory, localClassApi, true);
            var classes = classApi.getClasses();
            var localMethodApi = new CsvJavaMethodApi(Path.of(dataPath, "methods.csv").toString(), classes);
            var methods = localMethodApi.getLocal();
            System.out.printf("retrieved %d methods%n", methods.size());
            var codeSmellLinker = new CsvSmellLinker(reportsPath.toString());
            codeSmellLinker.link(classes);
            var diff = new DiffCollector(releases, methods);
            var diffed = diff.collect();
            var commits = new ArrayList<RevCommit>();
            git.log().call().forEach(commits::add);
            var localIssueApi = new CsvIssueApi(Path.of(dataPath, "issues.csv").toString(), releases, commits);
            var pattern = "%s-\\d+".formatted(projectName);
            var remoteIssueApi = new JiraIssueApi(projectName, pattern, restApi, releases, commits);
            var issueApi = new IssueRepository(remoteIssueApi, localIssueApi, useCache);
            var issues = issueApi.getIssues();
            System.out.printf("retrieved %d issues%n", issues.size());
            System.out.printf("issues with affectedVersions" + issues.stream().filter(i -> i.affectedVersions().size() > 0).count());
            var proportion = new Incremental(issues, releases);
            var finalIssues = proportion.makeLabels();
            localIssueApi.saveLocal(finalIssues, Path.of(dataPath, "issues-with-prop.csv").toString());
            var labelMaker = new LabelMakerImpl(git, finalIssues, diffed, releases);
            var labeledMethods = labelMaker.makeLabels();
            localMethodApi.saveLocal(labeledMethods, Path.of(dataPath, "lbl-with-prop-methods.csv").toString());
            System.out.println("number of buggy methods: " + labeledMethods.stream().filter(m -> m.isBuggy()).count());
        }
    }

    private static Path createReportsPath(String reportsBaseDir) {
        return Path.of(reportsBaseDir, String.valueOf(System.currentTimeMillis()));
    }

    private static Git createGit(Path projectPath) throws IOException {
        var gitPath = projectPath.resolve(".git");
        try (var repository = new FileRepositoryBuilder()
                .setGitDir(gitPath.toFile())
                .build()) {
            return new Git(repository);
        }
    }

    private static List<Release> getReleases(String projectName, double dropFactor, boolean useCache) {
        var releaseApi = getReleaseApi(projectName, restApi, dataPath, useCache);
        var releases = releaseApi.getReleases();
        var lastRelease = Math.min(((int) (releases.size() * dropFactor)) + 1, releases.size());
        return releases.subList(0, lastRelease + 1);
    }

    private static ReleaseApi getReleaseApi(String projectName, RestApi restApi, String localDataPath, boolean useCache) {
        var versionApi = getVersionApi(projectName, restApi, localDataPath, useCache);
        var remoteReleaseApi = new JiraReleaseApi(versionApi);
        var localReleaseApi = new CsvReleaseApi(Path.of(localDataPath, "releases.csv").toString());
        return new ReleaseRepository(remoteReleaseApi, localReleaseApi, useCache);
    }

    private static VersionApi getVersionApi(String projectName, RestApi restApi, String localDataPath, boolean useCache) {
        var remoteVersionApi = new JiraVersionApi(projectName, restApi);
        var localVersionApi = new CsvVersionApi(Path.of(localDataPath, "versions.csv").toString());
        return new VersionRepository(remoteVersionApi, localVersionApi, useCache);
    }

    private static List<RevCommit> getCommits(String projectPath) throws IOException, GitAPIException {
        var gitPath = Path.of(projectPath, ".git");
        try (var repository = new FileRepositoryBuilder()
                .setGitDir(gitPath.toFile())
                .build();
            var git = new Git(repository);) {
            var commits = new ArrayList<RevCommit>();
            git.log().call().forEach(commits::add);
            return commits;
        }
    }
}