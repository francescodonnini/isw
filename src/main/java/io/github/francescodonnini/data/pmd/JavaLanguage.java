package io.github.francescodonnini.data.pmd;

import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.LanguageVersion;

public class JavaLanguage {
    private static final String JAVA_VERSION_ID = "22";
    public static final LanguageVersion LANGUAGE_VERSION;

    static {
        LANGUAGE_VERSION = LanguageRegistry.PMD.getLanguageVersionById("java", JAVA_VERSION_ID);
        if (LANGUAGE_VERSION == null) {
            throw new IllegalStateException("Language Java 22 not found");
        }
    }

    private JavaLanguage() {}
}
