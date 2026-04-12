package io.github.francescodonnini.pipeline.ml;

import io.github.francescodonnini.pipeline.PipelineException;
import io.github.francescodonnini.pipeline.inputs.MLWorkloadInfo;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.weka.preprocessing.StandardScaler;

public class PreprocessingStep implements Step<MLWorkloadInfo, MLWorkloadInfo> {
    @Override
    public MLWorkloadInfo execute(MLWorkloadInfo input) throws PipelineException {
        input.getDataset().preprocess(new StandardScaler());
        return input;
    }
}
