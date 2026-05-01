package io.github.francescodonnini.collectors;

import io.github.francescodonnini.model.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DiffCollector {
    private final Logger logger = Logger.getLogger(DiffCollector.class.getName());
    private final List<Release> releases;
    private final Map<ClassID, List<RevisionJavaClass>> history;
    private final List<RevisionJavaClass> classes = new ArrayList<>();
    private final boolean fromStart;

    public DiffCollector(
            List<Release> releases,
            List<RevisionJavaClass> classes,
            boolean fromStart) {
        if (releases.isEmpty()) {
            throw new IllegalArgumentException("release list is empty");
        }
        if (classes.isEmpty()) {
            throw new IllegalArgumentException("method list is empty");
        }
        this.fromStart = fromStart;
        this.releases = releases.stream()
                .sorted(Comparator.comparing(Release::releaseDate))
                .toList();
        this.history = new HashMap<>();
        var maxDate = releases.getLast().releaseDate();
        classes.stream()
                .filter(c -> isBetween(c, LocalDate.MIN, maxDate))
                .sorted(Comparator.comparing(RevisionJavaClass::getTime))
                .forEach(c -> {
                    history.computeIfAbsent(ClassID.of(c), s -> new ArrayList<>()).add(c);
                    this.classes.add(c);
                });
    }

    /**
     * collect() calcola le metriche relative al cambiamento di un metodo nel tempo
     * @return una lista di snapshot di un metodo in una certa release
     */
    public List<ReleaseJavaClass> collect() {
        logger.log(Level.WARNING, "start to diff {0} methods", classes.size());
        var result = new ArrayList<ReleaseJavaClass>();
        var start = LocalDate.MIN;
        var previousEnd = LocalDate.MIN;
        var progress = 0;
        var order = 0;
        for (var release : releases) {
            var end = release.releaseDate();
            var classList = collect(start, end, previousEnd, order);
            calculateFanIn(classList);
            result.addAll(classList);
            previousEnd = end;
            if (!fromStart) {
                start = end;
            }
            ++order;
            ++progress;
            logger.log(Level.INFO, "extracted {0} classes from {1}", new Object[]{classList.size(), release.id()});
            logger.log(Level.INFO, "{0}/{1} ({2}%)", new Object[]{progress, releases.size(), ((double)progress / releases.size() * 100)});
        }
        return result;
    }

    private void calculateFanIn(List<ReleaseJavaClass> classList) {
        for (var cls : classList) {
            cls.getComplexityMetrics().setFanIn(0);
        }

        for (var outboundCls : classList) {
            calculateFanIn(outboundCls, classList);
        }
    }

    private void calculateFanIn(ReleaseJavaClass outboundCls, List<ReleaseJavaClass> classList) {
        var outboundDeps = outboundCls.getComplexityMetrics().getDependencies();
        if (outboundDeps == null || outboundDeps.isEmpty()) {
            return;
        }
        for (var targetCls : classList) {
            if (outboundCls != targetCls) {
                if (outboundDeps.contains(targetCls.getName())) {
                    targetCls.getComplexityMetrics().incFanIn();
                }
            }
        }
    }

    private boolean isBetween(RevisionJavaClass cls, LocalDate start, LocalDate end) {
        var date = cls.getTime().toLocalDate();
        return date.isAfter(start) && !date.isAfter(end);
    }

    private List<ReleaseJavaClass> collect(
            LocalDate start,
            LocalDate end,
            LocalDate previousEnd,
            int order) {
        var result = new ArrayList<ReleaseJavaClass>();
        for (var e : history.entrySet()) {
            var revisions = e.getValue().stream()
                    .filter(m -> isBetween(m, start, end))
                    .collect(Collectors.toCollection(ArrayList::new));
            diff(revisions, end, previousEnd, order)
                    .ifPresent(result::add);
        }
        return result;
    }

    private Optional<ReleaseJavaClass> diff(
            List<RevisionJavaClass> revisions,
            LocalDate end,
            LocalDate previousEnd,
            int order) {
        if (revisions.isEmpty()) {
            return Optional.empty();
        }
        var last = revisions.getLast();
        if (!isBetween(last, previousEnd, end)) {
            return Optional.empty();
        }
        var smellCount = 0;
        var smellRevision = getPrevious(last, previousEnd);
        if (smellRevision.isPresent()) {
            var revision = smellRevision.get();
            smellCount = revision.getMetrics().getSmellCount();
        }
        var processMetrics = new ProcessClassMetrics();
        var locTouched = new IntAccumulator();
        var changeSet = new IntAccumulator();
        var changeTime = new TimeAccumulator();
        var entropy = new DoubleAccumulator();
        var authors = new HashSet<String>();
        var commits = new HashSet<String>();
        for (var revision : revisions) {
            locTouched.add(revision.getMetrics().getLoc());
            changeSet.add(revision.getMetrics().getChangeSetSize());
            changeTime.add(revision.getTime());
            entropy.add(revision.getCommitEntropy());
            revision.getAuthor().ifPresent(authors::add);
            commits.add(revision.getCommit());
        }
        processMetrics.setNumOfAuthors(authors.size());
        processMetrics.setNumOfRevisions(revisions.size());
        setChurn(processMetrics, locTouched);
        setLocAdded(processMetrics, locTouched);
        setChangeSet(processMetrics, changeSet);
        setAge(processMetrics, last);
        setAverageChangeTime(processMetrics, changeTime);
        setEntropy(processMetrics, entropy);
        var complexity = last.getMetrics();
        complexity.setSmellCount(smellCount);
        return Optional.of(
                ReleaseJavaClass.builder()
                        .path(last.getPath())
                        .name(last.getName())
                        .trackingId(last.getTrackingId())
                        .time(last.getTime())
                        .order(order)
                        .complexity(last.getMetrics())
                        .process(processMetrics)
                        .commits(commits)
                        .build());
    }

    private void setEntropy(ProcessClassMetrics metrics, DoubleAccumulator entropy) {
        var r = entropy.getResult();
        metrics.setEntropy(r.sum());
        metrics.setAvgEntropy(r.average());
        metrics.setMaxEntropy(r.max());
    }

    private Optional<RevisionJavaClass> getPrevious(RevisionJavaClass cls, LocalDate end) {
        var id = ClassID.of(cls);
        if (!history.containsKey(id)) {
            return Optional.empty();
        }

        var list = history.get(id)
                .stream()
                .filter(c -> c.getTime().toLocalDate().isBefore(end))
                .toList();
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(list.getLast());
    }

    private void setChurn(ProcessClassMetrics metrics, IntAccumulator locTouched) {
        var churn = locTouched.getResult();
        metrics.setChurn(churn.sum());
        metrics.setAvgChurn(churn.average());
        metrics.setMaxChurn(churn.max());
    }

    private void setLocAdded(ProcessClassMetrics metrics, IntAccumulator locTouched) {
        var locAdded = locTouched.getResult(i -> i > 0);
        metrics.setLocAdded(locAdded.sum());
        metrics.setAvgLocAdded(locAdded.average());
        metrics.setMaxLocAdded(locAdded.max());
    }

    private void setChangeSet(ProcessClassMetrics metrics, IntAccumulator changeSet) {
        var result = changeSet.getResult();
        metrics.setChangeSet(result.sum());
        metrics.setAvgChangeSet(result.average());
        metrics.setMaxChangeSet(result.max());
    }

    private void setAge(ProcessClassMetrics metrics, RevisionJavaClass last) {
        var id = ClassID.of(last);
        var fullHistory = history.get(id);
        if (fullHistory != null && !fullHistory.isEmpty()) {
            var firstEverTime = fullHistory.getFirst().getTime();
            var lastTime = last.getTime();
            var absoluteAge = Duration.between(firstEverTime, lastTime);
            metrics.setAge(absoluteAge);
        } else {
            metrics.setAge(Duration.ZERO);
        }
    }

    private void setAverageChangeTime(ProcessClassMetrics metrics, TimeAccumulator changeTime) {
        metrics.setAverageChangeTime((Duration) changeTime.getResult().average());
    }
}