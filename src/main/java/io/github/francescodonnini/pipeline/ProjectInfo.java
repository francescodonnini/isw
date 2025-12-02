package io.github.francescodonnini.pipeline;

import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.JavaClass;
import io.github.francescodonnini.model.JavaMethod;
import io.github.francescodonnini.model.Release;

import java.util.ArrayList;
import java.util.List;

public class ProjectInfo {
    private final List<Release> allReleases = new ArrayList<>();
    private int projectReleasesEnd;
    private final List<JavaClass> classes = new ArrayList<>();
    private final List<JavaMethod> methods = new ArrayList<>();
    private final List<Issue> issues = new ArrayList<>();

    public int getProjectReleasesEnd() {
        return projectReleasesEnd;
    }

    public void setProjectReleasesEnd(int projectReleasesEnd) {
        this.projectReleasesEnd = projectReleasesEnd;
    }

    public List<Release> getAllReleases() {
        return allReleases;
    }

    public void setAllReleases(List<Release> allReleases) {
        this.allReleases.clear();
        this.allReleases.addAll(allReleases);
    }

    public List<Release> getProjectReleases() {
        return allReleases.subList(0, projectReleasesEnd);
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
}
