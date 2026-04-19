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

public class CreditScroller extends JPanel implements ActionListener {
    private int y; // Start below the panel
    private Timer timer;
    private CustomButton backButton;
    private final String[] credits = {
        "MEMBERS:", "", "   UI:", "     JAMIN", "",  "   MUSIC:", "     INOY", "", "   FILE:", "     ALTHEA", "", "   MOVEMENT:", "     ALLAN"
    };

    public CreditScroller() {
        setPreferredSize(new Dimension(Constants.screenWidth, Constants.screenHeight));
        setLayout(null);
        
        backButton = new CustomButton("Back");
        backButton.setBounds(10, 10, 80, 30);
        add(backButton);
        
        timer = new Timer(8, this); // ~30 FPS
    }

    public void startTimer() {
        this.y = 600;
        timer.start();
    }
    
    public void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }
    
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
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));

        // Draw credits
        int tempY = y;
        for (String line : credits) {
            g.drawString(line, getWidth() / 2 - 50, tempY);
            tempY += 30; // Space between lines
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        y--; // Scroll speed
        if (y < -400) y = getHeight(); // Reset loop
        repaint();
    }
}
