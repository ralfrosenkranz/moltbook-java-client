package de.ralfrosenkranz.moltbook.demo.swing;

import de.ralfrosenkranz.moltbook.client.model.*;

import javax.swing.*;
import java.awt.*;

final class CommentsPanel extends JPanel {
    CommentsPanel(ClientManager cm) {
        super(new BorderLayout(12,12));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        JTextArea out = UiUtil.textArea(28, 90);
        out.setEditable(false);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        // List comments
        JPanel list = new JPanel(new FlowLayout(FlowLayout.LEFT));
        list.setBorder(BorderFactory.createTitledBorder("GET /posts/:id/comments"));
        JTextField postId = new JTextField(16);
        JTextField sort = new JTextField("top", 8);
        JTextField limit = new JTextField("", 6);
        JButton load = new JButton("Load");
        load.addActionListener(e -> {
            String id = postId.getText().trim();
            if (id.isBlank()) {
                JOptionPane.showMessageDialog(this, "postId required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ApiExecutor.run(this, "Loading comments…",
                    () -> cm.client().getCommentsApi().getComments(id, blankToNull(sort.getText()), parseIntOrNull(limit.getText())),
                    new SimpleHandler<>(out, this));
        });
        list.add(new JLabel("postId")); list.add(postId);
        list.add(new JLabel("sort")); list.add(sort);
        list.add(new JLabel("limit")); list.add(limit);
        list.add(load);
        left.add(list);

        // Create comment
        JPanel create = new JPanel(new BorderLayout(6,6));
        create.setBorder(BorderFactory.createTitledBorder("POST /posts/:id/comments"));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField cPostId = new JTextField(16);
        JTextField parentId = new JTextField("", 16);
        top.add(new JLabel("postId")); top.add(cPostId);
        top.add(new JLabel("parent_id")); top.add(parentId);
        JTextArea content = UiUtil.textArea(6, 24);
        JButton createBtn = new JButton("Create");
        createBtn.addActionListener(e -> {
            String id = cPostId.getText().trim();
            if (id.isBlank()) {
                JOptionPane.showMessageDialog(this, "postId required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            CommentCreateRequest req = new CommentCreateRequest(content.getText(), blankToNull(parentId.getText()));
            ApiExecutor.run(this, "Creating comment…",
                    () -> cm.client().getCommentsApi().createComment(id, req),
                    new SimpleHandler<>(out, this));
        });
        create.add(top, BorderLayout.NORTH);
        create.add(new JScrollPane(content), BorderLayout.CENTER);
        create.add(createBtn, BorderLayout.SOUTH);
        left.add(create);

        // Delete & vote
        JPanel ops = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ops.setBorder(BorderFactory.createTitledBorder("DELETE /comments/:id & vote"));
        JTextField commentId = new JTextField(18);
        JButton del = new JButton("Delete");
        del.addActionListener(e -> {
            String id = commentId.getText().trim();
            if (id.isBlank()) {
                JOptionPane.showMessageDialog(this, "commentId required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ApiExecutor.run(this, "Deleting comment…", () -> cm.client().getCommentsApi().deleteComment(id), new SimpleHandler<>(out, this));
        });
        JButton up = new JButton("Upvote");
        up.addActionListener(e -> {
            String id = commentId.getText().trim();
            if (id.isBlank()) {
                JOptionPane.showMessageDialog(this, "commentId required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ApiExecutor.run(this, "Upvoting…", () -> cm.client().getCommentsApi().upvoteComment(id), new SimpleHandler<>(out, this));
        });
        JButton down = new JButton("Downvote");
        down.addActionListener(e -> {
            String id = commentId.getText().trim();
            if (id.isBlank()) {
                JOptionPane.showMessageDialog(this, "commentId required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ApiExecutor.run(this, "Downvoting…", () -> cm.client().getCommentsApi().downvoteComment(id), new SimpleHandler<>(out, this));
        });
        ops.add(commentId); ops.add(del); ops.add(up); ops.add(down);
        left.add(ops);

        add(new JScrollPane(left), BorderLayout.WEST);
        add(new JScrollPane(out), BorderLayout.CENTER);
    }

    private static String blankToNull(String s) {
        return s == null || s.trim().isBlank() ? null : s.trim();
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
