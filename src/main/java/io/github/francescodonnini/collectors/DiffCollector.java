package io.github.francescodonnini.collectors;

import io.github.francescodonnini.model.JavaMethod;
import io.github.francescodonnini.model.Release;

import java.util.*;

public class DiffCollector {
    private final List<Release> releases;
    private final List<JavaMethod> methods;
    private final Map<String, List<JavaMethod>> history = new HashMap<>();

    public DiffCollector(List<Release> releases, List<JavaMethod> methods) {
        this.releases = releases;
        this.methods = methods;
    }

    public List<JavaMethod> collect() {
        var list = new ArrayList<JavaMethod>();
        for (var release : releases) {
            var target = methods.stream()
                    .filter(m -> !isAfter(m, release))
                    .toList();
            list.addAll(collect(target));
            methods.removeAll(target);
        }
        return list;
    }

    private boolean isAfter(JavaMethod m, Release r) {
        return m.getJavaClass().getTime().toLocalDate().isAfter(r.releaseDate());
    }

    private List<JavaMethod> collect(List<JavaMethod> methods) {
        if (methods.isEmpty()) {
            return List.of();
        }
        // mapping permette di tenere traccia delle revision di un metodo all'interno di una release.
        methods.forEach(m -> history.computeIfAbsent(key(m), _ -> new ArrayList<>()).add(m));
        var list = new ArrayList<JavaMethod>();
        for (var entry : history.entrySet()) {
            diff(entry.getValue()).ifPresent(list::add);
        }
        return list;
    }

    private String key(JavaMethod method) {
        return "%s%s".formatted(method.getPath(), method.getSignature());
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