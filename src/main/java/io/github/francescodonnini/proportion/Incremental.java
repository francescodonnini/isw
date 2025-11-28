package io.github.francescodonnini.proportion;

import io.github.francescodonnini.data.IssueApi;
import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.Release;

import java.util.*;
import java.util.logging.Logger;

public class Incremental implements Proportion {
    private static final Logger logger = Logger.getLogger(Incremental.class.getName());
    private final IssueApi issueApi;
    private final List<Release> projectReleases;
    private final boolean complete;

    public Incremental(IssueApi issueApi, List<Release> projectReleases, boolean complete) {
        this.issueApi = issueApi;
        this.projectReleases = projectReleases;
        this.complete = complete;
    }

    @Override
    public List<Issue> makeLabels(String projectName) {
        var issues = issueApi.getIssues(projectName);
        var labeled = ProportionUtils.getLabelled(issues);
        var unlabeled = ProportionUtils.getUnlabelled(issues);
        var result = new ArrayList<Issue>();
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
        throwIfFalse(fv.isAfter(ov), "issue %s: fix version %s is before opening version %s".formatted(i, fv, ov));
        throwIfFalse(fv.order() > ov.order(), "%s < %s".formatted(fv, ov));
        var d = (double) (fv.order() - ov.order());
        if (d == 0) {
            d = 1;
        }
        throwIfFalse(iv.isBefore(fv), "issue %s: injected version %s is after fix version %s".formatted(i, iv, fv));
        throwIfFalse(iv.order() < fv.order(), "%s > %s".formatted(iv, fv));
        return (fv.order() - iv.order()) / d;
    }

    private void throwIfFalse(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}