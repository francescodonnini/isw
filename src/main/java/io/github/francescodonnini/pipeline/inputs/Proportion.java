package io.github.francescodonnini.pipeline.inputs;

public enum Proportion {
    ColdStart,
    Incremental,
    MovingWindow,
    Simple;

    public static Proportion from(String s) {
        return switch (s.toLowerCase()) {
            case "cold-start" -> ColdStart;
            case "incremental" -> Incremental;
            case "moving-window" -> MovingWindow;
            case "simple" -> Simple;
            default -> throw new IllegalArgumentException("unknown proportion method " + s);
        };
    }
}
