package io.github.francescodonnini.csv.entities;

import com.opencsv.bean.CsvBindByName;

public class JavaMethodLocalEntity {
    @CsvBindByName(column = "buggy", required = true)
    private boolean buggy;
    @CsvBindByName(column = "signature", required = true)
    private String signature;
    @CsvBindByName(column = "classPath", required = true)
    private String classPath;
    @CsvBindByName(column = "commit", required = true)
    private String commit;
    @CsvBindByName(column = "lineStart", required = true)
    private int lineStart;
    @CsvBindByName(column = "lineEnd", required = true)
    private int lineEnd;
    @CsvBindByName(column = "lineOfCode", required = true)
    private int lineOfCode;
    @CsvBindByName(column = "cyclomaticComplexity", required = true)
    int cyclomaticComplexity;
    @CsvBindByName(column = "parametersCount", required = true)
    int parametersCount;
    @CsvBindByName(column = "statementsCount", required = true)
    int statementsCount;
    @CsvBindByName(column = "elseCount", required = true)
    int elseCount;
    @CsvBindByName(column = "nestingDepth", required = true)
    int nestingDepth;

    public int getLocAdded() {
        return locAdded;
    }

    public void setLocAdded(int locAdded) {
        this.locAdded = locAdded;
    }

    public int getLocDeleted() {
        return locDeleted;
    }

    public void setLocDeleted(int locDeleted) {
        this.locDeleted = locDeleted;
    }

    public int getElseAdded() {
        return elseAdded;
    }

    public void setElseAdded(int elseAdded) {
        this.elseAdded = elseAdded;
    }

    public int getElseDeleted() {
        return elseDeleted;
    }

    public void setElseDeleted(int elseDeleted) {
        this.elseDeleted = elseDeleted;
    }

    public int getChurn() {
        return churn;
    }

    public void setChurn(int churn) {
        this.churn = churn;
    }

    public double getAvgChurn() {
        return avgChurn;
    }

    public void setAvgChurn(double avgChurn) {
        this.avgChurn = avgChurn;
    }

    public int getStatementsAdded() {
        return statementsAdded;
    }

    public void setStatementsAdded(int statementsAdded) {
        this.statementsAdded = statementsAdded;
    }

    public int getStatementsDeleted() {
        return statementsDeleted;
    }

    public void setStatementsDeleted(int statementsDeleted) {
        this.statementsDeleted = statementsDeleted;
    }

    @CsvBindByName(column = "locAdded")
    int locAdded;
    @CsvBindByName(column = "locDeleted")
    int locDeleted;
    @CsvBindByName(column = "elseAdded")
    int elseAdded;
    @CsvBindByName(column = "elseDeleted")
    int elseDeleted;
    @CsvBindByName(column = "churn")
    int churn;
    @CsvBindByName(column = "avgChurn")
    double avgChurn;
    @CsvBindByName(column = "statementsAdded")
    int statementsAdded;
    @CsvBindByName(column = "statementsDeleted")
    int statementsDeleted;

    public boolean isBuggy() {
        return buggy;
    }

    public void setBuggy(boolean buggy) {
        this.buggy = buggy;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public int getLineStart() {
        return lineStart;
    }

    public void setLineStart(int lineStart) {
        this.lineStart = lineStart;
    }

    public int getLineEnd() {
        return lineEnd;
    }

    public void setLineEnd(int lineEnd) {
        this.lineEnd = lineEnd;
    }

    public int getLineOfCode() {
        return lineOfCode;
    }

    public void setLineOfCode(int lineOfCode) {
        this.lineOfCode = lineOfCode;
    }

    public int getCyclomaticComplexity() {
        return cyclomaticComplexity;
    }

    public void setCyclomaticComplexity(int cyclomaticComplexity) {
        this.cyclomaticComplexity = cyclomaticComplexity;
    }

    public int getParametersCount() {
        return parametersCount;
    }

    public void setParametersCount(int parametersCount) {
        this.parametersCount = parametersCount;
    }

    public int getStatementsCount() {
        return statementsCount;
    }

    public void setStatementsCount(int statementsCount) {
        this.statementsCount = statementsCount;
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
}
