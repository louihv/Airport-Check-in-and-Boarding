import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.AbstractBorder;

public class AccountPanel extends JPanel {

    private String currentUsername;
    private String currentRole;
    private final MainFrame frame;
    private JLabel lblUsername, lblRole, lblCounter, lblStatus;
    private JPasswordField txtCurrentPass, txtNewPass, txtConfirmPass;
    private JButton btnChangePassword, btnRefresh;
    private JPanel forceChangeOverlay;
    private boolean forcePasswordChange = false;
    private boolean initialCheckPending = false;

    public AccountPanel(MainFrame mainFrame) {
        this.frame = mainFrame;
        this.currentUsername = null;
        this.currentRole = null;

        setBackground(MainFrame.MAIN_BG);
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("My Account");
        title.setFont(AppFonts.bold(22));
        title.setForeground(new Color(40, 40, 40));
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 18, 0));
        center.setOpaque(false);
        center.add(createInfoCard());
        center.add(createPasswordCard());

        forceChangeOverlay = createForceChangeOverlay();
        forceChangeOverlay.setVisible(false);

       JLayeredPane layered = new JLayeredPane() {
            @Override
            public void doLayout() {
                center.setBounds(0, 0, getWidth(), getHeight());
                forceChangeOverlay.setBounds(0, 0, getWidth(), getHeight());
            }
        };

        layered.setLayout(null);

        layered.add(center, JLayeredPane.DEFAULT_LAYER);
        layered.add(forceChangeOverlay, JLayeredPane.PALETTE_LAYER);

        add(layered, BorderLayout.CENTER);

        btnChangePassword.addActionListener(e -> changePassword());
        btnRefresh.addActionListener(e -> loadAccountInfo());
    }

    public void setUser(String username, String role) {
        this.currentUsername = username;
        this.currentRole = role;
        this.initialCheckPending = true;
        this.forcePasswordChange = false;
        txtCurrentPass.setText("");
        txtNewPass.setText("");
        txtConfirmPass.setText("");
        forceChangeOverlay.setVisible(false);
        loadAccountInfo();
    }

    private JPanel createInfoCard() {
        JPanel card = createRoundedCard();
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel leftTitle = new JLabel("Account Information");
        leftTitle.setFont(AppFonts.bold(16));
        leftTitle.setForeground(new Color(40, 40, 40));

        JSeparator divider = new JSeparator();
        divider.setForeground(new Color(220, 220, 220));

        JPanel header = new JPanel(new BorderLayout(0, 10));
        header.setOpaque(false);
        header.add(leftTitle, BorderLayout.NORTH);
        header.add(divider, BorderLayout.SOUTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        lblUsername = createValueLabel();
        lblRole = createValueLabel();
        lblCounter = createValueLabel();
        lblStatus = createValueLabel();

        form.add(createReadOnlyBlock("Username", lblUsername));
        form.add(Box.createVerticalStrut(14));
        form.add(createReadOnlyBlock("Role", lblRole));
        form.add(Box.createVerticalStrut(14));
        form.add(createReadOnlyBlock("Counter", lblCounter));
        form.add(Box.createVerticalStrut(14));
        form.add(createReadOnlyBlock("Status", lblStatus));

        btnRefresh = createOutlineButton("Refresh");
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setOpaque(false);
        bottom.add(btnRefresh);

        card.add(header, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createPasswordCard() {
        JPanel card = createRoundedCard();
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel rightTitle = new JLabel("Change Password");
        rightTitle.setFont(AppFonts.bold(16));
        rightTitle.setForeground(new Color(40, 40, 40));

        JSeparator divider = new JSeparator();
        divider.setForeground(new Color(220, 220, 220));

        JPanel header = new JPanel(new BorderLayout(0, 10));
        header.setOpaque(false);
        header.add(rightTitle, BorderLayout.NORTH);
        header.add(divider, BorderLayout.SOUTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        txtCurrentPass = createOutlinePassword();
        txtNewPass = createOutlinePassword();
        txtConfirmPass = createOutlinePassword();

        form.add(createFieldBlock("Current Password", txtCurrentPass));
        form.add(Box.createVerticalStrut(11));
        form.add(createFieldBlock("New Password", txtNewPass));
        form.add(Box.createVerticalStrut(11));
        form.add(createFieldBlock("Confirm New Password", txtConfirmPass));
        form.add(Box.createVerticalStrut(6));

        btnChangePassword = createFilledButton("Update Password");

        card.add(header, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(btnChangePassword, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createForceChangeOverlay() {
        JPanel overlay = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 140));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        overlay.setOpaque(false);
        overlay.addMouseListener(new java.awt.event.MouseAdapter() {});
        overlay.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {});

        JPanel dialog = createRoundedCard();
        dialog.setLayout(new BorderLayout(0, 16));
        dialog.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        dialog.setPreferredSize(new Dimension(420, 260));

        JLabel title = new JLabel("Password Change Required");
        title.setFont(AppFonts.bold(18));
        title.setForeground(new Color(180, 50, 50));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel msgPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        msgPanel.setOpaque(false);

        JLabel msg = new JLabel(
            "<html><div style='text-align:center'>" +
            "This is your first login or your account was just created.<br>" +
            "You must set a new password before continuing." +
            "</div></html>"
        );
        msg.setFont(AppFonts.regular(13));
        msg.setForeground(new Color(50, 65, 55));
        msgPanel.add(msg);

        JButton go = createFilledButton("Set New Password");
        go.addActionListener(e -> {
            forceChangeOverlay.setVisible(false);
            txtCurrentPass.requestFocusInWindow();
        });

        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnWrap.setOpaque(false);
        btnWrap.add(go);

        dialog.add(title, BorderLayout.NORTH);
        dialog.add(msg, BorderLayout.CENTER);
        dialog.add(btnWrap, BorderLayout.SOUTH);

        overlay.add(dialog);
        return overlay;
    }

    private void loadAccountInfo() {
        if (currentUsername == null || currentRole == null) return;

        btnRefresh.setEnabled(false);

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return FirebaseHelper.get("users/" + currentRole.toLowerCase() + "/" + currentUsername);
            }

            @Override
            protected void done() {
                btnRefresh.setEnabled(true);
                try {
                    String json = get();
                    if (json == null || json.equals("null") || json.isEmpty()) {
                        showError("Could not load account data.");
                        return;
                    }

                    lblUsername.setText(extract(json, "username"));
                    lblRole.setText(extract(json, "role"));
                    String counter = extract(json, "counter");
                    lblCounter.setText(counter == null || counter.isEmpty() ? "—" : counter);
                    lblStatus.setText(extract(json, "status"));

                    if ("Online".equalsIgnoreCase(lblStatus.getText())) {
                        lblStatus.setForeground(new Color(45, 190, 180));
                    } else {
                        lblStatus.setForeground(new Color(245, 166, 35));
                    }

                    String mustChange = extract(json, "mustChangePassword");
                    String password = extract(json, "password");

                    forcePasswordChange = "true".equalsIgnoreCase(mustChange)
                     || "changeme".equals(password);

                    forceChangeOverlay.setVisible(forcePasswordChange);

                    if (frame != null) {
                        if (forcePasswordChange) {
                            frame.setNavigationEnabled(false);
                        } else if (initialCheckPending) {
                            frame.setNavigationEnabled(true);
                            frame.showPanel("Dashboard");
                        }
                    }
                    initialCheckPending = false;

                    revalidate();
                    repaint();

                } catch (Exception ex) {
                    showError("Failed to load account info.");
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private String extract(String json, String key) {
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("\"" + key + "\"\\s*:\\s*(?:\"([^\"]*)\"|([^,}\\s]+))")
                .matcher(json);
        if (!m.find()) return "";
        return m.group(1) != null ? m.group(1) : m.group(2);
    }

    private void changePassword() {
        String current = new String(txtCurrentPass.getPassword());
        String newPass = new String(txtNewPass.getPassword());
        String confirm = new String(txtConfirmPass.getPassword());

        if (newPass.isEmpty()) {
            showWarn("New password cannot be empty.");
            return;
        }
        if (newPass.length() < 6) {
            showWarn("New password must be at least 6 characters.");
            return;
        }
        if (current.isEmpty()) {
            showWarn("Please enter your current password.");
            return;
        }
        if (newPass.equals(current) || newPass.equals("changeme")) {
            showWarn("Please choose a different password.");
            return;
        }
        if (!newPass.equals(confirm)) {
            showWarn("New password and confirmation do not match.");
            return;
        }

        btnChangePassword.setEnabled(false);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                String json = FirebaseHelper.get("users/" + currentRole.toLowerCase() + "/" + currentUsername);
                if (json == null || json.equals("null")) return false;

                String storedHash = extract(json, "password");
                if (!PasswordUtil.verify(current, storedHash)) {
                    return false;
                }
                String counter = extract(json, "counter");
                String status = extract(json, "status");

                String newJson = String.format(
                    "{\"username\":\"%s\",\"password\":\"%s\",\"role\":\"%s\"," +
                    "\"counter\":\"%s\",\"status\":\"%s\",\"mustChangePassword\":false}",
                    currentUsername,
                    hash(newPass),
                    currentRole,
                    counter == null ? "" : counter,
                    status == null ? "Offline" : status
                );

                FirebaseHelper.put("users/" + currentRole.toLowerCase() + "/" + currentUsername, newJson);
                return true;
            }

            @Override
            protected void done() {
                btnChangePassword.setEnabled(true);
                try {
                    if (get()) {
                        showInfo("Password updated successfully.");
                        txtCurrentPass.setText("");
                        txtNewPass.setText("");
                        txtConfirmPass.setText("");
                        forcePasswordChange = false;
                        forceChangeOverlay.setVisible(false);
                        revalidate();
                        repaint();
                        if (frame != null) {
                            frame.setNavigationEnabled(true);
                            frame.showPanel("Dashboard");
                        }
                        showInfo("Password updated successfully.");
                    } else {
                        showError("Current password is incorrect.");
                    }
                } catch (Exception ex) {
                    showError("Failed to update password.\nCheck your connection.");
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private JLabel createValueLabel() {
        JLabel lbl = new JLabel("—");
        lbl.setFont(AppFonts.regular(14));
        lbl.setForeground(new Color(40, 40, 40));
        return lbl;
    }

    private JPanel createReadOnlyBlock(String labelText, JLabel value) {
        JPanel block = new JPanel();
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setOpaque(false);
        block.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(AppFonts.bold(13));
        label.setForeground(new Color(60, 60, 60));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        value.setAlignmentX(Component.LEFT_ALIGNMENT);

        block.add(label);
        block.add(Box.createVerticalStrut(4));
        block.add(value);
        return block;
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

    private JPasswordField createOutlinePassword() {
        JPasswordField field = new JPasswordField();
        field.setFont(AppFonts.regular(13));
        field.setBorder(new RoundedOutlineBorder(1, new Color(216, 203, 194), 10));
        field.setBackground(Color.WHITE);
        field.setOpaque(true);
        field.setPreferredSize(new Dimension(0, 42));
        return field;
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
        btn.setPreferredSize(new Dimension(180, 42));
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

    private void showInfo(String msg) {
        showModernMessage(msg, "Account", false);
    }

    private void showError(String msg) {
        showModernMessage(msg, "Error", true);
    }

    private void showWarn(String msg) {
        showModernMessage(msg, "Notice", true);
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

    private String hash(String plain) {
        return PasswordUtil.hash(plain);
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