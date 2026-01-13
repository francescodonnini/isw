package io.github.francescodonnini.weka;

import weka.attributeSelection.HoldOutSubsetEvaluator;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Set;
import java.util.stream.IntStream;

public class OrderedHoldOutEvaluator extends HoldOutSubsetEvaluator {
    private static final Set<String> METRICS = Set.of(
            "accuracy",
            "auc",
            "auc-prc",
            "kappa",
            "matthews",
            "pearson",
            "precision",
            "recall",
            "weighted-auc",
            "weighted-auc-prc"
    );
    private Classifier classifier;
    private Instances data;
    private String metric;
    private double trainTestSplit;

    @Override
    public double evaluateSubset(BitSet subset) throws Exception {
        var remove = new Remove();
        remove.setInvertSelection(true);
        var indices = IntStream.concat(IntStream.of(data.classIndex()), subset.stream()).toArray();
        remove.setAttributeIndicesArray(indices);
        remove.setInputFormat(data);
        var filteredData = Filter.useFilter(data, remove);
        var trainSize = (int) (data.numInstances() * trainTestSplit);
        var train = new Instances(filteredData, 0, trainSize);
        var test = new Instances(filteredData, trainSize, data.numInstances() - trainSize);
        var localClassifier = AbstractClassifier.makeCopy(classifier);
        localClassifier.buildClassifier(train);
        var eval = new Evaluation(train);
        eval.evaluateModel(localClassifier, test);
        var positiveClassIndex = data.classAttribute().indexOfValue("1");
        return switch (metric) {
            case "accuracy" -> eval.pctCorrect();
            case "auc" -> eval.areaUnderROC(positiveClassIndex);
            case "auc-prc" -> eval.areaUnderPRC(positiveClassIndex);
            case "kappa" -> eval.kappa();
            case "matthews" -> eval.matthewsCorrelationCoefficient(positiveClassIndex);
            case "pearson" -> eval.correlationCoefficient();
            case "precision" -> eval.precision(positiveClassIndex);
            case "recall" -> eval.recall(positiveClassIndex);
            case "weighted-auc" -> eval.weightedAreaUnderROC();
            case "weighted-auc-prc" -> eval.weightedAreaUnderPRC();
            default -> throw new IllegalArgumentException("unknown metric " + metric);
        };
    }

    @Override
    public double evaluateSubset(BitSet subset, Instances holdOut) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public double evaluateSubset(BitSet subset, Instance holdOut, boolean retrain) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void buildEvaluator(Instances data) throws Exception {
        this.data = data;
    }

    public void setClassifier(Classifier classifier) {
        this.classifier = classifier;
    }

    public void setMetric(String metric) {
        if (!METRICS.contains(metric)) {
            throw new IllegalArgumentException("unknown metric " + metric);
        }
        this.metric = metric;
    }

    public void setTrainTestSplit(double trainTestSplit) {
        if (trainTestSplit < 0 || trainTestSplit > 1) {
            throw new IllegalArgumentException("trainTestSplit must be between 0 and 1");
        }
        this.trainTestSplit = trainTestSplit;
    }
}
