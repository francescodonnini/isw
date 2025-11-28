package io.github.francescodonnini.proportion;

import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.Release;

import java.util.ArrayList;
import java.util.List;

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
        var result = new ArrayList<Issue>();
        for (var issue : issues) {
            var fixVersion = issue.fixVersion();
            result.add(issue.withAffectedVersions(releases.subList(injectedVersion(issue, p), fixVersion.order())));
        }
        return result;
    }

    private static int injectedVersion(Issue i, double p) {
        return (int)(i.fixVersion().order() - (i.fixVersion().order() - i.openingVersion().order()) * p);
    }
}