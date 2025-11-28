package io.github.francescodonnini.pipeline;

import io.github.francescodonnini.weka.Dataset;

public class MLWorkloadInfo {
    private final Dataset dataset;

    public MLWorkloadInfo(Dataset dataset) {
        this.dataset = dataset;
    }

    public Dataset getDataset() {
        return dataset;
    }
}
