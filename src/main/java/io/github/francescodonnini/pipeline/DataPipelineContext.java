package io.github.francescodonnini.pipeline;

import io.github.francescodonnini.config.Settings;
import io.github.francescodonnini.utils.FileUtils;

import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataPipelineContext {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final Api api;
    private final String projectName;
    private final double dropFactor;
    private final Path cache;
    private final Path data;
    private final Path sources;
    private final Path reports;
    private final boolean useCache;
    private final String proportion;

    public DataPipelineContext(Api api, String projectName, Settings settings) {
        this.api = api;
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
        logInfo();
    }

    private void logInfo() {
        logger.log(Level.INFO, "project name: {0}", projectName);
        logger.log(Level.INFO, "cache path:   {0}", cache);
        logger.log(Level.INFO, "data path:    {0}", data);
        logger.log(Level.INFO, "sources path: {0}", sources);
        logger.log(Level.INFO, "reports path: {0}", reports);
        logger.log(Level.INFO, "dropFactor:   {0}", dropFactor);
        logger.log(Level.INFO, "proportion:   {0}", proportion);
        logger.log(Level.INFO, "useCache:     {0}", useCache);
    }

    public Api getApi() {
        return api;
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
}
