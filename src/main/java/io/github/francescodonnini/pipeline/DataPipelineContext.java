package io.github.francescodonnini.pipeline;

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
    private final String projectName;
    private final double dropFactor;
    private final Path cache;
    private final Path data;
    private final Path sources;
    private final Path reports;
    private final boolean useCache;
    private final String proportion;
    private final double movingWindowPercentage;

    public DataPipelineContext(IssueApi issueApi, ReleaseApi releaseApi, String projectName, Settings settings) {
        this.issueApi = issueApi;
        this.releaseApi = releaseApi;
        this.projectName = projectName;
        dropFactor = settings.getDouble("%s_dropFactor".formatted(projectName.toLowerCase()));
        cache = Path.of(settings.getString("cachePath"));
        FileUtils.createDirectory(cache);
        data = Path.of(settings.getString("dataPath"));
        FileUtils.createDirectory(data);
        sources = Path.of(settings.getString("sourcesPath"));
        FileUtils.createDirectory(sources);
        reports = Path.of(settings.getString("pmdReportsPath"));
        FileUtils.createDirectory(reports);
        useCache = settings.getBool("useCache", false);
        proportion = settings.getString("proportion");
        movingWindowPercentage = settings.getDouble("%s_movingWindowPercentage".formatted(projectName.toLowerCase()), 0.01);
        logInfo();
    }

    private void logInfo() {
        var s = new StringBuilder()
                .append("Project Configuration\n")
                .append("projectName: ").append(projectName).append("\n")
                .append("cachePath:   ").append(cache).append("\n")
                .append("dataPath:    ").append(data).append("\n")
                .append("sourcesPath: ").append(sources).append("\n")
                .append("reportsPath: ").append(reports).append("\n")
                .append("proportion:  ").append(proportion);
        if (proportion.equals("MovingWindow")) {
            s.append(" (windowP=%f)".formatted(movingWindowPercentage));
        }
        s.append("\n");
        logger.info(s.toString());
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

    public double getDropFactor() {
        return dropFactor;
    }

    public String getProjectName() {
        return projectName;
    }

    public Path getReports() {
        return reports;
    }

    public Path getSources() {
        return sources;
    }

    public String getProportion() {
        return proportion;
    }

    public boolean useCache() {
        return useCache;
    }

    public Double getMovingWindowPercentage() {
        return movingWindowPercentage;
    }
}
