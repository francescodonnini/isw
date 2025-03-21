
package io.github.francescodonnini.jira.json.issue;

import com.google.gson.annotations.Expose;

@SuppressWarnings("unused")
public class Project {

    @Expose
    private AvatarUrls avatarUrls;
    @Expose
    private String id;
    @Expose
    private String key;
    @Expose
    private String name;
    @Expose
    private ProjectCategory projectCategory;
    @Expose
    private String projectTypeKey;
    @Expose
    private String self;

    public AvatarUrls getAvatarUrls() {
        return avatarUrls;
    }

    public void setAvatarUrls(AvatarUrls avatarUrls) {
        this.avatarUrls = avatarUrls;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public ProjectCategory getProjectCategory() {
        return projectCategory;
    }

    public void setProjectCategory(ProjectCategory projectCategory) {
        this.projectCategory = projectCategory;
    }

    public String getProjectTypeKey() {
        return projectTypeKey;
    }

    public void setProjectTypeKey(String projectTypeKey) {
        this.projectTypeKey = projectTypeKey;
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

}
