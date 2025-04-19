package io.github.francescodonnini.history;

import java.time.LocalDateTime;

public class FileHistory {
    private final String commitId;
    private final String author;
    private final LocalDateTime commitTime;
    private final String content;

    public FileHistory(String commitId, String author, LocalDateTime commitTime, String content) {
        this.commitId = commitId;
        this.author = author;
        this.commitTime = commitTime;
        this.content = content;
    }

    @Override
    public String toString() {
        return "FileHistory{" +
                "author='" + author + '\'' +
                ", commitId='" + commitId + '\'' +
                ", commitTime=" + commitTime +
                ", content='" + (content.length() < 50 ? content : content.substring(0, 50) + "...") + '\'' +
                '}';
    }

    public String getAuthor() {
        return author;
    }

    public String getCommitId() {
        return commitId;
    }

    public LocalDateTime getCommitTime() {
        return commitTime;
    }

    public String getContent() {
        return content;
    }
}
