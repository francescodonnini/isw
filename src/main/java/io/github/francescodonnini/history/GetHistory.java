package io.github.francescodonnini.history;

import io.github.francescodonnini.model.Release;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetHistory {
    private final Path path;
    private final Repository repository;
    private final List<Release> releases;

    public GetHistory(Path path, List<Release> releases) throws IOException {
        this.path = path;
        this.repository = new FileRepositoryBuilder()
                .setGitDir(path.resolve(".git").toFile())
                .build();
        this.releases = releases;
    }

    public Map<String, List<FileHistory>> getHistories() throws GitAPIException, IOException {
        try (Git git = new Git(repository)) {
            var tags = git.tagList()
                    .call()
                    .stream()
                    .map(Ref::getName)
                    .toList();
            var histories = new HashMap<String, List<FileHistory>>();
            for (var r : releases) {
                var o = tags.stream().filter(t -> t.endsWith(r.name())).findFirst();
                // Non è stata trovata la release indicata nei tag di github tra le release selezionate da Jira quindi
                // si scarta il tag.
                if (o.isEmpty()) {
                    continue;
                }
                var tag = o.get();
                git.checkout().setName(tag).call();
                for (Path f : listAllFiles(path)) {
                    if (isJavaFile(f)) {
                        histories.put(f.toString(), getHistory(f.toString()));
                    }
                }
            }
            return histories;
        }
    }

    private List<Path> listAllFiles(Path basePath) throws IOException {
        var paths = new ArrayList<Path>();
        paths.add(basePath);
        var files = new ArrayList<Path>();
        while (!paths.isEmpty()) {
            var path = paths.removeLast();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (Path entry : stream) {
                    if (Files.isDirectory(entry)) {
                        if (!Files.isHidden(entry)) {
                            paths.add(entry);
                        }
                    } else if (isJavaFile(entry.toString())) {
                        files.add(basePath.relativize(entry));
                    }
                }
            }
        }
        return files;
    }

    public boolean isJavaFile(Path path) {
        return isJavaFile(path.toString());
    }

    private boolean isJavaFile(String path) {
        return path.endsWith(".java") && !path.endsWith("package-info.java");
    }

    public List<FileHistory> getHistory(String path)
            throws GitAPIException, IOException {
        List<FileHistory> history = new ArrayList<>();
        try (Git git = new Git(repository)) {
            Iterable<RevCommit> commits = git.log()
                    .addPath(path)
                    .call();
            for (RevCommit commit : commits) {
                RevTree tree = commit.getTree();
                try (TreeWalk treeWalk = new TreeWalk(repository)) {
                    treeWalk.addTree(tree);
                    treeWalk.setRecursive(true);
                    treeWalk.setFilter(PathFilter.create(path));
                    if (treeWalk.next()) {
                        ObjectId objectId = treeWalk.getObjectId(0);
                        ObjectLoader loader = repository.open(objectId);
                        try (var is = loader.openStream()) {
                            byte[] contentBytes = is.readAllBytes();
                            String content = new String(contentBytes);
                            history.add(new FileHistory(commit.getName(), commit.getAuthorIdent().getName(),
                                    getCommitTime(commit), content));
                        }
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
