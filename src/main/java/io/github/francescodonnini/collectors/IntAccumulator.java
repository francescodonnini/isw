package io.github.francescodonnini.collectors;

import java.util.function.Predicate;

public class IntAccumulator extends Accumulator<Integer, Integer> {
    public IntAccumulator() {
        super((a, b) -> a - b);
    }

    @Override
    public AccumulatorResult<Integer, Double> getResult() {
        if (items.size() == 1) {
            return new AccumulatorResult<>(items.getFirst(), (double) items.getFirst(), items.getFirst());
        }
        return getResult(unused -> true);
    }

    @Override
    public AccumulatorResult<Integer, Double> getResult(Predicate<Integer> pred) {
        var diff = diff(pred);
        var sum = diff.stream().mapToInt(Integer::intValue).sum();
        var average = diff.stream().mapToInt(Integer::intValue).average().orElse(0);
        var max = diff.stream().mapToInt(Integer::intValue).max().orElse(0);
        return new AccumulatorResult<>(sum, average, max);
    }
}
