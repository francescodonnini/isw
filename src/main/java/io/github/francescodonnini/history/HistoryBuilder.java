package io.github.francescodonnini.history;

import io.github.francescodonnini.model.Release;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HistoryBuilder {
    private Release lastRelease;
    private String filePath;
    private String sourcePath;

    public HistoryBuilder setLastRelease(Release lastRelease) {
        this.lastRelease = lastRelease;
        return this;
    }

    public HistoryBuilder setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public HistoryBuilder setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
        return this;
    }

    public List<FileHistory> build() throws IOException, GitAPIException {
        try (var git = new Git(new FileRepositoryBuilder()
                .setGitDir(Path.of(sourcePath, ".git").toFile())
                .build())) {
            var o = git.tagList()
                    .call()
                    .stream()
                    .map(Ref::getName)
                    .filter(t -> t.endsWith(lastRelease.name()))
                    .findFirst();
            // Non è stata trovata la release indicata nei tag di github tra le release selezionate da Jira quindi
            // si scarta il tag.
            if (o.isEmpty()) {
                return List.of();
            }
            git.checkout().setName(o.get()).call();
            return getHistory(git, filePath);
        }
    }

    public List<FileHistory> getHistory(Git git, String path)
            throws GitAPIException, IOException {
        var repository = git.getRepository();
        var history = new ArrayList<FileHistory>();
        var commits = git.log()
                .addPath(path)
                .call();
        for (RevCommit commit : commits) {
            var tree = commit.getTree();
            try (var treeWalk = new TreeWalk(repository)) {
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);
                treeWalk.setFilter(PathFilter.create(path));
                if (treeWalk.next()) {
                    var objectId = treeWalk.getObjectId(0);
                    var loader = repository.open(objectId);
                    try (var is = loader.openStream()) {
                        var contentBytes = is.readAllBytes();
                        var content = new String(contentBytes);
                        history.add(new FileHistory(commit.getName(), commit.getAuthorIdent().getName(),
                                getCommitTime(commit), content));
                    }
                }
            }
        }
        return history;
    }

    private static LocalDateTime getCommitTime(RevCommit commit) {
        return commit.getAuthorIdent().getWhenAsInstant().atZone(commit.getAuthorIdent().getZoneId()).toLocalDateTime();
    }
}
