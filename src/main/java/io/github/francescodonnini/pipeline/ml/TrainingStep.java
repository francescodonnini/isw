package io.github.francescodonnini.pipeline.ml;

import io.github.francescodonnini.pipeline.MLPipelineContext;
import io.github.francescodonnini.pipeline.MLWorkloadInfo;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.utils.FileUtils;
import io.github.francescodonnini.weka.Trainer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
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
        var results = context.getData()
                .resolve(context.getProjectName())
                .resolve("results");
        FileUtils.createDirectory(results);
        var trainer = new Trainer(input.getDataset());
        var workflowId = workflowId();
        var models = List.of("random-forest");
        for (var m : models) {
            trainer.train(m);
            var history = trainer.getHistory();
            logger.log(Level.INFO, "summary for model: {0}", "%s%n%s".formatted(m, history.getSummary()));
            history.save(results.resolve(workflowId + ".csv"));
        }
        return input;
    }

    private static String workflowId() {
        var time = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss").format(LocalDateTime.now());
        var uuid = UUID.randomUUID().toString();
        return time + "-" + uuid.substring(0, 6);
    }
}
