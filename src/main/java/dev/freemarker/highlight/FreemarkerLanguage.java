package dev.freemarker.highlight;

import com.intellij.lang.Language;

/**
 * The single language instance for FreeMarker templates.
 *
 * <p>Only a lexer and a syntax highlighter are registered in this version: there is no parser and
 * therefore no PSI beyond a flat file node.
 */
public final class FreemarkerLanguage extends Language {

    public static final FreemarkerLanguage INSTANCE = new FreemarkerLanguage();

    /** Distinct from JetBrains' own FreeMarker language id, so both can coexist. */
    public static final String ID = "FREEMARKER_HIGHLIGHT";

    private FreemarkerLanguage() {
        super(ID);
    }

    @Override
    public boolean isCaseSensitive() {
        return true;
    }
}
