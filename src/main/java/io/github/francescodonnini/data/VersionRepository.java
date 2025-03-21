package io.github.francescodonnini.data;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.github.francescodonnini.csv.CsvVersionApi;
import io.github.francescodonnini.jira.JsonVersionApi;
import io.github.francescodonnini.model.Version;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VersionRepository implements VersionApi {
    private final Logger logger = Logger.getLogger(VersionRepository.class.getName());
    private final JsonVersionApi remoteDataSource;
    private final CsvVersionApi localDataSource;

    public VersionRepository(JsonVersionApi remoteDataSource, CsvVersionApi localDataSource) {
        this.remoteDataSource = remoteDataSource;
        this.localDataSource = localDataSource;
    }

    @Override
    public List<Version> getVersions() {
        try {
            return localDataSource.getLocal();
        } catch (FileNotFoundException e) {
            var data = remoteDataSource.getRemoteVersions();
            saveLocal(data);
            return data;
        }
    }

    private void saveLocal(List<Version> versions) {
        try {
            localDataSource.saveLocal(versions);
        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException | IOException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }
}
