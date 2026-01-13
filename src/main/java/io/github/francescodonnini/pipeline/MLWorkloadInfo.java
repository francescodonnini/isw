package io.github.francescodonnini.pipeline;

import io.github.francescodonnini.weka.Dataset;
import weka.core.Attribute;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class MLWorkloadInfo {
    private final Dataset dataset;
    private final Path results;
    private final Set<Attribute> features = new HashSet<>();
    private final Set<Attribute> selectedFeatures = new HashSet<>();

    public MLWorkloadInfo(Dataset dataset, Path results) throws Exception {
        this(dataset, dataset.features(), results);
    }

    public MLWorkloadInfo(Dataset dataset, Set<Attribute> features, Path results) {
        this.dataset = dataset;
        this.features.addAll(features);
        this.selectedFeatures.addAll(features);
        this.results = results;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public Path getResults() {
        return results;
    }

    public Set<Attribute> getAllFeatures() {
        return features;
    }

    public Set<Attribute> getSelectedFeatures() {
        return selectedFeatures;
    }

    public void setSelectedFeatures(Set<Attribute> selectedFeatures) {
        this.selectedFeatures.clear();
        this.selectedFeatures.addAll(selectedFeatures);
    }
}
