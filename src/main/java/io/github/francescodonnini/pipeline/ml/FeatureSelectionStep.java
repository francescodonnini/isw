package io.github.francescodonnini.pipeline.ml;

import io.github.francescodonnini.pipeline.MLPipelineContext;
import io.github.francescodonnini.pipeline.MLWorkloadInfo;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.weka.tuning.ForwardSearch;
import io.github.francescodonnini.weka.tuning.ScoreFunction;
import weka.core.Attribute;

import java.util.logging.Level;
import java.util.logging.Logger;

public class FeatureSelectionStep implements Step<MLWorkloadInfo, MLWorkloadInfo> {
    private final Logger logger = Logger.getLogger(FeatureSelectionStep.class.getName());
    private final MLPipelineContext context;

    public FeatureSelectionStep(MLPipelineContext context) {
        this.context = context;
    }

    @Override
    public MLWorkloadInfo execute(MLWorkloadInfo input) throws Exception {
        var selector = new ForwardSearch(input.getDataset());
        selector.setScoreFunction(new ScoreFunction("recall"));
        var selected = selector.select("RandomForest");
        logger.log(Level.INFO, "Selected features: " + String.join(",", selected.stream().map(Attribute::name).toList()));
        input.setSelectedFeatures(selected);
        return input;
    }
}
