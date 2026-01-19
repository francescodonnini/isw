package io.github.francescodonnini.pipeline.inputs;

import io.github.francescodonnini.config.Settings;
import io.github.francescodonnini.data.IssueApi;
import io.github.francescodonnini.data.ReleaseApi;
import io.github.francescodonnini.utils.FileUtils;

import java.nio.file.Path;
import java.util.logging.Logger;

public class DataPipelineContext {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final IssueApi issueApi;
    private final ReleaseApi releaseApi;
    private final Path cache;
    private final Path data;
    private final Path sources;
    private final Path reports;
    private final boolean useCache;

    public DataPipelineContext(IssueApi issueApi, ReleaseApi releaseApi, Settings settings) {
        this.issueApi = issueApi;
        this.releaseApi = releaseApi;
        cache = Path.of(settings.getString("cachePath"));
        FileUtils.createDirectory(cache);
        data = Path.of(settings.getString("dataPath"));
        FileUtils.createDirectory(data);
        sources = Path.of(settings.getString("sourcesPath"));
        FileUtils.createDirectory(sources);
        reports = Path.of(settings.getString("pmdReportsPath"));
        FileUtils.createDirectory(reports);
        useCache = settings.getBool("useCache", false);
        logInfo();
    }

    private void logInfo() {
        String s = "Project Configuration\n" +
                "cachePath:   " + cache + "\n" +
                "dataPath:    " + data + "\n" +
                "sourcesPath: " + sources + "\n" +
                "reportsPath: " + reports + "\n";
        logger.info(s);
    }

    public IssueApi getIssueApi() {
        return issueApi;
    }

    public ReleaseApi getReleaseApi() {
        return releaseApi;
    }

    public Path getCache() {
        return cache;
    }

    public Path getData() {
        return data;
    }

    public Path getReports() {
        return reports;
    }

    public Path getSources() {
        return sources;
    }

    public boolean useCache() {
        return useCache;
    }
}
