
package io.github.francescodonnini.jira.json.issue;

import com.google.gson.annotations.Expose;

@SuppressWarnings("unused")
public class Creator {

    @Expose
    private Boolean active;
    @Expose
    private AvatarUrls avatarUrls;
    @Expose
    private String displayName;
    @Expose
    private String key;
    @Expose
    private String name;
    @Expose
    private String self;
    @Expose
    private String timeZone;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public AvatarUrls getAvatarUrls() {
        return avatarUrls;
    }

    public void setAvatarUrls(AvatarUrls avatarUrls) {
        this.avatarUrls = avatarUrls;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

}
