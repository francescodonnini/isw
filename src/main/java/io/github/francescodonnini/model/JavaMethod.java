package io.github.francescodonnini.model;

import java.nio.file.Path;
import java.util.Objects;

public class JavaMethod {
    private boolean buggy;
    private final String signature;
    private final JavaClass javaClass;
    private final LineRange range;
    private String content;
    // lineOfCode
    // cyclomaticComplexity
    // parametersCount
    // statementsCount
    // locTouched
    // churn
    // numOfRevisions
    // numOfAuthors
    // locAdded
    // avgLocAdded
    // changeSetSize
    // maxChangeSetSize
    // avgChangeSetSize
    private final Metrics metrics;


    public JavaMethod(boolean buggy, JavaClass javaClass, String signature, LineRange range) {
        this(buggy, javaClass, signature, range, "", new Metrics());
    }

    public JavaMethod(boolean buggy, JavaClass javaClass, String signature, LineRange range, String content) {
        this(buggy, javaClass, signature, range, content, new Metrics());
    }

    public JavaMethod(boolean buggy, JavaClass javaClass, String signature, LineRange range, String content, Metrics metrics) {
        this.buggy = buggy;
        this.signature = signature;
        this.javaClass = javaClass;
        this.range = range;
        this.content = content;
        this.metrics = metrics;
        javaClass.addMethod(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof JavaMethod that)) return false;
        return buggy == that.buggy && Objects.equals(signature, that.signature) && Objects.equals(javaClass, that.javaClass) && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(buggy, signature, javaClass, content);
    }

    @Override
    public String toString() {
        return "(%s, %s)".formatted(javaClass.getRelease().id(), signature);
    }

    public boolean isBuggy() {
        return buggy;
    }

    public void setBuggy(boolean buggy) {
        this.buggy = buggy;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public JavaClass getJavaClass() {
        return javaClass;
    }

    public String getSignature() {
        return signature;
    }

    public Release getRelease() {
        return javaClass.getRelease();
    }

    public Path getPath() {
        return javaClass.getPath();
    }

    public void setPath(Path path) {
        javaClass.setPath(path);
    }

    public long getStartLine() {
        return range.start();
    }

    public long getEndLine() {
        return range.end();
    }

    public LineRange getLineRange() {
        return range;
    }

    public Metrics getMetrics() {
        return metrics;
    }
}
