package io.github.francescodonnini.data;

import java.nio.file.Path;
import java.time.LocalDateTime;

public record ParseContext(String commit, Path parent, Path path, LocalDateTime time) {
    public Path getAbsolutePath() {
        return parent.resolve(path);
    }
}
