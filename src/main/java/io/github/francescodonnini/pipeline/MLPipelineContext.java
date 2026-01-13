package io.github.francescodonnini.pipeline;

import io.github.francescodonnini.config.IniSettings;
import io.github.francescodonnini.utils.FileUtils;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MLPipelineContext {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final String projectName;
    private final String labellingMethod;
    private final double trainingTestSplit;
    private final String model;
    private final Path data;
    private final Path results;
    private final double dropFactor;
    private final Set<String> features;

    public MLPipelineContext(String projectName, IniSettings settings, boolean inferFeatures) {
        this.projectName = projectName;
        this.features = getFeatures(settings, inferFeatures);
        this.labellingMethod = settings.getString("proportion");
        this.trainingTestSplit = settings.getDouble("trainingTestSplit", 0.8);
        this.model = settings.getString("model", "RandomForest");
        data = Path.of(settings.getString("dataPath"));
        this.dropFactor = settings.getDouble("%s_dropFactor".formatted(projectName.toLowerCase()));
        FileUtils.createDirectory(data);
        results = data
                .resolve(projectName)
                .resolve("results");
        FileUtils.createDirectory(results);
        logInfo();
    }

    private Set<String> getFeatures(IniSettings settings, boolean inferFeatures) {
        var s = settings.getString("features");
        if (!inferFeatures && s != null) {
            return Arrays.stream(s.trim().split(",")).collect(Collectors.toUnmodifiableSet());
        }
        return Set.of();
    }

    private void logInfo() {
        String s = "\n" +
                "project name:        " + projectName + "\n" +
                "labelling method:    " + labellingMethod + "\n" +
                "training test split: " + trainingTestSplit + "\n" +
                "data path:           " + data.toString() + "\n" +
                "results path:        " + results.toString() + "\n";
        logger.log(Level.INFO, "{0}", s);
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

    public double getDropFactor() {
        return dropFactor;
    }

    public Set<String> getFeatures() {
        return features;
    }
}
