package io.github.francescodonnini.csv.entities;

import com.opencsv.bean.CsvBindAndSplitByName;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import com.opencsv.bean.CsvRecurse;

import java.time.LocalDateTime;
import java.util.Set;

public class RevisionClassLocalEntity {
    @CsvBindByName(column = "name", required = true)
    private String name;
    @CsvRecurse
    private ClassComplexityMetricsLocalEntity metrics;
    @CsvCustomBindByName(column = "time", required = true, converter = io.github.francescodonnini.csv.converters.LocalDateTimeConverter.class)
    private LocalDateTime time;
    @CsvBindByName(column = "topLevel")
    private boolean topLevel;
    @CsvBindByName(column = "parent")
    private String parent;
    @CsvBindByName(column = "path", required = true)
    private String path;
    @CsvBindByName(column = "trackingId")
    private long trackingId;
    @CsvBindByName(column = "author")
    private String author;
    @CsvBindByName(column = "commit", required = true)
    private String commit;
    @CsvBindAndSplitByName(column = "dependencies", elementType = String.class, splitOn = "\\|", writeDelimiter = "|")
    private Set<String> dependencies;
    @CsvBindByName(column = "entropy", required = true)
    private double entropy;
    @CsvBindByName(column = "fileExp", required = true)
    private int fileExp;
    @CsvBindByName(column = "devExp", required = true)
    private int devExp;

    public int getFileExp() {
        return fileExp;
    }

    public void setFileExp(int fileExp) {
        this.fileExp = fileExp;
    }

    public int getDevExp() {
        return devExp;
    }

    public void setDevExp(int devExp) {
        this.devExp = devExp;
    }

    public double getEntropy() {
        return entropy;
    }

    public void setEntropy(double entropy) {
        this.entropy = entropy;
    }

    public long getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(long trackingId) {
        this.trackingId = trackingId;
    }

    public java.util.Optional<String> getAuthor() {
        if (author == null || author.isEmpty()) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(author);
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ClassComplexityMetricsLocalEntity getMetrics() {
        return metrics;
    }

    public void setMetrics(ClassComplexityMetricsLocalEntity metrics) {
        this.metrics = metrics;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public boolean isTopLevel() {
        return topLevel;
    }

    public void setTopLevel(boolean topLevel) {
        this.topLevel = topLevel;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Set<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Set<String> dependencies) {
        this.dependencies = dependencies;
    }
}