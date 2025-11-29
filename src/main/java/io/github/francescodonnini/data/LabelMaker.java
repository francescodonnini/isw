package io.github.francescodonnini.data;

import io.github.francescodonnini.model.JavaMethod;

import java.util.List;

public interface LabelMaker {
    void makeLabels(List<JavaMethod> methods);
}
