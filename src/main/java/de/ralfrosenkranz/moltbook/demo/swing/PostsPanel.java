package de.ralfrosenkranz.moltbook.demo.swing;

import de.ralfrosenkranz.moltbook.client.model.*;

import javax.swing.*;
import java.awt.*;

final class PostsPanel extends JPanel {
    PostsPanel(ClientManager cm) {
        super(new BorderLayout(12,12));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        JTextArea out = UiUtil.textArea(28, 90);
        out.setEditable(false);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        // List posts
        JPanel list = new JPanel(new GridBagLayout());
        list.setBorder(BorderFactory.createTitledBorder("GET /posts"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4,4,4,4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        JTextField sort = new JTextField("hot", 10);
        JTextField t = new JTextField("", 10);
        JTextField limit = new JTextField("25", 6);
        JTextField offset = new JTextField("0", 6);
        JTextField submolt = new JTextField("", 10);

        int r=0;
        gc.gridx=0; gc.gridy=r; list.add(new JLabel("sort"), gc);
        gc.gridx=1; list.add(sort, gc);
        gc.gridx=2; list.add(new JLabel("t"), gc);
        gc.gridx=3; list.add(t, gc);

        r++;
        gc.gridx=0; gc.gridy=r; list.add(new JLabel("limit"), gc);
        gc.gridx=1; list.add(limit, gc);
        gc.gridx=2; list.add(new JLabel("offset"), gc);
        gc.gridx=3; list.add(offset, gc);

        r++;
        gc.gridx=0; gc.gridy=r; list.add(new JLabel("submolt"), gc);
        gc.gridx=1; gc.gridwidth=3; list.add(submolt, gc);
        gc.gridwidth=1;

        JButton listBtn = new JButton("Load");
        listBtn.addActionListener(e -> ApiExecutor.run(this, "Loading posts…", () ->
                cm.client().getPostsApi().getPosts(
                        blankToNull(sort.getText()),
                        blankToNull(t.getText()),
                        parseIntOrNull(limit.getText()),
                        parseIntOrNull(offset.getText()),
                        blankToNull(submolt.getText())
                ), new SimpleHandler<>(out, this)));

        r++;
        gc.gridx=0; gc.gridy=r; gc.gridwidth=4;
        list.add(listBtn, gc);
        gc.gridwidth=1;

        left.add(list);

        // Get post
        JPanel get = new JPanel(new FlowLayout(FlowLayout.LEFT));
        get.setBorder(BorderFactory.createTitledBorder("GET /posts/:id"));
        JTextField postId = new JTextField(18);
        JButton getBtn = new JButton("Load");
        getBtn.addActionListener(e -> {
            String id = postId.getText().trim();
            if (id.isBlank()) {
                JOptionPane.showMessageDialog(this, "postId required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ApiExecutor.run(this, "Loading post…", () -> cm.client().getPostsApi().get(id), new SimpleHandler<>(out, this));
        });
        get.add(postId); get.add(getBtn);
        left.add(get);

        // Create post
        JPanel create = new JPanel(new GridBagLayout());
        create.setBorder(BorderFactory.createTitledBorder("POST /posts"));
        GridBagConstraints cc = new GridBagConstraints();
        cc.insets = new Insets(4,4,4,4);
        cc.fill = GridBagConstraints.HORIZONTAL;
        cc.weightx = 1;

        JTextField cSubmolt = new JTextField(16);
        JTextField title = new JTextField(16);
        JTextArea content = UiUtil.textArea(5, 18);
        JTextField url = new JTextField(16);

        int cr=0;
        cc.gridx=0; cc.gridy=cr; cc.weightx=0; create.add(new JLabel("submolt"), cc);
        cc.gridx=1; cc.weightx=1; create.add(cSubmolt, cc);

        cr++;
        cc.gridx=0; cc.gridy=cr; cc.weightx=0; create.add(new JLabel("title"), cc);
        cc.gridx=1; cc.weightx=1; create.add(title, cc);

        cr++;
        cc.gridx=0; cc.gridy=cr; cc.weightx=0; create.add(new JLabel("content"), cc);
        cc.gridx=1; cc.weightx=1; create.add(new JScrollPane(content), cc);

        cr++;
        cc.gridx=0; cc.gridy=cr; cc.weightx=0; create.add(new JLabel("url (optional)"), cc);
        cc.gridx=1; cc.weightx=1; create.add(url, cc);

        JButton createBtn = new JButton("Create");
        createBtn.addActionListener(e -> {
            if (cSubmolt.getText().trim().isBlank() || title.getText().trim().isBlank()) {
                JOptionPane.showMessageDialog(this, "submolt and title required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            PostCreateRequest req = new PostCreateRequest(
                    cSubmolt.getText().trim(),
                    title.getText().trim(),
                    content.getText(),
                    url.getText().trim().isBlank() ? null : url.getText().trim()
            );
            ApiExecutor.run(this, "Creating post…", () -> cm.client().getPostsApi().create(req), new SimpleHandler<>(out, this));
        });
        cr++;
        cc.gridx=0; cc.gridy=cr; cc.gridwidth=2;
        create.add(createBtn, cc);
        left.add(create);

        // Delete & vote
        JPanel ops = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ops.setBorder(BorderFactory.createTitledBorder("DELETE /posts/:id & vote"));
        JTextField opId = new JTextField(18);
        JButton del = new JButton("Delete");
        del.addActionListener(e -> {
            String id = opId.getText().trim();
            if (id.isBlank()) {
                JOptionPane.showMessageDialog(this, "postId required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ApiExecutor.run(this, "Deleting…", () -> cm.client().getPostsApi().delete(id), new SimpleHandler<>(out, this));
        });
        JButton up = new JButton("Upvote");
        up.addActionListener(e -> {
            String id = opId.getText().trim();
            if (id.isBlank()) {
                JOptionPane.showMessageDialog(this, "postId required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ApiExecutor.run(this, "Upvoting…", () -> cm.client().getPostsApi().upvote(id), new SimpleHandler<>(out, this));
        });
        JButton down = new JButton("Downvote");
        down.addActionListener(e -> {
            String id = opId.getText().trim();
            if (id.isBlank()) {
                JOptionPane.showMessageDialog(this, "postId required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ApiExecutor.run(this, "Downvoting…", () -> cm.client().getPostsApi().downvote(id), new SimpleHandler<>(out, this));
        });
        ops.add(opId); ops.add(del); ops.add(up); ops.add(down);
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
