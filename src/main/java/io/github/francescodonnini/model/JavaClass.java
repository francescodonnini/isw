package io.github.francescodonnini.model;

import java.nio.file.Path;

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
