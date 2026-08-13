import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class QueueMonitoringPanel extends JPanel {
    private JTable queueTable;
    private DefaultTableModel tableModel;

    public QueueMonitoringPanel() {
        setLayout(new BorderLayout());
        setBackground(MainFrame.MAIN_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] columns = {"Ticket No.", "Booking Ref", "Name", "Flight", "Counter", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
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
            "Live System Queue Monitor", 0, 0, (AppFonts.bold(14)), new Color(27, 77, 46)
        ));

        add(scrollPane, BorderLayout.CENTER);
        QueueManager.getInstance().addListener(this::refreshTable);
        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Passenger> passengers = QueueManager.getInstance().getAllPassengers();

        for (Passenger p : passengers) {
            tableModel.addRow(new Object[]{
                p.getTicketNumber(),
                p.getBookingRef(),
                p.getName(),
                p.getFlightNumber(),
                p.getAssignedCounter() == 0 ? "Unassigned" : "Counter " + p.getAssignedCounter(),
                p.getStatus()
            });
        }
    }
}