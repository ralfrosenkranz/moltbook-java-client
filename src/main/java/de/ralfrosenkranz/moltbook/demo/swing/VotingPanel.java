package de.ralfrosenkranz.moltbook.demo.swing;

import javax.swing.*;
import java.awt.*;

final class VotingPanel extends JPanel {
    VotingPanel(ClientManager cm) {
        super(new BorderLayout(12,12));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        JTextArea out = UiUtil.textArea(18, 80);
        out.setEditable(false);

        JPanel post = new JPanel(new FlowLayout(FlowLayout.LEFT));
        post.setBorder(BorderFactory.createTitledBorder("VotingApi - posts"));
        JTextField postId = new JTextField(20);
        JButton up = new JButton("upvotePost (void)");
        JButton down = new JButton("downvotePost (void)");
        up.addActionListener(e -> {
            String id = postId.getText().trim();
            if (id.isBlank()) {
                JOptionPane.showMessageDialog(this, "postId required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ApiExecutor.run(this, "Upvoting…", () -> {
                cm.client().getVotingApi().upvotePost(id);
                return "OK";
            }, new SimpleHandler<>(out, this));
        });
        down.addActionListener(e -> {
            String id = postId.getText().trim();
            if (id.isBlank()) {
                JOptionPane.showMessageDialog(this, "postId required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ApiExecutor.run(this, "Downvoting…", () -> {
                cm.client().getVotingApi().downvotePost(id);
                return "OK";
            }, new SimpleHandler<>(out, this));
        });
        post.add(new JLabel("postId")); post.add(postId); post.add(up); post.add(down);

        JPanel comment = new JPanel(new FlowLayout(FlowLayout.LEFT));
        comment.setBorder(BorderFactory.createTitledBorder("VotingApi - comments"));
        JTextField commentId = new JTextField(20);
        JButton upc = new JButton("upvoteComment (void)");
        upc.addActionListener(e -> {
            String id = commentId.getText().trim();
            if (id.isBlank()) {
                JOptionPane.showMessageDialog(this, "commentId required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ApiExecutor.run(this, "Upvoting…", () -> {
                cm.client().getVotingApi().upvoteComment(id);
                return "OK";
            }, new SimpleHandler<>(out, this));
        });
        comment.add(new JLabel("commentId")); comment.add(commentId); comment.add(upc);

        JPanel top = new JPanel(new GridLayout(0,1,8,8));
        top.add(post);
        top.add(comment);

        add(top, BorderLayout.NORTH);
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
