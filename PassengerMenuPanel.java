import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;
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

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 150, 40, 150));

        JPanel accountContainer = createSection(
            "Passenger Portal",
            "Choose how to access your account",
            new String[]{"Log In", "Registration"},
            new String[]{"Existing passengers", "New passengers"},
            new String[]{
                "https://img.icons8.com/ios-filled/50/112250/login-rounded-right.png",
                "https://img.icons8.com/ios-filled/50/112250/add-user-male.png"
            }
        );

        JPanel servicesContainer = createSection(
            "Passenger Menu",
            "Access available passenger services",
            new String[]{"Check-In", "Ticket Status"},
            new String[]{
                "Confirm flight and passenger details",
                "View ticket and boarding information"
            },
            new String[]{
                "https://img.icons8.com/ios-filled/50/112250/boarding-pass.png",
                "https://img.icons8.com/ios-filled/50/112250/ticket.png"
            }
        );

        accountContainer.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        servicesContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(Box.createVerticalGlue());
        content.add(accountContainer);
        content.add(Box.createVerticalStrut(10));
        content.add(servicesContainer);
        content.add(Box.createVerticalGlue());

        add(content, BorderLayout.CENTER);
    }

    private JPanel createSection(
        String mainTitle,
        String subtitle,
        String[] buttonTitles,
        String[] captions,
        String[] iconUrls
    ) {
        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel heading = new JLabel(mainTitle);
        heading.setFont(AppFonts.bold(26));
        heading.setForeground(MainFrame.NAV_BTN_BG);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel(subtitle);
        sub.setFont(AppFonts.regular(16));
        sub.setForeground(MainFrame.SECONDARY_BTN_BG);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel options = new JPanel(new GridLayout(1, 2, 20, 0));
        options.setOpaque(false);
        options.setAlignmentX(Component.CENTER_ALIGNMENT);
        options.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        for (int i = 0; i < buttonTitles.length; i++) {
            JButton button = createOutlineButton(
                buttonTitles[i],
                captions[i],
                iconUrls[i]
            );

            if (buttonTitles[i].equals("Log In")) {
                button.addActionListener(e -> mainFrame.showPanel("CustomerLogin"));
            } else if (buttonTitles[i].equals("Registration")) {
                button.addActionListener(e -> mainFrame.showCustomerRegistration());
            } else if (buttonTitles[i].equals("Check-In")) {
                button.addActionListener(e -> mainFrame.showCheckIn());
            } else if (buttonTitles[i].equals("Ticket Status")) {
                button.addActionListener(e -> mainFrame.showTicketStatus());
            }

            options.add(button);
        }
        

        container.add(heading);
        container.add(Box.createVerticalStrut(8));
        container.add(sub);
        container.add(Box.createVerticalStrut(16));
        container.add(options);

        return container;
    }

    private JButton createOutlineButton(String title, String caption, String iconUrl) {
        String html = "<html><center>"
                    + "<img src='" + iconUrl + "' width='32' height='32'><br>"
                    + "<div style='font-size:15px; font-weight:bold; margin-top:6px; margin-bottom:2px;'>"
                    + title
                    + "</div>"
                    + "<div style='font-size:10px; font-weight:normal;'>"
                    + caption
                    + "</div>"
                    + "</center></html>";

        JButton btn = new JButton(html) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
                );

                if (getModel().isPressed()) {
                    g2.setColor(new Color(0, 0, 0, 30));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(0, 0, 0, 15));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(AppFonts.regular(14));
        btn.setPreferredSize(new Dimension(Integer.MAX_VALUE, 110)); 
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        btn.setMinimumSize(new Dimension(200, 110));
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setForeground(MainFrame.NAV_BTN_BG);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setVerticalAlignment(SwingConstants.CENTER);
        btn.setBorder(new RoundedOutlineBorder(1.5f, new Color(216,203,194), 18));

        return btn;
    }

    private JButton createBackButton(String iconPath) {
        JButton btn = new JButton();

        try {
            ImageIcon original = new ImageIcon(
                getClass().getResource(iconPath)
            );
            Image scaled = original.getImage().getScaledInstance(
                22, 22, Image.SCALE_SMOOTH
            );
            btn.setIcon(new ImageIcon(scaled));
        } catch (Exception e) {
            System.err.println("Could not load icon: " + iconPath);
        }

        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);

        return btn;
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
        public void paintBorder(
            Component c, Graphics g, int x, int y,
            int width, int height
        ) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.draw(new RoundRectangle2D.Float(
                x + thickness / 2f,
                y + thickness / 2f,
                width - thickness,
                height - thickness,
                radius, radius
            ));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(8, 12, 8, 12);
        }
    }
}