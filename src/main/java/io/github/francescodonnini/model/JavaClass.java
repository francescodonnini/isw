package io.github.francescodonnini.model;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class JavaClass {
    private long trackingId;
    private String author;
    private String commit;
    private Path parent;
    private Path path;
    private final boolean topLevel;
    private final String name;
    private final LocalDateTime time;
    private final List<JavaMethod> methods = new ArrayList<>();

    public JavaClass(long trackingId, String commit, Path parent, Path path, String name, LocalDateTime time, boolean topLevel) {
        this(trackingId,null, commit, parent, path, topLevel, name, time);
    }

    public JavaClass(long trackingId, String author, String commit, Path parent, Path path, boolean topLevel, String name, LocalDateTime time) {
        this.trackingId = trackingId;
        this.author = author;
        this.commit = commit;
        this.parent = parent;
        this.path = path;
        this.topLevel = topLevel;
        this.name = name;
        this.time = time;
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

    public void setTrackingId(long trackingId) {
        this.trackingId = trackingId;
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

    public void setParent(Path parent) {
        this.parent = parent;
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

    public Path getAbsolutePath() {
        return parent.resolve(path);
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

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public String getName() {
        return name;
    }

    public boolean isTopLevel() {
        return topLevel;
    }
}
