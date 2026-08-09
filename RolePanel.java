import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class RolePanel extends JPanel {
    public RolePanel(MainFrame frame) {
        setBackground(MainFrame.MAIN_BG);
        setLayout(new GridBagLayout());

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(MainFrame.MAIN_BG);

        JLabel title = new JLabel("Select Role", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnKiosk = createBigButton("PASSENGER KIOSK");
        JButton btnLogin = createBigButton("LOGIN PORTAL");

        btnKiosk.addActionListener(e -> frame.showPassengerMenu());
        btnLogin.addActionListener(e -> frame.showLogin());

        box.add(title);
        box.add(Box.createVerticalStrut(40));
        box.add(btnKiosk);
        box.add(Box.createVerticalStrut(20));
        box.add(btnLogin);

        add(box);
    }

    private JButton createBigButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setPreferredSize(new Dimension(320, 60));
        btn.setMaximumSize(new Dimension(320, 60));
        btn.setBackground(MainFrame.NAV_BTN_BG);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }
}