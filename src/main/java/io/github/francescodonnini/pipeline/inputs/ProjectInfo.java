package io.github.francescodonnini.pipeline.inputs;

import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.Release;
import io.github.francescodonnini.model.ReleaseJavaClass;
import io.github.francescodonnini.model.RevisionJavaClass;

import java.util.ArrayList;
import java.util.List;

public class ProjectInfo {
    private String project;
    private Proportion proportion;
    private double movingWindowPercentage;
    private final List<Release> releases = new ArrayList<>();
    private final List<ReleaseJavaClass> releaseClasses = new ArrayList<>();
    private final List<RevisionJavaClass> revisionClasses = new ArrayList<>();
    private final List<Issue> issues = new ArrayList<>();
    private boolean fromStart;

    public List<Release> getAllReleases() {
        return releases.subList(0, 26);
    }

    public void setAllReleases(List<Release> allReleases) {
        this.releases.clear();
        this.releases.addAll(allReleases);
    }

    public List<ReleaseJavaClass> getReleaseClasses() {
        return releaseClasses;
    }

    public void setReleaseClasses(List<ReleaseJavaClass> releaseClasses) {
        this.releaseClasses.clear();
        this.releaseClasses.addAll(releaseClasses);
    }

    public List<RevisionJavaClass> getRevisionClasses() {
        return revisionClasses;
    }

    public void setRevisionClasses(List<RevisionJavaClass> revisionClasses) {
        this.revisionClasses.clear();
        this.revisionClasses.addAll(revisionClasses);
    }

    public List<Issue> getIssues() {
        return issues;
    }

    public void setIssues(List<Issue> issues) {
        this.issues.clear();
        this.issues.addAll(issues);
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public double getMovingWindowPercentage() {
        return movingWindowPercentage;
    }

    public void setMovingWindowPercentage(double movingWindowPercentage) {
        this.movingWindowPercentage = movingWindowPercentage;
    }

    public Proportion getProportion() {
        return proportion;
    }

    public void setProportion(Proportion proportion) {
        this.proportion = proportion;
    }

    public boolean isFromStart() {
        return fromStart;
    }

    public void setFromStart(boolean fromStart) {
        this.fromStart = fromStart;
    }
}
