package io.github.francescodonnini.weka.factories;

import weka.classifiers.Classifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.filters.unsupervised.attribute.Remove;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class FilteredModelFactory implements ModelFactory {
    private final ModelFactory factory = new SimpleModelFactory();
    private final Set<Attribute> features = new HashSet<>();

    public ModelFactory add(Collection<Attribute> attributes) {
        features.addAll(attributes);
        return this;
    }

    public ModelFactory add(Attribute attribute) {
        features.add(attribute);
        return this;
    }

    @Override
    public Classifier create(String model) throws ModelFactoryException {
        if (features.isEmpty()) {
            throw new IllegalStateException("features must be set!");
        }
        var wrapper = new FilteredClassifier();
        var filter = new Remove();
        filter.setAttributeIndicesArray(features.stream().mapToInt(Attribute::index).toArray());
        filter.setInvertSelection(true);
        wrapper.setFilter(filter);
        wrapper.setClassifier(factory.create(model));
        return wrapper;
    }
}
