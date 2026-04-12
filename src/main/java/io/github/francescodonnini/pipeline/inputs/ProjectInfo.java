package io.github.francescodonnini.pipeline.inputs;

import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.JavaClass;
import io.github.francescodonnini.model.JavaMethod;
import io.github.francescodonnini.model.Release;

import java.util.ArrayList;
import java.util.List;

public class ProjectInfo {
    private String project;
    private Proportion proportion;
    private double movingWindowPercentage;
    private final List<Release> releases = new ArrayList<>();
    private final List<JavaClass> classes = new ArrayList<>();
    private final List<JavaMethod> methods = new ArrayList<>();
    private final List<Issue> issues = new ArrayList<>();
    private boolean fromStart;

    public List<JavaClass> getClasses() {
        return classes;
    }

    public List<Release> getAllReleases() {
        return releases;
    }

    public void setAllReleases(List<Release> allReleases) {
        this.releases.clear();
        this.releases.addAll(allReleases);
    }

    public List<Release> getProjectReleases() {
        return releases;
    }

    public void setClasses(List<JavaClass> classes) {
        this.classes.clear();
        this.classes.addAll(classes);
    }

    public List<JavaMethod> getMethods() {
        return methods;
    }

    public void setMethods(List<JavaMethod> methods) {
        this.methods.clear();
        this.methods.addAll(methods);
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

    public ProjectInfo withFromStart(boolean b) {
        var copy = new ProjectInfo();
        copy.setFromStart(b);
        copy.setProject(getProject());
        copy.setProportion(getProportion());
        copy.setMovingWindowPercentage(getMovingWindowPercentage());
        copy.setIssues(getIssues());
        copy.setMethods(getMethods());
        copy.setAllReleases(getAllReleases());
        return copy;
    }
}
