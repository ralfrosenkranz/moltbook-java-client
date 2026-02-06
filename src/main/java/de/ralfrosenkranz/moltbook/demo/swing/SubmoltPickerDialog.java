package de.ralfrosenkranz.moltbook.demo.swing;

import de.ralfrosenkranz.moltbook.client.model.PaginatedSubmoltsResponse;
import de.ralfrosenkranz.moltbook.client.model.Submolt;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A large-list selector for Submolts with client-side search/filter and sortable table columns.
 *
 * The API (currently) offers pagination and a coarse server-side "sort" but no query/search parameter.
 * Therefore, this dialog supports:
 * - Load initial page + "Load more" pagination
 * - Search/filter in the currently loaded items
 * - Sort by clicking table headers (TableRowSorter)
 */
final class SubmoltPickerDialog extends JDialog {

    static String pickSubmoltName(JComponent parent, ClientManager cm) {
        Window owner = SwingUtilities.getWindowAncestor(parent);
        SubmoltPickerDialog d = new SubmoltPickerDialog(owner, cm);
        d.setLocationRelativeTo(parent);
        d.setVisible(true);
        Submolt picked = d.selected;
        if (picked == null) return null;
        String name = picked.name();
        if (name != null && !name.isBlank()) return name;
        String id = picked.id();
        return (id == null || id.isBlank()) ? null : id;
    }

    private final ClientManager cm;
    private final SubmoltTableModel model = new SubmoltTableModel();
    private final JTable table = new JTable(model);
    private final TableRowSorter<SubmoltTableModel> sorter = new TableRowSorter<>(model);

    private final JComboBox<String> serverSort = new JComboBox<>(new String[]{"hot", "new", "top"});
    private final JTextField search = new JTextField("", 24);
    private final JLabel status = new JLabel(" ");
    private final JButton load = new JButton("Load");
    private final JButton loadMore = new JButton("Load more");
    private final JButton ok = new JButton("OK");
    private final JButton cancel = new JButton("Cancel");

    private int offset = 0;
    private Integer total = null;
    private final int pageSize = 200;
    private Submolt selected = null;

    private SubmoltPickerDialog(Window owner, ClientManager cm) {
        super(owner, "Choose Submolt", ModalityType.APPLICATION_MODAL);
        this.cm = cm;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new GridBagLayout());
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        int r = 0;
        gc.gridx = 0; gc.gridy = r; gc.weightx = 0;
        top.add(new JLabel("Server sort"), gc);
        gc.gridx = 1; gc.weightx = 0;
        top.add(serverSort, gc);
        gc.gridx = 2; gc.weightx = 0;
        top.add(load, gc);
        gc.gridx = 3; gc.weightx = 0;
        top.add(loadMore, gc);
        r++;

        gc.gridx = 0; gc.gridy = r; gc.weightx = 0;
        top.add(new JLabel("Search"), gc);
        gc.gridx = 1; gc.weightx = 1; gc.gridwidth = 3;
        top.add(search, gc);

        add(top, BorderLayout.NORTH);

        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(false); // we already set one
        table.getSelectionModel().addListSelectionListener(e -> updateOkState());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && ok.isEnabled()) {
                    doOk();
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        add(sp, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(10, 10));
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        bottom.add(status, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancel);
        buttons.add(ok);
        bottom.add(buttons, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        load.addActionListener(e -> loadInitial());
        loadMore.addActionListener(e -> loadNextPage());

        cancel.addActionListener(e -> {
            selected = null;
            dispose();
        });

        ok.addActionListener(e -> doOk());
        ok.setEnabled(false);
        loadMore.setEnabled(false);

        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        setPreferredSize(new Dimension(900, 600));
        pack();

        // initial load
        loadInitial();
    }

    private void updateOkState() {
        ok.setEnabled(table.getSelectedRow() >= 0);
    }

    private void doOk() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return;
        int modelRow = table.convertRowIndexToModel(viewRow);
        selected = model.getAt(modelRow);
        dispose();
    }

    private void applyFilter() {
        String q = search.getText();
        String needle = q == null ? "" : q.trim().toLowerCase();
        if (needle.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(new RowFilter<>() {
                @Override public boolean include(Entry<? extends SubmoltTableModel, ? extends Integer> entry) {
                    Submolt s = entry.getModel().getAt(entry.getIdentifier());
                    return containsIgnoreCase(s.name(), needle)
                            || containsIgnoreCase(s.displayName(), needle)
                            || containsIgnoreCase(s.description(), needle);
                }
            });
        }
        updateStatusLabel();
    }

    private static boolean containsIgnoreCase(String haystack, String needleLower) {
        if (haystack == null) return false;
        return haystack.toLowerCase().contains(needleLower);
    }

    private void loadInitial() {
        offset = 0;
        total = null;
        model.clear();
        updateStatusLabel();
        loadNextPage();
    }

    private void loadNextPage() {
        String sort = (String) serverSort.getSelectedItem();
        int currentOffset = offset;

        load.setEnabled(false);
        loadMore.setEnabled(false);

        ApiExecutor.run((JComponent) getContentPane(), "Loading submolts…", () ->
                        cm.client().getSubmoltsApi().getSubmolts(sort, pageSize, currentOffset),
                new ApiExecutor.ResultHandler<>() {
                    @Override public void onSuccess(PaginatedSubmoltsResponse value) {
                        List<Submolt> items = value == null ? null : value.items();
                        if (items != null && !items.isEmpty()) {
                            model.addAll(items);
                            offset = currentOffset + items.size();
                        }
                        total = value == null ? total : value.total();
                        applyFilter();
                        load.setEnabled(true);
                        loadMore.setEnabled(total == null || offset < total);
                    }

                    @Override public void onError(Throwable error) {
                        load.setEnabled(true);
                        loadMore.setEnabled(total == null || offset < (total == null ? Integer.MAX_VALUE : total));
                        ApiExecutor.showError((JComponent) getContentPane(), error);
                    }
                });
    }

    private void updateStatusLabel() {
        int loaded = model.getRowCount();
        int visible = sorter.getViewRowCount();
        String totalText = (total == null) ? "?" : String.valueOf(total);
        status.setText("Loaded " + loaded + " / " + totalText + "   (showing " + visible + ")");
    }

    private static final class SubmoltTableModel extends AbstractTableModel {
        private final List<Submolt> items = new ArrayList<>();
        private final String[] cols = new String[]{"name", "display", "subscribers", "last activity"};

        void clear() {
            items.clear();
            fireTableDataChanged();
        }

        void addAll(List<Submolt> more) {
            int start = items.size();
            items.addAll(more);
            fireTableRowsInserted(start, items.size() - 1);
        }

        Submolt getAt(int row) {
            return items.get(row);
        }

        @Override public int getRowCount() { return items.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int column) { return cols[column]; }

        @Override public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 2) return Integer.class;
            return String.class;
        }

        @Override public Object getValueAt(int rowIndex, int columnIndex) {
            Submolt s = items.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> nullToEmpty(s.name());
                case 1 -> nullToEmpty(s.displayName());
                case 2 -> s.subscriberCount();
                case 3 -> nullToEmpty(s.lastActivityAt());
                default -> "";
            };
        }

        private static String nullToEmpty(String s) { return s == null ? "" : s; }
    }
}
