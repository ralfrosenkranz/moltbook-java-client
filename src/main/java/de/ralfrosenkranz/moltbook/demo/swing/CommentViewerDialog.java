package de.ralfrosenkranz.moltbook.demo.swing;

import de.ralfrosenkranz.moltbook.client.model.Author;
import de.ralfrosenkranz.moltbook.client.model.Comment;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.net.URI;

/**
 * Shows a single comment in full (rendered as Markdown/HTML) with simple navigation.
 */
final class CommentViewerDialog extends JDialog {
    private final ListModel<Comment> model;
    private final JList<Comment> listToSync;
    private int index;

    private final JButton up = new JButton("Up");
    private final JButton down = new JButton("Down");
    private final JButton close = new JButton("Close");
    private final JEditorPane content = new JEditorPane("text/html", "");

    static void show(Component parent, ListModel<Comment> model, int startIndex) {
        Window w = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        CommentViewerDialog dlg = new CommentViewerDialog(w, model, null, startIndex);
        dlg.setVisible(true);
    }

    static void show(Component parent, JList<Comment> listToSync, ListModel<Comment> model, int startIndex) {
        Window w = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        CommentViewerDialog dlg = new CommentViewerDialog(w, model, listToSync, startIndex);
        dlg.setVisible(true);
    }

    private CommentViewerDialog(Window owner, ListModel<Comment> model, JList<Comment> listToSync, int startIndex) {
        super(owner, "Comment", ModalityType.APPLICATION_MODAL);
        this.model = model;
        this.listToSync = listToSync;
        this.index = Math.max(0, Math.min(startIndex, model.getSize() - 1));

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        content.setEditable(false);
        content.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        content.addHyperlinkListener(e -> {
            if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED) return;
            if (e.getURL() == null) return;
            openUrl(this, e.getURL().toString());
        });

        JScrollPane sp = new JScrollPane(content);
        sp.setPreferredSize(new Dimension(900, 600));
        add(sp, BorderLayout.CENTER);

        // Right-side navigation.
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        up.setAlignmentX(Component.CENTER_ALIGNMENT);
        down.setAlignmentX(Component.CENTER_ALIGNMENT);
        nav.add(up);
        nav.add(Box.createVerticalStrut(6));
        nav.add(down);
        add(nav, BorderLayout.EAST);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(close);
        add(south, BorderLayout.SOUTH);

        up.addActionListener(e -> {
            if (index > 0) {
                index--;
                refresh();
            }
        });
        down.addActionListener(e -> {
            if (index < model.getSize() - 1) {
                index++;
                refresh();
            }
        });
        close.addActionListener(e -> dispose());

        // Keyboard shortcuts.
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        refresh();
        pack();
        setLocationRelativeTo(owner);
    }

    private void refresh() {
        int size = model.getSize();
        if (size <= 0) {
            content.setText(MarkdownUtil.wrapInHtmlDocument("<p>(no comment)</p>"));
            up.setEnabled(false);
            down.setEnabled(false);
            setTitle("Comment");
            return;
        }
        index = Math.max(0, Math.min(index, size - 1));
        Comment c = model.getElementAt(index);

        String author = formatAuthor(c == null ? null : c.author());
        String meta = "";
        if (c != null) {
            StringBuilder m = new StringBuilder();
            m.append("<div style='color:#555'>");
            if (author != null && !author.isBlank()) m.append("<b>").append(MarkdownUtil.escapeHtml(author)).append("</b>");
            if (c.score() != null) m.append(" &nbsp; · &nbsp; score: ").append(c.score());
            if (c.createdAt() != null) m.append(" &nbsp; · &nbsp; ").append(MarkdownUtil.escapeHtml(String.valueOf(c.createdAt())));
            if (c.id() != null) m.append(" &nbsp; · &nbsp; id: ").append(MarkdownUtil.escapeHtml(c.id()));
            m.append("</div>");
            meta = m.toString();
        }

        String bodyHtml = MarkdownUtil.markdownToHtmlBody(c == null ? "" : c.content());
        String html = MarkdownUtil.wrapInHtmlDocument(
                "<h2>Comment</h2>" + meta + "<hr/>" + bodyHtml
        );
        content.setText(html);
        content.setCaretPosition(0);

        // Sync selection in the underlying comments list (HomePanel) so the user sees where we are.
        if (listToSync != null) {
            final int idx = index;
            SwingUtilities.invokeLater(() -> {
                listToSync.setSelectedIndex(idx);
                listToSync.ensureIndexIsVisible(idx);
            });
        }

        up.setEnabled(index > 0);
        down.setEnabled(index < size - 1);
        setTitle("Comment (" + (index + 1) + "/" + size + ")");
    }

    private static String formatAuthor(Author a) {
        if (a == null) return "";
        // Prefer name/handle/username, fallback to id.
        if (a.name() != null && !a.name().isBlank()) return a.name();
        if (a.handle() != null && !a.handle().isBlank()) return a.handle();
        if (a.username() != null && !a.username().isBlank()) return a.username();
        if (a.id() != null && !a.id().isBlank()) return a.id();
        return "";
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
}
