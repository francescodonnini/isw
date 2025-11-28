package io.github.francescodonnini.pipeline;

import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.JavaClass;
import io.github.francescodonnini.model.JavaMethod;
import io.github.francescodonnini.model.Release;

import java.util.ArrayList;
import java.util.List;

public class ProjectInfo {
    private final List<Release> projectReleases = new ArrayList<>();
    private final List<JavaClass> classes = new ArrayList<>();
    private final List<JavaMethod> methods = new ArrayList<>();
    private final List<Issue> issues = new ArrayList<>();

    public List<Release> getProjectReleases() {
        return projectReleases;
    }

    public void setProjectReleases(List<Release> projectReleases) {
        this.projectReleases.clear();
        this.projectReleases.addAll(projectReleases);
    }

    public List<JavaClass> getClasses() {
        return classes;
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
