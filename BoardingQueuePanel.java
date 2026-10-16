import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Boarding gate control = group-call workflow + passenger/flight crossmatch.
 *
 * Passenger boarding state (computed from the ticket record):
 *   READY            checked in (status COMPLETED), baggage accepted or none declared
 *   BLOCKED          flagged / on hold, or baggage declared but not accepted
 *   NOT CHECKED IN   ticket status is not COMPLETED yet
 *   BOARDED          boarded flag or status BOARDED
 *   NO-SHOW          set when the gate is closed
 */
public class BoardingQueuePanel extends JPanel {

    // ---- data source: switch here if your tickets live elsewhere --------
    private static final String TICKET_NODE = "pendingTickets";
    private static List<Map<String, String>> fetchTickets() throws Exception {
        return FirebaseHelper.getAllPendingTickets();   // or FirebaseHelper.getAllTickets()
    }

    private static final String S_READY = "READY", S_BOARDED = "BOARDED", S_BLOCKED = "BLOCKED",
            S_PENDING = "NOT CHECKED IN", S_NOSHOW = "NO-SHOW";
    private static final String[] FILTERS = {"All", S_READY, S_BOARDED, S_BLOCKED, S_PENDING, S_NOSHOW};

    private static final Color GREEN_DARK = new Color(27, 77, 46);
    private static final Color OK_GREEN = new Color(46, 160, 90);
    private static final Color TEAL = new Color(45, 190, 180);
    private static final Color AMBER = new Color(245, 166, 35);
    private static final Color BLUE = new Color(52, 152, 219);
    private static final Color RED = new Color(220, 70, 70);
    private static final Color NEUTRAL = new Color(120, 130, 125);

    private final String[] CABIN_ORDER = {"First Class", "Business", "Premium Economy", "Economy"};

    private MainFrame mainFrame;
    private JComboBox<String> cmbFlight;
    private JComboBox<String> cmbFilter;
    private JTextField searchField;
    private JLabel lblGate, lblDeparture, lblFlightStatus, lblBoarded, lblReady, lblNotCleared, lblCurrentGroup;
    private DefaultTableModel tableModel;
    private JTable table;
    private JButton btnOpenBoarding, btnCallNextGroup, btnMarkBoarded, btnUndo, btnFinalCall, btnCloseGate, btnRefresh;

    private List<Map<String, String>> flights = new ArrayList<>();
    private List<Map<String, String>> allTickets = new ArrayList<>();
    private List<Map<String, String>> displayed = new ArrayList<>();
    private Map<String, String> currentFlight = null;
    private String selectedFlight = null;
    private boolean boardingOpen = false;
    private boolean updatingCombo = false;
    private int currentGroup = 0;
    private String nameFilter = "";

    public BoardingQueuePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(MainFrame.MAIN_BG);
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Boarding Gate Control");
        title.setFont(AppFonts.bold(22));
        title.setForeground(GREEN_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel north = new JPanel();
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.setOpaque(false);
        JPanel flightCard = createFlightCard();
        JPanel kpiRow = createKpiRow();
        JPanel toolRow = createToolRow();
        flightCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        kpiRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        toolRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        north.add(title);
        north.add(Box.createVerticalStrut(10));
        north.add(flightCard);
        north.add(Box.createVerticalStrut(12));
        north.add(kpiRow);
        north.add(Box.createVerticalStrut(10));
        north.add(toolRow);
        add(north, BorderLayout.NORTH);

        add(createTableCard(), BorderLayout.CENTER);
        add(createActionsCard(), BorderLayout.SOUTH);

        cmbFlight.addActionListener(e -> { if (!updatingCombo) onFlightSelected(); });
        cmbFilter.addActionListener(e -> refreshTable());
        btnRefresh.addActionListener(e -> loadFlights());
        btnOpenBoarding.addActionListener(e -> openBoarding());
        btnCallNextGroup.addActionListener(e -> callNextGroup());
        btnMarkBoarded.addActionListener(e -> markSelectedBoarded(true));
        btnUndo.addActionListener(e -> markSelectedBoarded(false));
        btnFinalCall.addActionListener(e -> finalCall());
        btnCloseGate.addActionListener(e -> closeGate());

        setBoardingControlsEnabled(false);
        loadFlights();
    }

    // =====================================================================
    //  UI
    // =====================================================================
    private JPanel createFlightCard() {
        RoundedPanel panel = new RoundedPanel(16, Color.WHITE);
        panel.setLayout(new BorderLayout(16, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1, MainFrame.SECONDARY_BTN_BG, 16),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel lbl = new JLabel("Flight");
        lbl.setFont(AppFonts.bold(13));
        lbl.setForeground(GREEN_DARK);

        cmbFlight = new JComboBox<>();
        styleModernCombo(cmbFlight);
        cmbFlight.setPreferredSize(new Dimension(150, 42));

        btnRefresh = createColoredButton("Refresh", new Color(70, 130, 180));
        btnRefresh.setPreferredSize(new Dimension(100, 36));

        left.add(lbl);
        left.add(cmbFlight);
        left.add(btnRefresh);

        JPanel info = new JPanel(new GridLayout(1, 3, 20, 0));
        info.setOpaque(false);
        lblGate = createInfoLabel("Gate: —");
        lblDeparture = createInfoLabel("Departure: —");
        lblFlightStatus = createInfoLabel("Status: —");
        info.add(lblGate);
        info.add(lblDeparture);
        info.add(lblFlightStatus);

        panel.add(left, BorderLayout.WEST);
        panel.add(info, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        lblBoarded = new JLabel("0 / 0");
        lblReady = new JLabel("0");
        lblNotCleared = new JLabel("0");
        lblCurrentGroup = new JLabel("—");

        row.add(createKpiCard("Boarded", lblBoarded, TEAL));
        row.add(createKpiCard("Ready to Board", lblReady, OK_GREEN));
        row.add(createKpiCard("Blocked / Not Checked In", lblNotCleared, RED));
        row.add(createKpiCard("Now Calling", lblCurrentGroup, AMBER));
        return row;
    }

    private JPanel createKpiCard(String title, JLabel value, Color bg) {
        RoundedPanel card = new RoundedPanel(12, bg);
        card.setLayout(new BorderLayout(4, 2));
        card.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JLabel t = new JLabel(title);
        t.setForeground(Color.WHITE);
        t.setFont(AppFonts.regular(12));

        value.setForeground(Color.WHITE);
        value.setFont(AppFonts.bold(24));

        card.add(t, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    /** Scan / name search + status filter. */
    private JPanel createToolRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        row.add(createSearchPanel());

        JLabel lbl = new JLabel("Show:");
        lbl.setFont(AppFonts.bold(13));
        lbl.setForeground(GREEN_DARK);
        cmbFilter = new JComboBox<>(FILTERS);
        styleModernCombo(cmbFilter);
        cmbFilter.setPreferredSize(new Dimension(170, 38));
        row.add(lbl);
        row.add(cmbFilter);
        return row;
    }

    private JPanel createSearchPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 0));
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(400, 38));

        JPanel fieldPanel = new JPanel(new BorderLayout(8, 0)) {
            @Override protected void paintComponent(Graphics g) {
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
        fieldPanel.add(icon, BorderLayout.WEST);

        searchField = new JTextField() {
            private final String placeholder = "Scan ticket / booking ref, or type a name...";
            @Override protected void paintComponent(Graphics g) {
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
            @Override public void focusLost(FocusEvent e) { searchField.repaint(); }
        });
        searchField.addActionListener(e -> performSearch());
        fieldPanel.add(searchField, BorderLayout.CENTER);

        JButton btnSearch = new JButton("Scan") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(getModel().isPressed() ? new Color(36, 130, 70)
                        : getModel().isRollover() ? new Color(40, 145, 80) : OK_GREEN);
                g2.fillRoundRect(0, 0, w, h, 12, 12);
                g2.fillRect(0, 0, 8, h);
                g2.setColor(Color.WHITE);
                g2.setFont(AppFonts.bold(12));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2,
                        (h + fm.getAscent() - fm.getDescent()) / 2);
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

    private JPanel createTableCard() {
        RoundedPanel panel = new RoundedPanel(16, Color.WHITE);
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1, MainFrame.SECONDARY_BTN_BG, 16),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));

        JLabel header = new JLabel("Boarding Queue");
        header.setFont(AppFonts.bold(15));
        header.setForeground(GREEN_DARK);

        String[] cols = {"Ticket No", "Passenger", "Cabin", "Seat", "Status", "Note"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        styleTable(table);
        table.setTableHeader(null);

        JPanel tableOuter = new JPanel(new BorderLayout(0, 10));
        tableOuter.setOpaque(false);

        JPanel headerPanel = new JPanel(new GridLayout(1, cols.length, 0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(MainFrame.SECONDARY_BTN_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 18, 18));
                g2.dispose();
            }
        };
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        headerPanel.setPreferredSize(new Dimension(0, 46));

        for (String col : cols) {
            JLabel lbl = new JLabel(col);
            lbl.setForeground(Color.WHITE);
            lbl.setFont(AppFonts.bold(12));
            lbl.setHorizontalAlignment(SwingConstants.LEFT);
            headerPanel.add(lbl);
        }

        RoundedPanel bodyCard = new RoundedPanel(12, Color.WHITE);
        bodyCard.setLayout(new BorderLayout());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setOpaque(false);
        bodyCard.add(scroll, BorderLayout.CENTER);

        tableOuter.add(headerPanel, BorderLayout.NORTH);
        tableOuter.add(bodyCard, BorderLayout.CENTER);

        panel.add(header, BorderLayout.NORTH);
        panel.add(tableOuter, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createActionsCard() {
        RoundedPanel panel = new RoundedPanel(16, Color.WHITE);
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        panel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1, MainFrame.SECONDARY_BTN_BG, 16),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));

        btnOpenBoarding = createColoredButton("Open Boarding", OK_GREEN);
        btnCallNextGroup = createColoredButton("Call Next Group", BLUE);
        btnMarkBoarded = createColoredButton("Mark Boarded", TEAL);
        btnUndo = createColoredButton("Undo Boarded", NEUTRAL);
        btnFinalCall = createColoredButton("Final Call", AMBER);
        btnCloseGate = createColoredButton("Close Gate", RED);

        panel.add(btnOpenBoarding);
        panel.add(btnCallNextGroup);
        panel.add(btnMarkBoarded);
        panel.add(btnUndo);
        panel.add(btnFinalCall);
        panel.add(btnCloseGate);
        return panel;
    }

    private JLabel createInfoLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppFonts.bold(13));
        lbl.setForeground(new Color(50, 65, 55));
        return lbl;
    }

    private JButton createColoredButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(AppFonts.bold(13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        return btn;
    }

    private void styleModernCombo(JComboBox<?> combo) {
        combo.setFont(AppFonts.regular(13));
        combo.setBackground(Color.WHITE);
        combo.setForeground(GREEN_DARK);
        combo.setPreferredSize(new Dimension(150, 42));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        combo.setMinimumSize(new Dimension(80, 38));
        combo.setFocusable(false);
        combo.setMaximumRowCount(8);

        combo.setUI(new ModernComboBoxUI());
        combo.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1, MainFrame.SECONDARY_BTN_BG, 10),
                BorderFactory.createEmptyBorder(2, 6, 2, 4)));

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                label.setFont(AppFonts.regular(13));
                label.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                label.setOpaque(true);
                label.setBackground(isSelected ? OK_GREEN : Color.WHITE);
                label.setForeground(isSelected ? Color.WHITE : GREEN_DARK);
                return label;
            }
        });
    }

    private void styleTable(JTable t) {
        t.setRowHeight(42);
        t.setFont(AppFonts.regular(13));
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(new Color(230, 233, 238));
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setFillsViewportHeight(true);
        t.setSelectionBackground(new Color(230, 245, 235));
        t.setSelectionForeground(GREEN_DARK);
        t.setBackground(Color.WHITE);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));
                setHorizontalAlignment(JLabel.LEFT);

                if (!isSelected) setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 251, 252));

                Color fg = new Color(40, 40, 40);
                setFont(AppFonts.regular(13));
                if (column == 4 && value != null) {
                    setFont(AppFonts.bold(13));
                    switch (value.toString().toUpperCase()) {
                        case S_BOARDED: fg = TEAL; break;
                        case S_READY: fg = OK_GREEN; break;
                        case S_BLOCKED: case S_NOSHOW: fg = RED; break;
                        case S_PENDING: fg = AMBER; break;
                        default: break;
                    }
                } else if (column == 5) {
                    fg = new Color(100, 115, 105);
                }
                setForeground(isSelected && column != 4 ? GREEN_DARK : fg);
                return c;
            }
        });
    }

    private void showModernMessage(String message, String title, boolean isError) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());

        RoundedPanel card = new RoundedPanel(16, Color.WHITE);
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1, MainFrame.SECONDARY_BTN_BG, 16),
                BorderFactory.createEmptyBorder(24, 28, 24, 28)));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppFonts.bold(16));
        lblTitle.setForeground(isError ? new Color(180, 50, 50) : GREEN_DARK);

        JLabel lblMsg = new JLabel("<html><body style='width:260px'>" + message.replace("\n", "<br>") + "</body></html>");
        lblMsg.setFont(AppFonts.regular(13));
        lblMsg.setForeground(new Color(50, 65, 55));

        JButton ok = createColoredButton("OK", isError ? new Color(180, 50, 50) : OK_GREEN);
        ok.setPreferredSize(new Dimension(100, 36));
        ok.addActionListener(e -> dialog.dispose());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(ok);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblMsg, BorderLayout.CENTER);
        card.add(btnRow, BorderLayout.SOUTH);

        dialog.add(card);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private boolean showModernConfirm(String message, String title, String okText, Color okColor) {
        final boolean[] result = {false};

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());

        RoundedPanel card = new RoundedPanel(16, Color.WHITE);
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1, MainFrame.SECONDARY_BTN_BG, 16),
                BorderFactory.createEmptyBorder(24, 28, 24, 28)));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppFonts.bold(16));
        lblTitle.setForeground(GREEN_DARK);

        JLabel lblMsg = new JLabel("<html><body style='width:280px'>" + message.replace("\n", "<br>") + "</body></html>");
        lblMsg.setFont(AppFonts.regular(13));
        lblMsg.setForeground(new Color(50, 65, 55));

        JButton no = createColoredButton("Cancel", NEUTRAL);
        no.setPreferredSize(new Dimension(100, 36));
        no.addActionListener(e -> dialog.dispose());

        JButton yes = createColoredButton(okText, okColor);
        yes.setPreferredSize(new Dimension(100, 36));
        yes.addActionListener(e -> { result[0] = true; dialog.dispose(); });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.add(no);
        btnRow.add(yes);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblMsg, BorderLayout.CENTER);
        card.add(btnRow, BorderLayout.SOUTH);

        dialog.add(card);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        return result[0];
    }

    // =====================================================================
    //  CROSSMATCH
    // =====================================================================
    private static class PaxState {
        final String code, note;
        final int priority;
        PaxState(String code, String note, int priority) {
            this.code = code; this.note = note; this.priority = priority;
        }
    }

    private String clean(String s) {
        String v = s == null ? "" : s.trim();
        return v.equalsIgnoreCase("null") ? "" : v;
    }

    private int cabinGroup(Map<String, String> t) {
        String c = assignCabin(t).toUpperCase();
        if (c.contains("FIRST")) return 0;
        if (c.contains("BUSINESS")) return 1;
        if (c.contains("PREMIUM")) return 2;
        return 3;
    }

    private String assignCabin(Map<String, String> ticket) {
        String cabin = clean(ticket.get("cabin"));
        if (cabin.isEmpty()) cabin = clean(ticket.get("seatClass"));
        return cabin.isEmpty() ? "Economy" : cabin;
    }

    private PaxState computeState(Map<String, String> t) {
        String status = clean(t.get("status")).toUpperCase();
        if (status.equals(S_BOARDED) || "true".equalsIgnoreCase(clean(t.get("boarded"))))
            return new PaxState(S_BOARDED, clean(t.get("boardedTime")).isEmpty() ? "" : "Boarded " + clean(t.get("boardedTime")), 4);
        if (status.equals(S_NOSHOW))
            return new PaxState(S_NOSHOW, "Did not board", 5);

        if ("true".equalsIgnoreCase(clean(t.get("baggageFlagged"))) || status.equals("ON HOLD")) {
            String r = clean(t.get("baggageFlagReason"));
            return new PaxState(S_BLOCKED, r.isEmpty() ? "On hold – see staff" : "Flagged: " + r, 1);
        }
        if (!status.equals("COMPLETED"))
            return new PaxState(S_PENDING, "Check-in / baggage not completed", 2);

        int pieces = 0;
        try { pieces = Integer.parseInt(clean(t.get("baggagePieces"))); } catch (NumberFormatException ignored) {}
        if (pieces > 0 && !"true".equalsIgnoreCase(clean(t.get("baggageAccepted"))))
            return new PaxState(S_BLOCKED, "Baggage not accepted", 1);

        int g = cabinGroup(t);
        String note = !boardingOpen ? "Cleared – boarding not open"
                : g <= currentGroup ? "Cleared – group called"
                : "Cleared – wait for " + CABIN_ORDER[g];
        return new PaxState(S_READY, note, 0);
    }

    private List<Map<String, String>> passengersFor(String flightNo) {
        List<Map<String, String>> out = new ArrayList<>();
        if (flightNo == null) return out;
        for (Map<String, String> t : allTickets) {
            if (flightNo.equalsIgnoreCase(clean(t.get("flightNo")))) out.add(t);
        }
        return out;
    }

    private Map<String, String> flightByNo(String no) {
        for (Map<String, String> f : flights) {
            if (clean(f.get("flightNo")).equalsIgnoreCase(no)) return f;
        }
        return null;
    }

    private boolean flightFinished() {
        if (currentFlight == null) return false;
        String s = clean(currentFlight.get("status")).toUpperCase();
        return s.equals("DEPARTED") || s.equals("CANCELLED");
    }

    // =====================================================================
    //  LOADING
    // =====================================================================
    private void setBoardingControlsEnabled(boolean enabled) {
        btnCallNextGroup.setEnabled(enabled);
        btnMarkBoarded.setEnabled(enabled);
        btnUndo.setEnabled(enabled);
        btnFinalCall.setEnabled(enabled);
        btnCloseGate.setEnabled(enabled);
        btnOpenBoarding.setEnabled(!enabled && selectedFlight != null && !flightFinished());
    }

    private void loadFlights() {
        final String keep = selectedFlight;
        new SwingWorker<List<Map<String, String>>, Void>() {
            @Override protected List<Map<String, String>> doInBackground() throws Exception {
                return FirebaseHelper.getAllFlights();
            }

            @Override protected void done() {
                try {
                    List<Map<String, String>> list = get();
                    flights = list == null ? new ArrayList<>() : list;
                    updatingCombo = true;
                    try {
                        cmbFlight.removeAllItems();
                        for (Map<String, String> f : flights) {
                            String no = clean(f.get("flightNo"));
                            if (!no.isEmpty()) cmbFlight.addItem(no);
                        }
                        if (keep != null) cmbFlight.setSelectedItem(keep);
                    } finally {
                        updatingCombo = false;
                    }
                    onFlightSelected();
                } catch (Exception ex) {
                    showModernMessage("Failed to load flights.", "Error", true);
                }
            }
        }.execute();
    }

    private void onFlightSelected() {
        selectedFlight = (String) cmbFlight.getSelectedItem();
        currentFlight = selectedFlight == null ? null : flightByNo(selectedFlight);
        nameFilter = "";
        if (searchField != null) searchField.setText("");

        boardingOpen = false;
        currentGroup = 0;
        lblCurrentGroup.setText("—");

        if (currentFlight != null) {
            lblGate.setText("Gate: " + orDash(currentFlight.get("gate")));
            lblDeparture.setText("Departure: " + orDash(currentFlight.get("departure")));
            lblFlightStatus.setText("Status: " + orDash(currentFlight.get("status")));

            String st = clean(currentFlight.get("status"));
            if (st.equalsIgnoreCase("Boarding")) {           // restore an in-progress boarding
                boardingOpen = true;
                try { currentGroup = Integer.parseInt(clean(currentFlight.get("currentGroup"))); }
                catch (NumberFormatException ignored) {}
                currentGroup = Math.max(0, Math.min(CABIN_ORDER.length - 1, currentGroup));
                lblCurrentGroup.setText(CABIN_ORDER[currentGroup]);
            } else if (st.equalsIgnoreCase("Departed")) {
                lblCurrentGroup.setText("GATE CLOSED");
            }
        } else {
            lblGate.setText("Gate: —");
            lblDeparture.setText("Departure: —");
            lblFlightStatus.setText("Status: —");
        }
        setBoardingControlsEnabled(boardingOpen);
        loadPassengers();
    }

    private String orDash(String s) {
        return clean(s).isEmpty() ? "—" : clean(s);
    }

    private void loadPassengers() {
        new SwingWorker<List<Map<String, String>>, Void>() {
            @Override protected List<Map<String, String>> doInBackground() throws Exception {
                return fetchTickets();
            }

            @Override protected void done() {
                try {
                    List<Map<String, String>> list = get();
                    allTickets = list == null ? new ArrayList<>() : list;
                    refreshTable();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showModernMessage("Failed to load passengers.", "Error", true);
                }
            }
        }.execute();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        displayed = new ArrayList<>();
        if (selectedFlight == null) {
            lblBoarded.setText("0 / 0");
            lblReady.setText("0");
            lblNotCleared.setText("0");
            return;
        }

        List<Map<String, String>> pax = passengersFor(selectedFlight);
        int boarded = 0, ready = 0, notCleared = 0;
        for (Map<String, String> t : pax) {
            switch (computeState(t).code) {
                case S_BOARDED: boarded++; break;
                case S_READY: ready++; break;
                case S_BLOCKED: case S_PENDING: notCleared++; break;
                default: break;
            }
        }
        lblBoarded.setText(boarded + " / " + pax.size());
        lblReady.setText(String.valueOf(ready));
        lblNotCleared.setText(String.valueOf(notCleared));

        String filter = (String) cmbFilter.getSelectedItem();
        for (Map<String, String> t : pax) {
            PaxState st = computeState(t);
            boolean okFilter = filter == null || filter.equals("All") || filter.equals(st.code);
            boolean okName = nameFilter.isEmpty()
                    || clean(t.get("name")).toLowerCase().contains(nameFilter.toLowerCase());
            if (okFilter && okName) displayed.add(t);
        }
        displayed.sort(Comparator.<Map<String, String>>comparingInt(t -> computeState(t).priority)
                .thenComparingInt(this::cabinGroup)
                .thenComparing(t -> clean(t.get("ticketNo"))));

        for (Map<String, String> t : displayed) {
            PaxState st = computeState(t);
            tableModel.addRow(new Object[]{
                    clean(t.get("ticketNo")), clean(t.get("name")), assignCabin(t),
                    orDash(t.get("seat")), st.code, st.note
            });
        }
    }

    // =====================================================================
    //  SCAN / SEARCH
    // =====================================================================
    private void performSearch() {
        String q = searchField.getText().trim();
        if (selectedFlight == null) {
            showModernMessage("Select a flight first.", "Search", false);
            return;
        }
        if (q.isEmpty()) {
            nameFilter = "";
            refreshTable();
            return;
        }

        Map<String, String> hit = null;
        for (Map<String, String> t : allTickets) {
            if (q.equalsIgnoreCase(clean(t.get("ticketNo"))) || q.equalsIgnoreCase(clean(t.get("bookingRef")))) {
                hit = t;
                break;
            }
        }

        if (hit != null) {
            String paxFlight = clean(hit.get("flightNo"));
            if (!paxFlight.equalsIgnoreCase(selectedFlight)) {
                Map<String, String> other = flightByNo(paxFlight);
                showModernMessage("WRONG FLIGHT\n" + clean(hit.get("name")) + " is booked on " + paxFlight
                        + ".\nGate: " + (other == null ? "—" : orDash(other.get("gate")))
                        + "   Departure: " + (other == null ? "—" : orDash(other.get("departure")))
                        + "\n\nThis gate is boarding " + selectedFlight + ".", "Wrong Flight", true);
                return;
            }
            nameFilter = "";
            cmbFilter.setSelectedIndex(0);
            refreshTable();
            int idx = displayed.indexOf(hit);
            if (idx >= 0) table.setRowSelectionInterval(idx, idx);
            boardPassenger(hit, true);
            searchField.setText("");
            return;
        }

        nameFilter = q;
        refreshTable();
        if (displayed.isEmpty()) {
            boolean elsewhere = false;
            for (Map<String, String> t : allTickets) {
                if (clean(t.get("name")).toLowerCase().contains(q.toLowerCase())) { elsewhere = true; break; }
            }
            showModernMessage(elsewhere
                    ? "No passenger matching \"" + q + "\" on this flight, but a match exists on another flight."
                    : "No matching passenger found.", "Search", false);
        }
    }

    // =====================================================================
    //  BOARDING ACTIONS
    // =====================================================================
    private void openBoarding() {
        if (selectedFlight == null) return;
        if (flightFinished()) {
            showModernMessage("Flight " + selectedFlight + " is already "
                    + clean(currentFlight.get("status")).toLowerCase() + ".", "Notice", true);
            return;
        }
        boardingOpen = true;
        currentGroup = 0;
        lblCurrentGroup.setText(CABIN_ORDER[0]);
        setBoardingControlsEnabled(true);
        updateFlightStatus("Boarding");
        putFlight("currentGroup", "0");

        int ready = 0;
        for (Map<String, String> t : passengersFor(selectedFlight))
            if (computeState(t).code.equals(S_READY)) ready++;

        refreshTable();
        showModernMessage("Boarding is now open for " + selectedFlight
                + "\nNow calling: " + CABIN_ORDER[0]
                + "\n" + ready + " passenger(s) cleared to board.", "Boarding Open", false);
    }

    private void callNextGroup() {
        if (!boardingOpen) return;
        if (currentGroup < CABIN_ORDER.length - 1) {
            currentGroup++;
            lblCurrentGroup.setText(CABIN_ORDER[currentGroup]);
            putFlight("currentGroup", String.valueOf(currentGroup));
            int waiting = 0;
            for (Map<String, String> t : passengersFor(selectedFlight))
                if (cabinGroup(t) == currentGroup && computeState(t).code.equals(S_READY)) waiting++;
            refreshTable();
            showModernMessage("Now calling: " + CABIN_ORDER[currentGroup] + " for flight " + selectedFlight
                    + "\n" + waiting + " cleared passenger(s) in this group.", "Next Group", false);
        } else {
            showModernMessage("All cabins have been called.", "Notice", false);
        }
    }

    private void markSelectedBoarded(boolean board) {
        int row = table.getSelectedRow();
        if (row < 0 || row >= displayed.size()) {
            showModernMessage("Select a passenger first.", "Notice", false);
            return;
        }
        boardPassenger(displayed.get(row), board);
    }

    private void boardPassenger(Map<String, String> t, boolean board) {
        final String ticket = clean(t.get("ticketNo"));
        if (ticket.isEmpty()) return;
        PaxState st = computeState(t);
        final String name = clean(t.get("name"));

        if (board) {
            if (!boardingOpen) {
                showModernMessage("Open boarding first.", "Notice", false);
                return;
            }
            if (st.code.equals(S_BOARDED)) {
                showModernMessage(name + " is already boarded.", "Notice", false);
                return;
            }
            if (!st.code.equals(S_READY)) {
                showModernMessage(name + " cannot board.\n" + st.note
                        + "\n\nRefer the passenger to the proper counter.", "Cannot Board", true);
                return;
            }
            int g = cabinGroup(t);
            if (g > currentGroup && !showModernConfirm(name + " is in " + CABIN_ORDER[g]
                            + ", which has not been called yet (now calling " + CABIN_ORDER[currentGroup]
                            + ").\nBoard anyway?", "Group Not Called", "Board Anyway", AMBER)) return;
            if (!showModernConfirm("Board " + name + "?\nFlight " + selectedFlight
                    + "   Seat " + orDash(t.get("seat")), "Confirm Boarding", "Board", OK_GREEN)) return;
        } else {
            if (!st.code.equals(S_BOARDED)) {
                showModernMessage(name + " is not marked as boarded.", "Notice", false);
                return;
            }
            if (!showModernConfirm("Undo boarding for " + name + "?", "Undo Boarding", "Undo", RED)) return;
        }

        final String user = mainFrame.getCurrentUsername() == null ? "" : mainFrame.getCurrentUsername();
        final String gate = clean(currentFlight == null ? "" : currentFlight.get("gate"));
        final String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        final String millis = String.valueOf(System.currentTimeMillis());

        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                if (board) {
                    putTicket(ticket, "boarded", "true");
                    putTicket(ticket, "boardedBy", user);
                    putTicket(ticket, "boardedAt", millis);
                    putTicket(ticket, "boardedTime", time);
                    putTicket(ticket, "boardingGate", gate);
                    putTicket(ticket, "status", "BOARDED");
                } else {
                    putTicket(ticket, "boarded", "false");
                    putTicket(ticket, "boardedBy", "");
                    putTicket(ticket, "boardedTime", "");
                    putTicket(ticket, "status", "COMPLETED");
                }
                return null;
            }

            @Override protected void done() {
                try {
                    get();
                    t.put("boarded", board ? "true" : "false");
                    t.put("boardedTime", board ? time : "");
                    t.put("status", board ? "BOARDED" : "COMPLETED");
                    searchField.setText("");
                    nameFilter = "";
                    refreshTable();
                } catch (Exception ex) {
                    showModernMessage("Failed to update ticket.\n" + ex.getMessage(), "Error", true);
                }
            }
        }.execute();
    }

    private void finalCall() {
        if (!boardingOpen) return;
        lblCurrentGroup.setText("FINAL CALL");
        int stillReady = 0;
        for (Map<String, String> t : passengersFor(selectedFlight))
            if (computeState(t).code.equals(S_READY)) stillReady++;
        showModernMessage("FINAL CALL for flight " + selectedFlight
                + "\nAll remaining passengers please proceed to the gate."
                + "\n" + stillReady + " cleared passenger(s) have not boarded.", "Final Call", false);
    }

    private void closeGate() {
        if (selectedFlight == null) return;
        final List<Map<String, String>> toMark = new ArrayList<>();
        int ready = 0, other = 0;
        for (Map<String, String> t : passengersFor(selectedFlight)) {
            PaxState st = computeState(t);
            if (st.code.equals(S_BOARDED) || st.code.equals(S_NOSHOW)) continue;
            toMark.add(t);
            if (st.code.equals(S_READY)) ready++; else other++;
        }

        if (!showModernConfirm("Close the gate for " + selectedFlight + "?\n"
                + toMark.size() + " passenger(s) will be marked NO-SHOW"
                + " (" + ready + " cleared, " + other + " blocked / not checked in).", "Close Gate",
                "Confirm", RED)) return;

        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                for (Map<String, String> t : toMark) {
                    putTicket(clean(t.get("ticketNo")), "status", "NO-SHOW");
                }
                FirebaseHelper.put("flights/" + selectedFlight + "/status", "\"Departed\"");
                return null;
            }

            @Override protected void done() {
                try {
                    get();
                    for (Map<String, String> t : toMark) t.put("status", "NO-SHOW");
                    if (currentFlight != null) currentFlight.put("status", "Departed");
                    boardingOpen = false;
                    setBoardingControlsEnabled(false);
                    lblCurrentGroup.setText("GATE CLOSED");
                    lblFlightStatus.setText("Status: Departed");
                    refreshTable();
                    showModernMessage("Gate closed. Flight " + selectedFlight + " is now Departed.", "Gate Closed", false);
                } catch (Exception ex) {
                    showModernMessage("Failed to close gate.\n" + ex.getMessage(), "Error", true);
                }
            }
        }.execute();
    }

    // =====================================================================
    //  FIREBASE WRITES
    // =====================================================================
    private String jsonValue(String value) {
        if (value == null) value = "";
        boolean isNumber = value.matches("-?\\d+(\\.\\d+)?");
        return isNumber ? value : "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private void putTicket(String ticket, String field, String value) throws Exception {
        FirebaseHelper.put(TICKET_NODE + "/" + ticket + "/" + field, jsonValue(value));
    }

    private void putFlight(String field, String value) {
        final String flightNo = selectedFlight;
        if (flightNo == null) return;
        if (currentFlight != null) currentFlight.put(field, value);
        new Thread(() -> {
            try { FirebaseHelper.put("flights/" + flightNo + "/" + field, jsonValue(value)); }
            catch (Exception ex) { ex.printStackTrace(); }
        }).start();
    }

    private void updateFlightStatus(String status) {
        if (currentFlight != null) currentFlight.put("status", status);
        lblFlightStatus.setText("Status: " + status);
        putFlight("status", status);
    }

    /** Call on logout / panel switch. */
    public void reset() {
        boardingOpen = false;
        nameFilter = "";
        if (searchField != null) searchField.setText("");
        onFlightSelected();
    }

    // =====================================================================
    //  INNER CLASSES
    // =====================================================================
    private static class ModernComboBoxUI extends javax.swing.plaf.basic.BasicComboBoxUI {
        @Override protected JButton createArrowButton() {
            JButton button = new JButton() {
                @Override public void paint(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth(), h = getHeight();
                    g2.setColor(new Color(100, 115, 105));
                    g2.fillPolygon(new int[]{w / 2 - 4, w / 2 + 4, w / 2},
                            new int[]{h / 2 - 2, h / 2 - 2, h / 2 + 3}, 3);
                    g2.dispose();
                }
                @Override public Dimension getPreferredSize() { return new Dimension(28, 28); }
            };
            button.setBorder(BorderFactory.createEmptyBorder());
            button.setContentAreaFilled(false);
            button.setFocusable(false);
            return button;
        }

        @Override public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) { }
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bg;

        public RoundedPanel(int radius, Color bg) {
            this.radius = radius;
            this.bg = bg;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
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

        @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.draw(new RoundRectangle2D.Float(x + thickness / 2f, y + thickness / 2f,
                    width - thickness, height - thickness, radius, radius));
            g2.dispose();
        }

        @Override public Insets getBorderInsets(Component c) {
            return new Insets(8, 14, 8, 14);
        }
    }
}