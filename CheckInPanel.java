import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxUI;

public class CheckInPanel extends JPanel {

    private static final Color ERROR_COLOR = new Color(200, 50, 50);
    private static final Color TEXT_COLOR = new Color(40, 55, 50);

    private static final int MODE_BOOKING = 0;
    private static final int MODE_ETICKET = 1;
    private static final int MODE_FREQUENT = 2;

    private final MainFrame mainFrame;
    private CheckInConfirmPanel confirmPanel;
    private BufferedImage backgroundImage;

    private JRadioButton rbBooking, rbEticket, rbFrequent;
    private JLabel lblId, lblAirline, lblName, lblStatus;
    private JTextField txtId, txtName;
    private JComboBox<String> cmbAirline;
    private JButton btnSubmit;

    private int mode = MODE_BOOKING;
    private String idPlaceholder = "Please enter a valid booking reference";
    private final String namePlaceholder = "Please enter last name";

    public CheckInPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        try {
            java.net.URL url = getClass().getResource("/resources/bg_passenger.jpg");
            if (url == null) {
                url = getClass().getResource("resources/bg_passenger.jpg");
            }
            if (url != null) {
                backgroundImage = javax.imageio.ImageIO.read(url);
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
        formCard.setLayout(new BorderLayout());
        formCard.setBorder(new EmptyBorder(28, 45, 32, 45));

        JPanel formContent = new JPanel(new GridBagLayout());
        formContent.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
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

        JLabel lblTitle = new JLabel("Passenger Check-In", SwingConstants.CENTER);
        lblTitle.setFont(AppFonts.bold(26));
        lblTitle.setForeground(MainFrame.NAV_BTN_BG);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 10, 6, 10);
        formContent.add(lblTitle, gbc);

        JLabel lblInfo = new JLabel(
                "<html><center>Choose how you would like to retrieve your booking,<br>"
              + "then enter your details exactly as they appear on your ticket.</center></html>",
                SwingConstants.CENTER);
        lblInfo.setFont(AppFonts.regular(12));
        lblInfo.setForeground(new Color(60, 80, 70));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 10, 14, 10);
        formContent.add(lblInfo, gbc);

        rbBooking = createRadio("Booking reference", true);
        rbEticket = createRadio("Eticket number", false);
        rbFrequent = createRadio("Frequent Flyer number", false);
        ButtonGroup group = new ButtonGroup();
        group.add(rbBooking);
        group.add(rbEticket);
        group.add(rbFrequent);

        JPanel radioRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        radioRow.setOpaque(false);
        radioRow.add(rbBooking);
        radioRow.add(rbEticket);
        radioRow.add(rbFrequent);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 10, 14, 10);
        formContent.add(radioRow, gbc);

        lblAirline = createFieldLabel("Frequent flyer airline title");
        cmbAirline = createStyledCombo(new String[]{"Philippine Airlines"});
        lblId = createFieldLabel("Booking reference");
        txtId = createStyledField(idPlaceholder);
        lblName = createFieldLabel("Last name");
        txtName = createStyledField(namePlaceholder);

        gbc.gridy = 4;
        gbc.insets = new Insets(6, 10, 2, 10);
        formContent.add(lblAirline, gbc);
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 10, 10, 10);
        formContent.add(cmbAirline, gbc);

        gbc.gridy = 6;
        gbc.insets = new Insets(6, 10, 2, 10);
        formContent.add(lblId, gbc);
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 10, 10, 10);
        formContent.add(txtId, gbc);

        gbc.gridy = 8;
        gbc.insets = new Insets(6, 10, 2, 10);
        formContent.add(lblName, gbc);
        gbc.gridy = 9;
        gbc.insets = new Insets(0, 10, 10, 10);
        formContent.add(txtName, gbc);

        lblAirline.setVisible(false);
        cmbAirline.setVisible(false);

        btnSubmit = new JButton("Submit") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnSubmit.setFont(AppFonts.regular(15));
        btnSubmit.setBackground(MainFrame.SECONDARY_BTN_BG);
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setFocusPainted(false);
        btnSubmit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSubmit.setContentAreaFilled(false);
        btnSubmit.setOpaque(false);
        btnSubmit.setBorder(new RoundedBorder(MainFrame.SECONDARY_BTN_BG, 25, 1.2f));
        btnSubmit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnSubmit.setBackground(MainFrame.NAV_BTN_BG);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnSubmit.setBackground(MainFrame.SECONDARY_BTN_BG);
            }
        });
        gbc.gridy = 10;
        gbc.insets = new Insets(16, 10, 6, 10);
        formContent.add(btnSubmit, gbc);

        lblStatus = new JLabel(" ", SwingConstants.CENTER);
        lblStatus.setFont(AppFonts.italic(12));
        lblStatus.setForeground(MainFrame.NAV_BTN_BG);
        gbc.gridy = 11;
        gbc.insets = new Insets(8, 10, 5, 10);
        formContent.add(lblStatus, gbc);

        formCard.add(formContent, BorderLayout.CENTER);
        formCard.setPreferredSize(new Dimension(650, 600));

        centerWrapper.add(formCard);
        add(centerWrapper, BorderLayout.CENTER);

        rbBooking.addActionListener(e -> switchMode(MODE_BOOKING));
        rbEticket.addActionListener(e -> switchMode(MODE_ETICKET));
        rbFrequent.addActionListener(e -> switchMode(MODE_FREQUENT));
        btnSubmit.addActionListener(e -> processCheckIn());
    }

    // public void setConfirmPanel(CheckInConfirmPanel confirmPanel) {
    //     this.confirmPanel = confirmPanel;
    // }

    private JRadioButton createRadio(String text, boolean selected) {
        JRadioButton rb = new JRadioButton(text, selected);
        rb.setFont(AppFonts.regular(13));
        rb.setForeground(MainFrame.NAV_BTN_BG);
        rb.setOpaque(false);
        rb.setFocusPainted(false);
        rb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return rb;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(AppFonts.regular(13));
        label.setForeground(MainFrame.NAV_BTN_BG);
        return label;
    }

    private void switchMode(int newMode) {
        mode = newMode;
        boolean frequent = newMode == MODE_FREQUENT;
        lblAirline.setVisible(frequent);
        cmbAirline.setVisible(frequent);

        switch (newMode) {
            case MODE_ETICKET:
                lblId.setText("Eticket number");
                idPlaceholder = "Please enter a ticket number";
                break;
            case MODE_FREQUENT:
                lblId.setText("Frequent Flyer number");
                idPlaceholder = "Please enter a valid frequent flyer number";
                break;
            default:
                lblId.setText("Booking reference");
                idPlaceholder = "Please enter a valid booking reference";
                break;
        }

        resetField(txtId, idPlaceholder);
        resetField(txtName, namePlaceholder);
        setFieldBorder(txtId, MainFrame.NAV_BTN_BG);
        setFieldBorder(txtName, MainFrame.NAV_BTN_BG);
        lblStatus.setText(" ");
        revalidate();
        repaint();
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
            g.drawImage(backgroundImage, (panelW - drawW) / 2, (panelH - drawH) / 2, drawW, drawH, this);
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

    private void setFieldBorder(JTextField field, Color color) {
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(color, 25, 1.2f),
                BorderFactory.createEmptyBorder(5, 16, 5, 16)));
        field.repaint();
    }

    private JTextField createStyledField(String placeholder) {
        JTextField field = new JTextField(20);
        field.setFont(AppFonts.regular(13));
        field.setOpaque(false);
        field.setCaretColor(MainFrame.NAV_BTN_BG);
        field.setText(placeholder);
        field.setForeground(MainFrame.SECONDARY_BTN_BG);
        setFieldBorder(field, MainFrame.NAV_BTN_BG);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (isPlaceholder(field)) {
                    field.setText("");
                    field.setForeground(TEXT_COLOR);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setText(currentPlaceholder(field));
                    field.setForeground(MainFrame.SECONDARY_BTN_BG);
                }
            }
        });

        field.getDocument().addDocumentListener(new DocumentListener() {
            private void clear() {
                if (field.hasFocus() && !field.getText().trim().isEmpty()) {
                    setFieldBorder(field, MainFrame.NAV_BTN_BG);
                }
            }
            public void insertUpdate(DocumentEvent e) { clear(); }
            public void removeUpdate(DocumentEvent e) { clear(); }
            public void changedUpdate(DocumentEvent e) { clear(); }
        });
        return field;
    }

    private String currentPlaceholder(JTextField field) {
        return field == txtName ? namePlaceholder : idPlaceholder;
    }

    private boolean isPlaceholder(JTextField field) {
        return field.getText().equals(currentPlaceholder(field));
    }

    private String getRealText(JTextField field) {
        String text = field.getText().trim();
        return text.equals(currentPlaceholder(field)) ? "" : text;
    }

    private void resetField(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(MainFrame.SECONDARY_BTN_BG);
    }

    private JComboBox<String> createStyledCombo(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(AppFonts.regular(13));
        combo.setOpaque(false);
        combo.setBackground(new Color(0, 0, 0, 0));
        combo.setForeground(TEXT_COLOR);
        combo.setPreferredSize(new Dimension(0, 42));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        combo.setMinimumSize(new Dimension(80, 42));
        combo.setUI(new ModernComboBoxUI());
        combo.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(MainFrame.NAV_BTN_BG, 25, 1.2f),
                BorderFactory.createEmptyBorder(5, 0, 5, 8)));

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setFont(AppFonts.regular(13));
                label.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
                label.setOpaque(true);
                if (isSelected) {
                    label.setBackground(MainFrame.NAV_BTN_BG);
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(TEXT_COLOR);
                }
                return label;
            }
        });

        Object popup = combo.getUI().getAccessibleChild(combo, 0);
        if (popup instanceof JPopupMenu) {
            JPopupMenu popupMenu = (JPopupMenu) popup;
            popupMenu.setBorder(BorderFactory.createLineBorder(MainFrame.NAV_BTN_BG, 1));
            popupMenu.setBackground(Color.WHITE);
            for (Component comp : popupMenu.getComponents()) {
                if (comp instanceof JScrollPane) {
                    JScrollPane sp = (JScrollPane) comp;
                    sp.getViewport().setBackground(Color.WHITE);
                    sp.setBackground(Color.WHITE);
                    JList<?> list = (JList<?>) sp.getViewport().getView();
                    if (list != null) {
                        list.setBackground(Color.WHITE);
                        list.setFixedCellHeight(38);
                    }
                }
            }
        }
        return combo;
    }

    private void processCheckIn() {
        String id = getRealText(txtId);
        String lastName = getRealText(txtName);

        boolean invalid = false;
        if (id.isEmpty()) {
            setFieldBorder(txtId, ERROR_COLOR);
            invalid = true;
        }
        if (lastName.isEmpty()) {
            setFieldBorder(txtName, ERROR_COLOR);
            invalid = true;
        }
        if (invalid) {
            lblStatus.setForeground(ERROR_COLOR);
            lblStatus.setText("Please complete the highlighted fields.");
            return;
        }

        final int searchMode = mode;
        btnSubmit.setEnabled(false);
        lblStatus.setForeground(MainFrame.NAV_BTN_BG);
        lblStatus.setText("Processing...");

        new SwingWorker<String[], Void>() {
            private String failure;

            @Override
            protected String[] doInBackground() throws Exception {
                Map<String, String> data = findPassenger(searchMode, id);
                if (data == null) {
                    failure = "NOT_FOUND";
                    return null;
                }

                String fullName = first(data, "passengerName", "name");
                if (!matchesLastName(fullName, lastName)) {
                    failure = "MISMATCH";
                    return null;
                }

                String ref = first(data, "bookingRef", "bookingReference", "ref");
                if (ref.isEmpty()) {
                    ref = id;
                }

                List<Map<String, String>> tickets = FirebaseHelper.getAllTickets();
                if (tickets != null) {
                    for (Map<String, String> t : tickets) {
                        if (ref.equalsIgnoreCase(t.get("bookingRef"))) {
                            failure = "ALREADY_EXISTS";
                            return null;
                        }
                    }
                }
                try {
                    List<Map<String, String>> pending = FirebaseHelper.getAllPendingTickets();
                    if (pending != null) {
                        for (Map<String, String> p : pending) {
                            if (ref.equalsIgnoreCase(p.get("bookingRef"))) {
                                failure = "ALREADY_EXISTS";
                                return null;
                            }
                        }
                    }
                } catch (Exception ignored) {
                }

                String flight = first(data, "flightId", "flight", "flightNumber");
                String cabin = first(data, "cabin", "cabinClass");
                if (cabin.isEmpty()) {
                    cabin = "Economy";
                }
                return new String[]{ref, fullName, flight, cabin};
            }

            @Override
            protected void done() {
                btnSubmit.setEnabled(true);
                try {
                    String[] result = get();
                    if (result == null) {
                        lblStatus.setForeground(ERROR_COLOR);
                        if ("ALREADY_EXISTS".equals(failure)) {
                            lblStatus.setText("This booking is already checked in.");
                        } else {
                            lblStatus.setText("Passenger not found or details do not match.");
                            setFieldBorder(txtId, ERROR_COLOR);
                            setFieldBorder(txtName, ERROR_COLOR);
                        }
                        return;
                    }
                    if (confirmPanel == null) {
                        confirmPanel = CheckInConfirmPanel.getInstance();
                    }
                    if (confirmPanel == null) {
                        lblStatus.setForeground(ERROR_COLOR);
                        lblStatus.setText("Confirm page was not created in MainFrame.");
                        return;
                    }
                    lblStatus.setText(" ");
                    confirmPanel.setPassenger(result[0], result[1], result[2], result[3]);
                    resetForm();
                    mainFrame.showPanel("CheckInConfirm");
                } catch (Exception ex) {
                    lblStatus.setForeground(ERROR_COLOR);
                    lblStatus.setText("Connection error. Check internet / Firebase.");
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private static String first(Map<String, String> map, String... keys) {
        for (String k : keys) {
            String v = map.get(k);
            if (v != null && !v.trim().isEmpty()) {
                return v.trim();
            }
        }
        return "";
    }

    private Map<String, String> findPassenger(int searchMode, String id) throws Exception {
        String[] keys;
        if (searchMode == MODE_ETICKET) {
            keys = new String[]{"eticketNumber", "eticket", "ticketNumber", "eTicketNumber"};
        } else if (searchMode == MODE_FREQUENT) {
            keys = new String[]{"frequentFlyerNumber", "frequentFlyer", "ffNumber"};
        } else {
            keys = new String[]{"bookingRef", "bookingReference", "ref"};
        }

        List<Map<String, String>> all = FirebaseHelper.getAllPassengers();
        if (all != null) {
            for (Map<String, String> p : all) {
                if (!id.equalsIgnoreCase(first(p, keys))) {
                    continue;
                }
                if (searchMode == MODE_FREQUENT) {
                    String airline = first(p, "frequentFlyerAirline", "airline");
                    if (!airline.isEmpty() && !airline.equalsIgnoreCase((String) cmbAirline.getSelectedItem())) {
                        continue;
                    }
                }
                return p;
            }
        }

        if (searchMode == MODE_BOOKING) {
            return FirebaseHelper.getPassenger(id);
        }
        return null;
    }

    private boolean matchesLastName(String fullName, String entered) {
        if (fullName.isEmpty()) {
            return false;
        }
        if (fullName.equalsIgnoreCase(entered)) {
            return true;
        }
        String[] parts = fullName.split("[\\s,]+");
        for (String part : parts) {
            if (part.equalsIgnoreCase(entered)) {
                return true;
            }
        }
        return false;
    }

    private void resetForm() {
        rbBooking.setSelected(true);
        switchMode(MODE_BOOKING);
        cmbAirline.setSelectedIndex(0);
        lblStatus.setText(" ");
    }

    private static class ModernComboBoxUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            JButton button = new JButton() {
                @Override
                public void paint(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth();
                    int h = getHeight();
                    g2.setColor(MainFrame.NAV_BTN_BG);
                    int[] xPoints = {w / 2 - 4, w / 2 + 4, w / 2};
                    int[] yPoints = {h / 2 - 2, h / 2 - 2, h / 2 + 3};
                    g2.fillPolygon(xPoints, yPoints, 3);
                    g2.dispose();
                }
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(28, 28);
                }
            };
            button.setBorder(BorderFactory.createEmptyBorder());
            button.setContentAreaFilled(false);
            button.setFocusable(false);
            return button;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
        }
    }

    private static class RoundedBorder implements javax.swing.border.Border {
        private final Color color;
        private final int radius;
        private final float thickness;

        RoundedBorder(Color color, int radius, float thickness) {
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
        GlassPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int arc = 32;
            g2.setColor(new Color(0, 0, 0, 35));
            g2.fill(new RoundRectangle2D.Float(5, 7, w - 10, h - 10, arc, arc));
            g2.setColor(new Color(255, 255, 255, 245));
            g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, arc, arc));
            g2.setColor(new Color(255, 255, 255));
            g2.setStroke(new BasicStroke(1.6f));
            g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, arc, arc));
            g2.dispose();
            super.paintComponent(g);
        }
    }
}