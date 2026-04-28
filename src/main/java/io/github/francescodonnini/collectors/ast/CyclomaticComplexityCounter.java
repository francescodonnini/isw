package io.github.francescodonnini.collectors.ast;

import com.sun.source.tree.*;
import io.github.francescodonnini.model.RevisionJavaClass;

public class CyclomaticComplexityCounter extends AbstractCounter {
    private int total = 1;

    @Override
    public Void visitClass(ClassTree node, RevisionJavaClass javaClass) {
        var unused = super.visitClass(node, javaClass);
        javaClass.getMetrics().setCc(total);
        return unused;
    }

    @Override
    public Void visitMethod(MethodTree node, RevisionJavaClass unused) {
        var body = node.getBody();
        // questo controllo è necessario nel caso in cui il metodo in esame non ha implementazione, cioè è un metodo
        // di un'interfaccia oppure astratto.
        if (body != null) {
            super.visitMethod(node, unused);
        }
        return null;
    }

    @Override
    public Void visitBinary(BinaryTree node, RevisionJavaClass unused) {
        if (node.getKind() == Tree.Kind.CONDITIONAL_AND || node.getKind() == Tree.Kind.CONDITIONAL_OR) {
            total++;
        }
        return super.visitBinary(node, unused);
    }

    @Override
    public Void visitCase(CaseTree node, RevisionJavaClass unused) {
        total++;
        return super.visitCase(node, unused);
    }

    @Override
    public Void visitPatternCaseLabel(PatternCaseLabelTree node, RevisionJavaClass unused) {
        total++;
        return super.visitPatternCaseLabel(node, unused);
    }

    @Override
    public Void visitConditionalExpression(ConditionalExpressionTree node, RevisionJavaClass unused) {
        total++;
        return super.visitConditionalExpression(node, unused);
    }

    @Override
    public Void visitDoWhileLoop(DoWhileLoopTree node, RevisionJavaClass unused) {
        total++;
        return super.visitDoWhileLoop(node, unused);
    }

    @Override
    public Void visitEnhancedForLoop(EnhancedForLoopTree node, RevisionJavaClass unused) {
        total++;
        return super.visitEnhancedForLoop(node, unused);
    }

    @Override
    public Void visitForLoop(ForLoopTree node, RevisionJavaClass unused) {
        total++;
        return super.visitForLoop(node, unused);
    }

    @Override
    public Void visitIf(IfTree node, RevisionJavaClass unused) {
        total++;
        return super.visitIf(node, unused);
    }

    @Override
    public Void visitWhileLoop(WhileLoopTree node, RevisionJavaClass unused) {
        total++;
        return super.visitWhileLoop(node, unused);
    }

    @Override
    public void reset() {
        super.reset();
        total = 1;
    }
}
