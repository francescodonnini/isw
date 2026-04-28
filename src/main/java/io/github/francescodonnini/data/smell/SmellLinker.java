package io.github.francescodonnini.data.smell;

import io.github.francescodonnini.model.RevisionJavaClass;

import java.util.List;

public interface SmellLinker {
    void link(List<RevisionJavaClass> classes);
}
