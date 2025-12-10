package io.github.francescodonnini.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class Metrics {
    private int cyclomaticComplexity;
    private int parametersCount;
    private int lineOfCode;
    private final List<Integer> locHistory = new ArrayList<>();
    private int locAdded;
    private int maxLocAdded;
    private double avgLocAdded;
    private int locDeleted;
    private int maxLocDeleted;
    private double avgLocDeleted;
    private int statementsCount;
    private final List<Integer> statementHistory = new ArrayList<>();
    private int statementsAdded;
    private int maxStatementsAdded;
    private double avgStatementsAdded;
    private int statementsDeleted;
    private int maxStatementsDeleted;
    private double avgStatementsDeleted;
    private int churn;
    private int maxChurn;
    private double avgChurn;
    private int nestingDepth;
    private int elseCount;
    private final List<Integer> elseHistory = new ArrayList<>();
    private int elseAdded;
    private int elseDeleted;
    private final Set<String> authors = new HashSet<>();
    private int codeSmells;
    private double codeDuplcation;
    private int halsteadEffort;

    @Override
    public String toString() {
        return "Metrics{" +
                "cyclomaticComplexity=" + cyclomaticComplexity +
                ", lineOfCode=" + lineOfCode +
                ", parametersCount=" + parametersCount +
                ", statementsCount=" + statementsCount +
                ", elseCount=" + elseCount +
                ", nestingDepth=" + nestingDepth +
                ", authorsCount=" + authors.size() +
                ", authors=" + String.join(",", authors) +
                ", codeSmells=" + codeSmells +
                '}';
    }

    public int getCyclomaticComplexity() {
        return cyclomaticComplexity;
    }

    public void setCyclomaticComplexity(int cyclomaticComplexity) {
        this.cyclomaticComplexity = cyclomaticComplexity;
    }

    public void addLoc(int loc) {
        locHistory.add(loc);
    }

    public int getLineOfCode() {
        return lineOfCode;
    }

    public int getLocAdded() {
        if (!locHistory.isEmpty()) {
            locAdded = getAdded(locHistory);
        }
        return locAdded;
    }

    public void setLocAdded(int locAdded) {
        this.locAdded = locAdded;
    }

    public int getMaxLocAdded() {
        if (!locHistory.isEmpty()) {
            maxLocAdded = getMaxPositiveChangeSetSize(locHistory);
        }
        return maxLocAdded;
    }

    public void setMaxLocAdded(int maxLocAdded) {
        this.maxLocAdded = maxLocAdded;
    }

    public double getAvgLocAdded() {
        if (!locHistory.isEmpty()) {
            avgLocAdded = getAvgPositiveChangeSetSize(locHistory);
        }
        return avgLocAdded;
    }

    public void setAvgLocAdded(double avgLocAdded) {
        this.avgLocAdded = avgLocAdded;
    }

    public int getLocDeleted() {
        if (!locHistory.isEmpty()) {
            locDeleted = getDeleted(locHistory);
        }
        return locDeleted;
    }

    public void setLocDeleted(int locDeleted) {
        this.locDeleted = locDeleted;
    }

    public int getMaxLocDeleted() {
        if (!locHistory.isEmpty()) {
            maxLocDeleted = getMaxNegativeChangeSetSize(locHistory);
        }
        return maxLocDeleted;
    }

    public void setMaxLocDeleted(int maxLocDeleted) {
        this.maxLocDeleted = maxLocDeleted;
    }

    public double getAvgLocDeleted() {
        if (!locHistory.isEmpty()) {
            avgLocDeleted = getAvgNegativeChangeSetSize(locHistory);
        }
        return avgLocDeleted;
    }

    public void setAvgLocDeleted(double avgLocDeleted) {
        this.avgLocDeleted = avgLocDeleted;
    }

    public void setLineOfCode(int lineOfCode) {
        this.lineOfCode = lineOfCode;
    }

    public int getParametersCount() {
        return parametersCount;
    }

    public void setParametersCount(int parametersCount) {
        this.parametersCount = parametersCount;
    }

    public void addStatementCount(int statementCount) {
        statementHistory.add(statementCount);
    }

    public int getStatementsCount() {
        return statementsCount;
    }

    public void setStatementsCount(int statementsCount) {
        this.statementsCount = statementsCount;
    }

    public int getStatementsAdded() {
        if (!statementHistory.isEmpty()) {
            statementsAdded = getAdded(statementHistory);
        }
        return statementsAdded;
    }

    public void setStatementsAdded(int statementsAdded) {
        this.statementsAdded = statementsAdded;
    }

    public int getMaxStatementsAdded() {
        if (!statementHistory.isEmpty()) {
            maxStatementsAdded = getMaxPositiveChangeSetSize(statementHistory);
        }
        return maxStatementsAdded;
    }

    public void setMaxStatementsAdded(int maxStatementsAdded) {
        this.maxStatementsAdded = maxStatementsAdded;
    }

    public double getAvgStatementsAdded() {
        if (!statementHistory.isEmpty()) {
            avgStatementsAdded = getAvgPositiveChangeSetSize(statementHistory);
        }
        return avgStatementsAdded;
    }

    public void setAvgStatementsAdded(double avgStatementsAdded) {
        this.avgStatementsAdded = avgStatementsAdded;
    }

    public int getStatementsDeleted() {
        if (!statementHistory.isEmpty()) {
            statementsDeleted = getDeleted(statementHistory);
        }
        return statementsDeleted;
    }

    public void setStatementsDeleted(int statementsDeleted) {
        this.statementsDeleted = statementsDeleted;
    }

    public double getAvgStatementsDeleted() {
        if (!statementHistory.isEmpty()) {
            avgStatementsDeleted = getAvgNegativeChangeSetSize(statementHistory);
        }
        return avgStatementsDeleted;
    }

    public void setAvgStatementsDeleted(double avgStatementsDeleted) {
        this.avgStatementsDeleted = avgStatementsDeleted;
    }

    public int getMaxStatementsDeleted() {
        if (!statementHistory.isEmpty()) {
            maxStatementsDeleted = getMaxNegativeChangeSetSize(statementHistory);
        }
        return maxStatementsDeleted;
    }

    public void setMaxStatementsDeleted(int maxStatementsDeleted) {
        this.maxStatementsDeleted = maxStatementsDeleted;
    }

    public int getChurn() {
        if (!statementHistory.isEmpty()) {
            churn = getDiff(statementHistory)
                    .stream()
                    .reduce(0, Integer::sum);
        }
        return churn;
    }

    public void setChurn(int churn) {
        this.churn = churn;
    }

    public double getAvgChurn() {
        if (!statementHistory.isEmpty())
            avgChurn = getAvgChangeSetSize(statementHistory);
        return avgChurn;
    }

    public void setAvgChurn(double avgChurn) {
        this.avgChurn = avgChurn;
    }

    public int getMaxChurn() {
        if (!statementHistory.isEmpty()) {
            maxChurn = getMaxChangeSetSize(statementHistory);
        }
        return maxChurn;
    }

    public void setMaxChurn(int maxChurn) {
        this.maxChurn = maxChurn;
    }

    public void addAuthor(String author) {
        authors.add(author);
    }

    public Set<String> getAuthors() {
        return authors;
    }

    public int getAuthorsCount() {
        return Math.max(authors.size(), 1);
    }

    public int getNestingDepth() {
        return nestingDepth;
    }

    public void setNestingDepth(int nestingDepth) {
        this.nestingDepth = nestingDepth;
    }

    public void addElseCount(int elseCount) {
        elseHistory.add(elseCount);
    }

    public int getElseCount() {
        return elseCount;
    }

    public int getElseAdded() {
        if (!elseHistory.isEmpty()) {
            elseAdded = getAdded(elseHistory);
        }
        return elseAdded;
    }

    public void setElseAdded(int elseAdded) {
        this.elseAdded = elseAdded;
    }

    public int getElseDeleted() {
        if (!elseHistory.isEmpty()) {
            elseDeleted = getDeleted(elseHistory);
        }
        return elseDeleted;
    }

    public void setElseDeleted(int elseDeleted) {
        this.elseDeleted = elseDeleted;
    }

    public void setElseCount(int elseCount) {
        this.elseCount = elseCount;
    }

    private List<Integer> getDiff(List<Integer> history) {
        if (history.isEmpty()) {
            return List.of();
        }
        var diff = new ArrayList<Integer>();
        for (var i = 1; i < history.size(); i++) {
            var prev = history.get(i - 1);
            diff.add(history.get(i) - prev);
        }
        return diff;
    }

    private int getAdded(List<Integer> history) {
        return Math.max(delta(history), 0);
    }

    private int getDeleted(List<Integer> history) {
        return Math.abs(Math.min(delta(history), 0));
    }

    private double getAvgPositiveChangeSetSize(List<Integer> history) {
        return getAvgChangeSetSize(history, i -> i > 0);
    }

    private double getAvgNegativeChangeSetSize(List<Integer> history) {
        return getAvgChangeSetSize(history, i -> i < 0);
    }

    private double getAvgChangeSetSize(List<Integer> history) {
        return getAvgChangeSetSize(history, i -> true);
    }

    private double getAvgChangeSetSize(List<Integer> history, Predicate<Integer> filter) {
        return getDiff(history).stream()
                .filter(filter)
                .map(Math::abs)
                .map(Integer::doubleValue)
                .reduce(0.0, Double::sum) / history.size();
    }

    private int delta(List<Integer> history) {
        if (history.isEmpty()) {
            return 0;
        } else if (history.size() == 1) {
            return history.getFirst();
        }
        return history.getLast() - history.getFirst();
    }

    private int getMaxNegativeChangeSetSize(List<Integer> history) {
        return getMaxChangeSetSize(history, i -> i < 0);
    }

    private int getMaxPositiveChangeSetSize(List<Integer> history) {
        return getMaxChangeSetSize(history, i -> i > 0);
    }

    private int getMaxChangeSetSize(List<Integer> history) {
        return getMaxChangeSetSize(history, i -> true);
    }

    private int getMaxChangeSetSize(List<Integer> history, Predicate<Integer> filter) {
        return getDiff(history).stream()
                .filter(filter)
                .mapToInt(Math::abs)
                .max()
                .orElse(0);
    }

    public int getCodeSmells() {
        return codeSmells;
    }

    public void incCodeSmells() {
        codeSmells += 1;
    }

    public void setCodeSmells(int codeSmells) {
        this.codeSmells = codeSmells;
    }

    public double getCodeDuplication() {
        return codeDuplcation;
    }

    public void setCodeDuplcation(double codeDuplcation) {
        this.codeDuplcation = codeDuplcation;
    }

    public void updateCodeDuplication(double codeDuplication) {
        if (this.codeDuplcation + codeDuplication > 1) {
            throw new IllegalArgumentException("Code duplication cannot be greater than 1");
        }
        this.codeDuplcation += codeDuplication;

    }

    public int getHalsteadEffort() {
        return halsteadEffort;
    }

    public void setHalsteadEffort(int halsteadEffort) {
        this.halsteadEffort = halsteadEffort;
    }
}
