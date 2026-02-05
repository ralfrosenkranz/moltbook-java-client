package de.ralfrosenkranz.moltbook.demo.swing;

import javax.swing.*;
import java.awt.*;

final class FollowingPanel extends JPanel {
    FollowingPanel(ClientManager cm) {
        super(new BorderLayout(12,12));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        JTextArea out = UiUtil.textArea(28, 90);
        out.setEditable(false);

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setBorder(BorderFactory.createTitledBorder("POST/DELETE /agents/:name/follow"));
        JTextField name = new JTextField(20);
        JButton follow = new JButton("Follow");
        JButton unfollow = new JButton("Unfollow");

        follow.addActionListener(e -> {
            String n = name.getText().trim();
            if (n.isBlank()) {
                JOptionPane.showMessageDialog(this, "agent name required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ApiExecutor.run(this, "Following…", () -> {
                cm.client().getFollowingApi().follow(n);
                return "OK";
            }, new SimpleHandler<>(out, this));
        });

        unfollow.addActionListener(e -> {
            String n = name.getText().trim();
            if (n.isBlank()) {
                JOptionPane.showMessageDialog(this, "agent name required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ApiExecutor.run(this, "Unfollowing…", () -> {
                cm.client().getFollowingApi().unfollow(n);
                return "OK";
            }, new SimpleHandler<>(out, this));
        });

        p.add(new JLabel("agent name")); p.add(name); p.add(follow); p.add(unfollow);

        add(p, BorderLayout.NORTH);
        add(new JScrollPane(out), BorderLayout.CENTER);
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
