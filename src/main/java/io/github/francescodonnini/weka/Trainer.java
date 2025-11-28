package io.github.francescodonnini.weka;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Trainer {
    private final Logger logger = Logger.getLogger(Trainer.class.getName());
    private final Dataset dataset;
    private final History history;

    public Trainer(Dataset dataset) {
        this.dataset = dataset;
        history = new History(dataset.getClassIndex());
    }

    public void train(String modelName) throws Exception {
        history.clear();
        var trainingRange = dataset.getTrainingRange();
        for (var validation = trainingRange.start() + 1; validation < trainingRange.endExcl(); validation++) {
            var trainingSet = dataset.trainingSet(0, validation);
            var validationSet = dataset.validationSet(validation);
            var model = createModel(modelName);
            model.buildClassifier(trainingSet);
            var eval = new Evaluation(validationSet);
            eval.evaluateModel(model, validationSet);
            logger.log(Level.INFO, "{}", "Evaluation for release %d: %s".formatted(validation, eval.toSummaryString()));
            history.add(eval);
        }
    }

    private static Classifier createModel(String name) {
        return switch (name) {
            case "j48" -> new J48();
            case "naive-bayes" -> new NaiveBayes();
            case "random-forest" -> new RandomForest();
            case "smo" -> new SMO();
            default -> throw new IllegalArgumentException("Unknown model: " + name);
        };
    }

    public History getHistory() {
        return history;
    }
}
