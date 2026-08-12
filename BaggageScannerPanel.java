import javax.swing.*;
import java.awt.*;

public class BaggageScannerPanel extends JPanel {
    private JTextField txtBookingRef, txtWeight, txtTagNo;
    private JLabel lblStatus;

    public BaggageScannerPanel() {
        setLayout(new GridBagLayout());
        setBackground(MainFrame.MAIN_BG);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 210, 190), 1),
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Baggage Check-In & Tagging", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(27, 77, 46));

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(title, gbc);
        gbc.gridwidth = 1;

        txtBookingRef = new JTextField(15);
        txtWeight = new JTextField(15);
        txtTagNo = new JTextField(15);

        addRow(card, "Booking Reference:", txtBookingRef, gbc, 1);
        addRow(card, "Baggage Weight (kg):", txtWeight, gbc, 2);
        addRow(card, "Luggage Tag ID:", txtTagNo, gbc, 3);

        JButton btnProcess = new JButton("Attach Tag & Verify Weight");
        btnProcess.setBackground(new Color(46, 125, 50));
        btnProcess.setForeground(Color.WHITE);
        btnProcess.setFont(new Font("Segoe UI", Font.BOLD, 13));

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        card.add(btnProcess, gbc);

        lblStatus = new JLabel("Ready to scan...", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        gbc.gridy = 5;
        card.add(lblStatus, gbc);

        btnProcess.addActionListener(e -> processBaggage());
        add(card);
    }

    private void addRow(JPanel panel, String label, JTextField field, GridBagConstraints gbc, int row) {
        gbc.gridy = row; gbc.gridx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void processBaggage() {
        String ref = txtBookingRef.getText().trim();
        String weightStr = txtWeight.getText().trim();
        String tag = txtTagNo.getText().trim();

        if (ref.isEmpty() || weightStr.isEmpty() || tag.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter all baggage fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double weight = Double.parseDouble(weightStr);
            if (weight > 23.0) {
                lblStatus.setText("Warning: Excess Baggage Fee Required (" + weight + " kg)");
                lblStatus.setForeground(Color.RED);
            } else {
                lblStatus.setText("Baggage Verified & Tagged Successfully!");
                lblStatus.setForeground(new Color(46, 125, 50));
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid weight value.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}