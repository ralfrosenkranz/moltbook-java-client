package de.ralfrosenkranz.moltbook.demo.swing;

import javax.swing.*;
import java.awt.*;

final class UiUtil {

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
