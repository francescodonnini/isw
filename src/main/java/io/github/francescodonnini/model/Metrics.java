package io.github.francescodonnini.model;

import java.util.HashSet;
import java.util.Set;

public class Metrics {
    private long lineOfCode;
    private int cyclomaticComplexity;
    private int parametersCount;
    private long statementsCount;
    private int locTouched;
    private int churn;
    private int numOfRevisions;
    private int numOfAuthors;
    private int locAdded;
    private int locDeleted;
    private int avgLocAdded;
    private int changeSetSize;
    private int maxChangeSetSize;
    private int avgChangeSetSize;
    private final Set<String> authors = new HashSet<>();

    public void addLocAdded(int locAdded) {
        this.locAdded += locAdded;
    }

    public void addLocDeleted(int locDeleted) {
        this.locDeleted += locDeleted;
    }

    public void addLocTouched(int locTouched) {
        this.locTouched += locTouched;
    }

    public int getAvgChangeSetSize() {
        return avgChangeSetSize;
    }

    public void setAvgChangeSetSize(int avgChangeSetSize) {
        this.avgChangeSetSize = avgChangeSetSize;
    }

    public int getAvgLocAdded() {
        return avgLocAdded;
    }

    public void setAvgLocAdded(int avgLocAdded) {
        this.avgLocAdded = avgLocAdded;
    }

    public int getChangeSetSize() {
        return changeSetSize;
    }

    public void setChangeSetSize(int changeSetSize) {
        this.changeSetSize = changeSetSize;
    }

    public void addChurn(int churn) {
        this.churn += churn;
    }

    public int getChurn() {
        return churn;
    }

    public void setChurn(int churn) {
        this.churn = churn;
    }

    public int getCyclomaticComplexity() {
        return cyclomaticComplexity;
    }

    public void setCyclomaticComplexity(int cyclomaticComplexity) {
        this.cyclomaticComplexity = cyclomaticComplexity;
    }

    public long getLineOfCode() {
        return lineOfCode;
    }

    public void setLineOfCode(long lineOfCode) {
        this.lineOfCode = lineOfCode;
    }

    public int getLocAdded() {
        return locAdded;
    }

    public void setLocAdded(int locAdded) {
        this.locAdded = locAdded;
    }

    public int getLocTouched() {
        return locTouched;
    }

    public void setLocTouched(int locTouched) {
        this.locTouched = locTouched;
    }

    public int getMaxChangeSetSize() {
        return maxChangeSetSize;
    }

    public void setMaxChangeSetSize(int maxChangeSetSize) {
        this.maxChangeSetSize = maxChangeSetSize;
    }

    public int getNumOfAuthors() {
        return numOfAuthors;
    }

    public void setNumOfAuthors(int numOfAuthors) {
        this.numOfAuthors = numOfAuthors;
    }

    public int getNumOfRevisions() {
        return numOfRevisions;
    }

    public void setNumOfRevisions(int numOfRevisions) {
        this.numOfRevisions = numOfRevisions;
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

    public int getLocDeleted() {
        return locDeleted;
    }

    public void setLocDeleted(int locDeleted) {
        this.locDeleted = locDeleted;
    }

    public Set<String> getAuthors() {
        return authors;
    }

    public void addAuthor(String author) {
        authors.add(author);
    }

    @Override
    public String toString() {
        return "Metrics{" +
                "avgChangeSetSize=" + avgChangeSetSize +
                ", lineOfCode=" + lineOfCode +
                ", cyclomaticComplexity=" + cyclomaticComplexity +
                ", parametersCount=" + parametersCount +
                ", statementsCount=" + statementsCount +
                ", locTouched=" + locTouched +
                ", churn=" + churn +
                ", numOfRevisions=" + numOfRevisions +
                ", numOfAuthors=" + numOfAuthors +
                ", locAdded=" + locAdded +
                ", locDeleted=" + locDeleted +
                ", avgLocAdded=" + avgLocAdded +
                ", changeSetSize=" + changeSetSize +
                ", maxChangeSetSize=" + maxChangeSetSize +
                ", authors=" + String.join(",", authors) +
                '}';
    }
}
