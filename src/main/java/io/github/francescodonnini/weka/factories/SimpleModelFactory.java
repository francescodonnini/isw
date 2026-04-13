package io.github.francescodonnini.weka.factories;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.Logistic;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;

public class SimpleModelFactory implements ModelFactory {
    @Override
    public Classifier create(String model) {
        return switch (model) {
            case "IBk" -> new IBk();
            case "NaiveBayes" -> new NaiveBayes();
            case "RandomForest" -> new RandomForest();
            case "Logistic" -> new Logistic();
            default -> throw new IllegalArgumentException("Unknown model: " + model);
        };
    }
}
