package io.github.francescodonnini.pipeline.data;

import io.github.francescodonnini.data.LabelMakerImpl;
import io.github.francescodonnini.pipeline.PipelineException;
import io.github.francescodonnini.pipeline.inputs.DataPipelineContext;
import io.github.francescodonnini.pipeline.inputs.ProjectInfo;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.utils.GitUtils;

import java.io.IOException;

public class LebellingStep implements Step<ProjectInfo, ProjectInfo> {
    private final DataPipelineContext context;

    public LebellingStep(DataPipelineContext context) {
        this.context = context;
    }

    @Override
    public ProjectInfo execute(ProjectInfo input) throws PipelineException {
        var source = context.getSources()
                .resolve(input.getProject().toLowerCase());
        try (var git = GitUtils.createGit(source)) {
            new LabelMakerImpl(git, input.getIssues(), input.getAllReleases())
                    .makeLabels(input.getMethods());
        } catch (IOException e) {
            throw new PipelineException("cannot read git repository " + source, e);
        }
        return input;
    }
}
