package io.github.francescodonnini.collectors.ast;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.NewClassTree;
import io.github.francescodonnini.model.RevisionJavaClass;

import java.util.HashSet;
import java.util.Set;

public class FanOutCounter extends AbstractCounter {
    private final Set<String> dependencies = new HashSet<>();

    @Override
    public void reset() {
        super.reset();
        dependencies.clear();
    }

    @Override
    public Void visitClass(ClassTree node, RevisionJavaClass cls) {
        var unused = super.visitClass(node, cls);
        cls.getMetrics().setFanOut(dependencies.size());
        cls.getMetrics().addDependencies(dependencies);
        return unused;
    }

    @Override
    public Void visitImport(ImportTree node, RevisionJavaClass cls) {
        if (!node.isStatic()) {
            dependencies.add(node.getQualifiedIdentifier().toString());
        }
        return super.visitImport(node, cls);
    }

    @Override
    public Void visitNewClass(NewClassTree node, RevisionJavaClass cls) {
        if (node.getIdentifier() != null) {
            dependencies.add(node.getIdentifier().toString());
        }
        return super.visitNewClass(node, cls);
    }
}
