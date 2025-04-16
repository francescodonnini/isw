package io.github.francescodonnini.model;

import java.nio.file.Path;
import java.util.Objects;

public class JavaClass {
    private Path parent;
    private Path path;
    private final Release release;
    private final String content;

    public JavaClass(String parent, String path, Release release, String content) {
        this(Path.of(parent), Path.of(path), release, content);
    }

    public JavaClass(Path parent, Path path, Release release, String content) {
        this.parent = parent;
        this.path = path;
        this.release = release;
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof JavaClass javaClass)) return false;
        return Objects.equals(parent, javaClass.parent) && Objects.equals(path, javaClass.path) && Objects.equals(release, javaClass.release) && Objects.equals(content, javaClass.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, path, release, content);
    }

    @Override
    public String toString() {
        return "%s %s".formatted(release.id(), path);
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

    public Release getRelease() {
        return release;
    }

    public String getContent() {
        return content;
    }
}
