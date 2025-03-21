
package io.github.francescodonnini.jira.json.issue;

import com.google.gson.annotations.Expose;

@SuppressWarnings("unused")
public class Watches {

    @Expose
    private Boolean isWatching;
    @Expose
    private String self;
    @Expose
    private Long watchCount;

    public Boolean getIsWatching() {
        return isWatching;
    }

    public void setIsWatching(Boolean isWatching) {
        this.isWatching = isWatching;
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public Long getWatchCount() {
        return watchCount;
    }

    public void setWatchCount(Long watchCount) {
        this.watchCount = watchCount;
    }

}
