package io.github.francescodonnini.proportion;

import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.Release;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

public class Incremental implements Proportion {
    private final List<Issue> issues;
    private final List<Release> releases;

    public Incremental(List<Issue> issues, List<Release> releases) {
        this.issues = issues;
        this.releases = releases;
    }

    @Override
    public List<Issue> makeLabels() {
        releases.sort((x, y) -> x.releaseDate().compareTo(y.releaseDate()));
        var unlabeled = getUnlabeledIssues();
        var labeled = getLabeledIssues();
        var result = new ArrayList<Issue>(labeled);
        for (var r : releases.subList(1, releases.size())) {
            var p = calculateProportion(labeled, r);
            if (p.isEmpty()) continue;
            var av = getRange(releases, r, p.getAsDouble());
            unlabeled.stream()
                    .filter(i -> i.fixVersion().equals(r))
                    .map(i -> i.withAffectedVersions(av))
                    .forEach(result::add);
        }
        return result;
    }

    private List<Release> getRange(List<Release> releases, Release current, double pIncrement) {
        var i = 0;
        for (var r : releases) {
            if (r.equals(current)) break;
            ++i;
        }
        var result = new ArrayList<Release>();
        var n = (int) pIncrement;
        while (n > 0) {
            result.add(releases.get(i - n));
            --n;
        }
        return result;
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

    private OptionalDouble calculateProportion(List<Issue> avPresent, Release r) {
        return avPresent.stream()
                .filter(i -> !i.fixVersion().isAfter(r))
                .mapToInt(i -> i.affectedVersions().size())
                .average();
    }
}