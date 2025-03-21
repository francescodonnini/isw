package io.github.francescodonnini.metrics;

import java.util.Objects;

public abstract class Metric {
    private final String name;

    public Metric(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Metric metric)) return false;
        return Objects.equals(name, metric.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
