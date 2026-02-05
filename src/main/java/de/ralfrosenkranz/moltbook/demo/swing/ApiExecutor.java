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
        StringBuilder sb = new StringBuilder();

        if (t instanceof MoltbookApiException mae) {
            sb.append("HTTP ").append(mae.statusCode()).append("\n\n");
            sb.append(mae.getMessage() == null ? "" : mae.getMessage());

            // Requirement: if a 401 error happens, show the auth error and the stored registration JSON.
            if (mae.statusCode() == 401) {
                sb.append("\n\n---\nStored fullAgentRegisterResponse (from ")
                        .append(ConfigStore.configPath().toAbsolutePath())
                        .append("):\n\n");
                try {
                    var props = ConfigStore.loadOrCreate();
                    String stored = props.getProperty(ClientManager.KEY_FULL_AGENT_REGISTER_RESPONSE, "");
                    if (stored == null || stored.trim().isEmpty()) {
                        sb.append("(no stored fullAgentRegisterResponse found)");
                    } else {
                        sb.append(stored);
                    }
                } catch (Exception ex) {
                    sb.append("(failed to load stored registration response: ")
                            .append(ex.getMessage())
                            .append(")");
                }
            }
        } else {
            sb.append(t.getClass().getSimpleName()).append("\n\n");
            sb.append(t.getMessage() == null ? "" : t.getMessage());
        }

        JTextArea area = new JTextArea(sb.toString(), 16, 80);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(area);
        JOptionPane.showMessageDialog(parent, sp, "API error", JOptionPane.ERROR_MESSAGE);
    }
}
