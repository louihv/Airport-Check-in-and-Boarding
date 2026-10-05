import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class QueueMonitoringPanel extends JPanel {
    private static final String SERVED_BY_KEY = "servedBy";

    private final List<Map<String, String>> visibleTickets = new ArrayList<>();
    private String staffId = "";
    private boolean admin;
    private JTable queueTable;
    private DefaultTableModel tableModel;
    private JButton btnRefresh;
    private JButton btnExport;

    public QueueMonitoringPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(MainFrame.MAIN_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] columns = {"Ticket No.", "Booking Ref", "Name", "Flight", "Counter", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        queueTable = new JTable(tableModel);
        styleTable(queueTable);
        queueTable.setTableHeader(null);

        JPanel outerCard = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 18, 18));
                g2.dispose();
            }
        };
        outerCard.setOpaque(false);
        outerCard.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel headerPanel = new JPanel(new GridLayout(1, columns.length, 0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(MainFrame.MAIN_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 18, 18));
                g2.dispose();
            }
        };
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        headerPanel.setPreferredSize(new Dimension(0, 46));

        for (String col : columns) {
            JLabel lbl = new JLabel(col);
            lbl.setForeground(MainFrame.SECONDARY_BTN_BG);
            lbl.setFont(AppFonts.bold(12));
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

        JScrollPane scrollPane = new JScrollPane(queueTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setOpaque(false);
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        bodyCard.add(scrollPane, BorderLayout.CENTER);

        outerCard.add(headerPanel, BorderLayout.NORTH);
        outerCard.add(bodyCard, BorderLayout.CENTER);

        btnRefresh = createOutlineButton("Refresh Tickets");
        btnExport = createOutlineButton("Export PDF");

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottom.setOpaque(false);
        bottom.add(btnExport);
        bottom.add(btnRefresh);

        add(outerCard, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> loadTickets());
        btnExport.addActionListener(e -> exportPdf());
    }

    public void setUser(String username, String role) {
        this.staffId = username == null ? "" : username.trim();
        this.admin = "ADMIN".equals(role);
        loadTickets();
    }

    public void clear() {
        staffId = "";
        admin = false;
        visibleTickets.clear();
        tableModel.setRowCount(0);
    }

    private JButton createOutlineButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isPressed()) {
                    g.setColor(new Color(0, 0, 0, 30));
                    g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                } else if (getModel().isRollover()) {
                    g.setColor(new Color(0, 0, 0, 15));
                    g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                }
                super.paintComponent(g);
            }
        };
        btn.setFont(AppFonts.bold(13));
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setForeground(MainFrame.NAV_BTN_BG);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new RoundedOutlineBorder(1, new Color(216, 203, 194), 12));
        btn.setPreferredSize(new Dimension(160, 38));
        return btn;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(42);
        table.setFont(AppFonts.regular(13));
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
                    if (row % 2 == 0) {
                        setBackground(Color.WHITE);
                    } else {
                        setBackground(new Color(250, 251, 252));
                    }
                }

                if (column == 5 && value != null) {
                    String status = value.toString();
                    if ("Pending".equalsIgnoreCase(status) || "Waiting".equalsIgnoreCase(status)) {
                        setForeground(new Color(245, 166, 35));
                    } else if ("Delivered".equalsIgnoreCase(status) || "Completed".equalsIgnoreCase(status)
                            || "Served".equalsIgnoreCase(status)) {
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

    private void showModernMessage(String message, String title, boolean isError) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setLayout(new BorderLayout());

        JPanel content = (JPanel) dialog.getContentPane();
        content.setOpaque(false);
        content.setLayout(new BorderLayout());

        JPanel card = new JPanel(new BorderLayout(0, 16)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1, MainFrame.SECONDARY_BTN_BG, 16),
                BorderFactory.createEmptyBorder(24, 28, 24, 28)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppFonts.bold(16));
        lblTitle.setForeground(isError ? new Color(180, 50, 50) : MainFrame.NAV_BTN_BG);

        JLabel lblMsg = new JLabel("<html><body style='width:280px'>" + message.replace("\n", "<br>") + "</body></html>");
        lblMsg.setFont(AppFonts.regular(13));
        lblMsg.setForeground(new Color(50, 65, 55));

        JButton ok = new JButton("OK");
        ok.setFont(AppFonts.bold(13));
        ok.setBackground(isError ? new Color(180, 50, 50) : MainFrame.SECONDARY_BTN_BG);
        ok.setForeground(Color.WHITE);
        ok.setFocusPainted(false);
        ok.setCursor(new Cursor(Cursor.HAND_CURSOR));
        ok.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
        ok.setOpaque(true);
        ok.setContentAreaFilled(true);
        ok.setPreferredSize(new Dimension(100, 36));
        ok.addActionListener(e -> dialog.dispose());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(ok);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblMsg, BorderLayout.CENTER);
        card.add(btnRow, BorderLayout.SOUTH);

        content.add(card, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
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
            return new Insets(8, 16, 8, 16);
        }
    }

    private boolean isServedByCurrentUser(Map<String, String> ticket) {
        if (admin) return true;
        String servedBy = ticket.get(SERVED_BY_KEY);
        return !staffId.isEmpty() && servedBy != null && servedBy.trim().equalsIgnoreCase(staffId);
    }

    private static String counterDisplay(Map<String, String> t) {
        String counter = t.get("counter");
        return (counter == null || counter.equals("0") || counter.isEmpty())
                ? "Unassigned"
                : "Counter " + counter;
    }

    private void loadTickets() {
        if (!admin && staffId.isEmpty()) return;

        btnRefresh.setEnabled(false);
        btnExport.setEnabled(false);

        new SwingWorker<List<Map<String, String>>, Void>() {
            @Override
            protected List<Map<String, String>> doInBackground() throws Exception {
                return FirebaseHelper.getAllTickets();
            }

            @Override
            protected void done() {
                btnRefresh.setEnabled(true);
                btnExport.setEnabled(true);
                try {
                    List<Map<String, String>> tickets = get();
                    tableModel.setRowCount(0);
                    visibleTickets.clear();

                    if (tickets != null) {
                        for (Map<String, String> t : tickets) {
                            if (!isServedByCurrentUser(t)) continue;
                            visibleTickets.add(t);
                            tableModel.addRow(new Object[]{
                                t.get("ticketNo"),
                                t.get("bookingRef"),
                                t.get("name"),
                                t.get("flightNo"),
                                counterDisplay(t),
                                t.get("status")
                            });
                        }
                    }
                } catch (Exception ex) {
                    showModernMessage(
                            "Failed to load tickets.\nCheck internet / Firebase URL.",
                            "Error", true);
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void exportPdf() {
        if (visibleTickets.isEmpty()) {
            showModernMessage("There are no customers to export.", "Export PDF", true);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save PDF");
        chooser.setSelectedFile(new File("served_customers.pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            file = new File(file.getParentFile(), file.getName() + ".pdf");
        }

        try {
            Files.write(file.toPath(), buildPdf());
            showModernMessage("PDF saved to:\n" + file.getAbsolutePath(), "Export PDF", false);
        } catch (IOException ex) {
            showModernMessage("Failed to save PDF.\n" + ex.getMessage(), "Error", true);
        }
    }

    private byte[] buildPdf() throws IOException {
        final int pageW = 842;
        final int pageH = 595;
        final int margin = 40;
        final int rowH = 22;
        final int[] colX = {40, 140, 260, 450, 560, 680};
        final String[] headers = {"Ticket No.", "Booking Ref", "Name", "Flight", "Counter", "Status"};
        final int headerY = pageH - 105;
        final int rowsPerPage = (headerY - 30 - margin) / rowH;
        final int totalPages = (visibleTickets.size() + rowsPerPage - 1) / rowsPerPage;
        final String generated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        final String title = admin ? "All Customers" : "Served Customers - " + staffId;

        List<String> streams = new ArrayList<>();
        for (int p = 0; p < totalPages; p++) {
            StringBuilder sb = new StringBuilder();
            pdfText(sb, "F2", 16, margin, pageH - 55, title);
            pdfText(sb, "F1", 10, margin, pageH - 75,
                    "Total: " + visibleTickets.size() + "    Generated: " + generated);

            for (int c = 0; c < headers.length; c++) {
                pdfText(sb, "F2", 10, colX[c], headerY, headers[c]);
            }
            sb.append("0.6 w ").append(margin).append(' ').append(headerY - 6).append(" m ")
                    .append(pageW - margin).append(' ').append(headerY - 6).append(" l S\n");

            int start = p * rowsPerPage;
            int end = Math.min(start + rowsPerPage, visibleTickets.size());
            int y = headerY - 6 - rowH + 6;
            for (int i = start; i < end; i++) {
                Map<String, String> t = visibleTickets.get(i);
                String[] cells = {
                    t.get("ticketNo"), t.get("bookingRef"), t.get("name"),
                    t.get("flightNo"), counterDisplay(t), t.get("status")
                };
                for (int c = 0; c < cells.length; c++) {
                    int width = (c + 1 < colX.length ? colX[c + 1] : pageW - margin) - colX[c] - 8;
                    pdfText(sb, "F1", 10, colX[c], y, fit(cells[c], width / 5));
                }
                y -= rowH;
            }

            pdfText(sb, "F1", 9, pageW / 2 - 30, margin - 15, "Page " + (p + 1) + " of " + totalPages);
            streams.add(sb.toString());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();
        writeAscii(out, "%PDF-1.4\n");

        StringBuilder kids = new StringBuilder();
        for (int p = 0; p < totalPages; p++) {
            kids.append(5 + p * 2).append(" 0 R ");
        }

        pdfObject(out, offsets, 1, "<< /Type /Catalog /Pages 2 0 R >>");
        pdfObject(out, offsets, 2, "<< /Type /Pages /Kids [" + kids + "] /Count " + totalPages + " >>");
        pdfObject(out, offsets, 3, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>");
        pdfObject(out, offsets, 4, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>");

        for (int p = 0; p < totalPages; p++) {
            int pageId = 5 + p * 2;
            int contentId = pageId + 1;
            byte[] content = streams.get(p).getBytes(StandardCharsets.ISO_8859_1);
            pdfObject(out, offsets, pageId,
                    "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + pageW + " " + pageH + "] "
                            + "/Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents " + contentId + " 0 R >>");
            offsets.add(out.size());
            writeAscii(out, contentId + " 0 obj\n<< /Length " + content.length + " >>\nstream\n");
            out.write(content);
            writeAscii(out, "\nendstream\nendobj\n");
        }

        int xrefPos = out.size();
        StringBuilder xref = new StringBuilder();
        xref.append("xref\n0 ").append(offsets.size() + 1).append("\n0000000000 65535 f \n");
        for (int off : offsets) {
            xref.append(String.format("%010d 00000 n \n", off));
        }
        xref.append("trailer\n<< /Size ").append(offsets.size() + 1).append(" /Root 1 0 R >>\nstartxref\n")
                .append(xrefPos).append("\n%%EOF\n");
        writeAscii(out, xref.toString());

        return out.toByteArray();
    }

    private static void pdfObject(ByteArrayOutputStream out, List<Integer> offsets, int id, String body) throws IOException {
        offsets.add(out.size());
        writeAscii(out, id + " 0 obj\n" + body + "\nendobj\n");
    }

    private static void writeAscii(ByteArrayOutputStream out, String s) throws IOException {
        out.write(s.getBytes(StandardCharsets.ISO_8859_1));
    }

    private static void pdfText(StringBuilder sb, String font, int size, int x, int y, String text) {
        sb.append("BT /").append(font).append(' ').append(size).append(" Tf ")
                .append(x).append(' ').append(y).append(" Td (")
                .append(pdfEscape(text)).append(") Tj ET\n");
    }

    private static String pdfEscape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char ch : s.toCharArray()) {
            if (ch == '\\' || ch == '(' || ch == ')') {
                sb.append('\\').append(ch);
            } else if (ch < 32 || ch > 255) {
                sb.append('?');
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    private static String fit(String s, int maxChars) {
        if (s == null) return "";
        return s.length() <= maxChars ? s : s.substring(0, Math.max(0, maxChars - 1)) + "...";
    }
}