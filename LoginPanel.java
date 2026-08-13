import java.awt.*;
import javax.swing.*;

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
        title.setFont(AppFonts.bold(22));
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

            if (user.isEmpty() || pass.isEmpty()) {
                msg.setText("Please enter username and password");
                return;
            }

            // disable button while logging in
            btnLogin.setEnabled(false);
            msg.setForeground(new Color(27, 77, 46));  
            msg.setText("Logging in...");

            // run network call in background so UI doesn't freeze
            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    return FirebaseHelper.login(user, pass);
                }

                @Override
                protected void done() {
                    btnLogin.setEnabled(true);
                    try {
                        String role = get();
                        if (role != null) {
                            msg.setText(" ");
                            frame.loginSuccess(role, user);
                        } else {
                            msg.setForeground(Color.RED);
                            msg.setText("Invalid username or password");
                        }
                    } catch (Exception ex) {
                        msg.setForeground(Color.RED);
                        msg.setText("Connection error. Check internet / Firebase URL");
                        ex.printStackTrace();
                    }
                }
            }.execute();
        });

        btnBack.addActionListener(e -> frame.backToRole());

        add(form);
    }
}