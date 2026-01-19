package io.github.francescodonnini.pipeline.data;

import io.github.francescodonnini.pipeline.inputs.DataPipelineContext;
import io.github.francescodonnini.pipeline.inputs.ProjectInfo;
import io.github.francescodonnini.pipeline.Step;

public class LoadProjectInfoStep implements Step<ProjectInfo, ProjectInfo> {
    private final DataPipelineContext context;

    public LoadProjectInfoStep(DataPipelineContext context) {
        this.context = context;
    }

    @Override
    public ProjectInfo execute(ProjectInfo input) {
        var releases = context.getReleaseApi()
                .getReleases(input.getProject());
        var issues = context.getIssueApi()
                .getIssues(input.getProject());
        input.setIssues(issues);
        input.setAllReleases(releases);
        return input;
    }
}
