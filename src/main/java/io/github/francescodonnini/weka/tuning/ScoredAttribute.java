package io.github.francescodonnini.weka.tuning;

import weka.core.Attribute;

public record ScoredAttribute(
        Attribute attribute,
        double score
) {}
