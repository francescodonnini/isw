package io.github.francescodonnini.model;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class JavaClass {
    private String author;
    private String commit;
    private Path oldPath;
    private Path parent;
    private Path path;
    private String name;
    private LocalDateTime time;
    private final List<JavaMethod> methods = new ArrayList<>();

    public JavaClass(String commit, Path parent, Path path, String name, LocalDateTime time) {
        this(null, commit, null, parent, path, name, time);
    }

    public JavaClass(String author, String commit, Path oldPath, Path parent, Path path, String name, LocalDateTime time) {
        this.author = author;
        this.commit = commit;
        this.oldPath = oldPath;
        this.parent = parent;
        this.path = path;
        this.name = name;
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof JavaClass javaClass)) return false;
        return Objects.equals(author, javaClass.author)
                && Objects.equals(commit, javaClass.commit)
                && Objects.equals(parent, javaClass.parent)
                && Objects.equals(path, javaClass.path)
                && Objects.equals(name, javaClass.name)
                && Objects.equals(time, javaClass.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(author, commit, parent, path, name, time);
    }

    @Override
    public String toString() {
        return "(%s %s %s %s)".formatted(author, commit, path, name);
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

    public Path getRealPath() {
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

    public Optional<Path> getOldPath() {
        return Optional.ofNullable(oldPath);
    }

    public void setOldPath(Path oldPath) {
        this.oldPath = oldPath;
    }

    public String getName() {
        return name;
    }
}
