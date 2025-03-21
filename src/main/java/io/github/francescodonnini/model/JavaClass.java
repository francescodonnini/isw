package io.github.francescodonnini.model;

public class JavaClass {
    private boolean buggy;
    private String path;
    private final Release release;
    private final String content;

    public JavaClass(boolean buggy, String path, Release release, String content) {
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Release getRelease() {
        return release;
    }

    public String getContent() {
        return content;
    }
}
