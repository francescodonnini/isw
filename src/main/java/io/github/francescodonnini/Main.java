package io.github.francescodonnini;

import io.github.francescodonnini.config.IniSettings;
import io.github.francescodonnini.csv.CsvIssueApi;
import io.github.francescodonnini.csv.CsvReleaseApi;
import io.github.francescodonnini.csv.CsvVersionApi;
import io.github.francescodonnini.data.IssueRepository;
import io.github.francescodonnini.data.ReleaseRepository;
import io.github.francescodonnini.data.VersionRepository;
import io.github.francescodonnini.git.GitLog;
import io.github.francescodonnini.jira.JsonIssueApi;
import io.github.francescodonnini.jira.JsonReleaseApi;
import io.github.francescodonnini.jira.JsonVersionApi;
import io.github.francescodonnini.jira.RestApi;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws ConfigurationException, IOException, GitAPIException {
        if (args.length != 1) {
            System.exit(1);
        }
        var projectName = "SYNCOPE";
        // regex "<project name>-d+" è presente in tutti i commit che chiudono un ticket di JIRA
        var pattern = "%s-\\d+".formatted(projectName);
        var settings = new IniSettings(args[0]);
        var restApi = new RestApi();
        var repositoryPath = Path.of(settings.getString("gitBasePath"), projectName.toLowerCase(), ".git").toString();
        var git = new GitLog(repositoryPath);
        var path = settings.getString("dataPath").formatted(projectName);
        var remoteVersionApi = new JsonVersionApi(projectName, restApi);
        var localVersionApi = new CsvVersionApi(path + "/versions.csv");
        var versionApi = new VersionRepository(remoteVersionApi, localVersionApi);
        var remoteReleaseApi = new JsonReleaseApi(versionApi);
        var localReleaseApi = new CsvReleaseApi(path + "/releases.csv");
        var releaseApi = new ReleaseRepository(remoteReleaseApi, localReleaseApi);
        var localIssueApi = new CsvIssueApi(path + "/issues.csv", releaseApi.getReleases(), git.getAll());
        var remoteIssueApi = new JsonIssueApi(projectName, pattern, git, restApi, releaseApi);
        var issueApi = new IssueRepository(remoteIssueApi, localIssueApi);
        var issues = issueApi.getIssues();
        var releases = releaseApi.getReleases();
        releases = releases.subList(0, releases.size() / 2);
        for (var i : issues) {
            System.out.printf("%s %s%n", i.project(), i.key());
        }
    }
}