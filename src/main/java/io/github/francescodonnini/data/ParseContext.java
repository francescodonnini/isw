package io.github.francescodonnini.data;

import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * Un record che contiene tutte le informazioni necessarie per effettuare il parsing di un file Java e costruire
 * uno o più oggetti JavaClass da esso.
 *
 * @param commit identificatore del commit che ha modificato il file
 * @param parent directory genitore che contiene il file o una sua directory
 * @param path percorso relativo del file rispetto alla directory genitore
 * @param time data e ora del commit
 */
public record ParseContext(long trackingId, String commit, Path parent, Path path, LocalDateTime time, String content) {
    public Path getAbsolutePath() {
        return parent.resolve(path);
    }
}
