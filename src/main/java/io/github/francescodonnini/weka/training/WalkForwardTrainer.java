package io.github.francescodonnini.weka.training;

import io.github.francescodonnini.weka.Dataset;
import io.github.francescodonnini.weka.factories.ModelFactory;
import weka.classifiers.Evaluation;
import weka.core.Attribute;

import java.util.logging.Level;
import java.util.logging.Logger;

public class WalkForwardTrainer {
    private final Logger logger = Logger.getLogger(WalkForwardTrainer.class.getName());
    private final Dataset dataset;
    private final ModelFactory factory;
    private final History history;

    public WalkForwardTrainer(Dataset dataset, ModelFactory factory) {
        this.dataset = dataset;
        this.factory = factory;
        history = new History(dataset.classIndex());
    }

    public void train(String modelName) {
        train(modelName, false);
    }

    public void train(String modelName, boolean showHistory) {
        history.clear();
        dataset
            .trainingRange()
            .stream()
            .skip(1)
            .limit(dataset.trainingRange().size() - 1)
            .parallel()
            .map(validationStart -> train(modelName, validationStart, showHistory))
            .sorted(java.util.Comparator.comparingInt(WalkForwardTrainingIteration::release))
            .forEachOrdered(r -> history.add(r.evaluation()));
        var featureList = dataset.features().stream().map(Attribute::name).toList();
        logger.log(Level.INFO, "TRAINING FINISHED W/ ({0}) {1}", new Object[] {featureList.size(), String.join(",", featureList) });
    }

    private WalkForwardTrainingIteration train(String modelName, int validationStart, boolean showHistory) {
        try {
            var model = factory.create(modelName);
            model.buildClassifier(dataset.trainingSet(0, validationStart));
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

    public History getHistory() {
        return history;
    }
}
