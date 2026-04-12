package io.github.francescodonnini.pipeline.ml;

import io.github.francescodonnini.pipeline.inputs.MLWorkloadInfo;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.weka.Dataset;

public class LoadDatasetStep implements Step<MLWorkloadInfo, MLWorkloadInfo> {
    @Override
    public MLWorkloadInfo execute(MLWorkloadInfo input) throws Exception {
        var datasetPath = input.getDataPath()
                .resolve(input.getProject())
                .resolve("data_%s.arff".formatted(input.getProportion()));
        input.setDataset(new Dataset(datasetPath, input.getFeatures(), input.getTrainTestSplit(), input.getDropFactor()));
        return input;
    }
}