package io.github.francescodonnini.csv.entities;

import com.opencsv.bean.CsvBindAndSplitByName;
import com.opencsv.bean.CsvBindByName;

import java.util.List;

public class JavaMethodLocalEntity {
    @CsvBindByName(column = "buggy", required = true)
    private boolean buggy;
    @CsvBindByName(column = "lineStart", required = true)
    private int lineStart;
    @CsvBindByName(column = "lineEnd", required = true)
    private int lineEnd;
    @CsvBindByName(column = "lineOfCode", required = true)
    private int lineOfCode;
    @CsvBindByName(column = "avgLocAdded", required = true)
    private double avgLocAdded;
    @CsvBindByName(column = "maxLocAdded", required = true)
    private int maxLocAdded;
    @CsvBindByName(column = "avgLocDeleted", required = true)
    private double avgLocDeleted;
    @CsvBindByName(column = "maxLocDeleted", required = true)
    private int maxLocDeleted;
    @CsvBindByName(column = "cyclomaticComplexity", required = true)
    int cyclomaticComplexity;
    @CsvBindByName(column = "parametersCount", required = true)
    int parametersCount;
    @CsvBindByName(column = "statementsCount", required = true)
    int statementsCount;
    @CsvBindByName(column = "statementsAdded", required = true)
    private int statementsAdded;
    @CsvBindByName(column = "avgStatementsAdded", required = true)
    private double avgStatementsAdded;
    @CsvBindByName(column = "maxStatementsAdded", required = true)
    private int maxStatementsAdded;
    @CsvBindByName(column = "statementsDeleted", required = true)
    private int statementsDeleted;
    @CsvBindByName(column = "avgStatementsDeleted", required = true)
    private double avgStatementsDeleted;
    @CsvBindByName(column = "maxStatementsDeleted", required = true)
    private int maxStatementsDeleted;
    @CsvBindByName(column = "churn", required = true)
    private int churn;
    @CsvBindByName(column = "avgChurn", required = true)
    private double avgChurn;
    @CsvBindByName(column = "maxChurn", required = true)
    private int maxChurn;
    @CsvBindByName(column = "elseCount", required = true)
    int elseCount;
    @CsvBindByName(column = "nestingDepth", required = true)
    int nestingDepth;
    @CsvBindByName(column = "locAdded")
    int locAdded;
    @CsvBindByName(column = "locDeleted")
    int locDeleted;
    @CsvBindByName(column = "elseAdded")
    int elseAdded;
    @CsvBindByName(column = "elseDeleted")
    int elseDeleted;
    @CsvBindAndSplitByName(column = "authors", elementType = String.class, splitOn = ",", writeDelimiter = ",", collectionType = List.class)
    List<String> authors;
    @CsvBindByName(column = "codeSmells")
    int codeSmells;
    @CsvBindByName(column = "codeDuplication")
    double codeDuplication;
    @CsvBindByName(column = "halsteadEffort")
    int halsteadEffort;
    @CsvBindByName(column = "signature", required = true)
    private String signature;
    @CsvBindByName(column = "classPath", required = true)
    private String classPath;
    @CsvBindByName(column = "className", required = true)
    private String className;
    @CsvBindByName(column = "commit", required = true)
    private String commit;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public double getAvgLocAdded() {
        return avgLocAdded;
    }

    public void setAvgLocAdded(double avgLocAdded) {
        this.avgLocAdded = avgLocAdded;
    }

    public int getMaxLocAdded() {
        return maxLocAdded;
    }

    public void setMaxLocAdded(int maxLocAdded) {
        this.maxLocAdded = maxLocAdded;
    }

    public double getAvgLocDeleted() {
        return avgLocDeleted;
    }

    public void setAvgLocDeleted(double avgLocDeleted) {
        this.avgLocDeleted = avgLocDeleted;
    }

    public int getMaxLocDeleted() {
        return maxLocDeleted;
    }

    public void setMaxLocDeleted(int maxLocDeleted) {
        this.maxLocDeleted = maxLocDeleted;
    }

    public double getAvgStatementsAdded() {
        return avgStatementsAdded;
    }

    public void setAvgStatementsAdded(double avgStatementsAdded) {
        this.avgStatementsAdded = avgStatementsAdded;
    }

    public int getMaxStatementsAdded() {
        return maxStatementsAdded;
    }

    public void setMaxStatementsAdded(int maxStatementsAdded) {
        this.maxStatementsAdded = maxStatementsAdded;
    }

    public double getAvgStatementsDeleted() {
        return avgStatementsDeleted;
    }

    public void setAvgStatementsDeleted(double avgStatementsDeleted) {
        this.avgStatementsDeleted = avgStatementsDeleted;
    }

    public int getMaxStatementsDeleted() {
        return maxStatementsDeleted;
    }

    public void setMaxStatementsDeleted(int maxStatementsDeleted) {
        this.maxStatementsDeleted = maxStatementsDeleted;
    }

    public int getMaxChurn() {
        return maxChurn;
    }

    public void setMaxChurn(int maxChurn) {
        this.maxChurn = maxChurn;
    }

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

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public int getCodeSmells() {
        return codeSmells;
    }

    public void setCodeSmells(int codeSmells) {
        this.codeSmells = codeSmells;
    }

    public double getCodeDuplication() {
        return codeDuplication;
    }

    public void setCodeDuplication(double codeDuplication) {
        this.codeDuplication = codeDuplication;
    }

    public int getHalsteadEffort() {
        return halsteadEffort;
    }

    public void setHalsteadEffort(int halsteadEffort) {
        this.halsteadEffort = halsteadEffort;
    }
}
