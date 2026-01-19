package io.github.francescodonnini.collectors;

import io.github.francescodonnini.model.JavaMethod;
import io.github.francescodonnini.model.Release;

import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiffCollector {
    private final Logger logger = Logger.getLogger(DiffCollector.class.getName());
    private final List<Release> releases;
    private final List<JavaMethod> methods;
    private final Map<JavaMethodId, List<JavaMethod>> history = new HashMap<>();
    private final boolean fromStart;

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
        logger.log(Level.WARNING, "starting to diffing {0} methods", methods.size());
        createMapping();
        var result = new ArrayList<JavaMethod>();
        var start = LocalDate.MIN;
        var previousEnd = LocalDate.MIN;
        for (var release : releases) {
            var end = release.releaseDate();
            var methods = collect(start, end, previousEnd);
            logger.log(Level.INFO, "A total of {0} methods have been read in release {1}", new Object[] { methods.size(), release });
            result.addAll(methods);
            previousEnd = end;
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
                .forEach(m -> history.computeIfAbsent(JavaMethodId.of(m), s -> new ArrayList<>()).add(m));
    }

    private boolean isBetween(JavaMethod m, LocalDate start, LocalDate end) {
        var date = m.getJavaClass().getTime().toLocalDate();
        return date.isAfter(start) && !date.isAfter(end);
    }

    private List<JavaMethod> collect(LocalDate start, LocalDate end, LocalDate previousEnd) {
        var result = new ArrayList<JavaMethod>();
        for (var e : history.entrySet()) {
            var revisions = e.getValue().stream()
                    .filter(m -> isBetween(m, start, end))
                    .toList();
            diff(revisions, end, previousEnd).ifPresent(result::add);
        }
        return result;
    }

    private Optional<JavaMethod> diff(List<JavaMethod> revisions, LocalDate end, LocalDate previousEnd) {
        if (revisions.isEmpty()) {
            return Optional.empty();
        }
        var last = revisions.getLast();
        if (last == null || !isBetween(last, previousEnd, end)) {
            return Optional.empty();
        }
        revisions.forEach(m -> addToHistory(last, m));
        return Optional.of(last);
    }

    private void addToHistory(JavaMethod to, JavaMethod from) {
        from.getJavaClass().getAuthor().ifPresent(a -> to.getMetrics().addAuthor(a));
        to.getMetrics().updateMethodHistories();
        to.getMetrics().addElseCount(from.getMetrics().getElseCount());
        to.getMetrics().addLoc(from.getMetrics().getLineOfCode());
        to.getMetrics().addStatementCount(from.getMetrics().getStatementsCount());
    }
}