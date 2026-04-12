package io.github.francescodonnini.pipeline.inputs;

public enum Proportion {
    COLD_START,
    INCREMENTAL,
    MOVING_WINDOW,
    SIMPLE;

    public static Proportion from(String s) {
        return switch (s.toLowerCase()) {
            case "cold-start" -> COLD_START;
            case "incremental" -> INCREMENTAL;
            case "moving-window" -> MOVING_WINDOW;
            case "simple" -> SIMPLE;
            default -> throw new IllegalArgumentException("unknown proportion method " + s);
        };
    }
}
