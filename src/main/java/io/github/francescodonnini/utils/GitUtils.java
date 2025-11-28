package io.github.francescodonnini.utils;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GitUtils {
    private GitUtils() {}

    public static List<RevCommit> getCommits(Path path, String remoteUrl) throws IOException, GitAPIException {
        try {
            return getCommits(path);
        } catch (GitAPIException e) {
            var git = Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setDirectory(path.toFile())
                    .call();
            git.close();
            return getCommits(path);
        }
    }

    public static List<RevCommit> getCommits(Path path) throws IOException, GitAPIException {
        try (var git = createGit(path)) {
            var commits = new ArrayList<RevCommit>();
            git.log().call().forEach(commits::add);
            return commits;
        }
    }

    public static Git createGit(Path path) throws IOException {
        try (var repository = new FileRepositoryBuilder()
                .setGitDir(path.resolve(".git").toFile())
                .build()) {
            return new Git(repository);
        }
    }
}
