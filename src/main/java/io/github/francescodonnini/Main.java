package io.github.francescodonnini;

import io.github.francescodonnini.config.IniSettings;
import io.github.francescodonnini.csv.*;
import io.github.francescodonnini.data.*;
import io.github.francescodonnini.jira.JiraIssueApi;
import io.github.francescodonnini.jira.JiraReleaseApi;
import io.github.francescodonnini.jira.JiraVersionApi;
import io.github.francescodonnini.jira.RestApi;
import io.github.francescodonnini.pipeline.Api;
import io.github.francescodonnini.pipeline.MLPipelineContext;
import io.github.francescodonnini.pipeline.Pipeline;
import io.github.francescodonnini.pipeline.DataPipelineContext;
import io.github.francescodonnini.pipeline.data.*;
import io.github.francescodonnini.pipeline.ml.LoadDatasetStep;
import io.github.francescodonnini.pipeline.ml.TrainingStep;

import java.nio.file.Path;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws Exception {
        var settings = new IniSettings(args[0]);
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
        var api = new Api(issueApi, releaseApi);
        var context = new DataPipelineContext(api, args[1], settings);
        dataPipeline(context);
    }

    private static void dataPipeline(DataPipelineContext context) throws Exception {
        Pipeline.start(new LoadProjectInfoStep(context))
                .next(new DoProportionStep(context))
                .next(new ExtractProgramDataStep(context))
                .next(new ExportToArffStep(context))
                .run(null);
    }

    private static void mlPipeline(MLPipelineContext context) throws Exception {
        Pipeline.start(new LoadDatasetStep(context))
                .next(new TrainingStep(context))
                .run(null);

    }
}
