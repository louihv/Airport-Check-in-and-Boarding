import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class UserManagementPanel extends JPanel {
    private DefaultTableModel onlineModel;

    public UserManagementPanel() {
        setBackground(MainFrame.MAIN_BG);
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("User Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 20, 0));
        center.setOpaque(false);

        //  Register New User 
        JPanel left = new JPanel(new BorderLayout(0, 10));
        left.setOpaque(false);
        left.setBorder(BorderFactory.createTitledBorder("Register New User"));

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.setOpaque(false);

        JTextField txtUser = new JTextField();
        JPasswordField txtPass = new JPasswordField();
        JComboBox<String> cmbRole = new JComboBox<>(new String[]{"STAFF", "ADMIN"});
        JTextField txtCounter = new JTextField();

        form.add(new JLabel("Username:"));
        form.add(txtUser);
        form.add(new JLabel("Password:"));
        form.add(txtPass);
        form.add(new JLabel("Role:"));
        form.add(cmbRole);
        form.add(new JLabel("Counter (optional):"));
        form.add(txtCounter);

        JButton btnRegister = new JButton("Register");
        btnRegister.setBackground(MainFrame.NAV_BTN_BG);
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFocusPainted(false);

        left.add(form, BorderLayout.CENTER);
        left.add(btnRegister, BorderLayout.SOUTH);

        //  Online Staff Table 
        JPanel right = new JPanel(new BorderLayout(0, 10));
        right.setOpaque(false);
        right.setBorder(BorderFactory.createTitledBorder("Online Staff / Counter Assignment"));

        String[] cols = {"Username", "Role", "Counter", "Status"};
        onlineModel = new DefaultTableModel(cols, 0);

        JTable table = new JTable(onlineModel);
        right.add(new JScrollPane(table), BorderLayout.CENTER);

        // Optional refresh button
        JButton btnRefresh = new JButton("Refresh Online Staff");
        btnRefresh.setFocusPainted(false);
        right.add(btnRefresh, BorderLayout.SOUTH);

        center.add(left);
        center.add(right);
        add(center, BorderLayout.CENTER);

        //  Register button action 
        btnRegister.addActionListener(e -> {
            String u = txtUser.getText().trim();
            String p = new String(txtPass.getPassword());
            String role = (String) cmbRole.getSelectedItem();
            String userRole = (String) cmbRole.getSelectedItem();
            String counter = txtCounter.getText().trim();

            if (u.isEmpty() || p.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and password required");
                return;
            }

            btnRegister.setEnabled(false);

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    FirebaseHelper.saveUser(u, p, role, counter.isEmpty() ? "" : counter);
                    return null;
                }

                @Override
                protected void done() {
                    btnRegister.setEnabled(true);
                    try {
                        get(); 
                        onlineModel.addRow(new Object[]{
                            u, role,
                            counter.isEmpty() ? "-" : counter,
                            "Registered"
                        });
                        txtUser.setText("");
                        txtPass.setText("");
                        txtCounter.setText("");
                        JOptionPane.showMessageDialog(UserManagementPanel.this, "User registered successfully");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(UserManagementPanel.this,
                            "Failed to register user.\nCheck internet / Firebase URL.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            }.execute();
        });

        btnRefresh.addActionListener(e -> {
        JOptionPane.showMessageDialog(this, "Online staff list is updated when staff log in and select a counter.");
        });
    }
}