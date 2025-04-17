package io.github.francescodonnini.model;

public record LineRange(long start, long end) {
    public boolean intersect(LineRange other) {
        return start >= other.start && other.end >= start
                || start <= other.start && end >= other.start
                || start <= other.start && end >= other.end
                || start >= other.start && end <= other.end;
    }
}
