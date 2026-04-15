package io.github.francescodonnini.data.pmd;

import net.sourceforge.pmd.cpd.CPDConfiguration;

public class CPDFactory {
    private CPDFactory() {}

    public static CPDConfiguration create() {
        var config = new CPDConfiguration();
        config.setMinimumTileSize(250);
        config.setDefaultLanguageVersion(JavaLanguage.LANGUAGE_VERSION);
        config.setIgnoreIdentifiers(false);
        config.setIgnoreLiterals(false);
        config.setIgnoreAnnotations(true);
        return config;
    }
}
