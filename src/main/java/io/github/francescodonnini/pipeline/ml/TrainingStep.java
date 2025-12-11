package io.github.francescodonnini.pipeline.ml;

import io.github.francescodonnini.pipeline.MLPipelineContext;
import io.github.francescodonnini.pipeline.MLWorkloadInfo;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.weka.factories.FilteredModelFactory;
import io.github.francescodonnini.weka.Trainer;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TrainingStep implements Step<MLWorkloadInfo, MLWorkloadInfo> {
    private final Logger logger = Logger.getLogger(TrainingStep.class.getName());
    private final MLPipelineContext context;

    public TrainingStep(MLPipelineContext context) {
        this.context = context;
    }

    @Override
    public MLWorkloadInfo execute(MLWorkloadInfo input) throws Exception {
        var factory = new FilteredModelFactory();
        factory.add(input.getSelectedFeatures());
        var trainer = new Trainer(input.getDataset(), factory);
        trainer.train(context.getModel());
        var history = trainer.getHistory();
        logger.log(Level.INFO, "summary for model: {0}", "%s%n%s".formatted(context.getModel(), history.getSummary()));
        history.save(input.getResults().resolve("results.csv"));
        return input;
    }
}
