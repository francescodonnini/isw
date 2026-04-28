package io.github.francescodonnini.data;

import io.github.francescodonnini.model.ReleaseJavaClass;

import java.util.List;

public interface LabelMaker {
    void makeLabels(List<ReleaseJavaClass> methods);
}
