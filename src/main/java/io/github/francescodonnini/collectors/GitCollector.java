package io.github.francescodonnini.collectors;

import io.github.francescodonnini.history.FileHistory;
import io.github.francescodonnini.model.JavaMethod;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class GitCollector {
    private final Logger logger = Logger.getLogger(GitCollector.class.getName());
    private final Git git;
    private final HashSet<String> changeSet = new HashSet<>();

    public GitCollector(Git git) {
        this.git = git;
    }

    /*
     * (*): è possibile misurare la metrica ispezionando linearmente i commit.
     * Data una entry afferente alla release i, calcola le seguenti metriche:
     * - [] LOC Touched: somma delle linee aggiunte/eliminate.
     * - [] Churn: differenza delle linee aggiunte/eliminate
     * - [] NR: numero di commit che modificano la classe.
     * - [] Number of Authors: numero di sviluppatori che hanno contribuito alla classe.
     * - [] LOC Added: somma delle line aggiunte.
     * - [] Avg LOC Added: numero medio di righe aggiunte per commit.
     * - [] Change Set Size: numero di file committati insieme.
     * - [] Max Change Set Size: Numero massimo di file committati insieme.
     * - [] Average Change Set: numero medio di file committati insieme.
     * - [] Age: età della release.
     * - [] Weighted Age: età della release pesate per il numero di linee toccate.
     */
    public void calculate(List<JavaMethod> methods, List<FileHistory> history) {
        // mapping contiene tutte le entries divise per numero di release.
        var mapping = history.stream()
                .map(h -> Map.entry(h.getCommitId(), h))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        try {
            // Si prende la lista di tutti i commit e si ordinano per data di pubblicazione.
            var commits = StreamSupport.stream(git.log().call().spliterator(), false)
                    .sorted(Comparator.comparing(this::getCommitTime))
                    .filter(c -> mapping.containsKey(c.getName()))
                    .toList();
            for (var commit : commits) {
                if (mapping.containsKey(commit.getName())) {
                    var h = mapping.get(commit.getName());
                    updateEntries(methods, commit, h);
                }
            }
        } catch (GitAPIException | IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void updateEntries(List<JavaMethod> susceptibles, RevCommit commit, FileHistory h) throws IOException {
        if (susceptibles != null && !susceptibles.isEmpty()) {
            // Un commit è una lista di modifiche fatte a uno o più file, è necessario iterare
            // tra le modifiche (diff) per raccogliere le metriche.
            var optionalParent = getParent(commit);
            if (optionalParent.isPresent()) {
                var parent = optionalParent.get();
                parseDiffs(susceptibles, parent, commit, h);
            }
        }
    }

    private void parseDiffs(List<JavaMethod> susceptibles, RevCommit parent, RevCommit commit, FileHistory h) throws IOException {
        var df = new DiffFormatter(DisabledOutputStream.INSTANCE);
        df.setRepository(git.getRepository());
        df.setDetectRenames(true);
        var diffs = df.scan(parent.getTree(), commit.getTree());
        for (var diff : diffs) {
            var path = diff.getNewPath();
            // Se il percorso del file modificato non è un file .java allora non è necessario analizzare
            // la modifica.
            if (path.endsWith(".java")) {
                changeSet.add(path);
                // I file potrebbero essere stati rinominati ed è necessario quindi aggiornare tutte le entry che hanno
                // il vecchio percorso. Bisogna controllare se il vecchio path è diverso da /dev/null dato che in tal caso non è necessario fare alcune
                // operazione perché significa che il file non esisteva prima di quel commit.
                var optionalEntry = susceptibles.stream().filter(it -> it.getPath().endsWith(path)).findFirst();
                // Se non esiste alcuna entry con quel percorso allora non è necessario analizzare la modifica.
                if (optionalEntry.isEmpty()) {
                    continue;
                }
                var entry = optionalEntry.get();
                // A questo punto si può analizzare come il commit in questione ha modificato la entry.
                // Un commit contiene le seguenti informazioni utili:
                // - autore (email).
                // - linee di codice aggiunte/eliminate.
                updateEntry(df, diff, commit, entry);
            }
        }
    }

    private void updateEntry(DiffFormatter df, DiffEntry diff, RevCommit commit, JavaMethod m) throws IOException {
        var del = 0;
        var add = 0;
        for (var edit : df.toFileHeader(diff).toEditList()) {
            edit.extendA();
            edit.extendB();
            del += edit.getEndA() - edit.getBeginA();
            add += edit.getEndB() - edit.getBeginB();
        }
        changeSet.clear();
    }



    private Optional<RevCommit> getParent(RevCommit commit) {
        try {
            return Optional.ofNullable(commit.getParent(0));
        } catch (IndexOutOfBoundsException e) {
            logger.log(Level.INFO, "Commit %s has no parent".formatted(commit));
            return Optional.empty();
        }
    }


    /*
     * calcola le metriche:
     * - [x] Average Change Set Size
     * - [x] Max Change Set Size
     */
    private void calculateAverages(Map<String, List<JavaMethod>> mapping) {
        var averages = new HashMap<String, Integer>();
        var maximums = new HashMap<String, Integer>();
        var numOfSamples = new HashMap<String, Integer>();
        var keys = mapping.keySet().stream().sorted(String::compareTo).toList();
        for (var r : keys) {
            var methods = mapping.get(r);
            for (var m : methods) {
                var path = m.getPath().toString();
                var count = numOfSamples.getOrDefault(path, 0) + 1;
                var prevAvg = averages.getOrDefault(path, 0);
                var changeSetSize = m.getMetrics().getChangeSetSize();
                var avg = (prevAvg + changeSetSize) / count;
                var prevMax = maximums.getOrDefault(path, 0);
                var max = changeSetSize > prevMax ? changeSetSize : prevMax;
                m.getMetrics().setAvgChangeSetSize(avg);
                m.getMetrics().setMaxChangeSetSize(max);
                averages.put(path, avg);
                maximums.put(path, max);
                numOfSamples.put(path, count);
            }
        }
    }

    private Optional<String> getAuthor(RevCommit commit) {
        var author = commit.getAuthorIdent().getEmailAddress();
        if (author == null || author.isEmpty() || author.equalsIgnoreCase("unknown@apache.org")) {
            return Optional.empty();
        }
        return Optional.of(author);
    }

    private LocalDateTime getCommitTime(RevCommit commit) {
        return commit.getAuthorIdent().getWhenAsInstant().atZone(commit.getAuthorIdent().getZoneId()).toLocalDateTime();
    }
}