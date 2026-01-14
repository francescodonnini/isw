package io.github.francescodonnini.weka.preprocessing;

import weka.core.Instances;

public interface PreprocessingFunction {
    void preprocess(Instances data, int attrIdx);
}