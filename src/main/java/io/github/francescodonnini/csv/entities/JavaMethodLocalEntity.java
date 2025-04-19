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
    private long lineStart;
    @CsvBindByName(column = "lineEnd", required = true)
    private long lineEnd;
    @CsvBindByName(column = "lineOfCode", required = true)
    private long lineOfCode;
    @CsvBindByName(column = "cyclomaticComplexity", required = true)
    int cyclomaticComplexity;
    @CsvBindByName(column = "parametersCount", required = true)
    int parametersCount;
    @CsvBindByName(column = "statementsCount", required = true)
    long statementsCount;

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

    public long getLineStart() {
        return lineStart;
    }

    public void setLineStart(long lineStart) {
        this.lineStart = lineStart;
    }

    public long getLineEnd() {
        return lineEnd;
    }

    public void setLineEnd(long lineEnd) {
        this.lineEnd = lineEnd;
    }

    public long getLineOfCode() {
        return lineOfCode;
    }

    public void setLineOfCode(long lineOfCode) {
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

    public long getStatementsCount() {
        return statementsCount;
    }

    public void setStatementsCount(long statementsCount) {
        this.statementsCount = statementsCount;
    }
}
