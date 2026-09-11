import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.AbstractBorder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CounterStaffPanel extends JPanel {
    private JToggleButton[] counterButtons;
    private int selectedCounter = 1;
    private JLabel lblServingTicket, lblPassengerName, lblFlight, lblBaggage;
    private JLabel lblCurrentTicket, lblNextTicket;
    private JLabel lblDone, lblRemaining, lblUpcoming, lblSkipped, lblTransferred, lblOnHold;
    private JLabel lblWaited;
    private MainFrame mainFrame;
    private String currentTicketNo = null;

    public CounterStaffPanel(MainFrame frame) {
        this.mainFrame = frame;

        setLayout(new BorderLayout(12, 12));
        setBackground(MainFrame.MAIN_BG);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topBar.setOpaque(false);

        JLabel lblSelect = new JLabel("Assigned Counter:");
        lblSelect.setFont(AppFonts.bold(13));
        lblSelect.setForeground(new Color(27, 77, 46));
        topBar.add(lblSelect);
        topBar.add(createCounterSelector());

        JPanel centerStack = new JPanel();
        centerStack.setLayout(new BoxLayout(centerStack, BoxLayout.Y_AXIS));
        centerStack.setOpaque(false);
        centerStack.add(createPassengerPanel());
        centerStack.add(Box.createVerticalStrut(12));
        centerStack.add(createControlPanel());

        add(topBar, BorderLayout.NORTH);
        add(centerStack, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> {
            updateOnlineStatus();
            loadCurrentServingTicket();
        });
    }

    private JPanel createPassengerPanel() {
        RoundedPanel panel = new RoundedPanel(16, Color.WHITE);
        panel.setLayout(new BorderLayout(16, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1, MainFrame.SECONDARY_BTN_BG, 16),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));

        JLabel title = new JLabel("Current Customer");
        title.setFont(AppFonts.bold(15));
        title.setForeground(new Color(27, 77, 46));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        lblServingTicket = new JLabel("---");
        lblServingTicket.setFont(AppFonts.bold(22));
        lblServingTicket.setForeground(new Color(27, 77, 46));

        lblPassengerName = createDetailLabel("Name: ");
        lblFlight = createDetailLabel("Flight: ");
        lblBaggage = createDetailLabel("Baggage: ");

        info.add(lblServingTicket);
        info.add(Box.createVerticalStrut(6));
        info.add(lblPassengerName);
        info.add(Box.createVerticalStrut(2));
        info.add(lblFlight);
        info.add(Box.createVerticalStrut(2));
        info.add(lblBaggage);

        lblWaited = new JLabel("00m 00s Waited");
        lblWaited.setFont(AppFonts.regular(13));
        lblWaited.setForeground(new Color(120, 130, 125));
        lblWaited.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.add(lblWaited, BorderLayout.NORTH);

        panel.add(title, BorderLayout.NORTH);
        panel.add(info, BorderLayout.CENTER);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    private JPanel createControlPanel() {
        RoundedPanel panel = new RoundedPanel(16, Color.WHITE);
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1, MainFrame.SECONDARY_BTN_BG, 16),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        JPanel topRow = new JPanel(new GridBagLayout());
        topRow.setOpaque(false);

        JPanel leftBlock = new JPanel(new BorderLayout(8, 8));
        leftBlock.setOpaque(false);

        RoundedPanel currentBox = new RoundedPanel(12, new Color(248, 250, 248));
        currentBox.setLayout(new BorderLayout());
        currentBox.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1, MainFrame.SECONDARY_BTN_BG, 12),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        JLabel currentLbl = new JLabel("Current");
        currentLbl.setFont(AppFonts.regular(11));
        currentLbl.setForeground(new Color(100, 115, 105));
        lblCurrentTicket = new JLabel("---");
        lblCurrentTicket.setFont(AppFonts.bold(26));
        lblCurrentTicket.setForeground(new Color(27, 77, 46));
        currentBox.add(currentLbl, BorderLayout.NORTH);
        currentBox.add(lblCurrentTicket, BorderLayout.CENTER);

        JPanel nextHoldRow = new JPanel(new GridLayout(1, 2, 8, 0));
        nextHoldRow.setOpaque(false);
        JButton btnNext = createColoredButton("Next", new Color(46, 160, 90));
        JButton btnHold = createColoredButton("Hold", new Color(70, 130, 180));
        nextHoldRow.add(btnNext);
        nextHoldRow.add(btnHold);

        leftBlock.add(currentBox, BorderLayout.CENTER);
        leftBlock.add(nextHoldRow, BorderLayout.SOUTH);

        RoundedPanel nextTicketBox = new RoundedPanel(12, new Color(35, 42, 48));
        nextTicketBox.setLayout(new BorderLayout());
        nextTicketBox.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        nextTicketBox.setPreferredSize(new Dimension(140, 0));
        JLabel nextLbl = new JLabel("Next Ticket");
        nextLbl.setFont(AppFonts.regular(11));
        nextLbl.setForeground(new Color(180, 190, 195));
        lblNextTicket = new JLabel("---");
        lblNextTicket.setFont(AppFonts.bold(22));
        lblNextTicket.setForeground(Color.WHITE);
        nextTicketBox.add(nextLbl, BorderLayout.NORTH);
        nextTicketBox.add(lblNextTicket, BorderLayout.CENTER);

        JPanel actionButtons = new JPanel(new GridLayout(1, 3, 8, 0));
        actionButtons.setOpaque(false);
        JButton btnNoShow = createColoredButton("No Show", new Color(220, 70, 70));
        JButton btnTransfer = createColoredButton("Transfer", new Color(110, 90, 220));
        JButton btnRecall = createColoredButton("Recall", new Color(50, 170, 200));
        actionButtons.add(btnNoShow);
        actionButtons.add(btnTransfer);
        actionButtons.add(btnRecall);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 8);
        gbc.gridx = 0;
        gbc.weightx = 0.35;
        topRow.add(leftBlock, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.25;
        topRow.add(nextTicketBox, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0.40;
        gbc.insets = new Insets(0, 0, 0, 0);
        topRow.add(actionButtons, gbc);

        JPanel statsRow = new JPanel(new GridLayout(1, 6, 8, 0));
        statsRow.setOpaque(false);
        lblDone = createStatCard("Done", "0");
        lblRemaining = createStatCard("Remaining", "0");
        lblUpcoming = createStatCard("Upcoming", "0");
        lblSkipped = createStatCard("Skipped", "0");
        lblTransferred = createStatCard("Transferred", "0");
        lblOnHold = createStatCard("On-hold", "0");
        statsRow.add(lblDone.getParent());
        statsRow.add(lblRemaining.getParent());
        statsRow.add(lblUpcoming.getParent());
        statsRow.add(lblSkipped.getParent());
        statsRow.add(lblTransferred.getParent());
        statsRow.add(lblOnHold.getParent());

        panel.add(topRow, BorderLayout.CENTER);
        panel.add(statsRow, BorderLayout.SOUTH);

        btnNext.addActionListener(e -> callNextPassenger());
        btnHold.addActionListener(e -> updateCurrentStatus("ON_HOLD"));
        btnNoShow.addActionListener(e -> updateCurrentStatus("SKIPPED"));
        btnTransfer.addActionListener(e -> transferPassenger());
        btnRecall.addActionListener(e -> recallPassenger());

        return panel;
    }

    private JLabel createStatCard(String title, String value) {
        RoundedPanel card = new RoundedPanel(10, new Color(248, 250, 248));
        card.setLayout(new BorderLayout()); 
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1, MainFrame.SECONDARY_BTN_BG, 10),
                BorderFactory.createEmptyBorder(10, 8, 10, 8)
        ));
        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(AppFonts.regular(11));
        t.setForeground(new Color(100, 115, 105));
        JLabel v = new JLabel(value, SwingConstants.CENTER);
        v.setFont(AppFonts.bold(18));
        v.setForeground(new Color(27, 77, 46));
        card.add(t, BorderLayout.NORTH);
        card.add(v, BorderLayout.CENTER);
        return v;
    }

    private JButton createColoredButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(AppFonts.bold(13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(14, 12, 14, 12));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        return btn;
    }

    private void loadCurrentServingTicket() {
        int counter = selectedCounter;

        new SwingWorker<Map<String, String>, Void>() {
            @Override
            protected Map<String, String> doInBackground() throws Exception {
                return FirebaseHelper.getServingTicketForCounter(counter);
            }

            @Override
            protected void done() {
                try {
                    Map<String, String> t = get();
                    if (t != null) {
                        currentTicketNo = t.get("ticketNo");
                        updateLabels(t);
                    } else {
                        currentTicketNo = null;
                        clearDisplay();
                        autoCallFirstPassenger();
                    }
                    refreshNextTicket();
                    refreshStats();
                } catch (Exception ex) {
                    currentTicketNo = null;
                    clearDisplay();
                    ex.printStackTrace();
                }
            }
        }.execute();
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
                        refreshNextTicket();
                        refreshStats();
                        return;
                    }
                    int counter = selectedCounter;
                    String ticketNo = next.get("ticketNo");
                    FirebaseHelper.updateTicketStatus(ticketNo, "SERVING", counter);
                    currentTicketNo = ticketNo;
                    updateLabels(next);
                    refreshNextTicket();
                    refreshStats();
                } catch (Exception ex) {
                    clearDisplay();
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void callNextPassenger() {
        if (currentTicketNo != null) {
            updateCurrentStatus("COMPLETED");
            return;
        }

        int counter = selectedCounter;
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
                        showModernMessage("No more passengers waiting in the queue.", "Queue Empty", false);
                        clearDisplay();
                        refreshNextTicket();
                        refreshStats();
                        return;
                    }
                    String ticketNo = next.get("ticketNo");
                    FirebaseHelper.updateTicketStatus(ticketNo, "SERVING", counter);
                    currentTicketNo = ticketNo;
                    updateLabels(next);
                    refreshNextTicket();
                    refreshStats();
                } catch (Exception ex) {
                    showModernMessage("Failed to call next passenger.\n" + ex.getMessage(), "Error", true);
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void updateCurrentStatus(String newStatus) {
        if (currentTicketNo == null) {
            showModernMessage("No passenger is currently being served.", "Notice", false);
            return;
        }

        int counter = selectedCounter;
        String ticket = currentTicketNo;

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                FirebaseHelper.updateTicketStatus(ticket, newStatus, counter);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    currentTicketNo = null;
                    clearDisplay();
                    autoCallFirstPassenger();
                    refreshStats();
                } catch (Exception ex) {
                   showModernMessage("Failed to update status.\n" + ex.getMessage(), "Error", true);
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void transferPassenger() {
        if (currentTicketNo == null) {
            JOptionPane.showMessageDialog(this, "No passenger is currently being served.");
            return;
        }

        String[] options = {"1", "2", "3", "4"};
        String chosen = showModernChoice(
                "Transfer passenger to which counter?",
                "Transfer",
                new String[]{"1", "2", "3", "4"}
        );
        if (chosen == null) return;

        int targetCounter = Integer.parseInt(chosen);
        int currentCounter = selectedCounter;
        if (targetCounter == currentCounter) {
            showModernMessage("Passenger is already at this counter.", "Transfer", false);            return;
        }

        String ticket = currentTicketNo;

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                FirebaseHelper.updateTicketStatus(ticket, "WAITING", targetCounter);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    currentTicketNo = null;
                    clearDisplay();
                    autoCallFirstPassenger();
                    refreshStats();
                } catch (Exception ex) {
                    showModernMessage("Failed to transfer.\n" + ex.getMessage(), "Error", true);
                }
            }
        }.execute();
    }

    private JPanel createCounterSelector() {
        JPanel strip = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        strip.setOpaque(false);
        strip.setBorder(new RoundedOutlineBorder(1, MainFrame.SECONDARY_BTN_BG, 20) {
            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(3, 3, 3, 3);
            }
        });
        strip.setBackground(Color.WHITE);

        ButtonGroup group = new ButtonGroup();
        counterButtons = new JToggleButton[4];

        for (int i = 0; i < 4; i++) {
            final int counter = i + 1;
            JToggleButton btn = new JToggleButton(String.valueOf(counter));
            btn.setFont(AppFonts.bold(13));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setPreferredSize(new Dimension(44, 32));
            btn.setBorder(BorderFactory.createEmptyBorder());
            btn.setContentAreaFilled(false);
            btn.setOpaque(true);

            // default look
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(27, 77, 46));

            btn.addItemListener(e -> {
                if (btn.isSelected()) {
                    btn.setBackground(new Color(46, 160, 90));
                    btn.setForeground(Color.WHITE);
                    selectedCounter = counter;
                    currentTicketNo = null;
                    updateOnlineStatus();
                    loadCurrentServingTicket();
                } else {
                    btn.setBackground(Color.WHITE);
                    btn.setForeground(new Color(27, 77, 46));
                }
            });

            group.add(btn);
            counterButtons[i] = btn;
            strip.add(btn);
        }

        // select counter 1 by default
        counterButtons[0].setSelected(true);

        return strip;
    }

    private void showModernMessage(String message, String title, boolean isError) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());

        RoundedPanel card = new RoundedPanel(16, Color.WHITE);
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1, MainFrame.SECONDARY_BTN_BG, 16),
                BorderFactory.createEmptyBorder(24, 28, 24, 28)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppFonts.bold(16));
        lblTitle.setForeground(isError ? new Color(180, 50, 50) : new Color(27, 77, 46));

        JLabel lblMsg = new JLabel("<html><body style='width:260px'>" + message.replace("\n", "<br>") + "</body></html>");
        lblMsg.setFont(AppFonts.regular(13));
        lblMsg.setForeground(new Color(50, 65, 55));

        JButton ok = createColoredButton("OK", isError ? new Color(180, 50, 50) : new Color(46, 160, 90));
        ok.setPreferredSize(new Dimension(100, 36));
        ok.addActionListener(e -> dialog.dispose());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(ok);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblMsg, BorderLayout.CENTER);
        card.add(btnRow, BorderLayout.SOUTH);

        dialog.add(card);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private String showModernChoice(String message, String title, String[] choices) {
        final String[] result = {null};

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());

        JPanel card = new JPanel(new BorderLayout(0, 16));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1, MainFrame.SECONDARY_BTN_BG, 16),
                BorderFactory.createEmptyBorder(24, 28, 24, 28)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppFonts.bold(16));
        lblTitle.setForeground(new Color(27, 77, 46));

        JLabel lblMsg = new JLabel(message);
        lblMsg.setFont(AppFonts.regular(13));
        lblMsg.setForeground(new Color(50, 65, 55));

        JComboBox<String> combo = new JComboBox<>(choices);
        styleModernCombo(combo);
        combo.setPreferredSize(new Dimension(320, 36));

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.add(lblMsg);
        center.add(Box.createVerticalStrut(12));
        center.add(combo);

        JButton cancel = createColoredButton("Cancel", new Color(120, 130, 125));
        cancel.setPreferredSize(new Dimension(100, 36));
        cancel.addActionListener(e -> dialog.dispose());

        JButton ok = createColoredButton("Recall", new Color(50, 170, 200));
        ok.setPreferredSize(new Dimension(100, 36));
        ok.addActionListener(e -> {
            result[0] = (String) combo.getSelectedItem();
            dialog.dispose();
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.add(cancel);
        btnRow.add(ok);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(btnRow, BorderLayout.SOUTH);

        dialog.add(card);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        return result[0];
    }

    private void styleModernCombo(JComboBox<?> combo) {
        combo.setFont(AppFonts.bold(13));
        combo.setBackground(Color.WHITE);
        combo.setForeground(new Color(27, 77, 46));
        combo.setFocusable(false);
        combo.setMaximumRowCount(6);

        combo.setBorder(new RoundedOutlineBorder(1, MainFrame.SECONDARY_BTN_BG, 10) {
            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(4, 10, 4, 10);  
            }
        });

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                        int index, boolean isSelected,
                                                        boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);

                label.setFont(AppFonts.regular(13));
                label.setOpaque(true);
                label.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

                if (isSelected) {
                    label.setBackground(new Color(46, 160, 90));
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(new Color(27, 77, 46));
                }
                return label;
            }
        });
    }

    private void recallPassenger() {
    new SwingWorker<List<Map<String, String>>, Void>() {
        @Override
        protected List<Map<String, String>> doInBackground() throws Exception {
            List<Map<String, String>> list = new ArrayList<>();
            list.addAll(FirebaseHelper.getTicketsByStatus("SKIPPED"));
            list.addAll(FirebaseHelper.getTicketsByStatus("ON_HOLD"));
            return list;
        }

        @Override
        protected void done() {
            try {
                List<Map<String, String>> candidates = get();
                if (candidates == null || candidates.isEmpty()) {
                    showModernMessage("No more passengers to recall.", "Recall", false);
                    return;
                }

                String[] choices = candidates.stream()
                        .map(t -> t.get("ticketNo") + " – " + t.get("name") + " (" + t.get("status") + ")")
                        .toArray(String[]::new);

                String selected = showModernChoice(
                        "Select ticket to recall:",
                        "Recall",
                        choices
                );
                if (selected == null) return;

                String ticketNo = selected.split(" – ")[0].trim();
                int counter = selectedCounter;

                if (currentTicketNo != null) {
                    FirebaseHelper.updateTicketStatus(currentTicketNo, "COMPLETED", counter);
                }

                FirebaseHelper.updateTicketStatus(ticketNo, "SERVING", counter);
                currentTicketNo = ticketNo;

                Map<String, String> t = FirebaseHelper.getTicketByNumber(ticketNo);
                if (t != null) updateLabels(t);
                refreshNextTicket();
                refreshStats();
            } catch (Exception ex) {
                showModernMessage("Failed to recall.\n" + ex.getMessage(), "Error", true);
            }
        }
    }.execute();
    }

    private void refreshNextTicket() {
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
                    lblNextTicket.setText(next != null ? next.get("ticketNo") : "---");
                } catch (Exception ex) {
                    lblNextTicket.setText("---");
                }
            }
        }.execute();
    }

    private void refreshStats() {
        new SwingWorker<int[], Void>() {
            @Override
            protected int[] doInBackground() throws Exception {
                return new int[]{
                        FirebaseHelper.countByStatus("COMPLETED"),
                        FirebaseHelper.countByStatus("WAITING"),
                        FirebaseHelper.countByStatus("WAITING"), 
                        FirebaseHelper.countByStatus("SKIPPED"),
                        FirebaseHelper.countByStatus("TRANSFERRED"),
                        FirebaseHelper.countByStatus("ON_HOLD")
                };
            }

            @Override
            protected void done() {
                try {
                    int[] c = get();
                    lblDone.setText(String.valueOf(c[0]));
                    lblRemaining.setText(String.valueOf(c[1]));
                    lblUpcoming.setText(String.valueOf(c[2]));
                    lblSkipped.setText(String.valueOf(c[3]));
                    lblTransferred.setText(String.valueOf(c[4]));
                    lblOnHold.setText(String.valueOf(c[5]));
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void updateLabels(Map<String, String> t) {
        String ticket = t.get("ticketNo");
        lblServingTicket.setText(ticket != null ? ticket : "---");
        lblCurrentTicket.setText(ticket != null ? ticket : "---");
        lblPassengerName.setText("Name: " + nullToEmpty(t.get("name")));
        lblFlight.setText("Flight: " + nullToEmpty(t.get("flightNo")));
        lblBaggage.setText("Baggage: " + nullToEmpty(t.get("baggage")));
    }

    private void clearDisplay() {
        lblServingTicket.setText("---");
        lblCurrentTicket.setText("---");
        lblPassengerName.setText("Name: ");
        lblFlight.setText("Flight: ");
        lblBaggage.setText("Baggage: ");
        lblWaited.setText("00m 00s Waited");
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private void updateOnlineStatus() {
        String username = mainFrame.getCurrentUsername();
        String role = mainFrame.getCurrentRole();
        if (username == null || username.isEmpty() || role == null) return;
        int counter = selectedCounter;
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

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private Color bg;

        public RoundedPanel(int radius, Color bg) {
            this.radius = radius;
            this.bg = bg;
            setOpaque(false);
        }

        public void setPanelBackground(Color bg) {
            this.bg = bg;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedOutlineBorder extends AbstractBorder {
        private final int thickness;
        private final Color color;
        private final int radius;

        public RoundedOutlineBorder(int thickness, Color color, int radius) {
            this.thickness = thickness;
            this.color = color;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.draw(new RoundRectangle2D.Float(
                    x + thickness / 2f,
                    y + thickness / 2f,
                    width - thickness,
                    height - thickness,
                    radius, radius));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(8, 14, 8, 14);
        }
    }
}