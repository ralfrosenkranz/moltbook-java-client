package de.ralfrosenkranz.moltbook.demo.swing;

import de.ralfrosenkranz.moltbook.client.model.SearchResponse;

import javax.swing.*;
import java.awt.*;

final class SearchPanel extends JPanel {
    SearchPanel(ClientManager cm) {
        super(new BorderLayout(12,12));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        JTextArea out = UiUtil.textArea(28, 90);
        out.setEditable(false);

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField q = new JTextField(30);
        JTextField limit = new JTextField("25", 6);
        JButton go = new JButton("Search");
        go.addActionListener(e -> {
            String query = q.getText().trim();
            if (query.isBlank()) {
                JOptionPane.showMessageDialog(this, "query required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ApiExecutor.run(this, "Searching…",
                    () -> cm.client().getSearchApi().search(query, parseIntOrNull(limit.getText())),
                    new SimpleHandler<>(out, this));
        });
        p.add(new JLabel("q")); p.add(q);
        p.add(new JLabel("limit")); p.add(limit);
        p.add(go);

        add(p, BorderLayout.NORTH);
        add(new JScrollPane(out), BorderLayout.CENTER);
    }

    private static Integer parseIntOrNull(String s) {
        try {
            String t = s == null ? "" : s.trim();
            if (t.isBlank()) return null;
            return Integer.parseInt(t);
        } catch (Exception e) {
            return null;
        }
    }

    private static final class SimpleHandler<T> implements ApiExecutor.ResultHandler<T> {
        private final JTextArea out;
        private final JComponent parent;
        SimpleHandler(JTextArea out, JComponent parent) {
            this.out = out; this.parent = parent;
        }
        @Override public void onSuccess(T value) {
            out.setText(JsonUtil.pretty(value));
        }
        @Override public void onError(Throwable error) {
            ApiExecutor.showError(parent, error);
        }
    }
}
