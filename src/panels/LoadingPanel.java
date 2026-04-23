package panels;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.AlphaComposite;

import util.Constants;

/*
 PURPOSE:
 - Display loading screen while resources are preloading
 - Shows progress bar and status messages
 - Appears at startup before opening panel
*/

public class LoadingPanel extends JPanel {

    // possible implementation... incomplete and unimplemented
    // although possible implementations are already added but still commmented

    // private JProgressBar progressBar;
    // private JLabel statusLabel;
    // private JLabel titleLabel;
    // private JLabel subtitleLabel;

    // public LoadingPanel() {
    //     setPreferredSize(new Dimension(Constants.screenWidth, Constants.screenHeight));
    //     setLayout(new BorderLayout());
    //     setBackground(new Color(15, 15, 20)); // Dark background
        
    //     // Title label - Upper
    //     titleLabel = new JLabel("Hawak ko ang Bit:", JLabel.CENTER);
    //     titleLabel.setForeground(new Color(153, 204, 255));
    //     titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        
    //     // Title label - Lower
    //     subtitleLabel = new JLabel("THE FINAL BIT", JLabel.CENTER);
    //     subtitleLabel.setForeground(new Color(255, 153, 51));
    //     subtitleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        
    //     // Status label
    //     statusLabel = new JLabel("Loading resources...", JLabel.CENTER);
    //     statusLabel.setForeground(new Color(200, 200, 200));
    //     statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
    //     // Progress bar with custom styling
    //     progressBar = new JProgressBar(0, 100);
    //     progressBar.setStringPainted(true);
    //     progressBar.setForeground(new Color(255, 153, 51));
    //     progressBar.setBackground(new Color(40, 40, 50));
    //     progressBar.setFont(new Font("Arial", Font.BOLD, 12));
    //     progressBar.setBorderPainted(false);
    //     progressBar.setOpaque(true);
    //     progressBar.setPreferredSize(new Dimension(400, 25));
        
    //     // Layout - Top panel with titles
    //     JPanel topPanel = new JPanel();
    //     topPanel.setBackground(new Color(15, 15, 20));
    //     topPanel.setLayout(new java.awt.GridLayout(2, 1));
    //     topPanel.add(titleLabel);
    //     topPanel.add(subtitleLabel);
    //     topPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(60, 0, 40, 0));
        
    //     // Center panel with progress
    //     JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
    //     centerPanel.setBackground(new Color(15, 15, 20));
    //     centerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(40, 80, 80, 80));
    //     centerPanel.add(statusLabel, BorderLayout.NORTH);
    //     centerPanel.add(progressBar, BorderLayout.CENTER);
        
    //     add(topPanel, BorderLayout.NORTH);
    //     add(centerPanel, BorderLayout.CENTER);
    // }
    
    // @Override
    // protected void paintComponent(Graphics g) {
    //     super.paintComponent(g);
    //     // Add subtle gradient effect in background
    //     Graphics2D g2 = (Graphics2D) g;
    //     g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    // }
    
    // /**
    //  * Update progress bar and status message
    //  */
    // public void setProgress(int value, String message) {
    //     progressBar.setValue(Math.min(value, 100));
    //     statusLabel.setText(message);
    //     repaint();
    // }
    
    // /**
    //  * Get progress bar for manual control if needed
    //  */
    // public JProgressBar getProgressBar() {
    //     return progressBar;
    // }
}
