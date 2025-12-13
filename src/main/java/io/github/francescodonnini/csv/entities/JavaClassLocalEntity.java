package io.github.francescodonnini.csv.entities;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import io.github.francescodonnini.csv.converters.LocalDateTimeConverter;

import java.time.LocalDateTime;
import java.util.Optional;

public class JavaClassLocalEntity {
    @CsvBindByName(column = "trackingId")
    private long trackingId;
    @CsvBindByName(column = "authors")
    private String author;
    @CsvBindByName(column = "commit", required = true)
    private String commit;
    @CsvBindByName(column = "path", required = true)
    private String path;
    @CsvBindByName(column = "parent", required = true)
    private String parent;
    @CsvBindByName(column = "name", required = true)
    private String name;
    @CsvCustomBindByName(column = "time", required = true, converter = LocalDateTimeConverter.class)
    private LocalDateTime time;
    @CsvBindByName(column = "topLevel")
    private boolean topLevel;

    public long getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(long trackingId) {
        this.trackingId = trackingId;
    }

    public Optional<String> getAuthor() {
        if (author == null || author.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(author);
    }

    public void setAuthor(String author) {
        this.author = author;
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

    public boolean isTopLevel() {
        return topLevel;
    }

    public void setTopLevel(boolean topLevel) {
        this.topLevel = topLevel;
    }
}
