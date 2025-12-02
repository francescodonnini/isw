package io.github.francescodonnini.pipeline.data;

import io.github.francescodonnini.pipeline.DataPipelineContext;
import io.github.francescodonnini.pipeline.ProjectInfo;
import io.github.francescodonnini.pipeline.Step;

public class LoadProjectInfoStep implements Step<Void, ProjectInfo> {
    private final DataPipelineContext context;

    public LoadProjectInfoStep(DataPipelineContext context) {
        this.context = context;
    }

    @Override
    public ProjectInfo execute(Void input) {
        var releases = context.getReleaseApi()
                .getReleases(context.getProjectName());
        var remainingReleases = (int) Math.ceil(releases.size() * (1 - context.getDropFactor()));
        var issues = context.getIssueApi()
                .getIssues(context.getProjectName());
        var runtime = new ProjectInfo();
        runtime.setIssues(issues);
        runtime.setAllReleases(releases);
        runtime.setProjectReleasesEnd(remainingReleases);
        return runtime;
    }
}
