import javax.swing.*;
import java.awt.*;

public class CounterStaffPanel extends JPanel {
    private JComboBox<Integer> comboCounters;
    private JLabel lblServingTicket, lblPassengerName, lblFlight, lblBaggage;

    public CounterStaffPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(MainFrame.MAIN_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top Selector Bar
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setOpaque(false);

        JLabel lblSelect = new JLabel("Assigned Workstation:");
        lblSelect.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSelect.setForeground(new Color(27, 77, 46));

        comboCounters = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        comboCounters.setFont(new Font("Segoe UI", Font.BOLD, 12));

        topPanel.add(lblSelect);
        topPanel.add(comboCounters);

        // Active Passenger Details Panel
        JPanel infoPanel = new JPanel(new GridLayout(4, 1, 8, 8));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 210, 190), 1),
                "Currently Serving Passenger", 0, 0, new Font("Segoe UI", Font.BOLD, 14), new Color(27, 77, 46)
            ),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        lblServingTicket = new JLabel("Queue Ticket: None");
        lblServingTicket.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblServingTicket.setForeground(new Color(27, 77, 46));

        lblPassengerName = createDetailLabel("Name: N/A");
        lblFlight = createDetailLabel("Flight: N/A");
        lblBaggage = createDetailLabel("Baggage: N/A");

        infoPanel.add(lblServingTicket);
        infoPanel.add(lblPassengerName);
        infoPanel.add(lblFlight);
        infoPanel.add(lblBaggage);

        // Actions
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setOpaque(false);

        JButton btnCallNext = createActionButton("Call Next Passenger", new Color(46, 125, 50));
        JButton btnComplete = createActionButton("Complete Service", new Color(27, 77, 46));
        JButton btnSkip = createActionButton("Skip Passenger", new Color(180, 40, 40)); // Red highlight for skip action

        btnPanel.add(btnCallNext);
        btnPanel.add(btnComplete);
        btnPanel.add(btnSkip);

        add(topPanel, BorderLayout.NORTH);
        add(infoPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        comboCounters.addActionListener(e -> refreshDisplay());
        btnCallNext.addActionListener(e -> {
            int counter = (Integer) comboCounters.getSelectedItem();
            QueueManager.getInstance().callNextPassenger(counter);
            refreshDisplay();
        });
        btnComplete.addActionListener(e -> {
            int counter = (Integer) comboCounters.getSelectedItem();
            QueueManager.getInstance().completeService(counter);
            refreshDisplay();
        });
        btnSkip.addActionListener(e -> {
            int counter = (Integer) comboCounters.getSelectedItem();
            QueueManager.getInstance().skipPassenger(counter);
            refreshDisplay();
        });

        QueueManager.getInstance().addListener(this::refreshDisplay);
    }

    private JLabel createDetailLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(new Color(50, 65, 55));
        return label;
    }

    private JButton createActionButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        return btn;
    }

    private void refreshDisplay() {
        int counter = (Integer) comboCounters.getSelectedItem();
        Passenger current = QueueManager.getInstance().getServingPassenger(counter);

        if (current != null) {
            lblServingTicket.setText("Queue Ticket: " + current.getTicketNumber());
            lblPassengerName.setText("Name: " + current.getName());
            lblFlight.setText("Flight: " + current.getFlightNumber());
            lblBaggage.setText("Baggage: " + current.getBaggageInfo());
        } else {
            lblServingTicket.setText("Queue Ticket: None");
            lblPassengerName.setText("Name: N/A");
            lblFlight.setText("Flight: N/A");
            lblBaggage.setText("Baggage: N/A");
        }
    }
}
