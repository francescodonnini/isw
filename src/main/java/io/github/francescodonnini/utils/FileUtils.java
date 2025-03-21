package io.github.francescodonnini.utils;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileUtils {
    private static final Logger logger = Logger.getLogger(FileUtils.class.getName());

    private FileUtils() {}

    public static void createFileIfNotExists(String path) {
        var file = new File(path);
        var message = "directory %s already exists".formatted(file.getParentFile());
        if (file.getParentFile().mkdirs()) {
            message = "directory %s has been created.".formatted(file.getParentFile());
        }
        logger.log(Level.INFO, message);
    }
}
