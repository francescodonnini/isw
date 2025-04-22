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
        var last = methods.getLast();
        var prev = 0;
        for (var curr = 1; curr < methods.size(); curr++) {
            var prevMethod = methods.get(prev);
            var currMethod = methods.get(curr);
            diff(last, prevMethod, currMethod);
            getAuthor(currMethod).ifPresent(a -> prevMethod.getMetrics().addAuthor(a));
            prev = curr;
        }
        getAuthor(last).ifPresent(a -> last.getMetrics().addAuthor(a));
        return Optional.ofNullable(last);
    }

    private Optional<String> getAuthor(JavaMethod m) {
        return m.getJavaClass().getAuthor();
    }

    private void diff(JavaMethod m, JavaMethod prev, JavaMethod curr) {
        diffLoc(m, prev, curr);
        diffElseCount(m, prev, curr);
    }

    private void diffLoc(JavaMethod m, JavaMethod prev, JavaMethod curr) {
        var prevLoc = prev.getLineRange().length();
        var currLoc = curr.getLineRange().length();
        if (prevLoc < currLoc) {
            var locAdded = (int) (currLoc - prevLoc);
            m.getMetrics().addLocAdded(locAdded);
            m.getMetrics().addLocTouched(locAdded);
        } else if (prevLoc > currLoc) {
            var locDeleted = (int) (prevLoc - currLoc);
            m.getMetrics().addLocDeleted(locDeleted);
            m.getMetrics().addLocTouched(locDeleted);
        }
    }

    private void diffElseCount(JavaMethod m, JavaMethod prev, JavaMethod curr) {
        var prevElse = prev.getMetrics().getElseCount();
        var currElse = curr.getMetrics().getElseCount();
        if (prevElse < currElse) {
            var elseAdded = (int) (currElse - prevElse);
            m.getMetrics().addElseAdded(elseAdded);
        } else if (prevElse > currElse) {
            var elseDeleted = (int) (prevElse - currElse);
            m.getMetrics().addElseDeleted(elseDeleted);
        }
    }
}
