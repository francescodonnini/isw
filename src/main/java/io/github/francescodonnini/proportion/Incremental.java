package io.github.francescodonnini.proportion;

import io.github.francescodonnini.data.ReleaseApi;
import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.Release;

import java.util.*;

public class Incremental implements Proportion {
    private final List<Issue> issues;
    private final ReleaseApi releaseApi;
    private final boolean complete;

    public Incremental(List<Issue> issues, ReleaseApi releaseApi, boolean complete) {
        this.issues = issues;
        this.releaseApi = releaseApi;
        this.complete = complete;
    }

    @Override
    public List<Issue> makeLabels(String projectName) {
        var labeled = ProportionUtils.getLabelled(issues);
        var unlabeled = ProportionUtils.getUnlabelled(issues);
        var releases = releaseApi.getReleases(projectName);
        var result = new ArrayList<>(labeled);
        if (complete) {
            calculateProportion(labeled, releases.getLast())
                    .ifPresent(p -> result.addAll(ProportionUtils.applyP(unlabeled, p, releases)));
        } else {
            for (var r : releases.subList(1, releases.size())) {
                var l = labeled.stream().filter(i -> !i.fixVersion().isAfter(r)).toList();
                var u = unlabeled.stream().filter(i -> i.fixVersion().equals(r)).toList();
                calculateProportion(l, r)
                        .ifPresent(p -> result.addAll(ProportionUtils.applyP(u, p, releases)));
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
        assertTrue(!fv.isBefore(ov), "issue %s: fix version %s must not come before opening version %s".formatted(i, fv, ov));
        var d = (double) (fv.order() - ov.order());
        if (d == 0) {
            d = 1;
        }
        assertTrue(fv.isAfter(iv), "issue %s: injected version %s must come before fix version %s".formatted(i, iv, fv));
        assertTrue(iv.order() < fv.order(), "%s,%s: %d should be smaller than %d".formatted(iv, fv, iv.order(), fv.order()));
        return (fv.order() - iv.order()) / d;
    }

    private void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}