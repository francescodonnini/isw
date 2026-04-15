package io.github.francescodonnini.data.pmd;

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.renderers.CSVRenderer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PMDFactory {
    private static final String PMD_RULESET = "io/github/francescodonnini/data/sonar-ruleset.xml";

    private PMDFactory() {}

    public static PMDConfiguration create() {
        var config = new PMDConfiguration();
        config.setDefaultLanguageVersion(JavaLanguage.LANGUAGE_VERSION);
        config.addRuleSet(PMD_RULESET);
        return config;
    }

    public static PmdAnalysis create(Path reportPath, PMDConfiguration config) throws IOException {
        var renderer = new CSVRenderer();
        renderer.setWriter(Files.newBufferedWriter(reportPath));
        config.setReportFile(reportPath);
        var analysis = PmdAnalysis.create(config);
        analysis.addRenderer(renderer);
        return analysis;
    }
}
