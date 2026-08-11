import java.awt.*;
import javax.swing.*;

public class PassengerMenuPanel extends JPanel {
    private MainFrame mainFrame;

    public PassengerMenuPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setBackground(MainFrame.MAIN_BG);
        setLayout(new BorderLayout());

        //  Top bar with Back button 
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        topBar.setOpaque(false);

        JButton backButton = new JButton("← Back");
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        backButton.setForeground(MainFrame.NAV_BTN_BG);
        backButton.setBackground(new Color(255, 255, 255, 0));
        backButton.setFocusPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        backButton.setContentAreaFilled(false);
        backButton.setOpaque(false);

        // Hover effect
        backButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                backButton.setBackground(MainFrame.NAV_BTN_BG);
                backButton.setForeground(Color.WHITE);
                backButton.setContentAreaFilled(true);
                backButton.setOpaque(true);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                backButton.setBackground(new Color(255, 255, 255, 0));
                backButton.setForeground(MainFrame.NAV_BTN_BG);
                backButton.setContentAreaFilled(false);
                backButton.setOpaque(false);
                backButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            }
        });

        backButton.addActionListener(e -> mainFrame.showPanel("Role")); 
        topBar.add(backButton);

        add(topBar, BorderLayout.NORTH);

        // ===== Center content (same style as RolePanel) =====
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);

        JLabel title = new JLabel("Start Here", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnCheckIn = createBigButton("Check-In");
        JButton btnTicket  = createBigButton("Ticket Status");

        btnCheckIn.addActionListener(e -> mainFrame.showCheckIn());
        btnTicket.addActionListener(e -> mainFrame.showTicketStatus());

        box.add(title);
        box.add(Box.createVerticalStrut(40));
        box.add(btnCheckIn);
        box.add(Box.createVerticalStrut(20));
        box.add(btnTicket);

        centerWrapper.add(box);
        add(centerWrapper, BorderLayout.CENTER);
    }

    private JButton createBigButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setPreferredSize(new Dimension(320, 60));
        btn.setMaximumSize(new Dimension(320, 60));
        btn.setBackground(MainFrame.NAV_BTN_BG);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }
}