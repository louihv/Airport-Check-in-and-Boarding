import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class UserManagementPanel extends JPanel {
    private DefaultTableModel userModel;
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

        // Register new user
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

        // Online staff table
        JPanel right = new JPanel(new BorderLayout(0, 10));
        right.setOpaque(false);
        right.setBorder(BorderFactory.createTitledBorder("Online Staff / Counter Assignment"));

        String[] cols = {"Username", "Role", "Counter", "Status"};
        onlineModel = new DefaultTableModel(cols, 0);
        // sample data
        onlineModel.addRow(new Object[]{"staff01", "STAFF", "Counter 3", "Online"});
        onlineModel.addRow(new Object[]{"staff02", "STAFF", "Counter 1", "Online"});
        onlineModel.addRow(new Object[]{"admin", "ADMIN", "-", "Online"});

        JTable table = new JTable(onlineModel);
        right.add(new JScrollPane(table), BorderLayout.CENTER);

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

            // for demo just add to online table
            onlineModel.addRow(new Object[]{
                u, role,
                counter.isEmpty() ? "-" : counter,
                "Registered"
            });

            txtUser.setText("");
            txtPass.setText("");
            txtCounter.setText("");
            JOptionPane.showMessageDialog(this, "User registered successfully");
        });
    }
}