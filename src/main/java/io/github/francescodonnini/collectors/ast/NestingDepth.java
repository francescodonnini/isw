package io.github.francescodonnini.collectors.ast;

import com.sun.source.tree.*;
import io.github.francescodonnini.model.JavaClass;

public class NestingDepth extends AbstractCounter {
    private int currentNestingDepth = 0;
    private int maxNestingDepth = 0;

    @Override
    public Void visitMethod(MethodTree node, JavaClass javaClass) {
        var parent = currentNestingDepth;
        var parent2 = maxNestingDepth;
        currentNestingDepth = 0;
        maxNestingDepth = 0;
        var v = super.visitMethod(node, javaClass);
        update(AstUtils.getSignature(node), m -> {
            m.setNestingDepth(currentNestingDepth);
            return null;
        });
        currentNestingDepth = parent;
        maxNestingDepth = parent2;
        return v;
    }

    @Override
    public Void visitCase(CaseTree node, JavaClass javaClass) {
        updateNestingDepth();
        var v = super.visitCase(node, javaClass);
        currentNestingDepth--;
        return v;
    }

    @Override
    public Void visitDoWhileLoop(DoWhileLoopTree node, JavaClass javaClass) {
        updateNestingDepth();
        var v = super.visitDoWhileLoop(node, javaClass);
        currentNestingDepth--;
        return v;
    }

    @Override
    public Void visitEnhancedForLoop(EnhancedForLoopTree node, JavaClass javaClass) {
        updateNestingDepth();
        var v = super.visitEnhancedForLoop(node, javaClass);
        currentNestingDepth--;
        return v;
    }

    @Override
    public Void visitForLoop(ForLoopTree node, JavaClass javaClass) {
        updateNestingDepth();
        var v = super.visitForLoop(node, javaClass);
        currentNestingDepth--;
        return v;
    }

    @Override
    public Void visitIf(IfTree node, JavaClass javaClass) {
        updateNestingDepth();
        var v = super.visitIf(node, javaClass);
        currentNestingDepth--;
        return v;
    }

    @Override
    public Void visitWhileLoop(WhileLoopTree node, JavaClass javaClass) {
        updateNestingDepth();
        var v = super.visitWhileLoop(node, javaClass);
        currentNestingDepth--;
        return v;
    }

    private void updateNestingDepth() {
        currentNestingDepth++;
        if (currentNestingDepth > maxNestingDepth) {
            maxNestingDepth = currentNestingDepth;
        }
    }
}
