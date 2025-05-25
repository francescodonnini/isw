package io.github.francescodonnini;

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.renderers.CSVRenderer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PMDAnalysis {
    private static final Logger logger = Logger.getLogger(PMDAnalysis.class.getName());
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            logger.log(Level.INFO, "Usage: java -jar pmd-analysis.jar <path to file> <reports path>");
            System.exit(1);
        }
        var fileName = getFileName(args[0]);
        var reportsPath = Path.of(args[1]);
        try (var pmd = createPmdAnalysis(reportsPath.resolve(fileName + ".csv"))) {
            pmd.files().addFile(Path.of(args[0]));
            pmd.performAnalysis();
        }
    }

    private static String getFileName(String path) {
        return Path.of(path)
                .getFileName()
                .toString()
                .replace(".java", "");
    }

    private static PmdAnalysis createPmdAnalysis(Path path) throws IOException {
        var renderer = new CSVRenderer();
        renderer.setWriter(Files.newBufferedWriter(path));
        var pmd = PmdAnalysis.create(getPmdConfig(path));
        pmd.addRenderer(renderer);
        return pmd;
    }

    private static PMDConfiguration getPmdConfig(Path path) {
        var config = new PMDConfiguration();
        config.setDefaultLanguageVersion(LanguageRegistry.PMD.getLanguageVersionById("java", "22"));
        config.setReportFile(path);
        config.addRuleSet("rulesets/java/quickstart.xml");
        return config;
    }

}
