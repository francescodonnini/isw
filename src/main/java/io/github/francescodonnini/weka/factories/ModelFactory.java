package io.github.francescodonnini.weka.factories;

import weka.classifiers.Classifier;

public interface ModelFactory {
    Classifier create(String model) throws ModelFactoryException;
}
