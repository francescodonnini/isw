package io.github.francescodonnini.model;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class ReleaseJavaClass {
    public static class Builder {
        private Path path;
        private String name;
        private long trackingId;
        private int order;
        private LocalDateTime time;
        private ComplexityClassMetrics complexityMetrics;
        private ProcessClassMetrics processMetrics;
        private final Set<String> commits = new HashSet<>();

        public Builder path(Path path) {
            this.path = path;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder trackingId(long trackingId) {
            this.trackingId = trackingId;
            return this;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public Builder time(LocalDateTime time) {
            this.time = time;
            return this;
        }

        public Builder complexity(ComplexityClassMetrics complexityMetrics) {
            this.complexityMetrics = complexityMetrics;
            return this;
        }

        public Builder process(ProcessClassMetrics processMetrics) {
            this.processMetrics = processMetrics;
            return this;
        }

        public Builder commits(Set<String> changeSet) {
            this.commits.addAll(changeSet);
            return this;
        }

        public ReleaseJavaClass build() {
            return new ReleaseJavaClass(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final Path path;
    private final String name;
    private final long trackingId;
    private final int order;
    private final LocalDateTime time;
    private final ComplexityClassMetrics complexityMetrics;
    private final ProcessClassMetrics processMetrics;
    private final Set<String> commits = new HashSet<>();
    private boolean buggy;

    private ReleaseJavaClass(Builder builder) {
        this.path = builder.path;
        this.name = builder.name;
        this.trackingId = builder.trackingId;
        this.order = builder.order;
        this.time = builder.time;
        this.commits.addAll(builder.commits);
        this.complexityMetrics = builder.complexityMetrics;
        this.processMetrics = builder.processMetrics;
    }

    public Path getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public long getTrackingId() {
        return trackingId;
    }

    public int getOrder() {
        return order;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public ComplexityClassMetrics getComplexityMetrics() {
        return complexityMetrics;
    }

    public ProcessClassMetrics getProcessMetrics() {
        return processMetrics;
    }

    public Set<String> getCommits() {
        return commits;
    }

    public boolean isBuggy() {
        return buggy;
    }

    public void setBuggy(boolean buggy) {
        this.buggy = buggy;
    }
}
