package io.github.francescodonnini.collectors;

import java.util.function.Predicate;

public class DoubleAccumulator extends Accumulator<Double, Double, Double> {
    public DoubleAccumulator() {
        super((a, b) -> a - b);
    }

    @Override
    public AccumulatorResult<Double, Double> getResult() {
        if (items.size() == 1) {
            return new AccumulatorResult<>(items.getFirst(), items.getFirst(), items.getFirst());
        }
        return getResult(unused -> true);
    }

    @Override
    public AccumulatorResult<Double, Double> getResult(Predicate<Double> pred) {
        var diff = diff(pred);
        var sum = diff.stream().mapToDouble(Double::doubleValue).sum();
        var average = diff.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        var max = diff.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        return new AccumulatorResult<>(sum, average, max);
    }
}
