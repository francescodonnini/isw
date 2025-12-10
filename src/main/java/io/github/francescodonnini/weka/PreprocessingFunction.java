package io.github.francescodonnini.weka;

import weka.core.Instances;

public interface PreprocessingFunction {
    void preprocess(Instances data, int attrIdx);
}