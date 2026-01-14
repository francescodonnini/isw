package io.github.francescodonnini.pipeline.ml;

import io.github.francescodonnini.pipeline.MLWorkloadInfo;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.weka.preprocessing.StandardScaler;

public class PreprocessingStep implements Step<MLWorkloadInfo, MLWorkloadInfo> {
    @Override
    public MLWorkloadInfo execute(MLWorkloadInfo input) throws Exception {
        input.getDataset().preprocess(new StandardScaler());
        return input;
    }
}
