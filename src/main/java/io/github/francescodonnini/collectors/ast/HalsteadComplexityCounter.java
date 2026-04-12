package io.github.francescodonnini.collectors.ast;

import com.sun.source.tree.*;
import io.github.francescodonnini.model.JavaClass;

import java.util.HashSet;
import java.util.Set;

public class HalsteadComplexityCounter extends AbstractCounter {
    private final Set<String> operators = new HashSet<>();
    private final Set<String> operands = new HashSet<>();
    private int totalOperators;
    private int totalOperands;

    public int getEffort() {
        return getDifficulty() * getVolume();
    }

    private int getDifficulty() {
        return (int) (getDistinctOperators() / 2.0 * totalOperands / Math.max(1.0, getDistinctOperands()));
    }

    private int getVolume() {
        return (int) (getProgramLength() * log2(getProgramVocabulary() + Double.MIN_VALUE));
    }

    private static double log2(double x) {
        return Math.log(x) / Math.log(2);
    }

    private int getProgramLength() {
        return totalOperators + totalOperands;
    }

    private int getProgramVocabulary() {
        return getDistinctOperators() + getDistinctOperands();
    }

    private int getDistinctOperators() {
        return operators.size();
    }

    private int getDistinctOperands() {
        return operands.size();
    }

    @Override
    public Void visitMethod(MethodTree node, JavaClass unused) {
        var oldOperators = new HashSet<>(operators);
        var oldOperands = new HashSet<>(operands);
        operands.clear();
        operators.clear();
        var oldTotalOperators = totalOperators;
        var oldTotalOperands = totalOperands;
        totalOperators = 0;
        totalOperands = 0;
        var rv = super.visitMethod(node, unused);
        update(AstUtils.getSignature(node), m -> {
            m.setHalsteadEffort(getEffort());
            return null;
        });
        totalOperators += oldTotalOperators;
        totalOperands += oldTotalOperands;
        operators.addAll(oldOperators);
        operands.addAll(oldOperands);
        return rv;
    }

    @Override
    public Void visitBinary(BinaryTree node, JavaClass javaClass) {
        visitOperator(node);
        return super.visitBinary(node, javaClass);
    }

    @Override
    public Void visitUnary(UnaryTree node, JavaClass javaClass) {
        visitOperator(node);
        return super.visitUnary(node, javaClass);
    }

    @Override
    public Void visitCompoundAssignment(CompoundAssignmentTree node, JavaClass javaClass) {
        visitOperator(node);
        return super.visitCompoundAssignment(node, javaClass);
    }

    @Override
    public Void visitAssignment(AssignmentTree node, JavaClass javaClass) {
        visitOperator(node);
        return super.visitAssignment(node, javaClass);
    }

    @Override
    public Void visitConditionalExpression(ConditionalExpressionTree node, JavaClass javaClass) {
        visitOperator(node);
        return super.visitConditionalExpression(node, javaClass);
    }

    @Override
    public Void visitInstanceOf(InstanceOfTree node, JavaClass javaClass) {
        visitOperator(node);
        return super.visitInstanceOf(node, javaClass);
    }

    @Override
    public Void visitPrimitiveType(PrimitiveTypeTree node, JavaClass javaClass) {
        operators.add(node.getPrimitiveTypeKind().toString());
        totalOperators++;
        return super.visitPrimitiveType(node, javaClass);
    }

    /*
     * Selection: if, else, switch, case, default
     * Iteration: for, while, do, continue, break
     * Jumps/Exception: return, throw, try, catch, finally
     * synchronized
     */

    @Override
    public Void visitCase(CaseTree node, JavaClass javaClass) {
        operators.add(node.getKind().toString());
        totalOperators++;
        return super.visitCase(node, javaClass);
    }

    @Override
    public Void visitIf(IfTree node, JavaClass javaClass) {
        visitOperator(node);
        if (node.getElseStatement() != null) {
            operators.add("else");
            totalOperators++;
        }
        return super.visitIf(node, javaClass);
    }

    @Override
    public Void visitSwitch(SwitchTree node, JavaClass javaClass) {
        visitOperator(node);
        return super.visitSwitch(node, javaClass);
    }

    @Override
    public Void visitEnhancedForLoop(EnhancedForLoopTree node, JavaClass javaClass) {
        visitOperator(node);
        return super.visitEnhancedForLoop(node, javaClass);
    }

    @Override
    public Void visitForLoop(ForLoopTree node, JavaClass javaClass) {
        visitOperator(node);
        return super.visitForLoop(node, javaClass);
    }

    @Override
    public Void visitWhileLoop(WhileLoopTree node, JavaClass javaClass) {
        visitOperator(node);
        return super.visitWhileLoop(node, javaClass);
    }

    @Override
    public Void visitDoWhileLoop(DoWhileLoopTree node, JavaClass javaClass) {
        visitOperator(node);
        return super.visitDoWhileLoop(node, javaClass);
    }

    @Override
    public Void visitContinue(ContinueTree node, JavaClass javaClass) {
        visitOperator(node);
        return super.visitContinue(node, javaClass);
    }

    @Override
    public Void visitBreak(BreakTree node, JavaClass javaClass) {
        visitOperator(node);
        return super.visitBreak(node, javaClass);
    }

    @Override
    public Void visitReturn(ReturnTree node, JavaClass javaClass) {
        visitOperator(node);
        return super.visitReturn(node, javaClass);
    }

    @Override
    public Void visitThrow(ThrowTree node, JavaClass javaClass) {
        visitOperator(node);
        return super.visitThrow(node, javaClass);
    }

    @Override
    public Void visitTry(TryTree node, JavaClass javaClass) {
        visitOperator(node);
        return super.visitTry(node, javaClass);
    }

    @Override
    public Void visitCatch(CatchTree node, JavaClass javaClass) {
        operators.add(node.getKind().name());
        totalOperators++;
        return super.visitCatch(node, javaClass);
    }

    @Override
    public Void visitSynchronized(SynchronizedTree node, JavaClass javaClass) {
        visitOperator(node);
        return super.visitSynchronized(node, javaClass);
    }

    /*
     * - Method Invocation
     * - Object Creation
     * - Member Access
     * - Array Access
     * - Type Casting
     */
    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, JavaClass javaClass) {
        visitOperator(node);
        return super.visitMethodInvocation(node, javaClass);
    }

    @Override
    public Void visitNewClass(NewClassTree node, JavaClass javaClass) {
        visitOperator(node);
        return super.visitNewClass(node, javaClass);
    }

    @Override
    public Void visitNewArray(NewArrayTree node, JavaClass javaClass) {
        visitOperator(node);
        return super.visitNewArray(node, javaClass);
    }

    @Override
    public Void visitMemberSelect(MemberSelectTree node, JavaClass javaClass) {
        visitOperator(node);
        visitOperand(node.getIdentifier().toString());
        return super.visitMemberSelect(node, javaClass);
    }

    @Override
    public Void visitArrayAccess(ArrayAccessTree node, JavaClass javaClass) {
        visitOperator(node);
        return super.visitArrayAccess(node, javaClass);
    }

    @Override
    public Void visitTypeCast(TypeCastTree node, JavaClass javaClass) {
        visitOperator(node);
        return super.visitTypeCast(node, javaClass);
    }

    @Override
    public Void visitIdentifier(IdentifierTree node, JavaClass javaClass) {
        visitOperand(node.getName().toString());
        return super.visitIdentifier(node, javaClass);
    }

    @Override
    public Void visitLiteral(LiteralTree node, JavaClass javaClass) {
        var value = node.getValue();
        visitOperand(value == null ? "null" : value.toString());
        return super.visitLiteral(node, javaClass);
    }

    @Override
    public Void visitVariable(VariableTree node, JavaClass javaClass) {
        visitOperand(node.getName().toString());
        return super.visitVariable(node, javaClass);
    }

    private void visitOperand(String operand) {
        operands.add(operand);
        totalOperands++;
    }

    private void visitOperator(ExpressionTree node) {
        operators.add(node.getKind().toString());
        totalOperators++;
    }

    private void visitOperator(StatementTree node) {
        operators.add(node.getKind().name());
        totalOperators++;
    }

    @Override
    public void reset() {
        super.reset();
        totalOperands = 0;
        totalOperators = 0;
        operands.clear();
        operators.clear();
    }
}
