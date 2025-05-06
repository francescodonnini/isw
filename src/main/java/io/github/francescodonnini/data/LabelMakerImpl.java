package io.github.francescodonnini.data;

import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.JavaMethod;
import io.github.francescodonnini.model.LineRange;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class LabelMakerImpl implements LabelMaker {
    private static final Logger logger = Logger.getLogger(LabelMakerImpl.class.getName());
    private final Git git;
    private final List<Issue> issues;
    private final List<JavaMethod> methods;

    public LabelMakerImpl(Git git, List<Issue> issues, List<JavaMethod> methods) {
        this.issues = issues;
        this.methods = methods;
        this.git = git;
    }

    @Override
    public List<JavaMethod> makeLabels() {
        try {
            var index = methods.stream()
                    .collect(Collectors.groupingBy(m -> m.getJavaClass().getCommit()));
            for (var issue : issues) {
                for (var commit : issue.commits()) {
                    var susceptibles = index.get(commit.getName());
                    parseCommit(susceptibles, commit);
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
        return List.of();
    }

    private void parseCommit(List<JavaMethod> susceptibles, RevCommit commit) throws IOException {
        var df = new DiffFormatter(DisabledOutputStream.INSTANCE);
        df.setRepository(git.getRepository());
        df.setDetectRenames(true);
        var diffList = df.scan(getParent(commit), commit.getTree());
        for (var diff : diffList) {
            var path = diff.getNewPath();
            var editList = df.toFileHeader(diff).toEditList();
            susceptibles.stream()
                    .filter(m -> m.getPath().toString().equals(path))
                    .filter(m -> isTouched(m, editList))
                    .forEach(m -> m.setBuggy(true));
        }
    }

    private boolean isTouched(JavaMethod m, EditList edits) {
        for (var edit : edits) {
            edit.extendA();
            var lineAdded = new LineRange(edit.getBeginA(), edit.getEndA());
            edit.extendB();
            var lineRemoved = new LineRange(edit.getBeginB(), edit.getEndB());
            if (m.getRange().intersects(lineAdded) || m.getRange().intersects(lineRemoved)) {
                return true;
            }
        }
        return false;
    }

    private RevTree getParent(RevCommit commit) {
        try {
            return commit.getParent(0).getTree();
        } catch (IndexOutOfBoundsException _) {
            logger.log(Level.INFO, "commit %s has no parent".formatted(commit));
            return null;
        }
    }
}
