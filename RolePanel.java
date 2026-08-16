import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;

public class RolePanel extends JPanel {

    public RolePanel(MainFrame frame) {
        setBackground(MainFrame.MAIN_BG);
        setLayout(new GridBagLayout());

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);
        box.setBackground(MainFrame.MAIN_BG);

        JLabel title = new JLabel("Select Role", SwingConstants.CENTER);
        title.setFont(AppFonts.bold(60));
        title.setForeground(MainFrame.SECONDARY_BTN_BG);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        Icon kioskIcon = loadIcon("/resources/passport.png", 100);
        Icon loginIcon = loadIcon("/resources/employee.png", 100);

        JButton btnKiosk = createBigOutlineButton("PASSENGER KIOSK", kioskIcon);
        JButton btnLogin = createBigOutlineButton("EMPLOYEE PORTAL", loginIcon);

        btnKiosk.addActionListener(e -> frame.showPassengerMenu());
        btnLogin.addActionListener(e -> frame.showLogin());

        // Side-by-side row
        JPanel buttonsRow = new JPanel();
        buttonsRow.setLayout(new BoxLayout(buttonsRow, BoxLayout.X_AXIS));
        buttonsRow.setOpaque(false);

        buttonsRow.add(btnKiosk);
        buttonsRow.add(Box.createHorizontalStrut(40)); 
        buttonsRow.add(btnLogin);

        box.add(title);
        box.add(Box.createVerticalStrut(70));
        box.add(buttonsRow);

        add(box);
    }

    private JButton createBigOutlineButton(String text, Icon icon) {
        JButton btn = new JButton(text, icon) {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isPressed()) {
                    g.setColor(new Color(0, 0, 0, 30));
                    g.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                } else if (getModel().isRollover()) {
                    g.setColor(new Color(0, 0, 0, 15));
                    g.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                }
                super.paintComponent(g);
            }
        };

        btn.setFont(AppFonts.bold(16));
        btn.setPreferredSize(new Dimension(300, 300)); 
        btn.setMaximumSize(new Dimension(300, 300));
        btn.setMinimumSize(new Dimension(300, 300));

        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setForeground(MainFrame.NAV_BTN_BG);

        // Icon on top of text
        btn.setHorizontalTextPosition(SwingConstants.CENTER);
        btn.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn.setIconTextGap(14);

        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Rounded outline border
        btn.setBorder(new RoundedOutlineBorder(1, new Color(216,203,194), 22));

        return btn;
    }

    private Icon loadIcon(String path, int size) {
        java.net.URL url = getClass().getResource(path);
        if (url == null) {
            System.err.println("Icon not found: " + path);
            return null;
        }
        ImageIcon raw = new ImageIcon(url);
        Image scaled = raw.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    // Custom rounded outline border
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
            g2.setStroke(new java.awt.BasicStroke(thickness));
            g2.draw(new RoundRectangle2D.Float(
                    x + thickness / 2f,
                    y + thickness / 2f,
                    width - thickness,
                    height - thickness,
                    radius, radius));
            g2.dispose();
        }

        @Override
        public java.awt.Insets getBorderInsets(Component c) {
            return new java.awt.Insets(12, 12, 12, 12);
        }
    }
}