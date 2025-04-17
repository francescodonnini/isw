package io.github.francescodonnini.collectors;

import io.github.francescodonnini.model.JavaMethod;
import io.github.francescodonnini.model.Release;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

public class VcsCollector {
    private final Logger logger = Logger.getLogger(VcsCollector.class.getName());
    private final Git git;
    private final List<Release> releases;
    private final HashSet<String> changeSet = new HashSet<>();

    public VcsCollector(List<Release> releases, Path repositoryPath) throws IOException {
        this.releases = new ArrayList<>(releases);
        this.git = createGit(repositoryPath);
    }

    private Git createGit(Path path) throws IOException {
        var repository = new FileRepositoryBuilder()
                .setGitDir(path.toFile())
                .build();
        return new Git(repository);
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
    public void calculate(List<JavaMethod> methods) {
        // mapping contiene tutte le entries divise per numero di release.
        var mapping = new HashMap<String, List<JavaMethod>>();
        methods.forEach(e -> mapping.computeIfAbsent(e.getRelease().id(), _ -> new ArrayList<>()).add(e));
        try {
            // Si prende la lista di tutti i commit e si ordinano per data di pubblicazione.
            var commits = StreamSupport.stream(git.log().call().spliterator(), false)
                    .sorted(Comparator.comparing(this::getCommitTime))
                    .filter(c -> !afterRelease(releases.getLast(), c))
                    .toList();
            for (var commit : commits) {
                // Si prende la release a cui quel commit appartiene.
                // Se non esiste una release disponibile si scarta il commit.
                var o = getReleaseByCommit(commit);
                if (o.isPresent()) {
                    var release = o.get();
                    // Si prende la lista delle entries afferenti a release.
                    // Queste sono le entries che possono essere influenzate dal commit che si sta analizzando.
                    // Se non ci sono entries afferenti a release allora si scarta il commit.
                    var susceptibles = mapping.get(release.id());
                    updateEntries(methods, susceptibles, commit);
                }
            }
            calculateAverages(mapping);
        } catch (GitAPIException | IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private boolean afterRelease(Release release, RevCommit commit) {
        return getCommitTime(commit).isAfter(release.releaseDate().atStartOfDay());
    }

    private void updateEntries(List<JavaMethod> entries, List<JavaMethod> susceptibles, RevCommit commit) throws IOException {
        if (susceptibles != null && !susceptibles.isEmpty()) {
            // Un commit è una lista di modifiche fatte a uno o più file, è necessario iterare
            // tra le modifiche (diff) per raccogliere le metriche.
            var optionalParent = getParent(commit);
            if (optionalParent.isPresent()) {
                var parent = optionalParent.get();
                parseDiffs(entries, susceptibles, parent, commit);
            }
        }
    }

    private void parseDiffs(List<JavaMethod> entries, List<JavaMethod> susceptibles, RevCommit parent, RevCommit commit) throws IOException {
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
                var oldPath = diff.getOldPath();
                if (!oldPath.equals("/dev/null") && !oldPath.equals(path)) {
                    renameAllEntries(entries, Path.of(oldPath), Path.of(path));
                }
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

    // cambia il path di tutte le entry da oldPath a newPath
    private void renameAllEntries(List<JavaMethod> methods, Path oldPath, Path newPath) {
        for (var m : methods) {
            if (m.getPath().equals(oldPath)) {
                m.setPath(newPath);
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

    private Optional<Release> getReleaseByCommit(RevCommit commit) {
        return getReleaseByDate(getCommitTime(commit));
    }

    private Optional<Release> getReleaseByDate(LocalDateTime commitTime) {
        for (Release r : releases) {
            if (r.releaseDate().isAfter(commitTime.toLocalDate())) {
                return Optional.of(r);
            }
        }
        return Optional.empty();
    }

    private LocalDateTime getCommitTime(RevCommit commit) {
        return commit.getAuthorIdent().getWhenAsInstant().atZone(commit.getAuthorIdent().getZoneId()).toLocalDateTime();
    }
}