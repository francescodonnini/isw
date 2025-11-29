package io.github.francescodonnini.data;

import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.JavaMethod;
import io.github.francescodonnini.model.Release;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class LabelMakerImpl implements LabelMaker {
    private static final Logger logger = Logger.getLogger(LabelMakerImpl.class.getName());
    private final Git git;
    private final List<Issue> issues;
    private final List<Release> releases;
    private final Map<String, HashSet<String>> releaseMethodMap = new HashMap<>();

    public LabelMakerImpl(Git git, List<Issue> issues, List<Release> releases) {
        this.issues = issues;
        this.git = git;
        this.releases = releases;
    }

    @Override
    public void makeLabels(List<JavaMethod> methods) {
        try {
            createReleaseMethodMap(methods);
            var df = new DiffFormatter(DisabledOutputStream.INSTANCE);
            df.setRepository(git.getRepository());
            df.setDetectRenames(true);
            var index = methods.stream()
                    .collect(Collectors.groupingBy(m -> m.getJavaClass().getCommit()));
            for (var issue : issues) {
                for (var commit : issue.commits()) {
                    var susceptible = index.getOrDefault(commit.getName(), List.of());
                    if (!susceptible.isEmpty()) {
                        parseCommit(df, susceptible, commit, issue, methods);
                    }
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    private void createReleaseMethodMap(List<JavaMethod> methods) {
        var prev = LocalDate.MIN;
        for (var release : releases) {
            var curr = release.releaseDate();
            LocalDate finalPrev = prev;
            methods.stream()
                    .filter(m -> isBetween(m, finalPrev, curr))
                    .forEach(m -> releaseMethodMap.computeIfAbsent(release.id(), h -> new HashSet<>()).add(getId(m)));
            prev = curr;
        }
    }

    private boolean isBetween(JavaMethod m, LocalDate a, LocalDate b) {
        var time = m.getJavaClass().getTime().toLocalDate();
        return !time.isBefore(a) && !time.isAfter(b);
    }


    private void parseCommit(DiffFormatter df, List<JavaMethod> susceptible, RevCommit commit, Issue issue, List<JavaMethod> methods) throws IOException {
        var diffList = df.scan(getParent(commit), commit.getTree());
        for (var diff : diffList) {
            var path = diff.getNewPath();
            var editList = df.toFileHeader(diff).toEditList();
            susceptible.stream()
                    .filter(m -> m.getPath().toString().equals(path))
                    .filter(m -> EditUtils.isTouched(m, editList, this::match))
                    .forEach(m -> setBuggy(m, issue, methods));
        }
    }

    private void setBuggy(JavaMethod m, Issue issue, List<JavaMethod> methods) {
        m.setBuggy(true);
        backtrack(m, issue.affectedVersions(), methods);
    }

    private String getId(JavaMethod m) {
        return m.getPath().toString() + m.getSignature();
    }

    private void backtrack(JavaMethod m, List<Release> affectedVersions, List<JavaMethod> methods) {
        methods.stream()
                .filter(x -> x != m)
                .filter(x -> getId(x).equals(getId(m)))
                .filter(x -> isAffected(x, affectedVersions))
                .forEach(x -> x.setBuggy(true));
    }

    private boolean isAffected(JavaMethod m, List<Release> affectedVersions) {
        for (var r : affectedVersions) {
            if (releaseMethodMap.getOrDefault(r.id(), HashSet.newHashSet(0)).contains(getId(m))) {
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

    private boolean match(Edit edit) {
        return switch (edit.getType()) {
            case DELETE, INSERT, REPLACE -> true;
            default -> false;
        };
    }
}
