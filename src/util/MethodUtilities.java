package util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;


import java.awt.Color;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.FontMetrics;
import java.awt.AlphaComposite;

import main.BaseFrame;

public class MethodUtilities {
    public static class exitAction implements ActionListener, WindowListener {
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
            windowExit();
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

        public static void exit() {
            int choice = JOptionPane.showConfirmDialog(
                frame,
                "Are you sure you want to exit?",
                "WARNING: Close program",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if(choice == JOptionPane.YES_OPTION) {
                frame.dispose();
            } else {
                frame.gamePanel.requestFocusInWindow();
            }
        }

        public void windowExit() {
            int choice = JOptionPane.showConfirmDialog(
                frame,
                "Are you sure you want to exit?",
                "WARNING: Close program",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if(choice == JOptionPane.YES_OPTION) {
                frame.gamePanel.stopGameThread();
                frame.getCredits().stopTimer();
                frame.dispose();
            } 
        } 
    }

public static class GlowLabel extends JLabel {
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

        // 1. Draw the "Glow" layers
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(getText())) / 2; // Center alignment example
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

        for (int i = glowSize; i > 0; i--) {
            float alpha = 0.1f * (1.0f - (float) i / glowSize); // Fade out
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setColor(glowColor);
            // Draw slightly offset in all directions
            g2.drawString(getText(), x - i, y - i);
            g2.drawString(getText(), x + i, y + i);
        }

        // 2. Draw the foreground text
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g2.setColor(getForeground());
        g2.drawString(getText(), x, y);

        g2.dispose();
    }
}

}
