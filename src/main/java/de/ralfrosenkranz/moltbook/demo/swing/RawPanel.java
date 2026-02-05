package de.ralfrosenkranz.moltbook.demo.swing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

final class RawPanel extends JPanel {
    RawPanel(ClientManager cm) {
        super(new BorderLayout(12,12));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        JComboBox<String> method = new JComboBox<>(new String[]{"GET","POST","PATCH","DELETE"});
        JTextField path = new JTextField("/feed", 40);
        JTextArea queryJson = UiUtil.textArea(3, 60);
        queryJson.setText("{}");
        JTextArea bodyJson = UiUtil.textArea(8, 60);
        bodyJson.setText("null");

        JTextArea out = UiUtil.textArea(28, 90);
        out.setEditable(false);

        JButton send = new JButton("Send");
        send.addActionListener(e -> {
            String m = (String) method.getSelectedItem();
            String p = path.getText().trim();
            if (p.isBlank()) {
                JOptionPane.showMessageDialog(this, "path required (e.g. /feed).", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            ApiExecutor.run(this, "Calling raw endpoint…", () -> {
                Map<String,String> q = parseQuery(queryJson.getText());
                Object body = parseBody(bodyJson.getText());
                return switch (m) {
                    case "GET" -> {
                        if (q == null || q.isEmpty()) yield cm.client().raw().get(p, JsonNode.class);
                        yield cm.client().raw().get(p, q, JsonNode.class);
                    }
                    case "POST" -> cm.client().raw().post(p, body, JsonNode.class);
                    case "PATCH" -> cm.client().raw().patch(p, body, JsonNode.class);
                    case "DELETE" -> cm.client().raw().delete(p, JsonNode.class);
                    default -> throw new IllegalStateException("Unknown method: " + m);
                };
            }, new ApiExecutor.ResultHandler<>() {
                @Override
                public void onSuccess(JsonNode value) {
                    out.setText(JsonUtil.pretty(value));
                }
                @Override public void onError(Throwable error) {
                    ApiExecutor.showError(RawPanel.this, error);
                }
            });
        });

        JPanel north = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4,4,4,4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        int r=0;
        gc.gridx=0; gc.gridy=r; gc.weightx=0; north.add(new JLabel("Method"), gc);
        gc.gridx=1; gc.weightx=0; north.add(method, gc);
        gc.gridx=2; gc.weightx=0; north.add(new JLabel("Path"), gc);
        gc.gridx=3; gc.weightx=1; north.add(path, gc);
        gc.gridx=4; gc.weightx=0; north.add(send, gc);

        r++;
        gc.gridx=0; gc.gridy=r; gc.weightx=0; north.add(new JLabel("Query JSON (object)"), gc);
        gc.gridx=1; gc.gridwidth=4; gc.weightx=1; north.add(new JScrollPane(queryJson), gc);
        gc.gridwidth=1;

        r++;
        gc.gridx=0; gc.gridy=r; gc.weightx=0; north.add(new JLabel("Body JSON"), gc);
        gc.gridx=1; gc.gridwidth=4; gc.weightx=1; north.add(new JScrollPane(bodyJson), gc);

        add(north, BorderLayout.NORTH);
        add(new JScrollPane(out), BorderLayout.CENTER);
    }

    private static Map<String,String> parseQuery(String json) throws Exception {
        if (json == null || json.trim().isBlank()) return Map.of();
        return JsonUtil.MAPPER.readValue(json, new TypeReference<>() {});
    }

    private static Object parseBody(String json) throws Exception {
        if (json == null || json.trim().isBlank() || json.trim().equals("null")) return null;
        return JsonUtil.MAPPER.readTree(json);
    }
}
