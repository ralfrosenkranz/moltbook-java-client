package de.ralfrosenkranz.moltbook.demo.swing;

import de.ralfrosenkranz.moltbook.client.model.*;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.net.URI;
import java.util.List;

final class HomePanel extends JPanel {
    private final ClientManager cm;

    private final DefaultListModel<Post> postModel = new DefaultListModel<>();
    private final JList<Post> postList = new JList<>(postModel);

    private final DefaultListModel<Comment> commentModel = new DefaultListModel<>();
    private final JList<Comment> commentList = new JList<>(commentModel);

    private final JEditorPane postDetails = new JEditorPane("text/html", "");
    private final JTextArea commentComposer = UiUtil.textArea(4, 60);

    private static final String[] SORT_OPTIONS = new String[]{"new", "hot", "top", "rising", "controversial"};

    private final JComboBox<String> source = new JComboBox<>(new String[]{"Feed (/feed)", "Posts (/posts)", "Submolt feed (/submolts/:name/feed)"});
    private final JComboBox<String> sort = new JComboBox<>(SORT_OPTIONS);
    private final JTextField limit = new JTextField("25", 4);
    private final JTextField offset = new JTextField("0", 4);
    private final JButton offsetUp = new JButton("▲");
    private final JButton offsetDown = new JButton("▼");
    private final JTextField submolt = new JTextField("", 14);
    private final JButton chooseSubmolt = new JButton("Choose…");

    private static class Value<T> {
        public T value;

        public Value(T value) {
            this.value = value;
        }
    }

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
        commentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Show full comment on click (many comments are truncated in the list renderer).
        commentList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) return;
                if (e.getClickCount() != 1) return;
                int idx = commentList.locationToIndex(e.getPoint());
                if (idx < 0) return;
                Rectangle cell = commentList.getCellBounds(idx, idx);
                if (cell == null || !cell.contains(e.getPoint())) return;
                commentList.setSelectedIndex(idx);
                CommentViewerDialog.show(HomePanel.this, commentList, commentModel, idx);
            }
        });

        // Also allow opening via Enter.
        commentList.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "openComment");
        commentList.getActionMap().put("openComment", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int idx = commentList.getSelectedIndex();
                if (idx >= 0) CommentViewerDialog.show(HomePanel.this, commentList, commentModel, idx);
            }
        });

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filters.add(source);
        filters.add(new JLabel("sort")); filters.add(sort);
        filters.add(new JLabel("limit")); filters.add(limit);
        filters.add(new JLabel("offset")); filters.add(offset);
        filters.add(offsetUp);
        filters.add(offsetDown);
        filters.add(new JLabel("submolt"));
        filters.add(submolt);
        filters.add(chooseSubmolt);

        // Only relevant for the "Submolt feed" source.
        // Changing the source is effectively switching context, so jump back to the first page.
        source.addActionListener(e -> {
            updateSubmoltChooserState();
            offset.setText("0");
            submolt.setText("");

            // If the user selects "Submolt feed" without having chosen a submolt yet,
            // immediately open the chooser instead of throwing an exception.
            if (isSubmoltFeedSelected() && blankToNull(submolt.getText()) == null) {
                String picked = SubmoltPickerDialog.pickSubmoltName(this, cm);
                if (picked != null && !picked.isBlank()) {
                    submolt.setText(picked);
                } else {
                    // No submolt chosen -> do not try to load; keep UI empty.
                    postModel.clear();
                    clearPostAndComments();
                    offsetUp.setEnabled(false);
                    offsetDown.setEnabled(false);
                    return;
                }
            }

            loadPosts();
        });
        updateSubmoltChooserState();

        // If the user types a submolt manually and presses Enter, treat this as a submolt switch.
        submolt.addActionListener(e -> {
            if (!isSubmoltFeedSelected()) return;
            offset.setText("0");
            loadPosts();
        });

        sort.addActionListener(e -> {
            loadPosts();
        });

        // If the user types a submolt manually and presses Enter, treat it like a submolt switch.
        submolt.addActionListener(e -> {
            offset.setText("0");
            if (isSubmoltFeedSelected()) loadPosts();
        });

        chooseSubmolt.addActionListener(e -> {
            String picked = SubmoltPickerDialog.pickSubmoltName(this, cm);
            if (picked != null && !picked.isBlank()) {
                submolt.setText(picked);
                // Switching submolt should always jump back to the first page.
                offset.setText("0");
                if (isSubmoltFeedSelected()) loadPosts();
            }
        });

        // Paging buttons: move offset by limit
        offsetUp.addActionListener(e -> pageOffset(-1));
        offsetDown.addActionListener(e -> pageOffset(+1));
        offsetUp.setEnabled(false);
        offsetDown.setEnabled(true);

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

        // Right side: actions + (post details | comments)
        JPanel right = new JPanel(new BorderLayout(8,8));

        postDetails.setEditable(false);
        postDetails.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        postDetails.addHyperlinkListener(ev -> {
            if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    URI uri = ev.getURL() != null ? ev.getURL().toURI() : URI.create(ev.getDescription());
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(uri);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(HomePanel.this, String.valueOf(ex.getMessage()), "Cannot open link", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JPanel postActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton up = new JButton("Upvote");
        JButton down = new JButton("Downvote");
        JButton delete = new JButton("Delete");
        JButton reloadComments = new JButton("Reload comments");
        postActions.add(up); postActions.add(down); postActions.add(delete); postActions.add(reloadComments);
        right.add(postActions, BorderLayout.NORTH);

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

        JSplitPane contentSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        contentSplit.setResizeWeight(0.65);
        contentSplit.setTopComponent(new JScrollPane(postDetails));
        contentSplit.setBottomComponent(commentsWrap);

        right.add(contentSplit, BorderLayout.CENTER);

        split.setRightComponent(right);

        add(split, BorderLayout.CENTER);

        // initial load
        loadPosts();
    }

    private void loadPosts() {
        String src = (String) source.getSelectedItem();
        String s = blankToNull((String) sort.getSelectedItem());
        Integer lim = parseIntOrNull(limit.getText());
        final Value <Integer> off = new Value (parseIntOrNull(offset.getText()));
        final Value <String> sub = new Value (blankToNull(submolt.getText()));
        // The posts endpoint supports an optional time-range parameter ("t"), but the HomePanel UI intentionally omits it.
        String tr = null;

        // If Submolt feed is selected but no submolt is set yet, open the chooser immediately.
        if (src != null && src.startsWith("Submolt") && sub == null) {
            String picked = SubmoltPickerDialog.pickSubmoltName(this, cm);
            if (picked != null && !picked.isBlank()) {
                submolt.setText(picked);
                offset.setText("0");
                // re-read after selection
                off.value = 0;
                sub.value = picked;
            } else {
                // user cancelled -> just clear the UI
                postModel.clear();
                clearPostAndComments();
                offsetUp.setEnabled(false);
                offsetDown.setEnabled(false);
                return;
            }
        }

        ApiExecutor.run(this, "Loading posts…", () -> {
            if (src != null && src.startsWith("Feed")) {
                return cm.client().getFeedApi().getFeed(s, lim, off.value);
            } else if (src != null && src.startsWith("Submolt")) {
                if (sub.value == null) throw new IllegalArgumentException("submolt required for submolt feed");
                return cm.client().getSubmoltsApi().getSubmoltFeed(sub.value, s, lim, off.value);
            } else {
                return cm.client().getPostsApi().getPosts(s, tr, lim, off.value, sub.value);
            }
        }, new ApiExecutor.ResultHandler<>() {
            @Override public void onSuccess(PaginatedPostsResponse value) {
                postModel.clear();
                List<Post> items = value != null ? value.items() : null;

                // If the current paging window yields no items (e.g. offset beyond end),
                // jump back to the first page automatically.
                if ((items == null || items.isEmpty()) && off.value != null && off.value > 0) {
                    offset.setText("0");
                    loadPosts();
                    return;
                }

                if (items != null) {
                    for (Post p : items) postModel.addElement(p);
                }

                updateOffsetButtons(value, lim, off.value);

                // Always select the first entry so the details area shows content immediately.
                if (!postModel.isEmpty()) {
                    postList.setSelectedIndex(0);
                    postList.ensureIndexIsVisible(0);
                } else {
                    clearPostAndComments();
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
                    // Show comments in reverse order (chronological), and scroll to the bottom.
                    for (int i = value.length - 1; i >= 0; i--) {
                        commentModel.addElement(value[i]);
                    }
                    SwingUtilities.invokeLater(() -> {
                        int last = commentModel.getSize() - 1;
                        if (last >= 0) commentList.ensureIndexIsVisible(last);
                    });
                }
            }
            @Override public void onError(Throwable error) {
                ApiExecutor.showError(HomePanel.this, error);
            }
        });
    }

    private void showPost(Post p) {
        if (p == null) {
            clearPostAndComments();
            return;
        }

        String title = MarkdownUtil.escapeHtml(p.title() == null ? "" : p.title());
        String meta = "submolt: " + MarkdownUtil.escapeHtml(submoltLabel(p.submolt()))
                + " &nbsp;&nbsp; author: " + MarkdownUtil.escapeHtml(authorLabel(p.author()))
                + " &nbsp;&nbsp; score: " + p.score();
        String ids = "id: " + MarkdownUtil.escapeHtml(p.id()) + " &nbsp;&nbsp; created_at: " + MarkdownUtil.escapeHtml(String.valueOf(p.createdAt()));
        String urlLine = "";
        if (p.url() != null && !p.url().isBlank()) {
            urlLine = "<div style=\"margin-top:6px\">url: " + MarkdownUtil.linkify(p.url()) + "</div>";
        }

        String body = p.content() == null ? "" : MarkdownUtil.markdownToHtmlBody(p.content());

        String html = "<h2 style=\"margin:0 0 6px 0\">" + title + "</h2>"
                + "<div style=\"color:#444\">" + meta + "</div>"
                + "<div style=\"color:#666;margin-top:2px\">" + ids + "</div>"
                + urlLine
                + "<hr style=\"margin:10px 0\">"
                + body;

        postDetails.setText(MarkdownUtil.wrapInHtmlDocument(html));
        postDetails.setCaretPosition(0);
    }

    private void clearPostAndComments() {
        postDetails.setText("");
        commentModel.clear();
    }

    private void pageOffset(int direction) {
        Integer lim = parseIntOrNull(limit.getText());
        Integer off = parseIntOrNull(offset.getText());
        if (lim == null || lim <= 0) lim = 25;
        if (off == null || off < 0) off = 0;
        int next = off + (direction * lim);
        if (next < 0) next = 0;
        offset.setText(String.valueOf(next));
        loadPosts();
    }

    private void updateOffsetButtons(PaginatedPostsResponse page, Integer lim, Integer off) {
        if (lim == null || lim <= 0) lim = 25;
        if (off == null || off < 0) off = 0;

        boolean canUp = off > 0;
        boolean canDown = true;

        if (page != null) {
            Integer total = page.total();
            List<Post> items = page.items();
            int size = items == null ? 0 : items.size();
            if (total != null) {
                // Some endpoints return "count" as the number of items in this page, not the grand total.
                // In that case total == size (often with off==0) and we must not disable paging prematurely.
                //boolean looksLikePageCountOnly = (total == size) && (off == 0);
                boolean looksLikePageCountOnly = true;
                if (looksLikePageCountOnly) {
                    // Fall back to the heuristic: if we got a full page, allow paging down.
                    canDown = size >= lim;
                } else {
                    canDown = (off + size) < total;
                }
            } else {
                // If the API doesn't provide totals, assume end reached when fewer than limit returned.
                canDown = size >= lim;
            }
        }

        offsetUp.setEnabled(canUp);
        offsetDown.setEnabled(canDown);
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

    private boolean isSubmoltFeedSelected() {
        String src = (String) source.getSelectedItem();
        return src != null && src.startsWith("Submolt");
    }

    private void updateSubmoltChooserState() {
        chooseSubmolt.setEnabled(isSubmoltFeedSelected());
    }

    private static String submoltLabel(de.ralfrosenkranz.moltbook.client.model.Submolt sm) {
        if (sm == null) return "";
        String name = sm.name();
        if (name != null && !name.isBlank()) return name;
        String id = sm.id();
        return id == null ? "" : id;
    }

    private static String authorLabel(de.ralfrosenkranz.moltbook.client.model.Author a) {
        if (a == null) return "";
        String label = a.displayLabel();
        return label == null ? "" : label;
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
                String sub = submoltLabel(p.submolt());
                Integer cc = p.commentCount();
                String comments = cc == null ? "" : ("  [" + cc + "]");
                Integer subs = p.submolt() == null ? null : p.submolt().subscriberCount();
                String subscribers = subs == null ? "" : ("  subs:" + subs);
                l.setText("[" + score + "] " + sub + comments + subscribers + "  " + (p.title() == null ? "" : p.title()));
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
                String author = authorLabel(c.author());
                String content = c.content() == null ? "" : c.content().replace("\n", " ");
                if (content.length() > 120) content = content.substring(0, 120) + "…";
                l.setText("[" + score + "] " + author + ": " + content);
                l.setToolTipText(c.id());
            }
            return l;
        }
    }
}
