package de.ralfrosenkranz.moltbook.demo.swing;

import de.ralfrosenkranz.moltbook.client.MoltbookClient;
import de.ralfrosenkranz.moltbook.client.MoltbookClientConfig;

import java.util.Objects;
import java.util.Properties;

/**
 * Owns a MoltbookClient instance and rebuilds it when settings change.
 */
final class ClientManager {
    static final String KEY_BASE_URL = "baseUrl";
    static final String KEY_API_KEY = "apiKey";

    private final Properties props;
    private MoltbookClient client;

    ClientManager(Properties props) {
        this.props = Objects.requireNonNull(props, "props");
        rebuild();
    }

    synchronized MoltbookClient client() {
        return client;
    }

    synchronized String baseUrl() {
        return props.getProperty(KEY_BASE_URL, "https://www.moltbook.com/api/v1");
    }

    synchronized String apiKey() {
        return props.getProperty(KEY_API_KEY, "");
    }

    synchronized void updateSettings(String baseUrl, String apiKey) {
        if (baseUrl != null) props.setProperty(KEY_BASE_URL, baseUrl.trim());
        if (apiKey != null) props.setProperty(KEY_API_KEY, apiKey.trim());
        rebuild();
    }

    private void rebuild() {
        if (client != null) {
            try { client.close(); } catch (Exception ignored) {}
        }
        MoltbookClientConfig cfg = MoltbookClientConfig.builder()
                .baseUrl(baseUrl())
                .apiKey(apiKey().isBlank() ? null : apiKey())
                .build();
        client = MoltbookClient.builder().config(cfg).build();
    }
}
