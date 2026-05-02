package io.github.francescodonnini.collectors;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Predicate;

public class TimeAccumulator extends Accumulator<LocalDateTime, Duration, Duration> {

    public TimeAccumulator() {
        super((a, b) -> Duration.between(b, a));
    }

    @Override
    public AccumulatorResult<Duration, Duration> getResult(Predicate<Duration> pred) {
        var diff = diff(pred);
        if (diff.isEmpty()) {
            return new AccumulatorResult<>(Duration.ZERO, Duration.ZERO, Duration.ZERO);
        }
        var sum = diff.stream().reduce(Duration.ZERO, Duration::plus);
        var average = sum.dividedBy(diff.size());
        var max = diff.stream().max(Duration::compareTo).orElse(Duration.ZERO);
        return new AccumulatorResult<>(sum, average, max);
    }
}
