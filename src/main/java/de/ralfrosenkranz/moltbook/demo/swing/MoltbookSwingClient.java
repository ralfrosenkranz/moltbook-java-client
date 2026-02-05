package de.ralfrosenkranz.moltbook.demo.swing;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;

/**
 * Swing demo client that exposes all functions available in moltbook-java-client.
 *
 * Package: demo.swing (subpackage of demo as requested).
 */
public final class MoltbookSwingClient {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            AppBootstrap.bootstrapAndShow();
        });
    }
}
