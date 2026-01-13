package io.github.francescodonnini;

import io.github.francescodonnini.config.IniSettings;
import io.github.francescodonnini.csv.*;
import io.github.francescodonnini.data.*;
import io.github.francescodonnini.jira.JiraIssueApi;
import io.github.francescodonnini.jira.JiraReleaseApi;
import io.github.francescodonnini.jira.JiraVersionApi;
import io.github.francescodonnini.jira.RestApi;
import io.github.francescodonnini.pipeline.MLPipelineContext;
import io.github.francescodonnini.pipeline.Pipeline;
import io.github.francescodonnini.pipeline.DataPipelineContext;
import io.github.francescodonnini.pipeline.data.*;
import io.github.francescodonnini.pipeline.ml.FeatureSelectionStep;
import io.github.francescodonnini.pipeline.ml.LoadDatasetStep;
import io.github.francescodonnini.pipeline.ml.PreprocessingStep;
import io.github.francescodonnini.pipeline.ml.TrainingStep;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.exit(1);
        }
        switch (args[0]) {
            case "DATA":
                dataPipeline(args);
                break;
            case "FEATURE_SELECTION":
                featureSelectionPipeline(args);
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
        var context = new DataPipelineContext(issueApi, releaseApi, args[2], settings);
        dataPipeline(context);
    }

    private static void dataPipeline(DataPipelineContext context) throws Exception {
        Pipeline.start(new LoadProjectInfoStep(context))
                .next(new ProportionStep(context))
                .next(new ExtractProgramDataStep(context))
                .next(new LebellingStep(context))
                .next(new ExportToArffStep(context))
                .run(null);
    }

    private static void featureSelectionPipeline(String[] args) throws Exception {
        var settings = new IniSettings(args[1]);
        var context = new MLPipelineContext(args[2], settings, true);
        featureSelectionPipeline(context);
    }

    private static void featureSelectionPipeline(MLPipelineContext context) throws Exception {
        Pipeline.start(new LoadDatasetStep(context))
                .next(new PreprocessingStep())
                .next(new FeatureSelectionStep())
                .run(null);
    }

    private static void trainingPipeline(String[] args) throws Exception {
        var settings = new IniSettings(args[1]);
        var context = new MLPipelineContext(args[2], settings, false);
        trainingPipeline(context);
    }

    private static void trainingPipeline(MLPipelineContext context) throws Exception {
        Pipeline.start(new LoadDatasetStep(context))
                .next(new PreprocessingStep())
                .next(new TrainingStep(context))
                .run(null);
    }
}
