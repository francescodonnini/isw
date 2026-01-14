package io.github.francescodonnini.weka.training;

import weka.classifiers.Evaluation;

public record WalkForwardTrainingIteration(
    int release,
    Evaluation evaluation
) {}