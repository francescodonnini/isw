package io.github.francescodonnini.data;

import io.github.francescodonnini.model.Version;

import java.util.List;

public interface VersionApi {
    List<Version> getVersions();
}
