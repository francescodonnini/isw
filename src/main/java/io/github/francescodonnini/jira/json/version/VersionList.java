package io.github.francescodonnini.jira.json.version;

import com.google.gson.annotations.Expose;

import java.util.List;

public class VersionList {
    @Expose
    private List<VersionNetworkEntity> versions;

    public List<VersionNetworkEntity> getVersions() {
        return versions;
    }

    public void setVersions(List<VersionNetworkEntity> versions) {
        this.versions = versions;
    }
}
