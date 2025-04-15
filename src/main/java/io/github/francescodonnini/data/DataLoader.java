package io.github.francescodonnini.data;

import io.github.francescodonnini.model.JavaClass;
import io.github.francescodonnini.model.JavaMethod;

import java.util.List;

public interface DataLoader {
    List<JavaClass> getClasses() throws DataLoaderException;
    List<JavaMethod> getMethods() throws DataLoaderException;
}
