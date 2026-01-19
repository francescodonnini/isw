package io.github.francescodonnini;

import io.github.francescodonnini.config.IniSettings;
import io.github.francescodonnini.csv.*;
import io.github.francescodonnini.data.*;
import io.github.francescodonnini.jira.JiraIssueApi;
import io.github.francescodonnini.jira.JiraReleaseApi;
import io.github.francescodonnini.jira.JiraVersionApi;
import io.github.francescodonnini.jira.RestApi;
import io.github.francescodonnini.pipeline.Pipeline;
import io.github.francescodonnini.pipeline.inputs.DataPipelineContext;
import io.github.francescodonnini.pipeline.data.*;
import io.github.francescodonnini.pipeline.inputs.MLWorkloadInfo;
import io.github.francescodonnini.pipeline.inputs.ProjectInfo;
import io.github.francescodonnini.pipeline.ml.FeatureSelectionStep;
import io.github.francescodonnini.pipeline.ml.LoadDatasetStep;
import io.github.francescodonnini.pipeline.ml.PreprocessingStep;
import io.github.francescodonnini.pipeline.ml.TrainingStep;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws Exception {
        switch (args[0]) {
            case "DATA":
                dataPipeline(args);
                break;
            case "FEATURE_SELECTION":
                featureSelectionPipeline(args);
                break;
            case "ML":
                trainingPipeline(args);
                break;
            default:
                throw new IllegalArgumentException("Unknown option: " + args[0]);
        }
    }

    private static void dataPipeline(String[] args) throws Exception {
        var settings = new IniSettings(args[1]);
        var jiraVersionApi = new JiraVersionApi(new RestApi());
        var cachePath = Path.of(settings.getString("cachePath"));
        var localVersionApi = new CsvVersionApi(cachePath);
        var versionApi = new VersionRepository(jiraVersionApi, localVersionApi, true);
        var jiraReleaseApi = new JiraReleaseApi(versionApi);
        var localReleaseApi = new CsvReleaseApi(cachePath);
        var releaseApi = new ReleaseRepository(jiraReleaseApi, localReleaseApi, true);
        var source = Path.of(settings.getString("sourcesPath"));
        var jiraIssueApi = new JiraIssueApi(new RestApi(), releaseApi, source);
        var localIssueApi = new CsvIssueApi(releaseApi, cachePath, source);
        var issueApi = new IssueRepository(jiraIssueApi, localIssueApi, true);
        var context = new DataPipelineContext(issueApi, releaseApi, settings);
        dataPipeline(args, context);
    }


    // From .INI file (args[1]):
    // From stdio:
    // projectName
    // proportion
    // [movingWindowPercentage]
    private static void dataPipeline(String[] args, DataPipelineContext context) throws Exception {
        var input = new ProjectInfo();
        input.setProject(args[2]);
        input.setProportion(args[3]);
        if (input.getProportion().equals("MovingWindow")) {
            input.setMovingWindowPercentage(Double.parseDouble(args[4]));
        }
        input.setFromStart(false);
        Pipeline.start(new LoadProjectInfoStep(context))
                .next(new ProportionStep(context))
                .next(new ExtractProgramDataStep(context))
                .next(new LebellingStep(context))
                .next(new ExportToArffStep(context))
                .run(input);
    }

    // From .INI file (args[1]):
    // dataPath
    // trainTestSplit
    // From stdio:
    // projectName
    // proportion
    // dropFactor
    // model
    // searchBackwards
    // metric
    private static void featureSelectionPipeline(String[] args) throws Exception {
        var settings = new IniSettings(args[1]);
        var input = new MLWorkloadInfo();
        input.setDataPath(settings.getString("dataPath"));
        var results = Path.of(settings.getString("dataPath"), args[2], "fs.csv");
        if (!results.toFile().exists()) {
            try (var file = Files.newBufferedWriter(results)) {
                file.write("id,project,proportion,search,model,metric,#features,features\n");
            }
        }
        input.setResults(results);
        input.setId(workloadId());
        input.setProject(args[2]);
        input.setProportion(args[3]);
        input.setTrainTestSplit(settings.getDouble("trainingTestSplit", 0.8));
        input.setDropFactor(Double.parseDouble(args[4]));
        input.setModel(args[5]);
        input.setBackwardSearch(Boolean.parseBoolean(args[6]));
        input.setMetric(args[7]);
        featureSelectionPipeline(input);
    }

    private static void featureSelectionPipeline(MLWorkloadInfo input) throws Exception {
        Pipeline.start(new LoadDatasetStep())
                .next(new PreprocessingStep())
                .next(new FeatureSelectionStep())
                .run(input);
    }

    // From .INI file (args[1]):
    // dataPath
    // trainTestSplit
    // features
    // From stdio:
    // projectName
    // proportion
    // dropFactor
    // model
    // searchBackwards
    private static void trainingPipeline(String[] args) throws Exception {
        var settings = new IniSettings(args[1]);
        var input = new MLWorkloadInfo();
        input.setDataPath(settings.getString("dataPath"));
        input.setProject(args[2]);
        input.setProportion(args[3]);
        input.setTrainTestSplit(settings.getDouble("trainingTestSplit", 0.8));
        input.setDropFactor(Double.parseDouble(args[4]));
        input.setModel(args[5]);
        input.setFeatures(Arrays.stream(settings.getString("features").split(",")).collect(Collectors.toSet()));
        trainingPipeline(input);
    }

    private static void trainingPipeline(MLWorkloadInfo input) throws Exception {
        Pipeline.start(new LoadDatasetStep())
                .next(new PreprocessingStep())
                .next(new TrainingStep())
                .run(input);
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
