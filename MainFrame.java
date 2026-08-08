import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public static final Color SIDEBAR_BG = new Color(20, 42, 31);      
    public static final Color NAV_BTN_BG = new Color(32, 68, 50);      
    public static final Color MAIN_BG = new Color(245, 247, 245);       

    public MainFrame() {
        setTitle("Airport Queuing System - Management Console");
        setSize(1050, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel sidebar = new JPanel(new GridLayout(7, 1, 8, 8));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setBorder(BorderFactory.createEmptyBorder(15, 12, 15, 12));

        JLabel lblTitle = new JLabel("AIRPORT QUEUE", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(new Color(180, 225, 195));
        sidebar.add(lblTitle);

        JButton btnDash = createNavButton("Dashboard");
        JButton btnCheckin = createNavButton("Passenger Check-In");
        JButton btnTicket = createNavButton("Queue Ticket / Status");
        JButton btnStaff = createNavButton("Counter Staff Interface");
        JButton btnMonitor = createNavButton("Queue Monitoring");
        JButton btnReports = createNavButton("Reports & Analytics");

        sidebar.add(btnDash);
        sidebar.add(btnCheckin);
        sidebar.add(btnTicket);
        sidebar.add(btnStaff);
        sidebar.add(btnMonitor);
        sidebar.add(btnReports);

        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(MAIN_BG);

        mainPanel.add(new DashboardPanel(), "Dashboard");
        mainPanel.add(new CheckInPanel(), "CheckIn");
        mainPanel.add(new TicketStatusPanel(), "TicketStatus");
        mainPanel.add(new CounterStaffPanel(), "Staff");
        mainPanel.add(new QueueMonitoringPanel(), "Monitor");
        mainPanel.add(new ReportsPanel(), "Reports");

       
        btnDash.addActionListener(e -> cardLayout.show(mainPanel, "Dashboard"));
        btnCheckin.addActionListener(e -> cardLayout.show(mainPanel, "CheckIn"));
        btnTicket.addActionListener(e -> cardLayout.show(mainPanel, "TicketStatus"));
        btnStaff.addActionListener(e -> cardLayout.show(mainPanel, "Staff"));
        btnMonitor.addActionListener(e -> cardLayout.show(mainPanel, "Monitor"));
        btnReports.addActionListener(e -> cardLayout.show(mainPanel, "Reports"));

        setLayout(new BorderLayout());
        add(sidebar, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setBackground(NAV_BTN_BG);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(45, 90, 65), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}