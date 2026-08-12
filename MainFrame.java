import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel sidebar;

    private JButton btnDash, btnStaff, btnMonitor, btnReports, btnUsers, btnLogout, btnLaunchTV;

    public static final Color SIDEBAR_BG = new Color(20, 42, 31);
    public static final Color NAV_BTN_BG = new Color(32, 68, 50);
    public static final Color MAIN_BG = new Color(245, 247, 245);

    private String currentRole = null; // "STAFF" or "ADMIN"

    public MainFrame() {
        setTitle("Airport Queuing System");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // navbar
        sidebar = new JPanel(new GridLayout(8, 1, 8, 8));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setBorder(BorderFactory.createEmptyBorder(15, 12, 15, 12));

        JLabel lblTitle = new JLabel("AIRPORT QUEUE", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(new Color(180, 225, 195));
        sidebar.add(lblTitle);

        btnDash = createNavButton("Dashboard");
        btnStaff = createNavButton("Counter Staff Interface");
        btnMonitor = createNavButton("Queue Monitoring");
        btnReports = createNavButton("Reports & Analytics");
        btnUsers = createNavButton("User Management");
        btnLaunchTV = createNavButton("Launch TV Display");
        btnLogout = createNavButton("Logout");

        sidebar.add(btnDash);
        sidebar.add(btnStaff);
        sidebar.add(btnMonitor);
        sidebar.add(btnReports);
        sidebar.add(btnUsers);
        sidebar.add(btnLaunchTV);
        sidebar.add(btnLogout);

        // hide sidebar initially
        sidebar.setVisible(false);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(MAIN_BG);

        mainPanel.add(new RolePanel(this), "Role");
        mainPanel.add(new PassengerMenuPanel(this), "PassengerMenu");
        mainPanel.add(new LoginPanel(this), "Login");
        mainPanel.add(new DashboardPanel(), "Dashboard");
        mainPanel.add(new CheckInPanel(), "CheckIn");
        mainPanel.add(new TicketStatusPanel(), "TicketStatus");
        mainPanel.add(new CounterStaffPanel(), "Staff");
        mainPanel.add(new QueueMonitoringPanel(), "Monitor");
        mainPanel.add(new ReportsPanel(), "Reports");
        mainPanel.add(new UserManagementPanel(), "Users");
        mainPanel.add(new BaggageScannerPanel(), "Baggage");
        mainPanel.add(new SystemLogsPanel(), "Logs");

        btnDash.addActionListener(e -> cardLayout.show(mainPanel, "Dashboard"));
        btnStaff.addActionListener(e -> cardLayout.show(mainPanel, "Staff"));
        btnMonitor.addActionListener(e -> cardLayout.show(mainPanel, "Monitor"));
        btnReports.addActionListener(e -> cardLayout.show(mainPanel, "Reports"));
        btnUsers.addActionListener(e -> cardLayout.show(mainPanel, "Users"));
        btnLaunchTV.addActionListener(e -> new PublicDisplayBoardFrame().setVisible(true));
        btnLogout.addActionListener(e -> logout());

        setLayout(new BorderLayout());
        add(sidebar, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);

        cardLayout.show(mainPanel, "Role");
    }

    public void showPassengerMenu() {
        sidebar.setVisible(false);
        cardLayout.show(mainPanel, "PassengerMenu");
    }

    public void showLogin() {
        sidebar.setVisible(false);
        cardLayout.show(mainPanel, "Login");
    }

    public void showCheckIn() {
        sidebar.setVisible(false);
        cardLayout.show(mainPanel, "CheckIn");
    }

    public void showTicketStatus() {
        sidebar.setVisible(false);
        cardLayout.show(mainPanel, "TicketStatus");
    }

    public void loginSuccess(String role) {
        this.currentRole = role;
        applyRoleSidebar(role);
        sidebar.setVisible(true);
        cardLayout.show(mainPanel, "Dashboard");
        revalidate();
        repaint();
    }

    private void applyRoleSidebar(String role) {
        // always show these for logged-in users
        btnDash.setVisible(true);
        btnStaff.setVisible(true);
        btnMonitor.setVisible(true);
        btnLaunchTV.setVisible(true);
        btnLogout.setVisible(true);

        if ("ADMIN".equals(role)) {
            btnReports.setVisible(true);
            btnUsers.setVisible(true);
        } else {
            // STAFF
            btnReports.setVisible(false);
            btnUsers.setVisible(false);
        }
    }

    public void logout() {
        currentRole = null;
        sidebar.setVisible(false);
        cardLayout.show(mainPanel, "Role");
        revalidate();
        repaint();
    }

    public void backToRole() {
        logout();
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