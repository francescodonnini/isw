package io.github.francescodonnini.model;

import java.nio.file.Path;

public class JavaMethod {
    private final String signature;
    private final JavaClass javaClass;
    private final String content;

    public JavaMethod(JavaClass javaClass, String signature, String content) {
        this.signature = signature;
        this.javaClass = javaClass;
        this.content = content;
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
