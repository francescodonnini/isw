package io.github.francescodonnini.pipeline.ml;

import io.github.francescodonnini.pipeline.PipelineException;
import io.github.francescodonnini.pipeline.inputs.MLWorkloadInfo;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.weka.Dataset;

import java.nio.file.Path;

public class LoadDatasetStep implements Step<MLWorkloadInfo, MLWorkloadInfo> {
    private final Path datasetPath;

    public LoadDatasetStep(Path datasetPath) {
        this.datasetPath = datasetPath;
    }

    @Override
    public MLWorkloadInfo execute(MLWorkloadInfo input) throws PipelineException {
        try {
            input.setDataset(new Dataset(datasetPath, input.getFeatures(), input.getTrainTestSplit(), input.getDropFactor()));
            return input;
        } catch (Exception e) {
            throw new PipelineException(e);
        }
    }
}