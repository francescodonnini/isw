package io.github.francescodonnini.collectors;

import io.github.francescodonnini.model.JavaMethod;
import io.github.francescodonnini.model.Release;

import java.time.LocalDate;
import java.util.*;

public class DiffCollector {
    private final List<Release> releases;
    private final List<JavaMethod> methods;
    private final Map<String, List<JavaMethod>> history = new HashMap<>();
    private final boolean fromStart;

    public DiffCollector(List<Release> releases, List<JavaMethod> methods) {
        this(releases, methods, false);
    }

    /**
     * DiffCollector colleziona le metriche relative al cambiamento di un metodo nel tempo
     * @param releases lista delle release di un progetto
     * @param methods lista degli snapshot dei metodi in un certo istante nel tempo (revision)
     */
    public DiffCollector(List<Release> releases, List<JavaMethod> methods, boolean fromStart) {
        this.releases = releases;
        this.methods = methods;
        this.fromStart = fromStart;
    }

    /**
     * collect calcola le metriche relative al cambiamento di un metodo nel tempo
     * @return una lista di snapshot di un metodo in una certa release
     */
    public List<JavaMethod> collect() {
        createMapping();
        var result = new ArrayList<JavaMethod>();
        var start = LocalDate.MIN;
        for (var end : releases.stream().map(Release::releaseDate).toList()) {
            result.addAll(collect(start, end));
            if (!fromStart) {
                start = end;
            }
        }
        return result;
    }

    private void createMapping() {
        methods.stream()
                .sorted(Comparator.comparing(a -> a.getJavaClass().getTime()))
                .filter(m -> isBetween(m, LocalDate.MIN, releases.getLast().releaseDate()))
                .forEach(m -> history.computeIfAbsent(key(m), s -> new ArrayList<>()).add(m));
    }

    private boolean isBetween(JavaMethod m, LocalDate start, LocalDate end) {
        var date = m.getJavaClass().getTime().toLocalDate();
        return !date.isBefore(start) && !date.isAfter(end);
    }


    private String key(JavaMethod method) {
        return "%s#%s".formatted(method.getPath(), method.getSignature());
    }

    private List<JavaMethod> collect(LocalDate start, LocalDate end) {
        var result = new ArrayList<JavaMethod>();
        for (var e : history.entrySet()) {
            var revisions = e.getValue().stream()
                    .filter(m -> isBetween(m, start, end))
                    .toList();
            diff(revisions).ifPresent(result::add);
        }
        return result;
    }

    private Optional<JavaMethod> diff(List<JavaMethod> revisions) {
        if (revisions.isEmpty()) {
            return Optional.empty();
        }
        var last = revisions.getLast();
        revisions.forEach(m -> addToHistory(last, m));
        return Optional.ofNullable(last);
    }

    private void addToHistory(JavaMethod to, JavaMethod from) {
        from.getJavaClass().getAuthor().ifPresent(a -> to.getMetrics().addAuthor(a));
        to.getMetrics().addElseCount(from.getMetrics().getElseCount());
        to.getMetrics().addLoc(from.getMetrics().getLineOfCode());
        to.getMetrics().addStatementCount(from.getMetrics().getStatementsCount());
    }
}