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
        var issues = input.getIssues().stream()
                .filter(i -> !i.created().isAfter(input.getProjectReleases().getLast().releaseDate().atStartOfDay()))
                .toList();
        var proportion = switch (context.getProportion()) {
            case "Incremental" -> new Incremental(issues, input.getProjectReleases(), true);
            case "ColdStart" -> new ColdStart(context.getApi().getIssueApi(), issues, input.getProjectReleases());
            default -> throw new IllegalStateException("unknown proportion method " + context.getProportion());
        };
        input.setIssues(proportion.makeLabels(context.getProjectName()));
        return input;
    }
}
