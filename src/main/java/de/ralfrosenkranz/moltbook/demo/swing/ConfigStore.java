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
        if (Files.exists(p)) {
            try (InputStream in = Files.newInputStream(p)) {
                props.load(in);
            }
        } else {
            Files.createDirectories(p.getParent());
            // create empty file
            try (OutputStream out = Files.newOutputStream(p, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                props.store(out, "Moltbook Swing client settings");
            }
        }
        return props;
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
