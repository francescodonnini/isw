package io.github.francescodonnini.pipeline.data;

import io.github.francescodonnini.pipeline.inputs.DataPipelineContext;
import io.github.francescodonnini.pipeline.inputs.ProjectInfo;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.pipeline.inputs.Proportion;
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
        var proportion = switch (input.getProportion()) {
            case Proportion.ColdStart -> new ColdStart(context.getIssueApi(), input.getIssues(), input.getAllReleases(), true);
            case Proportion.Incremental -> new Incremental(input.getIssues(), input.getAllReleases(), true);
            case Proportion.MovingWindow -> new MovingWindow(input.getIssues(), input.getAllReleases(), input.getMovingWindowPercentage());
            case Proportion.Simple -> new Simple(input.getIssues(), input.getAllReleases());
        };
        input.setIssues(proportion.makeLabels(input.getProject()));
        return input;
    }
}
