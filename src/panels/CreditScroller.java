package panels;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.Timer;
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
        "MEMBERS:", 
        "", 
        "UI and Game Mechanics:", 
        "Benjamin Abad Deypalan", 
        "", 
        "Audio and Resource Caching:", 
        "Constantino Tajantajan Cesista Jr.", 
        "", 
        "File Saving:",
        "Althea Kate Silvano",
        "", 
        "MOVEMENT:", 
        "Allan II Eamiguel Lerios"
    };

    /**
     * Builds the credits panel and its scroll timer.
     */
    public CreditScroller() {
        setPreferredSize(new Dimension(Constants.screenWidth, Constants.screenHeight));
        setLayout(new BorderLayout());
        
        backButton = new CustomButton("Back");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.NORTH);
        
        // Swing timer is enough for a simple scrolling text effect.
        timer = new Timer(8, this); // ~30 FPS
    }

    /**
     * Starts the credits scroll from the bottom.
     */
    public void startTimer() {
        // Reset scroll position whenever the credits screen opens.
        this.y = getHeight() > 0 ? getHeight() : Constants.screenHeight;
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
    /**
     * Stops the timer when Swing removes this panel from the display tree.
     */
    public void removeNotify() {
        stopTimer();
        super.removeNotify();
    }

    @Override
    /**
     * Draws the scrolling credits text over a black background.
     */
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
            int textWidth = g.getFontMetrics().stringWidth(line);
            g.drawString(line, (getWidth() - textWidth) / 2, tempY);
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
