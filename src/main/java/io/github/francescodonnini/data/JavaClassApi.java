package io.github.francescodonnini.data;

import io.github.francescodonnini.model.JavaClass;

import java.util.List;

public interface JavaClassApi {
    List<JavaClass> getClasses();
    void save(List<JavaClass> classes);
}
