package io.github.francescodonnini.model;

import java.nio.file.Path;

public class JavaClass {
    private boolean buggy;
    private Path path;
    private final Release release;
    private final String content;

    public JavaClass(boolean buggy, String path, Release release, String content) {
        this(buggy, Path.of(path), release, content);
    }

    public JavaClass(boolean buggy, Path path, Release release, String content) {
        this.buggy = buggy;
        this.path = path;
        this.release = release;
        this.content = content;
    }

    public boolean isBuggy() {
        return buggy;
    }

    public void setBuggy(boolean buggy) {
        this.buggy = buggy;
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

    public Release getRelease() {
        return release;
    }

    public String getContent() {
        return content;
    }
}
