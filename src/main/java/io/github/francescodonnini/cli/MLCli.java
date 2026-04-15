package io.github.francescodonnini.cli;

import io.github.francescodonnini.config.IniSettings;
import io.github.francescodonnini.config.ProjectSettings;
import io.github.francescodonnini.pipeline.Pipeline;
import io.github.francescodonnini.pipeline.PipelineException;
import io.github.francescodonnini.pipeline.inputs.MLWorkloadInfo;
import io.github.francescodonnini.pipeline.inputs.Proportion;
import io.github.francescodonnini.pipeline.ml.*;
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
        mixinStandardHelpOptions = true)
public class MLCli implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", description = "Path of the .INI file")
    private File iniFile;

    @CommandLine.Option(names = {"-P", "--project"}, required = true, description = "The name of the project")
    private String project;

    @CommandLine.Option(names = {"-R", "--proportion"}, required = true, description = "The labeling method")
    private String proportion;

    @CommandLine.Option(names = {"-M", "--model"}, description = "The machine learning model")
    private String model;

    @CommandLine.Option(names = {"-W"}, defaultValue = "false", description = "Use class weights")
    private boolean useClassWeights;

    @CommandLine.Option(names = {"-T", "--test"}, required = true, description = "Run name")
    private String runName;

    @CommandLine.Option(names = {"-S", "--features"}, defaultValue = "allFeatures", description = "Feature set")
    private String featureSet;

    @CommandLine.Option(names = "-E", description = "Evaluate model")
    private boolean evaluate;

    @CommandLine.Option(names = {"--fromStart"}, defaultValue = "false", description = "Use change metrics from the initial release")
    private boolean fromStart;

    @CommandLine.Option(names = "--smell-eval", description = "Reenact what-If scenario")
    private boolean smellEval;

    @CommandLine.Option(names = "--smell-attr", defaultValue = "smell_count", description = "The attribute name to zero out")
    private String smellAttr;

    @Override
    public Integer call() throws Exception {
        var settings = new ProjectSettings(new IniSettings(iniFile.getAbsolutePath()), project);
        var input = new MLWorkloadInfo();

        var dataPath = Path.of(settings.getString("dataPath"))
                .resolve(project);
        var datasetPath = datasetPath(dataPath);

        input.setProject(project);
        input.setProportion(Proportion.from(proportion));
        input.setTrainTestSplit(settings.getDouble("trainingTestSplit", 0.8));
        var dropFactor = settings.getDouble("dropFactor");
        if (dropFactor < 0 || dropFactor > 1) {
            throw new CommandLine.ParameterException(
                    new CommandLine(this).getCommandSpec().commandLine(),
                    "Error: The option (-D|--drop) must be between 0 and 1"
            );
        }
        input.setDropFactor(dropFactor);
        setModel(input, model);
        input.setFeatures(new HashSet<>(settings.getList(featureSet, String.class)));
        input.setUseClassWeights(useClassWeights);
        input.setResultsPath(resultsPath(dataPath));
        if (smellEval) {
            Pipeline.start(new LoadDatasetStep(datasetPath))
                    .next(new WhatIfStep(smellAttr))
                    .run(input);
        } else if (evaluate) {
            Pipeline.start(new LoadDatasetStep(datasetPath))
                    .next(new PreprocessingStep())
                    .next(new EvaluationStep())
                    .run(input);
        } else {
            Pipeline.start(new LoadDatasetStep(datasetPath))
                    .next(new PreprocessingStep())
                    .next(new TrainingStep())
                    .run(input);
        }
        return 0;
    }

    private void setModel(MLWorkloadInfo info, String model) throws PipelineException {
        if (!smellEval && (model == null || model.isEmpty())) {
            throw new PipelineException(new IllegalArgumentException("Model parameter must be set"));
        }
        info.setModel(model);
    }

    private Path datasetPath(Path parent) {
        var name = "data_" + proportion.toUpperCase();
        if (fromStart) {
            name += "_fromStart";
        }
        name += ".arff";
        return parent.resolve(name);
    }

    private Path resultsPath(Path parent) {
        var now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String dirName = now.format(formatter) + randomString();
        return parent
                .resolve("results")
                .resolve(runName)
                .resolve(dirName);
    }

    private String randomString() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 6);
    }
}