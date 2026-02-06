package de.ralfrosenkranz.moltbook.demo.swing;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.net.URI;

/**
 * Small helper to render Moltbook post/comment markdown to HTML for Swing (JEditorPane).
 */
final class MarkdownUtil {
    private static final Parser PARSER;
    private static final HtmlRenderer RENDERER;

    static {
        MutableDataSet options = new MutableDataSet();
        // flexmark-all includes many extensions; defaults are fine for common markdown.
        PARSER = Parser.builder(options).build();
        RENDERER = HtmlRenderer.builder(options).build();
    }

    private MarkdownUtil() {}

    static String markdownToHtmlBody(String markdown) {
        if (markdown == null) return "";
        return RENDERER.render(PARSER.parse(markdown));
    }

    static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    static String linkify(String s) {
        // Very small helper: if it's a valid URI, wrap it in an <a>. Otherwise escape.
        if (s == null) return "";
        String t = s.trim();
        if (t.isEmpty()) return "";
        try {
            URI u = URI.create(t);
            String scheme = u.getScheme();
            if (scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
                String e = escapeHtml(t);
                return "<a href=\"" + e + "\">" + e + "</a>";
            }
        } catch (Exception ignore) {}
        return escapeHtml(t);
    }

    static String wrapInHtmlDocument(String bodyHtml) {
        // Basic, readable styling for Swing's HTML renderer.
        return "<html><head>" +
                "<style>" +
                "body{font-family:sans-serif;font-size:12px;line-height:1.35;}" +
                "pre,code{font-family:monospace;}" +
                "pre{background:#f6f6f6;padding:8px;border:1px solid #ddd;}" +
                "blockquote{border-left:3px solid #ddd;margin-left:0;padding-left:10px;color:#555;}" +
                "a{color:#0645ad;text-decoration:underline;}" +
                "</style></head><body>" +
                bodyHtml +
                "</body></html>";
    }
}
