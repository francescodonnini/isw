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
    private final String model;
    private final Path data;
    private final Path results;

    public MLPipelineContext(String projectName, String labellingMethod, double trainingTestSplit, String model, String dataPath) {
        this.projectName = projectName;
        this.labellingMethod = labellingMethod;
        this.trainingTestSplit = trainingTestSplit;
        this.model = model;
        data = Path.of(dataPath);
        FileUtils.createDirectory(data);
        results = data
                .resolve(projectName)
                .resolve("results");
        FileUtils.createDirectory(results);
        logInfo();
    }

    public MLPipelineContext(String projectName, IniSettings settings) {
        this(projectName,
            settings.getString("proportion"),
            settings.getDouble("trainingTestSplit", 0.8),
            settings.getString("model"),
            settings.getString("dataPath"));
    }

    private void logInfo() {
        var s = new StringBuilder()
                .append("\n")
                .append("project name:        ").append(projectName).append("\n")
                .append("labelling method:    ").append(labellingMethod).append("\n")
                .append("training test split: ").append(trainingTestSplit).append("\n")
                .append("data path:           ").append(data.toString()).append("\n")
                .append("results path:        ").append(results.toString()).append("\n");
        logger.log(Level.INFO, "{0}", s.toString());
    }

    public String getModel() {
        return model;
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

    public Path getResults() {
        return results;
    }

    public double getTrainingTestSplit() {
        return trainingTestSplit;
    }
}
