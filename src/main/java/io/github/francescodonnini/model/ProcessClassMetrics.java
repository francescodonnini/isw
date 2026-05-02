package io.github.francescodonnini.model;

import java.time.Duration;

public class ProcessClassMetrics {
    private int locTouched;
    private int numOfRevisions;
    private int numOfFixes;
    private int numOfAuthors;
    private int locAdded;
    private int maxLocAdded;
    private double avgLocAdded;
    private int churn;
    private int maxChurn;
    private double avgChurn;
    private int changeSet;
    private int maxChangeSet;
    private double avgChangeSet;
    private Duration age;
    private Duration averageChangeTime;
    private double entropy;
    private double maxEntropy;
    private double avgEntropy;
    private int activeDays;
    private int fileExpMax;
    private double fileExpAvg;
    private int devExpMax;
    private double devExpAvg;

    public int getFileExpMax() {
        return fileExpMax;
    }

    public void setFileExpMax(int fileExpMax) {
        this.fileExpMax = fileExpMax;
    }

    public double getFileExpAvg() {
        return fileExpAvg;
    }

    public void setFileExpAvg(double fileExpAvg) {
        this.fileExpAvg = fileExpAvg;
    }

    public int getDevExpMax() {
        return devExpMax;
    }

    public void setDevExpMax(int devExpMax) {
        this.devExpMax = devExpMax;
    }

    public double getDevExpAvg() {
        return devExpAvg;
    }

    public void setDevExpAvg(double devExpAvg) {
        this.devExpAvg = devExpAvg;
    }

    public double getEntropy() {
        return entropy;
    }

    public void setEntropy(double entropy) {
        this.entropy = entropy;
    }

    public double getMaxEntropy() {
        return maxEntropy;
    }

    public void setMaxEntropy(double maxEntropy) {
        this.maxEntropy = maxEntropy;
    }

    public double getAvgEntropy() {
        return avgEntropy;
    }

    public void setAvgEntropy(double avgEntropy) {
        this.avgEntropy = avgEntropy;
    }

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

    public void incNumOfFixes() {
        ++numOfFixes;
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

    public Duration getAge() {
        return age;
    }

    public void setAge(Duration age) {
        this.age = age;
    }

    public Duration getAverageChangeTime() {
        return averageChangeTime;
    }

    public void setAverageChangeTime(Duration averageChangeTime) {
        this.averageChangeTime = averageChangeTime;
    }

    public int getActiveDays() {
        return activeDays;
    }

    public void setActiveDays(int activeDays) {
        this.activeDays = activeDays;
    }
}
