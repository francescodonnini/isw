package io.github.francescodonnini.metrics;

public class LongMetric extends Metric {
    private final long value;

    public LongMetric(String name, long value) {
        super(name);
        this.value = value;
    }

    public long getValue() {
        return value;
    }
}
