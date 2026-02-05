package de.ralfrosenkranz.moltbook.demo.swing;

import de.ralfrosenkranz.moltbook.client.model.AgentMe;
import de.ralfrosenkranz.moltbook.client.model.AgentProfileResponse;
import de.ralfrosenkranz.moltbook.client.model.AgentStatus;
import de.ralfrosenkranz.moltbook.client.model.AgentUpdateMeRequest;

import javax.swing.*;
import java.awt.*;

final class AgentsPanel extends JPanel {
    AgentsPanel(ClientManager cm) {
        super(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        JTextArea out = UiUtil.textArea(28, 90);
        out.setEditable(false);

        JPanel actions = new JPanel(new GridLayout(0, 1, 8, 8));

        JButton status = new JButton("GET /agents/status");
        status.addActionListener(e -> ApiExecutor.run(this, "Loading status…",
                () -> cm.client().getAgentApi().status(),
                new SimpleHandler<>(out, this)));

        JButton me = new JButton("GET /agents/me");
        me.addActionListener(e -> ApiExecutor.run(this, "Loading me…",
                () -> cm.client().getAgentApi().me(),
                new SimpleHandler<>(out, this)));

        JTextArea desc = UiUtil.textArea(4, 30);
        JButton update = new JButton("PATCH /agents/me (update description)");
        update.addActionListener(e -> ApiExecutor.run(this, "Updating…",
                () -> cm.client().getAgentApi().updateMe(new AgentUpdateMeRequest(desc.getText())),
                new SimpleHandler<>(out, this)));

        JTextField profileName = new JTextField(20);
        JButton profile = new JButton("GET /agents/profile?name=");
        profile.addActionListener(e -> {
            String n = profileName.getText().trim();
            if (n.isBlank()) {
                JOptionPane.showMessageDialog(this, "Agent name required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ApiExecutor.run(this, "Loading profile…",
                    () -> cm.client().getAgentApi().profileByName(n),
                    new SimpleHandler<>(out, this));
        });

        actions.add(status);
        actions.add(me);

        JPanel updatePanel = new JPanel(new BorderLayout(6,6));
        updatePanel.add(new JLabel("New description:"), BorderLayout.NORTH);
        updatePanel.add(new JScrollPane(desc), BorderLayout.CENTER);
        updatePanel.add(update, BorderLayout.SOUTH);
        actions.add(updatePanel);

        JPanel prof = new JPanel(new FlowLayout(FlowLayout.LEFT));
        prof.add(profileName);
        prof.add(profile);
        actions.add(prof);

        add(actions, BorderLayout.WEST);
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
