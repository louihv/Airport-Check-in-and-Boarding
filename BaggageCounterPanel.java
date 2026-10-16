import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

public class BaggageCounterPanel extends JPanel {
    private JToggleButton[] counterButtons;
    private int selectedCounter = 0;
    private ButtonGroup counterGroup;
    private JButton btnRefresh;
    private MainFrame mainFrame;
    private String currentTicketNo = null;
    private boolean revertingSelection = false;
    private JTextField searchField;
    private JPanel resultsContainer;
    private JTextField txtActualWeight;
    private JLabel lblRecordedWeight;
    private JLabel lblAllowance;
    private Map<String, String> currentTicket;
    private List<BagInfo> bags = new ArrayList<>();
    private int selectedBagIndex = 0;
    private JComboBox<String> bagSelector;
    private JPanel bagsListPanel;
    private boolean hasBaggage = false;
    private String currentTagBase = "";

    public BaggageCounterPanel(MainFrame frame) {
        this.mainFrame = frame;
        setLayout(new BorderLayout(12, 12));
        setBackground(MainFrame.MAIN_BG);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);

        JPanel counterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        counterRow.setOpaque(false);
        counterRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSelect = new JLabel("Assigned Counter:");
        lblSelect.setFont(AppFonts.bold(13));
        lblSelect.setForeground(new Color(27, 77, 46));
        btnRefresh = createOutlineButton("Refresh");

        counterRow.add(lblSelect);
        counterRow.add(createCounterSelector());
        counterRow.add(btnRefresh);

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchRow.setOpaque(false);
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchRow.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        searchRow.add(createSearchPanel());

        topSection.add(counterRow);
        topSection.add(searchRow);

        resultsContainer = new JPanel();
        resultsContainer.setLayout(new BoxLayout(resultsContainer, BoxLayout.Y_AXIS));
        resultsContainer.setOpaque(false);

        JScrollPane mainScroll = new JScrollPane(resultsContainer);
        mainScroll.setBorder(BorderFactory.createEmptyBorder());
        mainScroll.setOpaque(false);
        mainScroll.getViewport().setOpaque(true);
        mainScroll.getViewport().setBackground(MainFrame.MAIN_BG);
        mainScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mainScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainScroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        mainScroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);

        btnRefresh.addActionListener(e -> {
            if (currentTicketNo != null) loadTicket(currentTicketNo);
        });

        add(topSection, BorderLayout.NORTH);
        add(mainScroll, BorderLayout.CENTER);

        SwingUtilities.invokeLater(this::showEmptyState);
    }

    private void putStr(String ticket, String field, String value) throws Exception {
        if (value == null) value = "";
        boolean isNumber = value.matches("-?\\d+(\\.\\d+)?");
        String json = isNumber ? value : "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        FirebaseHelper.put("pendingTickets/" + ticket + "/" + field, json);
    }

    private JPanel createSearchPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 0));
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(340, 38));
        wrapper.setMaximumSize(new Dimension(340, 38));

        JPanel fieldPanel = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(245, 247, 250));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.setColor(new Color(216, 203, 194));
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1.5f, getHeight() - 1.5f, 12, 12));
                g2.dispose();
            }
        };
        fieldPanel.setOpaque(false);
        fieldPanel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 8));

        JLabel icon = new JLabel("\uD83D\uDD0D");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        icon.setForeground(new Color(140, 150, 160));
        fieldPanel.add(icon, BorderLayout.WEST);

        searchField = new JTextField() {
            private final String placeholder = "Search ticket, name, flight...";

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(160, 170, 180));
                    g2.setFont(AppFonts.regular(13));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(placeholder, 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                    g2.dispose();
                }
            }
        };
        searchField.setOpaque(false);
        searchField.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        searchField.setFont(AppFonts.regular(13));
        searchField.setForeground(new Color(40, 40, 40));
        searchField.setCaretColor(MainFrame.NAV_BTN_BG);

        searchField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { searchField.repaint(); }
            @Override public void focusLost(FocusEvent e)   { searchField.repaint(); }
        });
        searchField.addActionListener(e -> performSearch());

        fieldPanel.add(searchField, BorderLayout.CENTER);

        JButton btnSearch = new JButton("Search") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                Color bg = getModel().isPressed() ? new Color(36, 130, 70)
                        : getModel().isRollover() ? new Color(40, 145, 80)
                        : new Color(46, 160, 90);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, w, h, 12, 12);
                g2.fillRect(0, 0, 8, h);
                g2.setColor(Color.WHITE);
                g2.setFont(AppFonts.bold(12));
                FontMetrics fm = g2.getFontMetrics();
                String text = getText();
                int tx = (w - fm.stringWidth(text)) / 2;
                int ty = (h + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(text, tx, ty);
                g2.dispose();
            }
        };
        btnSearch.setPreferredSize(new Dimension(78, 38));
        btnSearch.setFocusPainted(false);
        btnSearch.setBorderPainted(false);
        btnSearch.setContentAreaFilled(false);
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSearch.addActionListener(e -> performSearch());

        wrapper.add(fieldPanel, BorderLayout.CENTER);
        wrapper.add(btnSearch, BorderLayout.EAST);
        return wrapper;
    }

    private void performSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            showModernMessage("Enter a ticket number, passenger name, or flight number.", "Search", false);
            return;
        }
        if (!requireCounter()) return;

        new SwingWorker<Map<String, String>, Void>() {
            @Override
            protected Map<String, String> doInBackground() throws Exception {
                Map<String, String> byTicket = FirebaseHelper.getTicketByNumber(query);
                if (byTicket != null) return byTicket;

                List<Map<String, String>> all = FirebaseHelper.getAllPendingTickets();
                String q = query.toLowerCase();
                for (Map<String, String> t : all) {
                    String ticket = nullToEmpty(t.get("ticketNo")).toLowerCase();
                    String name = nullToEmpty(t.get("name")).toLowerCase();
                    String flight = nullToEmpty(t.get("flightNo")).toLowerCase();
                    String bookingNo = nullToEmpty(t.get("bookingRef")).toLowerCase();
                    if (ticket.contains(q) || name.contains(q) || flight.contains(q) || bookingNo.contains(q)) {
                        return t;
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    Map<String, String> result = get();
                    if (result == null) {
                        showEmptyState();
                        showModernMessage("No matching passenger found.", "Search", false);
                    } else {
                        currentTicket = result;
                        currentTicketNo = result.get("ticketNo");
                        selectedBagIndex = 0;
                        displayPassenger(result);
                    }
                } catch (Exception ex) {
                    showModernMessage("Search failed.\n" + ex.getMessage(), "Error", true);
                }
            }
        }.execute();
    }

    private void loadTicket(String ticketNo) {
        new SwingWorker<Map<String, String>, Void>() {
            @Override
            protected Map<String, String> doInBackground() throws Exception {
                return FirebaseHelper.getTicketByNumber(ticketNo);
            }
            @Override
            protected void done() {
                try {
                    Map<String, String> t = get();
                    if (t != null) {
                        currentTicket = t;
                        currentTicketNo = ticketNo;
                        displayPassenger(t);
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void displayPassenger(Map<String, String> t) {
        resultsContainer.removeAll();
        buildBagsFromTicket(t);

        resultsContainer.add(buildPassengerCard(t));
        resultsContainer.add(Box.createVerticalStrut(10));
        if (hasBaggage) {
            resultsContainer.add(buildBaggageDetailsCard(t));
            resultsContainer.add(Box.createVerticalStrut(10));
            resultsContainer.add(buildActionButtons());
        } else {
            resultsContainer.add(buildNoBaggageCard());
        }

        resultsContainer.revalidate();
        resultsContainer.repaint();
    }
    
    private String clean(String s) {
        String v = nullToEmpty(s).trim();
        return v.equalsIgnoreCase("null") ? "" : v;
    }

    private int extractPieces(String baggage) {
        Matcher m = Pattern.compile("(\\d+)\\s*(?:pcs?|pieces?|bags?|luggage)",
                Pattern.CASE_INSENSITIVE).matcher(nullToEmpty(baggage));
        if (m.find()) {
            try { return Integer.parseInt(m.group(1)); } catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    private boolean detectBaggage(Map<String, String> t) {
        String pieces = clean(t.get("baggagePieces"));
        if (!pieces.isEmpty()) {
            try { return Integer.parseInt(pieces) > 0; } catch (NumberFormatException ignored) {}
        }
        if ("true".equalsIgnoreCase(clean(t.get("baggageConfirmedNone")))) return false;

        if (parseWeight(clean(t.get("baggageWeight"))) != null) return true;

        String baggage = clean(t.get("baggage"));
        if (parseWeight(extractWeight(baggage)) != null) return true;
        return extractPieces(baggage) > 0;
    }

    private void buildBagsFromTicket(Map<String, String> t) {
        bags.clear();
        currentTagBase = "";
        hasBaggage = detectBaggage(t);
        if (!hasBaggage) {
            selectedBagIndex = 0;
            return;
        }

        String piecesStr = clean(t.get("baggagePieces"));
        String weightStr = clean(t.get("baggageWeight"));
        String special = clean(t.get("baggageSpecial"));
        String tagBase = clean(t.get("baggageTag"));
        String baggage = clean(t.get("baggage"));
        String perBag = clean(t.get("baggageWeights"));

        if (Double.isNaN(parseWeight(weightStr))) weightStr = extractWeight(baggage);
        if (Double.isNaN(parseWeight(weightStr))) weightStr = "";

        int pieces = 0;
        try {
            if (!piecesStr.isEmpty()) pieces = Integer.parseInt(piecesStr);
        } catch (NumberFormatException ignored) {}
        if (pieces <= 0) pieces = extractPieces(baggage);
        if (pieces <= 0) pieces = 1;

        // "TAG123 (12.00 kg)" -> tag = TAG123
        if (tagBase.isEmpty()) {
            Matcher m = Pattern.compile("^\\s*([A-Za-z0-9_\\-]+)\\s*\\(").matcher(baggage);
            if (m.find()) tagBase = m.group(1);
        }
        if (tagBase.isEmpty() && !clean(t.get("ticketNo")).isEmpty()) {
            tagBase = "BAG-" + t.get("ticketNo");
        }
        currentTagBase = tagBase;

        String[] parts = perBag.isEmpty() ? new String[0] : perBag.split(",", -1);

        for (int i = 0; i < pieces; i++) {
            String tag = tagBase.isEmpty() ? "BAG-" + (i + 1)
                    : (pieces == 1 ? tagBase : tagBase + "-" + String.format("%02d", i + 1));
            String w = (i < parts.length && !parts[i].trim().isEmpty()) ? parts[i].trim()
                    : (i == 0 && pieces == 1 ? weightStr : "");
            bags.add(new BagInfo(i + 1, tag, w, i == 0 ? special : ""));
        }

        if (selectedBagIndex < 0 || selectedBagIndex >= bags.size()) selectedBagIndex = 0;
    }

    private JPanel buildNoBaggageCard() {
        RoundedPanel card = new RoundedPanel(14, Color.WHITE) {
            @Override public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1, MainFrame.SECONDARY_BTN_BG, 14),
                BorderFactory.createEmptyBorder(18, 16, 16, 16)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);

        JLabel title = new JLabel("No Baggage Declared");
        title.setFont(AppFonts.bold(15));
        title.setForeground(new Color(27, 77, 46));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("This passenger has no declared baggage. Declare one if they arrived with bags.");
        sub.setFont(AppFonts.regular(12));
        sub.setForeground(new Color(100, 115, 105));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        text.add(title);
        text.add(Box.createVerticalStrut(4));
        text.add(sub);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 0));
        buttons.setOpaque(false);

        JButton btnDeclare = createColoredButton("Declare Baggage", new Color(70, 130, 180));
        btnDeclare.addActionListener(e -> declareBaggage());

        JButton btnNone = createColoredButton("Confirm No Baggage", new Color(46, 160, 90));
        btnNone.addActionListener(e -> confirmNoBaggage());

        buttons.add(btnDeclare);
        buttons.add(btnNone);

        card.add(text, BorderLayout.NORTH);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private void declareBaggage() {
        if (!requireCounter() || currentTicketNo == null) return;

        String[] d = showDeclareDialog();
        if (d == null) return;

        final String ticket = currentTicketNo;
        final String user = currentUser();
        final String pieces = d[0];
        final String special = d[1];
        final String existingTag = clean(currentTicket.get("baggageTag"));
        final String tag = existingTag.isEmpty() ? "BAG-" + ticket : existingTag;

        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                putStr(ticket, "baggagePieces", pieces);
                putStr(ticket, "baggageTag", tag);
                if (!special.isEmpty()) putStr(ticket, "baggageSpecial", special);
                putStr(ticket, "baggageDeclared", "true");
                putStr(ticket, "baggageDeclaredBy", user);
                return null;
            }

            @Override protected void done() {
                try {
                    get();
                    Map<String, String> updated = new HashMap<>(currentTicket);
                    updated.put("baggagePieces", pieces);
                    updated.put("baggageTag", tag);
                    if (!special.isEmpty()) updated.put("baggageSpecial", special);
                    updated.put("baggageDeclared", "true");
                    updated.put("baggageConfirmedNone", "false");
                    currentTicket = updated;
                    selectedBagIndex = 0;
                    displayPassenger(currentTicket);
                } catch (Exception ex) {
                    showModernMessage("Failed to declare baggage.\n" + ex.getMessage(), "Error", true);
                }
            }
        }.execute();
    }

    private void confirmNoBaggage() {
        if (!requireCounter() || currentTicketNo == null) return;

        final String ticket = currentTicketNo;
        final int counter = selectedCounter;
        final String user = currentUser();

        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                putStr(ticket, "baggagePieces", "0");
                putStr(ticket, "baggageConfirmedNone", "true");
                FirebaseHelper.updateTicketStatus(ticket, "BOARDING", counter, user);
                return null;
            }

            @Override protected void done() {
                try {
                    get();
                    showModernMessage("Confirmed: passenger has no baggage.", "No Baggage", false);
                    clearCurrent();
                } catch (Exception ex) {
                    showModernMessage("Failed to save.\n" + ex.getMessage(), "Error", true);
                }
            }
        }.execute();
    }

    private String[] showDeclareDialog() {
        final String[] result = {null, null};

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Declare Baggage", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());

        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1, MainFrame.SECONDARY_BTN_BG, 16),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)
        ));

        JLabel lblTitle = new JLabel("Declare Baggage");
        lblTitle.setFont(AppFonts.bold(15));
        lblTitle.setForeground(new Color(27, 77, 46));

        JLabel lblPieces = new JLabel("Number of bags");
        lblPieces.setFont(AppFonts.regular(12));
        lblPieces.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSpinner spin = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        spin.setFont(AppFonts.bold(14));
        spin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        spin.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSpecial = new JLabel("Special handling (optional)");
        lblSpecial.setFont(AppFonts.regular(12));
        lblSpecial.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField txtSpecial = new JTextField(20);
        txtSpecial.setFont(AppFonts.regular(13));
        txtSpecial.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txtSpecial.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtSpecial.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1, MainFrame.SECONDARY_BTN_BG, 8),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.add(lblPieces);
        center.add(Box.createVerticalStrut(4));
        center.add(spin);
        center.add(Box.createVerticalStrut(10));
        center.add(lblSpecial);
        center.add(Box.createVerticalStrut(4));
        center.add(txtSpecial);

        JButton cancel = createColoredButton("Cancel", new Color(120, 130, 125));
        cancel.setPreferredSize(new Dimension(90, 34));
        cancel.addActionListener(e -> dialog.dispose());

        JButton ok = createColoredButton("Declare", new Color(46, 160, 90));
        ok.setPreferredSize(new Dimension(90, 34));
        ok.addActionListener(e -> {
            try { spin.commitEdit(); } catch (java.text.ParseException ignored) {}
            result[0] = String.valueOf(spin.getValue());
            result[1] = txtSpecial.getText().trim();
            dialog.dispose();
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(cancel);
        btnRow.add(ok);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(btnRow, BorderLayout.SOUTH);

        dialog.add(card);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        return result[0] == null ? null : result;
    }


    private JPanel buildPassengerCard(Map<String, String> t) {
        RoundedPanel card = new RoundedPanel(14, Color.WHITE);
        card.setLayout(new BorderLayout(0, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1, MainFrame.SECONDARY_BTN_BG, 14),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(true);
        header.setBackground(MainFrame.SECONDARY_BTN_BG);
        header.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel title = new JLabel("PASSENGER INFORMATION");
        title.setFont(AppFonts.bold(13));
        title.setForeground(Color.WHITE);

        JLabel ticketBig = new JLabel(nullToEmpty(t.get("ticketNo")));
        ticketBig.setFont(AppFonts.bold(16));
        ticketBig.setForeground(Color.WHITE);
        ticketBig.setHorizontalAlignment(SwingConstants.RIGHT);

        header.add(title, BorderLayout.WEST);
        header.add(ticketBig, BorderLayout.EAST);

        JPanel body = new JPanel(new GridLayout(1, 4, 12, 0));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        body.add(createInfoBlock("PASSENGER", nullToEmpty(t.get("name"))));
        body.add(createInfoBlock("FLIGHT", nullToEmpty(t.get("flightNo"))));
        body.add(createInfoBlock("CABIN", nullToEmpty(t.get("cabin"))));
        body.add(createInfoBlock("STATUS", nullToEmpty(t.get("status")).isEmpty() ? "WAITING" : t.get("status")));

        card.add(header, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel createInfoBlock(String label, String value) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(AppFonts.regular(10));
        l.setForeground(new Color(100, 115, 105));
        JLabel v = new JLabel(value.isEmpty() ? "—" : value);
        v.setFont(AppFonts.bold(14));
        v.setForeground(new Color(27, 77, 46));
        p.add(l);
        p.add(Box.createVerticalStrut(2));
        p.add(v);
        return p;
    }

    private JPanel buildBaggageDetailsCard(Map<String, String> t) {
        RoundedPanel card = new RoundedPanel(14, Color.WHITE) {
            @Override public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1, MainFrame.SECONDARY_BTN_BG, 14),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JLabel title = new JLabel("Baggage Details  (" + bags.size() + (bags.size() == 1 ? " bag)" : " bags)"));
        title.setFont(AppFonts.bold(14));
        title.setForeground(new Color(27, 77, 46));
        headerRow.add(title, BorderLayout.WEST);

        bagSelector = null;
        if (bags.size() > 1) {
            String[] items = new String[bags.size()];
            for (int i = 0; i < bags.size(); i++) {
                items[i] = "Bag " + bags.get(i).index + "  ·  " + bags.get(i).tag;
            }
            bagSelector = createStyledCombo(items);
            bagSelector.setPreferredSize(new Dimension(220, 36));
            bagSelector.setSelectedIndex(selectedBagIndex);
            bagSelector.addActionListener(e -> {
                selectedBagIndex = bagSelector.getSelectedIndex();
                txtActualWeight.setText(bags.get(selectedBagIndex).weight);
                refreshSelectedBagWeight();
            });
            headerRow.add(bagSelector, BorderLayout.EAST);
        }

        bagsListPanel = new JPanel();
        bagsListPanel.setLayout(new BoxLayout(bagsListPanel, BoxLayout.Y_AXIS));
        bagsListPanel.setOpaque(false);

        for (BagInfo bag : bags) {
            bagsListPanel.add(createBagRow(bag));
            bagsListPanel.add(Box.createVerticalStrut(6));
        }

        JScrollPane bagsScroll = new JScrollPane(bagsListPanel);
        bagsScroll.setBorder(BorderFactory.createEmptyBorder());
        bagsScroll.setOpaque(false);
        bagsScroll.getViewport().setOpaque(false);
        bagsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        bagsScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        bagsScroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        bagsScroll.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        bagsScroll.setPreferredSize(new Dimension(0, Math.min(bags.size() * 72 + 8, 180)));

        JPanel verifyRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        verifyRow.setOpaque(false);
        verifyRow.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        JLabel actualLbl = new JLabel("Actual Weight (kg):");
        actualLbl.setFont(AppFonts.regular(12));
        actualLbl.setForeground(new Color(60, 70, 65));

        txtActualWeight = new JTextField(7);
        txtActualWeight.setFont(AppFonts.bold(14));
        txtActualWeight.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1, MainFrame.SECONDARY_BTN_BG, 8),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        txtActualWeight.setBackground(new Color(248, 250, 248));

        if (!bags.get(selectedBagIndex).weight.isEmpty()) {
            txtActualWeight.setText(bags.get(selectedBagIndex).weight);
        }

        lblRecordedWeight = new JLabel();
        lblRecordedWeight.setFont(AppFonts.regular(12));
        lblRecordedWeight.setForeground(new Color(100, 115, 105));
        refreshSelectedBagWeight();

        lblAllowance = new JLabel(getAllowanceText(t));
        lblAllowance.setFont(AppFonts.regular(12));
        lblAllowance.setForeground(new Color(100, 115, 105));

        verifyRow.add(actualLbl);
        verifyRow.add(txtActualWeight);
        verifyRow.add(lblRecordedWeight);
        verifyRow.add(Box.createHorizontalStrut(8));
        verifyRow.add(lblAllowance);

        card.add(headerRow, BorderLayout.NORTH);
        card.add(bagsScroll, BorderLayout.CENTER);
        card.add(verifyRow, BorderLayout.SOUTH);
        return card;
    }

    private void refreshSelectedBagWeight() {
        if (bags.isEmpty() || selectedBagIndex < 0 || selectedBagIndex >= bags.size()) return;
        BagInfo bag = bags.get(selectedBagIndex);
        String w = bag.weight.isEmpty() ? "—" : bag.weight + " kg";
        lblRecordedWeight.setText("Recorded: " + w);
    }

    private JPanel createBagRow(BagInfo bag) {
        RoundedPanel row = new RoundedPanel(10, new Color(248, 250, 248));
        row.setLayout(new BorderLayout(10, 0));
        row.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1, new Color(220, 225, 230), 10),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel bagTitle = new JLabel("Bag " + bag.index);
        bagTitle.setFont(AppFonts.bold(13));
        bagTitle.setForeground(new Color(27, 77, 46));

        JLabel tagLbl = new JLabel("Tag: " + bag.tag);
        tagLbl.setFont(AppFonts.regular(11));
        tagLbl.setForeground(new Color(100, 115, 105));

        left.add(bagTitle);
        left.add(Box.createVerticalStrut(1));
        left.add(tagLbl);

        if (!bag.special.isEmpty()) {
            JLabel sp = new JLabel(bag.special);
            sp.setFont(AppFonts.regular(11));
            sp.setForeground(new Color(70, 130, 180));
            left.add(Box.createVerticalStrut(2));
            left.add(sp);
        }

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);

        JLabel weightLbl = new JLabel(bag.weight.isEmpty() ? "—" : bag.weight + " kg");
        weightLbl.setFont(AppFonts.bold(18));
        weightLbl.setForeground(new Color(27, 77, 46));
        weightLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel sub = new JLabel(bag.weight.isEmpty() ? "Not weighed yet" : "Recorded weight");
        sub.setFont(AppFonts.regular(10));
        sub.setForeground(new Color(140, 150, 145));
        sub.setAlignmentX(Component.RIGHT_ALIGNMENT);

        right.add(weightLbl);
        right.add(sub);

        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private String extractWeight(String baggage) {
        if (baggage == null) return "";
        Matcher m = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*kg", Pattern.CASE_INSENSITIVE).matcher(baggage);
        if (m.find()) return m.group(1);
        return "";
    }

    private static boolean isValidWeight(String text) {
        if (text == null) return false;
        String s = text.trim();
        if (s.isEmpty()) return false;
        // whole number OR 1–2 decimal places
        return s.matches("^\\d+(\\.\\d{1,2})?$");
    }


    private double getAllowance(Map<String, String> t) {
        String cabin = nullToEmpty(t.get("cabin")).toUpperCase();
        if (cabin.contains("FIRST") || cabin.contains("BUSINESS")) return 32;
        if (cabin.contains("PREMIUM")) return 25;
        return 23;
    }

    private String getAllowanceText(Map<String, String> t) {
        return String.format(Locale.US, "Allowance: %.0f kg", getAllowance(t));
    }

    
    private static Double parseWeight(String text) {
        if (!isValidWeight(text)) return null;
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Returns {perBagCsv, total, baggageText}. Call on the UI thread. */
    private String[] weightSnapshot(int overrideIdx, String overrideWeight) {
        StringBuilder sb = new StringBuilder();
        double total = 0;
        for (int i = 0; i < bags.size(); i++) {
            String w = (i == overrideIdx && overrideWeight != null) ? overrideWeight : bags.get(i).weight;
            if (i > 0) sb.append(',');
            sb.append(w);
            try { if (!w.isEmpty()) total += Double.parseDouble(w); } catch (NumberFormatException ignored) {}
        }
        String totalStr = String.format(Locale.US, "%.1f", total);
        String tag = currentTagBase.isEmpty() ? "BAG" : currentTagBase;
        return new String[]{sb.toString(), totalStr, tag + " (" + totalStr + " kg)"};
    }

    private void saveWeights(String ticket, String[] snap) throws Exception {
        putStr(ticket, "baggageWeights", snap[0]);
        putStr(ticket, "baggageWeight", snap[1]);
        putStr(ticket, "baggageWeightActual", snap[1]);
        putStr(ticket, "baggage", snap[2]);
        putStr(ticket, "baggageVerified", "true");
    }

    private JPanel buildActionButtons() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        JButton btnVerify = createColoredButton("Verify actual weight", new Color(70, 130, 180));
        btnVerify.setToolTipText("Compare against the recorded weight and applicable baggage allowance.");
        btnVerify.addActionListener(e -> verifyWeight());

        JButton btnAccept = createColoredButton("Accept Baggage", new Color(46, 160, 90));
        btnAccept.setToolTipText("Confirm acceptance and save the processing record.");
        btnAccept.addActionListener(e -> acceptBaggage());

        JButton btnFlag = createColoredButton("Flag for Assistance", new Color(220, 70, 70));
        btnFlag.setToolTipText("Record a discrepancy or refer the passenger to authorized staff.");
        btnFlag.addActionListener(e -> flagAssistance());

        panel.add(btnVerify);
        panel.add(btnAccept);
        panel.add(btnFlag);
        return panel;
    }

    private void verifyWeight() {
        if (currentTicketNo == null || bags.isEmpty()) return;
        String actual = txtActualWeight.getText().trim();
        if (actual.isEmpty()) {
            showModernMessage("Enter the actual measured weight first.", "Verify", false);
            return;
        }
        if (!isValidWeight(actual)) {
            showModernMessage("Weight must be a whole number or have 1–2 decimal places\n(e.g. 10, 10.5, 18.25).", "Verify", true);
            return;
        }
        Double actualW = parseWeight(actual);
        if (actualW == null || actualW <= 0) {
            showModernMessage("Weight must be greater than 0 kg.", "Verify", true);
            return;
        }

        final int idx = selectedBagIndex;
        final BagInfo bag = bags.get(idx);
        double rec = 0;
        try { if (!bag.weight.isEmpty()) rec = Double.parseDouble(bag.weight); } catch (NumberFormatException ignored) {}
        final double recorded = rec;
        final double allowance = getAllowance(currentTicket);
        final String ticket = currentTicketNo;
        final String newWeight = String.format(Locale.US, "%.1f", actualW);
        final String[] snap = weightSnapshot(idx, newWeight);

        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                saveWeights(ticket, snap);
                return null;
            }

            @Override protected void done() {
                try {
                    get();
                    bag.weight = newWeight;

                    Map<String, String> updated = new HashMap<>(currentTicket);
                    updated.put("baggageWeights", snap[0]);
                    updated.put("baggageWeight", snap[1]);
                    updated.put("baggageWeightActual", snap[1]);
                    updated.put("baggage", snap[2]);
                    updated.put("baggageVerified", "true");
                    currentTicket = updated;

                    StringBuilder msg = new StringBuilder();
                    msg.append("Bag ").append(idx + 1).append(" actual weight: ").append(newWeight).append(" kg\n");
                    msg.append("Previous recorded: ")
                            .append(recorded > 0 ? String.format(Locale.US, "%.1f kg", recorded) : "—").append("\n");
                    msg.append("Allowance: ").append(String.format(Locale.US, "%.0f", allowance)).append(" kg\n\n");

                    boolean warn = false;
                    if (actualW > allowance) {
                        msg.append("Over allowance by ")
                                .append(String.format(Locale.US, "%.1f", actualW - allowance)).append(" kg");
                        warn = true;
                    } else if (recorded > 0 && Math.abs(actualW - recorded) > 1.5) {
                        msg.append("Differs from recorded by ")
                                .append(String.format(Locale.US, "%.1f", Math.abs(actualW - recorded))).append(" kg");
                        warn = true;
                    } else {
                        msg.append("Within allowance. Weight updated.");
                    }

                    displayPassenger(currentTicket);
                    showModernMessage(msg.toString(), "Weight Verification", warn);
                } catch (Exception ex) {
                    showModernMessage("Failed to update weight.\n" + ex.getMessage(), "Error", true);
                }
            }
        }.execute();
    }

    private void acceptBaggage() {
        if (!requireCounter()) return;
        if (currentTicketNo == null || bags.isEmpty()) {
            showModernMessage("No passenger loaded.", "Notice", false);
            return;
        }

        String actual = txtActualWeight.getText().trim();
        String override = null;

        if (!actual.isEmpty()) {
            if (!isValidWeight(actual)) {
                showModernMessage("Weight must be a whole number or have 1–2 decimal places\n(e.g. 10, 10.5, 18.25).", "Accept Baggage", true);
                return;
            }
            Double w = parseWeight(actual);
            if (w == null || w <= 0) {
                showModernMessage("Weight must be greater than 0 kg.", "Accept Baggage", true);
                return;
            }
            override = String.format(Locale.US, "%.1f", w);
        }

        for (int i = 0; i < bags.size(); i++) {
            String w = (i == selectedBagIndex && override != null) ? override : bags.get(i).weight;
            if (w == null || w.trim().isEmpty()) {
                showModernMessage("Bag " + (i + 1) + " has not been weighed yet.\n"
                        + "Weigh every bag before accepting.", "Accept Baggage", true);
                return;
            }
        }

        final String[] weights = weightSnapshot(selectedBagIndex, override);
        final String ticket = currentTicketNo;
        final int counter = selectedCounter;
        final String user = currentUser();

        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                saveWeights(ticket, weights);
                putStr(ticket, "baggageAccepted", "true");
                putStr(ticket, "baggageAcceptedBy", user);
                putStr(ticket, "baggageAcceptedCounter", String.valueOf(counter));
                FirebaseHelper.updateTicketStatus(ticket, "BOARDING", counter, user);
                return null;
            }

            @Override protected void done() {
                try {
                    get();
                    showModernMessage("Baggage accepted and record saved.", "Accept Baggage", false);
                    clearCurrent();
                } catch (Exception ex) {
                    showModernMessage("Failed to accept baggage.\n" + ex.getMessage(), "Error", true);
                }
            }
        }.execute();
    }

    private void flagAssistance() {
        if (!requireCounter() || currentTicketNo == null) {
            showModernMessage("No passenger loaded.", "Notice", false);
            return;
        }

        String reason = showModernChoice(
                "Select reason for flagging:",
                "Flag for Assistance",
                new String[]{"Overweight", "Tag mismatch", "Damaged bag", "Special handling", "Other discrepancy"}
        );
        if (reason == null) return;

        final String ticket = currentTicketNo;
        final String user = currentUser();
        final int counter = selectedCounter;

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                putStr(ticket, "baggageFlagged", "true");
                putStr(ticket, "baggageFlagReason", reason);
                putStr(ticket, "baggageFlaggedBy", user);
                FirebaseHelper.updateTicketStatus(ticket, "ON HOLD", counter, null);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    showModernMessage("Passenger flagged for assistance.\nReason: " + reason, "Flagged", false);
                    clearCurrent();
                } catch (Exception ex) {
                    showModernMessage("Failed to flag.\n" + ex.getMessage(), "Error", true);
                }
            }
        }.execute();
    }

    private void clearCurrent() {
        currentTicketNo = null;
        currentTicket = null;
        bags.clear();
        hasBaggage = false;
        selectedBagIndex = 0;
        showEmptyState();
        searchField.setText("");
    }

    private void showEmptyState() {
        hasBaggage = false;
        resultsContainer.removeAll();
        JPanel empty = new JPanel();
        empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
        empty.setOpaque(false);
        empty.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel icon = new JLabel("\uD83D\uDEEC");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msg = new JLabel("Search for a passenger to begin baggage processing");
        msg.setFont(AppFonts.regular(14));
        msg.setForeground(new Color(140, 150, 145));
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        empty.add(Box.createVerticalStrut(60));
        empty.add(icon);
        empty.add(Box.createVerticalStrut(10));
        empty.add(msg);

        resultsContainer.add(empty);
        resultsContainer.revalidate();
        resultsContainer.repaint();
    }

    // =====================================================================
    //  UI BUILDERS
    // =====================================================================
    private JComboBox<String> createStyledCombo(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(AppFonts.regular(13));
        combo.setBackground(Color.WHITE);
        combo.setForeground(new Color(40, 40, 40));
        combo.setPreferredSize(new Dimension(0, 36));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        combo.setMinimumSize(new Dimension(80, 36));

        combo.setUI(new ModernComboBoxUI());
        combo.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1, new Color(216, 203, 194), 10),
                BorderFactory.createEmptyBorder(2, 6, 2, 4)
        ));

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setFont(AppFonts.regular(13));
                label.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                if (isSelected) {
                    label.setBackground(MainFrame.NAV_BTN_BG);
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(new Color(40, 40, 40));
                }
                label.setOpaque(true);
                return label;
            }
        });

        return combo;
    }

    private JPanel createCounterSelector() {
        JPanel strip = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        strip.setOpaque(false);
        strip.setBorder(new RoundedOutlineBorder(1, MainFrame.SECONDARY_BTN_BG, 20) {
            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(3, 3, 3, 3);
            }
        });

        counterGroup = new ButtonGroup();
        counterButtons = new JToggleButton[4];

        for (int i = 0; i < 4; i++) {
            final int counter = i + 1;
            JToggleButton btn = new JToggleButton(String.valueOf(counter)) {
                @Override
                protected void paintComponent(Graphics g) {
                    if (isSelected()) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(MainFrame.NAV_BTN_BG);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                        g2.dispose();
                    }
                    super.paintComponent(g);
                }
            };
            btn.setFont(AppFonts.bold(13));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setPreferredSize(new Dimension(44, 32));
            btn.setBorder(BorderFactory.createEmptyBorder());
            btn.setContentAreaFilled(false);
            btn.setOpaque(false);
            btn.setForeground(MainFrame.NAV_BTN_BG);

            btn.addItemListener(e -> {
                boolean sel = btn.isSelected();
                btn.setForeground(sel ? Color.WHITE : MainFrame.NAV_BTN_BG);
                btn.repaint();
                if (!sel || revertingSelection || counter == selectedCounter) return;

                final int prev = selectedCounter;
                final String user = currentUser();

                new SwingWorker<String, Void>() {
                    @Override protected String doInBackground() throws Exception {
                        return FirebaseHelper.getCounterOwner(counter);
                    }
                    @Override protected void done() {
                        try {
                            String owner = get();
                            if (owner != null && !owner.isEmpty() && !owner.equals(user)) {
                                revertSelection(prev);
                                showModernMessage("Counter " + counter + " is already in use by "
                                        + owner + ".", "Counter Unavailable", true);
                                return;
                            }
                            applyCounterSelection(prev, counter);
                        } catch (Exception ex) {
                            revertSelection(prev);
                            showModernMessage("Could not check the counter.\n" + ex.getMessage(), "Error", true);
                        }
                    }
                }.execute();
            });

            counterGroup.add(btn);
            counterButtons[i] = btn;
            strip.add(btn);
        }
        return strip;
    }

    private void revertSelection(int prev) {
        revertingSelection = true;
        try {
            if (prev > 0) counterButtons[prev - 1].setSelected(true);
            else counterGroup.clearSelection();
        } finally {
            revertingSelection = false;
        }
    }

    private void applyCounterSelection(int prev, int counter) {
        selectedCounter = counter;
        currentTicketNo = null;
        currentTicket = null;
        bags.clear();
        hasBaggage = false;
        showEmptyState();
        updateOnlineStatus();

        if (prev > 0 && prev != counter) {
            new Thread(() -> {
                try { FirebaseHelper.releaseCounter(prev); } catch (Exception ex) { ex.printStackTrace(); }
            }).start();
        }
    }

    public void resetCounter() {
        final int prev = selectedCounter;
        currentTicketNo = null;
        currentTicket = null;
        bags.clear();
        hasBaggage = false;
        selectedCounter = 0;
        if (counterGroup != null) counterGroup.clearSelection();
        showEmptyState();
        searchField.setText("");

        if (prev > 0) {
            new Thread(() -> {
                try { FirebaseHelper.releaseCounter(prev); }
                catch (Exception ex) { ex.printStackTrace(); }
            }).start();
        }
    }

    private String currentUser() {
        String u = mainFrame.getCurrentUsername();
        return u == null ? "" : u;
    }

    private boolean requireCounter() {
        if (selectedCounter == 0) {
            showModernMessage("Please select your counter first.", "Select Counter", false);
            return false;
        }
        return true;
    }

    private void updateOnlineStatus() {
        String username = mainFrame.getCurrentUsername();
        String role = mainFrame.getCurrentRole();
        if (username == null || username.isEmpty() || role == null) return;
        int counter = selectedCounter;
        new Thread(() -> {
            try {
                FirebaseHelper.setOnline(username, role, String.valueOf(counter));
            } catch (Exception ex) {
                System.err.println("Failed to update online status: " + ex.getMessage());
            }
        }).start();
    }

    private JButton createOutlineButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isPressed()) {
                    g.setColor(new Color(0, 0, 0, 30));
                    g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                } else if (getModel().isRollover()) {
                    g.setColor(new Color(0, 0, 0, 15));
                    g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                }
                super.paintComponent(g);
            }
        };
        btn.setFont(AppFonts.bold(13));
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setForeground(MainFrame.NAV_BTN_BG);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new RoundedOutlineBorder(1, new Color(216, 203, 194), 12));
        btn.setPreferredSize(new Dimension(100, 38));
        return btn;
    }

    private JButton createColoredButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(AppFonts.bold(13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        return btn;
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
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1, MainFrame.SECONDARY_BTN_BG, 16),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppFonts.bold(15));
        lblTitle.setForeground(isError ? new Color(180, 50, 50) : new Color(27, 77, 46));

        JLabel lblMsg = new JLabel("<html><body style='width:260px'>" + message.replace("\n", "<br>") + "</body></html>");
        lblMsg.setFont(AppFonts.regular(13));
        lblMsg.setForeground(new Color(50, 65, 55));

        JButton ok = createColoredButton("OK", isError ? new Color(180, 50, 50) : new Color(46, 160, 90));
        ok.setPreferredSize(new Dimension(90, 34));
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

    private String showModernChoice(String message, String title, String[] choices) {
        final String[] result = {null};

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());

        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1, MainFrame.SECONDARY_BTN_BG, 16),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppFonts.bold(15));
        lblTitle.setForeground(new Color(27, 77, 46));

        JLabel lblMsg = new JLabel(message);
        lblMsg.setFont(AppFonts.regular(13));
        lblMsg.setForeground(new Color(50, 65, 55));

        JComboBox<String> combo = createStyledCombo(choices);
        combo.setPreferredSize(new Dimension(300, 36));

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.add(lblMsg);
        center.add(Box.createVerticalStrut(10));
        center.add(combo);

        JButton cancel = createColoredButton("Cancel", new Color(120, 130, 125));
        cancel.setPreferredSize(new Dimension(90, 34));
        cancel.addActionListener(e -> dialog.dispose());

        JButton ok = createColoredButton("OK", new Color(50, 170, 200));
        ok.setPreferredSize(new Dimension(90, 34));
        ok.addActionListener(e -> {
            result[0] = (String) combo.getSelectedItem();
            dialog.dispose();
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(cancel);
        btnRow.add(ok);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(btnRow, BorderLayout.SOUTH);

        dialog.add(card);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        return result[0];
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    // =====================================================================
    //  INNER CLASSES
    // =====================================================================
    private static class BagInfo {
        int index;
        String tag;
        String weight;
        String special;

        BagInfo(int index, String tag, String weight, String special) {
            this.index = index;
            this.tag = tag;
            this.weight = weight;
            this.special = special;
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private Color bg;

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
            return new Insets(6, 10, 6, 10);
        }
    }

    private static class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(180, 190, 200);
            this.trackColor = new Color(245, 247, 250);
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
            g2.fillRoundRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height, 8, 8);
            g2.dispose();
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(thumbBounds.x + 1, thumbBounds.y + 1, thumbBounds.width - 2, thumbBounds.height - 2, 6, 6);
            g2.dispose();
        }
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
                    g2.setColor(new Color(100, 115, 105));
                    int[] xPoints = {w / 2 - 4, w / 2 + 4, w / 2};
                    int[] yPoints = {h / 2 - 2, h / 2 - 2, h / 2 + 3};
                    g2.fillPolygon(xPoints, yPoints, 3);
                    g2.dispose();
                }
            };
            button.setBorder(BorderFactory.createEmptyBorder());
            button.setContentAreaFilled(false);
            button.setFocusable(false);
            return button;
        }

        @Override
        protected ComboPopup createPopup() {
            return new BasicComboPopup(comboBox) {
                @Override
                protected void configurePopup() {
                    super.configurePopup();
                    setBorder(BorderFactory.createLineBorder(new Color(216, 203, 194), 1));
                }
            };
        }
    }
}