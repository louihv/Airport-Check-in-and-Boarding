import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.AbstractBorder;

public final class ModernDialog {

    private ModernDialog() {}

    public static void showMessage(Component parent, String message, String title, boolean isError) {
        Window owner = parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
        JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel content = (JPanel) dialog.getContentPane();
        content.setOpaque(false);
        content.setLayout(new BorderLayout());

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

        JButton ok = createButton("OK", isError ? new Color(180, 50, 50) : new Color(46, 160, 90));
        ok.addActionListener(e -> dialog.dispose());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(ok);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblMsg, BorderLayout.CENTER);
        card.add(btnRow, BorderLayout.SOUTH);

        content.add(card, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    public static void showInfo(Component parent, String message) {
        showMessage(parent, message, "Info", false);
    }

    public static void showError(Component parent, String message) {
        showMessage(parent, message, "Error", true);
    }

    public static void showWarn(Component parent, String message) {
        showMessage(parent, message, "Notice", false);
    }

    public static boolean showConfirm(Component parent, String message, String title) {
        final boolean[] result = {false};

        Window owner = parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
        JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel content = (JPanel) dialog.getContentPane();
        content.setOpaque(false);
        content.setLayout(new BorderLayout());

        RoundedPanel card = new RoundedPanel(16, Color.WHITE);
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedOutlineBorder(1, MainFrame.SECONDARY_BTN_BG, 16),
                BorderFactory.createEmptyBorder(24, 28, 24, 28)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppFonts.bold(16));
        lblTitle.setForeground(new Color(27, 77, 46));

        JLabel lblMsg = new JLabel("<html><body style='width:280px'>" + message.replace("\n", "<br>") + "</body></html>");
        lblMsg.setFont(AppFonts.regular(13));
        lblMsg.setForeground(new Color(50, 65, 55));

        JButton no = createButton("Cancel", new Color(120, 130, 125));
        no.addActionListener(e -> dialog.dispose());

        JButton yes = createButton("Confirm", new Color(220, 70, 70));
        yes.addActionListener(e -> {
            result[0] = true;
            dialog.dispose();
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.add(no);
        btnRow.add(yes);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblMsg, BorderLayout.CENTER);
        card.add(btnRow, BorderLayout.SOUTH);

        content.add(card, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return result[0];
    }

    public static String showChoice(Component parent, String message, String title, String[] choices) {
        final String[] result = {null};

        Window owner = parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
        JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel content = (JPanel) dialog.getContentPane();
        content.setOpaque(false);
        content.setLayout(new BorderLayout());

        RoundedPanel card = new RoundedPanel(16, Color.WHITE);
        card.setLayout(new BorderLayout(0, 16));
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
        styleCombo(combo);
        combo.setPreferredSize(new Dimension(320, 36));

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.add(lblMsg);
        center.add(Box.createVerticalStrut(12));
        center.add(combo);

        JButton cancel = createButton("Cancel", new Color(120, 130, 125));
        cancel.addActionListener(e -> dialog.dispose());

        JButton ok = createButton("OK", new Color(46, 160, 90));
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

        content.add(card, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return result[0];
    }

    private static JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(AppFonts.bold(13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setPreferredSize(new Dimension(100, 36));
        return btn;
    }

    private static void styleCombo(JComboBox<?> combo) {
        combo.setFont(AppFonts.regular(13));
        combo.setBackground(Color.WHITE);
        combo.setForeground(new Color(27, 77, 46));
        combo.setFocusable(false);
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

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bg;

        public RoundedPanel(int radius, Color bg) {
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