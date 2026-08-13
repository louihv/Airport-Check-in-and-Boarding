import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SystemLogsPanel extends JPanel {
    private DefaultTableModel logModel;

    public SystemLogsPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(MainFrame.MAIN_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("System Audit Trail & Operations Log");
        title.setFont(AppFonts.bold(20));
        title.setForeground(new Color(27, 77, 46));

        String[] cols = {"Timestamp", "Event Type", "Description"};
        logModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(logModel);
        table.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);

        add(title, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Initial system event
        addLog("SYSTEM_INIT", "System Audit Panel initialized.");
    }

    public void addLog(String eventType, String description) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        logModel.insertRow(0, new Object[]{timestamp, eventType, description});
    }
}