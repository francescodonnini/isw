package io.github.francescodonnini.ast;

import com.sun.source.tree.*;
import com.sun.source.util.TreeScanner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Uno statement è un'unità completa di esecuzione (v. https://docs.oracle.com/javase/tutorial/java/nutsandbolts/expressions.html).
 * Le seguenti espressioni possono essere trasformate in statement aggiungendo
 * un ';' alla fine:
 * - Assegnazioni.
 * - Un qualsiasi utilizzo di '++' o '--'.
 * - Creazione di un oggetto.
 * Uno statement di questo tipo è rappresentato da un oggetto di tipo ExpressionStatementTree. Un'altra tipologia di statement
 * sono le dichiarazioni e i controlli di flusso che sono suddivisi per:
 * - decision-making: if-then, if-then-else e switch.
 * - looping: for, while, do-while.
 * - branching: break, continue, return.
 */
public class StatementsCounter extends TreeScanner<Void, Void> {
    public record StatementsCount(String name, Long count) {}

    private final Map<String, Long> count = new HashMap<>();
    private long counter = 0L;

    public List<StatementsCounter.StatementsCount> getCount() {
        return count.entrySet().stream().map(e -> new StatementsCounter.StatementsCount(e.getKey(), e.getValue())).toList();
    }

    @Override
    public Void visitMethod(MethodTree node, Void unused) {
        var oldCounter = counter;
        counter = 0L;
        var rv = super.visitMethod(node, unused);
        count.put(AstUtils.getSignature(node), counter);
        counter = oldCounter;
        return rv;
    }

    @Override
    public Void visitExpressionStatement(ExpressionStatementTree node, Void unused) {
        counter++;
        return super.visitExpressionStatement(node, unused);
    }

    @Override
    public Void visitIf(IfTree node, Void unused) {
        counter++;
        return super.visitIf(node, unused);
    }

    @Override
    public Void visitSwitch(SwitchTree node, Void unused) {
        counter++;
        return super.visitSwitch(node, unused);
    }

    @Override
    public Void visitEnhancedForLoop(EnhancedForLoopTree node, Void unused) {
        counter++;
        return super.visitEnhancedForLoop(node, unused);
    }

    @Override
    public Void visitForLoop(ForLoopTree node, Void unused) {
        counter++;
        return super.visitForLoop(node, unused);
    }

    @Override
    public Void visitDoWhileLoop(DoWhileLoopTree node, Void unused) {
        counter++;
        return super.visitDoWhileLoop(node, unused);
    }

    @Override
    public Void visitWhileLoop(WhileLoopTree node, Void unused) {
        counter++;
        return super.visitWhileLoop(node, unused);
    }

    @Override
    public Void visitBreak(BreakTree node, Void unused) {
        counter++;
        return super.visitBreak(node, unused);
    }

    @Override
    public Void visitContinue(ContinueTree node, Void unused) {
        counter++;
        return super.visitContinue(node, unused);
    }

    @Override
    public Void visitReturn(ReturnTree node, Void unused) {
        counter++;
        return super.visitReturn(node, unused);
    }
}
