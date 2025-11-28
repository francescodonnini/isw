package io.github.francescodonnini.proportion;

import io.github.francescodonnini.data.IssueApi;
import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.Release;
import io.github.francescodonnini.utils.ApacheProjects;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ColdStart implements Proportion {
    private final Logger logger = Logger.getLogger(ColdStart.class.getName());
    private final IssueApi issueApi;
    private final List<Issue> issues;
    private final List<Release> projectReleases;

    public ColdStart(IssueApi issueApi, List<Issue> issues, List<Release> projectReleases) {
        this.issueApi = issueApi;
        this.issues = issues;
        this.projectReleases = projectReleases;
    }

    @Override
    public List<Issue> makeLabels(String projectName) {
        var pIssues = new HashMap<String, List<Issue>>();
        for (var project : ApacheProjects.PROJECTS) {
            pIssues.put(project, issueApi.getIssues(ApacheProjects.jiraKey(project)).stream()
                    .filter(i -> !i.affectedVersions().isEmpty())
                    .toList());
        }
        var labeled = ProportionUtils.getLabelled(issues);
        var unlabeled = ProportionUtils.getUnlabelled(labeled);
        var result = new ArrayList<>(labeled);
        var pAvg = new ArrayList<Double>();
        for (var k = 1; k < projectReleases.size(); k++) {
            var curr = projectReleases.get(k);
            var prev = projectReleases.get(k - 1);
            var toLabel = unlabeled.stream()
                            .filter(i -> !i.created().isAfter(curr.releaseDate().atStartOfDay()))
                            .toList();
            for (var project : ApacheProjects.PROJECTS) {
                calculateP(pIssues.getOrDefault(project, List.of()).stream()
                        .filter(i -> isBetween(i, prev, curr))
                        .toList())
                        .ifPresent(pAvg::add);
            }
            median(pAvg).ifPresent(p -> {
                logger.log(Level.INFO, "Median P = {0}", p);
                result.addAll(ProportionUtils.applyP(toLabel, p, projectReleases));
            });
            pAvg.clear();
        }
        return result;
    }

    private boolean isBetween(Issue issue, Release startExclusive, Release endInclusive) {
        return issue.created().isAfter(startExclusive.releaseDate().atStartOfDay())
                && !issue.created().isAfter(endInclusive.releaseDate().atStartOfDay());
    }

    private Optional<Double> median(List<Double> v) {
        v.sort(Comparator.naturalOrder());
        var m = v.size() % 2 == 0 ? v.size() / 2 : v.size() / 2 + 1;
        if (m < v.size()) {
            return Optional.of(v.get(m));
        }
        return Optional.empty();
    }

    private OptionalDouble calculateP(List<Issue> issues) {
        return issues.stream()
                .map(this::calculateP)
                .filter(Optional::isPresent)
                .mapToDouble(Optional::get)
                .average();
    }

    private Optional<Double> calculateP(Issue issue) {
        var fv = (double)issue.fixVersion().order();
        var o = issue.injectedVersion();
        if (o.isEmpty()) {
            return Optional.empty();
        }
        var iv = o.get().order();
        var ov = issue.openingVersion().order();
        if (fv == ov) {
            return Optional.empty();
        }
        return Optional.of((fv - iv)/(fv - ov));
    }
}
