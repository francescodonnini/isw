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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class OrderedHoldOutEvaluator extends HoldOutSubsetEvaluator {
    private static final Logger logger = Logger.getLogger(OrderedHoldOutEvaluator.class.getName());
    private Classifier classifier;
    private Instances data;
    private AccuracyMetric metric;
    private double trainTestSplit;

    @Override
    public double evaluateSubset(BitSet subset) throws Exception {
        if (subset.isEmpty()) {
            logger.log(Level.WARNING, "subset is empty");
            return metric.defaultValue();
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
            case AccuracyMetric.ACCURACY -> eval.pctCorrect();
            case AccuracyMetric.AUC -> eval.areaUnderROC(positiveClassIndex);
            case AccuracyMetric.AUC_PRC -> eval.areaUnderPRC(positiveClassIndex);
            case AccuracyMetric.F1 -> eval.fMeasure(positiveClassIndex);
            case AccuracyMetric.KAPPA -> eval.kappa();
            case AccuracyMetric.MATTHEWS -> eval.matthewsCorrelationCoefficient(positiveClassIndex);
            case AccuracyMetric.PRECISION -> eval.precision(positiveClassIndex);
            case AccuracyMetric.RECALL -> eval.recall(positiveClassIndex);
        };
        logger.log(Level.INFO, "Evaluation result is {0} ({1})", new Object[] {score, filteredAttributes});
        return score;
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

    public void setMetric(AccuracyMetric metric) {
        this.metric = metric;
    }

    public void setTrainTestSplit(double trainTestSplit) {
        if (trainTestSplit < 0 || trainTestSplit > 1) {
            throw new IllegalArgumentException("trainTestSplit must be between 0 and 1");
        }
        this.trainTestSplit = trainTestSplit;
    }
}
