package de.ralfrosenkranz.moltbook.demo.swing;

import de.ralfrosenkranz.moltbook.client.model.AgentRegisterResponse;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.net.URI;
import java.util.Map;

/**
 * Human-friendly registration response viewer with clickable links.
 */
final class RegistrationUi {

    private RegistrationUi() {
    }

    static void showRegistrationResponse(Component parent, AgentRegisterResponse resp) {
        String html = toHtml(resp);

        JEditorPane pane = new JEditorPane("text/html", html);
        pane.setEditable(false);
        pane.setCaretPosition(0);
        pane.addHyperlinkListener(e -> {
            if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED) return;
            if (e.getURL() == null) return;
            openUrl(parent, e.getURL().toString());
        });

        JScrollPane sp = new JScrollPane(pane);
        sp.setPreferredSize(new Dimension(900, 600));

        JButton copyJson = new JButton("Copy JSON");
        copyJson.addActionListener(ev -> {
            String json = resp == null ? "" : resp.asJson();
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(json), null);
        });

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(copyJson);

        JPanel wrap = new JPanel(new BorderLayout(10, 10));
        wrap.add(sp, BorderLayout.CENTER);
        wrap.add(south, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(parent, wrap,
                "Registration response (human readable)",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private static void openUrl(Component parent, String url) {
        try {
            if (!Desktop.isDesktopSupported()) throw new UnsupportedOperationException("Desktop not supported");
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent,
                    "Cannot open link:\n" + url + "\n\n" + ex.getMessage(),
                    "Open link failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String toHtml(AgentRegisterResponse resp) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family:sans-serif;'>");

        if (resp == null) {
            sb.append("<h2>Registration response</h2><p>(null)</p></body></html>");
            return sb.toString();
        }

        sb.append("<h2>Registration response</h2>");
        sb.append("<p><b>success:</b> ").append(resp.success()).append("<br/>");
        if (resp.status() != null) sb.append("<b>status:</b> ").append(esc(resp.status())).append("<br/>");
        if (resp.message() != null) sb.append("<b>message:</b> ").append(esc(resp.message())).append("<br/>");
        sb.append("</p>");

        if (resp.agent() != null) {
            sb.append("<h3>Agent</h3><ul>");
            if (resp.agent().id() != null) sb.append(li("ID", esc(resp.agent().id())));
            if (resp.agent().name() != null) sb.append(li("Name", esc(resp.agent().name())));
            if (resp.agent().apiKey() != null) sb.append(li("API key", esc(resp.agent().apiKey())));
            if (resp.agent().claimUrl() != null) sb.append(liLink("Claim URL", resp.agent().claimUrl()));
            if (resp.agent().profileUrl() != null) sb.append(liLink("Profile URL", resp.agent().profileUrl()));
            if (resp.agent().verificationCode() != null) sb.append(li("Verification code", esc(resp.agent().verificationCode())));
            if (resp.agent().createdAt() != null) sb.append(li("Created at", esc(resp.agent().createdAt())));
            sb.append("</ul>");
        }

        if (resp.setup() != null && !resp.setup().isEmpty()) {
            sb.append("<h3>Setup steps</h3>");
            sb.append("<ol>");
            for (Map.Entry<String, AgentRegisterResponse.SetupStep> e : resp.setup().entrySet()) {
                String key = e.getKey();
                AgentRegisterResponse.SetupStep step = e.getValue();
                sb.append("<li>");
                sb.append("<b>").append(esc(key)).append("</b>");
                if (step != null) {
                    sb.append("<div style='margin-top:4px;'>");
                    if (step.action() != null) sb.append("<div><b>action:</b> ").append(esc(step.action())).append("</div>");
                    if (step.details() != null) sb.append("<div><b>details:</b> ").append(esc(step.details())).append("</div>");
                    if (step.why() != null) sb.append("<div><b>why:</b> ").append(esc(step.why())).append("</div>");
                    if (step.url() != null && !step.url().isBlank()) {
                        sb.append("<div><b>url:</b> ").append(a(step.url())).append("</div>");
                    }
                    if (step.messageTemplate() != null && !step.messageTemplate().isBlank()) {
                        sb.append("<div><b>message template:</b><br/>");
                        sb.append("<pre style='white-space:pre-wrap;border:1px solid #ddd;padding:6px;'>")
                                .append(esc(step.messageTemplate()))
                                .append("</pre></div>");
                    }
                    sb.append("</div>");
                }
                sb.append("</li>");
            }
            sb.append("</ol>");
        }

        if (resp.skillFiles() != null && !resp.skillFiles().isEmpty()) {
            sb.append("<h3>Skill files</h3><ul>");
            for (Map.Entry<String, String> e : resp.skillFiles().entrySet()) {
                sb.append("<li><b>").append(esc(e.getKey())).append("</b>: ");
                if (e.getValue() != null && looksLikeUrl(e.getValue())) sb.append(a(e.getValue()));
                else sb.append(esc(String.valueOf(e.getValue())));
                sb.append("</li>");
            }
            sb.append("</ul>");
        }

        if (resp.tweetTemplate() != null && !resp.tweetTemplate().isBlank()) {
            sb.append("<h3>Tweet template</h3>");
            sb.append("<pre style='white-space:pre-wrap;border:1px solid #ddd;padding:6px;'>")
                    .append(esc(resp.tweetTemplate()))
                    .append("</pre>");
        }

        sb.append("<h3>Raw JSON</h3>");
        sb.append("<pre style='white-space:pre-wrap;border:1px solid #ddd;padding:6px;'>")
                .append(esc(resp.asJson()))
                .append("</pre>");

        sb.append("</body></html>");
        return sb.toString();
    }

    private static boolean looksLikeUrl(String s) {
        if (s == null) return false;
        String t = s.trim().toLowerCase();
        return t.startsWith("http://") || t.startsWith("https://");
    }

    private static String li(String k, String v) {
        return "<li><b>" + esc(k) + ":</b> " + v + "</li>";
    }

    private static String liLink(String k, String url) {
        if (url == null || url.isBlank()) return "";
        return "<li><b>" + esc(k) + ":</b> " + a(url) + "</li>";
    }

    private static String a(String url) {
        String u = url == null ? "" : url.trim();
        return "<a href='" + escAttr(u) + "'>" + esc(u) + "</a>";
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String escAttr(String s) {
        // Keep it simple: same escaping as text is fine for href.
        return esc(s).replace("'", "&#39;");
    }
}
