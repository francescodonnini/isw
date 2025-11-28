package io.github.francescodonnini.data;

import io.github.francescodonnini.model.Release;

import java.util.List;

public interface ReleaseApi {
    List<Release> getReleases(String projectName);
}
