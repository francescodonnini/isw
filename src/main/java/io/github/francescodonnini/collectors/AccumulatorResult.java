package io.github.francescodonnini.collectors;

public record AccumulatorResult<S, A>(S sum, A average, S max) {}
