package de.ralfrosenkranz.moltbook.demo.swing;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;

import javax.swing.*;


/**
 * Swing demo client that exposes all functions available in moltbook-java-client.
 *
 * Package: demo.swing (subpackage of demo as requested).
 */
public final class MoltbookSwingClient {

    // Toggle Look & Feel here:
    private static final boolean USE_FLATLAF_MACOS_DARK = true;


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                if (USE_FLATLAF_MACOS_DARK) {
                    FlatMacDarkLaf.setup();
                } else {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
            } catch (Exception ignored) {}
            AppBootstrap.bootstrapAndShow();
        });
    }
}
