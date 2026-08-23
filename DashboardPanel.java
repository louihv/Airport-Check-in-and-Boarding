import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class DashboardPanel extends JPanel {
    private JLabel lblTotalQueue, lblActiveCounters, lblTotalServed;
    private JTable flightSummaryTable;
    private DefaultTableModel tableModel;
    private JButton btnRefresh;

    private static final Color CARD_BLUE_DARK = new Color(12, 24, 58);

    public DashboardPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(MainFrame.MAIN_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel kpiPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        kpiPanel.setOpaque(false);

        lblTotalQueue = createKpiCard(kpiPanel, "Passengers in Queue", "0", CARD_BLUE_DARK);
        lblActiveCounters = createKpiCard(kpiPanel, "Active Counters", "0", MainFrame.NAV_BTN_BG);
        lblTotalServed = createKpiCard(kpiPanel, "Served Today", "0", MainFrame.SECONDARY_BTN_BG);

        String[] columns = {"Flight No.", "Waiting Passengers", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        flightSummaryTable = new JTable(tableModel);
        styleTable(flightSummaryTable);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(180, 200, 185), 1),
            "", 0, 0, AppFonts.bold(12), new Color(27, 77, 46)
        ));

        JScrollPane scrollPane = new JScrollPane(flightSummaryTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        btnRefresh = new JButton("Refresh Dashboard");
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBackground(MainFrame.NAV_BTN_BG);
        btnRefresh.setForeground(Color.WHITE);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(btnRefresh);

        add(kpiPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> refreshData());
        refreshData();
    }

    private JLabel createKpiCard(JPanel parent, String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(new Color(230, 245, 235));
        lblTitle.setFont(AppFonts.bold(13));

        JLabel lblValue = new JLabel(value, SwingConstants.LEFT);
        lblValue.setForeground(Color.WHITE);
        lblValue.setFont(AppFonts.bold(32));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        parent.add(card);
        return lblValue;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(AppFonts.regular(12));
        table.getTableHeader().setFont(AppFonts.bold(12));
        table.getTableHeader().setBackground(new Color(225, 240, 230));
        table.getTableHeader().setForeground(new Color(20, 50, 30));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);
    }

    public void refreshData() {
        btnRefresh.setEnabled(false);

        new SwingWorker<Void, Void>() {
            private int totalTickets = 0;
            private int activeCounters = 0;
            private int servedCount = 0;
            private List<Map<String, String>> flights;
            private Map<String, Integer> waitingPerFlight = new HashMap<>();

            @Override
            protected Void doInBackground() throws Exception {
                List<Map<String, String>> tickets = FirebaseHelper.getAllTickets();
                totalTickets = tickets.size();

                for (Map<String, String> t : tickets) {
                    String status = t.get("status");
                    String flight = t.get("flightNo");

                    if ("COMPLETED".equalsIgnoreCase(status) || "SERVED".equalsIgnoreCase(status)) {
                        servedCount++;
                    } else {
                        if (flight != null && !flight.isEmpty()) {
                            waitingPerFlight.put(flight, waitingPerFlight.getOrDefault(flight, 0) + 1);
                        }
                    }
                }

                List<Object[]> users = FirebaseHelper.getAllUsers();
                activeCounters = 0;
                if (users != null) {
                    for (Object[] u : users) {
                        String role   = String.valueOf(u[1]);
                        String status = String.valueOf(u[3]);

                        if ("STAFF".equalsIgnoreCase(role) && "Online".equalsIgnoreCase(status)) {
                            activeCounters++;
                        }
                    }
                }

                flights = FirebaseHelper.getAllFlights();

                    return null;
                }

            @Override
            protected void done() {
                btnRefresh.setEnabled(true);
                try {
                    get();

                    lblTotalQueue.setText(String.valueOf(totalTickets));
                    lblActiveCounters.setText(activeCounters + "/4");
                    lblTotalServed.setText(String.valueOf(servedCount));

                    tableModel.setRowCount(0);

                    if (flights != null) {
                        for (Map<String, String> f : flights) {
                            String flightNo = f.getOrDefault("flightNo", "-");
                            String status = f.getOrDefault("status", "Unknown");
                            int waiting = waitingPerFlight.getOrDefault(flightNo, 0);

                            tableModel.addRow(new Object[]{
                                flightNo,
                                waiting,
                                status
                            });
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DashboardPanel.this,
                        "Failed to load dashboard data.\nCheck internet / Firebase URL.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        }.execute();
    }
}