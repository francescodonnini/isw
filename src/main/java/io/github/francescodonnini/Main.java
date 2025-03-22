package io.github.francescodonnini;

import io.github.francescodonnini.config.IniSettings;
import io.github.francescodonnini.csv.CsvIssueApi;
import io.github.francescodonnini.csv.CsvReleaseApi;
import io.github.francescodonnini.csv.CsvVersionApi;
import io.github.francescodonnini.data.*;
import io.github.francescodonnini.git.GitLog;
import io.github.francescodonnini.jira.JsonIssueApi;
import io.github.francescodonnini.jira.JsonReleaseApi;
import io.github.francescodonnini.jira.JsonVersionApi;
import io.github.francescodonnini.jira.RestApi;
import io.github.francescodonnini.sqlite.SQLiteClassApi;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws ConfigurationException, IOException, GitAPIException, SQLException {
        if (args.length != 2) {
            System.exit(-1);
        }
        var projectName = args[1];
        // regex "<project name>-d+" è presente in tutti i commit che chiudono un ticket di JIRA
        var pattern = "%s-\\d+".formatted(projectName);
        var settings = new IniSettings(args[0]);
        var restApi = new RestApi();
        var projectPath = Path.of(settings.getString("gitBasePath"), projectName.toLowerCase()).toString();
        var repositoryPath = Path.of(projectPath, ".git").toString();
        var git = new GitLog(repositoryPath);
        var path = Path.of(settings.getString("dataPath"), projectName).toString();
        var remoteVersionApi = new JsonVersionApi(projectName, restApi);
        var localVersionApi = new CsvVersionApi(Path.of(path, "versions.csv").toString());
        var versionApi = new VersionRepository(remoteVersionApi, localVersionApi);
        var remoteReleaseApi = new JsonReleaseApi(versionApi);
        var localReleaseApi = new CsvReleaseApi(Path.of(path, "releases.csv").toString());
        var releaseApi = new ReleaseRepository(remoteReleaseApi, localReleaseApi);
        var localIssueApi = new CsvIssueApi(Path.of(path, "issues.csv").toString(), releaseApi.getReleases(), git.getAll());
        var remoteIssueApi = new JsonIssueApi(projectName, pattern, git, restApi, releaseApi);
        var issueApi = new IssueRepository(remoteIssueApi, localIssueApi);
        var issues = issueApi.getIssues();
        var releases = releaseApi.getReleases();
        releases = releases.subList(0, releases.size() / 2);
        var factory = new JavaClassFactory(projectPath, releases);
        var localClassApi = new SQLiteClassApi(Path.of(path, "classes.db"), releases);
        var classApi = new JavaClassRepository(factory, localClassApi);
        var classes = classApi.getClasses();
        for (var clazz : classes.subList(classes.size() - 20, classes.size())) {
            System.out.printf("class=%s, release=%s%n", clazz.getPath(), clazz.getRelease().name());
        }
    }
}