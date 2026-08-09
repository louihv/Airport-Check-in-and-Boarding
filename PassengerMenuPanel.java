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

public class PassengerMenuPanel extends JPanel {
    public PassengerMenuPanel(MainFrame frame) {
        setBackground(MainFrame.MAIN_BG);
        setLayout(new GridBagLayout());

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(MainFrame.MAIN_BG);

        JLabel title = new JLabel("What would you like to do?", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnCheckIn = createBigButton("Check-In");
        JButton btnTicket = createBigButton("Ticket Status");
        JButton btnBack = createBigButton("Back");

        btnCheckIn.addActionListener(e -> frame.showCheckIn());
        btnTicket.addActionListener(e -> frame.showTicketStatus());
        btnBack.addActionListener(e -> frame.backToRole());

        box.add(title);
        box.add(Box.createVerticalStrut(40));
        box.add(btnCheckIn);
        box.add(Box.createVerticalStrut(20));
        box.add(btnTicket);
        box.add(Box.createVerticalStrut(30));
        box.add(btnBack);

        add(box);
    }

    private JButton createBigButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setPreferredSize(new Dimension(280, 50));
        btn.setMaximumSize(new Dimension(280, 50));
        btn.setBackground(MainFrame.NAV_BTN_BG);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }
}