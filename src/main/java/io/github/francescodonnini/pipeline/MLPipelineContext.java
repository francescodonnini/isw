package io.github.francescodonnini.pipeline;

import io.github.francescodonnini.config.IniSettings;
import io.github.francescodonnini.utils.FileUtils;

import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MLPipelineContext {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final String projectName;
    private final String labellingMethod;
    private final double trainingTestSplit;
    private final Path data;

    public MLPipelineContext(String projectName, String labellingMethod, double trainingTestSplit, String dataPath) {
        this.projectName = projectName;
        this.labellingMethod = labellingMethod;
        this.trainingTestSplit = trainingTestSplit;
        data = Path.of(dataPath);
        FileUtils.createDirectory(data);
        logInfo();
    }

    public MLPipelineContext(String projectName, IniSettings settings) {
        this(projectName, settings.getString("proportion"), settings.getDouble("trainingTestSplit", 0.8), settings.getString("dataPath"));
    }

    private void logInfo() {
        var s = new StringBuilder()
                .append("\n")
                .append("project name:        ").append(projectName).append("\n")
                .append("labelling method:    ").append(labellingMethod).append("\n")
                .append("training test split: ").append(trainingTestSplit).append("\n")
                .append("data path:           ").append(data.toString());
        logger.log(Level.INFO, "{0}", s.toString());
    }

    public Path getData() {
        return data;
    }

    public String getLabellingMethod() {
        return labellingMethod;
    }

    public String getProjectName() {
        return projectName;
    }

    public double getTrainingTestSplit() {
        return trainingTestSplit;
    }
}
