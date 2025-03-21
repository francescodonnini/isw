
package io.github.francescodonnini.jira.json.issue;

import com.google.gson.annotations.Expose;

@SuppressWarnings("unused")
public class AggregateProgress {

    @Expose
    private Long progress;
    @Expose
    private Long total;

    public Long getProgress() {
        return progress;
    }

    public void setProgress(Long progress) {
        this.progress = progress;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

}
