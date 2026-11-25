package dev.freemarker.highlight;

import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Supplies a {@link FreemarkerSyntaxHighlighter} for every editor that opens a template. */
public final class FreemarkerSyntaxHighlighterFactory extends SyntaxHighlighterFactory {

    @Override
    public @NotNull SyntaxHighlighter getSyntaxHighlighter(@Nullable Project project,
                                                           @Nullable VirtualFile virtualFile) {
        return new FreemarkerSyntaxHighlighter();
    }
}
