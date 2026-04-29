package io.github.francescodonnini.model;

public class ComplexityClassMetrics {
    int loc;
    int cc;
    int elseCount;
    int nestingDepth;
    private int statementCount;
    private int smellCount;
    private int changeSetSize;

    public int getElseCount() {
        return elseCount;
    }

    public void setElseCount(int elseCount) {
        this.elseCount = elseCount;
    }

    public int getLoc() {
        return loc;
    }

    public void setLoc(int loc) {
        this.loc = loc;
    }

    public int getCc() {
        return cc;
    }

    public void setCc(int cc) {
        this.cc = cc;
    }

    public int getNestingDepth() {
        return nestingDepth;
    }

    public void setNestingDepth(int nestingDepth) {
        this.nestingDepth = nestingDepth;
    }

    public int getStatementCount() {
        return statementCount;
    }

    public void setStatementCount(int statementCount) {
        this.statementCount = statementCount;
    }

    public int getSmellCount() {
        return smellCount;
    }

    public void incSmellCount() {
        this.smellCount++;
    }

    public int getChangeSetSize() {
        return changeSetSize;
    }

    public void setChangeSetSize(int changeSetSize) {
        this.changeSetSize = changeSetSize;
    }

    public void setSmellCount(int smellCount) {
        this.smellCount = smellCount;
    }
}
