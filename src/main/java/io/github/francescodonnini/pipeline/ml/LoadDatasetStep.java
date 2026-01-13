package io.github.francescodonnini.pipeline.ml;

import io.github.francescodonnini.pipeline.MLPipelineContext;
import io.github.francescodonnini.pipeline.MLWorkloadInfo;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.weka.Dataset;

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
        return new MLWorkloadInfo(new Dataset(dataset, context.getFeatures(), context.getTrainingTestSplit(), context.getDropFactor()), results);
    }

    private Path createResultsFolder(Path parent) throws IOException {
        var uuid = UUID.randomUUID()
                .toString()
                .substring(0, 6);
        var time = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss")
                .format(LocalDateTime.now());
        var dir = parent.resolve(time + "-" + uuid);
        Files.createDirectories(dir);
        return dir;
    }
}
