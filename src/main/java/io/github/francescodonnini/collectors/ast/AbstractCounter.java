package io.github.francescodonnini.collectors.ast;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreeScanner;
import io.github.francescodonnini.model.RevisionJavaClass;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractCounter extends TreeScanner<Void, RevisionJavaClass> {
    private final Map<String, RevisionJavaClass> index = new HashMap<>();

    @Override
    public Void visitClass(ClassTree node, RevisionJavaClass javaClass) {
        if (javaClass != null) {
            setClass(javaClass);
            return super.visitClass(node, javaClass);
        }
        return null;
    }

    private void setClass(RevisionJavaClass clazz) {
        index.put(clazz.getName(), clazz);
    }

    public void reset() {
        index.clear();
    }
}
