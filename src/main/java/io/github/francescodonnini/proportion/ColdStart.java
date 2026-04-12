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
    private final List<Release> releases;
    private final boolean complete;

    public ColdStart(IssueApi issueApi, List<Issue> issues, List<Release> releases, boolean complete) {
        this.issueApi = issueApi;
        this.issues = issues;
        this.releases = releases;
        this.complete = complete;
    }

    @Override
    public List<Issue> makeLabels(String projectName) {
        if (complete) {
            return makeLabelsComplete(projectName);
        }
        return makeLabelsRealistic(projectName);
    }

    private List<Issue> makeLabelsComplete(String projectName) {
        var pAvg = new ArrayList<Double>();
        for (var project : ApacheProjects.getProjects(projectName)) {
            issueApi.getIssues(ApacheProjects.jiraKey(project)).stream()
                    .filter(i -> !i.affectedVersions().isEmpty())
                    .mapToInt(i -> i.affectedVersions().size())
                    .average()
                    .ifPresent(pAvg::add);
        }
        var m = median(pAvg);
        return m.map(p ->
                        ProportionUtils.applyP(
                            ProportionUtils.getUnlabelled(issues),
                            p,
                            releases))
                .orElseGet(List::of);
    }

    private List<Issue> makeLabelsRealistic(String projectName) {
        var pIssues = new HashMap<String, List<Issue>>();
        for (var project : ApacheProjects.PROJECTS) {
            var issueList = issueApi.getIssues(ApacheProjects.jiraKey(project)).stream()
                    .filter(issue -> !issue.affectedVersions().isEmpty())
                    .toList();
            if (!issueList.isEmpty()) {
                pIssues.put(project, issueList);
            }
        }
        var labeled = ProportionUtils.getLabelled(issues);
        var unlabeled = ProportionUtils.getUnlabelled(issues);
        var result = new ArrayList<>(labeled);
        for (var k = 1; k < releases.size(); k++) {
            final var curr = releases.get(k);
            final var prev = releases.get(k - 1);
            var pAvg = new ArrayList<Double>();
            for (var project : ApacheProjects.getProjects(projectName)) {
                ProportionUtils
                        .calculateP(pIssues.getOrDefault(project, List.of()), i -> !i.created().isAfter(curr.releaseDate().atStartOfDay()))
                        .ifPresent(pAvg::add);
            }
            var toLabel = unlabeled.stream()
                    .filter(i -> isBetween(i, prev, curr))
                    .toList();
            median(pAvg).ifPresent(p -> {
                logger.log(Level.INFO, "Release %d: P=%f".formatted(curr.order(), p));
                result.addAll(ProportionUtils.applyP(toLabel, p, releases));
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
}
