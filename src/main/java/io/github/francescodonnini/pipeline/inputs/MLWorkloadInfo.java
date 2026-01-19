package io.github.francescodonnini.pipeline.inputs;

import io.github.francescodonnini.weka.Dataset;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class MLWorkloadInfo {
    private String id;
    private Path dataPath;
    private String project;
    private String proportion;
    private final Set<String> features = new HashSet<>();
    private double trainTestSplit;
    private double dropFactor;
    private Dataset dataset;
    private Path results;
    private String model;
    private boolean backwardSearch;
    private String metric;

    public Path getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = Path.of(dataPath);
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getProportion() {
        return proportion;
    }

    public void setProportion(String proportion) {
        this.proportion = proportion;
    }

    public Set<String> getFeatures() {
        return features;
    }

    public void setFeatures(Set<String> features) {
        this.features.addAll(features);
    }

    public double getTrainTestSplit() {
        return trainTestSplit;
    }

    public void setTrainTestSplit(double trainTestSplit) {
        this.trainTestSplit = trainTestSplit;
    }

    public double getDropFactor() {
        return dropFactor;
    }

    public void setDropFactor(double dropFactor) {
        this.dropFactor = dropFactor;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public Path getResults() {
        return results;
    }

    public void setResults(Path results) {
        this.results = results;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public boolean isBackwardSearch() {
        return backwardSearch;
    }

    public void setBackwardSearch(boolean backwardSearch) {
        this.backwardSearch = backwardSearch;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
