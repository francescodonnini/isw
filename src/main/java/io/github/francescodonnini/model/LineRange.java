package io.github.francescodonnini.model;

public record LineRange(int start, int end) {
     public int length() {
        return end - start;
    }
}
