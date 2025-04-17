package io.github.francescodonnini.data;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.github.francescodonnini.csv.CsvIssueApi;
import io.github.francescodonnini.jira.JsonIssueApi;
import io.github.francescodonnini.model.Issue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IssueRepository implements IssueApi {
    private final Logger logger = Logger.getLogger(IssueRepository.class.getName());
    private final JsonIssueApi remoteSource;
    private final CsvIssueApi localSource;
    private final boolean useCache;

    public IssueRepository(JsonIssueApi remoteSource, CsvIssueApi localSource, boolean useCache) {
        this.remoteSource = remoteSource;
        this.localSource = localSource;
        this.useCache = useCache;
    }

    @Override
    public List<Issue> getIssues() {
        if (useCache) {
            return tryGetCache();
        } else {
            return tryGetFreshData();
        }
    }

    private List<Issue> tryGetCache() {
        try {
            var issues = localSource.getLocal();
            if (issues.isEmpty()) {
                issues = remoteSource.getIssues();
                saveLocal(issues);
            }
            return issues;
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, e.getMessage());
            return tryGetFreshData();
        }
    }

    private List<Issue> tryGetFreshData() {
        var data = remoteSource.getIssues();
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
