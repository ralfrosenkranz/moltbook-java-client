package de.ralfrosenkranz.moltbook.demo.swing;

import de.ralfrosenkranz.moltbook.client.MoltbookClient;
import de.ralfrosenkranz.moltbook.client.MoltbookClientConfig;
import de.ralfrosenkranz.moltbook.client.model.AgentRegisterRequest;
import de.ralfrosenkranz.moltbook.client.model.AgentRegisterResponse;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.List;

final class AppBootstrap {

    static void bootstrapAndShow() {
        Properties props;
        try {
            props = ConfigStore.loadOrCreate();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Cannot load config: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ClientManager cm = new ClientManager(props);

        // Requirement: if properties are incomplete, guide the user through registration (like ShyClient)
        if (!cm.isConfigComplete()) {
            try {
                AgentRegisterResponse resp = maybeRunRegistrationWizard(null, cm, props);
                if (resp != null) {
                    showRegistrationPopup(null, resp);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Registration failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        // Optional console overview (best-effort; never prevent startup)
        try {
            if (!cm.apiKey().isBlank()) {
                dumpOverview(cm.client(), Collections.emptyMap());
            }
        } catch (Exception ignored) {
            // ignore
        }

        MoltbookFrame f = new MoltbookFrame(cm, props);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setSize(1200, 800);
        f.setLocationRelativeTo(null);
        f.setVisible(true);

        // If still no apiKey -> hint.
        if (cm.apiKey().isBlank()) {
            JOptionPane.showMessageDialog(f,
                    "No API key configured. You can register an agent or paste an API key in the Settings tab.\n\nConfig file: " + ConfigStore.configPath(),
                    "Moltbook Swing Client", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static AgentRegisterResponse maybeRunRegistrationWizard(JComponent parent, ClientManager cm, Properties props) throws Exception {
        String defaultBaseUrl = props.getProperty(ClientManager.KEY_BASE_URL, "https://www.moltbook.com/api/v1");
        String baseUrl = askInput(parent, "Moltbook Base URL", "Base URL", defaultBaseUrl);
        if (baseUrl == null) return null; // canceled

        String defaultName = props.getProperty(ClientManager.KEY_AGENT_NAME, "RRSwingClient_" + randomSuffix());
        String agentName = askInput(parent, "Agent name", "Agent name", defaultName);
        if (agentName == null) return null;

        String defaultDesc = props.getProperty("description", "Swing demo agent (RRSwingClient)");
        String desc = askMultiline(parent, "Description", defaultDesc);
        if (desc == null) return null;

        // Register without API key
        MoltbookClientConfig cfg = MoltbookClientConfig.builder()
                .baseUrl(baseUrl.trim())
                .apiKey(null)
                .build();

        try (MoltbookClient client = MoltbookClient.builder().config(cfg).build()) {
            AgentRegisterRequest req = new AgentRegisterRequest(agentName.trim(), desc);
            AgentRegisterResponse resp = client.getAgentApi().register(req);

            String newKey = resp != null && resp.agent() != null ? resp.agent().apiKey() : null;
            String claimUrl = resp != null && resp.agent() != null ? resp.agent().claimUrl() : null;
            if (newKey == null || newKey.trim().isEmpty()) {
                throw new IOException("Registration succeeded but no api_key was returned.");
            }

            // Persist using the same keys as ShyClient
            cm.storeRegistration(baseUrl, newKey, agentName, resp.asJson(), claimUrl);
            props.setProperty(ClientManager.KEY_BASE_URL, cm.baseUrl());
            props.setProperty(ClientManager.KEY_API_KEY, cm.apiKey());
            props.setProperty(ClientManager.KEY_AGENT_NAME, agentName.trim());
            props.setProperty(ClientManager.KEY_FULL_AGENT_REGISTER_RESPONSE, resp.asJson());
            if (claimUrl != null && !claimUrl.trim().isEmpty()) {
                props.setProperty(ClientManager.KEY_CLAIM_URL, claimUrl.trim());
            }
            // Keep description only as a convenience for future prompts
            props.setProperty("description", desc);
            ConfigStore.save(props);
            return resp;
        }
    }

    private static String askInput(JComponent parent, String title, String label, String initial) {
        JTextField tf = new JTextField(initial == null ? "" : initial, 40);
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.add(new JLabel(label), BorderLayout.NORTH);
        p.add(tf, BorderLayout.CENTER);
        int r = JOptionPane.showConfirmDialog(parent, p, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return null;
        String v = tf.getText() == null ? "" : tf.getText().trim();
        return v.isEmpty() ? (initial == null ? "" : initial.trim()) : v;
    }

    private static String askMultiline(JComponent parent, String title, String initial) {
        JTextArea ta = new JTextArea(initial == null ? "" : initial, 6, 48);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(ta);
        int r = JOptionPane.showConfirmDialog(parent, sp, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return null;
        String v = ta.getText() == null ? "" : ta.getText().trim();
        return v.isEmpty() ? (initial == null ? "" : initial) : v;
    }

    private static void showRegistrationPopup(JComponent parent, AgentRegisterResponse resp) {
        String txt = resp == null ? "(null)" : resp.asJson();
        JTextArea area = new JTextArea(txt, 22, 90);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(area);
        JOptionPane.showMessageDialog(parent, sp, "Registration response (incl. setup steps)", JOptionPane.INFORMATION_MESSAGE);
    }

    private static String randomSuffix() {
        String alphabet = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random r = new Random();
        for (int i = 0; i < 6; i++) sb.append(alphabet.charAt(r.nextInt(alphabet.length())));
        return sb.toString();
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

    private static int parseIntOr(String s, int def) {
        try {
            if (s == null) return def;
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (!isBlank(v)) return v;
        }
        return null;
    }

    private static void dumpOverview(MoltbookClient client, Map<String, String> flags) throws IOException {
        int submoltLimit = parseIntOr(flags.get("submolts"), 30);
        int postLimit = parseIntOr(flags.get("posts"), 25);
        int samplePerSubmolt = parseIntOr(flags.get("sample"), 2);
        String sort = firstNonBlank(flags.get("sort"), "hot");
        String submoltSort = firstNonBlank(flags.get("submoltSort"), "popular");

        // 1) List submolts (paginated)
        var submoltsPage = client.getSubmoltsApi().getSubmolts("new", 100, 0);
        var submolts = (submoltsPage != null && submoltsPage.items() != null) ? submoltsPage.items() : List.<de.ralfrosenkranz.moltbook.client.model.Submolt>of();
        if (submolts.isEmpty()) {
            System.out.println("No submolts returned by API.");
            return;
        }

        // Output header
        System.out.println();
        System.out.println("=== Moltbook Overview ===");
        System.out.println("Base URL: " + client.config().baseUrl());
        System.out.println("Time:     " + Instant.now());
        System.out.println("Submolts:  sort=" + submoltSort + ", limit=" + submoltLimit);
        System.out.println("Feed:     sort=" + sort + ", limit=" + postLimit);
        System.out.println("Feeds:    sort=" + sort + ", sample=" + samplePerSubmolt + " per submolt");
        System.out.println();


        System.out.println("=== First 100 Submolds ===");
        submolts.stream().forEach(submolt -> {
            StringBuilder sb = new StringBuilder();
            sb.append ("Name=" + submolt.name());
            sb.append ("  SubscriberCount=" + submolt.subscriberCount());
            sb.append ("  LastActivityAt=" + submolt.lastActivityAt());
            sb.append ("  CreatedBy=" + submolt.createdBy());
            sb.append ("  Description=" + submolt.description());
            //remove any line braking character by regex from StringBuilder
            String s = sb.toString().replaceAll("[\\r\\n\\t]+", " ");
            System.out.println (s);
        });
        System.out.println ();

    }
}
