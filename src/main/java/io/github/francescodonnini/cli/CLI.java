package io.github.francescodonnini.cli;

public enum CLI {
    DATA,
    ML;

    public static CLI from(String s) {
        return switch (s.toLowerCase()) {
            case "data" -> CLI.DATA;
            case "ml" -> CLI.ML;
            default -> throw new IllegalArgumentException("Unknown CLI workflow " + s);
        };
    }
}
