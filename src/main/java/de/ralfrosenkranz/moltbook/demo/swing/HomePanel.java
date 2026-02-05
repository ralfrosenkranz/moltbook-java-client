package de.ralfrosenkranz.moltbook.demo.swing;

import de.ralfrosenkranz.moltbook.client.model.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

final class HomePanel extends JPanel {
    private final ClientManager cm;

    private final DefaultListModel<Post> postModel = new DefaultListModel<>();
    private final JList<Post> postList = new JList<>(postModel);

    private final DefaultListModel<Comment> commentModel = new DefaultListModel<>();
    private final JList<Comment> commentList = new JList<>(commentModel);

    private final JTextArea postDetails = UiUtil.textArea(12, 60);
    private final JTextArea commentComposer = UiUtil.textArea(4, 60);

    private final JComboBox<String> source = new JComboBox<>(new String[]{"Feed (/feed)", "Posts (/posts)", "Submolt feed (/submolts/:name/feed)"});
    private final JTextField sort = new JTextField("hot", 8);
    private final JTextField timeRange = new JTextField("", 6);
    private final JTextField limit = new JTextField("25", 4);
    private final JTextField offset = new JTextField("0", 4);
    private final JTextField submolt = new JTextField("", 10);

    HomePanel(ClientManager cm) {
        super(new BorderLayout(8,8));
        this.cm = cm;
        setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        postList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        postList.setCellRenderer(new PostRenderer());
        postList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Post p = postList.getSelectedValue();
                showPost(p);
                if (p != null) loadComments(p.id());
            }
        });

        commentList.setCellRenderer(new CommentRenderer());

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filters.add(source);
        filters.add(new JLabel("sort")); filters.add(sort);
        filters.add(new JLabel("t")); filters.add(timeRange);
        filters.add(new JLabel("limit")); filters.add(limit);
        filters.add(new JLabel("offset")); filters.add(offset);
        filters.add(new JLabel("submolt")); filters.add(submolt);

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> loadPosts());
        JButton newPost = new JButton("New Post…");
        newPost.addActionListener(e -> createPostDialog());
        filters.add(refresh);
        filters.add(newPost);

        add(filters, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.35);

        split.setLeftComponent(new JScrollPane(postList));

        // Right side: post + actions + comments
        JPanel right = new JPanel(new BorderLayout(8,8));

        postDetails.setEditable(false);
        right.add(new JScrollPane(postDetails), BorderLayout.NORTH);

        JPanel postActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton up = new JButton("Upvote");
        JButton down = new JButton("Downvote");
        JButton delete = new JButton("Delete");
        JButton reloadComments = new JButton("Reload comments");
        postActions.add(up); postActions.add(down); postActions.add(delete); postActions.add(reloadComments);
        right.add(postActions, BorderLayout.CENTER);

        up.addActionListener(e -> {
            Post p = postList.getSelectedValue();
            if (p == null) return;
            ApiExecutor.run(this, "Upvoting…", () -> cm.client().getPostsApi().upvote(p.id()), new ApiExecutor.ResultHandler<>() {
                @Override public void onSuccess(VoteActionResponse value) {
                    loadPosts();
                }
                @Override public void onError(Throwable error) {
                    ApiExecutor.showError(HomePanel.this, error);
                }
            });
        });
        down.addActionListener(e -> {
            Post p = postList.getSelectedValue();
            if (p == null) return;
            ApiExecutor.run(this, "Downvoting…", () -> cm.client().getPostsApi().downvote(p.id()), new ApiExecutor.ResultHandler<>() {
                @Override public void onSuccess(VoteActionResponse value) {
                    loadPosts();
                }
                @Override public void onError(Throwable error) {
                    ApiExecutor.showError(HomePanel.this, error);
                }
            });
        });
        delete.addActionListener(e -> {
            Post p = postList.getSelectedValue();
            if (p == null) return;
            int ok = JOptionPane.showConfirmDialog(this, "Delete post " + p.id() + "?", "Confirm delete", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;
            ApiExecutor.run(this, "Deleting…", () -> cm.client().getPostsApi().delete(p.id()), new ApiExecutor.ResultHandler<>() {
                @Override public void onSuccess(ApiSuccessResponse value) {
                    loadPosts();
                    commentModel.clear();
                    postDetails.setText("");
                }
                @Override public void onError(Throwable error) {
                    ApiExecutor.showError(HomePanel.this, error);
                }
            });
        });
        reloadComments.addActionListener(e -> {
            Post p = postList.getSelectedValue();
            if (p != null) loadComments(p.id());
        });

        JPanel commentsWrap = new JPanel(new BorderLayout(6,6));
        commentsWrap.setBorder(BorderFactory.createTitledBorder("Comments"));
        commentsWrap.add(new JScrollPane(commentList), BorderLayout.CENTER);

        JPanel compose = new JPanel(new BorderLayout(6,6));
        compose.add(new JScrollPane(commentComposer), BorderLayout.CENTER);
        JButton send = new JButton("Add comment");
        send.addActionListener(e -> {
            Post p = postList.getSelectedValue();
            if (p == null) {
                JOptionPane.showMessageDialog(this, "Select a post first.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String c = commentComposer.getText();
            if (c.trim().isBlank()) return;
            CommentCreateRequest req = new CommentCreateRequest(c, null);
            ApiExecutor.run(this, "Posting comment…", () -> cm.client().getCommentsApi().createComment(p.id(), req), new ApiExecutor.ResultHandler<>() {
                @Override public void onSuccess(Comment value) {
                    commentComposer.setText("");
                    loadComments(p.id());
                }
                @Override public void onError(Throwable error) {
                    ApiExecutor.showError(HomePanel.this, error);
                }
            });
        });
        compose.add(send, BorderLayout.SOUTH);
        commentsWrap.add(compose, BorderLayout.SOUTH);

        right.add(commentsWrap, BorderLayout.SOUTH);

        split.setRightComponent(right);

        add(split, BorderLayout.CENTER);

        // initial load
        loadPosts();
    }

    private void loadPosts() {
        String src = (String) source.getSelectedItem();
        String s = blankToNull(sort.getText());
        Integer lim = parseIntOrNull(limit.getText());
        Integer off = parseIntOrNull(offset.getText());
        String sub = blankToNull(submolt.getText());
        String tr = blankToNull(timeRange.getText());

        ApiExecutor.run(this, "Loading posts…", () -> {
            if (src != null && src.startsWith("Feed")) {
                return cm.client().getFeedApi().getFeed(s, lim, off);
            } else if (src != null && src.startsWith("Submolt")) {
                if (sub == null) throw new IllegalArgumentException("submolt required for submolt feed");
                return cm.client().getSubmoltsApi().getSubmoltFeed(sub, s, lim, off);
            } else {
                return cm.client().getPostsApi().getPosts(s, tr, lim, off, sub);
            }
        }, new ApiExecutor.ResultHandler<>() {
            @Override public void onSuccess(PaginatedPostsResponse value) {
                postModel.clear();
                List<Post> items = value != null ? value.items() : null;
                if (items != null) {
                    for (Post p : items) postModel.addElement(p);
                }
            }
            @Override public void onError(Throwable error) {
                ApiExecutor.showError(HomePanel.this, error);
            }
        });
    }

    private void loadComments(String postId) {
        ApiExecutor.run(this, "Loading comments…", () -> cm.client().getCommentsApi().getComments(postId, "top", 200), new ApiExecutor.ResultHandler<>() {
            @Override public void onSuccess(Comment[] value) {
                commentModel.clear();
                if (value != null) {
                    for (Comment c : value) commentModel.addElement(c);
                }
            }
            @Override public void onError(Throwable error) {
                ApiExecutor.showError(HomePanel.this, error);
            }
        });
    }

    private void showPost(Post p) {
        if (p == null) {
            postDetails.setText("");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(p.title() == null ? "" : p.title()).append("\n");
        sb.append("submolt: ").append(p.submolt()).append("   author: ").append(p.author()).append("   score: ").append(p.score()).append("\n");
        sb.append("id: ").append(p.id()).append("   created_at: ").append(p.createdAt()).append("\n\n");
        if (p.url() != null && !p.url().isBlank()) {
            sb.append("url: ").append(p.url()).append("\n\n");
        }
        if (p.content() != null) {
            sb.append(p.content());
        }
        postDetails.setText(sb.toString());
        postDetails.setCaretPosition(0);
    }

    private void createPostDialog() {
        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this), "Create Post", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,6,6,6);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        JTextField sub = new JTextField(submolt.getText().trim(), 20);
        JTextField title = new JTextField(30);
        JTextArea content = UiUtil.textArea(8, 40);
        JTextField url = new JTextField("", 30);

        int r=0;
        gc.gridx=0; gc.gridy=r; gc.weightx=0; p.add(new JLabel("submolt"), gc);
        gc.gridx=1; gc.weightx=1; p.add(sub, gc);
        r++;
        gc.gridx=0; gc.gridy=r; gc.weightx=0; p.add(new JLabel("title"), gc);
        gc.gridx=1; gc.weightx=1; p.add(title, gc);
        r++;
        gc.gridx=0; gc.gridy=r; gc.weightx=0; p.add(new JLabel("content"), gc);
        gc.gridx=1; gc.weightx=1; p.add(new JScrollPane(content), gc);
        r++;
        gc.gridx=0; gc.gridy=r; gc.weightx=0; p.add(new JLabel("url (optional)"), gc);
        gc.gridx=1; gc.weightx=1; p.add(url, gc);

        JButton create = new JButton("Create");
        JButton cancel = new JButton("Cancel");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancel); buttons.add(create);

        cancel.addActionListener(e -> d.dispose());
        create.addActionListener(e -> {
            if (sub.getText().trim().isBlank() || title.getText().trim().isBlank()) {
                JOptionPane.showMessageDialog(d, "submolt and title required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            PostCreateRequest req = new PostCreateRequest(sub.getText().trim(), title.getText().trim(),
                    content.getText(), url.getText().trim().isBlank() ? null : url.getText().trim());
            ApiExecutor.run(this, "Creating post…", () -> cm.client().getPostsApi().create(req), new ApiExecutor.ResultHandler<>() {
                @Override public void onSuccess(Post value) {
                    d.dispose();
                    loadPosts();
                }
                @Override public void onError(Throwable error) {
                    ApiExecutor.showError(HomePanel.this, error);
                }
            });
        });

        JPanel wrap = new JPanel(new BorderLayout(8,8));
        wrap.add(p, BorderLayout.CENTER);
        wrap.add(buttons, BorderLayout.SOUTH);

        d.setContentPane(wrap);
        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
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

    private static final class PostRenderer extends DefaultListCellRenderer {
        @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Post p) {
                String score = p.score() == null ? "" : String.valueOf(p.score());
                String sub = p.submolt() == null ? "" : p.submolt();
                l.setText("[" + score + "] " + sub + "  " + (p.title() == null ? "" : p.title()));
                l.setToolTipText(p.id());
            }
            return l;
        }
    }

    private static final class CommentRenderer extends DefaultListCellRenderer {
        @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Comment c) {
                String score = c.score() == null ? "" : String.valueOf(c.score());
                String author = c.author() == null ? "" : c.author();
                String content = c.content() == null ? "" : c.content().replace("\n", " ");
                if (content.length() > 120) content = content.substring(0, 120) + "…";
                l.setText("[" + score + "] " + author + ": " + content);
                l.setToolTipText(c.id());
            }
            return l;
        }
    }
}
