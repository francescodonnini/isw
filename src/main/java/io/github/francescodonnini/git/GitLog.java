package io.github.francescodonnini.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GitLog {
    private final Git git;

    public GitLog(String path) throws IOException {
        var repository = new FileRepositoryBuilder()
                .setGitDir(new File(path))
                .build();
        git = new Git(repository);
    }

    public List<RevCommit> getAll() throws GitAPIException {
        var commits = new ArrayList<RevCommit>();
        git.log().call().forEach(commits::add);
        return commits;
    }

    public List<Ref> getTags() throws GitAPIException {
        return git.tagList().call();
    }

    public List<String> listTree(String tag) throws IOException {
        var head = git.getRepository().findRef(tag);
        if (head == null) {
            return List.of();
        }
        var walk = new RevWalk(git.getRepository());
        var commit = walk.parseCommit(head.getObjectId());
        var tree = commit.getTree();
        var treeWalk = new TreeWalk(git.getRepository());
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        var files = new ArrayList<String>();
        while (treeWalk.next()) {
            var file = treeWalk.getPathString();
            if (file.endsWith(".java"))
                files.add(file);
        }
        return files;
    }
}
