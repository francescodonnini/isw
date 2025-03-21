package io.github.francescodonnini.csv.entities;

import com.opencsv.bean.CsvBindAndSplitByName;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import io.github.francescodonnini.csv.converters.LocalDateTimeConverter;

import java.time.LocalDateTime;
import java.util.List;

public class IssueLocalEntity {
    @CsvBindAndSplitByName(column = "Affected Versions", elementType = Integer.class, splitOn = ",", writeDelimiter = ",", collectionType = List.class)
    List<Integer> affectedVersions;

    @CsvCustomBindByName(column = "Created", required = true, converter = LocalDateTimeConverter.class)
    LocalDateTime created;

    @CsvBindByName(column = "Fix Version", required = true)
    int fixVersion;

    @CsvBindByName(column = "Opening Version", required = true)
    int openingVersion;

    @CsvBindByName(column = "Commits", required = true)
    @CsvBindAndSplitByName(elementType = String.class, splitOn = ",", writeDelimiter = ",")
    List<String> commits;

    @CsvBindByName(column = "Key", required = true)
    String key;

    @CsvBindByName(column = "project", required = true)
    String project;

    public List<Integer> getAffectedVersions() {
        return affectedVersions;
    }

    public void setAffectedVersions(List<Integer> affectedVersions) {
        this.affectedVersions = affectedVersions;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public int getFixVersion() {
        return fixVersion;
    }

    public void setFixVersion(int fixVersion) {
        this.fixVersion = fixVersion;
    }

    public int getOpeningVersion() {
        return openingVersion;
    }

    public void setOpeningVersion(int openingVersion) {
        this.openingVersion = openingVersion;
    }

    public List<String> getCommits() {
        return commits;
    }

    public void setCommits(List<String> commits) {
        this.commits = commits;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }
}
