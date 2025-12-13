package io.github.francescodonnini.collectors.ast;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreeScanner;
import io.github.francescodonnini.model.JavaClass;
import io.github.francescodonnini.model.JavaMethod;
import io.github.francescodonnini.model.Metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class AbstractCounter extends TreeScanner<Void, JavaClass> {
    private final Map<String, JavaMethod> index = new HashMap<>();

    protected void update(String signature, Function<Metrics, Void> f) {
        var method = index.get(signature);
        if (method != null) {
            f.apply(method.getMetrics());
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
        clazz.getMethods().forEach(method -> index.put(method.getSignature(), method));
    }

    public void reset() {
        index.clear();
    }
}
