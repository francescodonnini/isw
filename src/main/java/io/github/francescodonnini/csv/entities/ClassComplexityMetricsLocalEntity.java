package io.github.francescodonnini.csv.entities;

import com.opencsv.bean.CsvBindByName;

public class ClassComplexityMetricsLocalEntity {
    @CsvBindByName(column = "loc", required = true)
    private int loc;
    @CsvBindByName(column = "cc", required = true)
    private int cc;
    @CsvBindByName(column = "elseCount", required = true)
    private int elseCount;
    @CsvBindByName(column = "nestingDepth", required = true)
    private int nestingDepth;
    @CsvBindByName(column = "statementCount", required = true)
    private int statementCount;
    @CsvBindByName(column = "smellCount", required = true)
    private int smellCount;
    @CsvBindByName(column = "changeSetSize", required = true)
    private int changeSetSize;
    @CsvBindByName(column = "fanOut", required = true)
    private int fanOut;
    @CsvBindByName(column = "fanIn", required = true)
    private int fanIn;

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

    public int getElseCount() {
        return elseCount;
    }

    public void setElseCount(int elseCount) {
        this.elseCount = elseCount;
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

    public void setSmellCount(int smellCount) {
        this.smellCount = smellCount;
    }

    public int getChangeSetSize() {
        return changeSetSize;
    }

    public void setChangeSetSize(int changeSetSize) {
        this.changeSetSize = changeSetSize;
    }

    public int getFanIn() {
        return fanIn;
    }

    public void setFanIn(int fanIn) {
        this.fanIn = fanIn;
    }

    public int getFanOut() {
        return fanOut;
    }

    public void setFanOut(int fanOut) {
        this.fanOut = fanOut;
    }
}
