package io.github.francescodonnini.pipeline.ml;

import io.github.francescodonnini.pipeline.MLPipelineContext;
import io.github.francescodonnini.pipeline.MLWorkloadInfo;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.weka.factories.FilteredModelFactory;
import io.github.francescodonnini.weka.WalkForwardTrainer;
import weka.core.Attribute;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class TrainingStep implements Step<MLWorkloadInfo, MLWorkloadInfo> {
    private final Logger logger = Logger.getLogger(TrainingStep.class.getName());
    private final MLPipelineContext context;

    public TrainingStep(MLPipelineContext context) throws IOException {
        this.context = context;
        var file = new FileHandler("training-step.log", true);
        file.setFormatter(new SimpleFormatter());
        logger.addHandler(file);
    }

    @Override
    public MLWorkloadInfo execute(MLWorkloadInfo input) throws Exception {
        var factory = new FilteredModelFactory();
        createSummary(input.getResults(), input);
        factory.add(input.getSelectedFeatures());
        factory.add(input.getDataset().classAttribute());
        var trainer = new WalkForwardTrainer(input.getDataset(), factory);
        trainer.train(context.getModel());
        var history = trainer.getHistory();
        logger.log(Level.INFO, "summary for model: {0}", "%s%n%s".formatted(context.getModel(), history.getSummary()));
        history.save(input.getResults().resolve("%s-results.csv".formatted(context.getModel())));
        return input;
    }

    private void createSummary(Path parent, MLWorkloadInfo info) throws IOException {
        try (var summary = new FileWriter(parent.resolve("SUMMARY").toFile())) {
            var s = new StringBuilder()
                    .append("Project: ").append(context.getProjectName()).append("\n")
                    .append("Dataset Path: ").append(context.getData().toAbsolutePath()).append("\n")
                    .append("Proportion: ").append(context.getLabellingMethod()).append("\n")
                    .append("Training-Test Split: ").append(context.getTrainingTestSplit()).append("\n")
                    .append("Drop Factor: ").append(context.getDropFactor()).append("\n")
                    .append("Features: ").append(String.join(",", info.getAllFeatures().stream().map(Attribute::name).toList())).append("\n")
                    .append("Selected Features: ").append(String.join(",", info.getSelectedFeatures().stream().map(Attribute::name).toList())).append("\n");
            summary.write(s.toString());
        }
    }
}
