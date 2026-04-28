package io.github.francescodonnini.csv.entities;

import com.opencsv.bean.CsvBindAndSplitByName;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import com.opencsv.bean.CsvRecurse;
import io.github.francescodonnini.csv.converters.LocalDateTimeConverter;

import java.time.LocalDateTime;
import java.util.Set;

public class ReleaseClassLocalEntity {
    @CsvBindByName(column = "version", required = true)
    private int order;
    @CsvBindByName(column = "path", required = true)
    private String path;
    @CsvBindByName(column = "name", required = true)
    private String name;
    @CsvCustomBindByName(column = "time", required = true, converter = LocalDateTimeConverter.class)
    private LocalDateTime time;
    @CsvBindByName(column = "buggy", required = true)
    private boolean buggy;
    @CsvRecurse
    private ClassComplexityMetricsLocalEntity complexityMetrics;
    @CsvRecurse
    private ClassProcessMetricsLocalEntity processMetrics;
    @CsvBindAndSplitByName(column = "commits", elementType = String.class, splitOn = "\\|", writeDelimiter = "|")
    private Set<String> commits;

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public boolean isBuggy() {
        return buggy;
    }

    public void setBuggy(boolean buggy) {
        this.buggy = buggy;
    }

    public ClassComplexityMetricsLocalEntity getComplexityMetrics() {
        return complexityMetrics;
    }

    public void setComplexityMetrics(ClassComplexityMetricsLocalEntity complexityMetrics) {
        this.complexityMetrics = complexityMetrics;
    }

    public ClassProcessMetricsLocalEntity getProcessMetrics() {
        return processMetrics;
    }

    public void setProcessMetrics(ClassProcessMetricsLocalEntity processMetrics) {
        this.processMetrics = processMetrics;
    }

    public Set<String> getCommits() {
        return commits;
    }

    public void setCommits(Set<String> commits) {
        this.commits = commits;
    }
}
