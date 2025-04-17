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
    private final JsonReleaseApi remoteSource;
    private final CsvReleaseApi localSource;
    private final boolean useCache;

    public ReleaseRepository(JsonReleaseApi remoteSource, CsvReleaseApi localSource, boolean useCache) {
        this.remoteSource = remoteSource;
        this.localSource = localSource;
        this.useCache = useCache;
    }

    @Override
    public List<Release> getReleases() {
        if (useCache) {
            return tryGetCache();
        } else {
            return tryGetFreshData();
        }
    }

    private List<Release> tryGetCache() {
        try {
            var releases = localSource.getLocal();
            if (releases.isEmpty()) {
                releases = remoteSource.getReleases();
                saveLocal(releases);
            }
            return releases;
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, e.getMessage());
            return tryGetFreshData();
        }
    }

    private List<Release> tryGetFreshData() {
        var data = remoteSource.getReleases();
        saveLocal(data);
        return data;
    }

    private void saveLocal(List<Release> releases) {
        try {
            localSource.saveLocal(releases);
        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException | IOException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }
}
