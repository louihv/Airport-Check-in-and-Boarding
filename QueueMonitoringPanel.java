import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.plaf.basic.BasicComboBoxUI;

public class QueueMonitoringPanel extends JPanel {
    private static final String SERVED_BY_KEY = "servedBy";

    private final List<Map<String, String>> allTickets = new ArrayList<>();
    private final List<Map<String, String>> visibleTickets = new ArrayList<>();
    private String staffId = "";
    private boolean admin;
    private JTable queueTable;
    private DefaultTableModel tableModel;
    private JButton btnRefresh;
    private JButton btnExport;

    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JComboBox<String> counterFilter;
    private JComboBox<String> servedByFilter;
    private JPanel filterRow;
    private JPanel headerPanel;
    private String[] currentColumns;

    public QueueMonitoringPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(MainFrame.MAIN_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        currentColumns = new String[]{"Ticket No.", "Booking Ref", "Name", "Flight", "Counter", "Status"};
        tableModel = new DefaultTableModel(currentColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        queueTable = new JTable(tableModel);
        styleTable(queueTable);
        queueTable.setTableHeader(null);

        JPanel outerCard = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 18, 18));
                g2.dispose();
            }
        };
        outerCard.setOpaque(false);
        outerCard.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel toolbar = new JPanel(new BorderLayout(12, 10));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));

        JPanel searchPanel = createSearchPanel();
        toolbar.add(searchPanel, BorderLayout.WEST);

        filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterRow.setOpaque(false);

        statusFilter = createFilterCombo("All Status");
        counterFilter = createFilterCombo("All Counters");
        servedByFilter = createFilterCombo("All Staff");

        filterRow.add(createFilterLabel("Status"));
        filterRow.add(statusFilter);
        filterRow.add(createFilterLabel("Counter"));
        filterRow.add(counterFilter);

        toolbar.add(filterRow, BorderLayout.CENTER);

        JButton btnClearFilters = createSmallOutlineButton("Clear");
        btnClearFilters.addActionListener(e -> clearFilters());
        JPanel clearWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        clearWrap.setOpaque(false);
        clearWrap.add(btnClearFilters);
        toolbar.add(clearWrap, BorderLayout.EAST);

        headerPanel = createHeaderPanel(currentColumns);

        JPanel bodyCard = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
            }
        };
        bodyCard.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(queueTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setOpaque(false);
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        bodyCard.add(scrollPane, BorderLayout.CENTER);

        outerCard.add(toolbar, BorderLayout.NORTH);

        JPanel tableSection = new JPanel(new BorderLayout(0, 0));
        tableSection.setOpaque(false);
        tableSection.add(headerPanel, BorderLayout.NORTH);
        tableSection.add(bodyCard, BorderLayout.CENTER);
        outerCard.add(tableSection, BorderLayout.CENTER);

        btnRefresh = createOutlineButton("Refresh Tickets");
        btnExport = createOutlineButton("Export PDF");

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottom.setOpaque(false);
        bottom.add(btnExport);
        bottom.add(btnRefresh);

        add(outerCard, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> loadTickets());
        btnExport.addActionListener(e -> exportPdf());

        statusFilter.addActionListener(e -> applyFilters());
        counterFilter.addActionListener(e -> applyFilters());
        servedByFilter.addActionListener(e -> applyFilters());

        setFocusable(true);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
            }
        });
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 0)) {
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
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(280, 38));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

        JLabel icon = new JLabel("\uD83D\uDD0D"); // 🔍
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        icon.setForeground(new Color(140, 150, 160));
        panel.add(icon, BorderLayout.WEST);

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

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        panel.add(searchField, BorderLayout.CENTER);
        return panel;
    }

    private JLabel createFilterLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppFonts.regular(12));
        lbl.setForeground(new Color(120, 130, 140));
        return lbl;
    }

    private JComboBox<String> createFilterCombo(String firstItem) {
        JComboBox<String> combo = new JComboBox<>();
        combo.setFont(AppFonts.regular(12));
        combo.setOpaque(false);
        combo.setBackground(new Color(0, 0, 0, 0));
        combo.setForeground(new Color(40, 55, 50));
        combo.setPreferredSize(new Dimension(140, 36));

        combo.setUI(new ModernComboBoxUI());
        combo.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(MainFrame.NAV_BTN_BG, 25, 1.2f),
                BorderFactory.createEmptyBorder(5, 0, 5, 8)
        ));

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                        boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setFont(AppFonts.regular(12));
                label.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
                label.setOpaque(true);
                if (isSelected) {
                    label.setBackground(MainFrame.NAV_BTN_BG);
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(new Color(40, 55, 50));
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

        combo.addItem(firstItem);
        return combo;
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

    private JButton createSmallOutlineButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isPressed()) {
                    g.setColor(new Color(0, 0, 0, 30));
                    g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                } else if (getModel().isRollover()) {
                    g.setColor(new Color(0, 0, 0, 15));
                    g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
                super.paintComponent(g);
            }
        };
        btn.setFont(AppFonts.bold(12));
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setForeground(MainFrame.NAV_BTN_BG);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new RoundedOutlineBorder(1, new Color(216, 203, 194), 10));
        btn.setPreferredSize(new Dimension(70, 34));
        return btn;
    }

    private JPanel createHeaderPanel(String[] columns) {
        JPanel panel = new JPanel(new GridLayout(1, columns.length, 0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(MainFrame.MAIN_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 18, 18));
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        panel.setPreferredSize(new Dimension(0, 46));

        for (String col : columns) {
            JLabel lbl = new JLabel(col);
            lbl.setForeground(MainFrame.SECONDARY_BTN_BG);
            lbl.setFont(AppFonts.bold(12));
            lbl.setHorizontalAlignment(SwingConstants.LEFT);
            panel.add(lbl);
        }
        return panel;
    }

    private void rebuildTableForRole() {
        currentColumns = admin
                ? new String[]{"Ticket No.", "Booking Ref", "Name", "Flight", "Counter", "Status", "Served By"}
                : new String[]{"Ticket No.", "Booking Ref", "Name", "Flight", "Counter", "Status"};

        tableModel = new DefaultTableModel(currentColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        queueTable.setModel(tableModel);
        styleTable(queueTable);

        Container parent = headerPanel.getParent();
        if (parent != null) {
            parent.remove(headerPanel);
            headerPanel = createHeaderPanel(currentColumns);
            parent.add(headerPanel, BorderLayout.NORTH);
            parent.revalidate();
            parent.repaint();
        }

        filterRow.removeAll();
        filterRow.add(createFilterLabel("Status"));
        filterRow.add(statusFilter);
        filterRow.add(createFilterLabel("Counter"));
        filterRow.add(counterFilter);
        if (admin) {
            filterRow.add(createFilterLabel("Served By"));
            filterRow.add(servedByFilter);
        }
        filterRow.revalidate();
        filterRow.repaint();
    }

    public void setUser(String username, String role) {
        this.staffId = username == null ? "" : username.trim();
        this.admin = "ADMIN".equals(role);
        rebuildTableForRole();
        loadTickets();
    }

    public void clear() {
        staffId = "";
        admin = false;
        allTickets.clear();
        visibleTickets.clear();
        tableModel.setRowCount(0);
        if (searchField != null) searchField.setText("");
        clearFilters();
    }

    private void clearFilters() {
        if (statusFilter != null) statusFilter.setSelectedIndex(0);
        if (counterFilter != null) counterFilter.setSelectedIndex(0);
        if (servedByFilter != null) servedByFilter.setSelectedIndex(0);
        if (searchField != null) searchField.setText("");
        applyFilters();
    }

    private void applyFilters() {
        visibleTickets.clear();
        tableModel.setRowCount(0);

        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        String statusSel = statusFilter != null && statusFilter.getSelectedItem() != null
                ? statusFilter.getSelectedItem().toString() : "All Status";
        String counterSel = counterFilter != null && counterFilter.getSelectedItem() != null
                ? counterFilter.getSelectedItem().toString() : "All Counters";
        String servedBySel = servedByFilter != null && servedByFilter.getSelectedItem() != null
                ? servedByFilter.getSelectedItem().toString() : "All Staff";

        for (Map<String, String> t : allTickets) {
            if (!isServedByCurrentUser(t)) continue;

            if (!"All Status".equals(statusSel)) {
                String st = t.get("status");
                if (st == null || !st.equalsIgnoreCase(statusSel)) continue;
            }

            if (!"All Counters".equals(counterSel)) {
                String disp = counterDisplay(t);
                if (!disp.equals(counterSel)) continue;
            }

            if (admin && !"All Staff".equals(servedBySel)) {
                String sb = t.get(SERVED_BY_KEY);
                if (sb == null || !sb.trim().equalsIgnoreCase(servedBySel)) continue;
            }

            if (!query.isEmpty()) {
                String haystack = (
                        nullToEmpty(t.get("ticketNo")) + " " +
                        nullToEmpty(t.get("bookingRef")) + " " +
                        nullToEmpty(t.get("name")) + " " +
                        nullToEmpty(t.get("flightNo")) + " " +
                        counterDisplay(t) + " " +
                        nullToEmpty(t.get("status")) + " " +
                        nullToEmpty(t.get(SERVED_BY_KEY))
                ).toLowerCase();
                if (!haystack.contains(query)) continue;
            }

            visibleTickets.add(t);
            addRow(t);
        }
    }

    private void addRow(Map<String, String> t) {
        if (admin) {
            tableModel.addRow(new Object[]{
                    t.get("ticketNo"),
                    t.get("bookingRef"),
                    t.get("name"),
                    t.get("flightNo"),
                    counterDisplay(t),
                    t.get("status"),
                    nullToEmpty(t.get(SERVED_BY_KEY)).isEmpty() ? "—" : t.get(SERVED_BY_KEY)
            });
        } else {
            tableModel.addRow(new Object[]{
                    t.get("ticketNo"),
                    t.get("bookingRef"),
                    t.get("name"),
                    t.get("flightNo"),
                    counterDisplay(t),
                    t.get("status")
            });
        }
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private void populateFilterOptions() {
        Set<String> statuses = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        Set<String> counters = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        Set<String> staff = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        for (Map<String, String> t : allTickets) {
            if (!isServedByCurrentUser(t)) continue;
            String st = t.get("status");
            if (st != null && !st.isEmpty()) statuses.add(st);
            counters.add(counterDisplay(t));
            String sb = t.get(SERVED_BY_KEY);
            if (sb != null && !sb.trim().isEmpty()) staff.add(sb.trim());
        }

        Object prevStatus = statusFilter.getSelectedItem();
        statusFilter.removeAllItems();
        statusFilter.addItem("All Status");
        for (String s : statuses) statusFilter.addItem(s);
        if (prevStatus != null) statusFilter.setSelectedItem(prevStatus);

        Object prevCounter = counterFilter.getSelectedItem();
        counterFilter.removeAllItems();
        counterFilter.addItem("All Counters");
        for (String c : counters) counterFilter.addItem(c);
        if (prevCounter != null) counterFilter.setSelectedItem(prevCounter);

        Object prevServed = servedByFilter.getSelectedItem();
        servedByFilter.removeAllItems();
        servedByFilter.addItem("All Staff");
        for (String s : staff) servedByFilter.addItem(s);
        if (prevServed != null) servedByFilter.setSelectedItem(prevServed);
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
        btn.setPreferredSize(new Dimension(160, 38));
        return btn;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(42);
        table.setFont(AppFonts.regular(13));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(230, 233, 238));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(245, 248, 250));
        table.setSelectionForeground(new Color(40, 40, 40));
        table.setBackground(Color.WHITE);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));
                setHorizontalAlignment(JLabel.LEFT);

                if (!isSelected) {
                    if (row % 2 == 0) {
                        setBackground(Color.WHITE);
                    } else {
                        setBackground(new Color(250, 251, 252));
                    }
                }

                int statusCol = admin ? 5 : 5;
                if (column == statusCol && value != null) {
                    String status = value.toString();
                    if ("Pending".equalsIgnoreCase(status) || "Waiting".equalsIgnoreCase(status)) {
                        setForeground(new Color(245, 166, 35));
                    } else if ("Delivered".equalsIgnoreCase(status) || "Completed".equalsIgnoreCase(status)
                            || "Served".equalsIgnoreCase(status)) {
                        setForeground(new Color(45, 190, 180));
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

    private static class ModernScrollBarUI extends BasicScrollBarUI {
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

    private void showModernMessage(String message, String title, boolean isError) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setLayout(new BorderLayout());

        JPanel content = (JPanel) dialog.getContentPane();
        content.setOpaque(false);
        content.setLayout(new BorderLayout());

        JPanel card = new JPanel(new BorderLayout(0, 16)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1, MainFrame.SECONDARY_BTN_BG, 16),
                BorderFactory.createEmptyBorder(24, 28, 24, 28)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppFonts.bold(16));
        lblTitle.setForeground(isError ? new Color(180, 50, 50) : MainFrame.NAV_BTN_BG);

        JLabel lblMsg = new JLabel("<html><body style='width:280px'>" + message.replace("\n", "<br>") + "</body></html>");
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
            return new Insets(8, 16, 8, 16);
        }
    }

    private boolean isServedByCurrentUser(Map<String, String> ticket) {
        if (admin) return true;
        String servedBy = ticket.get(SERVED_BY_KEY);
        return !staffId.isEmpty() && servedBy != null && servedBy.trim().equalsIgnoreCase(staffId);
    }

    private static String counterDisplay(Map<String, String> t) {
        String counter = t.get("counter");
        return (counter == null || counter.equals("0") || counter.isEmpty())
                ? "Unassigned"
                : "Counter " + counter;
    }

    private void loadTickets() {
        if (!admin && staffId.isEmpty()) return;

        btnRefresh.setEnabled(false);
        btnExport.setEnabled(false);

        new SwingWorker<List<Map<String, String>>, Void>() {
            @Override
            protected List<Map<String, String>> doInBackground() throws Exception {
                List<Map<String, String>> result = new ArrayList<>();

                List<Map<String, String>> main = FirebaseHelper.getAllTickets();
                if (main != null) {
                    for (Map<String, String> t : main) {
                        Map<String, String> copy = new HashMap<>(t);
                        copy.put("source", "tickets");
                        result.add(copy);
                    }
                }

                if (admin) {
                    List<Map<String, String>> pending = FirebaseHelper.getAllPendingTickets();
                    if (pending != null) {
                        for (Map<String, String> t : pending) {
                            Map<String, String> copy = new HashMap<>(t);
                            copy.put("source", "pendingTickets");
                            copy.putIfAbsent("status", "Pending");
                            result.add(copy);
                        }
                    }
                }
                return result;
            }

            @Override
            protected void done() {
                btnRefresh.setEnabled(true);
                btnExport.setEnabled(true);
                try {
                    List<Map<String, String>> tickets = get();
                    allTickets.clear();
                    if (tickets != null) {
                        allTickets.addAll(tickets);
                    }
                    populateFilterOptions();
                    applyFilters();
                } catch (Exception ex) {
                    showModernMessage(
                            "Failed to load tickets.\nCheck internet / Firebase URL.",
                            "Error", true);
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void exportPdf() {
        if (visibleTickets.isEmpty()) {
            showModernMessage("There are no customers to export.", "Export PDF", true);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save PDF");
        chooser.setSelectedFile(new File("served_customers.pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            file = new File(file.getParentFile(), file.getName() + ".pdf");
        }

        try {
            Files.write(file.toPath(), buildPdf());
            showModernMessage("PDF saved to:\n" + file.getAbsolutePath(), "Export PDF", false);
        } catch (IOException ex) {
            showModernMessage("Failed to save PDF.\n" + ex.getMessage(), "Error", true);
        }
    }

    private byte[] buildPdf() throws IOException {
        final int pageW = 842;
        final int pageH = 595;
        final int margin = 40;
        final int rowH = 22;

        final boolean showServedBy = admin;
        final int[] colX = showServedBy
                ? new int[]{40, 120, 230, 380, 480, 580, 680}
                : new int[]{40, 140, 260, 450, 560, 680};
        final String[] headers = showServedBy
                ? new String[]{"Ticket No.", "Booking Ref", "Name", "Flight", "Counter", "Status", "Served By"}
                : new String[]{"Ticket No.", "Booking Ref", "Name", "Flight", "Counter", "Status"};

        final int headerY = pageH - 105;
        final int rowsPerPage = (headerY - 30 - margin) / rowH;
        final int totalPages = (visibleTickets.size() + rowsPerPage - 1) / rowsPerPage;
        final String generated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        final String title = admin ? "All Customers" : "Served Customers - " + staffId;

        List<String> streams = new ArrayList<>();
        for (int p = 0; p < totalPages; p++) {
            StringBuilder sb = new StringBuilder();
            pdfText(sb, "F2", 16, margin, pageH - 55, title);
            pdfText(sb, "F1", 10, margin, pageH - 75,
                    "Total: " + visibleTickets.size() + "    Generated: " + generated);

            for (int c = 0; c < headers.length; c++) {
                pdfText(sb, "F2", 10, colX[c], headerY, headers[c]);
            }
            sb.append("0.6 w ").append(margin).append(' ').append(headerY - 6).append(" m ")
                    .append(pageW - margin).append(' ').append(headerY - 6).append(" l S\n");

            int start = p * rowsPerPage;
            int end = Math.min(start + rowsPerPage, visibleTickets.size());
            int y = headerY - 6 - rowH + 6;
            for (int i = start; i < end; i++) {
                Map<String, String> t = visibleTickets.get(i);
                String[] cells;
                if (showServedBy) {
                    cells = new String[]{
                            t.get("ticketNo"), t.get("bookingRef"), t.get("name"),
                            t.get("flightNo"), counterDisplay(t), t.get("status"),
                            nullToEmpty(t.get(SERVED_BY_KEY)).isEmpty() ? "—" : t.get(SERVED_BY_KEY)
                    };
                } else {
                    cells = new String[]{
                            t.get("ticketNo"), t.get("bookingRef"), t.get("name"),
                            t.get("flightNo"), counterDisplay(t), t.get("status")
                    };
                }
                for (int c = 0; c < cells.length; c++) {
                    int width = (c + 1 < colX.length ? colX[c + 1] : pageW - margin) - colX[c] - 8;
                    pdfText(sb, "F1", 10, colX[c], y, fit(cells[c], width / 5));
                }
                y -= rowH;
            }

            pdfText(sb, "F1", 9, pageW / 2 - 30, margin - 15, "Page " + (p + 1) + " of " + totalPages);
            streams.add(sb.toString());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();
        writeAscii(out, "%PDF-1.4\n");

        StringBuilder kids = new StringBuilder();
        for (int p = 0; p < totalPages; p++) {
            kids.append(5 + p * 2).append(" 0 R ");
        }

        pdfObject(out, offsets, 1, "<< /Type /Catalog /Pages 2 0 R >>");
        pdfObject(out, offsets, 2, "<< /Type /Pages /Kids [" + kids + "] /Count " + totalPages + " >>");
        pdfObject(out, offsets, 3, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>");
        pdfObject(out, offsets, 4, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>");

        for (int p = 0; p < totalPages; p++) {
            int pageId = 5 + p * 2;
            int contentId = pageId + 1;
            byte[] content = streams.get(p).getBytes(StandardCharsets.ISO_8859_1);
            pdfObject(out, offsets, pageId,
                    "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + pageW + " " + pageH + "] "
                            + "/Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents " + contentId + " 0 R >>");
            offsets.add(out.size());
            writeAscii(out, contentId + " 0 obj\n<< /Length " + content.length + " >>\nstream\n");
            out.write(content);
            writeAscii(out, "\nendstream\nendobj\n");
        }

        int xrefPos = out.size();
        StringBuilder xref = new StringBuilder();
        xref.append("xref\n0 ").append(offsets.size() + 1).append("\n0000000000 65535 f \n");
        for (int off : offsets) {
            xref.append(String.format("%010d 00000 n \n", off));
        }
        xref.append("trailer\n<< /Size ").append(offsets.size() + 1).append(" /Root 1 0 R >>\nstartxref\n")
                .append(xrefPos).append("\n%%EOF\n");
        writeAscii(out, xref.toString());

        return out.toByteArray();
    }

    private static void pdfObject(ByteArrayOutputStream out, List<Integer> offsets, int id, String body) throws IOException {
        offsets.add(out.size());
        writeAscii(out, id + " 0 obj\n" + body + "\nendobj\n");
    }

    private static void writeAscii(ByteArrayOutputStream out, String s) throws IOException {
        out.write(s.getBytes(StandardCharsets.ISO_8859_1));
    }

    private static void pdfText(StringBuilder sb, String font, int size, int x, int y, String text) {
        sb.append("BT /").append(font).append(' ').append(size).append(" Tf ")
                .append(x).append(' ').append(y).append(" Td (")
                .append(pdfEscape(text)).append(") Tj ET\n");
    }

    private static String pdfEscape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char ch : s.toCharArray()) {
            if (ch == '\\' || ch == '(' || ch == ')') {
                sb.append('\\').append(ch);
            } else if (ch < 32 || ch > 255) {
                sb.append('?');
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    private static String fit(String s, int maxChars) {
        if (s == null) return "";
        return s.length() <= maxChars ? s : s.substring(0, Math.max(0, maxChars - 1)) + "...";
    }

    
}