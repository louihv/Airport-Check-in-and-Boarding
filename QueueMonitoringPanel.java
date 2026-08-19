import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class QueueMonitoringPanel extends JPanel {
    private JTable queueTable;
    private DefaultTableModel tableModel;
    private JButton btnRefresh;

    public QueueMonitoringPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(MainFrame.MAIN_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] columns = {"Ticket No.", "Booking Ref", "Name", "Flight", "Counter", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        queueTable = new JTable(tableModel);

        queueTable.setRowHeight(28);
        queueTable.setFont(AppFonts.regular(12));
        queueTable.getTableHeader().setFont(AppFonts.bold(12));
        queueTable.getTableHeader().setBackground(new Color(220, 238, 225));
        queueTable.getTableHeader().setForeground(new Color(20, 50, 30));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        queueTable.setDefaultRenderer(Object.class, centerRenderer);

        JScrollPane scrollPane = new JScrollPane(queueTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(180, 210, 190), 1),
            "All Tickets (Firebase)", 0, 0, AppFonts.bold(14), new Color(27, 77, 46)
        ));

        btnRefresh = new JButton("Refresh Tickets");
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBackground(MainFrame.NAV_BTN_BG);
        btnRefresh.setForeground(Color.WHITE);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(btnRefresh);

        add(scrollPane, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> loadTickets());
        loadTickets();
    }

    private void loadTickets() {
        btnRefresh.setEnabled(false);

        new SwingWorker<List<Map<String, String>>, Void>() {
            @Override
            protected List<Map<String, String>> doInBackground() throws Exception {
                return FirebaseHelper.getAllTickets();
            }

            @Override
            protected void done() {
                btnRefresh.setEnabled(true);
                try {
                    List<Map<String, String>> tickets = get();
                    tableModel.setRowCount(0);

                    if (tickets != null) {
                        for (Map<String, String> t : tickets) {
                            String counter = t.get("counter");
                            String counterDisplay = (counter == null || counter.equals("0") || counter.isEmpty())
                                    ? "Unassigned"
                                    : "Counter " + counter;

                            tableModel.addRow(new Object[]{
                                t.get("ticketNo"),
                                t.get("bookingRef"),
                                t.get("name"),
                                t.get("flightNo"),
                                counterDisplay,
                                t.get("status")
                            });
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(QueueMonitoringPanel.this,
                        "Failed to load tickets.\nCheck internet / Firebase URL.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        }.execute();
    }
}