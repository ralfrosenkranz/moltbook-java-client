package de.ralfrosenkranz.moltbook.demo.swing;

import de.ralfrosenkranz.moltbook.client.http.MoltbookApiException;

import javax.swing.*;
import java.util.concurrent.Callable;

/**
 * Runs API calls off the EDT and marshals results back to Swing.
 */
final class ApiExecutor {

    interface ResultHandler<T> {
        void onSuccess(T value);
        void onError(Throwable error);
    }

    static <T> void run(JComponent parentForDialogs,
                        String busyMessage,
                        Callable<T> call,
                        ResultHandler<T> handler) {

        JDialog busy = UiUtil.busyDialog(SwingUtilities.getWindowAncestor(parentForDialogs), busyMessage);

        SwingWorker<T, Void> worker = new SwingWorker<>() {
            @Override protected T doInBackground() throws Exception {
                return call.call();
            }
            @Override protected void done() {
                busy.dispose();
                try {
                    handler.onSuccess(get());
                } catch (Exception e) {
                    Throwable t = e.getCause() != null ? e.getCause() : e;
                    handler.onError(t);
                }
            }
        };
        worker.execute();
        busy.setVisible(true);
    }

    static void showError(JComponent parent, Throwable t) {
        String msg;
        if (t instanceof MoltbookApiException mae) {
            msg = "HTTP " + mae.statusCode() + "\n\n" + mae.getMessage();
        } else {
            msg = t.getClass().getSimpleName() + "\n\n" + t.getMessage();
        }
        JTextArea area = new JTextArea(msg, 10, 60);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(area);
        JOptionPane.showMessageDialog(parent, sp, "API error", JOptionPane.ERROR_MESSAGE);
    }
}
