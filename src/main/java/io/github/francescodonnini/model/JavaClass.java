package io.github.francescodonnini.model;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class JavaClass {
    public static class Builder {
        private long trackingId;
        private String author;
        private String commit;
        private Path parent;
        private Path path;
        private boolean topLevel;
        private String name;
        private LocalDateTime time;

        public JavaClass create() {
            return new JavaClass(this);
        }

        public Builder trackingId(long trackingId) {
            this.trackingId = trackingId;
            return this;
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }

        public Builder commit(String commit) {
            this.commit = commit;
            return this;
        }

        public Builder parent(Path parent) {
            this.parent = parent;
            return this;
        }

        public Builder path(Path path) {
            this.path = path;
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

        public Builder time(LocalDateTime time) {
            this.time = time;
            return this;
        }
    }
    private final long trackingId;
    private String author;
    private final String commit;
    private final Path parent;
    private Path path;
    private final boolean topLevel;
    private final String name;
    private final LocalDateTime time;
    private final List<JavaMethod> methods = new ArrayList<>();

    public static Builder builder() {
        return new Builder();
    }

    private JavaClass(Builder builder) {
        this.trackingId = builder.trackingId;
        this.author = builder.author;
        this.commit = builder.commit;
        this.parent = builder.parent;
        this.path = builder.path;
        this.topLevel = builder.topLevel;
        this.name = builder.name;
        this.time = builder.time;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof JavaClass javaClass)) return false;
        return Objects.equals(trackingId, javaClass.trackingId)
                && Objects.equals(author, javaClass.author)
                && Objects.equals(commit, javaClass.commit)
                && Objects.equals(parent, javaClass.parent)
                && Objects.equals(path, javaClass.path)
                && Objects.equals(name, javaClass.name)
                && Objects.equals(time, javaClass.time)
                && Objects.equals(topLevel, javaClass.topLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trackingId, author, commit, parent, path, name, time, topLevel);
    }

    @Override
    public String toString() {
        return "%s(id=%d, %s %s %s %s)".formatted(topLevel ? "P" : "N", trackingId, author, commit, path, name);
    }

    public long getTrackingId() {
        return trackingId;
    }

    public void addMethod(JavaMethod method) {
        methods.add(method);
    }

    public List<JavaMethod> getMethods() {
        return methods;
    }

    public Path getParent() {
        return parent;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(String path) {
        setPath(Path.of(path));
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public Optional<String> getAuthor() {
        return Optional.ofNullable(author);
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCommit() {
        return commit;
    }

    public String getName() {
        return name;
    }

    public boolean isTopLevel() {
        return topLevel;
    }
}
