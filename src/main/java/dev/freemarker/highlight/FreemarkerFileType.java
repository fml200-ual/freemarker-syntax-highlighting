package dev.freemarker.highlight;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

/**
 * File type for FreeMarker templates: {@code .ftl} (plain), {@code .ftlh} (HTML auto-escaping) and
 * {@code .ftlx} (XML auto-escaping).
 */
public final class FreemarkerFileType extends LanguageFileType {

    public static final FreemarkerFileType INSTANCE = new FreemarkerFileType();

    private FreemarkerFileType() {
        super(FreemarkerLanguage.INSTANCE);
    }

    @Override
    public @NotNull String getName() {
        return "FreeMarker (highlight)";
    }

    @Override
    public @NotNull String getDescription() {
        return "FreeMarker template";
    }

    @Override
    public @NotNull String getDefaultExtension() {
        return "ftl";
    }

    @Override
    public @Nullable Icon getIcon() {
        return AllIcons.FileTypes.Text;
    }
}
