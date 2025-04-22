package io.github.francescodonnini.collectors;

import io.github.francescodonnini.model.JavaMethod;
import io.github.francescodonnini.model.Release;

import java.time.LocalDate;
import java.util.*;

public class DiffCollector {
    private final List<Release> releases;
    private final List<JavaMethod> methods;

    public DiffCollector(List<Release> releases, List<JavaMethod> methods) {
        this.releases = releases;
        this.methods = methods;
    }

    public List<JavaMethod> collect() {
        var list = new ArrayList<JavaMethod>();
        var start = LocalDate.MIN;
        for (var release : releases) {
            var end = release.releaseDate();
            var s = start;
            var target = collect(methods.stream()
                    .filter(m -> isBetween(m, s, end))
                    .toList());
            list.addAll(collect(target));
            start = end;
        }
        return list;
    }

    private List<JavaMethod> collect(List<JavaMethod> methods) {
        if (methods.isEmpty()) {
            return List.of();
        }
        // mapping permette di tenere traccia
        var mapping = new HashMap<String, List<JavaMethod>>();
        methods.forEach(m -> mapping.computeIfAbsent(key(m), _ -> new ArrayList<>()).add(m));
        var list = new ArrayList<JavaMethod>();
        for (var entry : mapping.entrySet()) {
            diff(entry.getValue()).ifPresent(list::add);
        }
        return list;
    }

    private String key(JavaMethod method) {
        return "%s%s".formatted(method.getPath(), method.getSignature());
    }

    private boolean isBetween(JavaMethod method, LocalDate start, LocalDate end) {
        var date = method.getJavaClass().getTime().toLocalDate();
        return !date.isBefore(start) && !date.isAfter(end);
    }

    private Optional<JavaMethod> diff(List<JavaMethod> methods) {
        if (methods.isEmpty()) {
            return Optional.empty();
        }
        var last = methods.getLast();
        methods.forEach(m -> addToHistory(last, m));
        return Optional.ofNullable(last);
    }

    private void addToHistory(JavaMethod to, JavaMethod from) {
        from.getJavaClass().getAuthor().ifPresent(a -> to.getMetrics().addAuthor(a));
        to.getMetrics().addElseCount(from.getMetrics().getElseCount());
        to.getMetrics().addLoc(from.getMetrics().getLineOfCode());
        to.getMetrics().addStatementCount(from.getMetrics().getStatementsCount());
    }
}