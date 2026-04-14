package io.github.francescodonnini.cli;

import io.github.francescodonnini.config.IniSettings;
import io.github.francescodonnini.config.ProjectSettings;
import io.github.francescodonnini.pipeline.Pipeline;
import io.github.francescodonnini.pipeline.inputs.MLWorkloadInfo;
import io.github.francescodonnini.pipeline.inputs.Proportion;
import io.github.francescodonnini.pipeline.ml.LoadDatasetStep;
import io.github.francescodonnini.pipeline.ml.PreprocessingStep;
import io.github.francescodonnini.pipeline.ml.TrainingStep;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "ml",
        mixinStandardHelpOptions = true,
        description = "")
public class MLCli implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", description = "The path of the .INI file")
    private File iniFile;

    @CommandLine.Option(names = {"-P", "--project"}, required = true, description = "The name of the project")
    private String project;

    @CommandLine.Option(names = {"-R", "--proportion"}, required = true, description = "The labeling method")
    private String proportion;

    @CommandLine.Option(names = {"-f", "--drop"}, required = true, description = "How many releases to drop (percentage)")
    private Double dropFactor;

    @CommandLine.Option(names = {"-M", "--model"}, required = true, description = "The machine learning model")
    private String model;

    @CommandLine.Option(names = {"-W"}, defaultValue = "false", description = "Use class weights")
    private boolean useClassWeights;

    @Override
    public Integer call() throws Exception {
        var settings = new ProjectSettings(new IniSettings(iniFile.getAbsolutePath()), project);
        var input = new MLWorkloadInfo();
        var dataPath = Path.of(settings.getString("dataPath"));
        input.setDataPath(dataPath);
        input.setProject(project);
        input.setProportion(Proportion.from(proportion));
        input.setTrainTestSplit(settings.getDouble("trainingTestSplit", 0.8));
        if (dropFactor < 0 || dropFactor > 1) {
            throw new CommandLine.ParameterException(
                    new CommandLine(this).getCommandSpec().commandLine(),
                    "Error: The option (-D|--drop) must be between 0 and 1"
            );
        }
        input.setDropFactor(dropFactor);
        input.setModel(model);
        input.setFeatures(new HashSet<>(settings.getList("features", String.class)));
        input.setUseClassWeights(useClassWeights);

        var now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String dirName = now.format(formatter) + randomString();
        var resultsPath = dataPath
                .resolve(input.getProject())
                .resolve("results")
                .resolve(dirName);
        input.setResultsPath(resultsPath);
        Pipeline.start(new LoadDatasetStep())
                .next(new PreprocessingStep())
                .next(new TrainingStep())
                .run(input);
        return 0;
    }

    private String randomString() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 6);
    }
}