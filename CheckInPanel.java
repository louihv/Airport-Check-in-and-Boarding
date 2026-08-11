import javax.swing.*;
import java.awt.*;

public class CheckInPanel extends JPanel {
    private JTextField txtBookingRef, txtName, txtFlight, txtBaggage;
    private MainFrame mainFrame;

    public CheckInPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(0, 0));
        setBackground(MainFrame.MAIN_BG);

        //  Top bar with Back button 
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        topBar.setOpaque(false);

        JButton backButton = new JButton("← Start Over");
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        backButton.setForeground(MainFrame.NAV_BTN_BG);
        backButton.setBackground(new Color(255, 255, 255, 0));
        backButton.setFocusPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        backButton.setContentAreaFilled(false);
        backButton.setOpaque(false);

        // Hover effect
        backButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                backButton.setBackground(MainFrame.NAV_BTN_BG);
                backButton.setForeground(Color.WHITE);
                backButton.setContentAreaFilled(true);
                backButton.setOpaque(true);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                backButton.setBackground(new Color(255, 255, 255, 0));
                backButton.setForeground(MainFrame.NAV_BTN_BG);
                backButton.setContentAreaFilled(false);
                backButton.setOpaque(false);
                backButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            }
        });

        backButton.addActionListener(e -> mainFrame.showPanel("PassengerMenu"));
        topBar.add(backButton);

        add(topBar, BorderLayout.NORTH);

        // Centered form card 
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

        // Title
        JLabel lblTitle = new JLabel("Passenger Check-In", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(MainFrame.NAV_BTN_BG);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 10, 20, 10);
        formCard.add(lblTitle, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Fields
        txtBookingRef = createStyledField();
        txtName = createStyledField();
        txtFlight = createStyledField();
        txtBaggage = createStyledField();

        addFormRow(formCard, "Booking Reference:", txtBookingRef, gbc, 1);
        addFormRow(formCard, "Passenger Name:", txtName, gbc, 2);
        addFormRow(formCard, "Flight Number:", txtFlight, gbc, 3);
        addFormRow(formCard, "Baggage Details:", txtBaggage, gbc, 4);

        // Submit button
        JButton btnSubmit = new JButton("Register & Issue Queue Ticket");
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSubmit.setBackground(MainFrame.SECONDARY_BTN_BG);
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setFocusPainted(false);
        btnSubmit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSubmit.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        btnSubmit.setContentAreaFilled(true);
        btnSubmit.setOpaque(true);

        // Hover effect for submit
        btnSubmit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnSubmit.setBackground(MainFrame.NAV_BTN_BG);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnSubmit.setBackground(MainFrame.SECONDARY_BTN_BG);
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(25, 10, 5, 10);
        formCard.add(btnSubmit, gbc);

        centerWrapper.add(formCard);
        add(centerWrapper, BorderLayout.CENTER);

        btnSubmit.addActionListener(e -> processCheckIn());
    }

    private JTextField createStyledField() {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
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
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(MainFrame.NAV_BTN_BG);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(field, gbc);
    }

    private void processCheckIn() {
        String ref = txtBookingRef.getText().trim();
        String name = txtName.getText().trim();
        String flight = txtFlight.getText().trim();
        String baggage = txtBaggage.getText().trim();

        if (ref.isEmpty() || name.isEmpty() || flight.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Invalid passenger details! Please fill all required fields.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        QueueManager qm = QueueManager.getInstance();
        String ticketNo = qm.generateTicketNumber();
        Passenger passenger = new Passenger(ref, name, flight, baggage, ticketNo);
        qm.addPassenger(passenger);

        JOptionPane.showMessageDialog(this,
                "Check-in Successful!\nQueue Ticket Issued: " + ticketNo,
                "Queue Ticket Generated", JOptionPane.INFORMATION_MESSAGE);

        // Clear fields
        txtBookingRef.setText("");
        txtName.setText("");
        txtFlight.setText("");
        txtBaggage.setText("");
    }
}