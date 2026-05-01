package io.github.francescodonnini.collectors.ast;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreeScanner;
import io.github.francescodonnini.model.RevisionJavaClass;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractCounter extends TreeScanner<Void, RevisionJavaClass> {
    public abstract void reset();
}
