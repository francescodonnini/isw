package io.github.francescodonnini.proportion;

import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.Release;

import java.util.*;

public class Incremental implements Proportion {
    private final List<Issue> issues;
    private final List<Release> releases;
    private final int projectReleasesEnd;
    private final boolean complete;

    public Incremental(List<Issue> issues, List<Release> releases, int projectReleasesEnd, boolean complete) {
        this.issues = issues;
        this.releases = releases;
        this.projectReleasesEnd = projectReleasesEnd;
        this.complete = complete;
    }

    @Override
    public List<Issue> makeLabels(String projectName) {
        var labeled = ProportionUtils.getLabelled(issues);
        var unlabeled = ProportionUtils.getUnlabelled(issues);
        var result = new ArrayList<>(labeled);
        if (complete) {
             ProportionUtils.calculateP(labeled)
                    .ifPresent(p -> result.addAll(ProportionUtils.applyP(unlabeled, p, releases)));
        } else {
            for (var r : releases.subList(0, projectReleasesEnd)) {
                ProportionUtils.calculateP(labeled, i -> i.created().isBefore(r.releaseDate().atStartOfDay()))
                        .ifPresent(p -> {
                            var u = unlabeled.stream().filter(i -> i.fixVersion().equals(r)).toList();
                            result.addAll(ProportionUtils.applyP(u, p, releases));
                        });
            }
        }
        return result;
    }
}