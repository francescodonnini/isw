package io.github.francescodonnini.data.smell;

import com.opencsv.bean.CsvBindByName;

public class CsvReportEntity {

    @CsvBindByName(column = "Problem")
    private int problem;

    @CsvBindByName(column = "Package")
    private String packageName;

    @CsvBindByName(column = "File")
    private String filePath;

    @CsvBindByName(column = "Priority")
    private int priority;

    @CsvBindByName(column = "Line")
    private int line;

    @CsvBindByName(column = "Description")
    private String description;

    @CsvBindByName(column = "Rule set")
    private String ruleSet;

    @CsvBindByName(column = "Rule")
    private String rule;


    public CsvReportEntity() { // Il costruttore pubblico vuoto è richiesto da opencsv
    }

    public int getProblem() {
        return problem;
    }

    public void setProblem(int problem) {
        this.problem = problem;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRuleSet() {
        return ruleSet;
    }

    public void setRuleSet(String ruleSet) {
        this.ruleSet = ruleSet;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    @Override
    public String toString() {
        return "CsvReportEntity{" +
                "problem=" + problem +
                ", packageName='" + packageName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", priority=" + priority +
                ", line=" + line +
                ", description='" + description + '\'' +
                ", ruleSet='" + ruleSet + '\'' +
                ", rule='" + rule + '\'' +
                '}';
    }
}