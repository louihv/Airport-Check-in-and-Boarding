import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class LoginPanel extends JPanel {
    public LoginPanel(MainFrame frame) {
        setBackground(MainFrame.MAIN_BG);
        setLayout(new GridBagLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 200)),
            BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Staff / Admin Login", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        form.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0;
        form.add(new JLabel("Username:"), gbc);
        JTextField txtUser = new JTextField(18);
        gbc.gridx = 1;
        form.add(txtUser, gbc);

        gbc.gridy = 2; gbc.gridx = 0;
        form.add(new JLabel("Password:"), gbc);
        JPasswordField txtPass = new JPasswordField(18);
        gbc.gridx = 1;
        form.add(txtPass, gbc);

        JButton btnLogin = new JButton("Login");
        btnLogin.setBackground(MainFrame.NAV_BTN_BG);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        form.add(btnLogin, gbc);

        JButton btnBack = new JButton("Back");
        btnBack.setFocusPainted(false);
        gbc.gridy = 4;
        form.add(btnBack, gbc);

        JLabel msg = new JLabel(" ", SwingConstants.CENTER);
        msg.setForeground(Color.RED);
        gbc.gridy = 5;
        form.add(msg, gbc);

        btnLogin.addActionListener(e -> {
            String user = txtUser.getText().trim();
            String pass = new String(txtPass.getPassword());

            if (user.equals("admin") && pass.equals("admin123")) {
                frame.loginSuccess("ADMIN");
            } else if (user.equals("staff") && pass.equals("staff123")) {
                frame.loginSuccess("STAFF");
            } else {
                msg.setText("Invalid username or password");
            }
        });

        btnBack.addActionListener(e -> frame.backToRole());

        add(form);
    }
}