package io.github.francescodonnini.pipeline.data;

import io.github.francescodonnini.pipeline.DataPipelineContext;
import io.github.francescodonnini.pipeline.ProjectInfo;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.proportion.ColdStart;
import io.github.francescodonnini.proportion.Incremental;

public class ProportionStep implements Step<ProjectInfo, ProjectInfo> {
    private final DataPipelineContext context;

    public ProportionStep(DataPipelineContext context) {
        this.context = context;
    }

    @Override
    public ProjectInfo execute(ProjectInfo input) {
        var api = context.getApi();
        var proportion = switch (context.getProportion()) {
            case "Incremental" -> new Incremental(input.getIssues(), input.getProjectReleases(), true);
            case "ColdStart" -> new ColdStart(api.getIssueApi(), input.getIssues(), input.getProjectReleases());
            default -> throw new IllegalStateException("unknown proportion method " + context.getProportion());
        };
        var issues = proportion.makeLabels(context.getProjectName());
        input.setIssues(issues);
        return input;
    }
}
