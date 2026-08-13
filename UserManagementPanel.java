import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class UserManagementPanel extends JPanel {
    private DefaultTableModel onlineModel;
    private JButton btnRefresh;

    public UserManagementPanel() {
        setBackground(MainFrame.MAIN_BG);
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("User Management");
        title.setFont(AppFonts.bold(22));
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 20, 0));
        center.setOpaque(false);

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

        JPanel right = new JPanel(new BorderLayout(0, 10));
        right.setOpaque(false);
        right.setBorder(BorderFactory.createTitledBorder("All Users (Admin / Staff)"));

        String[] cols = {"Username", "Role", "Counter", "Status"};
        onlineModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(onlineModel);
        right.add(new JScrollPane(table), BorderLayout.CENTER);

        btnRefresh = new JButton("Refresh Users");
        btnRefresh.setFocusPainted(false);
        right.add(btnRefresh, BorderLayout.SOUTH);

        center.add(left);
        center.add(right);
        add(center, BorderLayout.CENTER);

        btnRegister.addActionListener(e -> {
            String u = txtUser.getText().trim();
            String p = new String(txtPass.getPassword());
            String role = (String) cmbRole.getSelectedItem();
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
                        txtUser.setText("");
                        txtPass.setText("");
                        txtCounter.setText("");
                        JOptionPane.showMessageDialog(UserManagementPanel.this, "User registered successfully");
                        loadUsers();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(UserManagementPanel.this,
                            "Failed to register user.\nCheck internet / Firebase URL.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            }.execute();
        });

        btnRefresh.addActionListener(e -> loadUsers());

        loadUsers();
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
                    JOptionPane.showMessageDialog(UserManagementPanel.this,
                        "Failed to load users.\nCheck internet / Firebase URL.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        }.execute();
    }
}