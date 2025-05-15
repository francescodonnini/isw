package io.github.francescodonnini.data;

import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.JavaMethod;
import io.github.francescodonnini.model.LineRange;
import io.github.francescodonnini.model.Release;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class LabelMakerImpl implements LabelMaker {
    private static final Logger logger = Logger.getLogger(LabelMakerImpl.class.getName());
    private final Git git;
    private final List<Issue> issues;
    private final List<JavaMethod> methods;
    private final List<Release> releases;

    public LabelMakerImpl(Git git, List<Issue> issues, List<JavaMethod> methods, List<Release> releases) {
        this.issues = issues;
        this.methods = methods;
        this.git = git;
        this.releases = releases;
    }

    @Override
    public List<JavaMethod> makeLabels() {
        try {
            var df = new DiffFormatter(DisabledOutputStream.INSTANCE);
            df.setRepository(git.getRepository());
            df.setDetectRenames(true);
            var index = methods.stream()
                    .collect(Collectors.groupingBy(m -> m.getJavaClass().getCommit()));
            for (var issue : issues) {
                for (var commit : issue.commits()) {
                    var susceptibles = index.getOrDefault(commit.getName(), List.of());
                    if (!susceptibles.isEmpty()) {
                        parseCommit(df, susceptibles, commit, issue);
                    }
                }
            }
            return methods;
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
        return List.of();
    }

    private void parseCommit(DiffFormatter df, List<JavaMethod> susceptibles, RevCommit commit, Issue issue) throws IOException {
        var diffList = df.scan(getParent(commit), commit.getTree());
        for (var diff : diffList) {
            var path = diff.getNewPath();
            var editList = df.toFileHeader(diff).toEditList();
            susceptibles.stream()
                    .filter(m -> m.getPath().toString().equals(path))
                    .filter(m -> isTouched(m, editList))
                    .forEach(m -> setBuggy(m, issue));
        }
    }

    private void setBuggy(JavaMethod m, Issue issue) {
        m.setBuggy(true);
        methods.stream()
                .filter(x -> x != m)
                .filter(x -> getId(m).equals(getId(x)))
                .filter(x -> isAffected(m, issue.affectedVersions()))
                .forEach(x -> x.setBuggy(true));
    }

    private String getId(JavaMethod m) {
        return m.getPath().toString() + m.getSignature();
    }

    private boolean isAffected(JavaMethod m, List<Release> affectedVersions) {
        for (var r : affectedVersions) {
            var p = releases.stream()
                    .filter(x -> x.isBefore(r))
                    .max((x, y) -> x.releaseDate().compareTo(y.releaseDate()));
            if (p.isEmpty())
                return false;
            else if (isBetween(m, p.get(), r))
                return true;
        }
        return false;
    }

    private boolean isBetween(JavaMethod m, Release p, Release r) {
        var time = m.getJavaClass().getTime().toLocalDate();
        return !time.isBefore(p.releaseDate()) && !time.isAfter(r.releaseDate());
    }

    private RevTree getParent(RevCommit commit) {
        try {
            return commit.getParent(0).getTree();
        } catch (IndexOutOfBoundsException _) {
            logger.log(Level.INFO, "commit %s has no parent".formatted(commit));
            return null;
        }
    }

    private boolean isTouched(JavaMethod m, EditList edits) {
        return edits.stream()
                .filter(this::match)
                .anyMatch(e -> overlaps(m, e));
    }

    private boolean match(Edit edit) {
        switch (edit.getType()) {
            case DELETE, INSERT, REPLACE:
                return true;
            default:
                return false;
        }
    }

    private boolean overlaps(JavaMethod m, Edit e) {
        e.extendB();
        var range = new LineRange(e.getBeginB(), e.getEndB());
        return m.getRange().intersects(range);
    }
}
