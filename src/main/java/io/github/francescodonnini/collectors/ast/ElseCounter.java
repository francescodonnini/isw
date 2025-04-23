package io.github.francescodonnini.collectors.ast;

import com.sun.source.tree.IfTree;
import com.sun.source.tree.MethodTree;
import io.github.francescodonnini.model.JavaClass;

public class ElseCounter extends AbstractCounter {
    private int numberOfElse = 0;

    @Override
    public Void visitMethod(MethodTree node, JavaClass javaClass) {
        var parent = numberOfElse;
        numberOfElse = 0;
        var v = super.visitMethod(node, javaClass);
        update(AstUtils.getSignature(node), m -> {
            m.setElseCount(numberOfElse);
            return null;
        });
        numberOfElse = parent;
        return v;
    }

    @Override
    public Void visitIf(IfTree node, JavaClass javaClass) {
        if (node.getElseStatement() != null) {
            numberOfElse++;
        }
        return super.visitIf(node, javaClass);
    }
}
