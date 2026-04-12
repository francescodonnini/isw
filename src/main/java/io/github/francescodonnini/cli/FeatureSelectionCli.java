package io.github.francescodonnini.cli;

import io.github.francescodonnini.config.IniSettings;
import io.github.francescodonnini.pipeline.Pipeline;
import io.github.francescodonnini.pipeline.data.*;
import io.github.francescodonnini.pipeline.inputs.MLWorkloadInfo;
import io.github.francescodonnini.pipeline.inputs.Proportion;
import io.github.francescodonnini.pipeline.ml.FeatureSelectionStep;
import io.github.francescodonnini.pipeline.ml.LoadDatasetStep;
import io.github.francescodonnini.pipeline.ml.PreprocessingStep;
import io.github.francescodonnini.weka.AccuracyMetric;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "feature_selection",
        mixinStandardHelpOptions = true,
        description = "")
public class FeatureSelectionCli implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", description = "The path of the .INI file")
    private File iniFile;

    @CommandLine.Option(names = {"-P", "--project"}, required = true, description = "The name of the project")
    private String project;

    @CommandLine.Option(names = {"-R", "--proportion"}, required = true, description = "The labeling method")
    private String proportion;

    @CommandLine.Option(names = {"-f", "--drop"}, required = true, description = "How many releases to drop (percentage)")
    private double dropFactor;

    @CommandLine.Option(names = {"-M", "--model"}, required = true, description = "The machine learning model")
    private String model;

    @CommandLine.Option(names = {"-E", "--metric"}, required = true, description = "The machine learning metric")
    private String metric;

    @CommandLine.Option(names = {"-B", "--backward"}, arity = "0..1", defaultValue = "false", description = "Set backward search direction")
    private boolean backward;

    @Override
    public Integer call() throws Exception {
        var settings = new IniSettings(iniFile.getAbsolutePath());
        var input = new MLWorkloadInfo();
        input.setDataPath(settings.getString("dataPath"));
        var results = Path.of(settings.getString("dataPath"), project, "fs.csv");
        if (!results.toFile().exists()) {
            try (var file = Files.newBufferedWriter(results)) {
                file.write("id,project,proportion,search,model,metric,#features,features\n");
            }
        }
        input.setResults(results);
        input.setId(workloadId());
        input.setProject(project);
        input.setProportion(Proportion.from(proportion));
        input.setTrainTestSplit(settings.getDouble("trainingTestSplit", 0.8));
        input.setDropFactor(dropFactor);
        input.setModel(model);
        input.setBackwardSearch(backward);
        input.setMetric(AccuracyMetric.fromString(metric));
        Pipeline.start(new LoadDatasetStep())
                .next(new PreprocessingStep())
                .next(new FeatureSelectionStep())
                .run(input);
        return 0;
    }

    private static String workloadId() {
        var id = UUID.randomUUID()
                .toString()
                .substring(0, 6);
        var time = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss")
                .format(LocalDateTime.now());
        return id + "-" + time;
    }
}