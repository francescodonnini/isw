package io.github.francescodonnini.weka;

import io.github.francescodonnini.weka.factories.ModelFactory;
import weka.classifiers.Evaluation;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Trainer {
    private final Logger logger = Logger.getLogger(Trainer.class.getName());
    private final Dataset dataset;
    private final ModelFactory factory;
    private final History history;

    public Trainer(Dataset dataset, ModelFactory factory) {
        this.dataset = dataset;
        this.factory = factory;
        history = new History(dataset.getClassIndex());
    }

    public void train(String modelName) throws Exception {
        train(modelName, false);
    }

    public void train(String modelName, boolean showHistory) throws Exception {
        history.clear();
        var trainingRange = dataset.getTrainingRange();
        for (var validation = trainingRange.start() + 1; validation < trainingRange.endExcl(); validation++) {
            var trainingSet = dataset.trainingSet(0, validation);
            var validationSet = dataset.validationSet(validation);
            var model = factory.create(modelName);
            model.buildClassifier(trainingSet);
            var eval = new Evaluation(validationSet);
            eval.evaluateModel(model, validationSet);
            if (showHistory) {
                logger.log(Level.INFO, "{0}", "Evaluation for release %d: %s".formatted(validation, eval.toSummaryString()));
            }
            history.add(eval);
        }
    }

    public History getHistory() {
        return history;
    }
}
