package io.github.francescodonnini.weka.training;

import io.github.francescodonnini.weka.Dataset;
import io.github.francescodonnini.weka.factories.CostSensitiveModelFactory;
import io.github.francescodonnini.weka.factories.ModelFactory;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;

import java.util.logging.Level;
import java.util.logging.Logger;

public class WalkForwardTrainer {
    private final Logger logger = Logger.getLogger(WalkForwardTrainer.class.getName());
    private final Dataset dataset;
    private final ModelFactory factory;
    private final boolean useClassWeights;


    public WalkForwardTrainer(Dataset dataset, ModelFactory factory, boolean useClassWeights) {
        this.dataset = dataset;
        this.factory = factory;
        this.useClassWeights = useClassWeights;
    }

    public History train(String modelName) {
        return train(modelName, dataset, false);
    }

    public History train(String modelName, Dataset dataset, boolean showHistory) {
        var history = new History(dataset.classIndex());
        dataset
            .trainingRange()
            .stream()
            .skip(1)
            .limit(dataset.trainingRange().size() - 1)
            .map(validationStart -> train(modelName, validationStart, showHistory))
            .sorted(java.util.Comparator.comparingInt(WalkForwardTrainingIteration::release))
            .forEachOrdered(r -> history.add(r.evaluation()));
        var featureList = dataset.features().stream().map(Attribute::name).toList();
        logger.log(Level.INFO, "TRAINING FINISHED W/ ({0}) {1}", new Object[] {featureList.size(), String.join(",", featureList) });
        return history;
    }

    private WalkForwardTrainingIteration train(String modelName, int validationStart, boolean showHistory) {
        try {
            var trainingSet = dataset.trainingSet(0, validationStart);
            Classifier model;
            if (useClassWeights) {
                model = new CostSensitiveModelFactory(factory)
                        .setClassWeights(trainingSet)
                        .create(modelName);
            } else {
                model = factory.create(modelName);
            }
            model.buildClassifier(trainingSet);
            var validationSet = dataset.validationSet(validationStart);
            var eval = new Evaluation(validationSet);
            eval.evaluateModel(model, validationSet);
            if (showHistory) {
                logger.log(Level.INFO, "{0}", "Evaluation for release %d: %s".formatted(validationStart, eval.toSummaryString()));
            }
            return new WalkForwardTrainingIteration(validationStart, eval);
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
