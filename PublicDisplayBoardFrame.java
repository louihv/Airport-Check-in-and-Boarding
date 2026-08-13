import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

public class PublicDisplayBoardFrame extends JFrame {
    private JLabel lblCurrentTicket, lblCurrentCounter;
    private DefaultTableModel activeCountersModel;

    public PublicDisplayBoardFrame() {
        setTitle("Airport Waiting Area - Queue Display");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(new Color(15, 30, 22));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Big Header Announcement Banner
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        headerPanel.setOpaque(false);

        JLabel lblAnnounce = new JLabel("NOW SERVING", SwingConstants.CENTER);
        lblAnnounce.setFont(AppFonts.bold(28));
        lblAnnounce.setForeground(new Color(180, 225, 195));

        lblCurrentTicket = new JLabel("---", SwingConstants.CENTER);
        lblCurrentTicket.setFont(AppFonts.bold(72));
        lblCurrentTicket.setForeground(Color.YELLOW);

        lblCurrentCounter = new JLabel("Please proceed to your assigned counter", SwingConstants.CENTER);
        lblCurrentCounter.setFont(AppFonts.bold(20));
        lblCurrentCounter.setForeground(Color.WHITE);

        headerPanel.add(lblAnnounce);
        headerPanel.add(lblCurrentTicket);

        // Counter Status Overview Table
        String[] cols = {"Counter", "Serving Ticket", "Status"};
        activeCountersModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(activeCountersModel);
        table.setRowHeight(40);
        table.setFont(AppFonts.bold(18));
        table.getTableHeader().setFont(AppFonts.bold(18));
        table.getTableHeader().setBackground(new Color(32, 68, 50));
        table.getTableHeader().setForeground(Color.WHITE);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(25, 45, 35));

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(lblCurrentCounter, BorderLayout.CENTER);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);

        add(mainPanel);

        // Subscribe to Queue updates
        QueueManager.getInstance().addListener(this::refreshDisplay);
        refreshDisplay();
    }

    private void refreshDisplay() {
        activeCountersModel.setRowCount(0);
        Map<Integer, Passenger> active = QueueManager.getInstance().getActiveCounters();

        String latestCall = "---";
        String latestCounter = "Please proceed to your assigned counter";

        for (Map.Entry<Integer, Passenger> entry : active.entrySet()) {
            int counterId = entry.getKey();
            Passenger p = entry.getValue();

            if (p != null) {
                activeCountersModel.addRow(new Object[]{"Counter " + counterId, p.getTicketNumber(), "BUSY"});
                latestCall = p.getTicketNumber();
                latestCounter = "Ticket " + p.getTicketNumber() + " -> Proceed to Counter " + counterId;
            } else {
                activeCountersModel.addRow(new Object[]{"Counter " + counterId, "AVAILABLE", "OPEN"});
            }
        }

        lblCurrentTicket.setText(latestCall);
        lblCurrentCounter.setText(latestCounter);
    }
}