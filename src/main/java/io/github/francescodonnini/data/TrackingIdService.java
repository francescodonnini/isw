package io.github.francescodonnini.data;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class TrackingIdService {
    private long nextId = 0;
    private final Map<Path, Long> map = new HashMap<>();

    public long getTrackingId(Path path) {
        return map.computeIfAbsent(path, unused -> nextId++);
    }

    public long updateTrackingId(Path oldPath, Path newPath) {
        var id = map.remove(oldPath);
        if (id == null) {
            throw new IllegalArgumentException("file %s has not been tracked yet".formatted(oldPath));
        }
        if (map.replace(newPath, id) == null) {
            throw new IllegalArgumentException("file %s has not been tracked yet".formatted(newPath));
        }
        return id;
    }
}
