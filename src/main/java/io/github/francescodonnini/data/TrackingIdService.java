package io.github.francescodonnini.data;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class TrackingIdService {
    private final AtomicLong id = new AtomicLong(0);
    private final Map<Path, Long> map = new ConcurrentHashMap<>();

    public long generateId(Path path) {
        return map.computeIfAbsent(path, unused -> id.getAndIncrement());
    }

    public long updateId(Path oldPath, Path newPath) {
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
