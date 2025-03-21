package io.github.francescodonnini.metrics;

public class LongMetric extends Metric {
    private long value;

    public LongMetric(String name) {
        this(name, 0);
    }

    public LongMetric(String name, int defaultValue) {
        super(name);
        this.value = defaultValue;
    }

    @Override
    public String toString() {
        return "IntMetric{name='%s', value='%d'}".formatted(getName(), value);
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}
