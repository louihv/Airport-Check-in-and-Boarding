import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class CheckInConfirmPanel extends JPanel {

    private static final Color TEXT_COLOR = new Color(40, 55, 50);

    private static CheckInConfirmPanel instance;

    private final MainFrame mainFrame;
    private BufferedImage backgroundImage;
    private final JPanel body;

    public static CheckInConfirmPanel getInstance() {
        return instance;
    }

    private String bookingRef = "";
    private String passengerName = "";
    private String flight = "";
    private String cabin = "";

    public CheckInConfirmPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        instance = this;

        try {
            java.net.URL url = getClass().getResource("/resources/bg_passenger.jpg");
            if (url == null) {
                url = getClass().getResource("resources/bg_passenger.jpg");
            }
            if (url != null) {
                backgroundImage = javax.imageio.ImageIO.read(url);
            }
        } catch (Exception e) {
            System.err.println("Could not load background image: " + e.getMessage());
        }

        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        topBar.setOpaque(false);
        JButton backButton = createBackButton("/resources/back.png");
        backButton.addActionListener(e -> mainFrame.showPanel("CheckIn"));
        topBar.add(backButton);
        add(topBar, BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        GlassPanel card = new GlassPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(28, 45, 32, 45));

        body = new JPanel(new GridBagLayout());
        body.setOpaque(false);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        card.add(scroll, BorderLayout.CENTER);
        card.setPreferredSize(new Dimension(700, 620));
        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);
    }

    public void setPassenger(String bookingRef, String passengerName, String flight, String cabin) {
        this.bookingRef = bookingRef;
        this.passengerName = passengerName;
        this.flight = flight;
        this.cabin = cabin;
        showSummary();
    }

    private GridBagConstraints baseConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        return gbc;
    }

    private JLabel createLogo() {
        JLabel logoLabel = new JLabel();
        try {
            java.net.URL logoUrl = getClass().getResource("/resources/logo.png");
            if (logoUrl != null) {
                ImageIcon original = new ImageIcon(logoUrl);
                Image scaled = original.getImage().getScaledInstance(120, 60, Image.SCALE_SMOOTH);
                logoLabel.setIcon(new ImageIcon(scaled));
            }
        } catch (Exception e) {
            System.err.println("Could not load logo: " + e.getMessage());
        }
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        return logoLabel;
    }

    private JLabel createTitle(String text) {
        JLabel title = new JLabel(text, SwingConstants.CENTER);
        title.setFont(AppFonts.bold(26));
        title.setForeground(MainFrame.NAV_BTN_BG);
        return title;
    }

    private void showSummary() {
        body.removeAll();
        GridBagConstraints gbc = baseConstraints();

        gbc.gridy = 0;
        gbc.insets = new Insets(0, 10, 12, 10);
        body.add(createLogo(), gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 10, 6, 10);
        body.add(createTitle("Confirm Check-In"), gbc);

        JLabel sub = new JLabel("Please review your details before checking in.", SwingConstants.CENTER);
        sub.setFont(AppFonts.regular(12));
        sub.setForeground(new Color(60, 80, 70));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 10, 20, 10);
        body.add(sub, gbc);

        JPanel info = new JPanel(new GridLayout(3, 1, 0, 10));
        info.setOpaque(false);
        info.add(createInfoRow("Passenger", passengerName));
        info.add(createInfoRow("Flight", flight));
        info.add(createInfoRow("Cabin", cabin));
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 10, 24, 10);
        body.add(info, gbc);

        JButton confirm = createPrimaryButton("Confirm and Check In");
        confirm.addActionListener(e -> confirmCheckIn(confirm));
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 10, 6, 10);
        body.add(confirm, gbc);

        JLabel status = new JLabel(" ", SwingConstants.CENTER);
        status.setFont(AppFonts.italic(12));
        status.setForeground(new Color(200, 50, 50));
        status.setName("status");
        gbc.gridy = 5;
        gbc.insets = new Insets(8, 10, 5, 10);
        body.add(status, gbc);

        body.revalidate();
        body.repaint();
    }

    private JPanel createInfoRow(String label, String value) {
        JPanel row = new RoundedPanel(20, new Color(245, 249, 247));
        row.setLayout(new BorderLayout());
        row.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel l = new JLabel(label);
        l.setFont(AppFonts.regular(12));
        l.setForeground(new Color(90, 110, 100));

        JLabel v = new JLabel(value);
        v.setFont(AppFonts.bold(16));
        v.setForeground(MainFrame.NAV_BTN_BG);

        JPanel text = new JPanel(new GridLayout(2, 1));
        text.setOpaque(false);
        text.add(l);
        text.add(v);
        row.add(text, BorderLayout.CENTER);
        return row;
    }

    private void confirmCheckIn(JButton confirm) {
        confirm.setEnabled(false);
        confirm.setText("Checking in...");

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                QueueManager qm = QueueManager.getInstance();
                String ticketNo = qm.generateTicketNumber();
                Passenger passenger = new Passenger(bookingRef, passengerName, flight, "No baggage", ticketNo, cabin);
                passenger.setCabin(cabin);
                qm.addPassenger(passenger);
                try {
                    FirebaseHelper.put("pendingTickets/" + ticketNo + "/baggage", "\"No baggage\"");
                } catch (Exception ignored) {
                }
                return ticketNo;
            }

            @Override
            protected void done() {
                try {
                    showBoardingPass(get());
                } catch (Exception ex) {
                    confirm.setEnabled(true);
                    confirm.setText("Confirm and Check In");
                    for (Component c : body.getComponents()) {
                        if ("status".equals(c.getName())) {
                            ((JLabel) c).setText("Check-in failed. Check internet / Firebase.");
                        }
                    }
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void showBoardingPass(String ticketNo) {
        body.removeAll();
        GridBagConstraints gbc = baseConstraints();

        gbc.gridy = 0;
        gbc.insets = new Insets(0, 10, 8, 10);
        body.add(createTitle("Printing Boarding Pass"), gbc);

        JLabel sub = new JLabel("Please wait while your boarding pass is printed.", SwingConstants.CENTER);
        sub.setFont(AppFonts.regular(12));
        sub.setForeground(new Color(60, 80, 70));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 10, 16, 10);
        body.add(sub, gbc);

        String payload = "BP|" + bookingRef + "|" + passengerName + "|" + flight + "|" + cabin + "|" + ticketNo;
        BoardingPass pass = new BoardingPass(passengerName, flight, cabin, ticketNo, generateQr(payload, 130));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 10, 16, 10);
        gbc.fill = GridBagConstraints.NONE;
        body.add(pass, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel questionHolder = new JPanel(new GridBagLayout());
        questionHolder.setOpaque(false);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 10, 0, 10);
        body.add(questionHolder, gbc);

        body.revalidate();
        body.repaint();

        pass.startPrinting(() -> showBaggageQuestion(questionHolder, ticketNo));
    }

    private void showBaggageQuestion(JPanel holder, String ticketNo) {
        holder.removeAll();
        GridBagConstraints gbc = baseConstraints();

        JLabel q = new JLabel("Do you have checked baggage?", SwingConstants.CENTER);
        q.setFont(AppFonts.bold(18));
        q.setForeground(MainFrame.NAV_BTN_BG);
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(8, 0, 12, 0);
        holder.add(q, gbc);

        JButton yes = createPrimaryButton("Yes");
        JButton no = createPrimaryButton("No");
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 0, 8);
        holder.add(yes, gbc);
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 8, 0, 0);
        holder.add(no, gbc);

        yes.addActionListener(e -> {
            yes.setEnabled(false);
            no.setEnabled(false);
            flagBaggage(ticketNo);
            showDirection(holder, "Baggage Counter", "Go to the assigned counter or bag-drop station");
        });
        no.addActionListener(e -> {
            yes.setEnabled(false);
            no.setEnabled(false);
            new Thread(() -> {
                try {
                    FirebaseHelper.put("pendingTickets/" + ticketNo + "/status", "\"WAITING BOARDING\"");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
            showDirection(holder, "Security", "Proceed toward the boarding gate");
        });

        holder.revalidate();
        holder.repaint();
        SwingUtilities.invokeLater(() -> body.scrollRectToVisible(holder.getBounds()));
    }

    private void flagBaggage(String ticketNo) {
        new Thread(() -> {
            try {
                FirebaseHelper.put("pendingTickets/" + ticketNo + "/baggage",
                        "\"Declared - pending weigh-in\"");
                FirebaseHelper.put("pendingTickets/" + ticketNo + "/status",
                        "\"BAGGAGE DROP\"");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void showDirection(JPanel holder, String title, String message) {
        holder.removeAll();
        GridBagConstraints gbc = baseConstraints();

        JPanel box = new RoundedPanel(20, new Color(232, 244, 238));
        box.setLayout(new GridLayout(2, 1, 0, 4));
        box.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(AppFonts.bold(20));
        t.setForeground(MainFrame.NAV_BTN_BG);
        JLabel m = new JLabel(message, SwingConstants.CENTER);
        m.setFont(AppFonts.regular(13));
        m.setForeground(TEXT_COLOR);
        box.add(t);
        box.add(m);

        gbc.gridy = 0;
        gbc.insets = new Insets(8, 0, 14, 0);
        holder.add(box, gbc);

        JButton done = createPrimaryButton("Done");
        done.addActionListener(e -> {
            body.removeAll();
            mainFrame.showPanel("PassengerMenu");
        });
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        holder.add(done, gbc);

        holder.revalidate();
        holder.repaint();
        SwingUtilities.invokeLater(() -> body.scrollRectToVisible(holder.getBounds()));
    }

    private BufferedImage generateQr(String text, int size) {
        try {
            boolean[][] matrix = QrEncoder.encode(text);
            int n = matrix.length;
            int border = 2;
            int total = n + border * 2;
            BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            for (int px = 0; px < size; px++) {
                for (int py = 0; py < size; py++) {
                    int mx = px * total / size - border;
                    int my = py * total / size - border;
                    boolean dark = mx >= 0 && my >= 0 && mx < n && my < n && matrix[my][mx];
                    img.setRGB(px, py, dark ? 0x000000 : 0xFFFFFF);
                }
            }
            return img;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? getBackground() : new Color(160, 175, 168));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(AppFonts.regular(15));
        btn.setBackground(MainFrame.SECONDARY_BTN_BG);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        btn.setPreferredSize(new Dimension(200, 46));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(MainFrame.NAV_BTN_BG);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(MainFrame.SECONDARY_BTN_BG);
            }
        });
        return btn;
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
        return btn;
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
            g.drawImage(backgroundImage, (panelW - drawW) / 2, (panelH - drawH) / 2, drawW, drawH, this);
        } else {
            g.setColor(MainFrame.MAIN_BG);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private static class BoardingPass extends JPanel {
        private static final int PASS_W = 560;
        private static final int PASS_H = 190;

        private final String name;
        private final String flight;
        private final String cabin;
        private final String ticketNo;
        private final BufferedImage qr;
        private int revealed = 0;
        private Timer timer;

        BoardingPass(String name, String flight, String cabin, String ticketNo, BufferedImage qr) {
            this.name = name;
            this.flight = flight;
            this.cabin = cabin;
            this.ticketNo = ticketNo;
            this.qr = qr;
            setOpaque(false);
            setPreferredSize(new Dimension(PASS_W + 10, PASS_H + 14));
        }

        void startPrinting(Runnable onFinish) {
            revealed = 0;
            timer = new Timer(18, null);
            timer.addActionListener(e -> {
                revealed += 4;
                if (revealed >= PASS_H) {
                    revealed = PASS_H;
                    timer.stop();
                    repaint();
                    onFinish.run();
                    return;
                }
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2.setColor(new Color(60, 70, 66));
            g2.fillRoundRect(0, 0, PASS_W + 10, 10, 10, 10);

            g2.setClip(5, 6, PASS_W, revealed);
            g2.translate(5, 6 + revealed - PASS_H);

            g2.setColor(new Color(0, 0, 0, 30));
            g2.fillRoundRect(2, 4, PASS_W, PASS_H, 18, 18);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, PASS_W, PASS_H, 18, 18);
            g2.setColor(MainFrame.NAV_BTN_BG);
            g2.fillRoundRect(0, 0, PASS_W, 40, 18, 18);
            g2.fillRect(0, 20, PASS_W, 20);

            g2.setColor(Color.WHITE);
            g2.setFont(AppFonts.bold(15));
            g2.drawString("BOARDING PASS", 20, 26);
            g2.setFont(AppFonts.regular(12));
            g2.drawString(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")), PASS_W - 170, 26);

            drawField(g2, "PASSENGER", name, 20, 66);
            drawField(g2, "FLIGHT", flight, 20, 120);
            drawField(g2, "CABIN", cabin, 190, 120);
            drawField(g2, "TICKET", ticketNo, 330, 120);

            g2.setColor(new Color(190, 200, 195));
            g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{5f, 5f}, 0f));
            g2.drawLine(PASS_W - 160, 48, PASS_W - 160, PASS_H - 10);

            if (qr != null) {
                g2.drawImage(qr, PASS_W - 148, 56, 120, 120, null);
            }
            g2.dispose();
        }

        private void drawField(Graphics2D g2, String label, String value, int x, int y) {
            g2.setColor(new Color(110, 125, 118));
            g2.setFont(AppFonts.regular(10));
            g2.drawString(label, x, y);
            g2.setColor(TEXT_COLOR);
            g2.setFont(AppFonts.bold(15));
            g2.drawString(value, x, y + 20);
        }
    }

    private static final class QrEncoder {
        private static final int[] ECC_PER_BLOCK = {-1, 10, 16, 26, 18, 24, 16, 18, 22, 22};
        private static final int[] NUM_BLOCKS = {-1, 1, 1, 1, 2, 2, 4, 4, 4, 5};

        private final int version;
        private final int size;
        private final boolean[][] modules;
        private final boolean[][] isFunction;

        static boolean[][] encode(String text) {
            byte[] data = text.getBytes(StandardCharsets.UTF_8);
            int ver = 1;
            while (ver <= 9 && 12 + data.length * 8 > dataCodewords(ver) * 8) {
                ver++;
            }
            if (ver > 9) {
                throw new IllegalArgumentException("Data too long for QR code");
            }

            List<Boolean> bits = new ArrayList<>();
            appendBits(bits, 4, 4);
            appendBits(bits, data.length, 8);
            for (byte b : data) {
                appendBits(bits, b & 0xFF, 8);
            }
            int capacityBits = dataCodewords(ver) * 8;
            appendBits(bits, 0, Math.min(4, capacityBits - bits.size()));
            appendBits(bits, 0, (8 - bits.size() % 8) % 8);
            for (int pad = 0xEC; bits.size() < capacityBits; pad ^= 0xEC ^ 0x11) {
                appendBits(bits, pad, 8);
            }
            byte[] dataCw = new byte[bits.size() / 8];
            for (int i = 0; i < bits.size(); i++) {
                if (bits.get(i)) {
                    dataCw[i >>> 3] |= 1 << (7 - (i & 7));
                }
            }
            return new QrEncoder(ver, dataCw).modules;
        }

        private QrEncoder(int version, byte[] dataCw) {
            this.version = version;
            this.size = version * 4 + 17;
            this.modules = new boolean[size][size];
            this.isFunction = new boolean[size][size];

            drawFunctionPatterns();
            drawCodewords(addEccAndInterleave(dataCw));

            int bestMask = 0;
            int minPenalty = Integer.MAX_VALUE;
            for (int m = 0; m < 8; m++) {
                applyMask(m);
                drawFormatBits(m);
                int p = penalty();
                if (p < minPenalty) {
                    minPenalty = p;
                    bestMask = m;
                }
                applyMask(m);
            }
            applyMask(bestMask);
            drawFormatBits(bestMask);
        }

        private static void appendBits(List<Boolean> list, int value, int len) {
            for (int i = len - 1; i >= 0; i--) {
                list.add(((value >>> i) & 1) != 0);
            }
        }

        private static int rawModules(int ver) {
            int result = (16 * ver + 128) * ver + 64;
            if (ver >= 2) {
                int numAlign = ver / 7 + 2;
                result -= (25 * numAlign - 10) * numAlign - 55;
                if (ver >= 7) {
                    result -= 36;
                }
            }
            return result;
        }

        private static int dataCodewords(int ver) {
            return rawModules(ver) / 8 - ECC_PER_BLOCK[ver] * NUM_BLOCKS[ver];
        }

        private void setFunction(int x, int y, boolean dark) {
            modules[y][x] = dark;
            isFunction[y][x] = true;
        }

        private void drawFunctionPatterns() {
            for (int i = 0; i < size; i++) {
                setFunction(6, i, i % 2 == 0);
                setFunction(i, 6, i % 2 == 0);
            }
            drawFinder(3, 3);
            drawFinder(size - 4, 3);
            drawFinder(3, size - 4);

            int[] pos = alignmentPositions();
            int n = pos.length;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if ((i == 0 && j == 0) || (i == 0 && j == n - 1) || (i == n - 1 && j == 0)) {
                        continue;
                    }
                    drawAlignment(pos[i], pos[j]);
                }
            }
            drawFormatBits(0);
            drawVersion();
        }

        private void drawFinder(int cx, int cy) {
            for (int dy = -4; dy <= 4; dy++) {
                for (int dx = -4; dx <= 4; dx++) {
                    int dist = Math.max(Math.abs(dx), Math.abs(dy));
                    int xx = cx + dx;
                    int yy = cy + dy;
                    if (xx >= 0 && xx < size && yy >= 0 && yy < size) {
                        setFunction(xx, yy, dist != 2 && dist != 4);
                    }
                }
            }
        }

        private void drawAlignment(int cx, int cy) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dx = -2; dx <= 2; dx++) {
                    setFunction(cx + dx, cy + dy, Math.max(Math.abs(dx), Math.abs(dy)) != 1);
                }
            }
        }

        private int[] alignmentPositions() {
            if (version == 1) {
                return new int[0];
            }
            int numAlign = version / 7 + 2;
            int step = (version * 4 + numAlign * 2 + 1) / (numAlign * 2 - 2) * 2;
            int[] result = new int[numAlign];
            result[0] = 6;
            for (int i = numAlign - 1, pos = size - 7; i >= 1; i--, pos -= step) {
                result[i] = pos;
            }
            return result;
        }

        private static boolean bit(int value, int i) {
            return ((value >>> i) & 1) != 0;
        }

        private void drawFormatBits(int mask) {
            int data = mask;
            int rem = data;
            for (int i = 0; i < 10; i++) {
                rem = (rem << 1) ^ ((rem >>> 9) * 0x537);
            }
            int bits = ((data << 10) | rem) ^ 0x5412;

            for (int i = 0; i <= 5; i++) {
                setFunction(8, i, bit(bits, i));
            }
            setFunction(8, 7, bit(bits, 6));
            setFunction(8, 8, bit(bits, 7));
            setFunction(7, 8, bit(bits, 8));
            for (int i = 9; i < 15; i++) {
                setFunction(14 - i, 8, bit(bits, i));
            }
            for (int i = 0; i < 8; i++) {
                setFunction(size - 1 - i, 8, bit(bits, i));
            }
            for (int i = 8; i < 15; i++) {
                setFunction(8, size - 15 + i, bit(bits, i));
            }
            setFunction(8, size - 8, true);
        }

        private void drawVersion() {
            if (version < 7) {
                return;
            }
            int rem = version;
            for (int i = 0; i < 12; i++) {
                rem = (rem << 1) ^ ((rem >>> 11) * 0x1F25);
            }
            int bits = (version << 12) | rem;
            for (int i = 0; i < 18; i++) {
                boolean b = bit(bits, i);
                int a = size - 11 + i % 3;
                int c = i / 3;
                setFunction(a, c, b);
                setFunction(c, a, b);
            }
        }

        private byte[] addEccAndInterleave(byte[] data) {
            int numBlocks = NUM_BLOCKS[version];
            int blockEccLen = ECC_PER_BLOCK[version];
            int rawCodewords = rawModules(version) / 8;
            int numShortBlocks = numBlocks - rawCodewords % numBlocks;
            int shortBlockLen = rawCodewords / numBlocks;

            byte[][] blocks = new byte[numBlocks][];
            byte[] divisor = rsDivisor(blockEccLen);
            for (int i = 0, k = 0; i < numBlocks; i++) {
                int len = shortBlockLen - blockEccLen + (i < numShortBlocks ? 0 : 1);
                byte[] dat = Arrays.copyOfRange(data, k, k + len);
                k += len;
                byte[] ecc = rsRemainder(dat, divisor);
                byte[] block = new byte[shortBlockLen + 1];
                System.arraycopy(dat, 0, block, 0, dat.length);
                System.arraycopy(ecc, 0, block, block.length - blockEccLen, ecc.length);
                blocks[i] = block;
            }

            byte[] result = new byte[rawCodewords];
            for (int i = 0, k = 0; i < blocks[0].length; i++) {
                for (int j = 0; j < blocks.length; j++) {
                    if (i != shortBlockLen - blockEccLen || j >= numShortBlocks) {
                        result[k++] = blocks[j][i];
                    }
                }
            }
            return result;
        }

        private static byte[] rsDivisor(int degree) {
            byte[] result = new byte[degree];
            result[degree - 1] = 1;
            int root = 1;
            for (int i = 0; i < degree; i++) {
                for (int j = 0; j < degree; j++) {
                    result[j] = (byte) gfMul(result[j] & 0xFF, root);
                    if (j + 1 < degree) {
                        result[j] ^= result[j + 1];
                    }
                }
                root = gfMul(root, 0x02);
            }
            return result;
        }

        private static byte[] rsRemainder(byte[] data, byte[] divisor) {
            byte[] result = new byte[divisor.length];
            for (byte b : data) {
                int factor = (b ^ result[0]) & 0xFF;
                System.arraycopy(result, 1, result, 0, result.length - 1);
                result[result.length - 1] = 0;
                for (int i = 0; i < result.length; i++) {
                    result[i] ^= gfMul(divisor[i] & 0xFF, factor);
                }
            }
            return result;
        }

        private static int gfMul(int x, int y) {
            int z = 0;
            for (int i = 7; i >= 0; i--) {
                z = (z << 1) ^ ((z >>> 7) * 0x11D);
                z ^= ((y >>> i) & 1) * x;
            }
            return z & 0xFF;
        }

        private void drawCodewords(byte[] data) {
            int i = 0;
            for (int right = size - 1; right >= 1; right -= 2) {
                if (right == 6) {
                    right = 5;
                }
                for (int vert = 0; vert < size; vert++) {
                    for (int j = 0; j < 2; j++) {
                        int x = right - j;
                        boolean upward = ((right + 1) & 2) == 0;
                        int y = upward ? size - 1 - vert : vert;
                        if (!isFunction[y][x] && i < data.length * 8) {
                            modules[y][x] = bit(data[i >>> 3], 7 - (i & 7));
                            i++;
                        }
                    }
                }
            }
        }

        private void applyMask(int mask) {
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    boolean invert;
                    switch (mask) {
                        case 0: invert = (x + y) % 2 == 0; break;
                        case 1: invert = y % 2 == 0; break;
                        case 2: invert = x % 3 == 0; break;
                        case 3: invert = (x + y) % 3 == 0; break;
                        case 4: invert = (x / 3 + y / 2) % 2 == 0; break;
                        case 5: invert = x * y % 2 + x * y % 3 == 0; break;
                        case 6: invert = (x * y % 2 + x * y % 3) % 2 == 0; break;
                        default: invert = ((x + y) % 2 + x * y % 3) % 2 == 0; break;
                    }
                    if (invert && !isFunction[y][x]) {
                        modules[y][x] = !modules[y][x];
                    }
                }
            }
        }

        private int penalty() {
            int result = 0;
            for (int a = 0; a < size; a++) {
                int runRow = 1;
                int runCol = 1;
                for (int b = 1; b < size; b++) {
                    if (modules[a][b] == modules[a][b - 1]) {
                        runRow++;
                    } else {
                        if (runRow >= 5) result += runRow - 2;
                        runRow = 1;
                    }
                    if (modules[b][a] == modules[b - 1][a]) {
                        runCol++;
                    } else {
                        if (runCol >= 5) result += runCol - 2;
                        runCol = 1;
                    }
                }
                if (runRow >= 5) result += runRow - 2;
                if (runCol >= 5) result += runCol - 2;
            }
            int dark = 0;
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    if (modules[y][x]) dark++;
                    if (x > 0 && y > 0 && modules[y][x] == modules[y][x - 1]
                            && modules[y][x] == modules[y - 1][x] && modules[y][x] == modules[y - 1][x - 1]) {
                        result += 3;
                    }
                }
            }
            int total = size * size;
            int k = (Math.abs(dark * 20 - total * 10) + total - 1) / total - 1;
            return result + k * 10;
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bg;

        RoundedPanel(int radius, Color bg) {
            this.radius = radius;
            this.bg = bg;
            setOpaque(false);
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

    private static class GlassPanel extends JPanel {
        GlassPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int arc = 32;
            g2.setColor(new Color(0, 0, 0, 35));
            g2.fill(new RoundRectangle2D.Float(5, 7, w - 10, h - 10, arc, arc));
            g2.setColor(new Color(255, 255, 255, 245));
            g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, arc, arc));
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.6f));
            g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, arc, arc));
            g2.dispose();
            super.paintComponent(g);
        }
    }
}