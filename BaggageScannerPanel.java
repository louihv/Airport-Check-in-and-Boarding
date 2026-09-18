import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class BaggageScannerPanel extends JPanel {

    private JTextField txtBookingRef;
    private JLabel lblStatus;
    private final MainFrame mainFrame;
    private BufferedImage backgroundImage;
    private final JPanel luggageContainer;
    private final List<LuggageRow> luggageRows = new ArrayList<>();
    private static final AtomicLong TAG_SEQ = new AtomicLong(System.currentTimeMillis() % 1_000_000_000L);
    private JScrollPane scrollPane;

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
        formCard.setLayout(new BorderLayout());
        formCard.setBorder(new EmptyBorder(28, 50, 32, 50));

        JPanel formContent = new JPanel(new GridBagLayout());
        formContent.setOpaque(false);

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
        formContent.add(logoLabel, gbc);

        JLabel lblTitle = new JLabel("Baggage Check-In & Tagging", SwingConstants.CENTER);
        lblTitle.setFont(AppFonts.bold(22));
        lblTitle.setForeground(MainFrame.NAV_BTN_BG);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 10, 18, 10);
        formContent.add(lblTitle, gbc);

        txtBookingRef = createStyledField("Enter Booking Reference");
        addFormField(formContent, "Booking Reference:", txtBookingRef, gbc, 2);

        JPanel luggageHeader = new JPanel(new BorderLayout(8, 0));
        luggageHeader.setOpaque(false);

        JLabel lblLuggage = new JLabel("Luggage Items:");
        lblLuggage.setFont(AppFonts.regular(13));
        lblLuggage.setForeground(MainFrame.NAV_BTN_BG);
        luggageHeader.add(lblLuggage, BorderLayout.WEST);

        gbc.gridy = 4;
        gbc.insets = new Insets(6, 10, 2, 10);
        formContent.add(luggageHeader, gbc);

        luggageContainer = new JPanel();
        luggageContainer.setLayout(new BoxLayout(luggageContainer, BoxLayout.Y_AXIS));
        luggageContainer.setOpaque(false);

        gbc.gridy = 5;
        gbc.insets = new Insets(0, 10, 6, 10);
        formContent.add(luggageContainer, gbc);

        addLuggageRow();

        JButton btnProcess = new JButton("Verify Weight") {
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

        gbc.gridy = 6;
        gbc.insets = new Insets(16, 10, 6, 10);
        formContent.add(btnProcess, gbc);

        lblStatus = new JLabel("...", SwingConstants.CENTER);
        lblStatus.setFont(AppFonts.italic(12));
        lblStatus.setForeground(MainFrame.NAV_BTN_BG);

        gbc.gridy = 7;
        gbc.insets = new Insets(10, 10, 5, 10);
        formContent.add(lblStatus, gbc);

        scrollPane = new JScrollPane(formContent);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scrollPane.getVerticalScrollBar().setOpaque(false);
        scrollPane.getVerticalScrollBar().setBackground(MainFrame.SECONDARY_BTN_BG);

        formCard.add(scrollPane, BorderLayout.CENTER);
        formCard.setPreferredSize(new Dimension(750, 560));

        centerWrapper.add(formCard);
        add(centerWrapper, BorderLayout.CENTER);

        btnProcess.addActionListener(e -> processBaggage(btnProcess));
    }

    private void addLuggageRow() {
        LuggageRow row = new LuggageRow(generateTagId(), luggageRows.isEmpty());
        luggageRows.add(row);
        luggageContainer.add(row);
        luggageContainer.revalidate();
        luggageContainer.repaint();

        if (luggageRows.size() > 1) {
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        }

        formCardParentRevalidate();

        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    private void formCardParentRevalidate() {
        Container c = luggageContainer.getParent();
        while (c != null) {
            c.revalidate();
            c.repaint();
            c = c.getParent();
        }
    }

    private String generateTagId() {
        long seq = TAG_SEQ.incrementAndGet() % 1_000_000_000L;
        int rand = ThreadLocalRandom.current().nextInt(10000);
        long mixed = (seq * 31L + rand) % 10_000_000_000L;
        if (mixed < 0) mixed = -mixed;
        return String.format("%010d", mixed);
    }

    private JButton createPlusButton() {
        JButton btn = new JButton();
        try {
            ImageIcon icon = new ImageIcon(new URL("https://img.icons8.com/ios-filled/24/2d6a4f/plus-math.png"));
            Image scaled = icon.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(scaled));
        } catch (Exception e) {
            btn.setText("+");
            btn.setFont(AppFonts.bold(18));
            btn.setForeground(MainFrame.NAV_BTN_BG);
        }
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setToolTipText("Add another luggage tag");
        btn.setPreferredSize(new Dimension(32, 32));
        return btn;
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

    private JTextField createReadOnlyTagField(String tagId) {
        JTextField field = new JTextField(tagId);
        field.setFont(AppFonts.regular(13));
        field.setOpaque(false);
        field.setEditable(false);
        field.setFocusable(false);
        field.setForeground(new Color(40, 55, 50));
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(MainFrame.NAV_BTN_BG, 25, 1.2f),
                BorderFactory.createEmptyBorder(5, 16, 5, 16)
        ));
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

    private void showModernMessage(String message, String title, boolean isError) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setLayout(new BorderLayout());

        JPanel content = (JPanel) dialog.getContentPane();
        content.setOpaque(false);
        content.setLayout(new BorderLayout());

        RoundedPanel card = new RoundedPanel(16, Color.WHITE);
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1, MainFrame.SECONDARY_BTN_BG, 16),
                BorderFactory.createEmptyBorder(24, 28, 24, 28)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppFonts.bold(16));
        lblTitle.setForeground(isError ? new Color(180, 50, 50) : MainFrame.NAV_BTN_BG);

        JLabel lblMsg = new JLabel("<html><body style='width:260px'>" + message.replace("\n", "<br>") + "</body></html>");
        lblMsg.setFont(AppFonts.regular(13));
        lblMsg.setForeground(new Color(50, 65, 55));

        JButton ok = new JButton("OK");
        ok.setFont(AppFonts.bold(13));
        ok.setBackground(isError ? new Color(180, 50, 50) : MainFrame.SECONDARY_BTN_BG);
        ok.setForeground(Color.WHITE);
        ok.setFocusPainted(false);
        ok.setCursor(new Cursor(Cursor.HAND_CURSOR));
        ok.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
        ok.setOpaque(true);
        ok.setContentAreaFilled(true);
        ok.setPreferredSize(new Dimension(100, 36));
        ok.addActionListener(e -> dialog.dispose());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(ok);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblMsg, BorderLayout.CENTER);
        card.add(btnRow, BorderLayout.SOUTH);

        content.add(card, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void processBaggage(JButton btnProcess) {
        String ref = getRealText(txtBookingRef, "Enter Booking Reference");
        if (ref.isEmpty()) {
            showModernMessage("Please enter booking reference.", "Error", true);
            return;
        }

        List<double[]> validBags = new ArrayList<>();
        List<String> tags = new ArrayList<>();

        for (LuggageRow row : luggageRows) {
            String weightStr = getRealText(row.weightField, "Enter Baggage Weight (kg)");
            String tag = row.tagField.getText().trim();

            if (weightStr.isEmpty()) {
                showModernMessage("Please enter weight for all luggage items.", "Error", true);
                return;
            }

            double weight;
            try {
                weight = Double.parseDouble(weightStr);
            } catch (NumberFormatException ex) {
                showModernMessage("Invalid weight value.", "Error", true);
                return;
            }

            if (weight <= 0) {
                showModernMessage("Weight must be greater than 0 kg.", "Error", true);
                return;
            }
            if (weight > 50) {
                showModernMessage("Weight exceeds maximum allowed limit (50 kg).", "Error", true);
                return;
            }

            validBags.add(new double[]{weight});
            tags.add(tag);
        }

        if (validBags.isEmpty()) {
            showModernMessage("Add at least one luggage item.", "Error", true);
            return;
        }

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

                StringBuilder combinedInfo = new StringBuilder();
                boolean anyExcess = false;

                for (int i = 0; i < validBags.size(); i++) {
                    double weight = validBags.get(i)[0];
                    String tag = tags.get(i);

                    if (weight > 23.0) anyExcess = true;

                    String json = String.format(
                        "{\"bookingRef\":\"%s\",\"tagId\":\"%s\",\"weight\":%.1f,\"passengerName\":\"%s\",\"flightId\":\"%s\",\"timestamp\":\"%s\"}",
                        ref,
                        tag,
                        weight,
                        passenger.get("passengerName"),
                        passenger.get("flightId"),
                        java.time.LocalDateTime.now().toString()
                    );
                    FirebaseHelper.put("baggage/" + tag, json);

                    if (combinedInfo.length() > 0) combinedInfo.append("; ");
                    combinedInfo.append(String.format("%.1f kg | Tag: %s", weight, tag));
                }

                java.util.List<java.util.Map<String, String>> tickets = FirebaseHelper.getAllTickets();
                for (java.util.Map<String, String> t : tickets) {
                    if (ref.equalsIgnoreCase(t.get("bookingRef"))) {
                        FirebaseHelper.put("tickets/" + t.get("ticketNo") + "/baggage", "\"" + combinedInfo.toString() + "\"");
                        break;
                    }
                }

                return anyExcess ? "EXCESS" : "OK";
            }

            @Override
            protected void done() {
                btnProcess.setEnabled(true);
                try {
                    String result = get();
                    if ("NOT_FOUND".equals(result)) {
                        lblStatus.setForeground(Color.RED);
                        lblStatus.setText("Booking reference not found.");
                        showModernMessage("Booking reference not found.", "Error", true);
                    } else if ("EXCESS".equals(result)) {
                        lblStatus.setForeground(Color.RED);
                        lblStatus.setText("Warning: Excess Baggage Fee Required");
                        showModernMessage("One or more bags exceed 23 kg.\nExcess baggage fee may apply.", "Warning", true);
                        resetForm();
                    } else {
                        lblStatus.setForeground(MainFrame.SECONDARY_BTN_BG);
                        lblStatus.setText("Baggage Verified & Tagged Successfully!");
                        showModernMessage("All baggage items verified and tagged successfully.", "Success", false);
                        resetForm();
                    }
                } catch (Exception ex) {
                    lblStatus.setForeground(Color.RED);
                    lblStatus.setText("Connection error. Check internet / Firebase.");
                    showModernMessage("Connection error.\nCheck internet / Firebase.", "Error", true);
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void resetForm() {
        resetField(txtBookingRef, "Enter Booking Reference");
        luggageContainer.removeAll();
        luggageRows.clear();
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        addLuggageRow();
        luggageContainer.revalidate();
        luggageContainer.repaint();
    }

    private String getRealText(JTextField field, String placeholder) {
        String text = field.getText().trim();
        return text.equals(placeholder) ? "" : text;
    }

    private void resetField(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(MainFrame.SECONDARY_BTN_BG);
    }

    private class LuggageRow extends JPanel {
        final JTextField weightField;
        final JTextField tagField;

        LuggageRow(String tagId, boolean showPlus) {
            setLayout(new GridBagLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(4, 0, 8, 0));

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;

            JLabel wLabel = new JLabel("Weight (kg):");
            wLabel.setFont(AppFonts.regular(12));
            wLabel.setForeground(MainFrame.NAV_BTN_BG);
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 2;
            c.insets = new Insets(0, 0, 2, 0);
            add(wLabel, c);

            weightField = createStyledField("Enter Baggage Weight (kg)");
            c.gridy = 1;
            c.gridx = 0;
            c.gridwidth = 1;
            c.weightx = 1.0;
            c.insets = new Insets(0, 0, 6, 4);
            add(weightField, c);

            if (showPlus) {
                JButton plusBtn = createPlusButton();
                plusBtn.addActionListener(e -> addLuggageRow());
                c.gridx = 1;
                c.weightx = 0;
                c.fill = GridBagConstraints.NONE;
                c.insets = new Insets(0, 0, 6, 0);
                add(plusBtn, c);
            }

            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            c.gridwidth = 2;

            JLabel tLabel = new JLabel("Luggage Tag ID:");
            tLabel.setFont(AppFonts.regular(12));
            tLabel.setForeground(MainFrame.NAV_BTN_BG);
            c.gridx = 0;
            c.gridy = 2;
            c.insets = new Insets(0, 0, 2, 0);
            add(tLabel, c);

            tagField = createReadOnlyTagField(tagId);
            c.gridy = 3;
            c.insets = new Insets(0, 0, 0, 0);
            add(tagField, c);
        }
    }

    private static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = MainFrame.SECONDARY_BTN_BG;
            this.trackColor = new Color(184,190,210);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            btn.setMinimumSize(new Dimension(0, 0));
            btn.setMaximumSize(new Dimension(0, 0));
            return btn;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(trackColor);
            g2.fillRoundRect(trackBounds.x + 2, trackBounds.y, trackBounds.width - 4, trackBounds.height, 8, 8);
            g2.dispose();
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(thumbBounds.x + 1, thumbBounds.y + 2, thumbBounds.width - 2, thumbBounds.height - 4, 8, 8);
            g2.dispose();
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bg;

        public RoundedPanel(int radius, Color bg) {
            this.radius = radius;
            this.bg = bg;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));
            g2.dispose();
            super.paintComponent(g);
        }
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
            return new Insets(8, 14, 8, 14);
        }
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