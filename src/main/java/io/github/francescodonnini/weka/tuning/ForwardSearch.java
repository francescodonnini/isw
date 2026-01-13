package io.github.francescodonnini.weka.tuning;

import io.github.francescodonnini.weka.*;
import io.github.francescodonnini.weka.factories.FilteredModelFactory;
import io.github.francescodonnini.weka.factories.ModelFactory;
import weka.core.Attribute;

import java.util.HashSet;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ForwardSearch implements FeatureSelection {
    private final Logger logger = Logger.getLogger(ForwardSearch.class.getName());
    private final Dataset dataset;
    private int maxFeatureSelected = 0;
    private Function<History, OptionalDouble> scoreFunction;

    public ForwardSearch(Dataset dataset) {
        this.dataset = dataset;
    }

    @Override
    public Set<Attribute> select(String model) throws Exception {
        if (maxFeatureSelected <= 0 || scoreFunction == null) {
            throw new IllegalStateException("must specify at least one between maxFeatureSelected or scoreFunction");
        }
        var factory = new FilteredModelFactory();
        var selected = new HashSet<Attribute>();
        var remaining = new HashSet<>(dataset.features());
        var improved = true;
        var currentBestScore = Double.NEGATIVE_INFINITY;
        while (!stop(selected, remaining) && improved) {
            logger.log(Level.INFO, "selected features: %s, remaining features: %s".formatted(String.join(",", selected.stream().map(Attribute::name).toList()), String.join(",", remaining.stream().map(Attribute::name).toList())));
            improved = false;
            var bestCandidate = (Attribute) null;
            for (var feature : remaining) {
                var candidate = new HashSet<>(selected);
                candidate.add(feature);

                factory.reset();
                factory.add(candidate);
                factory.add(dataset.classAttribute());
                var score = train(model, factory);
                if (score.isPresent() && score.getAsDouble() > currentBestScore) {
                    bestCandidate = feature;
                    currentBestScore = score.getAsDouble();
                    improved = true;
                }
            }
            if (bestCandidate != null) {
                selected.add(bestCandidate);
                remaining.remove(bestCandidate);
            }
        }
        return selected;
    }

    private boolean stop(Set<Attribute> selected, Set<Attribute> remaining) {
        return (maxFeatureSelected > 0 && selected.size() >= maxFeatureSelected) || remaining.isEmpty();
    }

    private OptionalDouble train(String model, ModelFactory factory) throws Exception {
        var trainer = new WalkForwardTrainer(dataset, factory);
        trainer.train(model);
        if (scoreFunction == null) {
            return OptionalDouble.empty();
        }
        return scoreFunction.apply(trainer.getHistory());
    }

    @Override
    public void setMaxFeatureSelected(int n) {
        maxFeatureSelected = n;
    }

    @Override
    public void setScoreFunction(Function<History, OptionalDouble> scoreFunction) {
        this.scoreFunction = scoreFunction;
    }
}
