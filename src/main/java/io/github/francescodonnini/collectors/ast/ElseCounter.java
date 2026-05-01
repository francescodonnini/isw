package io.github.francescodonnini.collectors.ast;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.IfTree;
import io.github.francescodonnini.model.RevisionJavaClass;

public class ElseCounter extends AbstractCounter {
    private int total = 0;

    @Override
    public Void visitClass(ClassTree node, RevisionJavaClass javaClass) {
        var unused = super.visitClass(node, javaClass);
        javaClass.getMetrics().setElseCount(total);
        return unused;
    }

    @Override
    public Void visitIf(IfTree node, RevisionJavaClass javaClass) {
        if (node.getElseStatement() != null) {
            total++;
        }
        return super.visitIf(node, javaClass);
    }

    @Override
    public void reset() {
        total = 0;
    }
}
