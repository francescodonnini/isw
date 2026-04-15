package io.github.francescodonnini.pipeline.ml;

import io.github.francescodonnini.pipeline.PipelineException;
import io.github.francescodonnini.pipeline.inputs.MLWorkloadInfo;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class ReportingUtils {
    private ReportingUtils() {}

    public static void summary(Path path, MLWorkloadInfo info) throws PipelineException {
        try (var summary = new FileWriter(path.toFile())) {
            String s =
                    "Project:          " + info.getProject() + "\n" +
                    "Dataset Path:     " + info.getDataset().getPath() + "\n" +
                    "Proportion:       " + info.getProportion() + "\n" +
                    "Train-Test Split: " + info.getTrainTestSplit() + "\n" +
                    "Drop Factor:      " + info.getDropFactor() + "\n" +
                    "Model:            " + info.getModel() + "\n" +
                    "Class Weights:    " + (info.useClassWeights() ? "Y" : "N") + "\n" +
                    "Features:         " + String.join(",", info.getFeatures()) + "\n";
            summary.write(s);
        } catch (IOException e) {
            throw new PipelineException(e);
        }
    }
}
