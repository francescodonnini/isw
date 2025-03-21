package io.github.francescodonnini.model;

import org.eclipse.jgit.revwalk.RevCommit;

import java.time.LocalDateTime;
import java.util.List;

public record Issue(
        List<Release> affectedVersions,
        LocalDateTime created,
        Release fixVersion,
        Release openingVersion,
        List<RevCommit> commits,
        String key,
        String project) {}