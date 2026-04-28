package io.github.francescodonnini.collectors;

import io.github.francescodonnini.model.RevisionJavaClass;

import java.util.Objects;

public record ClassID(long trackingId, String className) {
    public static ClassID of(RevisionJavaClass c) {
        var name = c.getName();
        if (c.isTopLevel()) {
            name += "[P]";
        }
        return new ClassID(c.getTrackingId(), name);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ClassID that = (ClassID) o;
        return trackingId == that.trackingId && Objects.equals(className, that.className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trackingId, className);
    }
}
