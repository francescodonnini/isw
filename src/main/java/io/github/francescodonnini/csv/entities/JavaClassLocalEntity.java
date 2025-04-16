package io.github.francescodonnini.csv.entities;

import com.opencsv.bean.CsvBindByName;

public class JavaClassLocalEntity {
    @CsvBindByName(column = "name", required = true)
    private String path;
    @CsvBindByName(column = "parent", required = true)
    private String parent;
    @CsvBindByName(column = "releaseId", required = true)
    private String releaseId;

    public String getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(String releaseId) {
        this.releaseId = releaseId;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
