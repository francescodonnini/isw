package io.github.francescodonnini.model;

import java.nio.file.Path;

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
