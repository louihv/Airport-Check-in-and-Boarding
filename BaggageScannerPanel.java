import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class BaggageScannerPanel extends JPanel {

    private JTextField txtBookingRef, txtWeight, txtTagNo;
    private JLabel lblStatus;
    private final MainFrame mainFrame;
    private BufferedImage backgroundImage;

    public BaggageScannerPanel(MainFrame mainFrame) {
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
        backButton.addActionListener(e -> mainFrame.showPanel("Role"));
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

        JLabel lblTitle = new JLabel("Baggage Check-In & Tagging", SwingConstants.CENTER);
        lblTitle.setFont(AppFonts.bold(22));
        lblTitle.setForeground(MainFrame.NAV_BTN_BG);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 10, 18, 10);
        formCard.add(lblTitle, gbc);

        txtBookingRef = createStyledField("Enter Booking Reference");
        txtWeight = createStyledField("Enter Baggage Weight (kg)");
        txtTagNo = createStyledField("Enter Luggage Tag ID");

        addFormField(formCard, "Booking Reference:", txtBookingRef, gbc, 2);
        addFormField(formCard, "Baggage Weight (kg):", txtWeight, gbc, 4);
        addFormField(formCard, "Luggage Tag ID:", txtTagNo, gbc, 6);

        JButton btnProcess = new JButton("Attach Tag & Verify Weight") {
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
        btnProcess.setFont(AppFonts.regular(15));
        btnProcess.setBackground(MainFrame.SECONDARY_BTN_BG);
        btnProcess.setForeground(Color.WHITE);
        btnProcess.setFocusPainted(false);
        btnProcess.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnProcess.setContentAreaFilled(false);
        btnProcess.setOpaque(false);
        btnProcess.setBorder(new RoundedBorder(MainFrame.SECONDARY_BTN_BG, 25, 1.2f));

        btnProcess.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnProcess.setBackground(MainFrame.NAV_BTN_BG);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnProcess.setBackground(MainFrame.SECONDARY_BTN_BG);
            }
        });

        gbc.gridy = 8;
        gbc.insets = new Insets(22, 10, 6, 10);
        formCard.add(btnProcess, gbc);

        lblStatus = new JLabel("Ready to scan...", SwingConstants.CENTER);
        lblStatus.setFont(AppFonts.italic(12));
        lblStatus.setForeground(MainFrame.NAV_BTN_BG);

        gbc.gridy = 9;
        gbc.insets = new Insets(10, 10, 5, 10);
        formCard.add(lblStatus, gbc);

        centerWrapper.add(formCard);
        add(centerWrapper, BorderLayout.CENTER);

        btnProcess.addActionListener(e -> processBaggage(btnProcess));
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

    private void processBaggage(JButton btnProcess) {
        String ref = getRealText(txtBookingRef, "Enter Booking Reference");
        String weightStr = getRealText(txtWeight, "Enter Baggage Weight (kg)");
        String tag = getRealText(txtTagNo, "Enter Luggage Tag ID");

        if (ref.isEmpty() || weightStr.isEmpty() || tag.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter all baggage fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double weight;
        try {
            weight = Double.parseDouble(weightStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid weight value.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (weight <= 0) {
            JOptionPane.showMessageDialog(this, "Weight must be greater than 0 kg.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (weight > 50) {
            JOptionPane.showMessageDialog(this, "Weight exceeds maximum allowed limit (50 kg).", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final double finalWeight = weight;

        btnProcess.setEnabled(false);
        lblStatus.setForeground(MainFrame.NAV_BTN_BG);
        lblStatus.setText("Processing...");

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                Map<String, String> passenger = FirebaseHelper.getPassenger(ref);
                if (passenger == null) {
                    return "NOT_FOUND";
                }

                String baggageInfo = String.format("%.1f kg | Tag: %s", finalWeight, tag);
                String json = String.format(
                    "{\"bookingRef\":\"%s\",\"tagId\":\"%s\",\"weight\":%.1f,\"passengerName\":\"%s\",\"flightId\":\"%s\",\"timestamp\":\"%s\"}",
                    ref,
                    tag,
                    finalWeight,
                    passenger.get("passengerName"),
                    passenger.get("flightId"),
                    java.time.LocalDateTime.now().toString()
                );
                FirebaseHelper.put("baggage/" + tag, json);

                java.util.List<java.util.Map<String, String>> tickets = FirebaseHelper.getAllTickets();
                for (java.util.Map<String, String> t : tickets) {
                    if (ref.equalsIgnoreCase(t.get("bookingRef"))) {
                        FirebaseHelper.put("tickets/" + t.get("ticketNo") + "/baggage", "\"" + baggageInfo + "\"");
                        break;
                    }
                }

                return finalWeight > 23.0 ? "EXCESS" : "OK";
            }

            @Override
            protected void done() {
                btnProcess.setEnabled(true);
                try {
                    String result = get();
                    if ("NOT_FOUND".equals(result)) {
                        lblStatus.setForeground(Color.RED);
                        lblStatus.setText("Booking reference not found.");
                    } else if ("EXCESS".equals(result)) {
                        lblStatus.setForeground(Color.RED);
                        lblStatus.setText("Warning: Excess Baggage Fee Required (" + finalWeight + " kg)");
                    } else {
                        lblStatus.setForeground(MainFrame.SECONDARY_BTN_BG);
                        lblStatus.setText("Baggage Verified & Tagged Successfully!");
                        resetField(txtBookingRef, "Enter Booking Reference");
                        resetField(txtWeight, "Enter Baggage Weight (kg)");
                        resetField(txtTagNo, "Enter Luggage Tag ID");
                    }
                } catch (Exception ex) {
                    lblStatus.setForeground(Color.RED);
                    lblStatus.setText("Connection error. Check internet / Firebase.");
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private String getRealText(JTextField field, String placeholder) {
        String text = field.getText().trim();
        return text.equals(placeholder) ? "" : text;
    }

    private void resetField(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(MainFrame.SECONDARY_BTN_BG);
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