package io.github.francescodonnini.sqlite;

import java.nio.file.Path;

public class JavaMethodLocalEntity {
    private boolean buggy;
    private String signature;
    private Path path;
    private String releaseId;
    private long lineStart;
    private long lineEnd;
    private String content;

    public boolean isBuggy() {
        return buggy;
    }

    public void setBuggy(boolean buggy) {
        this.buggy = buggy;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public String getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(String releaseId) {
        this.releaseId = releaseId;
    }

    public long getLineStart() {
        return lineStart;
    }

    public void setLineStart(long lineStart) {
        this.lineStart = lineStart;
    }

    public long getLineEnd() {
        return lineEnd;
    }

    public void setLineEnd(long lineEnd) {
        this.lineEnd = lineEnd;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "%s\t%s\t%s".formatted(signature, path, releaseId);
    }
}
