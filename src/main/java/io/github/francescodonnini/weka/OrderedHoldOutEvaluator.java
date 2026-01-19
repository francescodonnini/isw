package io.github.francescodonnini.weka;

import weka.attributeSelection.HoldOutSubsetEvaluator;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.util.BitSet;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class OrderedHoldOutEvaluator extends HoldOutSubsetEvaluator {
    private static final Set<String> METRICS = Set.of(
            "accuracy",
            "auc",
            "auc-prc",
            "f1",
            "kappa",
            "matthews",
            "pearson",
            "precision",
            "recall",
            "weighted-auc",
            "weighted-auc-prc"
    );
    private static final Logger logger = Logger.getLogger(OrderedHoldOutEvaluator.class.getName());
    private Classifier classifier;
    private Instances data;
    private String metric;
    private double trainTestSplit;

    @Override
    public double evaluateSubset(BitSet subset) throws Exception {
        if (subset.isEmpty()) {
            logger.log(Level.WARNING, "subset is empty");
            return defaultValue(metric);
        }
        var remove = new Remove();
        remove.setInvertSelection(true);
        var indices = IntStream.concat(IntStream.of(data.classIndex()), subset.stream()).toArray();
        remove.setAttributeIndicesArray(indices);
        remove.setInputFormat(data);
        var filteredData = Filter.useFilter(data, remove);

        var filteredAttributes = String.join(",", Collections.list(filteredData.enumerateAttributes())
                .stream()
                .map(Attribute::name)
                .toList());
        logger.log(Level.INFO, "feature set ({0})): {1}", new Object[]{filteredData.numAttributes(), filteredAttributes});

        var trainSize = (int) (filteredData.numInstances() * trainTestSplit);
        var train = new Instances(filteredData, 0, trainSize);
        var validation = new Instances(filteredData, trainSize, filteredData.numInstances() - trainSize);
        var localClassifier = AbstractClassifier.makeCopy(classifier);
        localClassifier.buildClassifier(train);
        var eval = new Evaluation(train);
        eval.evaluateModel(localClassifier, validation);
        var positiveClassIndex = filteredData.classAttribute().indexOfValue("1");
        var score = switch (metric) {
            case "accuracy" -> eval.pctCorrect();
            case "auc" -> eval.areaUnderROC(positiveClassIndex);
            case "auc-prc" -> eval.areaUnderPRC(positiveClassIndex);
            case "f1" -> eval.fMeasure(positiveClassIndex);
            case "kappa" -> eval.kappa();
            case "matthews" -> eval.matthewsCorrelationCoefficient(positiveClassIndex);
            case "pearson" -> eval.correlationCoefficient();
            case "precision" -> eval.precision(positiveClassIndex);
            case "recall" -> eval.recall(positiveClassIndex);
            case "weighted-auc" -> eval.weightedAreaUnderROC();
            case "weighted-auc-prc" -> eval.weightedAreaUnderPRC();
            default -> throw new IllegalArgumentException("unknown metric " + metric);
        };
        logger.log(Level.INFO, "Evaluation result is {0} ({1})", new Object[] {score, filteredAttributes});
        return score;
    }

    private double defaultValue(String metric) {
        return switch (metric) {
            case "accuracy" -> Double.MAX_VALUE;
            case "auc", "weighted-auc-prc", "auc-prc", "f1", "kappa", "matthews", "pearson", "precision", "recall",
                 "weighted-auc" -> 0.0;
            default -> throw new IllegalArgumentException("unknown metric " + metric);
        };
    }

    @Override
    public double evaluateSubset(BitSet subset, Instances holdOut) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double evaluateSubset(BitSet subset, Instance holdOut, boolean retrain) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void buildEvaluator(Instances data) {
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
