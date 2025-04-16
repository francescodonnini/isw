package io.github.francescodonnini.metrics;

public class IntMetric extends Metric {
    private final int value;

    public IntMetric(String name, int value) {
        super(name);
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
