package io.github.francescodonnini.proportion;

import io.github.francescodonnini.model.Issue;

import java.util.List;

public class ColdStart implements Proportion {
    @Override
    public List<Issue> makeLabels() {
        return List.of();
    }
}
