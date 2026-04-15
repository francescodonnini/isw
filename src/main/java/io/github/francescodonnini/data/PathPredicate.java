package io.github.francescodonnini.data;

import io.github.francescodonnini.model.Release;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

class PathPredicate implements Predicate<Path> {
    private boolean lastCommitPerRelease = false;
    private final Set<String> commitChangeSet = new HashSet<>();
    private final Set<String> releaseChangeSet = new HashSet<>();

    @Override
    public boolean test(Path path) {
        var s = path.toString();
        if (!s.endsWith(".java") || s.endsWith("package-info.java")) {
            return false;
        }
        if (lastCommitPerRelease) {
            return commitChangeSet.contains(path.toString()) || !releaseChangeSet.contains(path.toString());
        }
        return commitChangeSet.contains(path.toString());
    }

    public void setLastCommitPerRelease(Release release) {
        this.lastCommitPerRelease = release != null;
    }

    public void add(Set<String> files) {
        commitChangeSet.clear();
        commitChangeSet.addAll(files);
        if (!lastCommitPerRelease) {
            releaseChangeSet.addAll(files);
        }
    }

    public void reset() {
        lastCommitPerRelease = false;
        commitChangeSet.clear();
        releaseChangeSet.clear();
    }
}