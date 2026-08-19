import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class TicketStatusPanel extends JPanel {

    private JTextField txtSearchTicket;
    private JLabel lblTicketValue, lblStatusValue, lblPositionValue, lblEstWaitValue;
    private final MainFrame mainFrame;
    private BufferedImage backgroundImage;

    private static final int AVG_SERVICE_MINUTES = 3;

    public TicketStatusPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        try {
            java.net.URL url = getClass().getResource("/resources/bg_passenger.jpg");
            if (url == null) {
                url = getClass().getResource("resources/bg_passenger.jpg");
            }
            if (url != null) {
                backgroundImage = javax.imageio.ImageIO.read(url);
            } else {
                System.err.println("Background image not found: /resources/bg_passenger.jpg");
            }
        } catch (Exception e) {
            System.err.println("Could not load background image: " + e.getMessage());
        }

        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        topBar.setOpaque(false);

        JButton backButton = createBackButton("/resources/back.png");
        backButton.addActionListener(e -> mainFrame.showPanel("PassengerMenu"));
        topBar.add(backButton);
        add(topBar, BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        GlassPanel formCard = new GlassPanel();
        formCard.setLayout(new GridBagLayout());
        formCard.setBorder(new EmptyBorder(28, 45, 32, 45));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel logoLabel = new JLabel();
        try {
            java.net.URL logoUrl = getClass().getResource("/resources/logo.png");
            if (logoUrl != null) {
                ImageIcon original = new ImageIcon(logoUrl);
                Image scaled = original.getImage().getScaledInstance(120, 60, Image.SCALE_SMOOTH);
                logoLabel.setIcon(new ImageIcon(scaled));
            }
        } catch (Exception e) {
            System.err.println("Could not load logo: " + e.getMessage());
        }
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridy = 0;
        gbc.insets = new Insets(0, 10, 12, 10);
        formCard.add(logoLabel, gbc);

        JLabel lblTitle = new JLabel("Queue Ticket Status", SwingConstants.CENTER);
        lblTitle.setFont(AppFonts.bold(22));
        lblTitle.setForeground(MainFrame.NAV_BTN_BG);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 10, 18, 10);
        formCard.add(lblTitle, gbc);

        txtSearchTicket = createStyledField("Enter Ticket Number");

        addFormField(formCard, "Ticket Number:", txtSearchTicket, gbc, 2);

        JButton btnSearch = new JButton("Check Status") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                int arc = 25;
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, w, h, arc, arc);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnSearch.setFont(AppFonts.bold(14));
        btnSearch.setBackground(MainFrame.SECONDARY_BTN_BG);
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFocusPainted(false);
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSearch.setContentAreaFilled(false);
        btnSearch.setOpaque(false);
        btnSearch.setBorder(new RoundedBorder(MainFrame.SECONDARY_BTN_BG, 25, 1.2f));

        btnSearch.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnSearch.setBackground(MainFrame.NAV_BTN_BG);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnSearch.setBackground(MainFrame.SECONDARY_BTN_BG);
            }
        });

        gbc.gridy = 4;
        gbc.insets = new Insets(18, 10, 12, 10);
        formCard.add(btnSearch, gbc);

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(new Color(180, 210, 190));
        separator.setPreferredSize(new Dimension(1, 2));

        gbc.gridy = 5;
        gbc.insets = new Insets(8, 10, 14, 10);
        formCard.add(separator, gbc);

        JPanel statusCard = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                int arc = 20;
                g2.setColor(new Color(255, 255, 255, 120));
                g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);
                g2.setColor(new Color(255, 255, 255, 180));
                g2.setStroke(new BasicStroke(1.4f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        statusCard.setOpaque(false);
        statusCard.setBorder(new EmptyBorder(18, 25, 18, 25));

        GridBagConstraints sc = new GridBagConstraints();
        sc.fill = GridBagConstraints.HORIZONTAL;
        sc.insets = new Insets(6, 8, 6, 8);
        sc.anchor = GridBagConstraints.WEST;

        sc.gridx = 0;
        sc.gridy = 0;
        JLabel lblTicketHeader = new JLabel("Ticket Number:");
        lblTicketHeader.setFont(AppFonts.regular(12));
        lblTicketHeader.setForeground(new Color(80, 90, 85));
        statusCard.add(lblTicketHeader, sc);

        sc.gridx = 1;
        lblTicketValue = new JLabel("--");
        lblTicketValue.setFont(AppFonts.bold(20));
        lblTicketValue.setForeground(MainFrame.NAV_BTN_BG);
        statusCard.add(lblTicketValue, sc);

        sc.gridx = 0;
        sc.gridy = 1;
        JLabel lblStatusHeader = new JLabel("Status:");
        lblStatusHeader.setFont(AppFonts.regular(12));
        lblStatusHeader.setForeground(new Color(80, 90, 85));
        statusCard.add(lblStatusHeader, sc);

        sc.gridx = 1;
        lblStatusValue = new JLabel("--");
        lblStatusValue.setFont(AppFonts.bold(15));
        lblStatusValue.setForeground(MainFrame.SECONDARY_BTN_BG);
        statusCard.add(lblStatusValue, sc);

        sc.gridx = 0;
        sc.gridy = 2;
        JLabel lblPosHeader = new JLabel("Position in Queue:");
        lblPosHeader.setFont(AppFonts.regular(12));
        lblPosHeader.setForeground(new Color(80, 90, 85));
        statusCard.add(lblPosHeader, sc);

        sc.gridx = 1;
        lblPositionValue = new JLabel("--");
        lblPositionValue.setFont(AppFonts.regular(13));
        lblPositionValue.setForeground(new Color(40, 50, 45));
        statusCard.add(lblPositionValue, sc);

        sc.gridx = 0;
        sc.gridy = 3;
        JLabel lblWaitHeader = new JLabel("Estimated Wait Time:");
        lblWaitHeader.setFont(AppFonts.regular(12));
        lblWaitHeader.setForeground(new Color(80, 90, 85));
        statusCard.add(lblWaitHeader, sc);

        sc.gridx = 1;
        lblEstWaitValue = new JLabel("--");
        lblEstWaitValue.setFont(AppFonts.regular(13));
        lblEstWaitValue.setForeground(new Color(40, 50, 45));
        statusCard.add(lblEstWaitValue, sc);

        gbc.gridy = 6;
        gbc.insets = new Insets(5, 10, 5, 10);
        formCard.add(statusCard, gbc);

        centerWrapper.add(formCard);
        add(centerWrapper, BorderLayout.CENTER);

        btnSearch.addActionListener(e -> updateStatusDisplay());
        txtSearchTicket.addActionListener(e -> updateStatusDisplay());

        SwingUtilities.invokeLater(this::updateStatusDisplay);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            int panelW = getWidth();
            int panelH = getHeight();
            int imgW = backgroundImage.getWidth();
            int imgH = backgroundImage.getHeight();
            double scale = Math.max((double) panelW / imgW, (double) panelH / imgH);
            int drawW = (int) (imgW * scale);
            int drawH = (int) (imgH * scale);
            int x = (panelW - drawW) / 2;
            int y = (panelH - drawH) / 2;
            g.drawImage(backgroundImage, x, y, drawW, drawH, this);
        } else {
            g.setColor(MainFrame.MAIN_BG);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private void updateStatusDisplay() {
        lblTicketValue.setText("Loading...");
        lblStatusValue.setText("...");
        lblPositionValue.setText("...");
        lblEstWaitValue.setText("...");

        String searchTicket = getRealText(txtSearchTicket, "Enter Ticket Number");

        new SwingWorker<Void, Void>() {
            private Map<String, String> target;
            private List<Map<String, String>> waiting;
            private String errorMsg;

            @Override
            protected Void doInBackground() {
                try {
                    List<Map<String, String>> all = FirebaseHelper.getAllTickets();
                    waiting = FirebaseHelper.getWaitingTickets();

                    if (searchTicket.isEmpty()) {
                        target = findCurrentOrRecentTicket(all);
                    } else {
                        target = FirebaseHelper.getTicketByNumber(searchTicket);
                    }
                } catch (Exception ex) {
                    errorMsg = ex.getMessage();
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done() {
                if (errorMsg != null) {
                    lblTicketValue.setText("Error");
                    lblStatusValue.setText("Could not reach Firebase");
                    lblPositionValue.setText("--");
                    lblEstWaitValue.setText("--");
                    return;
                }

                if (target != null) {
                    displayTicket(target, waiting);
                } else {
                    if (searchTicket.isEmpty()) {
                        lblTicketValue.setText("No tickets yet");
                        lblStatusValue.setText("--");
                        lblPositionValue.setText("--");
                        lblEstWaitValue.setText("--");
                    } else {
                        lblTicketValue.setText("Not Found");
                        lblStatusValue.setText("--");
                        lblPositionValue.setText("--");
                        lblEstWaitValue.setText("--");
                    }
                }
            }
        }.execute();
    }

    private Map<String, String> findCurrentOrRecentTicket(List<Map<String, String>> all) {
        for (Map<String, String> t : all) {
            if ("SERVING".equalsIgnoreCase(t.get("status"))) {
                return t;
            }
        }
        for (int i = all.size() - 1; i >= 0; i--) {
            Map<String, String> t = all.get(i);
            if (!"WAITING".equalsIgnoreCase(t.get("status"))) {
                return t;
            }
        }
        return null;
    }

    private void displayTicket(Map<String, String> found, List<Map<String, String>> waiting) {
        String ticketNo = found.get("ticketNo");
        String status = found.get("status") != null ? found.get("status").toUpperCase() : "";

        lblTicketValue.setText(ticketNo != null ? ticketNo : "--");
        lblStatusValue.setText(status.isEmpty() ? "--" : status);

        if ("WAITING".equals(status)) {
            int position = -1;
            for (int i = 0; i < waiting.size(); i++) {
                if (ticketNo != null && ticketNo.equalsIgnoreCase(waiting.get(i).get("ticketNo"))) {
                    position = i + 1;
                    break;
                }
            }

            if (position > 0) {
                int ahead = position - 1;
                lblPositionValue.setText(position + "  (" + ahead + " ticket" + (ahead == 1 ? "" : "s") + " ahead)");

                int waitMins = ahead * AVG_SERVICE_MINUTES;
                lblEstWaitValue.setText(waitMins <= 0
                        ? "Almost your turn"
                        : waitMins + " min" + (waitMins > 1 ? "s" : ""));
            } else {
                lblPositionValue.setText("Calculating...");
                lblEstWaitValue.setText("Calculating...");
            }
        } else if ("SERVING".equals(status)) {
            String counter = found.get("counter");
            if (counter != null && !counter.equals("0") && !counter.isEmpty()) {
                lblPositionValue.setText("Proceed to Counter " + counter);
            } else {
                lblPositionValue.setText("Proceed to Counter");
            }
            lblEstWaitValue.setText("Now Serving");
        } else {
            lblPositionValue.setText("N/A");
            lblEstWaitValue.setText("N/A");
        }
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
    
        return btn;
    }

    private JTextField createStyledField(String placeholder) {
        JTextField field = new JTextField(20);
        field.setFont(AppFonts.regular(13));
        field.setOpaque(false);
        field.setForeground(MainFrame.SECONDARY_BTN_BG);
        field.setCaretColor(MainFrame.NAV_BTN_BG);
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(MainFrame.NAV_BTN_BG, 25, 1.2f),
                BorderFactory.createEmptyBorder(5, 16, 5, 16)
        ));
        field.setText(placeholder);
        field.setForeground(MainFrame.SECONDARY_BTN_BG);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(new Color(40, 55, 50));
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(MainFrame.SECONDARY_BTN_BG);
                }
            }
        });
        return field;
    }

    private void addFormField(JPanel panel, String labelText, JTextField field,
                              GridBagConstraints gbc, int row) {
        gbc.gridy = row;
        gbc.insets = new Insets(6, 10, 2, 10);
        JLabel label = new JLabel(labelText);
        label.setFont(AppFonts.regular(13));
        label.setForeground(MainFrame.NAV_BTN_BG);
        panel.add(label, gbc);

        gbc.gridy = row + 1;
        gbc.insets = new Insets(0, 10, 10, 10);
        panel.add(field, gbc);
    }

    private String getRealText(JTextField field, String placeholder) {
        String text = field.getText().trim();
        return text.equals(placeholder) ? "" : text;
    }

    private static class RoundedBorder implements javax.swing.border.Border {
        private final Color color;
        private final int radius;
        private final float thickness;

        public RoundedBorder(Color color, int radius, float thickness) {
            this.color = color;
            this.radius = radius;
            this.thickness = thickness;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 3, radius / 2, radius / 3, radius / 2);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }

    private static class GlassPanel extends JPanel {
        public GlassPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            int w = getWidth();
            int h = getHeight();
            int arc = 32;
            g2.setColor(new Color(0, 0, 0, 35));
            g2.fillRoundRect(5, 7, w - 10, h - 10, arc, arc);
            g2.setColor(new Color(255, 255, 255, 95));
            g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);
            g2.setPaint(new GradientPaint(
                    0, 0, new Color(255, 255, 255, 110),
                    0, h / 2.5f, new Color(255, 255, 255, 15)));
            g2.fillRoundRect(0, 0, w - 1, (int) (h / 2.2), arc, arc);
            g2.setColor(new Color(255, 255, 255, 160));
            g2.setStroke(new BasicStroke(1.6f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}