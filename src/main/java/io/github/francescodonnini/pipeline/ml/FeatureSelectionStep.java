package io.github.francescodonnini.pipeline.ml;

import io.github.francescodonnini.pipeline.MLWorkloadInfo;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.weka.tuning.BackwardSearch;
import io.github.francescodonnini.weka.tuning.ScoreFunction;
import weka.core.Attribute;

import java.util.logging.Level;
import java.util.logging.Logger;

public class FeatureSelectionStep implements Step<MLWorkloadInfo, MLWorkloadInfo> {
    private final Logger logger = Logger.getLogger(FeatureSelectionStep.class.getName());

    @Override
    public MLWorkloadInfo execute(MLWorkloadInfo input) throws Exception {
        var selector = new BackwardSearch(input.getDataset());
        selector.setScoreFunction(new ScoreFunction("recall"));
        var selected = selector.select("RandomForest");
        logger.log(Level.INFO, "Selected features (%d): %s".formatted(selected.size(), String.join(",", selected.stream().map(Attribute::name).toList())));
        input.setSelectedFeatures(selected);
        return input;
    }
}
