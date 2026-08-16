import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class CheckInPanel extends JPanel {

    private JTextField txtBookingRef, txtName, txtFlight, txtBaggage;
    private final MainFrame mainFrame;
    private BufferedImage backgroundImage;

    public CheckInPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        try {
            // Try both common resource locations
            java.net.URL url = getClass().getResource("/resources/bg_passenger.jpg");
            if (url == null) {
                url = getClass().getResource("resources/bg_passenger.jpg");
            }
            if (url != null) {
                backgroundImage = javax.imageio.ImageIO.read(url);
            } else {
                System.err.println("Background image not found: /resources/bg_passenger.jpg");
            }
        } catch (Exception e) {
            System.err.println("Could not load background image: " + e.getMessage());
        }

        setLayout(new BorderLayout());
        setOpaque(false); 

        //  Top bar 
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        topBar.setOpaque(false);

        JButton backButton = createBackButton("/resources/back.png");
        backButton.addActionListener(e -> mainFrame.showPanel("PassengerMenu"));
        topBar.add(backButton);
        add(topBar, BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        GlassPanel formCard = new GlassPanel();
        formCard.setLayout(new GridBagLayout());
        formCard.setBorder(new EmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel lblTitle = new JLabel("Passenger Check-In", SwingConstants.CENTER);
        lblTitle.setFont(AppFonts.bold(22));
        lblTitle.setForeground(MainFrame.NAV_BTN_BG);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 10, 25, 10);
        formCard.add(lblTitle, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 10, 10, 10);

        txtBookingRef = createStyledField();
        txtName       = createStyledField();
        txtFlight     = createStyledField();
        txtBaggage    = createStyledField();

        addFormRow(formCard, "Booking Reference:", txtBookingRef, gbc, 1);
        addFormRow(formCard, "Passenger Name:",    txtName,       gbc, 2);
        addFormRow(formCard, "Flight Number:",     txtFlight,     gbc, 3);
        addFormRow(formCard, "Baggage Details:",   txtBaggage,    gbc, 4);

        JButton btnSubmit = new JButton("Register & Issue Queue Ticket");
        btnSubmit.setFont(AppFonts.bold(14));
        btnSubmit.setBackground(MainFrame.SECONDARY_BTN_BG);
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setFocusPainted(false);
        btnSubmit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSubmit.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));
        btnSubmit.setContentAreaFilled(true);
        btnSubmit.setOpaque(true);

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
        gbc.insets = new Insets(28, 10, 5, 10);
        formCard.add(btnSubmit, gbc);

        centerWrapper.add(formCard);
        add(centerWrapper, BorderLayout.CENTER);

        btnSubmit.addActionListener(e -> processCheckIn());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            int panelW = getWidth();
            int panelH = getHeight();
            int imgW = backgroundImage.getWidth();
            int imgH = backgroundImage.getHeight();

            double scale = Math.max((double) panelW / imgW, (double) panelH / imgH);
            int drawW = (int) (imgW * scale);
            int drawH = (int) (imgH * scale);
            int x = (panelW - drawW) / 2;
            int y = (panelH - drawH) / 2;

            g.drawImage(backgroundImage, x, y, drawW, drawH, this);
        } else {
            g.setColor(MainFrame.MAIN_BG);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private JButton createBackButton(String iconPath) {
        JButton btn = new JButton();

        try {
            ImageIcon original = new ImageIcon(getClass().getResource(iconPath));
            Image scaled = original.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(scaled));
        } catch (Exception e) {
            System.err.println("Could not load icon: " + iconPath);
        }

        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(MainFrame.NAV_BTN_BG);
                btn.setContentAreaFilled(true);
                btn.setOpaque(true);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setContentAreaFilled(false);
                btn.setOpaque(false);
            }
        });

        return btn;
    }

    private JTextField createStyledField() {
        JTextField field = new JTextField(20);
        field.setFont(AppFonts.regular(13));
        field.setOpaque(true);
        field.setBackground(new Color(255, 255, 255, 200));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 200, 185, 180), 1),
            BorderFactory.createEmptyBorder(9, 12, 9, 12)
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

    private void processCheckIn() {
        String ref     = txtBookingRef.getText().trim();
        String name    = txtName.getText().trim();
        String flight  = txtFlight.getText().trim();
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
            "Queue Ticket Generated",
            JOptionPane.INFORMATION_MESSAGE);

        txtBookingRef.setText("");
        txtName.setText("");
        txtFlight.setText("");
        txtBaggage.setText("");
    }

    private static class GlassPanel extends JPanel {

        public GlassPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 28;

            // Soft shadow
            g2.setColor(new Color(0, 0, 0, 40));
            g2.fillRoundRect(4, 6, w - 8, h - 8, arc, arc);

            // Glass fill (semi-transparent white)
            g2.setColor(new Color(255, 255, 255, 110));
            g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);

            // Subtle top highlight
            g2.setColor(new Color(255, 255, 255, 80));
            g2.fillRoundRect(0, 0, w - 1, h / 3, arc, arc);

            // Border
            g2.setColor(new Color(255, 255, 255, 180));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}