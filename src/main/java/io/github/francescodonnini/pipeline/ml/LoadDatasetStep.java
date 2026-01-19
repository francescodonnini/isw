package io.github.francescodonnini.pipeline.ml;

import io.github.francescodonnini.pipeline.inputs.MLWorkloadInfo;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.weka.Dataset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class LoadDatasetStep implements Step<MLWorkloadInfo, MLWorkloadInfo> {
    @Override
    public MLWorkloadInfo execute(MLWorkloadInfo input) throws Exception {
        var datasetPath = input.getDataPath()
                .resolve(input.getProject())
                .resolve("data_%s.arff".formatted(input.getProportion()));
        input.setDataset(new Dataset(datasetPath, input.getFeatures(), input.getTrainTestSplit(), input.getDropFactor()));
        return input;
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
