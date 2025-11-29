package io.github.francescodonnini.collectors.ast;

import com.sun.source.tree.IfTree;
import com.sun.source.tree.MethodTree;
import io.github.francescodonnini.model.JavaClass;

public class ElseCounter extends AbstractCounter {
    private int counter = 0;

    @Override
    public Void visitMethod(MethodTree node, JavaClass javaClass) {
        var oldCounter = counter;
        counter = 0;
        var v = super.visitMethod(node, javaClass);
        update(AstUtils.getSignature(node), m -> {
            m.setElseCount(oldCounter + counter);
            return null;
        });
        return v;
    }

    @Override
    public Void visitIf(IfTree node, JavaClass javaClass) {
        if (node.getElseStatement() != null) {
            counter++;
        }
        return super.visitIf(node, javaClass);
    }
}
