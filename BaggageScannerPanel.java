import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class BaggageScannerPanel extends JPanel {

    private static final Color ERROR_COLOR = new Color(200, 50, 50);
    private static final Color TEXT_COLOR = new Color(40, 55, 50);
    private static final String REF_PLACEHOLDER = "Please enter a valid booking reference";
    private static final String WEIGHT_PLACEHOLDER = "e.g. 18.5";
    private static final double MAX_WEIGHT = 50;
    private static final int MAX_BAGS = 10;
    private static final AtomicLong TAG_SEQ = new AtomicLong(System.currentTimeMillis() % 1_000_000_000L);

    private final MainFrame mainFrame;
    private BufferedImage backgroundImage;

    private JTextField txtBookingRef;
    private JLabel lblStatus;
    private JButton btnSubmit;
    private JButton btnAdd;
    private JScrollPane scrollPane;
    private final JPanel luggageContainer;
    private final List<LuggageRow> luggageRows = new ArrayList<>();

    public BaggageScannerPanel(MainFrame mainFrame) {
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
        backButton.addActionListener(e -> mainFrame.showPanel("Role"));
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

        JLabel lblTitle = new JLabel("Baggage Check-In & Tagging", SwingConstants.CENTER);
        lblTitle.setFont(AppFonts.bold(26));
        lblTitle.setForeground(MainFrame.NAV_BTN_BG);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 10, 6, 10);
        formContent.add(lblTitle, gbc);

        JLabel lblInfo = new JLabel(
                "<html><center>Enter the passenger's booking reference, then record the weight<br>"
              + "of each bag. A tag ID is generated automatically for every bag.</center></html>",
                SwingConstants.CENTER);
        lblInfo.setFont(AppFonts.regular(12));
        lblInfo.setForeground(new Color(60, 80, 70));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 10, 14, 10);
        formContent.add(lblInfo, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(6, 10, 2, 10);
        formContent.add(createFieldLabel("Booking reference"), gbc);

        txtBookingRef = createStyledField(REF_PLACEHOLDER, 20);
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 10, 10, 10);
        formContent.add(txtBookingRef, gbc);

        JPanel luggageHeader = new JPanel(new BorderLayout(8, 0));
        luggageHeader.setOpaque(false);
        luggageHeader.add(createFieldLabel("Luggage items"), BorderLayout.WEST);
        btnAdd = createAddButton();
        btnAdd.addActionListener(e -> addLuggageRow());
        luggageHeader.add(btnAdd, BorderLayout.EAST);
        gbc.gridy = 5;
        gbc.insets = new Insets(6, 10, 4, 10);
        formContent.add(luggageHeader, gbc);

        luggageContainer = new JPanel();
        luggageContainer.setLayout(new BoxLayout(luggageContainer, BoxLayout.Y_AXIS));
        luggageContainer.setOpaque(false);
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 10, 0, 10);
        formContent.add(luggageContainer, gbc);

        btnSubmit = new JButton("Verify & Tag Baggage") {
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
        gbc.gridy = 7;
        gbc.insets = new Insets(14, 10, 6, 10);
        formContent.add(btnSubmit, gbc);

        lblStatus = new JLabel(" ", SwingConstants.CENTER);
        lblStatus.setFont(AppFonts.italic(12));
        lblStatus.setForeground(MainFrame.NAV_BTN_BG);
        gbc.gridy = 8;
        gbc.insets = new Insets(8, 10, 5, 10);
        formContent.add(lblStatus, gbc);

        scrollPane = new JScrollPane(formContent);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scrollPane.getVerticalScrollBar().setOpaque(false);

        formCard.add(scrollPane, BorderLayout.CENTER);
        formCard.setPreferredSize(new Dimension(650, 600));

        centerWrapper.add(formCard);
        add(centerWrapper, BorderLayout.CENTER);

        addLuggageRow();
        btnSubmit.addActionListener(e -> processBaggage());
    }

    private void addLuggageRow() {
        if (luggageRows.size() >= MAX_BAGS) {
            lblStatus.setForeground(ERROR_COLOR);
            lblStatus.setText("A maximum of " + MAX_BAGS + " bags per passenger.");
            return;
        }
        LuggageRow row = new LuggageRow(luggageRows.size() + 1, generateTagId());
        luggageRows.add(row);
        luggageContainer.add(row);
        refreshRows();

        if (luggageRows.size() > 1) {
            SwingUtilities.invokeLater(() -> {
                JScrollBar bar = scrollPane.getVerticalScrollBar();
                bar.setValue(bar.getMaximum());
            });
        }
    }

    private void removeLuggageRow(LuggageRow row) {
        if (luggageRows.size() <= 1) return;
        luggageRows.remove(row);
        luggageContainer.remove(row);
        for (int i = 0; i < luggageRows.size(); i++) {
            luggageRows.get(i).setNumber(i + 1);
        }
        refreshRows();
    }

    private void refreshRows() {
        for (int i = 0; i < luggageRows.size(); i++) {
            luggageRows.get(i).setRemovable(i > 0);
        }
        luggageContainer.revalidate();
        luggageContainer.repaint();
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

    private class LuggageRow extends JPanel {
        final JTextField noField;
        final JTextField tagField;
        final JTextField weightField;
        final JButton removeBtn;

        LuggageRow(int number, String tagId) {
            setLayout(new GridBagLayout());
            setOpaque(false);
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setBorder(new EmptyBorder(10, 12, 18, 12));   // 10 padding + 8 gap below the card

            noField = createReadOnlyField(String.valueOf(number), 3);
            noField.setHorizontalAlignment(SwingConstants.CENTER);
            tagField = createReadOnlyField(tagId, 12);
            weightField = createStyledField(WEIGHT_PLACEHOLDER, 7);

            removeBtn = new JButton("\u2715");
            removeBtn.setFont(AppFonts.bold(13));
            removeBtn.setForeground(ERROR_COLOR);
            removeBtn.setContentAreaFilled(false);
            removeBtn.setOpaque(false);
            removeBtn.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 4));
            removeBtn.setFocusPainted(false);
            removeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            removeBtn.setToolTipText("Remove this bag");
            removeBtn.setVisible(false);
            removeBtn.addActionListener(e -> removeLuggageRow(this));

            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.HORIZONTAL;

            c.gridy = 0;
            c.insets = new Insets(0, 0, 2, 6);
            c.gridx = 0; c.weightx = 0.2; add(createSmallLabel("Baggage No."), c);
            c.gridx = 1; c.weightx = 1.0; add(createSmallLabel("Tag ID"), c);
            c.gridx = 2; c.weightx = 0.7; add(createSmallLabel("Weight (kg)"), c);

            c.gridy = 1;
            c.insets = new Insets(0, 0, 0, 6);
            c.gridx = 0; c.weightx = 0.2; add(noField, c);
            c.gridx = 1; c.weightx = 1.0; add(tagField, c);
            c.gridx = 2; c.weightx = 0.7; add(weightField, c);

            c.gridx = 3; c.weightx = 0; c.fill = GridBagConstraints.NONE;
            c.insets = new Insets(0, 0, 0, 0);
            add(removeBtn, c);
        }

        void setNumber(int n) {
            noField.setText(String.valueOf(n));
        }

        int number() {
            try { return Integer.parseInt(noField.getText().trim()); } catch (NumberFormatException e) { return 1; }
        }

        void setRemovable(boolean removable) {
            removeBtn.setVisible(removable);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth() - 1;
            int h = getHeight() - 9;
            g2.setColor(new Color(248, 250, 248));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));
            g2.setColor(new Color(216, 203, 194));
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 14, 14));
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }
    }

    private static double allowanceFor(String cabin) {
        String c = cabin == null ? "" : cabin.toUpperCase();
        if (c.contains("FIRST") || c.contains("BUSINESS")) return 32;
        if (c.contains("PREMIUM")) return 25;
        return 23;
    }

    private static String s(String v) {
        return v == null ? "" : v;
    }

    private static String esc(String v) {
        return s(v).replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void putStr(String ticketNo, String field, String value) throws Exception {
        if (value == null) value = "";
        boolean isNumber = value.matches("-?\\d+(\\.\\d+)?");
        String json = isNumber ? value : "\"" + esc(value) + "\"";
        FirebaseHelper.put("pendingTickets/" + ticketNo + "/" + field, json);
    }

    private void putPassengerStr(String bookingRef, String field, String value) throws Exception {
        if (value == null) value = "";
        boolean isNumber = value.matches("-?\\d+(\\.\\d+)?");
        String json = isNumber ? value : "\"" + esc(value) + "\"";
        FirebaseHelper.put("passengers/" + bookingRef.trim() + "/" + field, json);
    }

    private static String extractJsonNumber(String json, String key) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?[0-9]+(?:\\.[0-9]+)?)");
        Matcher m = p.matcher(json);
        if (m.find()) return m.group(1);
        return null;
    }

    private static boolean isValidWeight(String text) {
        if (text == null) return false;
        String s = text.trim();
        if (s.isEmpty()) return false;
        // whole number OR 1–2 decimal places
        return s.matches("^\\d+(\\.\\d{1,2})?$");
    }

    private static Double parseWeight(String text) {
        if (!isValidWeight(text)) return null;
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void processBaggage() {
        final String ref = getRealText(txtBookingRef);

        boolean invalid = false;
        String message = null;

        if (ref.isEmpty()) {
            setFieldBorder(txtBookingRef, ERROR_COLOR);
            invalid = true;
            message = "Please enter the booking reference.";
        }

        final List<Integer> nos = new ArrayList<>();
        final List<String> tags = new ArrayList<>();
        final List<Double> weights = new ArrayList<>();

        for (LuggageRow row : luggageRows) {
            String ws = getRealText(row.weightField);
            Double w = null;
            String problem = null;

            if (ws.isEmpty()) {
                problem = "Please enter the weight for every bag.";
            } else if (!isValidWeight(ws)) {
                problem = "Weight must be a whole number or have 1–2 decimal places (e.g. 10, 10.5, 18.25).";
            } else {
                w = parseWeight(ws);
                if (w == null || w <= 0) {
                    problem = "Weight must be greater than 0 kg.";
                } else if (w > MAX_WEIGHT) {
                    problem = "Weight exceeds the maximum allowed limit (" + (int) MAX_WEIGHT + " kg).";
                }
            }

            if (problem != null) {
                setFieldBorder(row.weightField, ERROR_COLOR);
                invalid = true;
                if (message == null) message = problem;
            } else {
                nos.add(row.number());
                tags.add(row.tagField.getText().trim());
                weights.add(w);
            }
        }

        if (invalid) {
            lblStatus.setForeground(ERROR_COLOR);
            lblStatus.setText(message);
            return;
        }

        btnSubmit.setEnabled(false);
        lblStatus.setForeground(MainFrame.NAV_BTN_BG);
        lblStatus.setText("Processing...");

        new SwingWorker<String, Void>() {
            String ticketNo = "";
            String cabin = "Economy";
            double allowance = 23;
            double total = 0;
            final List<Integer> excess = new ArrayList<>();

            @Override
            protected String doInBackground() throws Exception {
                Map<String, String> passenger = FirebaseHelper.getPassenger(ref);
                if (passenger == null) return "NOT_FOUND";

                Map<String, String> ticket = null;
                for (Map<String, String> t : FirebaseHelper.getAllPendingTickets()) {
                    if (ref.equalsIgnoreCase(t.get("bookingRef"))) { ticket = t; break; }
                }
                if (ticket == null) {
                    for (Map<String, String> t : FirebaseHelper.getAllTickets()) {
                        if (ref.equalsIgnoreCase(t.get("bookingRef"))) return "ALREADY_DONE";
                    }
                    return "NO_TICKET";
                }

                ticketNo = s(ticket.get("ticketNo"));
                cabin = s(ticket.get("cabin")).isEmpty() ? "Economy" : ticket.get("cabin");
                allowance = allowanceFor(cabin);

                StringBuilder csvTags = new StringBuilder();
                StringBuilder csvWeights = new StringBuilder();
                StringBuilder text = new StringBuilder();

                for (int i = 0; i < weights.size(); i++) {
                    double w = weights.get(i);
                    String tag = tags.get(i);
                    int no = nos.get(i);
                    String wStr = String.format(Locale.US, "%.1f", w);

                    total += w;
                    if (w > allowance) excess.add(no);

                    String json = String.format(Locale.US,
                            "{\"bookingRef\":\"%s\",\"ticketNo\":\"%s\",\"baggageNo\":%d,\"tagId\":\"%s\","
                          + "\"weight\":%.1f,\"passengerName\":\"%s\",\"flightId\":\"%s\",\"timestamp\":\"%s\"}",
                            esc(ref), esc(ticketNo), no, esc(tag), w,
                            esc(passenger.get("passengerName")), esc(passenger.get("flightId")),
                            java.time.LocalDateTime.now().toString());
                    FirebaseHelper.put("baggage/" + tag, json);

                    if (i > 0) { csvTags.append(','); csvWeights.append(','); text.append("; "); }
                    csvTags.append(tag);
                    csvWeights.append(wStr);
                    text.append(tag).append(" (").append(wStr).append(" kg)");
                }
                for (int i = 0; i < weights.size(); i++) {
                    double w = weights.get(i);
                    String tag = tags.get(i);
                    int no = nos.get(i);
                    String wStr = String.format(Locale.US, "%.1f", w);

                    total += w;
                    if (w > allowance) excess.add(no);

                    String json = String.format(Locale.US,
                            "{\"bookingRef\":\"%s\",\"ticketNo\":\"%s\",\"baggageNo\":%d,\"tagId\":\"%s\","
                        + "\"weight\":%.1f,\"passengerName\":\"%s\",\"flightId\":\"%s\",\"timestamp\":\"%s\"}",
                            esc(ref), esc(ticketNo), no, esc(tag), w,
                            esc(passenger.get("passengerName")), esc(passenger.get("flightId")),
                            java.time.LocalDateTime.now().toString());
                    FirebaseHelper.put("baggage/" + tag, json);

                    if (i > 0) { csvTags.append(','); csvWeights.append(','); text.append("; "); }
                    csvTags.append(tag);
                    csvWeights.append(wStr);
                    text.append(tag).append(" (").append(wStr).append(" kg)");
                }

                putStr(ticketNo, "baggagePieces", String.valueOf(weights.size()));
                putStr(ticketNo, "baggageTags", csvTags.toString());
                putStr(ticketNo, "baggageTag", tags.get(0));
                putStr(ticketNo, "baggageWeights", csvWeights.toString());
                putStr(ticketNo, "baggageWeight", String.format(Locale.US, "%.1f", total));
                putStr(ticketNo, "baggage", text.toString());
                putStr(ticketNo, "baggageDeclared", "true");
                putStr(ticketNo, "baggageDeclaredBy", "Baggage Scanner");
                putStr(ticketNo, "baggageConfirmedNone", "false");
                putStr(ticketNo, "baggageVerified", "false");

                putPassengerStr(ref, "baggagePieces", String.valueOf(weights.size()));
                putPassengerStr(ref, "baggageTags", csvTags.toString());
                putPassengerStr(ref, "baggageTag", tags.get(0));
                putPassengerStr(ref, "baggageWeights", csvWeights.toString());
                putPassengerStr(ref, "baggageWeight", String.format(Locale.US, "%.1f", total));
                putPassengerStr(ref, "baggage", text.toString());
                putPassengerStr(ref, "baggageDeclared", "true");
                putPassengerStr(ref, "baggageDeclaredBy", "Baggage Scanner");
                putPassengerStr(ref, "baggageVerified", "false");

                return excess.isEmpty() ? "OK" : "EXCESS";
            }

            @Override
            protected void done() {
                btnSubmit.setEnabled(true);
                try {
                    String result = get();
                    switch (result) {
                        case "NOT_FOUND":
                            lblStatus.setForeground(ERROR_COLOR);
                            lblStatus.setText("Booking reference not found.");
                            setFieldBorder(txtBookingRef, ERROR_COLOR);
                            break;
                        case "NO_TICKET":
                            lblStatus.setForeground(ERROR_COLOR);
                            lblStatus.setText("This passenger has not checked in yet.");
                            showModernMessage("No active check-in found for this booking.\n"
                                    + "The passenger must check in before baggage can be tagged.", "Not Checked In", true);
                            break;
                        case "ALREADY_DONE":
                            lblStatus.setForeground(ERROR_COLOR);
                            lblStatus.setText("This booking has already been processed.");
                            break;
                        case "EXCESS": {
                            lblStatus.setForeground(ERROR_COLOR);
                            lblStatus.setText("Warning: Excess Baggage Fee Required");
                            StringBuilder bagList = new StringBuilder();
                            for (int n : excess) {
                                if (bagList.length() > 0) bagList.append(", ");
                                bagList.append(n);
                            }
                            showModernMessage("Bag " + bagList + " exceeds the "
                                    + String.format(Locale.US, "%.0f", allowance) + " kg allowance (" + cabin + ").\n"
                                    + "Excess baggage fee may apply.\n\n"
                                    + "Tagged " + weights.size() + " bag(s), total "
                                    + String.format(Locale.US, "%.1f", total) + " kg.\n"
                                    + "Ticket: " + ticketNo, "Warning", true);
                            resetForm();
                            break;
                        }
                        default:
                            lblStatus.setForeground(MainFrame.SECONDARY_BTN_BG);
                            lblStatus.setText("Baggage Verified & Tagged Successfully!");
                            showModernMessage("Tagged " + weights.size() + " bag(s), total "
                                    + String.format(Locale.US, "%.1f", total) + " kg.\n"
                                    + "Ticket: " + ticketNo, "Success", false);
                            resetForm();
                            break;
                    }
                } catch (Exception ex) {
                    lblStatus.setForeground(ERROR_COLOR);
                    lblStatus.setText("Connection error. Check internet / Firebase.");
                    showModernMessage("Connection error.\nCheck internet / Firebase.\n\n" + ex.getMessage(), "Error", true);
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void resetForm() {
        resetField(txtBookingRef, REF_PLACEHOLDER);
        setFieldBorder(txtBookingRef, MainFrame.NAV_BTN_BG);
        luggageContainer.removeAll();
        luggageRows.clear();
        addLuggageRow();
        scrollPane.getVerticalScrollBar().setValue(0);
    }

    // =====================================================================
    //  FIELD HELPERS  (same look as CheckInPanel)
    // =====================================================================
    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(AppFonts.regular(13));
        label.setForeground(MainFrame.NAV_BTN_BG);
        return label;
    }

    private JLabel createSmallLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(AppFonts.regular(11));
        label.setForeground(MainFrame.NAV_BTN_BG);
        return label;
    }

    private void setFieldBorder(JTextField field, Color color) {
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(color, 25, 1.2f),
                BorderFactory.createEmptyBorder(5, 16, 5, 16)));
        field.repaint();
    }

    private boolean isPlaceholder(JTextField field) {
        Object ph = field.getClientProperty("ph");
        return ph != null && field.getText().equals(ph);
    }

    private String getRealText(JTextField field) {
        return isPlaceholder(field) ? "" : field.getText().trim();
    }

    private void resetField(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(MainFrame.SECONDARY_BTN_BG);
    }

    private JTextField createStyledField(String placeholder, int columns) {
        JTextField field = new JTextField(columns);
        field.putClientProperty("ph", placeholder);
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
                    field.setText(placeholder);
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

    private JTextField createReadOnlyField(String text, int columns) {
        JTextField field = new JTextField(text, columns);
        field.setFont(AppFonts.regular(13));
        field.setOpaque(false);
        field.setEditable(false);
        field.setFocusable(false);
        field.setForeground(TEXT_COLOR);
        setFieldBorder(field, MainFrame.NAV_BTN_BG);
        return field;
    }

    private JButton createAddButton() {
        JButton btn = new JButton("+ Add Baggage");
        btn.setFont(AppFonts.bold(12));
        btn.setForeground(MainFrame.NAV_BTN_BG);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new RoundedBorder(MainFrame.NAV_BTN_BG, 25, 1.2f));
        btn.setToolTipText("Add another bag");
        btn.setPreferredSize(new Dimension(140, 32));
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
            g.drawImage(backgroundImage, (panelW - drawW) / 2, (panelH - drawH) / 2, drawW, drawH, this);
        } else {
            g.setColor(MainFrame.MAIN_BG);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    // =====================================================================
    //  DIALOG
    // =====================================================================
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

    // =====================================================================
    //  INNER CLASSES
    // =====================================================================
    private static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = MainFrame.SECONDARY_BTN_BG;
            this.trackColor = new Color(184, 190, 210);
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

        RoundedPanel(int radius, Color bg) {
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

        RoundedOutlineBorder(int thickness, Color color, int radius) {
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
                    x + thickness / 2f, y + thickness / 2f,
                    width - thickness, height - thickness, radius, radius));
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