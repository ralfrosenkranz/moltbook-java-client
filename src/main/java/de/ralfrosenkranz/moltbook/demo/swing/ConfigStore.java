package de.ralfrosenkranz.moltbook.demo.swing;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.Objects;
import java.util.Properties;

/**
 * Loads and stores Swing client settings in ~/.moltbook/swingclient.properties
 * (mirrors the ShyClient approach).
 */
final class ConfigStore {
    static final String CONFIG_DIR = ".moltbook";
    static final String CONFIG_FILE = "swingclient.properties";

    static Path configPath() {
        String home = System.getProperty("user.home");
        return Path.of(home, CONFIG_DIR, CONFIG_FILE);
    }

    /**
     * Loads settings from ~/.moltbook/swingclient.properties.
     *
     * Important: This method MUST NOT create or write the properties file.
     * The file is only created when we actually persist real data (e.g. after registration).
     */
    static Properties loadOrCreate() throws IOException {
        Path p = configPath();
        Properties props = new Properties();
        if (Files.exists(p)) {
            try (InputStream in = Files.newInputStream(p)) {
                props.load(in);
            }
        }
        // No auto-population here; defaults are applied in the UI/business logic.
        return props;
    }

    static void save(Properties props) throws IOException {
        Objects.requireNonNull(props, "props");
        Path p = configPath();
        Files.createDirectories(p.getParent());

        // When we DO save, we mirror ShyClient by ensuring the expected keys exist in the file.
        ensureKey(props, ClientManager.KEY_BASE_URL, "https://www.moltbook.com/api/v1");
        ensureKey(props, ClientManager.KEY_API_KEY, "");
        ensureKey(props, ClientManager.KEY_AGENT_NAME, "");
        ensureKey(props, ClientManager.KEY_FULL_AGENT_REGISTER_RESPONSE, "");
        ensureKey(props, ClientManager.KEY_CLAIM_URL, "");

        try (OutputStream out = Files.newOutputStream(p, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            props.store(out, "Moltbook Swing client settings");
        }
    }

    private static void ensureKey(Properties props, String key, String defaultValue) {
        if (props.containsKey(key)) return;
        props.setProperty(key, defaultValue == null ? "" : defaultValue);
    }
}
