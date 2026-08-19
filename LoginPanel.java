import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.*;

public class LoginPanel extends JPanel {

    public LoginPanel(MainFrame frame) {
        setBackground(MainFrame.MAIN_BG);
        setLayout(new BorderLayout());

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        topBar.setOpaque(false);

        JButton backButton = createBackButton("/resources/back.png");
        backButton.addActionListener(e -> frame.backToRole());
        topBar.add(backButton);
        add(topBar, BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        JPanel form = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        form.setOpaque(false);
        form.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1, new Color(216, 203, 194), 22),
                new EmptyBorder(28, 45, 32, 45)
        ));

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
        form.add(logoLabel, gbc);

        JLabel title = new JLabel("Staff Login", SwingConstants.CENTER);
        title.setFont(AppFonts.bold(22));
        title.setForeground(MainFrame.NAV_BTN_BG);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 10, 18, 10);
        form.add(title, gbc);

        JTextField txtUser = createStyledField("Enter Username");
        JPasswordField txtPass = createStyledPasswordField("Enter Password");

        addFormField(form, "Username:", txtUser, gbc, 2);
        addFormField(form, "Password:", txtPass, gbc, 4);

        JButton btnLogin = new JButton("Login") {
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
        btnLogin.setFont(AppFonts.bold(14));
        btnLogin.setBackground(MainFrame.NAV_BTN_BG);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setContentAreaFilled(false);
        btnLogin.setOpaque(false);
        btnLogin.setBorder(new RoundedOutlineBorder(1.2f, MainFrame.NAV_BTN_BG, 25));

        btnLogin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnLogin.setBackground(MainFrame.SECONDARY_BTN_BG);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnLogin.setBackground(MainFrame.NAV_BTN_BG);
            }
        });

        gbc.gridy = 6;
        gbc.insets = new Insets(22, 10, 8, 10);
        form.add(btnLogin, gbc);

        JLabel msg = new JLabel(" ", SwingConstants.CENTER);
        msg.setFont(AppFonts.regular(12));
        msg.setForeground(Color.RED);

        gbc.gridy = 8;
        gbc.insets = new Insets(10, 10, 5, 10);
        form.add(msg, gbc);

        btnLogin.addActionListener(e -> {
            String user = getRealText(txtUser, "Enter Username");
            String pass = getRealPassword(txtPass, "Enter Password");

            if (user.isEmpty() || pass.isEmpty()) {
                msg.setForeground(Color.RED);
                msg.setText("Please enter username and password");
                return;
            }

            btnLogin.setEnabled(false);
            msg.setForeground(new Color(27, 77, 46));
            msg.setText("Logging in...");

            new SwingWorker<String[], Void>() {
                @Override
                protected String[] doInBackground() throws Exception {
                    return FirebaseHelper.login(user, pass);   
                }

                @Override
                protected void done() {
                    btnLogin.setEnabled(true);
                    try {
                        String[] result = get();
                        if (result != null) {
                            String role = result[0];
                            msg.setText(" ");
                            frame.loginSuccess(role, user);
                            txtUser.setText("");
                            txtPass.setText("");
                        } else {
                            msg.setForeground(Color.RED);
                            msg.setText("Invalid username or password");
                        }
                    } catch (Exception ex) {
                        msg.setForeground(Color.RED);
                        msg.setText("Check connection");
                        ex.printStackTrace();
                    }
                }
            }.execute();
        });

        centerWrapper.add(form);
        add(centerWrapper, BorderLayout.CENTER);
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
        JTextField field = new JTextField(18);
        field.setFont(AppFonts.regular(13));
        field.setOpaque(false);
        field.setForeground(MainFrame.SECONDARY_BTN_BG);
        field.setCaretColor(MainFrame.NAV_BTN_BG);
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1.2f, MainFrame.NAV_BTN_BG, 25),
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
        JPasswordField field = new JPasswordField(18);
        field.setFont(AppFonts.regular(13));
        field.setOpaque(false);
        field.setForeground(MainFrame.SECONDARY_BTN_BG);
        field.setCaretColor(MainFrame.NAV_BTN_BG);
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1.2f, MainFrame.NAV_BTN_BG, 25),
                BorderFactory.createEmptyBorder(5, 16, 5, 16)
        ));
        field.setEchoChar((char) 0);
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

    private String getRealText(JTextField field, String placeholder) {
        String text = field.getText().trim();
        return text.equals(placeholder) ? "" : text;
    }

    private String getRealPassword(JPasswordField field, String placeholder) {
        String text = new String(field.getPassword()).trim();
        return text.equals(placeholder) ? "" : text;
    }

    private static class RoundedOutlineBorder extends AbstractBorder {
        private final float thickness;
        private final Color color;
        private final int radius;

        public RoundedOutlineBorder(float thickness, Color color, int radius) {
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
            return new Insets(radius / 3, radius / 2, radius / 3, radius / 2);
        }
    }
}