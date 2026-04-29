package io.github.francescodonnini.data;

import io.github.francescodonnini.model.ClassID;
import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.Release;
import io.github.francescodonnini.model.ReleaseJavaClass;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class LabelMaker {
    private static final Logger logger = Logger.getLogger(LabelMaker.class.getName());
    private final Git git;
    private final List<Issue> issues;
    private final List<Release> releases;
    private Map<Release, Set<ReleaseJavaClass>> releaseClassMap;
    private Map<ClassID, List<ReleaseJavaClass>> idClassMap;
    private int buggy;

    public LabelMaker(Git git, List<Issue> issues, List<Release> releases) {
        this.issues = issues;
        this.git = git;
        this.releases = releases;
    }

    public void makeLabels(List<ReleaseJavaClass> classes) {
        try {
            createClassIndices(classes);
            var df = new DiffFormatter(DisabledOutputStream.INSTANCE);
            df.setRepository(git.getRepository());
            df.setDetectRenames(true);
            var index = new HashMap<String, List<ReleaseJavaClass>>();
            for (var cls : classes) {
                for (var commit : cls.getCommits()) {
                    index.computeIfAbsent(commit, h -> new ArrayList<>()).add(cls);
                }
            }
            var progress = 0;
            for (var issue : issues) {
                buggy = 0;
                for (var commit : issue.commits()) {
                    var susceptible = index.getOrDefault(commit.getName(), List.of());
                    if (!susceptible.isEmpty()) {
                        parseCommit(df, susceptible, commit, issue);
                    }
                }
                progress++;
                logger.log(Level.INFO, "{0}/{1} ({2}%)", new Object[]{progress, issues.size(), ((double)progress / issues.size() * 100)});
                logger.log(Level.INFO, "buggy classes: {0}", buggy);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    private void createClassIndices(List<ReleaseJavaClass> classes) {
        releaseClassMap = new HashMap<>();
        var prev = LocalDate.MIN;
        for (var release : releases) {
            var curr = release.releaseDate();
            final var finalPrev = prev;
            classes.stream()
                    .filter(c -> isBetween(c, finalPrev, curr))
                    .forEach(c -> releaseClassMap.computeIfAbsent(release, h -> new HashSet<>()).add(c));
            prev = curr;
        }
        idClassMap = classes.stream()
                .collect(Collectors.groupingBy(ClassID::of));
    }

    private boolean isBetween(ReleaseJavaClass c, LocalDate a, LocalDate b) {
        var time = c.getTime().toLocalDate();
        return !time.isBefore(a) && !time.isAfter(b);
    }


    private void parseCommit(DiffFormatter df, List<ReleaseJavaClass> susceptible, RevCommit commit, Issue issue) throws IOException {
        var diffList = df.scan(getParent(commit), commit.getTree());
        for (var diff : diffList) {
            var path = Path.of(diff.getNewPath());
            susceptible.stream()
                    .filter(c -> c.getPath().equals(path))
                    .forEach(c -> setBuggy(c, issue));
        }
    }

    private void setBuggy(ReleaseJavaClass c, Issue issue) {
        c.getProcessMetrics().incNumOfFixes();
        backtrack(c, issue.affectedVersions());
    }

    private void backtrack(ReleaseJavaClass c, List<Release> affectedVersions) {
        Optional.ofNullable(idClassMap.get(ClassID.of(c)))
                .ifPresent(list -> list.stream()
                        .filter(x -> x != c)
                        .filter(x -> isAffected(x, affectedVersions))
                        .forEach(x -> {
                            x.setBuggy(true);
                            ++buggy;
                        }));
    }

    private boolean isAffected(ReleaseJavaClass c, List<Release> affectedVersions) {
        for (var r : affectedVersions) {
            if (releaseClassMap.getOrDefault(r, Set.of()).contains(c)) {
                return true;
            }
        }
        return false;
    }

    private RevTree getParent(RevCommit commit) {
        try {
            return commit.getParent(0).getTree();
        } catch (IndexOutOfBoundsException e) {
            logger.log(Level.INFO, "commit %s has no parent".formatted(commit));
            return null;
        }
    }
}
