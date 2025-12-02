package io.github.francescodonnini.pipeline.data;

import io.github.francescodonnini.pipeline.DataPipelineContext;
import io.github.francescodonnini.pipeline.ProjectInfo;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.proportion.ColdStart;
import io.github.francescodonnini.proportion.Incremental;
import io.github.francescodonnini.proportion.MovingWindow;
import io.github.francescodonnini.proportion.Simple;

public class ProportionStep implements Step<ProjectInfo, ProjectInfo> {
    private final DataPipelineContext context;

    public ProportionStep(DataPipelineContext context) {
        this.context = context;
    }

    @Override
    public ProjectInfo execute(ProjectInfo input) {
        var proportion = switch (context.getProportion()) {
            case "ColdStart" -> new ColdStart(context.getIssueApi(), input.getIssues(), input.getAllReleases(), input.getProjectReleasesEnd(), true);
            case "Incremental" -> new Incremental(input.getIssues(), input.getAllReleases(), input.getProjectReleasesEnd(), true);
            case "MovingWindow" -> new MovingWindow(input.getIssues(), input.getAllReleases(), context.getMovingWindowPercentage());
            case "Simple" -> new Simple(input.getIssues(), input.getAllReleases());
            default -> throw new IllegalStateException("unknown proportion method " + context.getProportion());
        };
        input.setIssues(proportion.makeLabels(context.getProjectName()));
        return input;
    }
}
