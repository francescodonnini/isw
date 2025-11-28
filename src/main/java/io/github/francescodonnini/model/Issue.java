package io.github.francescodonnini.model;

import org.eclipse.jgit.revwalk.RevCommit;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public record Issue(
        List<Release> affectedVersions,
        LocalDateTime created,
        Release fixVersion,
        Release openingVersion,
        List<RevCommit> commits,
        String key,
        String project) {
    public Issue withAffectedVersions(List<Release> affectedVersions) {
        return new Issue(affectedVersions, created, fixVersion, openingVersion, commits, key, project);
    }

    public Optional<Release> injectedVersion() {
        if (affectedVersions.isEmpty()) {
            return Optional.empty();
        }
        return affectedVersions.stream().min(Comparator.comparing(Release::releaseDate));
    }
}