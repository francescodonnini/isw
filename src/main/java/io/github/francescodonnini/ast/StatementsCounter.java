package io.github.francescodonnini.ast;

import com.sun.source.tree.*;
import io.github.francescodonnini.model.JavaClass;

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
    private long counter = 0L;

    public StatementsCounter() {
        super("statementsCount");
    }

    @Override
    public Void visitMethod(MethodTree node, JavaClass unused) {
        var oldCounter = counter;
        counter = 0L;
        var rv = super.visitMethod(node, unused);
        update(AstUtils.getSignature(node), counter);
        counter = oldCounter;
        return rv;
    }

    @Override
    public Void visitExpressionStatement(ExpressionStatementTree node, JavaClass unused) {
        counter++;
        return super.visitExpressionStatement(node, unused);
    }

    @Override
    public Void visitIf(IfTree node, JavaClass unused) {
        counter++;
        return super.visitIf(node, unused);
    }

    @Override
    public Void visitSwitch(SwitchTree node, JavaClass unused) {
        counter++;
        return super.visitSwitch(node, unused);
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
    public Void visitDoWhileLoop(DoWhileLoopTree node, JavaClass unused) {
        counter++;
        return super.visitDoWhileLoop(node, unused);
    }

    @Override
    public Void visitWhileLoop(WhileLoopTree node, JavaClass unused) {
        counter++;
        return super.visitWhileLoop(node, unused);
    }

    @Override
    public Void visitBreak(BreakTree node, JavaClass unused) {
        counter++;
        return super.visitBreak(node, unused);
    }

    @Override
    public Void visitContinue(ContinueTree node, JavaClass unused) {
        counter++;
        return super.visitContinue(node, unused);
    }

    @Override
    public Void visitReturn(ReturnTree node, JavaClass unused) {
        counter++;
        return super.visitReturn(node, unused);
    }
}
