package io.github.francescodonnini.pipeline.data;

import io.github.francescodonnini.pipeline.PipelineException;
import io.github.francescodonnini.pipeline.inputs.DataPipelineContext;
import io.github.francescodonnini.pipeline.inputs.ProjectInfo;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.weka.arff.JavaMethodArffSerializer;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExportToArffStep implements Step<ProjectInfo, ProjectInfo> {
    private final Logger logger = Logger.getLogger(ExportToArffStep.class.getName());
    private final DataPipelineContext context;

    public ExportToArffStep(DataPipelineContext context) {
        this.context = context;
    }

    @Override
    public ProjectInfo execute(ProjectInfo input) throws PipelineException {
        var name = "data_%s".formatted(input.getProportion());
        if (input.isFromStart()) {
            name += "_fromStart";
        }
        var path = context.getData()
                .resolve(input.getProject())
                .resolve("%s.arff".formatted(name));
        try {
            new JavaMethodArffSerializer()
                    .toArff(path, input.getAllReleases(), input.getMethods());
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
            throw new PipelineException("cannot convert dataset to arff", e);
        }
        return input;
    }
}