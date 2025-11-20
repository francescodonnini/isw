package io.github.francescodonnini.weka;

import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.RemoveByName;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.logging.Logger;

public class Trainer {
    private final Logger logger = Logger.getLogger(Trainer.class.getName());
    private final Path dataPath;

    public Trainer(Path dataPath) throws Exception {
        this.dataPath = dataPath;
    }

    public void train() throws Exception {
        var source = new ConverterUtils.DataSource(dataPath.toString());
        var data = source.getDataSet();
        if (data.classIndex() == -1) {
            data.setClassIndex(0);
        }
        var order = data.numAttributes() - 1;
        var distinct = new TreeSet<Integer>();
        for (var i : data) {
            distinct.add((int) i.value(order));
        }
        var releases = new ArrayList<>(distinct);
        logger.info("Found releases %s".formatted(String.join(",", releases.stream().map(String::valueOf).toList())));
        var models = new ArrayList<RandomForest>();
        var trainingSize = (int) Math.ceil(0.8 * releases.size());
        for (var validation = 1; validation < trainingSize; validation++) {
            int finalValidation = validation;
            var train = getBy(i -> i.value(order) < finalValidation, data);
            var test = getBy(i -> i.value(order) == finalValidation, data);
            if (train.numInstances() == 0 || test.numInstances() == 0) {
                logger.warning("No instances for release %d".formatted(validation));
                continue;
            }
            removeReleaseAttribute(train);
            removeReleaseAttribute(test);
            var model = new RandomForest();
            model.buildClassifier(train);
            var eval = new Evaluation(test);
            eval.evaluateModel(model, test);
            logger.info("Evaluation for release %d: %s".formatted(validation, eval.toSummaryString()));
            models.add(model);
        }
        var test = getBy(i -> i.value(order) >= trainingSize, data);
        for (var m : models) {
            var eval = new Evaluation(test);
            eval.evaluateModel(m, test);
            logger.info("Evaluation for test set: %s".formatted(eval.toSummaryString()));
        }
    }

    private static Instances getBy(Predicate<Instance> predicate, Instances data) {
        var result = new Instances(data, 0);
        for (var i : data) {
            if (predicate.test(i)) {
                result.add(i);
            }
        }
        return result;
    }

    private static Instances removeReleaseAttribute(Instances data) throws Exception {
        var remove = new RemoveByName();
        remove.setExpression("release");
        remove.setInputFormat(data);
        return Filter.useFilter(data, remove);
    }
}
