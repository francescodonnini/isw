package io.github.francescodonnini.proportion;

import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.Release;

import java.util.List;
import java.util.OptionalDouble;
import java.util.function.Predicate;

public class ProportionUtils {
    private ProportionUtils() {}

    public static List<Issue> getLabelled(List<Issue> issues) {
        return issues.stream()
                .filter(i -> !i.affectedVersions().isEmpty())
                .toList();
    }

    public static List<Issue> getUnlabelled(List<Issue> issues) {
        return issues.stream()
                .filter(i -> i.affectedVersions().isEmpty())
                .toList();
    }

    public static List<Issue> applyP(List<Issue> issues, double p, List<Release> releases) {
        return issues.stream()
                .map(issue -> ProportionUtils.applyP(issue, p, releases))
                .toList();
    }

    public static Issue applyP(Issue i, double p, List<Release> releases) {
        return i.withAffectedVersions(releases.subList(Math.max(0, injectedVersion(i, p)), i.fixVersion().order()));
    }

    private static int injectedVersion(Issue i, double p) {
        var d = i.fixVersion().order() - i.openingVersion().order();
        if (d == 0) {
            d = 1;
        }
        return (int)(i.fixVersion().order() - d * p);
    }

    public static OptionalDouble calculateP(List<Issue> issues) {
        return calculateP(issues, unused -> true);
    }

    public static OptionalDouble calculateP(List<Issue> issues, Predicate<Issue> filter) {
        return issues.stream()
                .filter(filter)
                .mapToDouble(ProportionUtils::calculateP)
                .average();
    }

    public static double calculateP(Issue i) {
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

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}