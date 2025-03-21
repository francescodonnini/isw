package io.github.francescodonnini.model;

import java.time.LocalDate;

public record Release(
        int number,
        String id,
        String name,
        LocalDate releaseDate
) {
    public boolean isBefore(Release release) {
        return releaseDate.isBefore(release.releaseDate);
    }

    public boolean isAfter(Release release) {
        return releaseDate.isAfter(release.releaseDate);
    }
}

