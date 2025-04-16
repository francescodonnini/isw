package io.github.francescodonnini.data;

import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.JavaMethod;
import io.github.francescodonnini.model.Release;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Labeller {
    private final Logger logger = Logger.getLogger(Labeller.class.getName());
    private final List<JavaMethod> methods;
    private final List<Issue> issues;
    private Release end;
    private Release start;
    private String path;

    public Labeller(List<JavaMethod> methods, List<Issue> issues) {
        this.methods = methods;
        this.issues = issues;
    }

    public void setEnd(Release endInclusive) {
        end = endInclusive;
    }

    public void setStart(Release start) {
        this.start = start;
    }

    public void setRepository(String path) {
        this.path = path;
    }

    public List<JavaMethod> fill() {
        var list = methods.stream()
                .filter(e -> !e.getRelease().isAfter(end))
                .toList();
        try {
            return fillBugginess(list);
        } catch (IOException e) {
            logger.log(Level.INFO, e.getMessage());
            return List.of();
        }
    }

    private List<JavaMethod> fillBugginess(List<JavaMethod> entries) throws IOException {
        var repository = new FileRepositoryBuilder()
                .setGitDir(new File(path))
                .build();
        // Lista di issues afferenti alle release nell'intervallo [start, end]. Se start non viene specificato si
        // parte dalla prima release, se non viene specificato end si considera come ultima quella finale. Almeno
        // una delle due release deve essere specificata.
        var list = selectIssues();
        // mapping divide le entry per identificativo di release per facilitare il labelling.
        var mapping = new HashMap<String, List<JavaMethod>>();
        entries.forEach(e -> mapping.computeIfAbsent(e.getRelease().id(), k -> new ArrayList<>()).add(e));
        // lista di entry che devono essere etichettate come buggy.
        var buggyClasses = new ArrayList<JavaMethod>();
        for (var issue : list) {
            for (var commit : issue.commits()) {
                // classes sono le entry potenzialmente interessate dal ticket
                var classes = new ArrayList<JavaMethod>();
                issue.affectedVersions().forEach(v -> {
                    if (mapping.containsKey(v.id())) {
                        classes.addAll(mapping.get(v.id()));
                    }
                });
                parseDiffs(classes, buggyClasses, repository, commit);
            }
        }
        // Si scorrono tutte le entry da entries e si aggiornano quelle che si è scoperto essere buggy
        var all = new ArrayList<JavaMethod>();
        for (var e : entries) {
            var isBuggy = buggyClasses.stream()
                    .anyMatch(c -> c.getPath().equals(e.getPath()) && e.getRelease().id() == c.getRelease().id());
            e.setBuggy(isBuggy);
            all.add(e);
        }
        return all;
    }

    private void parseDiffs(List<JavaMethod> classes, List<JavaMethod> buggyClasses, Repository repository, RevCommit commit) throws IOException {
        var df = new DiffFormatter(DisabledOutputStream.INSTANCE);
        df.setRepository(repository);
        df.setDiffComparator(RawTextComparator.DEFAULT);
        df.setDetectRenames(true);
        var diffs = df.scan(commit.getParent(0).getId(), commit.getTree());
        for (var diff : diffs) {
            // Percorso del file modificato da un commit afferente a issue.
            var file = diff.getNewPath();
            if (file.endsWith(".java")) {
                var targets = classes.stream()
                        .filter(c -> file.contains(c.getPath().toString()))
                        .peek(e -> e.setBuggy(true))
                        .toList();
                buggyClasses.addAll(targets);
            }
        }
    }

    // Seleziona un sottoinsieme di issues in funzione dell'intervallo delle release selezionato.
    private List<Issue> selectIssues() {
        if (start == null && end == null) {
            throw new IllegalArgumentException("selezionare almeno un parametro tra start e end.");
        } else if (start == null) {
            return getIssuesBeforeRelease(end);
        } else if (end == null) {
            return getIssuesAfterRelease(start);
        } else {
            return getIssuesBetween(start, end);
        }
    }

    private Predicate<Issue> after(Release start) {
        return issue -> !issue.created().toLocalDate().isAfter(start.releaseDate());
    }

    private Predicate<Issue> before(Release end) {
        return issue -> !issue.created().toLocalDate().isAfter(end.releaseDate());
    }

    private List<Issue> getIssuesAfterRelease(Release start) {
        return issues.stream()
                .filter(i -> after(start).test(i))
                .toList();
    }

    private List<Issue> getIssuesBeforeRelease(Release endInclusive) {
        return issues.stream()
                .filter(i -> before(endInclusive).test(i))
                .toList();
    }

    private List<Issue> getIssuesBetween(Release start, Release endInclusive) {
        return issues.stream()
                .filter(i -> after(start).test(i))
                .filter(i -> before(endInclusive).test(i))
                .toList();

    }
}
