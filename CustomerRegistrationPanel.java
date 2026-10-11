import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;

public class CustomerRegistrationPanel extends JPanel {

    private JTextField txtName, txtEmail, txtPhone;
    private JPasswordField txtPassword, txtConfirmPassword;
    private final MainFrame mainFrame;
    private BufferedImage backgroundImage;

    public CustomerRegistrationPanel(MainFrame mainFrame) {
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
        formCard.setLayout(new GridBagLayout());
        formCard.setBorder(new EmptyBorder(28, 45, 32, 45));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
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
        formCard.add(logoLabel, gbc);

        JLabel lblTitle = new JLabel("Customer Registration", SwingConstants.CENTER);
        lblTitle.setFont(AppFonts.bold(28));
        lblTitle.setForeground(MainFrame.NAV_BTN_BG);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 10, 6, 10);
        formCard.add(lblTitle, gbc);

        JLabel lblInfo = new JLabel(
                "<html><center>Create an account to book flights and manage your trips later.</center></html>",
                SwingConstants.CENTER);
        lblInfo.setFont(AppFonts.regular(12));
        lblInfo.setForeground(new Color(60, 80, 70));

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 10, 18, 10);
        formCard.add(lblInfo, gbc);

        txtName = createStyledField("Enter your Full Name");
        txtEmail = createStyledField("Enter your Email Address");
        txtPhone = createStyledField("Enter your Phone Number");
        txtPassword = createStyledPasswordField("Enter Password");
        txtConfirmPassword = createStyledPasswordField("Confirm Password");

        addFormField(formCard, "Full Name:", txtName, gbc, 3);
        addFormField(formCard, "Email Address:", txtEmail, gbc, 5);
        addFormField(formCard, "Phone Number:", txtPhone, gbc, 7);
        addFormField(formCard, "Password:", txtPassword, gbc, 9);
        addFormField(formCard, "Confirm Password:", txtConfirmPassword, gbc, 11);

        JButton btnRegister = new JButton("Create Account") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                int arc = 25;
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, w, h, arc, arc);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnRegister.setFont(AppFonts.regular(15));
        btnRegister.setBackground(MainFrame.SECONDARY_BTN_BG);
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFocusPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.setContentAreaFilled(false);
        btnRegister.setOpaque(false);
        btnRegister.setBorder(new RoundedBorder(MainFrame.SECONDARY_BTN_BG, 25, 1.2f));

        btnRegister.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnRegister.setBackground(MainFrame.NAV_BTN_BG);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnRegister.setBackground(MainFrame.SECONDARY_BTN_BG);
            }
        });

        gbc.gridy = 13;
        gbc.insets = new Insets(22, 10, 6, 10);
        formCard.add(btnRegister, gbc);

        centerWrapper.add(formCard);
        add(centerWrapper, BorderLayout.CENTER);

        btnRegister.addActionListener(e -> processRegistration(btnRegister));
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
            int x = (panelW - drawW) / 2;
            int y = (panelH - drawH) / 2;
            g.drawImage(backgroundImage, x, y, drawW, drawH, this);
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

    private JTextField createStyledField(String placeholder) {
        JTextField field = new JTextField(20);
        field.setFont(AppFonts.regular(13));
        field.setOpaque(false);
        field.setForeground(MainFrame.SECONDARY_BTN_BG);
        field.setCaretColor(MainFrame.NAV_BTN_BG);
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(MainFrame.NAV_BTN_BG, 25, 1.2f),
                BorderFactory.createEmptyBorder(5, 16, 5, 16)
        ));
        field.setText(placeholder);
        field.setForeground(MainFrame.SECONDARY_BTN_BG);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(new Color(40, 55, 50));
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
        return field;
    }

    private JPasswordField createStyledPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField(20);
        field.setFont(AppFonts.regular(13));
        field.setOpaque(false);
        field.setForeground(MainFrame.SECONDARY_BTN_BG);
        field.setCaretColor(MainFrame.NAV_BTN_BG);
        field.setEchoChar((char) 0);
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(MainFrame.NAV_BTN_BG, 25, 1.2f),
                BorderFactory.createEmptyBorder(5, 16, 5, 16)
        ));
        field.setText(placeholder);
        field.setForeground(MainFrame.SECONDARY_BTN_BG);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                String text = new String(field.getPassword());
                if (text.equals(placeholder)) {
                    field.setText("");
                    field.setEchoChar('•');
                    field.setForeground(new Color(40, 55, 50));
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                String text = new String(field.getPassword());
                if (text.trim().isEmpty()) {
                    field.setEchoChar((char) 0);
                    field.setText(placeholder);
                    field.setForeground(MainFrame.SECONDARY_BTN_BG);
                }
            }
        });
        return field;
    }

    private void addFormField(JPanel panel, String labelText, JComponent field,
                              GridBagConstraints gbc, int row) {
        gbc.gridy = row;
        gbc.insets = new Insets(6, 10, 2, 10);
        JLabel label = new JLabel(labelText);
        label.setFont(AppFonts.regular(13));
        label.setForeground(MainFrame.NAV_BTN_BG);
        panel.add(label, gbc);

        gbc.gridy = row + 1;
        gbc.insets = new Insets(0, 10, 10, 10);
        panel.add(field, gbc);
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
        card.setLayout(new BorderLayout(0, 16));
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

    private void processRegistration(JButton btnRegister) {
        String name = getRealText(txtName, "Enter your Full Name");
        String email = getRealText(txtEmail, "Enter your Email Address");
        String phone = getRealText(txtPhone, "Enter your Phone Number");
        String password = getRealPassword(txtPassword, "Enter Password");
        String confirm = getRealPassword(txtConfirmPassword, "Confirm Password");

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showModernMessage("Please fill in all fields.", "Missing Information", true);
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            showModernMessage("Please enter a valid email address.", "Invalid Email", true);
            return;
        }

        if (password.length() < 6) {
            showModernMessage("Password must be at least 6 characters.", "Weak Password", true);
            return;
        }

        if (!password.equals(confirm)) {
            showModernMessage("Passwords do not match.", "Password Mismatch", true);
            return;
        }

        btnRegister.setEnabled(false);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                Map<String, String> existing = FirebaseHelper.getCustomerByEmail(email);
                if (existing != null) {
                    return false;
                }

                String json = String.format(
                    "{\"fullName\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"password\":\"%s\",\"createdAt\":\"%s\"}",
                    name, email, phone, password,
                    java.time.LocalDateTime.now().toString()
                );

                FirebaseHelper.put("customers/" + email.replace(".", "_"), json);
                return true;
            }

            @Override
            protected void done() {
                btnRegister.setEnabled(true);
                try {
                    boolean success = get();
                    if (!success) {
                        showModernMessage("An account with this email already exists.", "Registration Failed", true);
                        return;
                    }

                    showModernMessage(
                            "Account created successfully!\nYou can now book flights and check-in later.",
                            "Registration Successful", false);

                    resetField(txtName, "Enter your Full Name");
                    resetField(txtEmail, "Enter your Email Address");
                    resetField(txtPhone, "Enter your Phone Number");
                    resetPasswordField(txtPassword, "Enter Password");
                    resetPasswordField(txtConfirmPassword, "Confirm Password");

                } catch (Exception ex) {
                    showModernMessage(
                            "Failed to create account.\nCheck internet / Firebase connection.",
                            "Error", true);
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private String getRealText(JTextField field, String placeholder) {
        String text = field.getText().trim();
        return text.equals(placeholder) ? "" : text;
    }

    private String getRealPassword(JPasswordField field, String placeholder) {
        String text = new String(field.getPassword()).trim();
        return text.equals(placeholder) ? "" : text;
    }

    private void resetField(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(MainFrame.SECONDARY_BTN_BG);
    }

    private void resetPasswordField(JPasswordField field, String placeholder) {
        field.setEchoChar((char) 0);
        field.setText(placeholder);
        field.setForeground(MainFrame.SECONDARY_BTN_BG);
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bg;

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

    private static class GlassPanel extends JPanel {
        public GlassPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            int w = getWidth();
            int h = getHeight();
            int arc = 32;
            g2.setColor(new Color(0, 0, 0, 35));
            g2.fillRoundRect(5, 7, w - 10, h - 10, arc, arc);
            g2.setColor(new Color(255, 255, 255, 95));
            g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);
            g2.setPaint(new GradientPaint(
                    0, 0, new Color(255, 255, 255, 110),
                    0, h / 2.5f, new Color(255, 255, 255, 15)));
            g2.fillRoundRect(0, 0, w - 1, (int) (h / 2.2), arc, arc);
            g2.setColor(new Color(255, 255, 255, 160));
            g2.setStroke(new BasicStroke(1.6f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}