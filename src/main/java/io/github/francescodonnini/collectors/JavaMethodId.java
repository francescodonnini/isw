package io.github.francescodonnini.collectors;

import io.github.francescodonnini.model.JavaMethod;

import java.util.Objects;

public record JavaMethodId(long trackingId, String className, String signature) {
    public static JavaMethodId of(JavaMethod m) {
        var clazz = m.getJavaClass();
        if (clazz == null) {
            throw new IllegalArgumentException("Java class is null");
        }
        var name = clazz.getName();
        if (clazz.isTopLevel()) {
            name = "P";
        }
        return new JavaMethodId(
                m.getJavaClass().getTrackingId(),
                name,
                m.getSignature()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        JavaMethodId that = (JavaMethodId) o;
        return trackingId == that.trackingId && Objects.equals(className, that.className) && Objects.equals(signature, that.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trackingId, className, signature);
    }
}
