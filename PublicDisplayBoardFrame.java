import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class PublicDisplayBoardFrame extends JFrame {
    private JLabel lblCurrentTicket, lblCurrentCounter;
    private DefaultTableModel activeCountersModel;
    private Timer autoRefreshTimer;

    public PublicDisplayBoardFrame() {
        setTitle("Airport Waiting Area - Queue Display");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(MainFrame.MAIN_BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel announceCard = createRoundedCard();
        announceCard.setLayout(new BorderLayout(0, 12));
        announceCard.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel lblAnnounce = new JLabel("NOW SERVING", SwingConstants.CENTER);
        lblAnnounce.setFont(AppFonts.bold(22));
        lblAnnounce.setForeground(new Color(120, 130, 145));

        lblCurrentTicket = new JLabel("---", SwingConstants.CENTER);
        lblCurrentTicket.setFont(AppFonts.bold(64));
        lblCurrentTicket.setForeground(MainFrame.NAV_BTN_BG);

        lblCurrentCounter = new JLabel("Please proceed to your assigned counter", SwingConstants.CENTER);
        lblCurrentCounter.setFont(AppFonts.bold(16));
        lblCurrentCounter.setForeground(new Color(80, 90, 100));

        announceCard.add(lblAnnounce, BorderLayout.NORTH);
        announceCard.add(lblCurrentTicket, BorderLayout.CENTER);
        announceCard.add(lblCurrentCounter, BorderLayout.SOUTH);

        String[] cols = {"Counter", "Serving Ticket", "Status"};
        activeCountersModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(activeCountersModel);
        styleTable(table);
        table.setTableHeader(null);

        JPanel tableCard = createRoundedCard();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel headerPanel = new JPanel(new GridLayout(1, cols.length, 0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(MainFrame.SECONDARY_BTN_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 18, 18));
                g2.dispose();
            }
        };
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        headerPanel.setPreferredSize(new Dimension(0, 46));

        for (String col : cols) {
            JLabel lbl = new JLabel(col);
            lbl.setForeground(Color.WHITE);
            lbl.setFont(AppFonts.bold(13));
            lbl.setHorizontalAlignment(SwingConstants.LEFT);
            headerPanel.add(lbl);
        }

        JPanel bodyCard = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
            }
        };
        bodyCard.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setOpaque(false);
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        bodyCard.add(scrollPane, BorderLayout.CENTER);

        tableCard.add(headerPanel, BorderLayout.NORTH);
        tableCard.add(bodyCard, BorderLayout.CENTER);

        mainPanel.add(announceCard, BorderLayout.NORTH);
        mainPanel.add(tableCard, BorderLayout.CENTER);

        add(mainPanel);

        autoRefreshTimer = new Timer(5000, e -> refreshDisplay());
        autoRefreshTimer.start();

        refreshDisplay();
    }

    private JPanel createRoundedCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 18, 18));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        return card;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(44);
        table.setFont(AppFonts.regular(15));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(230, 233, 238));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(245, 248, 250));
        table.setSelectionForeground(new Color(40, 40, 40));
        table.setBackground(Color.WHITE);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));
                setHorizontalAlignment(JLabel.LEFT);

                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 251, 252));
                }

                if (column == 2 && value != null) {
                    String status = value.toString();
                    if ("BUSY".equalsIgnoreCase(status)) {
                        setForeground(new Color(245, 166, 35));
                    } else if ("OPEN".equalsIgnoreCase(status) || "AVAILABLE".equalsIgnoreCase(status)) {
                        setForeground(new Color(45, 190, 180));
                    } else {
                        setForeground(new Color(40, 40, 40));
                    }
                } else {
                    setForeground(new Color(40, 40, 40));
                }
                return c;
            }
        });
    }

    private static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(180, 190, 200);
            this.trackColor = new Color(245, 247, 250);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            btn.setMinimumSize(new Dimension(0, 0));
            btn.setMaximumSize(new Dimension(0, 0));
            return btn;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(trackColor);
            g2.fillRoundRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height, 8, 8);
            g2.dispose();
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(thumbBounds.x + 1, thumbBounds.y + 1, thumbBounds.width - 2, thumbBounds.height - 2, 6, 6);
            g2.dispose();
        }
    }

    private void refreshDisplay() {
        new SwingWorker<Void, Void>() {
            private String latestTicket = "---";
            private String latestMessage = "Please proceed to your assigned counter";
            private final DefaultTableModel tempModel = new DefaultTableModel(
                    new String[]{"Counter", "Serving Ticket", "Status"}, 0);

            @Override
            protected Void doInBackground() throws Exception {
                List<Object[]> users = FirebaseHelper.getAllUsers();
                Map<String, String> counterToTicket = new HashMap<>(); 

                List<Map<String, String>> tickets = FirebaseHelper.getAllTickets();
                for (Map<String, String> t : tickets) {
                    String status = t.get("status");
                    String counter = t.get("counter");
                    String ticketNo = t.get("ticketNo");

                    if (counter != null && !counter.equals("0") && !counter.isEmpty()
                            && status != null
                            && !status.equalsIgnoreCase("COMPLETED")
                            && !status.equalsIgnoreCase("SERVED")) {

                        counterToTicket.put(counter, ticketNo);
                    }
                }

                if (users != null) {
                    for (Object[] u : users) {
                        String role    = String.valueOf(u[1]);
                        String counter = String.valueOf(u[2]);
                        String status  = String.valueOf(u[3]);

                        if (!"STAFF".equalsIgnoreCase(role)) continue;
                        if (!"Online".equalsIgnoreCase(status)) continue;
                        if (counter == null || counter.equals("-") || counter.isEmpty()) continue;

                        String servingTicket = counterToTicket.getOrDefault(counter, "—");
                        String displayStatus = servingTicket.equals("—") ? "OPEN" : "BUSY";

                        tempModel.addRow(new Object[]{
                                "Counter " + counter,
                                servingTicket,
                                displayStatus
                        });

                        if (!servingTicket.equals("—") && latestTicket.equals("---")) {
                            latestTicket = servingTicket;
                            latestMessage = "Ticket " + servingTicket + " → Proceed to Counter " + counter;
                        }
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); 

                    activeCountersModel.setRowCount(0);
                    for (int i = 0; i < tempModel.getRowCount(); i++) {
                        activeCountersModel.addRow(new Object[]{
                                tempModel.getValueAt(i, 0),
                                tempModel.getValueAt(i, 1),
                                tempModel.getValueAt(i, 2)
                        });
                    }

                    lblCurrentTicket.setText(latestTicket);
                    lblCurrentCounter.setText(latestMessage);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    @Override
    public void dispose() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.stop();
        }
        super.dispose();
    }
}