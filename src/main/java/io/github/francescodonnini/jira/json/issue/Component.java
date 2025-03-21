
package io.github.francescodonnini.jira.json.issue;

import com.google.gson.annotations.Expose;

@SuppressWarnings("unused")
public class Component {

    @Expose
    private String id;
    @Expose
    private String name;
    @Expose
    private String self;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
