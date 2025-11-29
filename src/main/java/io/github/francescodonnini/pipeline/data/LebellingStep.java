package io.github.francescodonnini.pipeline.data;

import io.github.francescodonnini.data.LabelMakerImpl;
import io.github.francescodonnini.pipeline.DataPipelineContext;
import io.github.francescodonnini.pipeline.ProjectInfo;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.utils.GitUtils;

public class LebellingStep implements Step<ProjectInfo, ProjectInfo> {
    private final DataPipelineContext context;

    public LebellingStep(DataPipelineContext context) {
        this.context = context;
    }

    @Override
    public ProjectInfo execute(ProjectInfo input) throws Exception {
        var source = context.getSources()
                .resolve(context.getProjectName().toLowerCase());
        try (var git = GitUtils.createGit(source)) {
            var methods = new LabelMakerImpl(git, input.getIssues(), input.getMethods(), input.getProjectReleases())
                    .makeLabels();
            input.setMethods(methods);
        }
        return input;
    }
}
