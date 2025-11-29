package io.github.francescodonnini.jira;

import io.github.francescodonnini.data.VersionApi;
import io.github.francescodonnini.model.Release;
import io.github.francescodonnini.model.Version;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class JiraReleaseApi {
    private final VersionApi versionApi;

    public JiraReleaseApi(VersionApi versionApi) {
        this.versionApi = versionApi;
    }

    public List<Release> getReleases(String projectName) {
        var versions = versionApi.getVersions(projectName).stream()
                .filter(Version::released)
                .filter(v -> v.releaseDate() != null)
                .sorted(Comparator.comparing(Version::releaseDate))
                .toList();
        var order = 0;
        var releases = new ArrayList<Release>();
        for (Version v : versions) {
            releases.add(new Release(v.id(), v.name(), v.releaseDate(), order));
            order++;
        }
        return releases;
    }
}
