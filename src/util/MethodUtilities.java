package util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileInputStream;
import java.io.InputStream;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.FontMetrics;
import java.awt.AlphaComposite;
import java.awt.LayoutManager;
import java.awt.Component;

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
                frame.gamePanel.stopGameThread();
                frame.getCredits().stopTimer();
                frame.openPanel.stopBackgroundAnimation();
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
                frame.openPanel.stopBackgroundAnimation();
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

    //custom panels for the buttons
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

            //makes the drawing smooth
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(color);

            //fill the entire panel with the rounded rect
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            // //make a line border
            // g2.setColor(Color.BLACK);
            // g2.drawRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        }
    }

    public static class CustomButton extends JButton {
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
            
            try (InputStream font = new FileInputStream("res/Font/texts.ttf")) {
                
                Font textFont = Font.createFont(Font.TRUETYPE_FONT, font);
                textFont = textFont.deriveFont(Font.BOLD, 20f);

                this.setFont(textFont);
                this.setAlignmentX(Component.CENTER_ALIGNMENT);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(temp);
            g2.fillRect(0, 0, getWidth(), getHeight());

            super.paintComponent(g2);
            g2.dispose();
        }
        
    }
}
