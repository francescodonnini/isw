package io.github.francescodonnini.collectors.ast;

import com.sun.source.tree.*;
import io.github.francescodonnini.model.JavaClass;

public class CyclomaticComplexityCounter extends AbstractCounter {
    private int complexity = 1;

    @Override
    public Void visitMethod(MethodTree node, JavaClass unused) {
        var parent = complexity;
        complexity = 1;
        var body = node.getBody();
        // questo controllo è necessario nel caso in cui il metodo in esame non ha implementazione, cioè è un metodo
        // di un'interfaccia oppure astratto.
        if (body != null) {
            visitBlock(node.getBody(), null);
        }
        update(AstUtils.getSignature(node), m -> {
            m.setCyclomaticComplexity(complexity);
            return null;
        });
        complexity = parent;
        return super.visitMethod(node, null);
    }

    @Override
    public Void visitBinary(BinaryTree node, JavaClass unused) {
        if (node.getKind() == Tree.Kind.CONDITIONAL_AND || node.getKind() == Tree.Kind.CONDITIONAL_OR) {
            complexity++;
        }
        return super.visitBinary(node, unused);
    }

    @Override
    public Void visitCase(CaseTree node, JavaClass unused) {
        complexity++;
        return super.visitCase(node, unused);
    }

    @Override
    public Void visitPatternCaseLabel(PatternCaseLabelTree node, JavaClass unused) {
        complexity++;
        return super.visitPatternCaseLabel(node, unused);
    }

    @Override
    public Void visitConditionalExpression(ConditionalExpressionTree node, JavaClass unused) {
        complexity++;
        return super.visitConditionalExpression(node, unused);
    }

    @Override
    public Void visitDoWhileLoop(DoWhileLoopTree node, JavaClass unused) {
        complexity++;
        return super.visitDoWhileLoop(node, unused);
    }

    @Override
    public Void visitEnhancedForLoop(EnhancedForLoopTree node, JavaClass unused) {
        complexity++;
        return super.visitEnhancedForLoop(node, unused);
    }

    @Override
    public Void visitForLoop(ForLoopTree node, JavaClass unused) {
        complexity++;
        return super.visitForLoop(node, unused);
    }

    @Override
    public Void visitIf(IfTree node, JavaClass unused) {
        complexity++;
        return super.visitIf(node, unused);
    }

    @Override
    public Void visitWhileLoop(WhileLoopTree node, JavaClass unused) {
        complexity++;
        return super.visitWhileLoop(node, unused);
    }
}
