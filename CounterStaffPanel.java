import java.awt.*;
import javax.swing.*;
import java.util.List;
import java.util.Map;

public class CounterStaffPanel extends JPanel {
    private JComboBox<Integer> comboCounters;
    private JLabel lblServingTicket, lblPassengerName, lblFlight, lblBaggage;
    private MainFrame mainFrame;
    private String currentTicketNo = null;   

    public CounterStaffPanel(MainFrame frame) {
        this.mainFrame = frame;

        setLayout(new BorderLayout(15, 15));
        setBackground(MainFrame.MAIN_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setOpaque(false);

        JLabel lblSelect = new JLabel("Assigned Counter:");
        lblSelect.setFont(AppFonts.bold(13));
        lblSelect.setForeground(new Color(27, 77, 46));

        comboCounters = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        comboCounters.setFont(AppFonts.bold(12));

        topPanel.add(lblSelect);
        topPanel.add(comboCounters);

        JPanel infoPanel = new JPanel(new GridLayout(4, 1, 8, 8));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 210, 190), 1),
                "Currently Serving Passenger", 0, 0, AppFonts.bold(14), new Color(27, 77, 46)
            ),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        lblServingTicket = new JLabel("Queue Ticket: None");
        lblServingTicket.setFont(AppFonts.bold(20));
        lblServingTicket.setForeground(new Color(27, 77, 46));

        lblPassengerName = createDetailLabel("Name: N/A");
        lblFlight        = createDetailLabel("Flight: N/A");
        lblBaggage       = createDetailLabel("Baggage: N/A");

        infoPanel.add(lblServingTicket);
        infoPanel.add(lblPassengerName);
        infoPanel.add(lblFlight);
        infoPanel.add(lblBaggage);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setOpaque(false);

        JButton btnCallNext = createActionButton("Call Next Passenger", new Color(46, 125, 50));
        JButton btnComplete = createActionButton("Complete Service", new Color(27, 77, 46));
        JButton btnSkip     = createActionButton("Skip Passenger", new Color(180, 40, 40));

        btnPanel.add(btnCallNext);
        btnPanel.add(btnComplete);
        btnPanel.add(btnSkip);

        add(topPanel, BorderLayout.NORTH);
        add(infoPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        comboCounters.addActionListener(e -> {
            currentTicketNo = null;
            refreshDisplay();
            updateOnlineStatus();
            autoCallFirstPassenger();  
        });

        btnCallNext.addActionListener(e -> callNextPassenger());
        btnComplete.addActionListener(e -> completeService());
        btnSkip.addActionListener(e -> skipPassenger());

        SwingUtilities.invokeLater(() -> {
            updateOnlineStatus();
            autoCallFirstPassenger();
            refreshDisplay();
        });
    }

    private void autoCallFirstPassenger() {
        if (currentTicketNo != null) return;

        new SwingWorker<Map<String, String>, Void>() {
            @Override
            protected Map<String, String> doInBackground() throws Exception {
                List<Map<String, String>> waiting = FirebaseHelper.getWaitingTickets();
                if (waiting == null || waiting.isEmpty()) return null;
                return waiting.get(0);
            }

            @Override
            protected void done() {
                try {
                    Map<String, String> next = get();
                    if (next == null) {
                        clearDisplay();
                        return;
                    }

                    int counter = (Integer) comboCounters.getSelectedItem();
                    String ticketNo = next.get("ticketNo");

                    FirebaseHelper.updateTicketStatus(ticketNo, "SERVING", counter);
                    currentTicketNo = ticketNo;
                    updateLabels(next);
                } catch (Exception ex) {
                    clearDisplay();
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void callNextPassenger() {
        int counter = (Integer) comboCounters.getSelectedItem();

        new SwingWorker<Map<String, String>, Void>() {
            @Override
            protected Map<String, String> doInBackground() throws Exception {
                if (currentTicketNo != null) return null;

                List<Map<String, String>> waiting = FirebaseHelper.getWaitingTickets();
                if (waiting == null || waiting.isEmpty()) return null;
                return waiting.get(0);
            }

            @Override
            protected void done() {
                try {
                    Map<String, String> next = get();
                    if (next == null) {
                        JOptionPane.showMessageDialog(CounterStaffPanel.this,
                                "No more passengers waiting in the queue.",
                                "Queue Empty", JOptionPane.INFORMATION_MESSAGE);
                        clearDisplay();
                        return;
                    }

                    String ticketNo = next.get("ticketNo");
                    FirebaseHelper.updateTicketStatus(ticketNo, "SERVING", counter);
                    currentTicketNo = ticketNo;
                    updateLabels(next);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(CounterStaffPanel.this,
                            "Failed to call next passenger.\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void completeService() {
        if (currentTicketNo == null) {
            JOptionPane.showMessageDialog(this, "No passenger is currently being served.");
            return;
        }

        int counter = (Integer) comboCounters.getSelectedItem();
        String ticket = currentTicketNo;

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                FirebaseHelper.updateTicketStatus(ticket, "COMPLETED", counter);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    currentTicketNo = null;
                    clearDisplay();
                    autoCallFirstPassenger();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(CounterStaffPanel.this,
                            "Failed to complete service.\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void skipPassenger() {
        if (currentTicketNo == null) {
            JOptionPane.showMessageDialog(this, "No passenger is currently being served.");
            return;
        }

        int counter = (Integer) comboCounters.getSelectedItem();
        String ticket = currentTicketNo;

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                FirebaseHelper.updateTicketStatus(ticket, "SKIPPED", counter);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    currentTicketNo = null;
                    clearDisplay();
                    autoCallFirstPassenger();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(CounterStaffPanel.this,
                            "Failed to skip passenger.\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void refreshDisplay() {
        if (currentTicketNo == null) {
            clearDisplay();
            return;
        }

        new SwingWorker<Map<String, String>, Void>() {
            @Override
            protected Map<String, String> doInBackground() throws Exception {
                return FirebaseHelper.getTicketByNumber(currentTicketNo);
            }

            @Override
            protected void done() {
                try {
                    Map<String, String> t = get();
                    if (t == null || !"SERVING".equalsIgnoreCase(t.get("status"))) {
                        currentTicketNo = null;
                        clearDisplay();
                    } else {
                        updateLabels(t);
                    }
                } catch (Exception ex) {
                    clearDisplay();
                }
            }
        }.execute();
    }

    private void updateLabels(Map<String, String> t) {
        lblServingTicket.setText("Queue Ticket: " + t.get("ticketNo"));
        lblPassengerName.setText("Name: " + t.get("name"));
        lblFlight.setText("Flight: " + t.get("flightNo"));
        lblBaggage.setText("Baggage: " + t.get("baggage"));
    }

    private void clearDisplay() {
        lblServingTicket.setText("Queue Ticket: None");
        lblPassengerName.setText("Name: N/A");
        lblFlight.setText("Flight: N/A");
        lblBaggage.setText("Baggage: N/A");
    }

    private void updateOnlineStatus() {
        String username = mainFrame.getCurrentUsername();
        String role     = mainFrame.getCurrentRole();

        if (username == null || username.isEmpty() || role == null) return;

        int counter = (Integer) comboCounters.getSelectedItem();

        new Thread(() -> {
            try {
                FirebaseHelper.setOnline(username, role, String.valueOf(counter));
            } catch (Exception ex) {
                System.err.println("Failed to update online status: " + ex.getMessage());
            }
        }).start();
    }

    private JLabel createDetailLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(AppFonts.regular(14));
        label.setForeground(new Color(50, 65, 55));
        return label;
    }

    private JButton createActionButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(AppFonts.bold(12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        return btn;
    }
}