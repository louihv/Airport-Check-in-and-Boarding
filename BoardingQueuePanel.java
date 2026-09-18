import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class BoardingQueuePanel extends JPanel {
    private MainFrame mainFrame;
    private JComboBox<String> cmbFlight;
    private JLabel lblGate, lblDeparture, lblFlightStatus, lblBoarded, lblRemaining, lblCurrentGroup;
    private DefaultTableModel tableModel;
    private JTable table;
    private JButton btnOpenBoarding, btnCallNextGroup, btnMarkBoarded, btnFinalCall, btnCloseGate, btnRefresh;

    private String selectedFlight = null;
    private boolean boardingOpen = false;
    private int currentGroup = 0;
    private final String[] CABIN_ORDER = {"First Class", "Business", "Premium Economy", "Economy"};

    public BoardingQueuePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(MainFrame.MAIN_BG);
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Boarding Gate Control");
        title.setFont(AppFonts.bold(22));
        title.setForeground(new Color(27, 77, 46));
        add(title, BorderLayout.NORTH);

        JPanel north = new JPanel();
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.setOpaque(false);
        north.add(createFlightCard());
        north.add(Box.createVerticalStrut(12));
        north.add(createKpiRow());
        add(north, BorderLayout.NORTH);

        add(createTableCard(), BorderLayout.CENTER);
        add(createActionsCard(), BorderLayout.SOUTH);

        cmbFlight.addActionListener(e -> onFlightSelected());
        btnRefresh.addActionListener(e -> loadFlights());
        btnOpenBoarding.addActionListener(e -> openBoarding());
        btnCallNextGroup.addActionListener(e -> callNextGroup());
        btnMarkBoarded.addActionListener(e -> markSelectedBoarded());
        btnFinalCall.addActionListener(e -> finalCall());
        btnCloseGate.addActionListener(e -> closeGate());

        setBoardingControlsEnabled(false);
        loadFlights();
    }

    private JPanel createFlightCard() {
        RoundedPanel panel = new RoundedPanel(16, Color.WHITE);
        panel.setLayout(new BorderLayout(16, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1, MainFrame.SECONDARY_BTN_BG, 16),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel lbl = new JLabel("Flight");
        lbl.setFont(AppFonts.bold(13));
        lbl.setForeground(new Color(27, 77, 46));

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
        JPanel row = new JPanel(new GridLayout(1, 3, 10, 0));
        row.setOpaque(false);

        lblBoarded = createKpiCard("Boarded", "0", new Color(45, 190, 180));
        lblRemaining = createKpiCard("Remaining", "0", new Color(245, 166, 35));
        lblCurrentGroup = createKpiCard("Now Calling", "—", new Color(46, 160, 90));

        row.add(lblBoarded.getParent());
        row.add(lblRemaining.getParent());
        row.add(lblCurrentGroup.getParent());
        return row;
    }

    private JLabel createKpiCard(String title, String value, Color bg) {
        RoundedPanel card = new RoundedPanel(12, bg);
        card.setLayout(new BorderLayout(4, 2));
        card.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JLabel t = new JLabel(title);
        t.setForeground(Color.WHITE);
        t.setFont(AppFonts.regular(12));

        JLabel v = new JLabel(value);
        v.setForeground(Color.WHITE);
        v.setFont(AppFonts.bold(24));

        card.add(t, BorderLayout.NORTH);
        card.add(v, BorderLayout.CENTER);
        return v;
    }

    private JPanel createTableCard() {
        RoundedPanel panel = new RoundedPanel(16, Color.WHITE);
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1, MainFrame.SECONDARY_BTN_BG, 16),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel header = new JLabel("Boarding Queue");
        header.setFont(AppFonts.bold(15));
        header.setForeground(new Color(27, 77, 46));

        String[] cols = {"Ticket No", "Passenger", "Cabin", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(tableModel);
        styleTable(table);
        table.setTableHeader(null);

        JPanel tableOuter = new JPanel(new BorderLayout(0, 10));
        tableOuter.setOpaque(false);

        JPanel headerPanel = new JPanel(new GridLayout(1, cols.length, 0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
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
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));

        btnOpenBoarding = createColoredButton("Open Boarding", new Color(46, 160, 90));
        btnCallNextGroup = createColoredButton("Call Next Group", new Color(52, 152, 219));
        btnMarkBoarded = createColoredButton("Mark Boarded", new Color(45, 190, 180));
        btnFinalCall = createColoredButton("Final Call", new Color(245, 166, 35));
        btnCloseGate = createColoredButton("Close Gate", new Color(220, 70, 70));

        panel.add(btnOpenBoarding);
        panel.add(btnCallNextGroup);
        panel.add(btnMarkBoarded);
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
        combo.setForeground(new Color(27, 77, 46));
        combo.setPreferredSize(new Dimension(150, 42));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        combo.setMinimumSize(new Dimension(80, 42));
        combo.setFocusable(false);
        combo.setMaximumRowCount(8);

        combo.setUI(new ModernComboBoxUI());
        combo.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1, MainFrame.SECONDARY_BTN_BG, 10),
                BorderFactory.createEmptyBorder(2, 6, 2, 4)
        ));

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                        int index, boolean isSelected,
                                                        boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                label.setFont(AppFonts.regular(13));
                label.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                label.setOpaque(true);
                if (isSelected) {
                    label.setBackground(new Color(46, 160, 90));
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(new Color(27, 77, 46));
                }
                return label;
            }
        });
    }

    private static class ModernComboBoxUI extends javax.swing.plaf.basic.BasicComboBoxUI {
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

    private void styleTable(JTable t) {
    t.setRowHeight(42);
    t.setFont(AppFonts.regular(13));
    t.setShowVerticalLines(false);
    t.setShowHorizontalLines(true);
    t.setGridColor(new Color(230, 233, 238));
    t.setIntercellSpacing(new Dimension(0, 1));
    t.setFillsViewportHeight(true);
    t.setSelectionBackground(new Color(230, 245, 235));
    t.setSelectionForeground(new Color(27, 77, 46));
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

            if (!isSelected) {
                setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 251, 252));
            }

            if (column == 3 && value != null) {
                String status = value.toString();
                if ("BOARDED".equalsIgnoreCase(status)) {
                    setForeground(new Color(45, 190, 180));
                } else if ("NO-SHOW".equalsIgnoreCase(status)) {
                    setForeground(new Color(220, 70, 70));
                } else if ("SERVING".equalsIgnoreCase(status) || "WAITING".equalsIgnoreCase(status)) {
                    setForeground(new Color(52, 152, 219));
                } else {
                    setForeground(new Color(40, 40, 40));
                }
            } else {
                setForeground(new Color(40, 40, 40));
            }
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
                BorderFactory.createEmptyBorder(24, 28, 24, 28)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppFonts.bold(16));
        lblTitle.setForeground(isError ? new Color(180, 50, 50) : new Color(27, 77, 46));

        JLabel lblMsg = new JLabel("<html><body style='width:260px'>" + message.replace("\n", "<br>") + "</body></html>");
        lblMsg.setFont(AppFonts.regular(13));
        lblMsg.setForeground(new Color(50, 65, 55));

        JButton ok = createColoredButton("OK", isError ? new Color(180, 50, 50) : new Color(46, 160, 90));
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

    private boolean showModernConfirm(String message, String title) {
        final boolean[] result = {false};

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());

        RoundedPanel card = new RoundedPanel(16, Color.WHITE);
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1, MainFrame.SECONDARY_BTN_BG, 16),
                BorderFactory.createEmptyBorder(24, 28, 24, 28)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppFonts.bold(16));
        lblTitle.setForeground(new Color(27, 77, 46));

        JLabel lblMsg = new JLabel("<html><body style='width:280px'>" + message.replace("\n", "<br>") + "</body></html>");
        lblMsg.setFont(AppFonts.regular(13));
        lblMsg.setForeground(new Color(50, 65, 55));

        JButton no = createColoredButton("Cancel", new Color(120, 130, 125));
        no.setPreferredSize(new Dimension(100, 36));
        no.addActionListener(e -> dialog.dispose());

        JButton yes = createColoredButton("Confirm", new Color(220, 70, 70));
        yes.setPreferredSize(new Dimension(100, 36));
        yes.addActionListener(e -> {
            result[0] = true;
            dialog.dispose();
        });

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

    private void setBoardingControlsEnabled(boolean enabled) {
        btnCallNextGroup.setEnabled(enabled);
        btnMarkBoarded.setEnabled(enabled);
        btnFinalCall.setEnabled(enabled);
        btnCloseGate.setEnabled(enabled);
        btnOpenBoarding.setEnabled(!enabled && selectedFlight != null);
    }

    private void loadFlights() {
        new SwingWorker<List<Map<String, String>>, Void>() {
            @Override
            protected List<Map<String, String>> doInBackground() throws Exception {
                return FirebaseHelper.getAllFlights();
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, String>> flights = get();
                    cmbFlight.removeAllItems();
                    if (flights != null) {
                        for (Map<String, String> f : flights) {
                            String no = f.getOrDefault("flightNo", "");
                            if (!no.isEmpty()) cmbFlight.addItem(no);
                        }
                    }
                } catch (Exception ex) {
                    showModernMessage("Failed to load flights.", "Error", true);
                }
            }
        }.execute();
    }

    private void onFlightSelected() {
        selectedFlight = (String) cmbFlight.getSelectedItem();
        boardingOpen = false;
        currentGroup = 0;
        setBoardingControlsEnabled(false);
        lblCurrentGroup.setText("—");

        if (selectedFlight == null) return;

        new SwingWorker<Map<String, String>, Void>() {
            @Override
            protected Map<String, String> doInBackground() throws Exception {
                List<Map<String, String>> flights = FirebaseHelper.getAllFlights();
                if (flights != null) {
                    for (Map<String, String> f : flights) {
                        if (selectedFlight.equals(f.get("flightNo"))) return f;
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    Map<String, String> f = get();
                    if (f != null) {
                        lblGate.setText("Gate: " + f.getOrDefault("gate", "—"));
                        lblDeparture.setText("Departure: " + f.getOrDefault("departure", "—"));
                        lblFlightStatus.setText("Status: " + f.getOrDefault("status", "—"));
                    }
                    loadPassengers();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void loadPassengers() {
        if (selectedFlight == null) return;

        new SwingWorker<List<Map<String, String>>, Void>() {
            @Override
            protected List<Map<String, String>> doInBackground() throws Exception {
                List<Map<String, String>> all = FirebaseHelper.getAllTickets();
                List<Map<String, String>> forFlight = new ArrayList<>();
                if (all != null) {
                    for (Map<String, String> t : all) {
                        if (selectedFlight.equalsIgnoreCase(t.get("flightNo"))) {
                            forFlight.add(t);
                        }
                    }
                }
                forFlight.sort((a, b) -> {
                    boolean aBoarded = "BOARDED".equalsIgnoreCase(a.get("status"));
                    boolean bBoarded = "BOARDED".equalsIgnoreCase(b.get("status"));
                    if (aBoarded != bBoarded) return aBoarded ? 1 : -1;
                    return String.valueOf(a.get("ticketNo")).compareTo(String.valueOf(b.get("ticketNo")));
                });
                return forFlight;
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, String>> list = get();
                    tableModel.setRowCount(0);
                    int boarded = 0;
                    int remaining = 0;

                    if (list != null) {
                        for (Map<String, String> t : list) {
                            String status = t.getOrDefault("status", "");
                            String cabin = assignCabin(t);

                            tableModel.addRow(new Object[]{
                                t.get("ticketNo"),
                                t.get("name"),
                                cabin,
                                status
                            });

                            if ("BOARDED".equalsIgnoreCase(status)) {
                                boarded++;
                            } else {
                                remaining++;
                            }
                        }
                    }
                    lblBoarded.setText(String.valueOf(boarded));
                    lblRemaining.setText(String.valueOf(remaining));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private String assignCabin(Map<String, String> ticket) {
        String cabin = ticket.get("cabin");
        if (cabin == null || cabin.trim().isEmpty()) {
            cabin = ticket.get("seatClass");
        }
        if (cabin == null || cabin.trim().isEmpty()) {
            return "Economy";
        }
        return cabin;
    }

    private void openBoarding() {
        if (selectedFlight == null) return;
        boardingOpen = true;
        currentGroup = 0;
        lblCurrentGroup.setText(CABIN_ORDER[0]);
        setBoardingControlsEnabled(true);
        updateFlightStatus("Boarding");
        showModernMessage("Boarding is now open for " + selectedFlight +
                "\nNow calling: " + CABIN_ORDER[0], "Boarding Open", false);
        loadPassengers();
    }

    private void callNextGroup() {
        if (!boardingOpen) return;
        if (currentGroup < CABIN_ORDER.length - 1) {
            currentGroup++;
            lblCurrentGroup.setText(CABIN_ORDER[currentGroup]);
            showModernMessage("Now calling: " + CABIN_ORDER[currentGroup] +
                    " for flight " + selectedFlight, "Next Group", false);
        } else {
            showModernMessage("All cabins have been called.", "Notice", false);
        }
    }

    private void markSelectedBoarded() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showModernMessage("Select a passenger first.", "Notice", false);
            return;
        }

        String ticketNo = String.valueOf(tableModel.getValueAt(row, 0));
        String currentStatus = String.valueOf(tableModel.getValueAt(row, 3));

        if ("BOARDED".equalsIgnoreCase(currentStatus)) {
            showModernMessage("This passenger is already boarded.", "Notice", false);
            return;
        }

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                FirebaseHelper.put("tickets/" + ticketNo + "/status", "\"BOARDED\"");
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    loadPassengers();
                } catch (Exception ex) {
                    showModernMessage("Failed to update ticket.", "Error", true);
                }
            }
        }.execute();
    }

    private void finalCall() {
        if (!boardingOpen) return;
        lblCurrentGroup.setText("FINAL CALL");
        showModernMessage("FINAL CALL for flight " + selectedFlight +
                "\nAll remaining passengers please proceed to the gate.", "Final Call", false);
    }

    private void closeGate() {
        if (!showModernConfirm("Close the gate for " + selectedFlight +
                "?\nRemaining passengers will be marked as NO-SHOW.", "Close Gate")) return;

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                List<Map<String, String>> all = FirebaseHelper.getAllTickets();
                if (all != null) {
                    for (Map<String, String> t : all) {
                        if (selectedFlight.equalsIgnoreCase(t.get("flightNo"))) {
                            String status = t.get("status");
                            if (status != null && !status.equalsIgnoreCase("BOARDED")) {
                                String ticketNo = t.get("ticketNo");
                                FirebaseHelper.put("tickets/" + ticketNo + "/status", "\"NO-SHOW\"");
                            }
                        }
                    }
                }
                FirebaseHelper.put("flights/" + selectedFlight + "/status", "\"Departed\"");
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    boardingOpen = false;
                    setBoardingControlsEnabled(false);
                    lblCurrentGroup.setText("GATE CLOSED");
                    lblFlightStatus.setText("Status: Departed");
                    loadPassengers();
                    showModernMessage("Gate closed. Flight " + selectedFlight + " is now Departed.", "Gate Closed", false);
                } catch (Exception ex) {
                    showModernMessage("Failed to close gate.", "Error", true);
                }
            }
        }.execute();
    }

    private void updateFlightStatus(String status) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                FirebaseHelper.put("flights/" + selectedFlight + "/status", "\"" + status + "\"");
                return null;
            }

            @Override
            protected void done() {
                lblFlightStatus.setText("Status: " + status);
            }
        }.execute();
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
            return new Insets(8, 14, 8, 14);
        }
    }
}