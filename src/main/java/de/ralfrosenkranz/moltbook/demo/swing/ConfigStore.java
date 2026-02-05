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

    static Properties loadOrCreate() throws IOException {
        Path p = configPath();
        Properties props = new Properties();
        boolean changed = false;
        if (Files.exists(p)) {
            try (InputStream in = Files.newInputStream(p)) {
                props.load(in);
            }
        } else {
            Files.createDirectories(p.getParent());
            changed = true;
        }

        // Ensure the same keys exist as in shyclient.properties (even if blank)
        changed |= ensureKey(props, ClientManager.KEY_BASE_URL, "https://www.moltbook.com/api/v1");
        changed |= ensureKey(props, ClientManager.KEY_API_KEY, "");
        changed |= ensureKey(props, ClientManager.KEY_AGENT_NAME, "");
        changed |= ensureKey(props, ClientManager.KEY_FULL_AGENT_REGISTER_RESPONSE, "");
        changed |= ensureKey(props, ClientManager.KEY_CLAIM_URL, "");

        if (changed) {
            Files.createDirectories(p.getParent());
            try (OutputStream out = Files.newOutputStream(p, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                props.store(out, "Moltbook Swing client settings");
            }
        }
        return props;
    }

    private static boolean ensureKey(Properties props, String key, String defaultValue) {
        if (props.containsKey(key)) return false;
        props.setProperty(key, defaultValue == null ? "" : defaultValue);
        return true;
    }

    static void save(Properties props) throws IOException {
        Objects.requireNonNull(props, "props");
        Path p = configPath();
        Files.createDirectories(p.getParent());
        try (OutputStream out = Files.newOutputStream(p, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            props.store(out, "Moltbook Swing client settings");
        }
    }
}
