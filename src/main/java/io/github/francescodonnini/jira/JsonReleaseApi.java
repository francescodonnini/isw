package io.github.francescodonnini.jira;

import io.github.francescodonnini.data.VersionApi;
import io.github.francescodonnini.model.Release;
import io.github.francescodonnini.model.Version;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class JsonReleaseApi {
    private final VersionApi versionApi;

    public JsonReleaseApi(VersionApi versionApi) {
        this.versionApi = versionApi;
    }

    public List<Release> getReleases() {
        var versions = versionApi.getVersions().stream()
                .filter(Version::released).filter(v -> v.releaseDate() != null)
                .sorted(Comparator.comparing(Version::releaseDate)).toList();
        var releases = new ArrayList<Release>();
        for (int i = 0; i < versions.size(); i++) {
            var v = versions.get(i);
            releases.add(new Release(i, v.id(), v.name(), v.releaseDate()));
        }
        return releases;
    }
}
