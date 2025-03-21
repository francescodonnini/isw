
package io.github.francescodonnini.jira.json.issue;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class Progress {

    @Expose
    @SerializedName("progress")
    private Long value;
    @Expose
    private Long total;

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

}
