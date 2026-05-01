package io.github.francescodonnini.collectors.ast;

import com.sun.source.util.TreeScanner;
import io.github.francescodonnini.model.RevisionJavaClass;

public abstract class AbstractCounter extends TreeScanner<Void, RevisionJavaClass> {
    public abstract void reset();
}
