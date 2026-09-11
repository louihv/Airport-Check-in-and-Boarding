import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class FlightSchedulerPanel extends JPanel {
    private DefaultTableModel tableModel;
    private JTextField txtFlightNo, txtOrigin, txtDestination, txtDeparture, txtGate;
    private JComboBox<String> cmbStatus;
    private JButton btnSave, btnRefresh;
    private int nextFlightNum = 20;
    private MainFrame mainFrame;

    public FlightSchedulerPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(MainFrame.MAIN_BG);
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Flight Scheduler");
        title.setFont(AppFonts.bold(22));
        title.setForeground(new Color(40, 40, 40));
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 20, 0));
        center.setOpaque(false);

        JPanel left = createRoundedCard();
        left.setLayout(new BorderLayout(0, 16));
        left.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel leftHeader = new JPanel(new BorderLayout(0, 10));
        leftHeader.setOpaque(false);

        JLabel leftTitle = new JLabel("Add / Update Flight");
        leftTitle.setFont(AppFonts.bold(16));
        leftTitle.setForeground(new Color(40, 40, 40));

        JSeparator divider = new JSeparator();
        divider.setForeground(new Color(220, 220, 220));
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        leftHeader.add(leftTitle, BorderLayout.NORTH);
        leftHeader.add(divider, BorderLayout.SOUTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        txtFlightNo = createOutlineField();
        txtFlightNo.setText("FL020");
        txtOrigin = createOutlineField();
        txtOrigin.setText("Manila");
        txtDestination = createOutlineField();
        txtDeparture = createOutlineField();
        txtDeparture.setText("08:30 AM");
        txtGate = createOutlineField();
        txtGate.setText("A1");
        cmbStatus = new JComboBox<>(new String[]{"On Time", "Delayed", "Boarding", "Departed", "Cancelled"});
        styleCombo(cmbStatus);

        form.add(createFieldBlock("Flight No:", txtFlightNo));
        form.add(Box.createVerticalStrut(12));
        form.add(createFieldBlock("Origin:", txtOrigin));
        form.add(Box.createVerticalStrut(12));
        form.add(createFieldBlock("Destination:", txtDestination));
        form.add(Box.createVerticalStrut(12));
        form.add(createFieldBlock("Departure:", txtDeparture));
        form.add(Box.createVerticalStrut(12));
        form.add(createFieldBlock("Gate:", txtGate));
        form.add(Box.createVerticalStrut(12));
        form.add(createFieldBlock("Status:", cmbStatus));

        btnSave = createFilledButton("Save Flight");

        left.add(leftHeader, BorderLayout.NORTH);
        left.add(form, BorderLayout.CENTER);
        left.add(btnSave, BorderLayout.SOUTH);

        JPanel right = createRoundedCard();
        right.setLayout(new BorderLayout(0, 12));
        right.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel rightTitle = new JLabel("Scheduled Flights");
        rightTitle.setFont(AppFonts.bold(16));
        rightTitle.setForeground(new Color(40, 40, 40));

        String[] cols = {"Flight No", "Origin", "Destination", "Departure", "Gate", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
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

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setOpaque(false);
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        bodyCard.add(scrollPane, BorderLayout.CENTER);

        tableOuter.add(headerPanel, BorderLayout.NORTH);
        tableOuter.add(bodyCard, BorderLayout.CENTER);

        btnRefresh = createOutlineButton("Refresh Flights");

        right.add(rightTitle, BorderLayout.NORTH);
        right.add(tableOuter, BorderLayout.CENTER);
        right.add(btnRefresh, BorderLayout.SOUTH);

        center.add(left);
        center.add(right);
        add(center, BorderLayout.CENTER);

        btnSave.addActionListener(e -> saveFlight());
        btnRefresh.addActionListener(e -> loadFlights());

        loadFlights();
    }

    private JPanel createRoundedCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 18, 18));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        return card;
    }

    private JPanel createFieldBlock(String labelText, JComponent field) {
        JPanel block = new JPanel();
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setOpaque(false);
        block.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(AppFonts.bold(13));
        label.setForeground(new Color(60, 60, 60));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        block.add(label);
        block.add(Box.createVerticalStrut(6));
        block.add(field);
        return block;
    }

    private JTextField createOutlineField() {
        JTextField field = new JTextField();
        field.setFont(AppFonts.regular(13));
        field.setBorder(new RoundedOutlineBorder(1, new Color(216, 203, 194), 10));
        field.setBackground(Color.WHITE);
        field.setOpaque(true);
        return field;
    }

    private void styleCombo(JComboBox<String> combo) {
        combo.setFont(AppFonts.regular(13));
        combo.setBackground(Color.WHITE);
        combo.setForeground(new Color(40, 40, 40));
        combo.setBorder(new RoundedOutlineBorder(1, new Color(216, 203, 194), 10));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        combo.setUI(new ModernComboBoxUI());
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
                if (isSelected) {
                    label.setBackground(MainFrame.NAV_BTN_BG);
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(new Color(40, 40, 40));
                }
                return label;
            }
        });
    }

    private JButton createFilledButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(MainFrame.NAV_BTN_BG.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(MainFrame.NAV_BTN_BG.brighter());
                } else {
                    g2.setColor(MainFrame.NAV_BTN_BG);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(AppFonts.bold(13));
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setForeground(MainFrame.MAIN_BG);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setPreferredSize(new Dimension(160, 40));
        return btn;
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

                if (column == 5 && value != null) {
                    String status = value.toString();
                    if ("Delayed".equalsIgnoreCase(status) || "Cancelled".equalsIgnoreCase(status)) {
                        setForeground(new Color(245, 166, 35));
                    } else if ("On Time".equalsIgnoreCase(status) || "Boarding".equalsIgnoreCase(status)
                            || "Departed".equalsIgnoreCase(status)) {
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
                    g2.setColor(new Color(120, 120, 120));
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

    private void saveFlight() {
        String flightNo = txtFlightNo.getText().trim().toUpperCase();
        String origin = txtOrigin.getText().trim();
        String destination = txtDestination.getText().trim();
        String departure = txtDeparture.getText().trim();
        String gate = txtGate.getText().trim();
        String status = (String) cmbStatus.getSelectedItem();

        if (flightNo.isEmpty() || origin.isEmpty() || destination.isEmpty() || departure.isEmpty() || gate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required");
            return;
        }

        btnSave.setEnabled(false);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                String json = String.format(
                    "{\"flightNo\":\"%s\",\"origin\":\"%s\",\"destination\":\"%s\",\"departure\":\"%s\",\"gate\":\"%s\",\"status\":\"%s\"}",
                    flightNo, origin, destination, departure, gate, status
                );
                FirebaseHelper.put("flights/" + flightNo, json);
                return null;
            }

            @Override
            protected void done() {
                btnSave.setEnabled(true);
                try {
                    get();
                    JOptionPane.showMessageDialog(FlightSchedulerPanel.this, "Flight saved successfully");
                    prepareNextFlightNo(flightNo);
                    loadFlights();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(FlightSchedulerPanel.this,
                        "Failed to save flight.\nCheck internet / Firebase URL.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void prepareNextFlightNo(String lastSaved) {
        try {
            if (lastSaved.startsWith("FL")) {
                int num = Integer.parseInt(lastSaved.substring(2));
                if (num >= nextFlightNum) {
                    nextFlightNum = num + 1;
                }
            }
        } catch (NumberFormatException ignored) {}
        txtFlightNo.setText(String.format("FL%03d", nextFlightNum));
        txtDestination.setText("");
        txtDeparture.setText("08:30 AM");
        txtGate.setText("A1");
        cmbStatus.setSelectedIndex(0);
    }

    private void loadFlights() {
        btnRefresh.setEnabled(false);

        new SwingWorker<List<Map<String, String>>, Void>() {
            @Override
            protected List<Map<String, String>> doInBackground() throws Exception {
                return FirebaseHelper.getAllFlights();
            }

            @Override
            protected void done() {
                btnRefresh.setEnabled(true);
                try {
                    List<Map<String, String>> flights = get();
                    tableModel.setRowCount(0);

                    int maxNum = 19;
                    if (flights != null) {
                        for (Map<String, String> f : flights) {
                            String flightNo = f.getOrDefault("flightNo", "-");
                            tableModel.addRow(new Object[]{
                                flightNo,
                                f.getOrDefault("origin", "-"),
                                f.getOrDefault("destination", "-"),
                                f.getOrDefault("departure", "-"),
                                f.getOrDefault("gate", "-"),
                                f.getOrDefault("status", "-")
                            });

                            if (flightNo.startsWith("FL")) {
                                try {
                                    int num = Integer.parseInt(flightNo.substring(2));
                                    if (num > maxNum) maxNum = num;
                                } catch (NumberFormatException ignored) {}
                            }
                        }
                    }
                    nextFlightNum = maxNum + 1;
                    txtFlightNo.setText(String.format("FL%03d", nextFlightNum));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(FlightSchedulerPanel.this,
                        "Failed to load flights.\nCheck internet / Firebase URL.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        }.execute();
    }
}