package io.github.francescodonnini.collectors.ast;

import com.sun.source.tree.*;
import io.github.francescodonnini.model.JavaClass;

public class CyclomaticComplexityCounter extends AbstractCounter {
    private int counter = 1;

    @Override
    public Void visitMethod(MethodTree node, JavaClass unused) {
        var oldCounter = counter;
        counter = 1;
        var body = node.getBody();
        // questo controllo è necessario nel caso in cui il metodo in esame non ha implementazione, cioè è un metodo
        // di un'interfaccia oppure astratto.
        if (body != null) {
            super.visitMethod(node, unused);
        }
        update(AstUtils.getSignature(node), m -> {
            m.setCyclomaticComplexity(counter);
            return null;
        });
        counter = oldCounter;
        return null;
    }

    @Override
    public Void visitBinary(BinaryTree node, JavaClass unused) {
        if (node.getKind() == Tree.Kind.CONDITIONAL_AND || node.getKind() == Tree.Kind.CONDITIONAL_OR) {
            counter++;
        }
        return super.visitBinary(node, unused);
    }

    @Override
    public Void visitCase(CaseTree node, JavaClass unused) {
        counter++;
        return super.visitCase(node, unused);
    }

    @Override
    public Void visitPatternCaseLabel(PatternCaseLabelTree node, JavaClass unused) {
        counter++;
        return super.visitPatternCaseLabel(node, unused);
    }

    @Override
    public Void visitConditionalExpression(ConditionalExpressionTree node, JavaClass unused) {
        counter++;
        return super.visitConditionalExpression(node, unused);
    }

    @Override
    public Void visitDoWhileLoop(DoWhileLoopTree node, JavaClass unused) {
        counter++;
        return super.visitDoWhileLoop(node, unused);
    }

    @Override
    public Void visitEnhancedForLoop(EnhancedForLoopTree node, JavaClass unused) {
        counter++;
        return super.visitEnhancedForLoop(node, unused);
    }

    @Override
    public Void visitForLoop(ForLoopTree node, JavaClass unused) {
        counter++;
        return super.visitForLoop(node, unused);
    }

    @Override
    public Void visitIf(IfTree node, JavaClass unused) {
        counter++;
        return super.visitIf(node, unused);
    }

    @Override
    public Void visitWhileLoop(WhileLoopTree node, JavaClass unused) {
        counter++;
        return super.visitWhileLoop(node, unused);
    }

    @Override
    public void reset() {
        super.reset();
        counter = 1;
    }
}
