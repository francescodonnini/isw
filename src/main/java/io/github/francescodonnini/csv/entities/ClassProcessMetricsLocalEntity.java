package io.github.francescodonnini.csv.entities;

import com.opencsv.bean.CsvBindByName;

public class ClassProcessMetricsLocalEntity {
    @CsvBindByName(column = "locTouched", required = true)
    private int locTouched;
    @CsvBindByName(column = "numOfRevisions", required = true)
    private int numOfRevisions;
    @CsvBindByName(column = "numOfFixes", required = true)
    private int numOfFixes;
    @CsvBindByName(column = "numOfAuthors", required = true)
    private int numOfAuthors;
    @CsvBindByName(column = "locAdded", required = true)
    private int locAdded;
    @CsvBindByName(column = "maxLocAdded", required = true)
    private int maxLocAdded;
    @CsvBindByName(column = "avgLocAdded", required = true)
    private double avgLocAdded;
    @CsvBindByName(column = "churn", required = true)
    private int churn;
    @CsvBindByName(column = "maxChurn", required = true)
    private int maxChurn;
    @CsvBindByName(column = "avgChurn", required = true)
    private double avgChurn;
    @CsvBindByName(column = "changeSet", required = true)
    private int changeSet;
    @CsvBindByName(column = "maxChangeSet", required = true)
    private int maxChangeSet;
    @CsvBindByName(column = "avgChangeSet", required = true)
    private double avgChangeSet;
    @CsvBindByName(column = "ageDays", required = true)
    private long ageDays;

    public int getLocTouched() {
        return locTouched;
    }

    public void setLocTouched(int locTouched) {
        this.locTouched = locTouched;
    }

    public int getNumOfRevisions() {
        return numOfRevisions;
    }

    public void setNumOfRevisions(int numOfRevisions) {
        this.numOfRevisions = numOfRevisions;
    }

    public int getNumOfFixes() {
        return numOfFixes;
    }

    public void setNumOfFixes(int numOfFixes) {
        this.numOfFixes = numOfFixes;
    }

    public int getNumOfAuthors() {
        return numOfAuthors;
    }

    public void setNumOfAuthors(int numOfAuthors) {
        this.numOfAuthors = numOfAuthors;
    }

    public int getLocAdded() {
        return locAdded;
    }

    public void setLocAdded(int locAdded) {
        this.locAdded = locAdded;
    }

    public int getMaxLocAdded() {
        return maxLocAdded;
    }

    public void setMaxLocAdded(int maxLocAdded) {
        this.maxLocAdded = maxLocAdded;
    }

    public double getAvgLocAdded() {
        return avgLocAdded;
    }

    public void setAvgLocAdded(double avgLocAdded) {
        this.avgLocAdded = avgLocAdded;
    }

    public int getChurn() {
        return churn;
    }

    public void setChurn(int churn) {
        this.churn = churn;
    }

    public int getMaxChurn() {
        return maxChurn;
    }

    public void setMaxChurn(int maxChurn) {
        this.maxChurn = maxChurn;
    }

    public double getAvgChurn() {
        return avgChurn;
    }

    public void setAvgChurn(double avgChurn) {
        this.avgChurn = avgChurn;
    }

    public int getChangeSet() {
        return changeSet;
    }

    public void setChangeSet(int changeSet) {
        this.changeSet = changeSet;
    }

    public int getMaxChangeSet() {
        return maxChangeSet;
    }

    public void setMaxChangeSet(int maxChangeSet) {
        this.maxChangeSet = maxChangeSet;
    }

    public double getAvgChangeSet() {
        return avgChangeSet;
    }

    public void setAvgChangeSet(double avgChangeSet) {
        this.avgChangeSet = avgChangeSet;
    }

    public long getAgeDays() {
        return ageDays;
    }

    public void setAgeDays(long ageDays) {
        this.ageDays = ageDays;
    }
}
