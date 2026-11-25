package dev.freemarker.highlight.lexer;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import dev.freemarker.highlight.FreemarkerLanguage;

/**
 * Token types produced by {@link FreemarkerLexer}.
 *
 * <p>Three families:
 * <ul>
 *   <li>{@code FTL_*} — FreeMarker constructs: directives, macro calls, interpolations, comments,
 *       and the expression tokens inside them.</li>
 *   <li>{@code HTML_*} — the markup surrounding the template.</li>
 * </ul>
 */
public final class FreemarkerTokenTypes {

    private FreemarkerTokenTypes() {
    }

    private static IElementType token(String name) {
        return new IElementType(name, FreemarkerLanguage.INSTANCE);
    }

    // ------------------------------------------------------------- FreeMarker

    public static final IElementType FTL_TAG_START = token("FTL_TAG_START");
    public static final IElementType FTL_CLOSE_TAG_START = token("FTL_CLOSE_TAG_START");
    public static final IElementType FTL_MACRO_START = token("FTL_MACRO_START");
    public static final IElementType FTL_MACRO_CLOSE_START = token("FTL_MACRO_CLOSE_START");
    public static final IElementType FTL_INTERP_START = token("FTL_INTERP_START");
    public static final IElementType FTL_TAG_CLOSE = token("FTL_TAG_CLOSE");
    public static final IElementType FTL_INTERP_CLOSE = token("FTL_INTERP_CLOSE");

    public static final IElementType FTL_DIRECTIVE_NAME = token("FTL_DIRECTIVE_NAME");
    public static final IElementType FTL_MACRO_NAME = token("FTL_MACRO_NAME");
    public static final IElementType FTL_KEYWORD = token("FTL_KEYWORD");
    public static final IElementType FTL_IDENTIFIER = token("FTL_IDENTIFIER");
    public static final IElementType FTL_BUILTIN = token("FTL_BUILTIN");
    public static final IElementType FTL_STRING = token("FTL_STRING");
    public static final IElementType FTL_NUMBER = token("FTL_NUMBER");
    public static final IElementType FTL_OPERATOR = token("FTL_OPERATOR");
    public static final IElementType FTL_PUNCTUATION = token("FTL_PUNCTUATION");
    public static final IElementType FTL_COMMENT = token("FTL_COMMENT");

    // ---------------------------------------------------------------- markup

    public static final IElementType HTML_TEXT = token("HTML_TEXT");
    public static final IElementType HTML_TAG_START = token("HTML_TAG_START");
    public static final IElementType HTML_TAG_END = token("HTML_TAG_END");
    public static final IElementType HTML_TAG_NAME = token("HTML_TAG_NAME");
    public static final IElementType HTML_ATTRIBUTE_NAME = token("HTML_ATTRIBUTE_NAME");
    public static final IElementType HTML_ATTRIBUTE_ASSIGN = token("HTML_ATTRIBUTE_ASSIGN");
    public static final IElementType HTML_ATTRIBUTE_VALUE = token("HTML_ATTRIBUTE_VALUE");
    public static final IElementType HTML_COMMENT = token("HTML_COMMENT");
    public static final IElementType HTML_DECLARATION = token("HTML_DECLARATION");
    public static final IElementType HTML_ENTITY = token("HTML_ENTITY");

    // ---------------------------------------------------------------- groups

    public static final TokenSet COMMENTS = TokenSet.create(FTL_COMMENT, HTML_COMMENT);

    public static final TokenSet STRINGS = TokenSet.create(FTL_STRING, HTML_ATTRIBUTE_VALUE);
}
