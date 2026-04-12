package io.github.francescodonnini.cli;

import io.github.francescodonnini.config.IniSettings;
import io.github.francescodonnini.csv.CsvIssueApi;
import io.github.francescodonnini.csv.CsvReleaseApi;
import io.github.francescodonnini.csv.CsvVersionApi;
import io.github.francescodonnini.data.IssueRepository;
import io.github.francescodonnini.data.ReleaseRepository;
import io.github.francescodonnini.data.VersionRepository;
import io.github.francescodonnini.jira.JiraIssueApi;
import io.github.francescodonnini.jira.JiraReleaseApi;
import io.github.francescodonnini.jira.JiraVersionApi;
import io.github.francescodonnini.jira.RestApi;
import io.github.francescodonnini.pipeline.Pipeline;
import io.github.francescodonnini.pipeline.data.*;
import io.github.francescodonnini.pipeline.inputs.DataPipelineContext;
import io.github.francescodonnini.pipeline.inputs.ProjectInfo;
import io.github.francescodonnini.pipeline.inputs.Proportion;
import org.apache.commons.configuration2.ex.ConfigurationException;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "data",
        mixinStandardHelpOptions = true,
        description = "")
public class DataCli implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", description = "The path of the .INI file")
    private File iniFile;

    @CommandLine.Option(names = {"-P", "--project"}, required = true, description = "The name of the project")
    private String project;

    @CommandLine.Option(names = {"-R", "--proportion"}, required = true, description = "The proportion algorithm")
    private String proportion;

    @CommandLine.Option(names = {"-M", "--window"}, arity = "0..1", description = "The moving window percentage")
    private Optional<Double> window;

    @CommandLine.Option(names = "-s", defaultValue = "false", description = "Calculate change metrics from the initial release")
    private boolean fromStart;

    @Override
    public Integer call() throws Exception {
        var context = dataPipelineContext();
        var input = new ProjectInfo();
        input.setProject(project);
        input.setProportion(Proportion.from(proportion));
        if (input.getProportion().equals(Proportion.MOVING_WINDOW)) {
            if (window.isEmpty()) {
                throw new CommandLine.ParameterException(
                        new CommandLine(this).getCommandSpec().commandLine(),
                        "Error: The option -W is required when -R='MovingWindow'"
                );
            }
            input.setMovingWindowPercentage(window.get());
        }
        input.setFromStart(fromStart);
        Pipeline.start(new LoadProjectInfoStep(context))
                .next(new ProportionStep(context))
                .next(new ExtractProgramDataStep(context))
                .next(new LebellingStep(context))
                .next(new ExportToArffStep(context))
                .run(input);
        return 0;
    }

    private DataPipelineContext dataPipelineContext() throws ConfigurationException, IOException {
        var settings = new IniSettings(iniFile.getAbsolutePath());
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
        return new DataPipelineContext(issueApi, releaseApi, settings);
    }
}