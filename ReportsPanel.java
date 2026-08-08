import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ReportsPanel extends JPanel {
    private JLabel lblAvgWait, lblTotalHandled;
    private DefaultTableModel performanceModel;

    public ReportsPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(MainFrame.MAIN_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        
        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        statsPanel.setOpaque(false);

        lblAvgWait = createSummaryCard(statsPanel, "Avg Wait Time", "~3.5 mins", new Color(27, 77, 46));
        lblTotalHandled = createSummaryCard(statsPanel, "Total Processed", "0 Passengers", new Color(46, 125, 50));

       
        String[] columns = {"Counter ID", "Completed Passengers", "Skipped Passengers", "Efficiency Rate"};
        performanceModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(performanceModel);
        
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(220, 238, 225));
        table.getTableHeader().setForeground(new Color(20, 50, 30));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(180, 210, 190), 1),
            "Counter Efficiency & Operational Metrics", 0, 0, new Font("Segoe UI", Font.BOLD, 14), new Color(27, 77, 46)
        ));

        add(statsPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        QueueManager.getInstance().addListener(this::refreshAnalytics);
        refreshAnalytics();
    }

    private JLabel createSummaryCard(JPanel parent, String title, String val, Color color) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(new Color(230, 245, 235));
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JLabel lblVal = new JLabel(val, SwingConstants.LEFT);
        lblVal.setForeground(Color.WHITE);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 22));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblVal, BorderLayout.CENTER);
        parent.add(card);
        return lblVal;
    }

    private void refreshAnalytics() {
        List<Passenger> all = QueueManager.getInstance().getAllPassengers();
        long totalCompleted = all.stream().filter(p -> "COMPLETED".equals(p.getStatus())).count();
        lblTotalHandled.setText(totalCompleted + " Passengers");

        performanceModel.setRowCount(0);
        for (int i = 1; i <= 4; i++) {
            final int cId = i;
            long completed = all.stream().filter(p -> p.getAssignedCounter() == cId && "COMPLETED".equals(p.getStatus())).count();
            long skipped = all.stream().filter(p -> p.getAssignedCounter() == cId && "SKIPPED".equals(p.getStatus())).count();
            long totalCounter = completed + skipped;
            String rate = (totalCounter == 0) ? "100%" : String.format("%.1f%%", ((double) completed / totalCounter) * 100);

            performanceModel.addRow(new Object[]{"Counter " + i, completed, skipped, rate});
        }
    }
}
