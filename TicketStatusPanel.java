import java.awt.*;
import java.util.List;
import java.util.Map;
import javax.swing.*;

public class TicketStatusPanel extends JPanel {
    private JTextField txtSearchTicket;
    private JLabel lblTicketValue, lblStatusValue, lblPositionValue, lblEstWaitValue;
    private MainFrame mainFrame;

    private static final int AVG_SERVICE_MINUTES = 3;

    public TicketStatusPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(0, 0));
        setBackground(MainFrame.MAIN_BG);

        // ========== TOP BAR ==========
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        topBar.setOpaque(false);

        JButton backButton = createBackButton("", "/resources/back.png");
        backButton.addActionListener(e -> mainFrame.showPanel("PassengerMenu"));
        topBar.add(backButton);
        add(topBar, BorderLayout.NORTH);

        // ========== CENTER CARD ==========
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 210, 190), 1),
            BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Title
        JLabel lblTitle = new JLabel("Queue Ticket Status", SwingConstants.CENTER);
        lblTitle.setFont(AppFonts.bold(20));
        lblTitle.setForeground(MainFrame.NAV_BTN_BG);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 10, 20, 10);
        card.add(lblTitle, gbc);

        // Search
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel lblSearch = new JLabel("Ticket Number:");
        lblSearch.setFont(AppFonts.bold(13));
        lblSearch.setForeground(MainFrame.NAV_BTN_BG);

        txtSearchTicket = createStyledField();

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        card.add(lblSearch, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        card.add(txtSearchTicket, gbc);

        // Search button
        JButton btnSearch = new JButton("Check Status");
        btnSearch.setFont(AppFonts.bold(13));
        btnSearch.setBackground(MainFrame.SECONDARY_BTN_BG);
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFocusPainted(false);
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSearch.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        btnSearch.setContentAreaFilled(true);
        btnSearch.setOpaque(true);

        btnSearch.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnSearch.setBackground(MainFrame.NAV_BTN_BG);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnSearch.setBackground(MainFrame.SECONDARY_BTN_BG);
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15, 10, 15, 10);
        card.add(btnSearch, gbc);

        // Horizontal line
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(new Color(180, 210, 190));
        separator.setPreferredSize(new Dimension(1, 2));

        gbc.gridy = 3;
        gbc.insets = new Insets(10, 10, 15, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        card.add(separator, gbc);

        // Status card
        JPanel statusCard = new JPanel(new GridBagLayout());
        statusCard.setBackground(new Color(235, 245, 255));
        statusCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MainFrame.NAV_BTN_BG, 1),
            BorderFactory.createEmptyBorder(18, 25, 18, 25)
        ));

        GridBagConstraints sc = new GridBagConstraints();
        sc.fill = GridBagConstraints.HORIZONTAL;
        sc.insets = new Insets(6, 8, 6, 8);
        sc.anchor = GridBagConstraints.WEST;

        // Ticket Number
        sc.gridx = 0; sc.gridy = 0;
        JLabel lblTicketHeader = new JLabel("Ticket Number:");
        lblTicketHeader.setFont(AppFonts.regular(12));
        lblTicketHeader.setForeground(new Color(80, 90, 85));
        statusCard.add(lblTicketHeader, sc);

        sc.gridx = 1;
        lblTicketValue = new JLabel("--");
        lblTicketValue.setFont(AppFonts.bold(20));
        lblTicketValue.setForeground(MainFrame.NAV_BTN_BG);
        statusCard.add(lblTicketValue, sc);

        // Status
        sc.gridx = 0; sc.gridy = 1;
        JLabel lblStatusHeader = new JLabel("Status:");
        lblStatusHeader.setFont(AppFonts.regular(12));
        lblStatusHeader.setForeground(new Color(80, 90, 85));
        statusCard.add(lblStatusHeader, sc);

        sc.gridx = 1;
        lblStatusValue = new JLabel("--");
        lblStatusValue.setFont(AppFonts.bold(15));
        lblStatusValue.setForeground(MainFrame.SECONDARY_BTN_BG);
        statusCard.add(lblStatusValue, sc);

        // Position
        sc.gridx = 0; sc.gridy = 2;
        JLabel lblPosHeader = new JLabel("Position in Queue:");
        lblPosHeader.setFont(AppFonts.regular(12));
        lblPosHeader.setForeground(new Color(80, 90, 85));
        statusCard.add(lblPosHeader, sc);

        sc.gridx = 1;
        lblPositionValue = new JLabel("--");
        lblPositionValue.setFont(AppFonts.regular(13));
        lblPositionValue.setForeground(new Color(40, 50, 45));
        statusCard.add(lblPositionValue, sc);

        // Estimated Wait
        sc.gridx = 0; sc.gridy = 3;
        JLabel lblWaitHeader = new JLabel("Estimated Wait Time:");
        lblWaitHeader.setFont(AppFonts.regular(12));
        lblWaitHeader.setForeground(new Color(80, 90, 85));
        statusCard.add(lblWaitHeader, sc);

        sc.gridx = 1;
        lblEstWaitValue = new JLabel("--");
        lblEstWaitValue.setFont(AppFonts.regular(13));
        lblEstWaitValue.setForeground(new Color(40, 50, 45));
        statusCard.add(lblEstWaitValue, sc);

        gbc.gridy = 4;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.gridwidth = 2;
        card.add(statusCard, gbc);

        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);

        // Listeners
        btnSearch.addActionListener(e -> updateStatusDisplay());

        // Also allow pressing Enter in the text field
        txtSearchTicket.addActionListener(e -> updateStatusDisplay());

        // Load default (current serving / recent) ticket when panel opens
        SwingUtilities.invokeLater(this::updateStatusDisplay);
    }

    // ==================== CORE LOGIC (Firebase) ====================
    private void updateStatusDisplay() {
        // Show loading state immediately
        lblTicketValue.setText("Loading...");
        lblStatusValue.setText("...");
        lblPositionValue.setText("...");
        lblEstWaitValue.setText("...");

        String searchTicket = txtSearchTicket.getText().trim();

        // Run network call off the EDT
        new SwingWorker<Void, Void>() {
            private Map<String, String> target;
            private List<Map<String, String>> waiting;
            private String errorMsg;

            @Override
            protected Void doInBackground() {
                try {
                    List<Map<String, String>> all = FirebaseHelper.getAllTickets();
                    waiting = FirebaseHelper.getWaitingTickets();

                    if (searchTicket.isEmpty()) {
                        // Default → currently SERVING, or most recent non-WAITING
                        target = findCurrentOrRecentTicket(all);
                    } else {
                        target = FirebaseHelper.getTicketByNumber(searchTicket);
                    }
                } catch (Exception ex) {
                    errorMsg = ex.getMessage();
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done() {
                if (errorMsg != null) {
                    lblTicketValue.setText("Error");
                    lblStatusValue.setText("Could not reach Firebase");
                    lblPositionValue.setText("--");
                    lblEstWaitValue.setText("--");
                    return;
                }

                if (target != null) {
                    displayTicket(target, waiting);
                } else {
                    if (searchTicket.isEmpty()) {
                        lblTicketValue.setText("No tickets yet");
                        lblStatusValue.setText("--");
                        lblPositionValue.setText("--");
                        lblEstWaitValue.setText("--");
                    } else {
                        lblTicketValue.setText("Not Found");
                        lblStatusValue.setText("--");
                        lblPositionValue.setText("--");
                        lblEstWaitValue.setText("--");
                    }
                }
            }
        }.execute();
    }

    private Map<String, String> findCurrentOrRecentTicket(List<Map<String, String>> all) {
        // Prefer currently SERVING
        for (Map<String, String> t : all) {
            if ("SERVING".equalsIgnoreCase(t.get("status"))) {
                return t;
            }
        }

        // Fallback: most recent non-WAITING ticket
        for (int i = all.size() - 1; i >= 0; i--) {
            Map<String, String> t = all.get(i);
            if (!"WAITING".equalsIgnoreCase(t.get("status"))) {
                return t;
            }
        }
        return null;
    }

    private void displayTicket(Map<String, String> found, List<Map<String, String>> waiting) {
        String ticketNo = found.get("ticketNo");
        String status   = found.get("status") != null ? found.get("status").toUpperCase() : "";

        lblTicketValue.setText(ticketNo != null ? ticketNo : "--");
        lblStatusValue.setText(status.isEmpty() ? "--" : status);

        if ("WAITING".equals(status)) {
            int position = -1;
            for (int i = 0; i < waiting.size(); i++) {
                if (ticketNo != null && ticketNo.equalsIgnoreCase(waiting.get(i).get("ticketNo"))) {
                    position = i + 1;
                    break;
                }
            }

            if (position > 0) {
                int ahead = position - 1;
                lblPositionValue.setText(position + "  (" + ahead + " ticket" + (ahead == 1 ? "" : "s") + " ahead)");

                int waitMins = ahead * AVG_SERVICE_MINUTES;
                lblEstWaitValue.setText(waitMins <= 0
                        ? "Almost your turn"
                        : waitMins + " min" + (waitMins > 1 ? "s" : ""));
            } else {
                lblPositionValue.setText("Calculating...");
                lblEstWaitValue.setText("Calculating...");
            }

        } else if ("SERVING".equals(status)) {
            String counter = found.get("counter");
            if (counter != null && !counter.equals("0") && !counter.isEmpty()) {
                lblPositionValue.setText("Proceed to Counter " + counter);
            } else {
                lblPositionValue.setText("Proceed to Counter");
            }
            lblEstWaitValue.setText("Now Serving");

        } else {
            // COMPLETED, CANCELLED, etc.
            lblPositionValue.setText("N/A");
            lblEstWaitValue.setText("N/A");
        }
    }

    // ==================== UI HELPERS ====================
    private JButton createBackButton(String text, String iconPath) {
        JButton btn = new JButton(text);
        try {
            ImageIcon original = new ImageIcon(getClass().getResource(iconPath));
            Image scaled = original.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
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
}