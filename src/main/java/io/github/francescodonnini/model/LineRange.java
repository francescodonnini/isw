package io.github.francescodonnini.model;

public record LineRange(int start, int end) {
     public int length() {
        return end - start;
    }

    /**
     * A ------- B
     *      C ------- D
     * C >= A && B <= D
     *
     *       A --------- B
     * C --------D
     * D >= A && D <= B
     *
     * A -------------- B
     *     C ------ D
     * C >= A && D <= B
     *
     *       A ----- B
     * C ----------------D
     * A >= C && B <= D
     *
     * A ------- B
     *               C ------ D
     *           A ------ B
     * C ---- D
     * @param other
     * @return
     */

    public boolean intersects(LineRange other) {
        return !(end <= other.start || start >= other.end);
    }
}
