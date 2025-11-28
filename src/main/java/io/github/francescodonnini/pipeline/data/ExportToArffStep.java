package io.github.francescodonnini.pipeline.data;

import io.github.francescodonnini.pipeline.DataPipelineContext;
import io.github.francescodonnini.pipeline.ProjectInfo;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.weka.JavaMethodArffSerializer;

public class ExportToArffStep implements Step<ProjectInfo, ProjectInfo> {
    private final DataPipelineContext context;

    public ExportToArffStep(DataPipelineContext context) {
        this.context = context;
    }

    @Override
    public ProjectInfo execute(ProjectInfo input) throws Exception {
        var path = context.getData()
                .resolve(context.getProjectName())
                .resolve("data.arff");
        new JavaMethodArffSerializer()
                .toArff(path, input.getProjectReleases(), input.getMethods());
        return input;
    }
}
