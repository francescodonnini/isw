package io.github.francescodonnini.data;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TrackingIdService {
    private final AtomicLong id = new AtomicLong(0);
    private final Map<Path, Long> map = new ConcurrentHashMap<>();

    public long getId(Path path) {
        return map.computeIfAbsent(path, unused -> id.getAndIncrement());
    }

    public void updateId(Path oldPath, Path newPath) {
        var oldId = map.get(oldPath);
        var newId = map.get(newPath);
        if (oldId != null) {
            map.remove(oldPath);
            map.put(newPath, oldId);
        }
        if (newId != null) {
            return;
        }
        getId(newPath);
    }
}
