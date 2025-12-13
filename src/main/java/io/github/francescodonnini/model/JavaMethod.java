package io.github.francescodonnini.model;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class JavaMethod {
    private boolean buggy;
    private final String signature;
    private final JavaClass javaClass;
    private final LineRange range;
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
        return "%s on %s".formatted(signature, javaClass.getTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
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

    public long getTrackingId() {
        return javaClass.getTrackingId();
    }

    public Path getPath() {
        return javaClass.getPath();
    }

    public void setPath(Path path) {
        javaClass.setPath(path);
    }

    public LineRange getRange() {
        return range;
    }

    public int getStartLine() {
        return range.start();
    }

    public int getEndLine() {
        return range.end();
    }

    public Metrics getMetrics() {
        return metrics;
    }
}
