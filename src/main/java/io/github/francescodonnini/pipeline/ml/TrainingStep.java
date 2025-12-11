package io.github.francescodonnini.pipeline.ml;

import io.github.francescodonnini.pipeline.MLPipelineContext;
import io.github.francescodonnini.pipeline.MLWorkloadInfo;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.weka.factories.FilteredModelFactory;
import io.github.francescodonnini.weka.Trainer;
import weka.core.Attribute;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TrainingStep implements Step<MLWorkloadInfo, MLWorkloadInfo> {
    private final Logger logger = Logger.getLogger(TrainingStep.class.getName());
    private final MLPipelineContext context;

    public TrainingStep(MLPipelineContext context) {
        this.context = context;
    }

    @Override
    public MLWorkloadInfo execute(MLWorkloadInfo input) throws Exception {
        var factory = new FilteredModelFactory();
        var features = Arrays.stream("loc,stmt_added_avg,churn,stmt_deleted_max,loc_added_avg,loc_deleted_max,else_added,stmt_added_max,else_count,churn_avg,cyclomatic_complexity,loc_deleted,halstead_effort,nesting_depth,stmt_count,stmt_deleted_avg,churn_max,loc_added_max,smell_count"
                .split(","))
                .map(Attribute::new)
                .collect(Collectors.toSet());
        factory.add(features);
        factory.add(input.getDataset().getClassAttribute());
        var trainer = new Trainer(input.getDataset(), factory);
        trainer.train(context.getModel());
        var history = trainer.getHistory();
        logger.log(Level.INFO, "summary for model: {0}", "%s%n%s".formatted(context.getModel(), history.getSummary()));
        history.save(input.getResults().resolve("results.csv"));
        return input;
    }
}
