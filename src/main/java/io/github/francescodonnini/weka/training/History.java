package io.github.francescodonnini.weka.training;

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

    public void save(Path path) throws IOException {
        try (var writer = Files.newBufferedWriter(path)) {
            writer.write("#iteration,#FN,#TN,#FP,#TP,precision,recall,F-score,kappa,matthews,AUC,AUC-PRC,abs,squared\n");
            var i = 1;
            for (var eval : evaluationHistory) {
                writer.write(row(i++, eval));
            }
        }
    }

    private String row(int i, Evaluation evaluation) {
        return i + "," +
                evaluation.numFalseNegatives(classIndex) + "," +
                evaluation.numTrueNegatives(classIndex) + "," +
                evaluation.numFalsePositives(classIndex) + "," +
                evaluation.numTruePositives(classIndex) + "," +
                evaluation.precision(classIndex) + "," +
                evaluation.recall(classIndex) + "," +
                evaluation.fMeasure(classIndex) + "," +
                evaluation.kappa() + "," +
                evaluation.matthewsCorrelationCoefficient(classIndex) + "," +
                evaluation.areaUnderROC(classIndex) + "," +
                evaluation.areaUnderPRC(classIndex) + "," +
                evaluation.meanAbsoluteError() + "," +
                evaluation.rootMeanSquaredError() + "\n";
    }
}
