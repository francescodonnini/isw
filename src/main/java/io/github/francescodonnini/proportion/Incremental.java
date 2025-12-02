package io.github.francescodonnini.proportion;

import io.github.francescodonnini.data.ReleaseApi;
import io.github.francescodonnini.model.Issue;

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
             ProportionUtils.calculateP(labeled)
                    .ifPresent(p -> result.addAll(ProportionUtils.applyP(unlabeled, p, releases)));
        } else {
            for (var r : releases.subList(1, releases.size())) {
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