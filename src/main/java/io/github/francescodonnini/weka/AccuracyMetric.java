package io.github.francescodonnini.weka;

public enum AccuracyMetric {
    ACCURACY,
    AUC,
    AUC_PRC,
    F1,
    KAPPA,
    MATTHEWS,
    PRECISION,
    RECALL;

    public static AccuracyMetric fromString(String s) {
        return switch (s.toLowerCase()) {
            case "acc" -> ACCURACY;
            case "auc" -> AUC;
            case "auc-prc" -> AUC_PRC;
            case "f1","f-score" -> F1;
            case "kappa" -> KAPPA;
            case "matthews" -> MATTHEWS;
            case "precision" -> PRECISION;
            case "recall" -> RECALL;
            default -> throw new IllegalArgumentException("unknown metric " + s);
        };
    }

    public double defaultValue() {
        if (this.equals(ACCURACY)) {
            return Double.MAX_VALUE;
        } else {
            return 0.0;
        }
    }
}
