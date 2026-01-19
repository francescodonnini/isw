package io.github.francescodonnini.pipeline.data;

import io.github.francescodonnini.pipeline.inputs.DataPipelineContext;
import io.github.francescodonnini.pipeline.inputs.ProjectInfo;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.weka.arff.JavaMethodArffSerializer;

public class ExportToArffStep implements Step<ProjectInfo, ProjectInfo> {
    private final DataPipelineContext context;

    public ExportToArffStep(DataPipelineContext context) {
        this.context = context;
    }

    @Override
    public ProjectInfo execute(ProjectInfo input) throws Exception {
        var name = "data_%s".formatted(input.getProportion());
        if (input.isFromStart()) {
            name += "_fromStart";
        }
        var path = context.getData()
                .resolve(input.getProject())
                .resolve("%s.arff".formatted(name));
        new JavaMethodArffSerializer()
                .toArff(path, input.getProjectReleases(), input.getMethods());
        return input;
    }
}