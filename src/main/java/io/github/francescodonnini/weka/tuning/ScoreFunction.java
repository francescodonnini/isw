package io.github.francescodonnini.weka.tuning;

import io.github.francescodonnini.weka.History;

import java.util.OptionalDouble;
import java.util.function.Function;

public class ScoreFunction implements Function<History, OptionalDouble> {
    private final String metric;

    public ScoreFunction(String metric) {
        this.metric = metric;
    }

    @Override
    public OptionalDouble apply(History history) {
        return history.avg(metric);
    }
}
