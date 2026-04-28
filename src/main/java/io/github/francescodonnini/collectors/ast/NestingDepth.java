package io.github.francescodonnini.collectors.ast;

import com.sun.source.tree.*;
import io.github.francescodonnini.model.RevisionJavaClass;

public class NestingDepth extends AbstractCounter {
    private int currentNestingDepth = 0;
    private int maxNestingDepth = 0;

    @Override
    public Void visitClass(ClassTree node, RevisionJavaClass javaClass) {
        var unused = super.visitClass(node, javaClass);
        javaClass.getMetrics().setNestingDepth(maxNestingDepth);
        return unused;
    }

    @Override
    public Void visitMethod(MethodTree node, RevisionJavaClass javaClass) {
        var oldNestingDepth = currentNestingDepth;
        currentNestingDepth = 0;
        var oldMaxNestingDepth = maxNestingDepth;
        maxNestingDepth = 0;
        var unused = super.visitMethod(node, javaClass);
        currentNestingDepth += oldNestingDepth;
        maxNestingDepth += oldMaxNestingDepth;
        return unused;
    }

    @Override
    public Void visitCase(CaseTree node, RevisionJavaClass javaClass) {
        updateNestingDepth();
        var unused = super.visitCase(node, javaClass);
        currentNestingDepth--;
        return unused;
    }

    @Override
    public Void visitDoWhileLoop(DoWhileLoopTree node, RevisionJavaClass javaClass) {
        updateNestingDepth();
        var unused = super.visitDoWhileLoop(node, javaClass);
        currentNestingDepth--;
        return unused;
    }

    @Override
    public Void visitEnhancedForLoop(EnhancedForLoopTree node, RevisionJavaClass javaClass) {
        updateNestingDepth();
        var unused = super.visitEnhancedForLoop(node, javaClass);
        currentNestingDepth--;
        return unused;
    }

    @Override
    public Void visitForLoop(ForLoopTree node, RevisionJavaClass javaClass) {
        updateNestingDepth();
        var unused = super.visitForLoop(node, javaClass);
        currentNestingDepth--;
        return unused;
    }

    @Override
    public Void visitIf(IfTree node, RevisionJavaClass javaClass) {
        updateNestingDepth();
        var unused = super.visitIf(node, javaClass);
        currentNestingDepth--;
        return unused;
    }

    @Override
    public Void visitWhileLoop(WhileLoopTree node, RevisionJavaClass javaClass) {
        updateNestingDepth();
        var unused = super.visitWhileLoop(node, javaClass);
        currentNestingDepth--;
        return unused;
    }

    private void updateNestingDepth() {
        currentNestingDepth++;
        if (currentNestingDepth > maxNestingDepth) {
            maxNestingDepth = currentNestingDepth;
        }
    }

    @Override
    public void reset() {
        super.reset();
        currentNestingDepth = 0;
        maxNestingDepth = 0;
    }
}
