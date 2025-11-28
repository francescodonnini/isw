package io.github.francescodonnini.data;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.github.francescodonnini.csv.CsvReleaseApi;
import io.github.francescodonnini.jira.JiraReleaseApi;
import io.github.francescodonnini.model.Release;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReleaseRepository implements ReleaseApi {
    private final Logger logger = Logger.getLogger(ReleaseRepository.class.getName());
    private final JiraReleaseApi remoteSource;
    private final CsvReleaseApi localSource;
    private final boolean useCache;

    public ReleaseRepository(JiraReleaseApi remoteSource, CsvReleaseApi localSource, boolean useCache) {
        this.remoteSource = remoteSource;
        this.localSource = localSource;
        this.useCache = useCache;
    }

    @Override
    public List<Release> getReleases(String projectName) {
        if (useCache) {
            return tryGetCache(projectName);
        } else {
            return tryGetFreshData(projectName);
        }
    }

    private List<Release> tryGetCache(String projectName) {
        try {
            var releases = localSource.getLocal(projectName);
            if (releases.isEmpty()) {
                releases = remoteSource.getReleases(projectName);
                saveLocal(releases, projectName);
            }
            return releases;
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, e.getMessage());
            return tryGetFreshData(projectName);
        }
    }

    private List<Release> tryGetFreshData(String projectName) {
        var data = remoteSource.getReleases(projectName);
        data.sort(Comparator.comparing(Release::releaseDate));
        saveLocal(data, projectName);
        return data;
    }

    private void saveLocal(List<Release> releases, String projectName) {
        try {
            localSource.saveLocal(releases, projectName);
        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException | IOException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }
}
