
package io.github.francescodonnini.jira.json.issue;

import com.google.gson.annotations.Expose;

@SuppressWarnings("unused")
public class StatusCategory {

    @Expose
    private String colorName;
    @Expose
    private Long id;
    @Expose
    private String key;
    @Expose
    private String name;
    @Expose
    private String self;

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

}
