package io.github.francescodonnini.data;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.github.francescodonnini.csv.CsvIssueApi;
import io.github.francescodonnini.jira.JiraIssueApi;
import io.github.francescodonnini.model.Issue;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IssueRepository implements IssueApi {
    private final Logger logger = Logger.getLogger(IssueRepository.class.getName());
    private final JiraIssueApi remoteSource;
    private final CsvIssueApi localSource;
    private final boolean useCache;

    public IssueRepository(JiraIssueApi remoteSource, CsvIssueApi localSource, boolean useCache) {
        this.remoteSource = remoteSource;
        this.localSource = localSource;
        this.useCache = useCache;
    }

    @Override
    public List<Issue> getIssues(String projectName) {
        if (useCache) {
            return tryGetCache(projectName);
        } else {
            return tryGetFreshData(projectName);
        }
    }

    private List<Issue> tryGetCache(String projectName) {
        try {
            var issues = localSource.getLocal(projectName);
            if (issues.isEmpty()) {
                issues = remoteSource.getIssues(projectName);
                saveLocal(issues);
            }
            return issues;
        } catch (IOException | GitAPIException e) {
            logger.log(Level.SEVERE, e.getMessage());
            return tryGetFreshData(projectName);
        }
    }

    private List<Issue> tryGetFreshData(String projectName) {
        var data = remoteSource.getIssues(projectName);
        saveLocal(data);
        return data;
    }

    private void saveLocal(List<Issue> issues) {
        try {
            localSource.saveLocal(issues);
        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException | IOException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }
}
