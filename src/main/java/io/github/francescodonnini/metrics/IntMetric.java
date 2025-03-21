package io.github.francescodonnini.metrics;

public class IntMetric extends Metric {
    private int value;

    public IntMetric(String name) {
        this(name, 0);
    }

    public IntMetric(String name, int defaultValue) {
        super(name);
        this.value = defaultValue;
    }

    @Override
    public String toString() {
        return "IntMetric{name='%s', value='%d'}".formatted(getName(), value);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
