package io.github.francescodonnini.pipeline;

import io.github.francescodonnini.config.Settings;
import io.github.francescodonnini.utils.FileUtils;

import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MLPipelineContext {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final String projectName;
    private final Path data;

    public MLPipelineContext(String projectName, Settings settings) {
        this.projectName = projectName;
        data = Path.of(settings.getString("dataPath"));
        FileUtils.createDirectory(data);
        logInfo();
    }

    private void logInfo() {
        logger.log(Level.INFO, "project name: {0}", projectName);
        logger.log(Level.INFO, "data path:    {0}", data);
    }

    public Path getData() {
        return data;
    }

    public String getProjectName() {
        return projectName;
    }
}
