
package io.github.francescodonnini.jira.json.version;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import io.github.francescodonnini.jira.json.adapters.ShortDateTypeAdapter;

import java.time.LocalDate;

@SuppressWarnings("unused")
public class VersionNetworkEntity {
    @Expose
    private Boolean archived;
    @Expose
    private String description;
    @Expose
    private String id;
    @Expose
    private String name;
    @Expose
    @JsonAdapter(ShortDateTypeAdapter.class)
    private LocalDate releaseDate;
    @Expose
    private Boolean released;
    @Expose
    private String self;

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

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

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Boolean getReleased() {
        return released;
    }

    public void setReleased(Boolean released) {
        this.released = released;
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

}
