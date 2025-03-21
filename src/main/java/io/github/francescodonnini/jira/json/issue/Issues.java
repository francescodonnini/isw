package io.github.francescodonnini.jira.json.issue;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Issues {
    @Expose
    private String expand;
    @Expose
    private int startAt;
    @Expose
    private int maxResults;
    @Expose
    private int total;
    @Expose
    @SerializedName("issues")
    private List<IssueNetworkEntity> issueList;

    public String getExpand() {
        return expand;
    }

    public void setExpand(String expand) {
        this.expand = expand;
    }

    public int getStartAt() {
        return startAt;
    }

    public void setStartAt(int startAt) {
        this.startAt = startAt;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<IssueNetworkEntity> getIssueList() {
        return issueList;
    }

    public void setIssueList(List<IssueNetworkEntity> issueList) {
        this.issueList = issueList;
    }
}
