import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardPanel extends JPanel {
    private JLabel lblTotalQueue, lblActiveCounters, lblTotalServed;
    private JTable flightSummaryTable;
    private DefaultTableModel tableModel;


    private static final Color CARD_BLUE_DARK  = new Color(12, 24, 58);   // Deep Navy
    private static final Color CARD_BLUE_MED   = new Color(17, 34, 80);   // Main Navy
    private static final Color CARD_BLUE_LIGHT = new Color(47, 70, 125);  // Fresh Blue

    public DashboardPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(MainFrame.MAIN_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    
        JPanel kpiPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        kpiPanel.setOpaque(false);

        lblTotalQueue = createKpiCard(kpiPanel, "Passengers in Queue", "0", CARD_BLUE_DARK);
        lblActiveCounters = createKpiCard(kpiPanel, "Active Counters", "4/4", CARD_BLUE_MED);
        lblTotalServed = createKpiCard(kpiPanel, "Served Today", "0", CARD_BLUE_LIGHT);

     
        String[] columns = {"Flight No.", "Waiting Passengers", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        flightSummaryTable = new JTable(tableModel);
        styleTable(flightSummaryTable);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(180, 200, 185), 1), 
            "Flight Queue Overview", 0, 0, (AppFonts.bold(12)), new Color(27, 77, 46)
        ));
        
        JScrollPane scrollPane = new JScrollPane(flightSummaryTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(kpiPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);

        QueueManager.getInstance().addListener(this::refreshData);
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
        QueueManager qm = QueueManager.getInstance();
        List<Passenger> all = qm.getAllPassengers();
        List<Passenger> waiting = qm.getWaitingQueue();

        lblTotalQueue.setText(String.valueOf(waiting.size()));
        long servedCount = all.stream().filter(p -> "COMPLETED".equals(p.getStatus())).count();
        lblTotalServed.setText(String.valueOf(servedCount));

        tableModel.setRowCount(0);
        Map<String, Integer> flightMap = new HashMap<>();
        for (Passenger p : waiting) {
            flightMap.put(p.getFlightNumber(), flightMap.getOrDefault(p.getFlightNumber(), 0) + 1);
        }

        for (Map.Entry<String, Integer> entry : flightMap.entrySet()) {
            tableModel.addRow(new Object[]{entry.getKey(), entry.getValue(), "Active Queue"});
        }
    }
}