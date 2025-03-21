
package io.github.francescodonnini.jira.json.issue;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class Votes {

    @Expose
    private Boolean hasVoted;
    @Expose
    private String self;
    @Expose
    @SerializedName("votes")
    private Long numOfVotes;

    public Boolean getHasVoted() {
        return hasVoted;
    }

    public void setHasVoted(Boolean hasVoted) {
        this.hasVoted = hasVoted;
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public Long getNumOfVotes() {
        return numOfVotes;
    }

    public void setNumOfVotes(Long numOfVotes) {
        this.numOfVotes = numOfVotes;
    }

}
