package io.github.francescodonnini.proportion;

import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.Release;

import java.util.*;
import java.util.logging.Logger;

public class Incremental implements Proportion {
    private static final Logger logger = Logger.getLogger(Incremental.class.getName());
    private final boolean complete;
    private final List<Issue> issues;
    private final List<Release> releases;

    public Incremental(List<Issue> issues, List<Release> releases, boolean complete) {
        this.complete = complete;
        this.issues = issues;
        this.releases = releases;
        releases.sort(Comparator.comparing(Release::releaseDate));
    }

    @Override
    public List<Issue> makeLabels() {
        var unlabeled = getUnlabeledIssues();
        var labeled = getLabeledIssues();
        var result = new ArrayList<>(labeled);
        if (complete) {
            var p = calculateProportion(labeled, releases.getLast());
            if (p.isEmpty()) {
                logger.warning("cannot calculate proportion");
            } else {
                unlabeled.stream()
                        .map(i -> i.withAffectedVersions(getRange(i, p.getAsDouble())))
                        .forEach(result::add);
            }
        } else {
            for (var r : releases.subList(1, releases.size())) {
                var p = calculateProportion(labeled, r);
                if (p.isEmpty()) {
                    logger.warning("cannot calculate proportion for " + r.name());
                    continue;
                }
                unlabeled.stream()
                        .filter(i -> i.fixVersion().equals(r))
                        .map(i -> i.withAffectedVersions(getRange(i, p.getAsDouble())))
                        .forEach(result::add);
            }
        }

        return result;
    }

    private List<Release> getRange(Issue issue, double p) {
        var fixVersion = issue.fixVersion().order();
        var openingVersion = issue.openingVersion().order();
        var injectedVersion = (int) Math.ceil(fixVersion - (fixVersion - openingVersion) * p);
        return releases.subList(injectedVersion, fixVersion);
    }

    private List<Issue> getLabeledIssues() {
        return issues.stream()
                .filter(i -> !i.affectedVersions().isEmpty())
                .toList();
    }

    private List<Issue> getUnlabeledIssues() {
        return issues.stream()
                .filter(i -> i.affectedVersions().isEmpty())
                .toList();
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