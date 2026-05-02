package util;
import main.BaseFrame;

import java.io.FileInputStream;
import java.io.InputStream;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.FontMetrics;
import java.awt.AlphaComposite;
import java.awt.LayoutManager;
import java.awt.Component;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JComponent;

/**
 * Collection of reusable Swing helpers and lightweight custom UI components.
 */
public class MethodUtilities {
    /**
     * Shared exit handler used by buttons and the main window close event.
     */
    public static class exitAction implements ActionListener, WindowListener {
        // Shared frame reference so one exit helper can be attached to multiple exit triggers.
        private static BaseFrame frame;

        public exitAction(BaseFrame frame) {
            exitAction.frame = frame;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            exit();
        }

        @Override
        public void windowClosing(WindowEvent e) {
            exit();
        }

        @Override
        public void windowOpened(WindowEvent e) {}

        @Override
        public void windowDeiconified(WindowEvent e) {}
        @Override
        public void windowActivated(WindowEvent e) {}
        @Override
        public void windowClosed(WindowEvent e) {}
        @Override
        public void windowDeactivated(WindowEvent e) {}
        @Override
        public void windowIconified(WindowEvent e) {}

        public void exit() {
            // Keep the exit flow in one place so button exit and window close behave the same.
            makeOptionDialog();
        }

        private void makeOptionDialog() {
            // Confirm before shutting down because the project may later include save-sensitive progress.
            int choice = JOptionPane.showConfirmDialog(
                frame,
                "Are you sure you want to exit?",
                "WARNING: Close program",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if(choice == JOptionPane.YES_OPTION) {
                // Stop any running timers/threads before disposing the frame.
                frame.scenePanel.stopScene();
                frame.gamePanel.stopGameThread();
                frame.getCredits().stopTimer();
                frame.openPanel.stopBackgroundAnimation();
                frame.dispose();
            } 
        }
    }

    /**
     * JLabel variant that paints a simple glow behind the text.
     */
    public static class GlowLabel extends JLabel {
        // Simple text glow effect settings.
        private Color glowColor = Color.CYAN;
        private int glowSize = 6;

        public GlowLabel(String text) {
            super(text);
            setOpaque(false); // Ensure background doesn't block the glow
        }

        public GlowLabel(String text, Color color) {
            super(text);
            setOpaque(false);
            this.glowColor = color;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Compute centered text coordinates once, then reuse them for glow and foreground.
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2; // Center alignment example
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

            // Draw a few translucent offset copies to fake a glow halo.
            for (int i = glowSize; i > 0; i--) {
                float alpha = 0.1f * (1.0f - (float) i / glowSize); // Fade out
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2.setColor(glowColor);
                // Draw slightly offset in all directions
                g2.drawString(getText(), x - i, y - i);
                g2.drawString(getText(), x + i, y + i);
            }

            // Finally draw the readable text on top.
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g2.setColor(getForeground());
            g2.drawString(getText(), x, y);

            g2.dispose();
        }
    }

    // RoundedPanel is a reusable painted background panel used across dialogs and menu sections.
    /**
     * Panel with a painted rounded rectangle background.
     */
    public static class RoundedPanel extends JPanel {
        private int radius;
        private Color color = new Color(159, 188, 143);

        public RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        public RoundedPanel(LayoutManager layout, int radius) {
            super(layout);
            this.radius = radius;
            setOpaque(false); 
        }

        public void setColor(Color color) {
            this.color = color;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D)g;

            // Anti-aliasing keeps the rounded corners from looking jagged.
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(color);

            // Paint the whole panel as one rounded rectangle.
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            // //make a line border
            // g2.setColor(Color.BLACK);
            // g2.drawRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        }
    }

    /**
     * JButton variant with a custom flat color treatment and hover/press feedback.
     */
    public static class CustomButton extends JButton {
        // Default and pressed colors for the custom flat-looking buttons.
        private Color backColor = new Color(129, 167, 109);
        private Color temp = new Color(129, 167, 109);
        
        public CustomButton(String text) {
            super(text);
            setForeground(new Color(47, 55, 47));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(true);
            setOpaque(false);
            setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, new Color(175, 199, 162), new Color(51, 69, 41)));

            // Mouse feedback changes border style and fill color so the button feels interactive.
            addMouseListener(new MouseAdapter(){
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, new Color(175, 199, 162), new Color(51, 69, 41)));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, new Color(175, 199, 162), new Color(51, 69, 41)));
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    temp = new Color(77, 104, 62);
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    temp = backColor;
                    repaint();
                }

            });
            
            this.setFont(MethodUtilities.getFont(20f, this));
        }

        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            // Paint the custom background first, then let JButton draw text and border details.
            g2.setColor(temp);
            g2.fillRect(0, 0, getWidth(), getHeight());

            super.paintComponent(g2);
            g2.dispose();
        }
        
    }

    /**
     * Loads the project's UI font and immediately applies it to the supplied component.
     */
    public static Font getFont(float size, JComponent component) {
        // Loads the project's button/body font and applies it directly to the given component.
        Font textFont;

        try (InputStream font = new FileInputStream("res/Font/texts.ttf")) {
            textFont = Font.createFont(Font.TRUETYPE_FONT, font);
            textFont = textFont.deriveFont(Font.BOLD, size);

            component.setFont(textFont);
            component.setAlignmentX(Component.CENTER_ALIGNMENT);

            return textFont;

        } catch (Exception e) {

            // If the custom font fails, fall back to a standard bold sans-serif font.
            Font fallbackFont = new Font("SansSerif", Font.BOLD, (int) size);
            component.setFont(fallbackFont);
            component.setAlignmentX(Component.CENTER_ALIGNMENT);

            return fallbackFont;
        }
    }

    /**
     * Returns the project's UI font without mutating any component.
     */
    public static Font getFont(float size) {
        // Variant that only returns the font object without touching a component.
        Font textFont;

        try (InputStream font = new FileInputStream("res/Font/texts.ttf")) {
            textFont = Font.createFont(Font.TRUETYPE_FONT, font);
            textFont = textFont.deriveFont(Font.BOLD, size);

            return textFont;

        } catch (Exception e) {

            // Fallback for places that still need a usable font even when assets are missing.
            Font fallbackFont = new Font("Arial", Font.BOLD, (int) size);

            return fallbackFont;
        }
    }
}
