package io.github.francescodonnini.pipeline;

import io.github.francescodonnini.data.IssueApi;
import io.github.francescodonnini.data.ReleaseApi;

public class Api {
    private final IssueApi issueApi;
    private final ReleaseApi releaseApi;

    public Api(IssueApi issueApi, ReleaseApi releaseApi) {
        this.issueApi = issueApi;
        this.releaseApi = releaseApi;
    }

    public IssueApi getIssueApi() {
        return issueApi;
    }

    public ReleaseApi getReleaseApi() {
        return releaseApi;
    }
}
