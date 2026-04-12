package io.github.francescodonnini.pipeline.data;

import io.github.francescodonnini.pipeline.inputs.DataPipelineContext;
import io.github.francescodonnini.pipeline.inputs.ProjectInfo;
import io.github.francescodonnini.pipeline.Step;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoadProjectInfoStep implements Step<ProjectInfo, ProjectInfo> {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final DataPipelineContext context;

    public LoadProjectInfoStep(DataPipelineContext context) {
        this.context = context;
    }

    @Override
    public ProjectInfo execute(ProjectInfo input) {
        var releases = context.getReleaseApi()
                .getReleases(input.getProject());
        logger.log(Level.INFO, "Total number of releases: {0}", releases.size());
        var issues = context.getIssueApi()
                .getIssues(input.getProject());
        final var avPresent = issues.stream().filter(i -> !i.affectedVersions().isEmpty()).count();
        logger.log(Level.INFO, "Total number of issues: {0}", issues.size());
        logger.log(Level.INFO, "AV%:                    {0}", (double)avPresent / issues.size());
        input.setIssues(issues);
        input.setAllReleases(releases);
        return input;
    }
}
