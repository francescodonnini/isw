package io.github.francescodonnini.pipeline.data;

import io.github.francescodonnini.pipeline.DataPipelineContext;
import io.github.francescodonnini.pipeline.ProjectInfo;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.proportion.ColdStart;
import io.github.francescodonnini.proportion.Incremental;

public class DoProportionStep implements Step<ProjectInfo, ProjectInfo> {
    private final DataPipelineContext context;

    public DoProportionStep(DataPipelineContext context) {
        this.context = context;
    }

    @Override
    public ProjectInfo execute(ProjectInfo input) throws Exception {
        var api = context.getApi();
        var proportion = switch (context.getProportion()) {
            case "Incremental" -> new Incremental(api.getIssueApi(), input.getProjectReleases(), true);
            case "ColdStart" -> new ColdStart(api.getIssueApi(), input.getProjectReleases());
            default -> throw new IllegalStateException("unknown proportion method " + context.getProportion());
        };
        var issues = proportion.makeLabels(context.getProjectName());
        input.setIssues(issues);
        return input;
    }
}
