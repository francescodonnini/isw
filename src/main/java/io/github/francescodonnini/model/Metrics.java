package io.github.francescodonnini.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Metrics {
    private int cyclomaticComplexity;
    private int lineOfCode;
    private final List<Integer> locHistory = new ArrayList<>();
    private int locAdded;
    private int locDeleted;
    private int parametersCount;
    private int statementsCount;
    private final List<Integer> statementHistory = new ArrayList<>();
    private int statementsAdded;
    private int statementsDeleted;
    private int churn;
    private double avgChurn;
    private int nestingDepth;
    private int elseCount;
    private final List<Integer> elseHistory = new ArrayList<>();
    private int elseAdded;
    private int elseDeleted;
    private final Set<String> authors = new HashSet<>();

    @Override
    public String toString() {
        return "Metrics{" +
                "cyclomaticComplexity=" + cyclomaticComplexity +
                "lineOfCode=" + lineOfCode +
                ", parametersCount=" + parametersCount +
                ", statementsCount=" + statementsCount +
                ", elseCount=" + elseCount +
                ", nestingDepth=" + nestingDepth +
                ", authorsCount=" + authors.size() +
                ", authors=" + String.join(",", authors) +
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
        if (!locHistory.isEmpty())
            locAdded = getAdded(locHistory);
        return locAdded;
    }

    public void setLocAdded(int locAdded) {
        this.locAdded = locAdded;
    }

    public int getLocDeleted() {
        if (!locHistory.isEmpty())
            locDeleted = getDeleted(locHistory);
        return locDeleted;
    }

    public void setLocDeleted(int locDeleted) {
        this.locDeleted = locDeleted;
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

    public double getAvgChurn() {
        if (!statementHistory.isEmpty())
            avgChurn = getAvgChangeSetSize(statementHistory);
        return avgChurn;
    }

    public void setAvgChurn(double avgChurn) {
        this.avgChurn = avgChurn;
    }

    public int getChurn() {
        if (!statementHistory.isEmpty())
            churn = getDiff(statementHistory)
                .stream()
                .reduce(0, Integer::sum);
        return churn;
    }

    public void setChurn(int churn) {
        this.churn = churn;
    }

    public int getStatementsAdded() {
        if (!statementHistory.isEmpty())
            statementsAdded = getAdded(statementHistory);
        return statementsAdded;
    }

    public void setStatementsAdded(int statementsAdded) {
        this.statementsAdded = statementsAdded;
    }

    public int getStatementsDeleted() {
        if (!statementHistory.isEmpty())
            statementsDeleted = getDeleted(statementHistory);
        return statementsDeleted;
    }

    public void setStatementsDeleted(int statementsDeleted) {
        this.statementsDeleted = statementsDeleted;
    }

    public void setStatementsCount(int statementsCount) {
        this.statementsCount = statementsCount;
    }

    public void addAuthor(String author) {
        authors.add(author);
    }

    public Set<String> getAuthors() {
        return authors;
    }

    public int getAuthorsCount() {
        return authors.size();
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
        if (!elseHistory.isEmpty())
            elseAdded = getAdded(elseHistory);
        return elseAdded;
    }

    public void setElseAdded(int elseAdded) {
        this.elseAdded = elseAdded;
    }

    public int getElseDeleted() {
        if (!elseHistory.isEmpty())
            elseDeleted = getDeleted(elseHistory);
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
        var prev = history.getFirst();
        for (var curr : history.subList(1, history.size())) {
            diff.add(curr - prev);
        }
        return diff;
    }

    private int getAdded(List<Integer> history) {
        return getDiff(history).stream()
                .filter(i -> i > 0)
                .reduce(0, Integer::sum);
    }

    private double getAvgChangeSetSize(List<Integer> history) {
        return getDiff(history).stream()
                .map(Integer::doubleValue)
                .reduce(0.0, Double::sum) / history.size();
    }

    private int getDeleted(List<Integer> history) {
        return getDiff(history).stream()
                .filter(i -> i < 0)
                .reduce(0, Integer::sum);
    }

    private int getMaxChangeSetSize(List<Integer> history) {
        return getDiff(history).stream()
                .max(Integer::compare)
                .orElse(0);
    }
}
