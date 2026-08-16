import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.AbstractBorder;

public class PassengerMenuPanel extends JPanel {

    private final MainFrame mainFrame;

    public PassengerMenuPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setBackground(MainFrame.MAIN_BG);
        setLayout(new BorderLayout());

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        topBar.setOpaque(false);

        JButton backButton = createBackButton("/resources/back.png");
        backButton.addActionListener(e -> mainFrame.showPanel("Role"));
        topBar.add(backButton);

        add(topBar, BorderLayout.NORTH);

        //  MAIN CONTENT (Left text + Right buttons) 
        JPanel content = new JPanel(new GridLayout(1, 2, 500, 0)); 
        content.setOpaque(false);
        
        content.setBorder(BorderFactory.createEmptyBorder(20, 60, 40, 0));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        JLabel welcome = new JLabel("Welcome");
        welcome.setFont(AppFonts.bold(80));
        welcome.setForeground(MainFrame.NAV_BTN_BG);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = new JLabel("Click an option below to begin.");
        subtitle.setFont(AppFonts.regular(20));
        subtitle.setForeground(MainFrame.SECONDARY_BTN_BG);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(welcome);
        leftPanel.add(subtitle);
        leftPanel.add(Box.createVerticalGlue());

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);

        rightPanel.add(Box.createVerticalGlue());
        JLabel start = new JLabel("Start Here");
        start.setFont(AppFonts.bold(20));
        start.setForeground(MainFrame.NAV_BTN_BG);
        start.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnCheckIn = createOutlineButton("Check-In");
        JButton btnBaggage = createOutlineButton("Baggage");
        JButton btnTicket  = createOutlineButton("Ticket Status");
        btnCheckIn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnBaggage.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnTicket.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnCheckIn.addActionListener(e -> mainFrame.showCheckIn());
        btnBaggage.addActionListener(e -> mainFrame.showBaggage());
        btnTicket.addActionListener(e -> mainFrame.showTicketStatus());

        rightPanel.add(start);
        rightPanel.add(btnCheckIn);
        rightPanel.add(Box.createVerticalStrut(13));
        rightPanel.add(btnBaggage);
        rightPanel.add(Box.createVerticalStrut(13));
        rightPanel.add(btnTicket);

        content.add(leftPanel);
        content.add(rightPanel);

        add(content, BorderLayout.CENTER);
    }

    private JButton createOutlineButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isPressed()) {
                    g.setColor(new Color(0, 0, 0, 30));
                    g.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                } else if (getModel().isRollover()) {
                    g.setColor(new Color(0, 0, 0, 15));
                    g.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                }
                super.paintComponent(g);
            }
        };

        btn.setFont(AppFonts.bold(17));
        btn.setPreferredSize(new Dimension(280, 70));
        btn.setMaximumSize(new Dimension(280, 70));
        btn.setMinimumSize(new Dimension(280, 70));

        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBackground(new Color(0, 0, 0, 0));
        btn.setForeground(MainFrame.NAV_BTN_BG);

        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        btn.setBorder(new RoundedOutlineBorder(2, MainFrame.NAV_BTN_BG, 20));

        return btn;
    }

    private JButton createBackButton(String iconPath) {
        JButton btn = new JButton();

        try {
            ImageIcon original = new ImageIcon(getClass().getResource(iconPath));
            Image scaled = original.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(scaled));
        } catch (Exception e) {
            System.err.println("Could not load icon: " + iconPath);
        }

        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(MainFrame.NAV_BTN_BG);
                btn.setContentAreaFilled(true);
                btn.setOpaque(true);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setContentAreaFilled(false);
                btn.setOpaque(false);
            }
        });

        return btn;
    }

    private static class RoundedOutlineBorder extends AbstractBorder {
        private final int thickness;
        private final Color color;
        private final int radius;

        public RoundedOutlineBorder(int thickness, Color color, int radius) {
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
            return new Insets(10, 10, 10, 10);
        }
    }
}