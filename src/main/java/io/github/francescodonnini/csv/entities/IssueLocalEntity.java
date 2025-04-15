package io.github.francescodonnini.csv.entities;

import com.opencsv.bean.CsvBindAndSplitByName;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import io.github.francescodonnini.csv.converters.LocalDateTimeConverter;

import java.time.LocalDateTime;
import java.util.List;

public class IssueLocalEntity {
    @CsvBindAndSplitByName(column = "Affected Versions", elementType = String.class, splitOn = ",", writeDelimiter = ",", collectionType = List.class)
    List<String> affectedVersions;

    @CsvCustomBindByName(column = "Created", required = true, converter = LocalDateTimeConverter.class)
    LocalDateTime created;

    @CsvBindByName(column = "Fix Version", required = true)
    String fixVersion;

    @CsvBindByName(column = "Opening Version", required = true)
    String openingVersion;

    @CsvBindByName(column = "Commits", required = true)
    @CsvBindAndSplitByName(elementType = String.class, splitOn = ",", writeDelimiter = ",")
    List<String> commits;

    @CsvBindByName(column = "Key", required = true)
    String key;

    @CsvBindByName(column = "project", required = true)
    String project;

    public List<String> getAffectedVersions() {
        return affectedVersions;
    }

    public void setAffectedVersions(List<String> affectedVersions) {
        this.affectedVersions = affectedVersions;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public String getFixVersion() {
        return fixVersion;
    }

    public void setFixVersion(String fixVersion) {
        this.fixVersion = fixVersion;
    }

    public String getOpeningVersion() {
        return openingVersion;
    }

    public void setOpeningVersion(String openingVersion) {
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
