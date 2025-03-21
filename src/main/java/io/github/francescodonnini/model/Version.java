package io.github.francescodonnini.model;

import java.time.LocalDate;

public record Version(
        boolean archived,
        String id,
        String name,
        boolean released,
        LocalDate releaseDate) {}
