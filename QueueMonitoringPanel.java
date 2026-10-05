import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class QueueMonitoringPanel extends JPanel {
    private JTable queueTable;
    private DefaultTableModel tableModel;
    private JButton btnRefresh;
    private JButton btnExportPdf;

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
        btnExportPdf = createOutlineButton("Export PDF");

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottom.setOpaque(false);
        bottom.add(btnExportPdf);
        bottom.add(btnRefresh);

        add(outerCard, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> loadTickets());
        btnExportPdf.addActionListener(e -> exportToPdf());
        loadTickets();
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

    private String getCurrentCounter() {
        return MainFrame.getLoggedInCounter();
    }

    private boolean isMyTicket(Map<String, String> t) {
        String myCounter = getCurrentCounter();
        if (myCounter == null || myCounter.isEmpty()) return false;
        String ticketCounter = t.get("counter");
        if (ticketCounter == null || ticketCounter.isEmpty() || "0".equals(ticketCounter)) return false;
        return myCounter.equals(ticketCounter.trim());
    }

    private void loadTickets() {
        btnRefresh.setEnabled(false);
        btnExportPdf.setEnabled(false);

        new SwingWorker<List<Map<String, String>>, Void>() {
            @Override
            protected List<Map<String, String>> doInBackground() throws Exception {
                return FirebaseHelper.getAllTickets();
            }

            @Override
            protected void done() {
                btnRefresh.setEnabled(true);
                btnExportPdf.setEnabled(true);
                try {
                    List<Map<String, String>> tickets = get();
                    tableModel.setRowCount(0);

                    if (tickets != null) {
                        for (Map<String, String> t : tickets) {
                            if (!isMyTicket(t)) continue;

                            String counter = t.get("counter");
                            String counterDisplay = (counter == null || counter.equals("0") || counter.isEmpty())
                                    ? "Unassigned"
                                    : "Counter " + counter;

                            tableModel.addRow(new Object[]{
                                t.get("ticketNo"),
                                t.get("bookingRef"),
                                t.get("name"),
                                t.get("flightNo"),
                                counterDisplay,
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

    private void exportToPdf() {
        if (tableModel.getRowCount() == 0) {
            showModernMessage("No tickets to export.", "Export PDF", false);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save PDF");
        chooser.setSelectedFile(new File("My_Served_Tickets.pdf"));
        chooser.setFileFilter(new FileNameExtensionFilter("PDF files (*.pdf)", "pdf"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            file = new File(file.getAbsolutePath() + ".pdf");
        }

        final File outFile = file;
        btnExportPdf.setEnabled(false);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(outFile));
                document.open();

                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
                Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

                Paragraph title = new Paragraph("Served Tickets — Counter " + getCurrentCounter(), titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(16f);
                document.add(title);

                PdfPTable pdfTable = new PdfPTable(6);
                pdfTable.setWidthPercentage(100);
                pdfTable.setWidths(new float[]{1.2f, 1.4f, 1.8f, 1.2f, 1.2f, 1.2f});

                String[] headers = {"Ticket No.", "Booking Ref", "Name", "Flight", "Counter", "Status"};
                for (String h : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                    cell.setBackgroundColor(new BaseColor(240, 240, 240));
                    cell.setPadding(6);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    pdfTable.addCell(cell);
                }

                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        Object val = tableModel.getValueAt(i, j);
                        PdfPCell cell = new PdfPCell(new Phrase(val == null ? "" : val.toString(), cellFont));
                        cell.setPadding(5);
                        pdfTable.addCell(cell);
                    }
                }

                document.add(pdfTable);
                document.close();
                return null;
            }

            @Override
            protected void done() {
                btnExportPdf.setEnabled(true);
                try {
                    get();
                    showModernMessage("PDF saved successfully.", "Export PDF", false);
                } catch (Exception ex) {
                    showModernMessage("Failed to export PDF.\n" + ex.getMessage(), "Error", true);
                    ex.printStackTrace();
                }
            }
        }.execute();
    }
}