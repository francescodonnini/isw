package io.github.francescodonnini.cli;

public enum CLI {
    DATA,
    EVAL,
    ML;

    public static CLI from(String s) {
        return switch (s.toLowerCase()) {
            case "data" -> CLI.DATA;
            case "ml" -> CLI.ML;
            case "eval" -> CLI.EVAL;
            default -> throw new IllegalArgumentException("Unknown CLI workflow " + s);
        };
    }
}
