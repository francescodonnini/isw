package io.github.francescodonnini.pipeline.inputs;

import io.github.francescodonnini.weka.Dataset;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class MLWorkloadInfo {
    private String id;
    private Path dataPath;
    private String project;
    private Proportion proportion;
    private final Set<String> features = new HashSet<>();
    private double trainTestSplit;
    private double dropFactor;
    private Dataset dataset;
    private Path results;
    private String model;
    private boolean useClassWeights;
    private boolean fromStart;

    public void setFromStart(boolean fromStart) {
        this.fromStart = fromStart;
    }

    public boolean fromStart() {
        return fromStart;
    }

    public Path getDataPath() {
        return dataPath;
    }

    public void setDataPath(Path dataPath) {
        this.dataPath = dataPath;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public Proportion getProportion() {
        return proportion;
    }

    public void setProportion(Proportion proportion) {
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

    public Path getResultsPath() {
        return results;
    }

    public void setResultsPath(Path results) {
        this.results = results;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUseClassWeights(boolean useClassWeights) {
        this.useClassWeights = useClassWeights;
    }

    public boolean useClassWeights() {
        return useClassWeights;
    }
}
