package io.github.francescodonnini.cli;

public enum CLI {
    ANALYZE,
    DATA,
    ML;

    public static CLI from(String s) {
        return switch (s.toLowerCase()) {
            case "analyze" -> CLI.ANALYZE;
            case "data" -> CLI.DATA;
            case "ml" -> CLI.ML;
            default -> throw new IllegalArgumentException("Unknown CLI workflow " + s);
        };
    }
}
