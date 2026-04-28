package io.github.francescodonnini.model;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

public class RevisionJavaClass {
    public static class Builder {
        private Path path;
        private String content;
        private boolean topLevel;
        private String name;
        private ComplexityClassMetrics metrics;

        public Builder path(Path path) {
            this.path = path;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder topLevel(boolean topLevel) {
            this.topLevel = topLevel;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder metrics(ComplexityClassMetrics metrics) {
            this.metrics = metrics;
            return this;
        }

        public RevisionJavaClass build() {
            return new RevisionJavaClass(this);
        }

    }

    public static Builder builder() {
        return new RevisionJavaClass.Builder();
    }

    private final Path path;
    private final boolean topLevel;
    private final String name;
    private final ComplexityClassMetrics metrics;
    private LocalDateTime time;
    private String commit;
    private long trackingId;
    private String author;

    private RevisionJavaClass(Builder builder) {
        this.path = builder.path;
        this.topLevel = builder.topLevel;
        this.name = builder.name;
        this.metrics = builder.metrics;
    }

    public Path getPath() {
        return path;
    }

    public boolean isTopLevel() {
        return topLevel;
    }

    public String getName() {
        return name;
    }

    public ComplexityClassMetrics getMetrics() {
        return metrics;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public long getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(long trackingId) {
        this.trackingId = trackingId;
    }

    public Optional<String> getAuthor() {
        return Optional.ofNullable(author);
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
