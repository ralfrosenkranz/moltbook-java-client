package de.ralfrosenkranz.moltbook.demo.swing;

import de.ralfrosenkranz.moltbook.client.MoltbookClient;

import javax.swing.*;
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

        try {
            dumpOverview(cm.client(), Collections.EMPTY_MAP);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        MoltbookFrame f = new MoltbookFrame(cm, props);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setSize(1200, 800);
        f.setLocationRelativeTo(null);
        f.setVisible(true);

        // If no apiKey -> hint.
        if (cm.apiKey().isBlank()) {
            JOptionPane.showMessageDialog(f,
                    "No API key configured. Use the 'Settings / Register' tab to register an agent or paste an API key.\n\nConfig file: " + ConfigStore.configPath(),
                    "Moltbook Swing Client", JOptionPane.INFORMATION_MESSAGE);
        }
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
