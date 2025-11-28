package io.github.francescodonnini.data;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.github.francescodonnini.csv.CsvVersionApi;
import io.github.francescodonnini.jira.JiraVersionApi;
import io.github.francescodonnini.model.Version;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VersionRepository implements VersionApi {
    private final Logger logger = Logger.getLogger(VersionRepository.class.getName());
    private final JiraVersionApi remoteSource;
    private final CsvVersionApi localSource;
    private boolean useCache;

    public VersionRepository(JiraVersionApi remoteSource, CsvVersionApi localSource, boolean useCache) {
        this.remoteSource = remoteSource;
        this.localSource = localSource;
        this.useCache = useCache;
    }

    @Override
    public List<Version> getVersions(String projectName) {
        if (useCache) {
            return tryGetCache(projectName);
        } else {
            return tryGetFreshData(projectName);
        }
    }

    private List<Version> tryGetCache(String projectName) {
        try {
            var versions = localSource.getLocal(projectName);
            if (versions.isEmpty()) {
                versions = remoteSource.getVersions(projectName);
                saveLocal(versions, projectName);
            }
            return versions;
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, e.getMessage());
            return tryGetFreshData(projectName);
        }
    }

    private List<Version> tryGetFreshData(String projectName) {
        var data = remoteSource.getVersions(projectName);
        saveLocal(data, projectName);
        return data;
    }

    private void saveLocal(List<Version> versions, String projectName) {
        try {
            localSource.saveLocal(versions, projectName);
        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException | IOException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }
}
