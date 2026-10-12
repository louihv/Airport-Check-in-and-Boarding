import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class PassengerDashboardPanel extends JPanel {

    private static final Color TEXT_DARK = new Color(40, 55, 50);
    private static final Color TEXT_MUTED = new Color(110, 125, 120);
    private static final Color FIELD_BORDER = new Color(200, 208, 205);
    private static final Color ACCENT_RED = new Color(200, 30, 45);

    private final MainFrame mainFrame;
    private BufferedImage heroImage;
    private final JLabel lblWelcome = new JLabel("Welcome, Passenger");

    private JRadioButton rbRound, rbOneWay;
    private JTextField txtFrom, txtTo, txtDepart, txtReturn;
    private JSpinner spnAdults, spnChildren;
    private JComboBox<String> cbCabin;
    private final CardLayout tabCards = new CardLayout();
    private final JPanel tabBody = new JPanel(tabCards);
    private final java.util.List<JButton> tabButtons = new java.util.ArrayList<>();

    public PassengerDashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        loadHero();

        setLayout(new BorderLayout());
        setBackground(new Color(244, 247, 246));

        add(buildNavBar(), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(buildHero());
        content.add(buildQuickServices());
        content.add(buildPromos());
        content.add(buildFooter());

        JScrollPane scroll = new JScrollPane(content,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        add(scroll, BorderLayout.CENTER);
    }

    public void setPassengerName(String fullName) {
        lblWelcome.setText("Welcome, " + (fullName == null || fullName.isBlank() ? "Passenger" : fullName));
    }

    private JComponent buildNavBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, MainFrame.NAV_BTN_BG),
                new EmptyBorder(8, 24, 8, 24)));

        JLabel logo = new JLabel();
        try {
            java.net.URL u = getClass().getResource("/resources/logo.png");
            if (u != null) {
                Image img = new ImageIcon(u).getImage().getScaledInstance(90, 45, Image.SCALE_SMOOTH);
                logo.setIcon(new ImageIcon(img));
            }
        } catch (Exception ignored) { }
        bar.add(logo, BorderLayout.WEST);

        JPanel links = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 8));
        links.setOpaque(false);
        links.add(navLink("Book", () -> selectTab(0)));
        links.add(navLink("Manage Booking", () -> selectTab(1)));
        links.add(navLink("Check-in", () -> mainFrame.showPanel("CheckInPassenger")));
        links.add(navLink("Flight Status", () -> selectTab(3)));
        links.add(navLink("Travel Info", () -> info("Travel Info",
                "Baggage allowance, travel requirements and airport guides will appear here.")));
        links.add(navLink("Mabuhay Miles", () -> info("Mabuhay Miles",
                "Your loyalty program details will appear here.")));
        bar.add(links, BorderLayout.CENTER);

        // right: user + logout
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        right.setOpaque(false);
        lblWelcome.setFont(AppFonts.regular(13));
        lblWelcome.setForeground(MainFrame.NAV_BTN_BG);
        right.add(lblWelcome);

        JButton logout = pillButton("Log out", MainFrame.NAV_BTN_BG, Color.WHITE, 12);
        logout.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(this, "Log out of your account?",
                    "Log out", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) mainFrame.showPanel("PassengerMenu");
        });
        right.add(logout);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JButton navLink(String text, Runnable action) {
        JButton b = new JButton(text);
        b.setFont(AppFonts.bold(13));
        b.setForeground(TEXT_DARK);
        b.setContentAreaFilled(false);
        b.setBorder(new EmptyBorder(8, 12, 8, 12));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setForeground(MainFrame.SECONDARY_BTN_BG); }
            public void mouseExited(java.awt.event.MouseEvent e) { b.setForeground(TEXT_DARK); }
        });
        b.addActionListener(e -> action.run());
        return b;
    }

    // --------------------------------------------------------------------- hero
    private JComponent buildHero() {
        JPanel hero = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                int w = getWidth(), h = getHeight();
                if (heroImage != null) {
                    double s = Math.max((double) w / heroImage.getWidth(), (double) h / heroImage.getHeight());
                    int dw = (int) (heroImage.getWidth() * s), dh = (int) (heroImage.getHeight() * s);
                    g2.drawImage(heroImage, (w - dw) / 2, (h - dh) / 2, dw, dh, null);
                } else {
                    g2.setPaint(new GradientPaint(0, 0, MainFrame.NAV_BTN_BG, w, h, MainFrame.SECONDARY_BTN_BG));
                    g2.fillRect(0, 0, w, h);
                }
                g2.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 40), 0, h, new Color(0, 0, 0, 150)));
                g2.fillRect(0, 0, w, h);
                g2.dispose();
            }
        };
        hero.setPreferredSize(new Dimension(1000, 520));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 520));
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));

        JLabel headline = new JLabel("Where would you like to go?");
        headline.setFont(AppFonts.bold(32));
        headline.setForeground(Color.WHITE);
        headline.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sub = new JLabel("Book flights, manage trips and check in, all in one place.");
        sub.setFont(AppFonts.regular(14));
        sub.setForeground(new Color(235, 240, 238));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        stack.add(headline);
        stack.add(Box.createVerticalStrut(6));
        stack.add(sub);
        stack.add(Box.createVerticalStrut(22));
        stack.add(buildBookingCard());

        hero.add(stack);
        return hero;
    }

    private JComponent buildBookingCard() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fill(new RoundRectangle2D.Float(3, 5, getWidth() - 6, getHeight() - 6, 22, 22));
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 4, 22, 22));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(8, 8, 12, 8));
        card.setPreferredSize(new Dimension(860, 270));
        card.setMaximumSize(new Dimension(860, 270));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        // tabs
        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        tabs.setOpaque(false);
        String[] names = {"Book a Flight", "Manage Booking", "Check-in", "Flight Status"};
        for (int i = 0; i < names.length; i++) {
            final int idx = i;
            JButton t = new JButton(names[i]);
            t.setFont(AppFonts.bold(13));
            t.setFocusPainted(false);
            t.setContentAreaFilled(false);
            t.setBorder(new EmptyBorder(10, 16, 10, 16));
            t.setCursor(new Cursor(Cursor.HAND_CURSOR));
            t.addActionListener(e -> {
                if (idx == 2) { mainFrame.showPanel("CheckInPassenger"); return; }
                selectTab(idx);
            });
            tabButtons.add(t);
            tabs.add(t);
        }
        card.add(tabs, BorderLayout.NORTH);

        tabBody.setOpaque(false);
        tabBody.add(buildBookTab(), "0");
        tabBody.add(buildManageTab(), "1");
        tabBody.add(new JPanel(), "2");
        tabBody.add(buildStatusTab(), "3");
        card.add(tabBody, BorderLayout.CENTER);

        selectTab(0);
        return card;
    }

    private void selectTab(int i) {
        tabCards.show(tabBody, String.valueOf(i));
        for (int k = 0; k < tabButtons.size(); k++) {
            JButton b = tabButtons.get(k);
            boolean on = k == i;
            b.setForeground(on ? MainFrame.NAV_BTN_BG : TEXT_MUTED);
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 3, 0, on ? ACCENT_RED : new Color(0, 0, 0, 0)),
                    new EmptyBorder(10, 16, 7, 16)));
        }
    }

    private JComponent buildBookTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(14, 14, 4, 14));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 6, 4, 6);
        c.weightx = 1;

        // trip type
        rbRound = new JRadioButton("Round trip", true);
        rbOneWay = new JRadioButton("One way");
        for (JRadioButton rb : new JRadioButton[]{rbRound, rbOneWay}) {
            rb.setOpaque(false);
            rb.setFont(AppFonts.regular(13));
            rb.setForeground(TEXT_DARK);
            rb.setFocusPainted(false);
        }
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbRound); bg.add(rbOneWay);
        JPanel trip = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        trip.setOpaque(false);
        trip.add(rbRound); trip.add(rbOneWay);
        c.gridx = 0; c.gridy = 0; c.gridwidth = 4;
        p.add(trip, c);

        txtFrom = field("Manila (MNL)");
        txtTo = field("Cebu (CEB)");
        txtDepart = field(LocalDate.now().plusDays(7).toString());
        txtReturn = field(LocalDate.now().plusDays(14).toString());
        rbOneWay.addActionListener(e -> txtReturn.setEnabled(false));
        rbRound.addActionListener(e -> txtReturn.setEnabled(true));

        c.gridwidth = 1;
        c.gridy = 1;
        c.gridx = 0; p.add(labeled("From", txtFrom), c);
        c.gridx = 1; p.add(labeled("To", txtTo), c);
        c.gridx = 2; p.add(labeled("Depart", txtDepart), c);
        c.gridx = 3; p.add(labeled("Return", txtReturn), c);

        spnAdults = new JSpinner(new SpinnerNumberModel(1, 1, 9, 1));
        spnChildren = new JSpinner(new SpinnerNumberModel(0, 0, 8, 1));
        cbCabin = new JComboBox<>(new String[]{"Economy", "Premium Economy", "Business"});
        cbCabin.setFont(AppFonts.regular(13));

        c.gridy = 2;
        c.gridx = 0; p.add(labeled("Adults (12+)", spnAdults), c);
        c.gridx = 1; p.add(labeled("Children (2-11)", spnChildren), c);
        c.gridx = 2; p.add(labeled("Cabin", cbCabin), c);

        JButton search = pillButton("Search Flights", ACCENT_RED, Color.WHITE, 14);
        search.addActionListener(e -> searchFlights());
        c.gridx = 3; c.insets = new Insets(18, 6, 4, 6);
        p.add(search, c);
        return p;
    }

    private JComponent buildManageTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 14, 4, 14));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 6, 4, 6);
        c.weightx = 1;
        JTextField ref = field("");
        JTextField last = field("");
        c.gridy = 0; c.gridx = 0; p.add(labeled("Booking reference", ref), c);
        c.gridx = 1; p.add(labeled("Last name", last), c);
        JButton go = pillButton("Retrieve Booking", ACCENT_RED, Color.WHITE, 14);
        go.addActionListener(e -> info("Manage Booking",
                ref.getText().isBlank() || last.getText().isBlank()
                        ? "Please enter your booking reference and last name."
                        : "Looking up booking " + ref.getText().trim().toUpperCase() + "..."));
        c.gridx = 2; c.insets = new Insets(18, 6, 4, 6); p.add(go, c);
        return p;
    }

    private JComponent buildStatusTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 14, 4, 14));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 6, 4, 6);
        c.weightx = 1;
        JTextField no = field("PR 101");
        JTextField date = field(LocalDate.now().toString());
        c.gridy = 0; c.gridx = 0; p.add(labeled("Flight number", no), c);
        c.gridx = 1; p.add(labeled("Date", date), c);
        JButton go = pillButton("Check Status", ACCENT_RED, Color.WHITE, 14);
        go.addActionListener(e -> info("Flight Status",
                "Status lookup for " + no.getText().trim() + " on " + date.getText().trim() + " is not connected yet."));
        c.gridx = 2; c.insets = new Insets(18, 6, 4, 6); p.add(go, c);
        return p;
    }

    private void searchFlights() {
        String from = txtFrom.getText().trim(), to = txtTo.getText().trim();
        if (from.isEmpty() || to.isEmpty()) {
            info("Missing Information", "Please enter both origin and destination.");
            return;
        }
        if (from.equalsIgnoreCase(to)) {
            info("Invalid Route", "Origin and destination must be different.");
            return;
        }
        // TODO: pass these values to your flight-search / booking panel
        String msg = from + " -> " + to
                + "\nDepart: " + txtDepart.getText().trim()
                + (rbRound.isSelected() ? "\nReturn: " + txtReturn.getText().trim() : "")
                + "\nPassengers: " + spnAdults.getValue() + " adult(s), " + spnChildren.getValue() + " child(ren)"
                + "\nCabin: " + cbCabin.getSelectedItem();
        info("Search Flights", msg);
    }

    // ----------------------------------------------------------- quick services
    private JComponent buildQuickServices() {
        JPanel wrap = section("Quick services");
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.add(serviceTile("Online Check-in", "Skip the queue and get your boarding pass.",
                () -> mainFrame.showPanel("CheckInPassenger")));
        row.add(serviceTile("My Trips", "View upcoming and past flights.",
                () -> info("My Trips", "Your trips will be listed here.")));
        row.add(serviceTile("Extra Baggage", "Add baggage before you fly.",
                () -> info("Extra Baggage", "Baggage add-ons will appear here.")));
        row.add(serviceTile("Seat Selection", "Pick the seat you prefer.",
                () -> info("Seat Selection", "Seat maps will appear here.")));
        wrap.add(row, BorderLayout.CENTER);
        return wrap;
    }

    private JComponent serviceTile(String title, String desc, Runnable action) {
        JPanel t = new JPanel(new BorderLayout(0, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.setColor(new Color(220, 228, 225));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
            }
        };
        t.setOpaque(false);
        t.setBorder(new EmptyBorder(18, 18, 18, 18));
        t.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JLabel a = new JLabel(title);
        a.setFont(AppFonts.bold(15));
        a.setForeground(MainFrame.NAV_BTN_BG);
        JLabel b = new JLabel("<html><body style='width:160px'>" + desc + "</body></html>");
        b.setFont(AppFonts.regular(12));
        b.setForeground(TEXT_MUTED);
        t.add(a, BorderLayout.NORTH);
        t.add(b, BorderLayout.CENTER);
        t.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) { action.run(); }
        });
        return t;
    }

    // ------------------------------------------------------------------- promos
    private JComponent buildPromos() {
        JPanel wrap = section("Featured destinations");
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);
        row.add(promoCard("Manila to Cebu", "from PHP 1,999", new Color(0, 105, 148), new Color(0, 160, 175)));
        row.add(promoCard("Manila to Davao", "from PHP 2,499", new Color(30, 90, 70), new Color(80, 160, 110)));
        row.add(promoCard("Manila to Tokyo", "from PHP 8,999", new Color(120, 40, 70), new Color(200, 90, 100)));
        wrap.add(row, BorderLayout.CENTER);
        return wrap;
    }

    private JComponent promoCard(String route, String price, Color c1, Color c2) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(280, 150));
        p.setBorder(new EmptyBorder(18, 20, 18, 20));
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JLabel r = new JLabel(route);
        r.setFont(AppFonts.bold(18));
        r.setForeground(Color.WHITE);
        JLabel pr = new JLabel(price);
        pr.setFont(AppFonts.regular(13));
        pr.setForeground(new Color(255, 255, 255, 220));
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(pr, BorderLayout.WEST);
        p.add(r, BorderLayout.NORTH);
        p.add(south, BorderLayout.SOUTH);
        p.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                String[] parts = route.split(" to ");
                txtFrom.setText(parts[0]);
                txtTo.setText(parts[1]);
                selectTab(0);
            }
        });
        return p;
    }

    private JComponent buildFooter() {
        JPanel f = new JPanel(new FlowLayout(FlowLayout.CENTER));
        f.setBackground(MainFrame.NAV_BTN_BG);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = new JLabel("Need help? Contact customer service 24/7.");
        l.setFont(AppFonts.regular(12));
        l.setForeground(Color.WHITE);
        f.add(l);
        return f;
    }

    // ------------------------------------------------------------------ helpers
    private JPanel section(String title) {
        JPanel s = new JPanel(new BorderLayout(0, 12));
        s.setOpaque(false);
        s.setBorder(new EmptyBorder(28, 60, 12, 60));
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        JLabel h = new JLabel(title);
        h.setFont(AppFonts.bold(20));
        h.setForeground(MainFrame.NAV_BTN_BG);
        s.add(h, BorderLayout.NORTH);
        return s;
    }

    private JPanel labeled(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(AppFonts.regular(11));
        l.setForeground(TEXT_MUTED);
        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private JTextField field(String text) {
        JTextField f = new JTextField(text, 12);
        f.setFont(AppFonts.regular(13));
        f.setForeground(TEXT_DARK);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER),
                new EmptyBorder(7, 10, 7, 10)));
        return f;
    }

    private JButton pillButton(String text, Color bg, Color fg, int size) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.darker() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(AppFonts.bold(size));
        b.setForeground(fg);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 22, 10, 22));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setRolloverEnabled(true);
        return b;
    }

    private void info(String title, String msg) {
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void loadHero() {
        try {
            java.net.URL url = getClass().getResource("/resources/bg_passenger.jpg");
            if (url == null) url = getClass().getResource("resources/bg_passenger.jpg");
            if (url != null) heroImage = javax.imageio.ImageIO.read(url);
        } catch (Exception e) {
            System.err.println("Could not load hero image: " + e.getMessage());
        }
    }
}