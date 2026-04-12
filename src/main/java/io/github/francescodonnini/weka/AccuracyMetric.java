package io.github.francescodonnini.weka;

public enum AccuracyMetric {
    Accuracy,
    AUC,
    AUC_PRC,
    F1,
    Kappa,
    Matthews,
    Precision,
    Recall;

    public static AccuracyMetric fromString(String s) {
        return switch (s.toLowerCase()) {
            case "acc" -> Accuracy;
            case "auc" -> AUC;
            case "auc-prc" -> AUC_PRC;
            case "f1","f-score" -> F1;
            case "kappa" -> Kappa;
            case "matthews" -> Matthews;
            case "precision" -> Precision;
            case "recall" -> Recall;
            default -> throw new IllegalArgumentException("unknown metric " + s);
        };
    }

    public double defaultValue() {
        if (this.equals(Accuracy)) {
            return Double.MAX_VALUE;
        } else {
            return 0.0;
        }
    }
}
