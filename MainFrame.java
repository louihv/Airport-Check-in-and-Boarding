import java.awt.*;
import java.net.URL;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.geom.RoundRectangle2D;
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

    private JLabel lblUsername;
    private JLabel lblRole;

    public MainFrame() {
        setTitle("Airport Queuing System");
        setMinimumSize(new Dimension(1000, 650));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SECONDARY_BTN_BG);
        sidebar.setBorder(BorderFactory.createEmptyBorder(0,0,15,0));
        sidebar.setPreferredSize(new Dimension(240, Integer.MAX_VALUE));
        sidebar.setMinimumSize(new Dimension(220, 0));
        sidebar.setMaximumSize(new Dimension(260, Integer.MAX_VALUE));

        JPanel userBox = new JPanel();
        userBox.setLayout(new BoxLayout(userBox, BoxLayout.Y_AXIS));
        userBox.setBackground(new Color(0x2D3E63));
        userBox.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        userBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        userBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        JLabel logoLabel = new JLabel();
        try {
            java.net.URL logoUrl = getClass().getResource("/resources/logo.png");
            if (logoUrl != null) {
                ImageIcon original = new ImageIcon(logoUrl);
                Image scaled = original.getImage().getScaledInstance(
                    120, 60, Image.SCALE_SMOOTH
                );
                logoLabel.setIcon(new ImageIcon(scaled));
            }
        } catch (Exception e) {
            System.err.println("Could not load logo: " + e.getMessage());
        }

        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel("Orion Skyways");
        lblTitle.setFont(AppFonts.bold(15));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblUsername = new JLabel("Username");
        lblUsername.setFont(AppFonts.regular(12));
        lblUsername.setForeground(new Color(210, 220, 235));
        lblUsername.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblRole = new JLabel("ADMIN");
        lblRole.setFont(AppFonts.bold(10));
        lblRole.setForeground(new Color(180, 225, 195));
        lblRole.setAlignmentX(Component.CENTER_ALIGNMENT);

        userBox.add(logoLabel);
        userBox.add(Box.createVerticalStrut(5));
        userBox.add(lblTitle);
        userBox.add(Box.createVerticalStrut(6));
        userBox.add(lblUsername);
        userBox.add(Box.createVerticalStrut(3));
        userBox.add(lblRole);
        userBox.add(Box.createVerticalStrut(15));
        sidebar.add(userBox);
        
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setOpaque(false);
        navPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        navPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
       
        btnDash = createNavButton("Dashboard","https://img.icons8.com/ios-filled/50/ffffff/dashboard.png");
        btnStaff = createNavButton("Staff Counter","https://img.icons8.com/ios-filled/50/ffffff/conference-call.png");
        btnMonitor = createNavButton("Queue Monitoring","https://img.icons8.com/ios-filled/50/ffffff/monitor.png");
        btnUsers = createNavButton("User Management","https://img.icons8.com/ios-filled/50/ffffff/group-foreground-selected.png");
        btnLaunchTV = createNavButton("Launch TV Display","https://img.icons8.com/ios-filled/50/ffffff/tv.png");
        btnLogout = createNavButton("Logout","https://img.icons8.com/ios-filled/50/ffffff/exit.png");
        navPanel.add(btnDash);
        navPanel.add(Box.createVerticalStrut(6));
        navPanel.add(btnStaff);
        navPanel.add(Box.createVerticalStrut(6));
        navPanel.add(btnMonitor);
        navPanel.add(Box.createVerticalStrut(6));
        navPanel.add(btnUsers);
        navPanel.add(Box.createVerticalStrut(6));
        navPanel.add(btnLaunchTV);

        sidebar.setVisible(false);
        sidebar.add(navPanel);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(btnLogout);

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
        mainPanel.add(new UserManagementPanel(), "Users");
        mainPanel.add(new BaggageScannerPanel(this), "Baggage");
        mainPanel.add(new SystemLogsPanel(), "Logs");

        btnDash.addActionListener(e -> cardLayout.show(mainPanel, "Dashboard"));
        btnStaff.addActionListener(e -> cardLayout.show(mainPanel, "Staff"));
        btnMonitor.addActionListener(e -> cardLayout.show(mainPanel, "Monitor"));
        btnLaunchTV.addActionListener(e -> new PublicDisplayBoardFrame().setVisible(true));
        btnUsers.addActionListener(e -> cardLayout.show(mainPanel, "Users"));
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
        lblUsername.setText(username);
        lblRole.setText(role);
        applyRoleSidebar(role);
        sidebar.setVisible(true);
        cardLayout.show(mainPanel, "Dashboard");
       
        revalidate();
        repaint();
    }

    private void applyRoleSidebar(String role) {
        btnDash.setVisible(true);
        btnStaff.setVisible(true);
        btnMonitor.setVisible(true);
        btnLaunchTV.setVisible(true);
        btnLogout.setVisible(true);
        // if ("ADMIN".equals(role)) {
        // btnReports.setVisible(true);
        // btnUsers.setVisible(true);
        // } else {
        // // STAFF
        // btnReports.setVisible(false);
        // btnUsers.setVisible(false);
        // }
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public String getCurrentRole() {
        return currentRole;
    }

    public void logout() {
        if (currentUsername != null) {
            new Thread(() -> {
                try {
                    FirebaseHelper.setOffline(currentUsername);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

        currentRole = null;
        currentUsername = null;
        lblUsername.setText("Username");
        lblRole.setText("ADMIN");
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
    try {
        ImageIcon original = new ImageIcon(new URL(iconUrl));
        Image scaled = original.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        btn.setIcon(new ImageIcon(scaled));
        btn.setIconTextGap(12);
    } catch (Exception e) {
        System.err.println("Could not load icon: " + iconUrl);
    }


    btn.setOpaque(false);
    btn.setContentAreaFilled(false);
    btn.setFocusPainted(false);
    btn.setBorderPainted(false);
    btn.setForeground(Color.WHITE);
    btn.setFont(AppFonts.regular(12));
    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

    btn.setHorizontalAlignment(SwingConstants.LEFT);
    btn.setHorizontalTextPosition(SwingConstants.RIGHT); 
    btn.setVerticalAlignment(SwingConstants.CENTER);

    btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
    btn.setMargin(new Insets(0, 0, 0, 0));

    btn.setAlignmentX(Component.LEFT_ALIGNMENT);
    btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
    btn.setPreferredSize(new Dimension(240, 48));
    btn.setMinimumSize(new Dimension(200, 48));
    return btn;
    }

    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
    }

    public static void main(String[] args) {
        AppFonts.loadFonts();
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
   
    private static class RoundedOutlineBorder extends AbstractBorder {
        private final float thickness;
        private final Color color;
        private final int radius;
        public RoundedOutlineBorder(float thickness, Color color, int radius) {
            this.thickness = thickness;
            this.color = color;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.draw(new RoundRectangle2D.Float(
                    x + thickness / 2f,
                    y + thickness / 2f,
                    width - thickness,
                    height - thickness,
                    radius, radius));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 3, radius / 2, radius / 3, radius / 2);
        }
    }
}