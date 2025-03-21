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
    private final JsonIssueApi remoteDataSource;
    private final CsvIssueApi localDataSource;

    public IssueRepository(JsonIssueApi remoteDataSource, CsvIssueApi localDataSource) {
        this.remoteDataSource = remoteDataSource;
        this.localDataSource = localDataSource;
    }

    @Override
    public List<Issue> getIssues() {
        try {
            return localDataSource.getLocal();
        } catch (FileNotFoundException e) {
            var data = remoteDataSource.getRemoteIssues();
            saveLocal(data);
            return data;
        }
    }

    private void saveLocal(List<Issue> issues) {
        try {
            localDataSource.saveLocal(issues);
        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException | IOException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }
}
