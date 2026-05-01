package io.github.francescodonnini.collectors.ast;

import com.sun.source.tree.*;
import io.github.francescodonnini.model.RevisionJavaClass;

/**
 * Uno statement è un'unità completa di esecuzione (v. <a href="https://docs.oracle.com/javase/tutorial/java/nutsandbolts/expressions.html"></a>).
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
public class StatementsCounter extends AbstractCounter {
    private int total = 0;
    private boolean insideBlock = false;

    @Override
    public Void visitMethod(MethodTree node, RevisionJavaClass unused) {
        var oldCounter = total;
        total = 0;
        var rv = super.visitMethod(node, unused);
        unused.getMetrics().setStatementCount(total);
        total += oldCounter;
        return rv;
    }

    @Override
    public Void visitBlock(BlockTree node, RevisionJavaClass unused) {
        var oldInsideBlock = insideBlock;
        insideBlock = true;
        var rv = super.visitBlock(node, unused);
        insideBlock = oldInsideBlock;
        return rv;
    }

    @Override
    public Void visitAssert(AssertTree node, RevisionJavaClass unused) {
        total++;
        return super.visitAssert(node, unused);
    }

    @Override
    public Void visitBreak(BreakTree node, RevisionJavaClass unused) {
        total++;
        return super.visitBreak(node, unused);
    }

    @Override
    public Void visitContinue(ContinueTree node, RevisionJavaClass unused) {
        total++;
        return super.visitContinue(node, unused);
    }

    @Override
    public Void visitExpressionStatement(ExpressionStatementTree node, RevisionJavaClass unused) {
        total++;
        return super.visitExpressionStatement(node, unused);
    }

    @Override
    public Void visitIf(IfTree node, RevisionJavaClass unused) {
        total++;
        return super.visitIf(node, unused);
    }

    @Override
    public Void visitEmptyStatement(EmptyStatementTree node, RevisionJavaClass unused) {
        total++;
        return super.visitEmptyStatement(node, unused);
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
    public Void visitDoWhileLoop(DoWhileLoopTree node, RevisionJavaClass unused) {
        total++;
        return super.visitDoWhileLoop(node, unused);
    }

    @Override
    public Void visitLabeledStatement(LabeledStatementTree node, RevisionJavaClass unused) {
        total++;
        return super.visitLabeledStatement(node, unused);
    }

    @Override
    public Void visitReturn(ReturnTree node, RevisionJavaClass unused) {
        total++;
        return super.visitReturn(node, unused);
    }

    @Override
    public Void visitSwitch(SwitchTree node, RevisionJavaClass unused) {
        total++;
        return super.visitSwitch(node, unused);
    }

    @Override
    public Void visitSynchronized(SynchronizedTree node, RevisionJavaClass unused) {
        total++;
        return super.visitSynchronized(node, unused);
    }

    @Override
    public Void visitTry(TryTree node, RevisionJavaClass unused) {
        total++;
        return super.visitTry(node, unused);
    }

    @Override
    public Void visitVariable(VariableTree node, RevisionJavaClass unused) {
        if (insideBlock) {
            total++;
        }
        return super.visitVariable(node, unused);
    }

    @Override
    public Void visitWhileLoop(WhileLoopTree node, RevisionJavaClass unused) {
        total++;
        return super.visitWhileLoop(node, unused);
    }

    @Override
    public void reset() {
        total = 0;
    }
}