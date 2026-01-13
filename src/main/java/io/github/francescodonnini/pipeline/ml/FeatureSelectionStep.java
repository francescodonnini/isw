package io.github.francescodonnini.pipeline.ml;

import io.github.francescodonnini.pipeline.MLPipelineContext;
import io.github.francescodonnini.pipeline.MLWorkloadInfo;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.weka.OrderedHoldOutEvaluator;
import weka.attributeSelection.*;
import weka.classifiers.CostMatrix;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FeatureSelectionStep implements Step<MLWorkloadInfo, MLWorkloadInfo> {
    private final Logger logger = Logger.getLogger(FeatureSelectionStep.class.getName());
    private final MLPipelineContext context;

    public FeatureSelectionStep(MLPipelineContext context) {
        this.context = context;
    }

    @Override
    public MLWorkloadInfo execute(MLWorkloadInfo input) throws Exception {
        var classifier = new CostSensitiveClassifier();
        classifier.setClassifier(new RandomForest());
        classifier.setCostMatrix(costMatrix(input));
        classifier.setMinimizeExpectedCost(true);

        var evaluator = new OrderedHoldOutEvaluator();
        evaluator.setClassifier(classifier);
        evaluator.setTrainTestSplit(0.8);
        evaluator.setMetric("kappa");

        var search = new GreedyStepwise();
        search.setSearchBackwards(context.isBackwardSearch());
        search.setNumExecutionSlots(Runtime.getRuntime().availableProcessors());

        var selector = new AttributeSelection();
        selector.setEvaluator(evaluator);
        selector.setSearch(search);

        var trainingSet = input.getDataset().trainingSet();
        selector.SelectAttributes(trainingSet);
        var selected = selector.selectedAttributes();
        logger.log(Level.INFO, "Selected attributes ({0}): {1}", new Object[] {selected.length, String.join(",", Arrays.stream(selected).mapToObj(trainingSet::attribute).map(Attribute::name).toList())});
        return input;
    }

    private CostMatrix costMatrix(MLWorkloadInfo input) {
        var dataset = input.getDataset();
        var weights = ClassWeights.classWeights(dataset.trainingSet().stream().map(i -> i.classValue() == 1.0).toList());
        var costs = new CostMatrix(2);
        costs.setElement(0, 0, 0.0);
        costs.setElement(0, 1, weights[0]);
        costs.setElement(1, 0, weights[1]);
        costs.setElement(1, 1, 0.0);
        return costs;
    }
}
