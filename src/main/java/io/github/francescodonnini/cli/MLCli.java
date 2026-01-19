package io.github.francescodonnini.cli;

import io.github.francescodonnini.config.IniSettings;
import io.github.francescodonnini.pipeline.Pipeline;
import io.github.francescodonnini.pipeline.data.*;
import io.github.francescodonnini.pipeline.inputs.MLWorkloadInfo;
import io.github.francescodonnini.pipeline.ml.LoadDatasetStep;
import io.github.francescodonnini.pipeline.ml.PreprocessingStep;
import io.github.francescodonnini.pipeline.ml.TrainingStep;
import picocli.CommandLine;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

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

    @Override
    public Integer call() throws Exception {
        var settings = new IniSettings(iniFile.getAbsolutePath());
        var input = new MLWorkloadInfo();
        input.setDataPath(settings.getString("dataPath"));
        input.setProject(project);
        input.setProportion(proportion);
        input.setTrainTestSplit(settings.getDouble("trainingTestSplit", 0.8));
        if (dropFactor < 0 || dropFactor > 1) {
            throw new CommandLine.ParameterException(
                    new CommandLine(this).getCommandSpec().commandLine(),
                    "Error: The option (-D|--drop) must be between 0 and 1"
            );
        }
        input.setDropFactor(dropFactor);
        input.setModel(model);
        input.setFeatures(Arrays.stream(settings.getString("features").split(",")).collect(Collectors.toSet()));
        Pipeline.start(new LoadDatasetStep())
                .next(new PreprocessingStep())
                .next(new TrainingStep())
                .run(input);
        return 0;
    }
}