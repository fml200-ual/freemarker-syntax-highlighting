package dev.freemarker.highlight.lexer;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static dev.freemarker.highlight.lexer.FreemarkerTokenTypes.*;

/**
 * Hand-written lexer for FreeMarker templates.
 *
 * <p>FreeMarker is a host language: HTML-ish template text surrounds FTL constructs. Two syntaxes
 * exist for the same constructs, and both are recognised:
 * <pre>
 *   angle brackets:   &lt;#if x&gt; ... &lt;/#if&gt;   &lt;@macro /&gt;   ${expr}   #{expr}   &lt;#-- c --&gt;
 *   square brackets:  [#if x] ... [/#if]   [@macro /]   [=expr]              [#-- c --]
 * </pre>
 * The interpolation form ({@code ${}}, {@code [=]}) and the tag form are independent in FreeMarker,
 * so the lexer tracks which opening it saw and closes on the matching terminator.
 *
 * <p>States:
 * <pre>
 *   S_TEXT              template text and markup
 *   S_TAG_NAME          after "&lt;" / "&lt;/", before an HTML element name
 *   S_TAG_BODY          inside an HTML tag, among attributes
 *   S_TAG_VALUE_*       inside an attribute value (may host FTL constructs)
 *   S_FTL_PENDING       at "&lt;#", "&lt;@", "${", "[#", "[=" ... decides the construct
 *   S_FTL_BODY          inside a directive/macro/interpolation, until "&gt;", "]" or "}"
 * </pre>
 */
public final class FreemarkerLexer extends LexerBase {

    // ------------------------------------------------------------------ states

    private static final int S_TEXT = 0;
    private static final int S_TAG_NAME = 1;
    private static final int S_TAG_BODY = 2;
    private static final int S_TAG_VALUE_START = 3;
    private static final int S_TAG_VALUE_DQ = 4;
    private static final int S_TAG_VALUE_DQ_CONT = 5;
    private static final int S_TAG_VALUE_SQ = 6;
    private static final int S_TAG_VALUE_SQ_CONT = 7;
    private static final int S_TAG_VALUE_UNQUOTED = 8;
    private static final int S_FTL_PENDING = 9;
    private static final int S_FTL_BODY = 10;

    private static final int STATE_BITS = 4;
    private static final int STATE_MASK = (1 << STATE_BITS) - 1;
    private static final int RETURN_SHIFT = STATE_BITS;
    private static final int TERMINATOR_SHIFT = 8;
    private static final int NAME_SHIFT = 10;
    private static final int IS_TAG_BIT = 12;

    private static final int TERM_GT = 0;
    private static final int TERM_BRACE = 1;
    private static final int TERM_BRACKET = 2;

    private static final int NAME_NONE = 0;
    private static final int NAME_DIRECTIVE = 1;
    private static final int NAME_MACRO = 2;

    private static final Set<String> KEYWORDS = Set.of("as", "in", "using", "true", "false");

    // ------------------------------------------------------------------ fields

    private CharSequence myBuffer = "";
    private int myBufferEnd;
    private int myTokenStart;
    private int myTokenEnd;
    private IElementType myTokenType;

    private int myState = S_TEXT;
    private int myFtlReturnState = S_TEXT;
    private int myTerminator = TERM_GT;
    private int myNameState = NAME_NONE;
    private boolean myFtlIsTag;

    // --------------------------------------------------------------- lexer API

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        myBuffer = buffer;
        myBufferEnd = endOffset;
        myTokenStart = startOffset;
        myTokenEnd = startOffset;
        myTokenType = null;

        myState = initialState & STATE_MASK;
        myFtlReturnState = (initialState >> RETURN_SHIFT) & STATE_MASK;
        myTerminator = (initialState >> TERMINATOR_SHIFT) & 0x3;
        myNameState = (initialState >> NAME_SHIFT) & 0x3;
        myFtlIsTag = (initialState & (1 << IS_TAG_BIT)) != 0;

        advance();
    }

    @Override
    public int getState() {
        return myState
                | (myFtlReturnState << RETURN_SHIFT)
                | (myTerminator << TERMINATOR_SHIFT)
                | (myNameState << NAME_SHIFT)
                | (myFtlIsTag ? 1 << IS_TAG_BIT : 0);
    }

    @Override
    public IElementType getTokenType() {
        return myTokenType;
    }

    @Override
    public int getTokenStart() {
        return myTokenStart;
    }

    @Override
    public int getTokenEnd() {
        return myTokenEnd;
    }

    @Override
    public @NotNull CharSequence getBufferSequence() {
        return myBuffer;
    }

    @Override
    public int getBufferEnd() {
        return myBufferEnd;
    }

    @Override
    public void advance() {
        myTokenStart = myTokenEnd;
        myTokenType = null;

        for (int guard = 0; myTokenType == null; guard++) {
            if (myTokenStart >= myBufferEnd) {
                myTokenEnd = myBufferEnd;
                return;
            }
            if (guard > 8) {
                myTokenEnd = myTokenStart + 1;
                myTokenType = TokenType.BAD_CHARACTER;
                return;
            }

            int stateBefore = myState;
            dispatch();

            if (myTokenType == null && myState == stateBefore) {
                myTokenEnd = myTokenStart + 1;
                myTokenType = TokenType.BAD_CHARACTER;
            }
        }

        if (myTokenEnd <= myTokenStart) {
            myTokenEnd = myTokenStart + 1;
        }
    }

    private void dispatch() {
        switch (myState) {
            case S_TEXT -> lexText();
            case S_TAG_NAME -> lexTagName();
            case S_TAG_BODY -> lexTagBody();
            case S_TAG_VALUE_START -> lexTagValueStart();
            case S_TAG_VALUE_DQ -> scanTagValue('"', myTokenStart + 1, S_TAG_VALUE_DQ_CONT);
            case S_TAG_VALUE_DQ_CONT -> scanTagValue('"', myTokenStart, S_TAG_VALUE_DQ_CONT);
            case S_TAG_VALUE_SQ -> scanTagValue('\'', myTokenStart + 1, S_TAG_VALUE_SQ_CONT);
            case S_TAG_VALUE_SQ_CONT -> scanTagValue('\'', myTokenStart, S_TAG_VALUE_SQ_CONT);
            case S_TAG_VALUE_UNQUOTED -> lexTagValueUnquoted();
            case S_FTL_PENDING -> lexFtlPending();
            case S_FTL_BODY -> lexFtlBody();
            default -> {
                myTokenEnd = myTokenStart + 1;
                myTokenType = TokenType.BAD_CHARACTER;
                myState = S_TEXT;
            }
        }
    }

    // -------------------------------------------------------------------- text

    private void lexText() {
        int i = myTokenStart;
        char c = at(i);

        if (isWhitespace(c)) {
            emitWhitespace();
            return;
        }

        if (c == '<') {
            if (startsWith(i, "<#--")) {
                myTokenEnd = indexOrEnd(i, "-->", 4);
                myTokenType = FTL_COMMENT;
                return;
            }
            if (startsWith(i, "<#") || startsWith(i, "</#") || startsWith(i, "<@") || startsWith(i, "</@")) {
                myFtlReturnState = S_TEXT;
                myState = S_FTL_PENDING;
                return;
            }
            if (startsWith(i, "<!--")) {
                myTokenEnd = indexOrEnd(i, "-->", 4);
                myTokenType = HTML_COMMENT;
                return;
            }
            if (startsWith(i, "<!")) {
                myTokenEnd = indexOrEnd(i, ">", 2);
                myTokenType = HTML_DECLARATION;
                return;
            }
            if (startsWith(i, "</")) {
                if (i + 2 < myBufferEnd && Character.isLetter(at(i + 2))) {
                    myTokenEnd = i + 2;
                    myTokenType = HTML_TAG_START;
                    myState = S_TAG_NAME;
                    myFtlIsTag = false;
                    return;
                }
            } else if (i + 1 < myBufferEnd && Character.isLetter(at(i + 1))) {
                myTokenEnd = i + 1;
                myTokenType = HTML_TAG_START;
                myState = S_TAG_NAME;
                myFtlIsTag = false;
                return;
            }
        }

        if (c == '[') {
            if (startsWith(i, "[#--")) {
                myTokenEnd = indexOrEnd(i, "--]", 4);
                myTokenType = FTL_COMMENT;
                return;
            }
            if (startsWith(i, "[#") || startsWith(i, "[/#") || startsWith(i, "[@") || startsWith(i, "[/@")
                    || startsWith(i, "[=")) {
                myFtlReturnState = S_TEXT;
                myState = S_FTL_PENDING;
                return;
            }
        }

        if ((c == '$' || c == '#') && i + 1 < myBufferEnd && at(i + 1) == '{') {
            myFtlReturnState = S_TEXT;
            myState = S_FTL_PENDING;
            return;
        }

        if (c == '&' && isEntityStart(i)) {
            myTokenEnd = entityEnd(i);
            myTokenType = HTML_ENTITY;
            return;
        }

        int j = i;
        while (j < myBufferEnd) {
            char ch = at(j);
            if (ch == '<' || ch == '[' || ch == '&') {
                break;
            }
            if ((ch == '$' || ch == '#') && j + 1 < myBufferEnd && at(j + 1) == '{') {
                break;
            }
            j++;
        }
        if (j == i) {
            j = i + 1;
        }
        myTokenEnd = j;
        myTokenType = HTML_TEXT;
    }

    // ------------------------------------------------------------ FTL: opening

    private void lexFtlPending() {
        int i = myTokenStart;

        if (startsWith(i, "<#--")) {
            myTokenEnd = indexOrEnd(i, "-->", 4);
            myTokenType = FTL_COMMENT;
            myState = myFtlReturnState;
            return;
        }
        if (startsWith(i, "[#--")) {
            myTokenEnd = indexOrEnd(i, "--]", 4);
            myTokenType = FTL_COMMENT;
            myState = myFtlReturnState;
            return;
        }

        if (startsWith(i, "</#")) {
            openTag(i + 3, FTL_CLOSE_TAG_START, TERM_GT, NAME_DIRECTIVE);
            return;
        }
        if (startsWith(i, "</@")) {
            openTag(i + 3, FTL_MACRO_CLOSE_START, TERM_GT, NAME_MACRO);
            return;
        }
        if (startsWith(i, "<#")) {
            openTag(i + 2, FTL_TAG_START, TERM_GT, NAME_DIRECTIVE);
            return;
        }
        if (startsWith(i, "<@")) {
            openTag(i + 2, FTL_MACRO_START, TERM_GT, NAME_MACRO);
            return;
        }

        if (startsWith(i, "[/#")) {
            openTag(i + 3, FTL_CLOSE_TAG_START, TERM_BRACKET, NAME_DIRECTIVE);
            return;
        }
        if (startsWith(i, "[/@")) {
            openTag(i + 3, FTL_MACRO_CLOSE_START, TERM_BRACKET, NAME_MACRO);
            return;
        }
        if (startsWith(i, "[#")) {
            char after = i + 2 < myBufferEnd ? at(i + 2) : ' ';
            if (isIdentifierStart(after)) {
                openTag(i + 2, FTL_TAG_START, TERM_BRACKET, NAME_DIRECTIVE);
            } else {
                openInterpolation(i + 2, TERM_BRACKET);
            }
            return;
        }
        if (startsWith(i, "[@")) {
            openTag(i + 2, FTL_MACRO_START, TERM_BRACKET, NAME_MACRO);
            return;
        }
        if (startsWith(i, "[=")) {
            openInterpolation(i + 2, TERM_BRACKET);
            return;
        }
        if (startsWith(i, "${") || startsWith(i, "#{")) {
            openInterpolation(i + 2, TERM_BRACE);
            return;
        }

        myTokenEnd = i + 1;
        myTokenType = HTML_TEXT;
        myState = myFtlReturnState;
    }

    private void openTag(int end, IElementType type, int terminator, int nameState) {
        myTokenEnd = end;
        myTokenType = type;
        myTerminator = terminator;
        myNameState = nameState;
        myFtlIsTag = true;
        myState = S_FTL_BODY;
    }

    private void openInterpolation(int end, int terminator) {
        myTokenEnd = end;
        myTokenType = FTL_INTERP_START;
        myTerminator = terminator;
        myNameState = NAME_NONE;
        myFtlIsTag = false;
        myState = S_FTL_BODY;
    }

    // --------------------------------------------------------------- FTL: body

    private void lexFtlBody() {
        int i = myTokenStart;
        char c = at(i);

        if (isWhitespace(c)) {
            emitWhitespace();
            return;
        }

        if (myNameState == NAME_DIRECTIVE && isIdentifierStart(c)) {
            int j = i + 1;
            while (j < myBufferEnd && isIdentifierPart(at(j))) {
                j++;
            }
            myTokenEnd = j;
            myTokenType = FTL_DIRECTIVE_NAME;
            myNameState = NAME_NONE;
            return;
        }
        if (myNameState == NAME_MACRO && isIdentifierStart(c)) {
            int j = i + 1;
            while (j < myBufferEnd && isMacroNamePart(at(j))) {
                j++;
            }
            myTokenEnd = j;
            myTokenType = FTL_MACRO_NAME;
            myNameState = NAME_NONE;
            return;
        }

        // Terminators of the enclosing construct.
        if (myTerminator == TERM_GT && c == '>') {
            myTokenEnd = i + 1;
            myTokenType = FTL_TAG_CLOSE;
            myState = myFtlReturnState;
            return;
        }
        if (myTerminator == TERM_GT && c == '/' && i + 1 < myBufferEnd && at(i + 1) == '>') {
            myTokenEnd = i + 2;
            myTokenType = FTL_TAG_CLOSE;
            myState = myFtlReturnState;
            return;
        }
        if (myTerminator == TERM_BRACE && c == '}') {
            myTokenEnd = i + 1;
            myTokenType = FTL_INTERP_CLOSE;
            myState = myFtlReturnState;
            return;
        }
        if (myTerminator == TERM_BRACKET && c == ']') {
            myTokenEnd = i + 1;
            myTokenType = myFtlIsTag ? FTL_TAG_CLOSE : FTL_INTERP_CLOSE;
            myState = myFtlReturnState;
            return;
        }

        // Builtins: ?upper_case, ?size, ??, ?date
        if (c == '?') {
            if (i + 1 < myBufferEnd && at(i + 1) == '?') {
                myTokenEnd = i + 2;
                myTokenType = FTL_BUILTIN;
                return;
            }
            if (i + 1 < myBufferEnd && isIdentifierPart(at(i + 1))) {
                int j = i + 2;
                while (j < myBufferEnd && isIdentifierPart(at(j))) {
                    j++;
                }
                myTokenEnd = j;
                myTokenType = FTL_BUILTIN;
                return;
            }
            myTokenEnd = i + 1;
            myTokenType = FTL_OPERATOR;
            return;
        }

        // Raw-string prefix: r"..." / f"..."
        if ((c == 'r' || c == 'f') && i + 1 < myBufferEnd
                && (at(i + 1) == '"' || at(i + 1) == '\'')) {
            myTokenEnd = stringEnd(i + 1, at(i + 1));
            myTokenType = FTL_STRING;
            return;
        }
        if (c == '"' || c == '\'') {
            myTokenEnd = stringEnd(i, c);
            myTokenType = FTL_STRING;
            return;
        }
        if (Character.isDigit(c) || (c == '.' && i + 1 < myBufferEnd && Character.isDigit(at(i + 1)))) {
            myTokenEnd = numberEnd(i);
            myTokenType = FTL_NUMBER;
            return;
        }
        if (isIdentifierStart(c)) {
            int j = i + 1;
            while (j < myBufferEnd && isIdentifierPart(at(j))) {
                j++;
            }
            String word = myBuffer.subSequence(i, j).toString();
            myTokenEnd = j;
            myTokenType = KEYWORDS.contains(word) ? FTL_KEYWORD : FTL_IDENTIFIER;
            return;
        }
        if (c == '.' && i + 1 < myBufferEnd && at(i + 1) == '.') {
            myTokenEnd = i + 2;
            myTokenType = FTL_OPERATOR;
            return;
        }
        if ("+-*/%=!<>&|^".indexOf(c) >= 0) {
            int j = i + 1;
            while (j < myBufferEnd && "+-*/%=!<>&|^".indexOf(at(j)) >= 0) {
                j++;
            }
            myTokenEnd = j;
            myTokenType = FTL_OPERATOR;
            return;
        }
        if ("(){}[],:.".indexOf(c) >= 0) {
            myTokenEnd = i + 1;
            myTokenType = FTL_PUNCTUATION;
            return;
        }

        myTokenEnd = i + 1;
        myTokenType = TokenType.BAD_CHARACTER;
    }

    // ------------------------------------------------------------------ markup

    private void lexTagName() {
        int i = myTokenStart;
        char c = at(i);

        if (isWhitespace(c)) {
            emitWhitespace();
            return;
        }
        if (c == '>') {
            myTokenEnd = i + 1;
            myTokenType = HTML_TAG_END;
            myState = S_TEXT;
            return;
        }
        if (c == '/' && i + 1 < myBufferEnd && at(i + 1) == '>') {
            myTokenEnd = i + 2;
            myTokenType = HTML_TAG_END;
            myState = S_TEXT;
            return;
        }
        if (isNameChar(c)) {
            int j = i + 1;
            while (j < myBufferEnd && isNameChar(at(j))) {
                j++;
            }
            myTokenEnd = j;
            myTokenType = HTML_TAG_NAME;
            myState = S_TAG_BODY;
            return;
        }

        myTokenEnd = i + 1;
        myTokenType = HTML_TEXT;
        myState = S_TAG_BODY;
    }

    private void lexTagBody() {
        int i = myTokenStart;
        char c = at(i);

        if (isWhitespace(c)) {
            emitWhitespace();
            return;
        }
        if (c == '>') {
            myTokenEnd = i + 1;
            myTokenType = HTML_TAG_END;
            myState = S_TEXT;
            return;
        }
        if (c == '/' && i + 1 < myBufferEnd && at(i + 1) == '>') {
            myTokenEnd = i + 2;
            myTokenType = HTML_TAG_END;
            myState = S_TEXT;
            return;
        }
        if (c == '=') {
            myTokenEnd = i + 1;
            myTokenType = HTML_ATTRIBUTE_ASSIGN;
            myState = S_TAG_VALUE_START;
            return;
        }
        if (c == '"') {
            myState = S_TAG_VALUE_DQ;
            scanTagValue('"', i + 1, S_TAG_VALUE_DQ_CONT);
            return;
        }
        if (c == '\'') {
            myState = S_TAG_VALUE_SQ;
            scanTagValue('\'', i + 1, S_TAG_VALUE_SQ_CONT);
            return;
        }
        if (ftlStartAt(i)) {
            myFtlReturnState = S_TAG_BODY;
            myState = S_FTL_PENDING;
            return;
        }
        if (isAttributeNameChar(c)) {
            int j = i + 1;
            while (j < myBufferEnd && isAttributeNameChar(at(j))) {
                j++;
            }
            myTokenEnd = j;
            myTokenType = HTML_ATTRIBUTE_NAME;
            return;
        }

        myTokenEnd = i + 1;
        myTokenType = HTML_TEXT;
    }

    private void lexTagValueStart() {
        int i = myTokenStart;
        char c = at(i);

        if (isWhitespace(c)) {
            emitWhitespace();
            return;
        }
        if (c == '"') {
            myState = S_TAG_VALUE_DQ;
            scanTagValue('"', i + 1, S_TAG_VALUE_DQ_CONT);
            return;
        }
        if (c == '\'') {
            myState = S_TAG_VALUE_SQ;
            scanTagValue('\'', i + 1, S_TAG_VALUE_SQ_CONT);
            return;
        }
        if (c == '>') {
            myTokenEnd = i + 1;
            myTokenType = HTML_TAG_END;
            myState = S_TEXT;
            return;
        }

        myState = S_TAG_VALUE_UNQUOTED;
    }

    private void scanTagValue(char quote, int from, int continuationState) {
        int i = from;

        while (i < myBufferEnd) {
            char c = at(i);

            if (c == quote) {
                myTokenEnd = i + 1;
                myTokenType = HTML_ATTRIBUTE_VALUE;
                myState = S_TAG_BODY;
                return;
            }
            if (ftlStartAt(i)) {
                if (i > myTokenStart) {
                    myTokenEnd = i;
                    myTokenType = HTML_ATTRIBUTE_VALUE;
                }
                myFtlReturnState = continuationState;
                myState = S_FTL_PENDING;
                return;
            }
            i++;
        }

        myTokenEnd = myBufferEnd;
        myTokenType = HTML_ATTRIBUTE_VALUE;
        myState = S_TAG_BODY;
    }

    private void lexTagValueUnquoted() {
        int i = myTokenStart;

        while (i < myBufferEnd) {
            char c = at(i);
            if (isWhitespace(c) || c == '>') {
                break;
            }
            if (c == '/' && i + 1 < myBufferEnd && at(i + 1) == '>') {
                break;
            }
            if (ftlStartAt(i)) {
                break;
            }
            i++;
        }

        if (i > myTokenStart) {
            myTokenEnd = i;
            myTokenType = HTML_ATTRIBUTE_VALUE;

            if (ftlStartAt(i)) {
                myFtlReturnState = S_TAG_BODY;
                myState = S_FTL_PENDING;
                return;
            }
            myState = S_TAG_BODY;
            return;
        }

        myState = S_TAG_BODY;
    }

    // ----------------------------------------------------------------- helpers

    private char at(int index) {
        return myBuffer.charAt(index);
    }

    private boolean startsWith(int offset, String text) {
        if (offset + text.length() > myBufferEnd) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            if (myBuffer.charAt(offset + i) != text.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    private boolean ftlStartAt(int index) {
        return startsWith(index, "${")
                || startsWith(index, "#{")
                || startsWith(index, "[=")
                || startsWith(index, "[#")
                || startsWith(index, "[@")
                || startsWith(index, "<#")
                || startsWith(index, "<@");
    }

    private int indexOrEnd(int from, String terminator, int searchFromOffset) {
        int length = terminator.length();
        for (int i = from + searchFromOffset; i + length <= myBufferEnd; i++) {
            boolean match = true;
            for (int k = 0; k < length; k++) {
                if (myBuffer.charAt(i + k) != terminator.charAt(k)) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return i + length;
            }
        }
        return myBufferEnd;
    }

    private int stringEnd(int start, char quote) {
        int i = start + 1;
        while (i < myBufferEnd) {
            char c = at(i);
            if (c == '\\') {
                i += 2;
                continue;
            }
            if (c == quote) {
                return i + 1;
            }
            if (c == '\n' || c == '\r') {
                return i;
            }
            i++;
        }
        return myBufferEnd;
    }

    private int numberEnd(int start) {
        int i = start;
        boolean seenDot = false;
        while (i < myBufferEnd) {
            char c = at(i);
            if (Character.isDigit(c)) {
                i++;
                continue;
            }
            if (c == '.' && !seenDot && i + 1 < myBufferEnd && Character.isDigit(at(i + 1))) {
                seenDot = true;
                i++;
                continue;
            }
            break;
        }
        return i;
    }

    private boolean isEntityStart(int index) {
        if (index + 1 >= myBufferEnd) {
            return false;
        }
        char next = at(index + 1);
        return Character.isLetter(next) || next == '#';
    }

    private int entityEnd(int start) {
        int limit = Math.min(start + 12, myBufferEnd);
        for (int i = start + 1; i < limit; i++) {
            char c = at(i);
            if (c == ';') {
                return i + 1;
            }
            if (!Character.isLetterOrDigit(c) && c != '#') {
                return start + 1;
            }
        }
        return start + 1;
    }

    private void emitWhitespace() {
        int i = myTokenStart + 1;
        while (i < myBufferEnd && isWhitespace(at(i))) {
            i++;
        }
        myTokenEnd = i;
        myTokenType = TokenType.WHITE_SPACE;
    }

    private static boolean isWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\f';
    }

    private static boolean isIdentifierStart(char c) {
        return Character.isLetter(c) || c == '_';
    }

    private static boolean isIdentifierPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '-';
    }

    private static boolean isMacroNamePart(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '.' || c == ':';
    }

    private static boolean isNameChar(char c) {
        return Character.isLetterOrDigit(c) || c == '-' || c == '_' || c == ':' || c == '.';
    }

    private static boolean isAttributeNameChar(char c) {
        return Character.isLetterOrDigit(c) || c == '-' || c == '_' || c == ':' || c == '.'
                || c == '@' || c == '[';
    }
}
