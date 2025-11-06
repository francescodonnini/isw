package io.github.francescodonnini.jira;

import io.github.francescodonnini.jira.json.version.VersionNetworkEntity;
import io.github.francescodonnini.model.Version;

import java.net.URISyntaxException;
import java.util.List;

public class JiraVersionApi {
    private final String projectName;
    private final RestApi restApi;

    public JiraVersionApi(String projectName, RestApi restApi) {
        this.projectName = projectName;
        this.restApi = restApi;
    }

    public List<Version> getVersions() {
        try {
            return restApi.getReleaseInfo(projectName).getVersions().stream()
                    .map(JiraVersionApi::fromVersionNetworkEntity)
                    .toList();
        } catch (URISyntaxException _) {
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
