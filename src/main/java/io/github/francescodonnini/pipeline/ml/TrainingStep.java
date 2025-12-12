package io.github.francescodonnini.pipeline.ml;

import io.github.francescodonnini.pipeline.MLPipelineContext;
import io.github.francescodonnini.pipeline.MLWorkloadInfo;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.weka.factories.FilteredModelFactory;
import io.github.francescodonnini.weka.Trainer;
import weka.core.Attribute;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
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
                .collect(Collectors.toSet());
        var selectedFeatures = input.getDataset()
                .getFeatures().stream()
                .filter(f -> features.contains(f.name()))
                .collect(Collectors.toSet());
        createSummary(input.getResults(), input);
        factory.add(selectedFeatures);
        factory.add(input.getDataset().getClassAttribute());
        var trainer = new Trainer(input.getDataset(), factory);
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
