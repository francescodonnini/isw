package io.github.francescodonnini.pipeline.ml;

import io.github.francescodonnini.pipeline.MLWorkloadInfo;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.weka.tuning.BackwardSearch;
import io.github.francescodonnini.weka.tuning.ScoreFunction;
import weka.core.Attribute;

import java.util.logging.Level;
import java.util.logging.Logger;

public class FeatureSelectionStep implements Step<MLWorkloadInfo, MLWorkloadInfo> {
    @Override
    public MLWorkloadInfo execute(MLWorkloadInfo input) throws Exception {
        var selector = new BackwardSearch(input.getDataset());
        selector.setScoreFunction(new ScoreFunction("mcc"));
        input.setSelectedFeatures(selector.select("RandomForest"));
        return input;
    }
}
