package dev.freemarker.highlight;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import dev.freemarker.highlight.lexer.FreemarkerLexer;
import dev.freemarker.highlight.lexer.FreemarkerTokenTypes;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

/**
 * Maps every token produced by {@link FreemarkerLexer} to a text attribute.
 *
 * <p>Every key has a fallback to a platform key that the default schemes always define. In
 * particular the markup tokens deliberately avoid {@code DefaultLanguageHighlighterColors.MARKUP_*}:
 * those are only defined by JetBrains' paid HTML plugins, so without them every tag would render as
 * plain text.
 */
public final class FreemarkerSyntaxHighlighter extends SyntaxHighlighterBase {

    // ------------------------------------------------------------- FreeMarker

    public static final TextAttributesKey FTL_DELIMITER = createTextAttributesKey(
            "FTL_DELIMITER", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey FTL_DIRECTIVE_NAME = createTextAttributesKey(
            "FTL_DIRECTIVE_NAME", DefaultLanguageHighlighterColors.METADATA);
    public static final TextAttributesKey FTL_MACRO_NAME = createTextAttributesKey(
            "FTL_MACRO_NAME", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
    public static final TextAttributesKey FTL_KEYWORD = createTextAttributesKey(
            "FTL_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey FTL_IDENTIFIER = createTextAttributesKey(
            "FTL_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey FTL_BUILTIN = createTextAttributesKey(
            "FTL_BUILTIN", DefaultLanguageHighlighterColors.FUNCTION_CALL);
    public static final TextAttributesKey FTL_STRING = createTextAttributesKey(
            "FTL_STRING", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey FTL_NUMBER = createTextAttributesKey(
            "FTL_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey FTL_OPERATOR = createTextAttributesKey(
            "FTL_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey FTL_PUNCTUATION = createTextAttributesKey(
            "FTL_PUNCTUATION", DefaultLanguageHighlighterColors.BRACES);
    public static final TextAttributesKey FTL_COMMENT = createTextAttributesKey(
            "FTL_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT);

    // ------------------------------------------------------------------ HTML

    public static final TextAttributesKey HTML_TEXT_KEY = createTextAttributesKey(
            "FTL_HTML_TEXT", HighlighterColors.TEXT);
    public static final TextAttributesKey HTML_TAG_NAME_KEY = createTextAttributesKey(
            "FTL_HTML_TAG_NAME", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
    public static final TextAttributesKey HTML_TAG_DELIMITER = createTextAttributesKey(
            "FTL_HTML_TAG_DELIMITER", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
    public static final TextAttributesKey HTML_ATTRIBUTE_NAME_KEY = createTextAttributesKey(
            "FTL_HTML_ATTRIBUTE_NAME", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    public static final TextAttributesKey HTML_ATTRIBUTE_VALUE_KEY = createTextAttributesKey(
            "FTL_HTML_ATTRIBUTE_VALUE", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey HTML_ATTRIBUTE_ASSIGN_KEY = createTextAttributesKey(
            "FTL_HTML_ATTRIBUTE_ASSIGN", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey HTML_COMMENT_KEY = createTextAttributesKey(
            "FTL_HTML_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    public static final TextAttributesKey HTML_DECLARATION_KEY = createTextAttributesKey(
            "FTL_HTML_DECLARATION", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
    public static final TextAttributesKey HTML_ENTITY_KEY = createTextAttributesKey(
            "FTL_HTML_ENTITY", DefaultLanguageHighlighterColors.NUMBER);

    private static final Map<IElementType, TextAttributesKey> ATTRIBUTES = new HashMap<>();

    static {
        ATTRIBUTES.put(FreemarkerTokenTypes.FTL_TAG_START, FTL_DELIMITER);
        ATTRIBUTES.put(FreemarkerTokenTypes.FTL_CLOSE_TAG_START, FTL_DELIMITER);
        ATTRIBUTES.put(FreemarkerTokenTypes.FTL_MACRO_START, FTL_DELIMITER);
        ATTRIBUTES.put(FreemarkerTokenTypes.FTL_MACRO_CLOSE_START, FTL_DELIMITER);
        ATTRIBUTES.put(FreemarkerTokenTypes.FTL_INTERP_START, FTL_DELIMITER);
        ATTRIBUTES.put(FreemarkerTokenTypes.FTL_TAG_CLOSE, FTL_DELIMITER);
        ATTRIBUTES.put(FreemarkerTokenTypes.FTL_INTERP_CLOSE, FTL_DELIMITER);

        ATTRIBUTES.put(FreemarkerTokenTypes.FTL_DIRECTIVE_NAME, FTL_DIRECTIVE_NAME);
        ATTRIBUTES.put(FreemarkerTokenTypes.FTL_MACRO_NAME, FTL_MACRO_NAME);
        ATTRIBUTES.put(FreemarkerTokenTypes.FTL_KEYWORD, FTL_KEYWORD);
        ATTRIBUTES.put(FreemarkerTokenTypes.FTL_IDENTIFIER, FTL_IDENTIFIER);
        ATTRIBUTES.put(FreemarkerTokenTypes.FTL_BUILTIN, FTL_BUILTIN);
        ATTRIBUTES.put(FreemarkerTokenTypes.FTL_STRING, FTL_STRING);
        ATTRIBUTES.put(FreemarkerTokenTypes.FTL_NUMBER, FTL_NUMBER);
        ATTRIBUTES.put(FreemarkerTokenTypes.FTL_OPERATOR, FTL_OPERATOR);
        ATTRIBUTES.put(FreemarkerTokenTypes.FTL_PUNCTUATION, FTL_PUNCTUATION);
        ATTRIBUTES.put(FreemarkerTokenTypes.FTL_COMMENT, FTL_COMMENT);

        ATTRIBUTES.put(FreemarkerTokenTypes.HTML_TEXT, HTML_TEXT_KEY);
        ATTRIBUTES.put(FreemarkerTokenTypes.HTML_TAG_START, HTML_TAG_DELIMITER);
        ATTRIBUTES.put(FreemarkerTokenTypes.HTML_TAG_END, HTML_TAG_DELIMITER);
        ATTRIBUTES.put(FreemarkerTokenTypes.HTML_TAG_NAME, HTML_TAG_NAME_KEY);
        ATTRIBUTES.put(FreemarkerTokenTypes.HTML_ATTRIBUTE_NAME, HTML_ATTRIBUTE_NAME_KEY);
        ATTRIBUTES.put(FreemarkerTokenTypes.HTML_ATTRIBUTE_ASSIGN, HTML_ATTRIBUTE_ASSIGN_KEY);
        ATTRIBUTES.put(FreemarkerTokenTypes.HTML_ATTRIBUTE_VALUE, HTML_ATTRIBUTE_VALUE_KEY);
        ATTRIBUTES.put(FreemarkerTokenTypes.HTML_COMMENT, HTML_COMMENT_KEY);
        ATTRIBUTES.put(FreemarkerTokenTypes.HTML_DECLARATION, HTML_DECLARATION_KEY);
        ATTRIBUTES.put(FreemarkerTokenTypes.HTML_ENTITY, HTML_ENTITY_KEY);
    }

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return new FreemarkerLexer();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        return pack(ATTRIBUTES.get(tokenType));
    }
}
