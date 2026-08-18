import java.awt.*;
import java.net.URL;
import javax.swing.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel sidebar;

    private JButton btnDash, btnStaff, btnMonitor, btnReports, btnUsers, btnLogout, btnLaunchTV;

    public static final Color SIDEBAR_BG = new Color(20, 42, 31);
    public static final Color NAV_BTN_BG = new Color(17, 34, 80);
    public static final Color SECONDARY_BTN_BG = new Color(60, 81, 126);
    public static final Color MAIN_BG = new Color(244, 239, 233);


    private String currentRole = null; // "STAFF" or "ADMIN"
    private String currentUsername = null;

    public MainFrame() {
        setTitle("Airport Queuing System");
        setMinimumSize(new Dimension(1000, 650));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // navbar
        sidebar = new JPanel(new GridLayout(8, 1, 8, 8));
        sidebar.setBackground(NAV_BTN_BG);
        sidebar.setBorder(BorderFactory.createEmptyBorder(15, 12, 15, 12));

        JLabel lblTitle = new JLabel("Orion Skyways", SwingConstants.CENTER);
        lblTitle.setFont(AppFonts.bold(15));
        lblTitle.setForeground(new Color(180, 225, 195));
        sidebar.add(lblTitle);

        btnDash= createNavButton("Dashboard","https://img.icons8.com/ios-filled/50/ffffff/dashboard.png");
        btnStaff= createNavButton("Counter Staff Interface", "https://img.icons8.com/ios-filled/50/ffffff/conference-call.png");
        btnMonitor= createNavButton("Queue Monitoring","https://img.icons8.com/ios-filled/50/ffffff/monitor.png");
        btnReports= createNavButton("Reports & Analytics","https://img.icons8.com/ios-filled/50/ffffff/combo-chart.png");
        btnUsers= createNavButton("User Management","https://img.icons8.com/ios-filled/50/ffffff/group-foreground-selected.png");
        btnLaunchTV= createNavButton("Launch TV Display","https://img.icons8.com/ios-filled/50/ffffff/tv.png");
        btnLogout= createNavButton("Logout","https://img.icons8.com/ios-filled/50/ffffff/exit.png");

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
        mainPanel.add(new CheckInPanel(this), "CheckIn");
        mainPanel.add(new TicketStatusPanel(this), "TicketStatus");
        mainPanel.add(new CounterStaffPanel(this), "Staff");
        mainPanel.add(new QueueMonitoringPanel(), "Monitor");
        mainPanel.add(new ReportsPanel(), "Reports");
        mainPanel.add(new UserManagementPanel(), "Users");
        mainPanel.add(new BaggageScannerPanel(this), "Baggage");
        mainPanel.add(new SystemLogsPanel(), "Logs");

        btnDash.addActionListener(e -> cardLayout.show(mainPanel, "Dashboard"));
        btnStaff.addActionListener(e -> cardLayout.show(mainPanel, "Staff"));
        btnMonitor.addActionListener(e -> cardLayout.show(mainPanel, "Monitor"));
        btnLaunchTV.addActionListener(e -> new PublicDisplayBoardFrame().setVisible(true));
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

    public void showBaggage() {
        sidebar.setVisible(false);
        cardLayout.show(mainPanel, "Baggage");
    }

    public void showTicketStatus() {
        sidebar.setVisible(false);
        cardLayout.show(mainPanel, "TicketStatus");
    }

    public void loginSuccess(String role, String username) {
        this.currentRole = role;
        this.currentUsername = username;
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

    public String getCurrentUsername() {
        return currentUsername;
    }

    public void logout() {
        // mark staff offline when logging out
        if (currentUsername != null) {
            new Thread(() -> {
                try {
                    FirebaseHelper.setStaffOffline(currentUsername);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

        currentRole = null;
        currentUsername = null;
        sidebar.setVisible(false);
        cardLayout.show(mainPanel, "Role");
        revalidate();
        repaint();
    }

    public void backToRole() {
        logout();
    }

    private JButton createNavButton(String text, String iconUrl) {
        JButton btn = new JButton(text);

        // Load & scale icon (24×24)
        try {
            ImageIcon original = new ImageIcon(new URL(iconUrl));
            Image scaled = original.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(scaled));
            btn.setIconTextGap(12);            
            btn.setHorizontalAlignment(SwingConstants.LEFT);
        } catch (Exception e) {
            System.err.println("Could not load icon: " + iconUrl);
            // button still works without icon
        }
    btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);

        btn.setForeground(Color.WHITE);
        btn.setFont(AppFonts.regular(12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Subtle border that still looks good on dark sidebar
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(45, 90, 65), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        // Optional: hover effect (slightly brighter border)
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(100, 180, 130), 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(45, 90, 65), 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
        });

        return btn;
    }

    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
    }

    public static void main(String[] args) {
        AppFonts.loadFonts();
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
    
}

