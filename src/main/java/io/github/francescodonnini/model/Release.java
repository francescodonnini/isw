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
        if (!(o instanceof Release(String id, String name, LocalDate date, int order))) return false;
        return Objects.equals(this.id, id)
                && Objects.equals(this.name, name)
                && Objects.equals(this.releaseDate, date)
                && Objects.equals(this.order, order);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, releaseDate, order);
    }

    @Override
    public String toString() {
        return "(%d %s, %s, %s)".formatted(order, id, name, releaseDate);
    }

    public boolean isBefore(Release release) {
        return releaseDate.isBefore(release.releaseDate);
    }

    public boolean isAfter(Release release) {
        return releaseDate.isAfter(release.releaseDate);
    }
}

