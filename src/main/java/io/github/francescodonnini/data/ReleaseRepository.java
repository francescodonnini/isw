package io.github.francescodonnini.data;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.github.francescodonnini.csv.CsvReleaseApi;
import io.github.francescodonnini.jira.JsonReleaseApi;
import io.github.francescodonnini.model.Release;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReleaseRepository implements ReleaseApi {
    private final Logger logger = Logger.getLogger(ReleaseRepository.class.getName());
    private final JsonReleaseApi remoteDataSource;
    private final CsvReleaseApi localDataSource;

    public ReleaseRepository(JsonReleaseApi remoteDataSource, CsvReleaseApi localDataSource) {
        this.remoteDataSource = remoteDataSource;
        this.localDataSource = localDataSource;
    }

    @Override
    public List<Release> getReleases() {
        try {
            return localDataSource.getLocal();
        } catch (FileNotFoundException e) {
            var data = remoteDataSource.getReleases();
            saveLocal(data);
            return data;
        }
    }

    private void saveLocal(List<Release> releases) {
        try {
            localDataSource.saveLocal(releases);
        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException | IOException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }
}
