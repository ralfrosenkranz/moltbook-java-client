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
    // Keep the same keys as used by ShyClient (shyclient.properties)
    static final String KEY_AGENT_NAME = "agentName";
    static final String KEY_FULL_AGENT_REGISTER_RESPONSE = "fullAgentRegisterResponse";
    static final String KEY_CLAIM_URL = "claimUrl";

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

    synchronized String agentName() {
        return props.getProperty(KEY_AGENT_NAME, "");
    }

    synchronized String fullAgentRegisterResponse() {
        return props.getProperty(KEY_FULL_AGENT_REGISTER_RESPONSE, "");
    }

    synchronized String claimUrl() {
        return props.getProperty(KEY_CLAIM_URL, "");
    }

    synchronized void updateSettings(String baseUrl, String apiKey) {
        if (baseUrl != null) props.setProperty(KEY_BASE_URL, baseUrl.trim());
        if (apiKey != null) props.setProperty(KEY_API_KEY, apiKey.trim());
        rebuild();
    }

    synchronized void storeRegistration(String baseUrl,
                                        String apiKey,
                                        String agentName,
                                        String fullAgentRegisterResponse,
                                        String claimUrl) {
        if (baseUrl != null) props.setProperty(KEY_BASE_URL, baseUrl.trim());
        if (apiKey != null) props.setProperty(KEY_API_KEY, apiKey.trim());
        if (agentName != null) props.setProperty(KEY_AGENT_NAME, agentName.trim());
        if (fullAgentRegisterResponse != null) props.setProperty(KEY_FULL_AGENT_REGISTER_RESPONSE, fullAgentRegisterResponse);
        if (claimUrl != null && !claimUrl.trim().isBlank()) props.setProperty(KEY_CLAIM_URL, claimUrl.trim());
        rebuild();
    }

    synchronized boolean isConfigComplete() {
        return !baseUrl().isBlank()
                && !apiKey().isBlank()
                && !agentName().isBlank()
                && !fullAgentRegisterResponse().isBlank();
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
