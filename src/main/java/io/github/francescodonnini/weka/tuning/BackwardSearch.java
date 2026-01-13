package io.github.francescodonnini.weka.tuning;

import io.github.francescodonnini.weka.Dataset;
import io.github.francescodonnini.weka.History;
import io.github.francescodonnini.weka.WalkForwardTrainer;
import io.github.francescodonnini.weka.factories.FilteredModelFactory;
import io.github.francescodonnini.weka.factories.ModelFactory;
import weka.core.Attribute;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class BackwardSearch implements FeatureSelection {
    private final Logger logger = Logger.getLogger(BackwardSearch.class.getName());
    private final Dataset dataset;
    private Function<History, OptionalDouble> scoreFunction;

    public BackwardSearch(Dataset dataset) throws IOException {
        this.dataset = dataset;
        var file = new FileHandler("backward-search.log", true);
        file.setFormatter(new SimpleFormatter());
        logger.addHandler(file);
    }

    @Override
    public Set<Attribute> select(String model) throws Exception {
        if (scoreFunction == null) {
            throw new IllegalStateException("must specify at least one between maxFeatureSelected or scoreFunction");
        }

        var remaining = new HashSet<>(dataset.features());
        logger.log(
                Level.INFO,
                "initial feature set ({0}) ({1})",
                new Object[]{ remaining.size(), String.join(",", remaining.stream().map(Attribute::name).toList()) }
        );

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
            var worstFeature = remaining
                    .parallelStream()
                    .map(feature -> {
                        var localFactory = new FilteredModelFactory();
                        var currentFeatures = new HashSet<>(remaining);
                        currentFeatures.remove(feature);
                        try {
                            var score = train(model, localFactory, currentFeatures);
                            if (score.isEmpty()) {
                                return null;
                            }
                            return new ScoredAttribute(feature, score.getAsDouble());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }})
                    .filter(Objects::nonNull)
                    .max(Comparator.comparingDouble(ScoredAttribute::score));

            if (worstFeature.isPresent() && worstFeature.get().score() > currentScore) {
                currentScore = worstFeature.get().score();
                improved = true;
                var attr = worstFeature.get().attribute();
                logger.log(Level.INFO, "new worst feature: %s, score: %f".formatted(attr.name(), currentScore));
                logger.log(Level.INFO, "removing feature %s".formatted(attr.name()));
                remaining.remove(attr);
            }
        } while (improved);

        logger.log(
                Level.INFO,
                "selected feature set ({0}) ({1})",
                new Object[]{ remaining.size(), String.join(",", remaining.stream().map(Attribute::name).toList()) }
        );

        return remaining;
    }

    private OptionalDouble train(String model, FilteredModelFactory factory, Set<Attribute> attributes) throws Exception {
        factory.reset();
        factory.add(attributes);
        factory.add(dataset.classAttribute());
        return train(model, factory);
    }

    private OptionalDouble train(String model, ModelFactory factory) {
        var trainer = new WalkForwardTrainer(dataset, factory);
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
