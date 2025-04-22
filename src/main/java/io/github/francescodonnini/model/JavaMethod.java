package io.github.francescodonnini.model;

import java.nio.file.Path;
import java.util.Objects;

public class JavaMethod {
    private boolean buggy;
    private final String signature;
    private final JavaClass javaClass;
    private final LineRange range;
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
        this(buggy, javaClass, signature, range, new Metrics());
    }

    public JavaMethod(boolean buggy, JavaClass javaClass, String signature, LineRange range, Metrics metrics) {
        this.buggy = buggy;
        this.signature = signature;
        this.javaClass = javaClass;
        this.range = range;
        this.metrics = metrics;
        javaClass.addMethod(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof JavaMethod that)) return false;
        return buggy == that.buggy && Objects.equals(signature, that.signature) && Objects.equals(javaClass, that.javaClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(buggy, signature, javaClass);
    }

    @Override
    public String toString() {
        return signature;
    }

    public boolean isBuggy() {
        return buggy;
    }

    public void setBuggy(boolean buggy) {
        this.buggy = buggy;
    }

    public JavaClass getJavaClass() {
        return javaClass;
    }

    public String getSignature() {
        return signature;
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
