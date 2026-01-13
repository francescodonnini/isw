package io.github.francescodonnini.weka;

import weka.classifiers.Evaluation;

public record WalkForwardTrainingIteration(
    int release,
    Evaluation evaluation
) {}