package io.github.francescodonnini.weka.factories;

import io.github.francescodonnini.pipeline.ml.ClassWeights;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.core.Instances;

public class CostSensitiveModelFactory implements ModelFactory {
    private final ModelFactory factory;
    private double[] weights = null;

    public CostSensitiveModelFactory(ModelFactory factory) {
        this.factory = factory;
    }

    public ModelFactory setClassWeights(Instances data) {
        if (data.isEmpty()) {
            return this;
        }
        var labels = data.stream()
                .map(i -> i.classValue() == 1.0)
                .toList();
        weights = ClassWeights.classWeights(labels);
        return this;
    }

    @Override
    public Classifier create(String model) throws Exception {
        var wrapper = new CostSensitiveClassifier();
        var m = new CostMatrix(2);
        m.setElement(0, 0, 0.0);
        m.setElement(1, 1, 0.0);
        m.setElement(0, 1, weights[0]);
        m.setElement(1, 0, weights[1]);
        wrapper.setCostMatrix(m);
        wrapper.setMinimizeExpectedCost(false);
        wrapper.setClassifier(factory.create(model));
        return wrapper;
    }
}
