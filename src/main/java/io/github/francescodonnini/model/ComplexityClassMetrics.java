package io.github.francescodonnini.model;

import java.util.HashSet;
import java.util.Set;

public class ComplexityClassMetrics {
    int loc;
    int cc;
    int elseCount;
    int nestingDepth;
    private int statementCount;
    private int smellCount;
    private int changeSetSize;
    private int fanOut;
    private final Set<String> dependencies = new HashSet<>();
    private int fanIn;
    private int cohesion;

    public int getCohesion() {
        return cohesion;
    }

    public void setCohesion(int cohesion) {
        this.cohesion = cohesion;
    }

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

    public int getFanOut() {
        return fanOut;
    }

    public void setFanOut(int fanOut) {
        this.fanOut = fanOut;
    }

    public Set<String> getDependencies() {
        return dependencies;
    }

    public void addDependencies(Set<String> dependencies) {
        this.dependencies.addAll(dependencies);
    }

    public int getFanIn() {
        return fanIn;
    }

    public void setFanIn(int fanIn) {
        this.fanIn = fanIn;
    }

    public void incFanIn() {
        this.fanIn++;
    }
}
