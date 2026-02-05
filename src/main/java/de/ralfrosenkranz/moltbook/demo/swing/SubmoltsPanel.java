package de.ralfrosenkranz.moltbook.demo.swing;

import de.ralfrosenkranz.moltbook.client.model.*;

import javax.swing.*;
import java.awt.*;

final class SubmoltsPanel extends JPanel {
    SubmoltsPanel(ClientManager cm) {
        super(new BorderLayout(12,12));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        JTextArea out = UiUtil.textArea(28, 90);
        out.setEditable(false);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        // List
        JPanel list = new JPanel(new FlowLayout(FlowLayout.LEFT));
        list.setBorder(BorderFactory.createTitledBorder("GET /submolts"));
        JTextField sort = new JTextField("hot", 8);
        JTextField limit = new JTextField("30", 6);
        JTextField offset = new JTextField("0", 6);
        JButton load = new JButton("Load");
        load.addActionListener(e -> ApiExecutor.run(this, "Loading submolts…",
                () -> cm.client().getSubmoltsApi().getSubmolts(blankToNull(sort.getText()), parseIntOrNull(limit.getText()), parseIntOrNull(offset.getText())),
                new SimpleHandler<>(out, this)));
        list.add(new JLabel("sort")); list.add(sort);
        list.add(new JLabel("limit")); list.add(limit);
        list.add(new JLabel("offset")); list.add(offset);
        list.add(load);
        left.add(list);

        // Get by name
        JPanel get = new JPanel(new FlowLayout(FlowLayout.LEFT));
        get.setBorder(BorderFactory.createTitledBorder("GET /submolts/:name"));
        JTextField name = new JTextField(16);
        JButton getBtn = new JButton("Load");
        getBtn.addActionListener(e -> {
            String n = name.getText().trim();
            if (n.isBlank()) {
                JOptionPane.showMessageDialog(this, "name required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ApiExecutor.run(this, "Loading submolt…", () -> cm.client().getSubmoltsApi().getSubmolt(n), new SimpleHandler<>(out, this));
        });
        get.add(name); get.add(getBtn);
        left.add(get);

        // Create
        JPanel create = new JPanel(new GridBagLayout());
        create.setBorder(BorderFactory.createTitledBorder("POST /submolts"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4,4,4,4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        JTextField cName = new JTextField(16);
        JTextField display = new JTextField(16);
        JTextArea desc = UiUtil.textArea(4, 18);

        int r=0;
        gc.gridx=0; gc.gridy=r; gc.weightx=0; create.add(new JLabel("name"), gc);
        gc.gridx=1; gc.weightx=1; create.add(cName, gc);
        r++;
        gc.gridx=0; gc.gridy=r; gc.weightx=0; create.add(new JLabel("display_name"), gc);
        gc.gridx=1; gc.weightx=1; create.add(display, gc);
        r++;
        gc.gridx=0; gc.gridy=r; gc.weightx=0; create.add(new JLabel("description"), gc);
        gc.gridx=1; gc.weightx=1; create.add(new JScrollPane(desc), gc);

        JButton createBtn = new JButton("Create");
        createBtn.addActionListener(e -> {
            if (cName.getText().trim().isBlank()) {
                JOptionPane.showMessageDialog(this, "name required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            SubmoltCreateRequest req = new SubmoltCreateRequest(cName.getText().trim(), blankToNull(display.getText()), desc.getText());
            ApiExecutor.run(this, "Creating submolt…", () -> cm.client().getSubmoltsApi().createSubmolt(req), new SimpleHandler<>(out, this));
        });
        r++;
        gc.gridx=0; gc.gridy=r; gc.gridwidth=2;
        create.add(createBtn, gc);
        left.add(create);

        // Subscribe / Unsubscribe / Feed
        JPanel ops = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ops.setBorder(BorderFactory.createTitledBorder("Subscribe / Feed"));
        JTextField opName = new JTextField(16);
        JButton sub = new JButton("Subscribe");
        sub.addActionListener(e -> {
            String n = opName.getText().trim();
            if (n.isBlank()) {
                JOptionPane.showMessageDialog(this, "name required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ApiExecutor.run(this, "Subscribing…", () -> cm.client().getSubmoltsApi().subscribeSubmolt(n), new SimpleHandler<>(out, this));
        });
        JButton unsub = new JButton("Unsubscribe");
        unsub.addActionListener(e -> {
            String n = opName.getText().trim();
            if (n.isBlank()) {
                JOptionPane.showMessageDialog(this, "name required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ApiExecutor.run(this, "Unsubscribing…", () -> cm.client().getSubmoltsApi().unsubscribeSubmolt(n), new SimpleHandler<>(out, this));
        });

        JTextField feedSort = new JTextField("hot", 6);
        JTextField feedLimit = new JTextField("25", 4);
        JTextField feedOffset = new JTextField("0", 4);
        JButton feed = new JButton("Load Feed");
        feed.addActionListener(e -> {
            String n = opName.getText().trim();
            if (n.isBlank()) {
                JOptionPane.showMessageDialog(this, "name required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ApiExecutor.run(this, "Loading submolt feed…", () ->
                    cm.client().getSubmoltsApi().getSubmoltFeed(n, blankToNull(feedSort.getText()), parseIntOrNull(feedLimit.getText()), parseIntOrNull(feedOffset.getText())),
                    new SimpleHandler<>(out, this));
        });

        ops.add(new JLabel("name")); ops.add(opName);
        ops.add(sub); ops.add(unsub);
        ops.add(new JLabel("sort")); ops.add(feedSort);
        ops.add(new JLabel("limit")); ops.add(feedLimit);
        ops.add(new JLabel("offset")); ops.add(feedOffset);
        ops.add(feed);
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
