package io.github.francescodonnini.pipeline.ml;

import io.github.francescodonnini.pipeline.MLPipelineContext;
import io.github.francescodonnini.pipeline.MLWorkloadInfo;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.weka.Dataset;

import java.util.logging.Logger;

public class LoadDatasetStep implements Step<Void, MLWorkloadInfo> {
    private final Logger logger = Logger.getLogger(LoadDatasetStep.class.getName());
    private final MLPipelineContext context;

    public LoadDatasetStep(MLPipelineContext context) {
        this.context = context;
    }

    @Override
    public MLWorkloadInfo execute(Void input) throws Exception {
        var path = context.getData()
                .resolve(context.getProjectName())
                .resolve("methods.arff");
        var dataset = new Dataset(path, 0.8);
        return new MLWorkloadInfo(dataset);
    }
}
