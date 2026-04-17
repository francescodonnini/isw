package io.github.francescodonnini.data.pmd;

import net.sourceforge.pmd.cpd.CPDConfiguration;

public class CPDFactory {
    private CPDFactory() {}

    public static CPDConfiguration create() {
        var config = new CPDConfiguration();
        config.setMinimumTileSize(100);
        config.setDefaultLanguageVersion(JavaLanguage.LANGUAGE_VERSION);
        return config;
    }
}
