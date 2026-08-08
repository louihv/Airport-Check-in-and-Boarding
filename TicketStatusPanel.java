import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TicketStatusPanel extends JPanel {
    private JTextField txtSearchTicket;
    private JLabel lblTicketNumber, lblStatus, lblPosition, lblEstWait;

    public TicketStatusPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(MainFrame.MAIN_BG);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        searchPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Enter Ticket Number:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSearch.setForeground(new Color(27, 77, 46));

        txtSearchTicket = new JTextField(12);
        txtSearchTicket.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JButton btnSearch = new JButton("Check Status");
        btnSearch.setBackground(new Color(46, 125, 50));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSearch.setFocusPainted(false);
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));

        searchPanel.add(lblSearch);
        searchPanel.add(txtSearchTicket);
        searchPanel.add(btnSearch);

        // Status Details Display
        JPanel card = new JPanel(new GridLayout(4, 1, 10, 10));
        card.setBackground(new Color(235, 247, 238)); // Soft Mint Background
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(140, 195, 155), 2),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));

        lblTicketNumber = createDisplayLabel("Ticket: --", new Font("Segoe UI", Font.BOLD, 26), new Color(20, 60, 35));
        lblStatus = createDisplayLabel("Status: --", new Font("Segoe UI", Font.BOLD, 18), new Color(46, 125, 50));
        lblPosition = createDisplayLabel("Position in Queue: --", new Font("Segoe UI", Font.PLAIN, 15), new Color(40, 50, 45));
        lblEstWait = createDisplayLabel("Estimated Wait Time: --", new Font("Segoe UI", Font.PLAIN, 15), new Color(40, 50, 45));

        card.add(lblTicketNumber);
        card.add(lblStatus);
        card.add(lblPosition);
        card.add(lblEstWait);

        add(searchPanel, BorderLayout.NORTH);
        add(card, BorderLayout.CENTER);

        btnSearch.addActionListener(e -> updateStatusDisplay());
        QueueManager.getInstance().addListener(this::updateStatusDisplay);
    }

    private JLabel createDisplayLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    private void updateStatusDisplay() {
        String ticket = txtSearchTicket.getText().trim();
        if (ticket.isEmpty()) return;

        QueueManager qm = QueueManager.getInstance();
        List<Passenger> all = qm.getAllPassengers();
        List<Passenger> waiting = qm.getWaitingQueue();

        Passenger found = all.stream()
                .filter(p -> p.getTicketNumber().equalsIgnoreCase(ticket))
                .findFirst().orElse(null);

        if (found != null) {
            lblTicketNumber.setText("Ticket: " + found.getTicketNumber());
            lblStatus.setText("Status: " + found.getStatus());

            if ("WAITING".equals(found.getStatus())) {
                int pos = waiting.indexOf(found) + 1;
                lblPosition.setText("Position in Queue: " + pos);
                lblEstWait.setText("Estimated Wait Time: " + (pos * 3) + " mins");
            } else if ("SERVING".equals(found.getStatus())) {
                lblPosition.setText("Proceed to Counter: " + found.getAssignedCounter());
                lblEstWait.setText("Estimated Wait Time: Now Serving");
            } else {
                lblPosition.setText("Position in Queue: N/A");
                lblEstWait.setText("Estimated Wait Time: N/A");
            }
        } else {
            lblTicketNumber.setText("Ticket: Not Found");
            lblStatus.setText("Status: --");
            lblPosition.setText("Position in Queue: --");
            lblEstWait.setText("Estimated Wait Time: --");
        }
    }
}