package io.github.francescodonnini.model;

public record ClassID(long trackingId, String name) {
    public static ClassID of(RevisionJavaClass c) {
        return new ClassID(c.getTrackingId(), c.getName());
    }

    public static ClassID of(ReleaseJavaClass c) {
        return new ClassID(c.getTrackingId(), c.getName());
    }
}
