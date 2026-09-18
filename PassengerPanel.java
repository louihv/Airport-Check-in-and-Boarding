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
import javax.swing.table.JTableHeader;

public class PassengerPanel extends JPanel {
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtBookingRef, txtPassengerName, txtFlightId, txtDistance, txtDuration, txtPrice;
    private JComboBox<String> cmbStatus, cmbCabin;
    private JButton btnSave, btnRefresh, btnEdit, btnDelete;
    private MainFrame mainFrame;
    private String editingKey = null;

    public PassengerPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(MainFrame.MAIN_BG);
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Passenger Management");
        title.setFont(AppFonts.bold(22));
        title.setForeground(new Color(40, 40, 40));
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 18, 0));
        center.setOpaque(false);

        center.add(createFormCard());
        center.add(createTableCard());
        add(center, BorderLayout.CENTER);

        btnSave.addActionListener(e -> savePassenger());
        btnRefresh.addActionListener(e -> loadPassengers());
        btnEdit.addActionListener(e -> editSelected());
        btnDelete.addActionListener(e -> deleteSelected());

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) editSelected();
            }
        });

        loadPassengers();
    }

    private JPanel createFormCard() {
        JPanel card = createRoundedCard();
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel leftHeader = new JPanel(new BorderLayout(0, 10));
        leftHeader.setOpaque(false);
        JLabel leftTitle = new JLabel("Add / Update Passenger");
        leftTitle.setFont(AppFonts.bold(16));
        leftTitle.setForeground(new Color(40, 40, 40));
        JSeparator divider = new JSeparator();
        divider.setForeground(new Color(220, 220, 220));
        leftHeader.add(leftTitle, BorderLayout.NORTH);
        leftHeader.add(divider, BorderLayout.SOUTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        txtBookingRef = createOutlineField();
        txtPassengerName = createOutlineField();
        txtFlightId = createOutlineField();
        txtDistance = createOutlineField();
        txtDuration = createOutlineField();
        txtPrice = createOutlineField();

        cmbCabin = createStyledCombo(new String[]{"Economy", "Premium Economy", "Business", "First Class"});
        cmbStatus = createStyledCombo(new String[]{"On Time", "Delayed", "Boarding", "Departed", "Cancelled"});

        form.add(createFieldBlock("Booking Reference", txtBookingRef));
        form.add(Box.createVerticalStrut(11));
        form.add(createFieldBlock("Passenger Name", txtPassengerName));
        form.add(Box.createVerticalStrut(11));
        form.add(createFieldBlock("Flight ID", txtFlightId));
        form.add(Box.createVerticalStrut(11));
        form.add(createFieldBlock("Distance (Miles)", txtDistance));
        form.add(Box.createVerticalStrut(11));
        form.add(createFieldBlock("Duration (Minutes)", txtDuration));
        form.add(Box.createVerticalStrut(11));
        form.add(createFieldBlock("Price (USD)", txtPrice));
        form.add(Box.createVerticalStrut(11));
        form.add(createFieldBlock("Cabin Class", cmbCabin));
        form.add(Box.createVerticalStrut(11));
        form.add(createFieldBlock("Flight Status", cmbStatus));
        form.add(Box.createVerticalStrut(6));

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(BorderFactory.createEmptyBorder());
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        formScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        formScroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        formScroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));

        btnSave = createFilledButton("Save Passenger");

        card.add(leftHeader, BorderLayout.NORTH);
        card.add(formScroll, BorderLayout.CENTER);
        card.add(btnSave, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createTableCard() {
        JPanel card = createRoundedCard();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel rightTitle = new JLabel("All Passengers");
        rightTitle.setFont(AppFonts.bold(16));
        rightTitle.setForeground(new Color(40, 40, 40));

        String[] cols = {"Booking Ref", "Passenger Name", "Flight ID", "Cabin", "Price", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        styleTable(table);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] colWidths = {140, 170, 110, 130, 90, 110};
        for (int i = 0; i < colWidths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
            table.getColumnModel().getColumn(i).setMinWidth(90);
        }

        JTableHeader header = table.getTableHeader();
        header.setFont(AppFonts.bold(12));
        header.setForeground(Color.WHITE);
        header.setBackground(MainFrame.SECONDARY_BTN_BG);
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);
        header.setPreferredSize(new Dimension(header.getWidth(), 42));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setBackground(MainFrame.SECONDARY_BTN_BG);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(AppFonts.bold(12));
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
                lbl.setHorizontalAlignment(JLabel.LEFT);
                lbl.setOpaque(true);
                return lbl;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionRow.setOpaque(false);
        btnEdit = createOutlineButton("Edit");
        btnDelete = createOutlineButton("Delete");
        btnRefresh = createOutlineButton("Refresh");
        actionRow.add(btnEdit);
        actionRow.add(btnDelete);
        actionRow.add(btnRefresh);

        card.add(rightTitle, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        card.add(actionRow, BorderLayout.SOUTH);
        return card;
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
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

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
        field.setPreferredSize(new Dimension(0, 42));
        return field;
    }

    private JComboBox<String> createStyledCombo(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(AppFonts.regular(13));
        combo.setBackground(Color.WHITE);
        combo.setForeground(new Color(40, 40, 40));
        combo.setPreferredSize(new Dimension(0, 42));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        combo.setMinimumSize(new Dimension(80, 42));

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
        btn.setPreferredSize(new Dimension(160, 42));
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
        btn.setPreferredSize(new Dimension(110, 38));
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
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
                setHorizontalAlignment(JLabel.LEFT);

                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 251, 252));
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

    private void showInfo(String message) {
        showModernMessage(message, "Passenger Management", false);
    }

    private void showError(String message) {
        showModernMessage(message, "Error", true);
    }

    private void showWarn(String message) {
        showModernMessage(message, "Notice", true);
    }

    private boolean confirm(String message) {
        return showModernConfirm(message, "Confirm");
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

    private boolean showModernConfirm(String message, String title) {
        final boolean[] result = {false};

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
        lblTitle.setForeground(MainFrame.NAV_BTN_BG);

        JLabel lblMsg = new JLabel("<html><body style='width:280px'>" + message.replace("\n", "<br>") + "</body></html>");
        lblMsg.setFont(AppFonts.regular(13));
        lblMsg.setForeground(new Color(50, 65, 55));

        JButton no = new JButton("Cancel");
        no.setFont(AppFonts.bold(13));
        no.setBackground(new Color(120, 130, 125));
        no.setForeground(Color.WHITE);
        no.setFocusPainted(false);
        no.setCursor(new Cursor(Cursor.HAND_CURSOR));
        no.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        no.setOpaque(true);
        no.setContentAreaFilled(true);
        no.setPreferredSize(new Dimension(100, 36));
        no.addActionListener(e -> dialog.dispose());

        JButton yes = new JButton("Confirm");
        yes.setFont(AppFonts.bold(13));
        yes.setBackground(new Color(220, 70, 70));
        yes.setForeground(Color.WHITE);
        yes.setFocusPainted(false);
        yes.setCursor(new Cursor(Cursor.HAND_CURSOR));
        yes.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        yes.setOpaque(true);
        yes.setContentAreaFilled(true);
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

        content.add(card, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        return result[0];
    }

    private void savePassenger() {
        String bookingRef = txtBookingRef.getText().trim().toUpperCase();
        String name = txtPassengerName.getText().trim();
        String flightId = txtFlightId.getText().trim().toUpperCase();
        String distance = txtDistance.getText().trim();
        String duration = txtDuration.getText().trim();
        String price = txtPrice.getText().trim();
        String cabin = (String) cmbCabin.getSelectedItem();
        String status = (String) cmbStatus.getSelectedItem();

        if (bookingRef.isEmpty() || name.isEmpty() || flightId.isEmpty()
                || distance.isEmpty() || duration.isEmpty() || price.isEmpty()) {
            showWarn("Please fill in all required fields.");
            return;
        }

        btnSave.setEnabled(false);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (editingKey != null && !editingKey.equals(bookingRef)) {
                    FirebaseHelper.delete("passengers/" + editingKey);
                }

                String json = String.format(
                    "{\"bookingRef\":\"%s\",\"passengerName\":\"%s\",\"flightId\":\"%s\"," +
                    "\"distanceMiles\":%s,\"flightDurationMinutes\":%s,\"priceUsd\":%s," +
                    "\"cabin\":\"%s\",\"flightStatus\":\"%s\"}",
                    bookingRef, name, flightId, distance, duration, price, cabin, status
                );

                FirebaseHelper.put("passengers/" + bookingRef, json);
                return null;
            }

            @Override
            protected void done() {
                btnSave.setEnabled(true);
                try {
                    get();
                    showInfo(editingKey != null ? "Passenger updated successfully." : "Passenger saved successfully.");
                    editingKey = null;
                    btnSave.setText("Save Passenger");
                    clearForm();
                    loadPassengers();
                } catch (Exception ex) {
                    showError("Failed to save passenger.\nCheck your internet connection or Firebase URL.");
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showWarn("Please select a passenger from the table first.");
            return;
        }

        String bookingRef = String.valueOf(tableModel.getValueAt(row, 0));
        editingKey = bookingRef.equals("-") ? null : bookingRef;

        txtBookingRef.setText(bookingRef.equals("-") ? "" : bookingRef);
        txtPassengerName.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        txtFlightId.setText(String.valueOf(tableModel.getValueAt(row, 2)));
        cmbCabin.setSelectedItem(String.valueOf(tableModel.getValueAt(row, 3)));
        txtPrice.setText(String.valueOf(tableModel.getValueAt(row, 4)));
        cmbStatus.setSelectedItem(String.valueOf(tableModel.getValueAt(row, 5)));

        new SwingWorker<Map<String, String>, Void>() {
            @Override
            protected Map<String, String> doInBackground() throws Exception {
                List<Map<String, String>> all = FirebaseHelper.getAllPassengers();
                if (all != null) {
                    for (Map<String, String> p : all) {
                        String ref = p.getOrDefault("bookingRef", p.getOrDefault("id", ""));
                        if (bookingRef.equalsIgnoreCase(ref)) return p;
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    Map<String, String> p = get();
                    if (p != null) {
                        txtDistance.setText(p.getOrDefault("distanceMiles", ""));
                        txtDuration.setText(p.getOrDefault("flightDurationMinutes", ""));
                        if (p.get("cabin") != null) cmbCabin.setSelectedItem(p.get("cabin"));
                        if (p.get("flightStatus") != null) cmbStatus.setSelectedItem(p.get("flightStatus"));
                    }
                } catch (Exception ignored) {}
                btnSave.setText("Update Passenger");
            }
        }.execute();
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showWarn("Please select a passenger from the table first.");
            return;
        }

        String bookingRef = String.valueOf(tableModel.getValueAt(row, 0));
        if (bookingRef.equals("-") || bookingRef.isEmpty()) {
            showWarn("Invalid booking reference.");
            return;
        }

        if (!confirm("Delete passenger \"" + bookingRef + "\"?\nThis action cannot be undone.")) return;

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                FirebaseHelper.delete("passengers/" + bookingRef);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    if (bookingRef.equals(editingKey)) {
                        editingKey = null;
                        btnSave.setText("Save Passenger");
                        clearForm();
                    }
                    loadPassengers();
                    showInfo("Passenger deleted successfully.");
                } catch (Exception ex) {
                    showError("Failed to delete passenger.");
                }
            }
        }.execute();
    }

    private void clearForm() {
        txtBookingRef.setText("");
        txtPassengerName.setText("");
        txtFlightId.setText("");
        txtDistance.setText("");
        txtDuration.setText("");
        txtPrice.setText("");
        cmbCabin.setSelectedIndex(0);
        cmbStatus.setSelectedIndex(0);
        editingKey = null;
        btnSave.setText("Save Passenger");
    }

    private void loadPassengers() {
        btnRefresh.setEnabled(false);

        new SwingWorker<List<Map<String, String>>, Void>() {
            @Override
            protected List<Map<String, String>> doInBackground() throws Exception {
                return FirebaseHelper.getAllPassengers();
            }

            @Override
            protected void done() {
                btnRefresh.setEnabled(true);
                try {
                    List<Map<String, String>> passengers = get();
                    tableModel.setRowCount(0);

                    if (passengers != null) {
                        for (Map<String, String> p : passengers) {
                            String ref = p.get("bookingRef");
                            if (ref == null || ref.trim().isEmpty()) {
                                ref = p.get("id");
                            }
                            if (ref == null || ref.trim().isEmpty()) {
                                ref = "-";
                            }

                            tableModel.addRow(new Object[]{
                                ref,
                                p.getOrDefault("passengerName", "-"),
                                p.getOrDefault("flightId", "-"),
                                p.getOrDefault("cabin", "Economy"),
                                p.getOrDefault("priceUsd", "-"),
                                p.getOrDefault("flightStatus", "-")
                            });
                        }
                    }
                } catch (Exception ex) {
                    showError("Failed to load passengers.\nCheck your internet connection or Firebase URL.");
                    ex.printStackTrace();
                }
            }
        }.execute();
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
                    g2.setColor(new Color(100, 100, 100));
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
            return new Insets(8, 12, 8, 12);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(8, 12, 8, 12);
            return insets;
        }
    }
}