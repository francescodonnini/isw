package io.github.francescodonnini.proportion;

import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.Release;

import java.util.*;
import java.util.logging.Logger;

public class Incremental implements Proportion {
    private static final Logger logger = Logger.getLogger(Incremental.class.getName());
    private final List<Issue> issues;
    private final List<Release> projectReleases;
    private final boolean complete;

    public Incremental(List<Issue> issues, List<Release> projectReleases, boolean complete) {
        this.issues = issues;
        this.projectReleases = projectReleases;
        this.complete = complete;
    }

    @Override
    public List<Issue> makeLabels(String projectName) {
        var labeled = ProportionUtils.getLabelled(issues);
        var unlabeled = ProportionUtils.getUnlabelled(issues);
        var result = new ArrayList<>(labeled);
        if (complete) {
            calculateProportion(labeled, projectReleases.getLast())
                    .ifPresent(p -> result.addAll(ProportionUtils.applyP(unlabeled, p, projectReleases)));
        } else {
            for (var r : projectReleases.subList(1, projectReleases.size())) {
                var l = labeled.stream().filter(i -> !i.fixVersion().isAfter(r)).toList();
                var u = unlabeled.stream().filter(i -> i.fixVersion().equals(r)).toList();
                calculateProportion(l, r)
                        .ifPresent(p -> result.addAll(ProportionUtils.applyP(u, p, projectReleases)));
            }
        }
        return result;
    }

    private OptionalDouble calculateProportion(List<Issue> issues, Release r) {
        return issues.stream()
                .filter(i -> !i.fixVersion().isAfter(r))
                .mapToDouble(this::calculateP)
                .average();
    }

    private double calculateP(Issue i) {
        var fv = i.fixVersion();
        var ov = i.openingVersion();
        var iv = i.affectedVersions().getFirst();
        assertTrue(fv.isAfter(ov), "issue %s: fix version %s must come after opening version %s".formatted(i, fv, ov));
        var d = (double) (fv.order() - ov.order());
        if (d == 0) {
            d = 1;
        }
        assertTrue(iv.isBefore(fv), "issue %s: injected version %s must come before fix version %s".formatted(i, iv, fv));
        assertTrue(iv.order() < fv.order(), "%s,%s: %d should be smaller than %d".formatted(iv, fv, iv.order(), fv.order()));
        return (fv.order() - iv.order()) / d;
    }

    private void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}