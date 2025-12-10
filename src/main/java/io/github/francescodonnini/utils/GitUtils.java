package io.github.francescodonnini.utils;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GitUtils {
    private static final Logger logger = Logger.getLogger(GitUtils.class.getName());

    private GitUtils() {}

    public static List<RevCommit> getCommits(Path path, String remoteUrl) throws IOException, GitAPIException {
        try {
            return getCommits(path);
        } catch (GitAPIException e) {
            logger.log(Level.INFO, e.getMessage());
            logger.log(Level.INFO, "{0}", "git clone %s %s".formatted(remoteUrl, path));
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
