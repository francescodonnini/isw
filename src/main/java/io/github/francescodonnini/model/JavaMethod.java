package io.github.francescodonnini.model;

import java.nio.file.Path;
import java.util.Objects;

public class JavaMethod {
    private boolean buggy;
    private final String signature;
    private final JavaClass javaClass;
    private final String content;

    public JavaMethod(boolean buggy, JavaClass javaClass, String signature, String content) {
        this.buggy = buggy;
        this.signature = signature;
        this.javaClass = javaClass;
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof JavaMethod that)) return false;
        return buggy == that.buggy && Objects.equals(signature, that.signature) && Objects.equals(javaClass, that.javaClass) && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(buggy, signature, javaClass, content);
    }

    @Override
    public String toString() {
        return "(%s, %s)".formatted(javaClass.getRelease().id(), signature);
    }

    public boolean isBuggy() {
        return buggy;
    }

    public String getContent() {
        return content;
    }

    public JavaClass getJavaClass() {
        return javaClass;
    }

    public String getSignature() {
        return signature;
    }

    public Release getRelease() {
        return javaClass.getRelease();
    }

    public Path getPath() {
        return javaClass.getPath();
    }
}
