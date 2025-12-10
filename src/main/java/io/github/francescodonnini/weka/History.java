package io.github.francescodonnini.weka;

import weka.classifiers.Evaluation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class History {
    private final List<Evaluation> evaluationHistory = new ArrayList<>();
    private final int classIndex;

    public History(int classIndex) {
        this.classIndex = classIndex;
    }

    public void add(Evaluation evaluation) {
        evaluationHistory.add(evaluation);
    }

    public void clear() {
        evaluationHistory.clear();
    }

    public String getSummary() {
        var s = new StringBuilder();
        var i = 0;
        for (var evaluation : evaluationHistory) {
            s.append("#iteration ").append(++i).append("\n");
            s.append(evaluation.toSummaryString()).append("\n");
        }
        return s.toString();
    }

    public OptionalDouble avg(String metric) {
        return evaluationHistory.stream()
                .mapToDouble(e -> getMetric(e, metric))
                .average();
    }

    public Set<Metric> avg(Set<String> metrics) {
        var averages = new HashSet<Metric>();
        for (var metric : metrics) {
            avg(metric).ifPresent(x -> averages.add(new Metric(metric, x)));
        }
        return averages;
    }

    private Double getMetric(Evaluation e, String metric) {
        return switch (metric) {
            case "f1" -> e.fMeasure(classIndex);
            case "kappa" -> e.kappa();
            case "precision" -> e.precision(classIndex);
            case "recall" -> e.recall(classIndex);
            case "roc" -> e.areaUnderROC(classIndex);
            case "square" -> e.rootMeanSquaredError();
            default -> throw new IllegalArgumentException("Invalid metric: " + metric);
        };
    }

    public void save(Path path) throws IOException {
        try (var writer = Files.newBufferedWriter(path)) {
            writer.write("#iteration,#FN,#TN,#FP,#TP,precision,recall,kappa,ROC,abs,squared\n");
            var i = 1;
            for (var eval : evaluationHistory) {
                writer.write(row(i++, eval));
            }
        }
    }

    private String row(int i, Evaluation evaluation) {
        return new StringBuilder()
                .append(i).append(",")
                .append(evaluation.numFalseNegatives(classIndex)).append(",")
                .append(evaluation.numTrueNegatives(classIndex)).append(",")
                .append(evaluation.numFalsePositives(classIndex)).append(",")
                .append(evaluation.numTruePositives(classIndex)).append(",")
                .append(evaluation.precision(classIndex)).append(",")
                .append(evaluation.fMeasure(classIndex)).append(",")
                .append(evaluation.recall(classIndex)).append(",")
                .append(evaluation.kappa()).append(",")
                .append(evaluation.matthewsCorrelationCoefficient(classIndex)).append(",")
                .append(evaluation.areaUnderROC(classIndex)).append(",")
                .append(evaluation.meanAbsoluteError()).append(",")
                .append(evaluation.rootMeanSquaredError()).append("\n")
                .toString();
    }
}
