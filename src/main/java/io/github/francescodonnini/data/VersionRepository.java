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
    private final boolean useCache;

    public VersionRepository(JiraVersionApi remoteSource, CsvVersionApi localSource, boolean useCache) {
        this.remoteSource = remoteSource;
        this.localSource = localSource;
        this.useCache = useCache;
    }

    @Override
    public List<Version> getVersions() {
        if (useCache) {
            return tryGetCache();
        } else {
            return tryGetFreshData();
        }
    }

    private List<Version> tryGetCache() {
        try {
            var versions = localSource.getLocal();
            if (versions.isEmpty()) {
                versions = remoteSource.getVersions();
                saveLocal(versions);
            }
            return versions;
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, e.getMessage());
            return tryGetFreshData();
        }
    }

    private List<Version> tryGetFreshData() {
        var data = remoteSource.getVersions();
        saveLocal(data);
        return data;
    }

    private void saveLocal(List<Version> versions) {
        try {
            localSource.saveLocal(versions);
        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException | IOException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }
}
