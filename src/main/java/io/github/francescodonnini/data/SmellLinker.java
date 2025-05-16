package io.github.francescodonnini.data;

import io.github.francescodonnini.model.JavaClass;

import java.util.List;

public interface SmellLinker {
    void link(List<JavaClass> classes);
}
