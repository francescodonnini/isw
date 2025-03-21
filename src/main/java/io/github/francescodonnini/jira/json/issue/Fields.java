
package io.github.francescodonnini.jira.json.issue;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import io.github.francescodonnini.jira.json.adapters.LongDateTypeAdapter;
import io.github.francescodonnini.jira.json.version.VersionNetworkEntity;

import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings("unused")
public class Fields {

    @Expose
    @SerializedName("aggregateprogress")
    private AggregateProgress aggregateProgress;
    @Expose
    @SerializedName("aggregatetimeestimate")
    private Object aggregateTimeEstimate;
    @Expose
    @SerializedName("aggregatetimeoriginalestimate")
    private Object aggregateTimeOriginalEstimate;
    @Expose
    @SerializedName("aggregatetimespent")
    private Object aggregateTimeSpent;
    @Expose
    private Object assignee;
    @Expose
    private List<Component> components;
    @Expose
    @JsonAdapter(LongDateTypeAdapter.class)
    private LocalDateTime created;
    @Expose
    private Creator creator;
    @Expose
    private String description;
    @Expose
    @SerializedName("duedate")
    private Object dueDate;
    @Expose
    private Object environment;
    @Expose
    private List<FixVersion> fixVersions;
    @Expose
    @SerializedName("issuelinks")
    private List<Object> issueLinks;
    @Expose
    @SerializedName("issuetype")
    private Issuetype issueType;
    @Expose
    private List<Object> labels;
    @Expose
    private String lastViewed;
    @Expose
    private Priority priority;
    @Expose
    private Progress progress;
    @Expose
    private Project project;
    @Expose
    private Reporter reporter;
    @Expose
    private Object resolution;
    @Expose
    @SerializedName("resolutiondate")
    @JsonAdapter(LongDateTypeAdapter.class)
    private LocalDateTime resolutionDate;
    @Expose
    private Status status;
    @Expose
    private List<Object> subtasks;
    @Expose
    private String summary;
    @Expose
    @SerializedName("timeestimate")
    private Object timeEstimate;
    @Expose
    @SerializedName("timeoriginalestimate")
    private Object timeOriginalEstimate;
    @Expose
    @SerializedName("timespent")
    private Object timeSpent;
    @Expose
    private String updated;
    @Expose
    @SerializedName("versions")
    private List<VersionNetworkEntity> affectedVersions;
    @Expose
    private Votes votes;
    @Expose
    private Watches watches;
    @Expose
    @SerializedName("workratio")
    private Long workRatio;

    public AggregateProgress getAggregateProgress() {
        return aggregateProgress;
    }

    public void setAggregateProgress(AggregateProgress aggregateProgress) {
        this.aggregateProgress = aggregateProgress;
    }

    public Object getAggregateTimeEstimate() {
        return aggregateTimeEstimate;
    }

    public void setAggregateTimeEstimate(Object aggregateTimeEstimate) {
        this.aggregateTimeEstimate = aggregateTimeEstimate;
    }

    public Object getAggregateTimeOriginalEstimate() {
        return aggregateTimeOriginalEstimate;
    }

    public void setAggregateTimeOriginalEstimate(Object aggregateTimeOriginalEstimate) {
        this.aggregateTimeOriginalEstimate = aggregateTimeOriginalEstimate;
    }

    public Object getAggregateTimeSpent() {
        return aggregateTimeSpent;
    }

    public void setAggregateTimeSpent(Object aggregateTimeSpent) {
        this.aggregateTimeSpent = aggregateTimeSpent;
    }

    public Object getAssignee() {
        return assignee;
    }

    public void setAssignee(Object assignee) {
        this.assignee = assignee;
    }

    public List<Component> getComponents() {
        return components;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public Creator getCreator() {
        return creator;
    }

    public void setCreator(Creator creator) {
        this.creator = creator;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getDueDate() {
        return dueDate;
    }

    public void setDueDate(Object dueDate) {
        this.dueDate = dueDate;
    }

    public Object getEnvironment() {
        return environment;
    }

    public void setEnvironment(Object environment) {
        this.environment = environment;
    }

    public List<FixVersion> getFixVersions() {
        return fixVersions;
    }

    public void setFixVersions(List<FixVersion> fixVersions) {
        this.fixVersions = fixVersions;
    }

    public List<Object> getIssueLinks() {
        return issueLinks;
    }

    public void setIssueLinks(List<Object> issueLinks) {
        this.issueLinks = issueLinks;
    }

    public Issuetype getIssueType() {
        return issueType;
    }

    public void setIssueType(Issuetype issueType) {
        this.issueType = issueType;
    }

    public List<Object> getLabels() {
        return labels;
    }

    public void setLabels(List<Object> labels) {
        this.labels = labels;
    }

    public String getLastViewed() {
        return lastViewed;
    }

    public void setLastViewed(String lastViewed) {
        this.lastViewed = lastViewed;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Progress getProgress() {
        return progress;
    }

    public void setProgress(Progress progress) {
        this.progress = progress;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Reporter getReporter() {
        return reporter;
    }

    public void setReporter(Reporter reporter) {
        this.reporter = reporter;
    }

    public Object getResolution() {
        return resolution;
    }

    public void setResolution(Object resolution) {
        this.resolution = resolution;
    }

    public LocalDateTime getResolutionDate() {
        return resolutionDate;
    }

    public void setResolutionDate(LocalDateTime resolutionDate) {
        this.resolutionDate = resolutionDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Object> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(List<Object> subtasks) {
        this.subtasks = subtasks;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Object getTimeEstimate() {
        return timeEstimate;
    }

    public void setTimeEstimate(Object timeEstimate) {
        this.timeEstimate = timeEstimate;
    }

    public Object getTimeOriginalEstimate() {
        return timeOriginalEstimate;
    }

    public void setTimeOriginalEstimate(Object timeOriginalEstimate) {
        this.timeOriginalEstimate = timeOriginalEstimate;
    }

    public Object getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(Object timeSpent) {
        this.timeSpent = timeSpent;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public List<VersionNetworkEntity> getAffectedVersions() {
        return affectedVersions;
    }

    public void setAffectedVersions(List<VersionNetworkEntity> affectedVersions) {
        this.affectedVersions = affectedVersions;
    }

    public Votes getVotes() {
        return votes;
    }

    public void setVotes(Votes votes) {
        this.votes = votes;
    }

    public Watches getWatches() {
        return watches;
    }

    public void setWatches(Watches watches) {
        this.watches = watches;
    }

    public Long getWorkRatio() {
        return workRatio;
    }

    public void setWorkRatio(Long workratio) {
        this.workRatio = workratio;
    }

}
