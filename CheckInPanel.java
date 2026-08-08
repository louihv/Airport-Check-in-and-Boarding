import javax.swing.*;
import java.awt.*;

public class CheckInPanel extends JPanel {
    private JTextField txtBookingRef, txtName, txtFlight, txtBaggage;

    public CheckInPanel() {
        setLayout(new GridBagLayout());
        setBackground(MainFrame.MAIN_BG);

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 210, 190), 1),
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = new JLabel("Passenger Check-In", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(27, 77, 46));

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formCard.add(lblTitle, gbc);
        gbc.gridwidth = 1;

        txtBookingRef = createStyledField();
        txtName = createStyledField();
        txtFlight = createStyledField();
        txtBaggage = createStyledField();

        addFormRow(formCard, "Booking Ref / QR:", txtBookingRef, gbc, 1);
        addFormRow(formCard, "Passenger Name:", txtName, gbc, 2);
        addFormRow(formCard, "Flight Number:", txtFlight, gbc, 3);
        addFormRow(formCard, "Baggage Details:", txtBaggage, gbc, 4);

        JButton btnSubmit = new JButton("Register & Issue Queue Ticket");
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSubmit.setBackground(new Color(46, 125, 50));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setFocusPainted(false);
        btnSubmit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSubmit.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        formCard.add(btnSubmit, gbc);

        add(formCard);

        btnSubmit.addActionListener(e -> processCheckIn());
    }

    private JTextField createStyledField() {
        JTextField field = new JTextField(18);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 200, 185), 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        return field;
    }

    private void addFormRow(JPanel panel, String labelText, JTextField field, GridBagConstraints gbc, int row) {
        gbc.gridy = row;
        gbc.gridx = 0;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(40, 60, 48));
        panel.add(label, gbc);

        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void processCheckIn() {
        String ref = txtBookingRef.getText().trim();
        String name = txtName.getText().trim();
        String flight = txtFlight.getText().trim();
        String baggage = txtBaggage.getText().trim();

        if (ref.isEmpty() || name.isEmpty() || flight.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Invalid passenger details! Please fill all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        QueueManager qm = QueueManager.getInstance();
        String ticketNo = qm.generateTicketNumber();
        Passenger passenger = new Passenger(ref, name, flight, baggage, ticketNo);
        qm.addPassenger(passenger);

        JOptionPane.showMessageDialog(this, "Check-in Successful!\nQueue Ticket Issued: " + ticketNo, "Queue Ticket Generated", JOptionPane.INFORMATION_MESSAGE);

        txtBookingRef.setText("");
        txtName.setText("");
        txtFlight.setText("");
        txtBaggage.setText("");
    }
}