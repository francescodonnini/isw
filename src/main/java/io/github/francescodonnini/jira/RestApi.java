package io.github.francescodonnini.jira;

import com.google.gson.Gson;
import io.github.francescodonnini.jira.json.issue.Issues;
import io.github.francescodonnini.jira.json.version.VersionList;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class RestApi {
    private final HttpClient client = HttpClient
            .newBuilder()
            .build();

    public Issues getIssues(String gql) throws URISyntaxException {
        return getIssues(gql, List.of(), 0, 1000, List.of());
    }

    public Issues getIssues(String jql, List<String> fields, int startAt, int maxResults, List<String> properties) throws URISyntaxException {
        var request = new StringBuilder()
                .append(createSearchQuery(jql))
                .append(String.format("&startAt=%d", startAt))
                .append(String.format("&maxResults=%d", maxResults));
        if (!fields.isEmpty()) {
            request.append("&fields=").append(String.join(",", fields));
        }
        if (!properties.isEmpty()) {
            request.append("&properties=").append(String.join(",", properties));
        }
        return get(request.toString().replace(" ", "%20"), Issues.class);
    }

    private static String createSearchQuery(String jql) {
        return String.format("https://issues.apache.org/jira/rest/api/2/search?jql=%s", jql);
    }

    public VersionList getReleaseInfo(String projectName) throws URISyntaxException {
        return get("https://issues.apache.org/jira/rest/api/2/project/" + projectName, VersionList.class);
    }

    public <T> T get(String uri, Class<T> clazz) throws URISyntaxException {
        var request = HttpRequest.newBuilder()
                .uri(new URI(uri))
                .header("Accept", "application/json")
                .GET()
                .build();

        return client
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply((String json) -> {
                    var mapper = new GsonMapper<T>();
                    return mapper.from(json, clazz);
                })
                .join();
    }

    private static class GsonMapper<T> {
        public T from(String json, Class<T> clazz) {
            var gson = new Gson();
            return gson.fromJson(json, clazz);
        }
    }
}
