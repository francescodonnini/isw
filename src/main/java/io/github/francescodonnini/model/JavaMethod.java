package io.github.francescodonnini.model;

import io.github.francescodonnini.metrics.IntMetric;
import io.github.francescodonnini.metrics.LongMetric;
import io.github.francescodonnini.metrics.Metric;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class JavaMethod {
    private boolean buggy;
    private final String signature;
    private final JavaClass javaClass;
    private final String content;
    private final List<Metric> metrics = new ArrayList<>();

    public JavaMethod(boolean buggy, JavaClass javaClass, String signature, String content) {
        this.buggy = buggy;
        this.signature = signature;
        this.javaClass = javaClass;
        this.content = content;
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

    public void addMetric(String name, int value) {
        metrics.add(new IntMetric(name, value));
    }

    public void addMetric(String name, long value) {
        metrics.add(new LongMetric(name, value));
    }

    public Optional<Metric> getMetric(String name) {
        return metrics.stream().filter(metric -> metric.getName().equals(name)).findFirst();
    }

    public List<Metric> getMetrics() {
        return metrics;
    }
}
