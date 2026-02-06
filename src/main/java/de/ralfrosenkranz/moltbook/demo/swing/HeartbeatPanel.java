package de.ralfrosenkranz.moltbook.demo.swing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ralfrosenkranz.moltbook.client.http.MoltbookApiException;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Safe “heartbeat” implementation: calls a small, fixed set of well-known endpoints
 * and displays their JSON responses. It does NOT execute remote instructions.
 */
final class HeartbeatPanel extends JPanel {

    private final ClientManager cm;

    private final JButton runBtn = new JButton("Run heartbeat");
    private final JCheckBox autoBox = new JCheckBox("Auto (every 4h)");
    private final JLabel lastRunLbl = new JLabel("Last run: —");

    private final JTextArea out = new JTextArea();
    private final JScrollPane scroll = new JScrollPane(out);

    private volatile Timer timer;

    HeartbeatPanel(ClientManager cm) {
        this.cm = cm;

        setLayout(new BorderLayout(8, 8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(runBtn);
        top.add(autoBox);
        top.add(lastRunLbl);

        out.setEditable(false);
        out.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        runBtn.addActionListener(e -> runOnce());
        autoBox.addActionListener(e -> toggleAuto());
    }

    private void toggleAuto() {
        if (autoBox.isSelected()) {
            if (timer != null) timer.stop();
            timer = new Timer(4 * 60 * 60 * 1000, e -> runOnce());
            timer.setRepeats(true);
            timer.start();
        } else {
            if (timer != null) timer.stop();
            timer = null;
        }
    }

    private void runOnce() {
        runBtn.setEnabled(false);
        out.setText("Running heartbeat...\n");

        SwingWorker<String, Void> w = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                ObjectMapper om = JsonUtil.MAPPER;

                Map<String, JsonNode> results = new LinkedHashMap<>();

                try {
                    JsonNode n = om.valueToTree(cm.client().getAgentApi().status());
                    results.put("GET /agents/status", n);
                } catch (Exception ex) {
                    results.put("GET /agents/status (error)", errorNode(om, ex));
                }

                try {
                    JsonNode n = cm.client().raw().get("/agents/dm/check", JsonNode.class);
                    results.put("GET /agents/dm/check", n);
                } catch (Exception ex) {
                    results.put("GET /agents/dm/check (error)", errorNode(om, ex));
                }

                try {
                    JsonNode n = om.valueToTree(cm.client().getFeedApi().getFeed("new", 10, 0));
                    results.put("GET /feed?sort=new&limit=10", n);
                } catch (Exception ex) {
                    results.put("GET /feed?sort=new&limit=10 (error)", errorNode(om, ex));
                }

                try {
                    JsonNode n = om.valueToTree(cm.client().getPostsApi().getPosts("new", null, 10, 0, null));
                    results.put("GET /posts?sort=new&limit=10", n);
                } catch (Exception ex) {
                    results.put("GET /posts?sort=new&limit=10 (error)", errorNode(om, ex));
                }

                StringBuilder sb = new StringBuilder();
                for (var e : results.entrySet()) {
                    sb.append("==== ").append(e.getKey()).append(" ====\n");
                    try {
                        sb.append(om.writerWithDefaultPrettyPrinter().writeValueAsString(e.getValue()));
                    } catch (Exception jex) {
                        sb.append(String.valueOf(e.getValue()));
                    }
                    sb.append("\n\n");
                }
                return sb.toString();
            }

            @Override
            protected void done() {
                try {
                    out.setText(get());
                    out.setCaretPosition(0);
                    lastRunLbl.setText("Last run: " + DateTimeFormatter
                            .ofPattern("yyyy-MM-dd HH:mm:ss")
                            .withZone(ZoneId.systemDefault())
                            .format(Instant.now()));
                } catch (Exception ex) {
                    ApiExecutor.showError(HeartbeatPanel.this, ex);
                } finally {
                    runBtn.setEnabled(true);
                }
            }
        };
        w.execute();
    }

    private static JsonNode errorNode(ObjectMapper om, Exception ex) {
        String msg = ex.getMessage();
        if (ex instanceof MoltbookApiException mbe) {
            msg = "HTTP " + mbe.statusCode() + " " + mbe.getMessage() + "\n" + mbe.responseBody();
        }
        return om.createObjectNode()
                .put("error", ex.getClass().getSimpleName())
                .put("message", msg == null ? "" : msg);
    }
}
