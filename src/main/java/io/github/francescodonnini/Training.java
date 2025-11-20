package io.github.francescodonnini;

import io.github.francescodonnini.config.IniSettings;
import io.github.francescodonnini.utils.FileUtils;
import io.github.francescodonnini.weka.Trainer;

import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Training {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.exit(-1);
        }
        var projectName = args[1].toUpperCase();
        // regex "<project name>-d+" ("%s-\\d+") è presente in tutti i commit che chiudono un ticket di JIRA
        var settings = new IniSettings(args[0]);
        var dataPath = Path.of(settings.getString("dataPath"), projectName);
        final var logger = Logger.getLogger(Training.class.getName());
        logger.log(Level.INFO, "project name: {0}", projectName);
        var trainer = new Trainer(dataPath.resolve("methods.arff"));
        trainer.train();
    }
}
