package io.github.francescodonnini.pipeline.ml;

import io.github.francescodonnini.pipeline.MLPipelineContext;
import io.github.francescodonnini.pipeline.MLWorkloadInfo;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.weka.Dataset;

public class LoadDatasetStep implements Step<Void, MLWorkloadInfo> {
    private final MLPipelineContext context;

    public LoadDatasetStep(MLPipelineContext context) {
        this.context = context;
    }

    @Override
    public MLWorkloadInfo execute(Void input) throws Exception {
        var path = context.getData()
                .resolve(context.getProjectName())
                .resolve("data_%s.arff".formatted(context.getLabellingMethod()));
        return new MLWorkloadInfo(new Dataset(path, context.getTrainingTestSplit()));
    }
}
