package panels;
import javax.swing.Timer;
import javax.swing.JPanel;
import javax.swing.JButton;

import java.awt.Graphics;
import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import util.Constants;
import util.MethodUtilities.CustomButton;

/**
 * Simple auto-scrolling credits screen.
 */
public class CreditScroller extends JPanel implements ActionListener {
    // Vertical offset of the first credit line.
    private int y; // Start below the panel
    private Timer timer;
    private CustomButton backButton;

    // Credits are stored as plain strings and drawn one line at a time.
    private final String[] credits = {
        "MEMBERS:", "", "   UI:", "     JAMIN", "",  "   MUSIC:", "     INOY", "", "   FILE:", "     ALTHEA", "", "   MOVEMENT:", "     ALLAN"
    };

    /**
     * Builds the credits panel and its scroll timer.
     */
    public CreditScroller() {
        setPreferredSize(new Dimension(Constants.screenWidth, Constants.screenHeight));
        setLayout(null);
        
        backButton = new CustomButton("Back");
        backButton.setBounds(10, 10, 80, 30);
        add(backButton);
        
        // Swing timer is enough for a simple scrolling text effect.
        timer = new Timer(8, this); // ~30 FPS
    }

    /**
     * Starts the credits scroll from the bottom.
     */
    public void startTimer() {
        // Reset scroll position whenever the credits screen opens.
        this.y = 600;
        timer.start();
    }
    
    /**
     * Stops the credits scroll timer.
     */
    public void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }
    
    /**
     * Lets the outer frame attach its own navigation behavior.
     */
    public JButton getBackButton() {
        return backButton;
    }
    
    @Override
    public void removeNotify() {
        stopTimer();
        super.removeNotify();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw a clean black backdrop so the white credit text stays readable.
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));

        // Each repaint places the lines slightly higher than last time, creating the scroll illusion.
        int tempY = y;
        for (String line : credits) {
            g.drawString(line, getWidth() / 2 - 50, tempY);
            tempY += 30; // Space between lines
        }
    }

    @Override
    /**
     * Advances the scroll position and requests repaint.
     */
    public void actionPerformed(ActionEvent e) {
        y--; // Scroll speed
        if (y < -400) y = getHeight(); // Loop back to the bottom once everything scrolls off-screen.
        repaint();
    }
}
