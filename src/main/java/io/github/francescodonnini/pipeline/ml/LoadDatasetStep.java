package io.github.francescodonnini.pipeline.ml;

import io.github.francescodonnini.pipeline.MLPipelineContext;
import io.github.francescodonnini.pipeline.MLWorkloadInfo;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.weka.Dataset;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class LoadDatasetStep implements Step<Void, MLWorkloadInfo> {
    private final MLPipelineContext context;

    public LoadDatasetStep(MLPipelineContext context) {
        this.context = context;
    }

    @Override
    public MLWorkloadInfo execute(Void input) throws Exception {
        var dataset = context.getData()
                .resolve(context.getProjectName())
                .resolve("data_%s.arff".formatted(context.getLabellingMethod()));
        var results = createResultsFolder(context.getResults());
        return new MLWorkloadInfo(new Dataset(dataset, context.getTrainingTestSplit()), results);
    }

    private Path createResultsFolder(Path parent) throws IOException {
        var uuid = UUID.randomUUID()
                .toString()
                .substring(0, 6);
        var time = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss")
                .format(LocalDateTime.now());
        var dir = parent.resolve(time + "-" + uuid);
        Files.createDirectories(dir);
        try (var summary = new FileWriter(dir.resolve("SUMMARY").toFile())) {
            var s = new StringBuilder()
                    .append("Time: ").append(time).append("\n")
                    .append("Project: ").append(context.getProjectName()).append("\n")
                    .append("Dataset Path: ").append(context.getData().toAbsolutePath()).append("\n")
                    .append("Proportion: ").append(context.getLabellingMethod())
                    .append("Model: ").append(context.getModel()).append("\n")
                    .append("Training-Test Split: ").append(context.getTrainingTestSplit()).append("\n");
            summary.write(s.toString());
            return dir;
        }
    }
}
