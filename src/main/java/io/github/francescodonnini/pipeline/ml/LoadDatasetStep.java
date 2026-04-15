package io.github.francescodonnini.pipeline.ml;

import io.github.francescodonnini.pipeline.PipelineException;
import io.github.francescodonnini.pipeline.inputs.MLWorkloadInfo;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.weka.Dataset;

public class LoadDatasetStep implements Step<MLWorkloadInfo, MLWorkloadInfo> {
    @Override
    public MLWorkloadInfo execute(MLWorkloadInfo input) throws PipelineException {
        var datasetPath = input.getDataPath()
                .resolve(input.getProject());
        if (input.fromStart()) {
            datasetPath = datasetPath.resolve("data_%s_fromStart.arff".formatted(input.getProportion()));
        } else {
            datasetPath = datasetPath.resolve("data_%s.arff".formatted(input.getProportion()));
        }
        try {
            input.setDataset(new Dataset(datasetPath, input.getFeatures(), input.getTrainTestSplit(), input.getDropFactor()));
            return input;
        } catch (Exception e) {
            throw new PipelineException(e);
        }
    }
}