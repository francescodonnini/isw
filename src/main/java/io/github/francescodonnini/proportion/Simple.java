package io.github.francescodonnini.proportion;

import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.Release;

import java.util.List;

public class Simple implements Proportion {
    private final List<Issue> issues;
    private final List<Release> releases;

    public Simple(List<Issue> issues, List<Release> releases) {
        this.issues = issues;
        this.releases = releases;
    }

    @Override
    public List<Issue> makeLabels(String projectName) {
        return ProportionUtils.getUnlabelled(issues).stream()
                .map(i -> i.withAffectedVersions(releases.subList(i.openingVersion().order(), i.fixVersion().order())))
                .toList();
    }
}
