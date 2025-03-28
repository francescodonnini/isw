package io.github.francescodonnini.ast;

import com.sun.source.tree.*;
import com.sun.source.util.TreeScanner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CyclomaticComplexity extends TreeScanner<Void, Void> {
    public record MethodCC(String name, int complexity) {}

    private final Map<String, Integer> complexities = new HashMap<>();
    private int complexity = 1;

    public List<MethodCC> getComplexity() {
        return complexities.entrySet().stream().map(e -> new MethodCC(e.getKey(), e.getValue())).toList();
    }

    @Override
    public Void visitMethod(MethodTree node, Void unused) {
        complexity = 0;
        var body = node.getBody();
        // questo controllo è necessario nel caso in cui il metodo in esame non ha implementazione, cioè è un metodo
        // di un'interfaccia oppure astratto.
        if (body != null) {
            visitBlock(node.getBody(), null);
        }
        complexities.put(AstUtils.getSignature(node), complexity);
        return super.visitMethod(node, null);
    }

    @Override
    public Void visitBinary(BinaryTree node, Void unused) {
        if (node.getKind() == Tree.Kind.CONDITIONAL_AND || node.getKind() == Tree.Kind.CONDITIONAL_OR) {
            complexity++;
        }
        return super.visitBinary(node, unused);
    }

    @Override
    public Void visitCase(CaseTree node, Void unused) {
        complexity++;
        return super.visitCase(node, unused);
    }

    @Override
    public Void visitPatternCaseLabel(PatternCaseLabelTree node, Void unused) {
        complexity++;
        return super.visitPatternCaseLabel(node, unused);
    }

    @Override
    public Void visitConditionalExpression(ConditionalExpressionTree node, Void unused) {
        complexity++;
        return super.visitConditionalExpression(node, unused);
    }

    @Override
    public Void visitDoWhileLoop(DoWhileLoopTree node, Void unused) {
        complexity++;
        return super.visitDoWhileLoop(node, unused);
    }

    @Override
    public Void visitEnhancedForLoop(EnhancedForLoopTree node, Void unused) {
        complexity++;
        return super.visitEnhancedForLoop(node, unused);
    }

    @Override
    public Void visitForLoop(ForLoopTree node, Void unused) {
        complexity++;
        return super.visitForLoop(node, unused);
    }

    @Override
    public Void visitIf(IfTree node, Void unused) {
        complexity++;
        return super.visitIf(node, unused);
    }

    @Override
    public Void visitWhileLoop(WhileLoopTree node, Void unused) {
        complexity++;
        return super.visitWhileLoop(node, unused);
    }
}
