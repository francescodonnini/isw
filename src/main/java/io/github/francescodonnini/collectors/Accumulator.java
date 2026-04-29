package io.github.francescodonnini.collectors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public abstract class Accumulator<T, D> {
    protected final List<T> items = new ArrayList<>();
    private final BiFunction<T, T, D> diffFunc;

    public Accumulator(BiFunction<T, T, D> diffFunc) {
        this.diffFunc = diffFunc;
    }

    public void add(T x) {
        items.add(x);
    }

    public List<D> diff(Predicate<D> pred) {
        if (items.size() < 2) {
            return List.of();
        }

        var result = new ArrayList<D>();
        for (var i = 1; i < items.size(); ++i) {
            var delta = diffFunc.apply(items.get(i), items.get(i - 1));
            if (pred.test(delta)) {
                result.add(delta);
            }
        }
        return result;
    }

    public abstract AccumulatorResult<D, ?> getResult(Predicate<D> pred);

    public AccumulatorResult<D, ?> getResult() {
        return getResult(unused -> true);
    }
}