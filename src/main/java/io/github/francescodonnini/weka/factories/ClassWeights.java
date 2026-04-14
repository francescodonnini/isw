package io.github.francescodonnini.weka.factories;

import java.util.List;

public class ClassWeights {
    private ClassWeights() {}

    public static double[] classWeights(List<Boolean> y) {
        return classWeights(y, 0.9999);
    }

    public static double[] classWeights(List<Boolean> y, double beta) {
        var samples = List.of(
                y.stream().filter(b ->!b).count(),
                y.stream().filter(b -> b).count()
        );
        var effective = samples.stream()
                .map(count -> 1.0 - Math.pow(beta, count))
                .toList();
        var weights = effective.stream()
                .map(e -> (1.0 - beta) / e)
                .toList();
        var sum = weights.stream()
                .mapToDouble(Double::doubleValue)
                .sum();
        return weights.stream()
                .mapToDouble(w -> w / sum * samples.size())
                .toArray();
    }
}
