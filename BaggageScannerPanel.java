import java.awt.*;
import javax.swing.*;

public class BaggageScannerPanel extends JPanel {
    private JTextField txtBookingRef, txtWeight, txtTagNo;
    private JLabel lblStatus;
    private MainFrame mainFrame;

    public BaggageScannerPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(0, 0));
        setBackground(MainFrame.MAIN_BG);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        topBar.setOpaque(false);

        JButton backButton = createBackButton("", "/resources/back.png");
        backButton.addActionListener(e ->
            mainFrame.showPanel("Role")
        );

        topBar.add(backButton);
        add(topBar, BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 210, 190), 1),
            BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = new JLabel("Baggage Check-In & Tagging", SwingConstants.CENTER);
        lblTitle.setFont(AppFonts.bold(20));
        lblTitle.setForeground(MainFrame.NAV_BTN_BG);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 10, 20, 10);
        formCard.add(lblTitle, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 10, 10, 10);

        txtBookingRef = createStyledField();
        txtWeight = createStyledField();
        txtTagNo = createStyledField();

        addFormRow(formCard, "Booking Reference:", txtBookingRef, gbc, 1);
        addFormRow(formCard, "Baggage Weight (kg):", txtWeight, gbc, 2);
        addFormRow(formCard, "Luggage Tag ID:", txtTagNo, gbc, 3);

        JButton btnProcess = new JButton("Attach Tag & Verify Weight");
        btnProcess.setFont(AppFonts.bold(13));
        btnProcess.setBackground(MainFrame.SECONDARY_BTN_BG);
        btnProcess.setForeground(Color.WHITE);
        btnProcess.setFocusPainted(false);
        btnProcess.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnProcess.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        btnProcess.setContentAreaFilled(true);
        btnProcess.setOpaque(true);

        btnProcess.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnProcess.setBackground(MainFrame.NAV_BTN_BG);
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                btnProcess.setBackground(MainFrame.SECONDARY_BTN_BG);
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(25, 10, 5, 10);
        formCard.add(btnProcess, gbc);

        lblStatus = new JLabel("Ready to scan...", SwingConstants.CENTER);
        lblStatus.setFont(AppFonts.italic(12));
        lblStatus.setForeground(MainFrame.NAV_BTN_BG);

        gbc.gridy = 5;
        gbc.insets = new Insets(10, 10, 5, 10);
        formCard.add(lblStatus, gbc);

        centerWrapper.add(formCard);
        add(centerWrapper, BorderLayout.CENTER);

        btnProcess.addActionListener(e -> processBaggage());
    }

    private JButton createBackButton(String text, String iconPath) {
        JButton btn = new JButton(text);

        try {
            ImageIcon original = new ImageIcon(
                getClass().getResource(iconPath)
            );

            Image scaled = original.getImage().getScaledInstance(
                22, 22, Image.SCALE_SMOOTH
            );

            btn.setIcon(new ImageIcon(scaled));
            btn.setHorizontalAlignment(SwingConstants.CENTER);

        } catch (Exception e) {
            System.err.println("Could not load icon: " + iconPath);
        }

        btn.setBackground(new Color(255, 255, 255, 0));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(MainFrame.NAV_BTN_BG);
                btn.setContentAreaFilled(true);
                btn.setOpaque(true);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(255, 255, 255, 0));
                btn.setContentAreaFilled(false);
                btn.setOpaque(false);
            }
        });

        return btn;
    }

    private JTextField createStyledField() {
        JTextField field = new JTextField(20);
        field.setFont(AppFonts.regular(13));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 200, 185), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }

    private void addFormRow(JPanel panel, String labelText, JTextField field,
                            GridBagConstraints gbc, int row) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;

        JLabel label = new JLabel(labelText);
        label.setFont(AppFonts.bold(13));
        label.setForeground(MainFrame.NAV_BTN_BG);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(field, gbc);
    }

    private void processBaggage() {
        String ref = txtBookingRef.getText().trim();
        String weightStr = txtWeight.getText().trim();
        String tag = txtTagNo.getText().trim();

        if (ref.isEmpty() || weightStr.isEmpty() || tag.isEmpty()) {
            JOptionPane.showMessageDialog(
                this, "Please enter all baggage fields.","Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double weight = Double.parseDouble(weightStr);

            if (weight > 23.0) {
                lblStatus.setText(
                    "Warning: Excess Baggage Fee Required (" + weight + " kg)"
                );
                lblStatus.setForeground(Color.RED);
            } else {
                lblStatus.setText(
                    "Baggage Verified & Tagged Successfully!"
                );
                lblStatus.setForeground(MainFrame.SECONDARY_BTN_BG);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Invalid weight value.",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}