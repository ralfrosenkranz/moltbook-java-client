package de.ralfrosenkranz.moltbook.demo.swing;

import de.ralfrosenkranz.moltbook.client.MoltbookClient;
import de.ralfrosenkranz.moltbook.client.model.AgentRegisterRequest;
import de.ralfrosenkranz.moltbook.client.model.AgentRegisterResponse;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

final class SettingsPanel extends JPanel {
    private final ClientManager cm;
    private final Properties props;

    SettingsPanel(ClientManager cm, Properties props) {
        super(new BorderLayout(12, 12));
        this.cm = cm;
        this.props = props;

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        JTextField baseUrl = new JTextField(cm.baseUrl(), 40);
        JTextField apiKey = new JTextField(cm.apiKey(), 40);

        int r = 0;
        gc.gridx = 0; gc.gridy = r; gc.weightx = 0;
        form.add(new JLabel("Base URL"), gc);
        gc.gridx = 1; gc.weightx = 1;
        form.add(baseUrl, gc);

        r++;
        gc.gridx = 0; gc.gridy = r; gc.weightx = 0;
        form.add(new JLabel("API Key"), gc);
        gc.gridx = 1; gc.weightx = 1;
        form.add(apiKey, gc);

        JButton save = new JButton("Save & Reconnect");
        save.addActionListener(e -> {
            cm.updateSettings(baseUrl.getText(), apiKey.getText());
            props.setProperty(ClientManager.KEY_BASE_URL, cm.baseUrl());
            props.setProperty(ClientManager.KEY_API_KEY, cm.apiKey());
            try {
                ConfigStore.save(props);
                JOptionPane.showMessageDialog(this, "Saved: " + ConfigStore.configPath(), "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Cannot save: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel top = new JPanel(new BorderLayout());
        top.add(form, BorderLayout.CENTER);
        top.add(save, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);

        // Registration (creates new API key)
        JPanel reg = new JPanel(new GridBagLayout());
        reg.setBorder(BorderFactory.createTitledBorder("Register new agent (POST /agents/register)"));
        GridBagConstraints gr = new GridBagConstraints();
        gr.insets = new Insets(6,6,6,6);
        gr.fill = GridBagConstraints.HORIZONTAL;
        gr.weightx = 1;

        JTextField name = new JTextField(30);
        JTextArea desc = UiUtil.textArea(4, 30);

        int rr=0;
        gr.gridx=0; gr.gridy=rr; gr.weightx=0;
        reg.add(new JLabel("Name"), gr);
        gr.gridx=1; gr.weightx=1;
        reg.add(name, gr);

        rr++;
        gr.gridx=0; gr.gridy=rr; gr.weightx=0;
        reg.add(new JLabel("Description"), gr);
        gr.gridx=1; gr.weightx=1;
        reg.add(new JScrollPane(desc), gr);

        JTextArea out = UiUtil.textArea(14, 80);
        out.setEditable(false);

        JButton doReg = new JButton("Register");
        doReg.addActionListener(e -> {
            String n = name.getText().trim();
            if (n.isBlank()) {
                JOptionPane.showMessageDialog(this, "Name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            AgentRegisterRequest req = new AgentRegisterRequest(n, desc.getText());
            ApiExecutor.run(this, "Registering agent…", () -> cm.client().getAgentApi().register(req), new ApiExecutor.ResultHandler<>() {
                @Override public void onSuccess(AgentRegisterResponse value) {
                    out.setText(JsonUtil.pretty(value));
                    String newKey = value != null && value.agent() != null ? value.agent().apiKey() : null;
                    String claimUrl = value != null && value.agent() != null ? value.agent().claimUrl() : null;
                    if (newKey != null && !newKey.isBlank()) {
                        apiKey.setText(newKey);
                        // Persist using the same keys as ShyClient
                        cm.storeRegistration(baseUrl.getText(), newKey, n, value.asJson(), claimUrl);
                        props.setProperty(ClientManager.KEY_BASE_URL, cm.baseUrl());
                        props.setProperty(ClientManager.KEY_API_KEY, cm.apiKey());
                        props.setProperty(ClientManager.KEY_AGENT_NAME, n);
                        props.setProperty(ClientManager.KEY_FULL_AGENT_REGISTER_RESPONSE, value.asJson());
                        if (claimUrl != null && !claimUrl.isBlank()) {
                            props.setProperty(ClientManager.KEY_CLAIM_URL, claimUrl);
                        }
                        try {
                            ConfigStore.save(props);
                        } catch (IOException ex) {
                            // ignore; still show API output
                        }
                        // Requirement: show the complete registration response in a popup.
                        JTextArea area = new JTextArea(value.asJson(), 22, 90);
                        area.setEditable(false);
                        area.setLineWrap(true);
                        area.setWrapStyleWord(true);
                        JOptionPane.showMessageDialog(SettingsPanel.this,
                                new JScrollPane(area),
                                "Registration response (incl. setup steps)",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
                @Override public void onError(Throwable error) {
                    ApiExecutor.showError(SettingsPanel.this, error);
                }
            });
        });

        JPanel regSouth = new JPanel(new FlowLayout(FlowLayout.LEFT));
        regSouth.add(doReg);

        JPanel regWrap = new JPanel(new BorderLayout(12,12));
        regWrap.add(reg, BorderLayout.NORTH);
        regWrap.add(regSouth, BorderLayout.CENTER);
        regWrap.add(new JScrollPane(out), BorderLayout.SOUTH);

        add(regWrap, BorderLayout.CENTER);
    }
}
