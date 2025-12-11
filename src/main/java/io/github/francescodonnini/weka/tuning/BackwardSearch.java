package io.github.francescodonnini.weka.tuning;

import io.github.francescodonnini.weka.Dataset;
import io.github.francescodonnini.weka.History;
import io.github.francescodonnini.weka.Trainer;
import io.github.francescodonnini.weka.factories.FilteredModelFactory;
import io.github.francescodonnini.weka.factories.ModelFactory;
import weka.core.Attribute;

import java.util.HashSet;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BackwardSearch implements FeatureSelection {
    private final Logger logger = Logger.getLogger(BackwardSearch.class.getName());
    private final Dataset dataset;
    private Function<History, OptionalDouble> scoreFunction;

    public BackwardSearch(Dataset dataset) {
        this.dataset = dataset;
    }

    @Override
    public Set<Attribute> select(String model) throws Exception {
        if (scoreFunction == null) {
            throw new IllegalStateException("must specify at least one between maxFeatureSelected or scoreFunction");
        }
        var remaining = new HashSet<>(dataset.getFeatures());
        var factory = new FilteredModelFactory();
         var o = train(model, factory, remaining);
        if (o.isEmpty()) {
            throw new IllegalStateException("cannot train model on whole dataset");
        }
        var currentScore = o.getAsDouble();
        logger.log(Level.INFO, "initial score: %f".formatted(currentScore));
        boolean improved;
        do {
            improved = false;
            var worstFeature = (Attribute) null;
            for (var feature : remaining) {
                var currentFeatures = new HashSet<>(remaining);
                currentFeatures.remove(feature);
                var score = train(model, factory, currentFeatures);
                if (score.isPresent() && score.getAsDouble() > currentScore) {
                    worstFeature = feature;
                    currentScore = score.getAsDouble();
                    improved = true;
                    logger.log(Level.INFO, "new worst feature: %s, score: %f".formatted(worstFeature.name(), currentScore));
                }
            }
            if (worstFeature != null) {
                logger.log(Level.INFO, "removing feature %s".formatted(worstFeature.name()));
                remaining.remove(worstFeature);
            }
        } while (improved);
        return remaining;
    }

    private OptionalDouble train(String model, FilteredModelFactory factory, Set<Attribute> attributes) throws Exception {
        factory.reset();
        factory.add(attributes);
        factory.add(dataset.getClassAttribute());
        return train(model, factory);
    }

    private OptionalDouble train(String model, ModelFactory factory) throws Exception {
        var trainer = new Trainer(dataset, factory);
        trainer.train(model);
        if (scoreFunction == null) {
            return OptionalDouble.empty();
        }
        return scoreFunction.apply(trainer.getHistory());
    }

    @Override
    public void setMaxFeatureSelected(int n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setScoreFunction(Function<History, OptionalDouble> scoreFunction) {
        this.scoreFunction = scoreFunction;
    }
}
