import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class SidebarPanel extends JPanel {

    public static final Color SIDEBAR_BG = new Color(20, 42, 31);
    public static final Color NAV_BTN_BG = new Color(32, 68, 50);
    public static final Color ACCENT     = new Color(180, 225, 195);

    private static final int EXPANDED_WIDTH  = 220;
    private static final int COLLAPSED_WIDTH = 62;
    private static final int ICON_SIZE       = 18;

    private boolean collapsed = false;
    private final JPanel contentPanel;
    private final JButton toggleBtn;
    private final JLabel titleLabel;

    private final JButton btnDash;
    private final JButton btnCheckin;
    private final JButton btnTicket;
    private final JButton btnStaff;
    private final JButton btnMonitor;
    private final JButton btnReports;

    public SidebarPanel() {
        setLayout(new BorderLayout());
        setBackground(SIDEBAR_BG);
        setPreferredSize(new Dimension(EXPANDED_WIDTH, 0));
        setBorder(new DropShadowBorder());

        toggleBtn = new JButton();
        toggleBtn.setIcon(FontIcon.of(FontAwesomeSolid.BARS, 20, ACCENT));
        toggleBtn.setBackground(SIDEBAR_BG);
        toggleBtn.setBorder(BorderFactory.createEmptyBorder(14, 0, 10, 0));
        toggleBtn.setFocusPainted(false);
        toggleBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleBtn.setContentAreaFilled(false);
        toggleBtn.addActionListener(e -> toggle());

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(SIDEBAR_BG);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 15, 10));

        titleLabel = new JLabel("AIRPORT QUEUE", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(ACCENT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        contentPanel.add(titleLabel);

        btnDash    = createNavButton("Dashboard", FontAwesomeSolid.TACHOMETER_ALT);
        btnCheckin = createNavButton("Passenger Check-In", FontAwesomeSolid.USER_CHECK);
        btnTicket  = createNavButton("Queue Ticket / Status", FontAwesomeSolid.TICKET_ALT);
        btnStaff   = createNavButton("Counter Staff Interface", FontAwesomeSolid.USER_TIE);
        btnMonitor = createNavButton("Queue Monitoring", FontAwesomeSolid.DESKTOP);
        btnReports = createNavButton("Reports & Analytics", FontAwesomeSolid.CHART_BAR);

        contentPanel.add(btnDash);
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(btnCheckin);
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(btnTicket);
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(btnStaff);
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(btnMonitor);
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(btnReports);

        add(toggleBtn, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }

    public void addDashboardListener(ActionListener l) { btnDash.addActionListener(l); }
    public void addCheckInListener(ActionListener l)   { btnCheckin.addActionListener(l); }
    public void addTicketListener(ActionListener l)    { btnTicket.addActionListener(l); }
    public void addStaffListener(ActionListener l)     { btnStaff.addActionListener(l); }
    public void addMonitorListener(ActionListener l)   { btnMonitor.addActionListener(l); }
    public void addReportsListener(ActionListener l)   { btnReports.addActionListener(l); }

    public boolean isCollapsed() { return collapsed; }

    public void toggle() {
        collapsed = !collapsed;

        if (collapsed) {
            setPreferredSize(new Dimension(COLLAPSED_WIDTH, getHeight()));
            titleLabel.setVisible(false);

            hideText(btnDash);
            hideText(btnCheckin);
            hideText(btnTicket);
            hideText(btnStaff);
            hideText(btnMonitor);
            hideText(btnReports);

            toggleBtn.setIcon(FontIcon.of(FontAwesomeSolid.ANGLE_DOUBLE_RIGHT, 18, ACCENT));
        } else {
            setPreferredSize(new Dimension(EXPANDED_WIDTH, getHeight()));
            titleLabel.setVisible(true);

            restoreText(btnDash,    "Dashboard");
            restoreText(btnCheckin, "Passenger Check-In");
            restoreText(btnTicket,  "Queue Ticket / Status");
            restoreText(btnStaff,   "Counter Staff Interface");
            restoreText(btnMonitor, "Queue Monitoring");
            restoreText(btnReports, "Reports & Analytics");

            toggleBtn.setIcon(FontIcon.of(FontAwesomeSolid.BARS, 20, ACCENT));
        }

        revalidate();
        if (getParent() != null) {
            getParent().revalidate();
            getParent().repaint();
        }
    }

    private void hideText(JButton btn) {
        btn.setText("");
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setToolTipText(getFullName(btn));
    }

    private void restoreText(JButton btn, String text) {
        btn.setText("  " + text);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setToolTipText(null);
    }

    private String getFullName(JButton btn) {
        if (btn == btnDash)    return "Dashboard";
        if (btn == btnCheckin) return "Passenger Check-In";
        if (btn == btnTicket)  return "Queue Ticket / Status";
        if (btn == btnStaff)   return "Counter Staff Interface";
        if (btn == btnMonitor) return "Queue Monitoring";
        if (btn == btnReports) return "Reports & Analytics";
        return "";
    }

    private JButton createNavButton(String text, FontAwesomeSolid iconCode) {
        JButton btn = new JButton("  " + text);
        btn.setIcon(FontIcon.of(iconCode, ICON_SIZE, Color.WHITE));
        btn.setIconTextGap(10);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setBackground(NAV_BTN_BG);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 90, 65), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static class DropShadowBorder extends AbstractBorder {
        private static final int SHADOW_WIDTH = 6;

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            for (int i = 0; i < SHADOW_WIDTH; i++) {
                float alpha = 0.18f * (1f - (float) i / SHADOW_WIDTH);
                g2.setColor(new Color(0, 0, 0, (int) (alpha * 255)));
                g2.drawLine(x + w - SHADOW_WIDTH + i, y,
                        x + w - SHADOW_WIDTH + i, y + h);
            }
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(0, 0, 0, SHADOW_WIDTH);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
}