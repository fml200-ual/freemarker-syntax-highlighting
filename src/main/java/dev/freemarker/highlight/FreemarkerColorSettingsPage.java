package dev.freemarker.highlight;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.util.Map;

/**
 * Settings page under <em>Editor &gt; Color Scheme &gt; FreeMarker (highlight)</em>.
 */
public final class FreemarkerColorSettingsPage implements ColorSettingsPage {

    private static final String DEMO_TEXT = """
            <#ftl output_format="HTML">
            <#-- Comentario FreeMarker: no llega al navegador -->
            <#assign titulo = "Panel de " + usuario.nombre>
            <#assign total = 0>
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <title>${titulo?upper_case}</title>
            </head>
            <body class="panel" data-usuario="${usuario.id}">
            <h1>Hola, ${usuario.nombre!"invitado"}</h1>
            <#if listaItems?has_content>
                <#list listaItems as item>
                    <p>${item?index + 1}. ${item.nombre} - ${item.precio?string("0.00")} EUR</p>
                    <#assign total = total + item.precio>
                <#else>
                    <p>Sin elementos.</p>
                </#list>
            <#else>
                <p class="aviso">No hay nada que mostrar.</p>
            </#if>
            <p>Total: ${total}</p>
            <@miBoton texto="Guardar" clase="primario" />
            <@liferay.portlet url=volverURL />
            <!-- comentario HTML -->
            </body>
            </html>
            """;

    private static final AttributesDescriptor[] DESCRIPTORS = {
            new AttributesDescriptor("FTL delimiters", FreemarkerSyntaxHighlighter.FTL_DELIMITER),
            new AttributesDescriptor("Directive name", FreemarkerSyntaxHighlighter.FTL_DIRECTIVE_NAME),
            new AttributesDescriptor("Macro name", FreemarkerSyntaxHighlighter.FTL_MACRO_NAME),
            new AttributesDescriptor("Keyword", FreemarkerSyntaxHighlighter.FTL_KEYWORD),
            new AttributesDescriptor("Identifier", FreemarkerSyntaxHighlighter.FTL_IDENTIFIER),
            new AttributesDescriptor("Builtin", FreemarkerSyntaxHighlighter.FTL_BUILTIN),
            new AttributesDescriptor("String", FreemarkerSyntaxHighlighter.FTL_STRING),
            new AttributesDescriptor("Number", FreemarkerSyntaxHighlighter.FTL_NUMBER),
            new AttributesDescriptor("Operator", FreemarkerSyntaxHighlighter.FTL_OPERATOR),
            new AttributesDescriptor("Punctuation", FreemarkerSyntaxHighlighter.FTL_PUNCTUATION),
            new AttributesDescriptor("FTL comment", FreemarkerSyntaxHighlighter.FTL_COMMENT),
            new AttributesDescriptor("HTML text", FreemarkerSyntaxHighlighter.HTML_TEXT_KEY),
            new AttributesDescriptor("HTML tag name", FreemarkerSyntaxHighlighter.HTML_TAG_NAME_KEY),
            new AttributesDescriptor("HTML tag delimiter", FreemarkerSyntaxHighlighter.HTML_TAG_DELIMITER),
            new AttributesDescriptor("HTML attribute name", FreemarkerSyntaxHighlighter.HTML_ATTRIBUTE_NAME_KEY),
            new AttributesDescriptor("HTML attribute value", FreemarkerSyntaxHighlighter.HTML_ATTRIBUTE_VALUE_KEY),
            new AttributesDescriptor("HTML comment", FreemarkerSyntaxHighlighter.HTML_COMMENT_KEY),
            new AttributesDescriptor("HTML declaration", FreemarkerSyntaxHighlighter.HTML_DECLARATION_KEY),
            new AttributesDescriptor("HTML entity", FreemarkerSyntaxHighlighter.HTML_ENTITY_KEY),
    };

    @Override
    public @Nullable Icon getIcon() {
        return AllIcons.FileTypes.Text;
    }

    @Override
    public @NotNull SyntaxHighlighter getHighlighter() {
        return new FreemarkerSyntaxHighlighter();
    }

    @Override
    public @NotNull String getDemoText() {
        return DEMO_TEXT;
    }

    @Override
    public @Nullable Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }

    @Override
    public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @Override
    public ColorDescriptor @NotNull [] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @Override
    public @NotNull String getDisplayName() {
        return "FreeMarker (highlight)";
    }
}
