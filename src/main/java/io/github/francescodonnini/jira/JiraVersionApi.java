package io.github.francescodonnini.jira;

import io.github.francescodonnini.jira.json.version.VersionNetworkEntity;
import io.github.francescodonnini.model.Version;

import java.net.URISyntaxException;
import java.util.List;

public class JiraVersionApi {
    private final RestApi restApi;

    public JiraVersionApi(RestApi restApi) {
        this.restApi = restApi;
    }

    public List<Version> getVersions(String projectName) {
        try {
            var result = restApi.getReleaseInfo(projectName);
            if (result == null || result.getVersions() == null) {
                return List.of();
            }
            return result.getVersions().stream()
                    .map(JiraVersionApi::fromVersionNetworkEntity)
                    .filter(v -> v.released() && v.releaseDate() != null)
                    .toList();
        } catch (URISyntaxException e) {
            return List.of();
        }
    }

    private static Version fromVersionNetworkEntity(VersionNetworkEntity v) {
        return new Version(v.getArchived(),
                v.getId(),
                v.getName(),
                v.getReleased(),
                v.getReleaseDate());
    }
}
