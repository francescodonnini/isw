package io.github.francescodonnini.metrics;

public abstract class Metric {
    private final String name;

    protected Metric(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
