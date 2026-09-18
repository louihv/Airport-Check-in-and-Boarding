import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class UserManagementPanel extends JPanel {
    private DefaultTableModel onlineModel;
    private JTable table;
    private JTextField txtUser, txtCounter;
    private JPasswordField txtPass;
    private JComboBox<String> cmbRole;
    private JButton btnRegister, btnRefresh, btnEdit, btnDelete;
    private String editingUsername = null;
    private String editingRole = null;

    public UserManagementPanel() {
        setBackground(MainFrame.MAIN_BG);
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("User Management");
        title.setFont(AppFonts.bold(22));
        title.setForeground(new Color(40, 40, 40));
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 18, 0));
        center.setOpaque(false);

        center.add(createFormCard());
        center.add(createTableCard());
        add(center, BorderLayout.CENTER);

        btnRegister.addActionListener(e -> registerOrUpdateUser());
        btnRefresh.addActionListener(e -> loadUsers());
        btnEdit.addActionListener(e -> editSelected());
        btnDelete.addActionListener(e -> deleteSelected());

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) editSelected();
            }
        });

        loadUsers();
    }

    private JPanel createFormCard() {
        JPanel card = createRoundedCard();
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel leftHeader = new JPanel(new BorderLayout(0, 10));
        leftHeader.setOpaque(false);
        JLabel leftTitle = new JLabel("Register / Update User");
        leftTitle.setFont(AppFonts.bold(16));
        leftTitle.setForeground(new Color(40, 40, 40));
        JSeparator divider = new JSeparator();
        divider.setForeground(new Color(220, 220, 220));
        leftHeader.add(leftTitle, BorderLayout.NORTH);
        leftHeader.add(divider, BorderLayout.SOUTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        txtUser = createOutlineField();
        txtPass = createOutlinePassword();
        cmbRole = createStyledCombo(new String[]{"STAFF", "ADMIN"});
        txtCounter = createOutlineField();

        form.add(createFieldBlock("Username", txtUser));
        form.add(Box.createVerticalStrut(11));
        form.add(createFieldBlock("Password", txtPass));
        form.add(Box.createVerticalStrut(11));
        form.add(createFieldBlock("Role", cmbRole));
        form.add(Box.createVerticalStrut(11));
        form.add(createFieldBlock("Counter (optional)", txtCounter));
        form.add(Box.createVerticalStrut(6));

        btnRegister = createFilledButton("Register");

        card.add(leftHeader, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(btnRegister, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createTableCard() {
        JPanel card = createRoundedCard();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel rightTitle = new JLabel("All Users (Admin / Staff)");
        rightTitle.setFont(AppFonts.bold(16));
        rightTitle.setForeground(new Color(40, 40, 40));

        String[] cols = {"Username", "Role", "Counter", "Status"};
        onlineModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(onlineModel);
        styleTable(table);
        table.setTableHeader(null);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionRow.setOpaque(false);
        btnEdit = createOutlineButton("Edit");
        btnDelete = createOutlineButton("Delete");
        btnRefresh = createOutlineButton("Refresh");
        actionRow.add(btnEdit);
        actionRow.add(btnDelete);
        actionRow.add(btnRefresh);

        card.add(rightTitle, BorderLayout.NORTH);
        card.add(tableOuter, BorderLayout.CENTER);
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

    private JPasswordField createOutlinePassword() {
        JPasswordField field = new JPasswordField();
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

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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
                    if ("Online".equalsIgnoreCase(status)) {
                        setForeground(new Color(45, 190, 180));
                    } else if ("Offline".equalsIgnoreCase(status)) {
                        setForeground(new Color(245, 166, 35));
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
        showModernMessage(message, "User Management", false);
    }

    private void showError(String message) {
        showModernMessage(message, "Error", true);
    }

    private void showWarn(String message) {
        showModernMessage(message, "Notice", true);
    }

    private boolean confirm(String message) {
        final boolean[] result = {false};

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Confirm", Dialog.ModalityType.APPLICATION_MODAL);
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

        JLabel lblTitle = new JLabel("Confirm");
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

    private void registerOrUpdateUser() {
        String u = txtUser.getText().trim();
        String p = new String(txtPass.getPassword());
        String role = (String) cmbRole.getSelectedItem();
        String counter = txtCounter.getText().trim();

        if (u.isEmpty()) {
            showWarn("Username is required.");
            return;
        }

        if (editingUsername == null && p.isEmpty()) {
            showWarn("Password is required for new users.");
            return;
        }

        btnRegister.setEnabled(false);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (editingUsername != null) {
                    boolean usernameChanged = !editingUsername.equalsIgnoreCase(u);
                    boolean roleChanged = editingRole != null && !editingRole.equalsIgnoreCase(role);

                    if (usernameChanged || roleChanged) {
                        FirebaseHelper.delete("users/" + editingRole.toLowerCase() + "/" + editingUsername);
                    }

                    if (p.isEmpty()) {
                        String existing = FirebaseHelper.get("users/" + role.toLowerCase() + "/" + u);
                        if (existing == null || existing.equals("null") || existing.isEmpty()) {
                            existing = FirebaseHelper.get("users/" + editingRole.toLowerCase() + "/" + editingUsername);
                        }
                        String hashed = null;
                        if (existing != null && !existing.equals("null")) {
                            java.util.regex.Matcher m = java.util.regex.Pattern
                                    .compile("\"password\"\\s*:\\s*\"([^\"]*)\"").matcher(existing);
                            if (m.find()) hashed = m.group(1);
                        }
                        if (hashed != null) {
                            String json = String.format(
                                "{\"username\":\"%s\",\"password\":\"%s\",\"role\":\"%s\",\"counter\":\"%s\",\"status\":\"Offline\"}",
                                u, hashed, role, counter
                            );
                            FirebaseHelper.put("users/" + role.toLowerCase() + "/" + u, json);
                        } else {
                            FirebaseHelper.saveUser(u, "changeme", role, counter);
                        }
                    } else {
                        FirebaseHelper.saveUser(u, p, role, counter);
                    }
                } else {
                    FirebaseHelper.saveUser(u, p, role, counter);
                }
                return null;
            }

            @Override
            protected void done() {
                btnRegister.setEnabled(true);
                try {
                    get();
                    showInfo(editingUsername != null ? "User updated successfully." : "User registered successfully.");
                    clearForm();
                    loadUsers();
                } catch (Exception ex) {
                    showError("Failed to save user.\nCheck your internet connection or Firebase URL.");
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showWarn("Please select a user from the table first.");
            return;
        }

        String username = String.valueOf(onlineModel.getValueAt(row, 0));
        String role = String.valueOf(onlineModel.getValueAt(row, 1));
        String counter = String.valueOf(onlineModel.getValueAt(row, 2));

        editingUsername = username;
        editingRole = role;

        txtUser.setText(username);
        txtPass.setText("");
        cmbRole.setSelectedItem(role.toUpperCase());
        txtCounter.setText("-".equals(counter) ? "" : counter);

        btnRegister.setText("Update User");
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showWarn("Please select a user from the table first.");
            return;
        }

        String username = String.valueOf(onlineModel.getValueAt(row, 0));
        String role = String.valueOf(onlineModel.getValueAt(row, 1));

        if (username.isEmpty() || username.equals("-")) {
            showWarn("Invalid username.");
            return;
        }

        if (!confirm("Delete user \"" + username + "\" (" + role + ")?\nThis action cannot be undone.")) return;

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                FirebaseHelper.delete("users/" + role.toLowerCase() + "/" + username);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    if (username.equals(editingUsername)) {
                        clearForm();
                    }
                    loadUsers();
                    showInfo("User deleted successfully.");
                } catch (Exception ex) {
                    showError("Failed to delete user.");
                }
            }
        }.execute();
    }

    private void clearForm() {
        txtUser.setText("");
        txtPass.setText("");
        txtCounter.setText("");
        cmbRole.setSelectedIndex(0);
        editingUsername = null;
        editingRole = null;
        btnRegister.setText("Register");
    }

    private void loadUsers() {
        btnRefresh.setEnabled(false);

        new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                return FirebaseHelper.getAllUsers();
            }

            @Override
            protected void done() {
                btnRefresh.setEnabled(true);
                try {
                    List<Object[]> users = get();
                    onlineModel.setRowCount(0);
                    if (users != null) {
                        for (Object[] row : users) {
                            onlineModel.addRow(row);
                        }
                    }
                } catch (Exception ex) {
                    showError("Failed to load users.\nCheck your internet connection or Firebase URL.");
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