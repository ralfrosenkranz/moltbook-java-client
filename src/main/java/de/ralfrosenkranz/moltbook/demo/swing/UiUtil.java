package de.ralfrosenkranz.moltbook.demo.swing;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;

final class UiUtil {

    static boolean isFlatLafActive() {
        LookAndFeel laf = UIManager.getLookAndFeel();
        if (laf == null)
            return false;
        String cn = laf.getClass().getName();
        return cn.contains("flatlaf") || cn.contains("FlatLaf") || cn.contains("com.formdev.flatlaf");
    }

    static void stylePrimaryButton(AbstractButton b) {
        if (!isFlatLafActive() || b == null)
            return;
        b.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE);
        // Use the global accent color defined in MoltbookSwingClient.applyUiAccents().
        b.putClientProperty(FlatClientProperties.STYLE, "background: $Component.accentColor; foreground: #ffffff");
    }

    static void styleSecondaryButton(AbstractButton b) {
        if (!isFlatLafActive() || b == null)
            return;
        b.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE);
        b.putClientProperty(FlatClientProperties.STYLE, "font: -1");
    }

    static void styleDangerButton(AbstractButton b) {
        if (!isFlatLafActive() || b == null)
            return;
        b.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE);
        b.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
        // Slightly stronger affordance than outline-only.
        b.putClientProperty(FlatClientProperties.STYLE, "background: #ff453a; foreground: #ffffff");
    }

    static void styleToolbarButton(AbstractButton b) {
        if (!isFlatLafActive() || b == null)
            return;
        b.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
    }

    static JDialog busyDialog(Window owner, String message) {
        JDialog d = new JDialog(owner, "Working…", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        p.add(new JLabel(message), BorderLayout.NORTH);
        p.add(bar, BorderLayout.CENTER);
        d.setContentPane(p);
        d.pack();
        d.setLocationRelativeTo(owner);
        return d;
    }

    static JTextArea textArea(int rows, int cols) {
        JTextArea ta = new JTextArea(rows, cols);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        return ta;
    }

    static JPanel labeled(String label, JComponent c) {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.add(new JLabel(label), BorderLayout.WEST);
        p.add(c, BorderLayout.CENTER);
        return p;
    }
}
