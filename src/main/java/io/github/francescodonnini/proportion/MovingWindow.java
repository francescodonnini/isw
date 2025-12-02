package io.github.francescodonnini.proportion;

import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.Release;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

public class MovingWindow implements Proportion {
    private final List<Issue> issues;
    private final List<Release> releases;
    private final double windowP;

    public MovingWindow(List<Issue> issues, List<Release> releases, double windowP) {
        this.issues = issues;
        this.releases = releases;
        this.windowP = windowP;
    }

    @Override
    public List<Issue> makeLabels(String projectName) {
        var labeled = ProportionUtils.getLabelled(issues).stream()
                .sorted(Comparator.comparing(Issue::created))
                .toList();
        var result = new ArrayList<>(labeled);
        for (var i : ProportionUtils.getUnlabelled(issues)) {
            var queue = labeled.stream()
                    .filter(j -> !j.created().isAfter(i.created()))
                    .collect(Collectors.toCollection(() -> new PriorityQueue<>(Comparator.comparing(Issue::created).reversed())));
            var windowSize = (int)(queue.size() * windowP);
            var window = new ArrayList<Issue>();
            while (windowSize-- > 0 && !queue.isEmpty()) {
                window.add(queue.poll());
            }
            ProportionUtils.calculateP(window)
                    .ifPresent(p -> result.add(ProportionUtils.applyP(i, p, releases)));
        }
        return result;
    }
}
