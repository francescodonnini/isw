package io.github.francescodonnini.data;

import io.github.francescodonnini.model.Issue;

import java.util.List;

public interface IssueApi {
    List<Issue> getIssues(String projectName);
}
