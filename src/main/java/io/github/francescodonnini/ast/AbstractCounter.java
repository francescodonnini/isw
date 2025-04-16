package io.github.francescodonnini.ast;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreeScanner;
import io.github.francescodonnini.model.JavaClass;
import io.github.francescodonnini.model.JavaMethod;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractCounter extends TreeScanner<Void, JavaClass> {
    private final String metricName;
    private final Map<String, JavaMethod> index = new HashMap<>();

    protected AbstractCounter(String metricName) {
        this.metricName = metricName;
    }

    protected void update(String signature, int value) {
        var method = index.get(signature);
        if (method != null) {
            method.addMetric(metricName, value);
        }
    }

    protected void update(String signature, long value) {
        var method = index.get(signature);
        if (method != null) {
            method.addMetric(metricName, value);
        }
    }

    @Override
    public Void visitClass(ClassTree node, JavaClass javaClass) {
        if (javaClass != null) {
            setClass(javaClass);
            return super.visitClass(node, javaClass);
        }
        return null;
    }

    private void setClass(JavaClass clazz) {
        reset();
        clazz.getMethods().forEach(method -> index.put(method.getSignature(), method));
    }

    private void reset() {
        index.clear();
    }
}
