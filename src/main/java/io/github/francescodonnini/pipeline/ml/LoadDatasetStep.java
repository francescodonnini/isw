package io.github.francescodonnini.pipeline.ml;

import io.github.francescodonnini.pipeline.PipelineException;
import io.github.francescodonnini.pipeline.inputs.MLWorkloadInfo;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.weka.Dataset;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoadDatasetStep implements Step<MLWorkloadInfo, MLWorkloadInfo> {
    private final Logger logger = Logger.getLogger(LoadDatasetStep.class.getName());

    @Override
    public MLWorkloadInfo execute(MLWorkloadInfo input) throws PipelineException {
        var datasetPath = input.getDataPath()
                .resolve(input.getProject())
                .resolve("data_%s.arff".formatted(input.getProportion()));
        try {
            input.setDataset(new Dataset(datasetPath, input.getFeatures(), input.getTrainTestSplit(), input.getDropFactor()));
            return input;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "cannot load dataset", e);
            throw new PipelineException(e);
        }
    }
}