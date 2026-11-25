package dev.freemarker.highlight;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import dev.freemarker.highlight.lexer.FreemarkerLexer;
import dev.freemarker.highlight.lexer.FreemarkerTokenTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Minimal parser definition: one flat {@code FTL_FILE} node over the whole token stream.
 *
 * <p>The editor needs a parser definition to build a PSI file; highlighting is driven by the
 * syntax highlighter and does not depend on the tree.
 */
public final class FreemarkerParserDefinition implements ParserDefinition {

    public static final IFileElementType FTL_FILE =
            new IFileElementType("FTL_FILE", FreemarkerLanguage.INSTANCE);

    @Override
    public @NotNull Lexer createLexer(Project project) {
        return new FreemarkerLexer();
    }

    @Override
    public @NotNull PsiParser createParser(Project project) {
        return (root, builder) -> {
            var marker = builder.mark();
            while (!builder.eof()) {
                builder.advanceLexer();
            }
            marker.done(root);
            return builder.getTreeBuilt();
        };
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return FTL_FILE;
    }

    @Override
    public @NotNull TokenSet getWhitespaceTokens() {
        return TokenSet.create(TokenType.WHITE_SPACE);
    }

    @Override
    public @NotNull TokenSet getCommentTokens() {
        return FreemarkerTokenTypes.COMMENTS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull TokenSet getStringLiteralElements() {
        return FreemarkerTokenTypes.STRINGS;
    }

    @Override
    public @NotNull PsiElement createElement(ASTNode node) {
        return new ASTWrapperPsiElement(node);
    }

    @Override
    public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new FtlPsiFile(viewProvider);
    }

    /** Flat PSI file: the whole file is one {@code FTL_FILE} node. */
    public static final class FtlPsiFile extends PsiFileBase {
        public FtlPsiFile(@NotNull FileViewProvider viewProvider) {
            super(viewProvider, FreemarkerLanguage.INSTANCE);
        }

        @Override
        public @NotNull FileType getFileType() {
            return FreemarkerFileType.INSTANCE;
        }

        @Override
        public String toString() {
            return "FreeMarker file";
        }
    }
}
