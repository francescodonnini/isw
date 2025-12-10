package io.github.francescodonnini.weka.tuning;

import io.github.francescodonnini.weka.History;
import weka.core.Attribute;

import java.util.OptionalDouble;
import java.util.Set;
import java.util.function.Function;

public interface FeatureSelection {
    Set<Attribute> select(String model) throws Exception;

    void setMaxFeatureSelected(int n);

    void setScoreFunction(Function<History, OptionalDouble> scoreFunction);
}
