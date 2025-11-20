package io.github.francescodonnini.model;

import java.time.LocalDate;
import java.util.Objects;

public record Release(
        String id,
        String name,
        LocalDate releaseDate,
        int order
) {
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Release(String otherId, String otherName, LocalDate otherDate, int order))) return false;
        return Objects.equals(id, otherId)
                && Objects.equals(name, otherName)
                && Objects.equals(releaseDate, otherDate)
                && Objects.equals(order, order);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, releaseDate, order);
    }

    @Override
    public String toString() {
        return "(%s, %s, %s)".formatted(id, name, releaseDate);
    }

    public boolean isBefore(Release release) {
        return releaseDate.isBefore(release.releaseDate);
    }

    public boolean isAfter(Release release) {
        return releaseDate.isAfter(release.releaseDate);
    }
}

