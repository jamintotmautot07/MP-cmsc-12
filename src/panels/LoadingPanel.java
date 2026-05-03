package panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import ui.PreloadProgressBar;
import util.Constants;
import util.ResourceCache;

/*
 PURPOSE:
 - Display loading screen while resources are preloading
 - Shows progress bar and status messages
 - Appears at startup before opening panel
*/

public class LoadingPanel extends JPanel {

    /*
     * This panel is intentionally left as a commented template.
     * It documents one possible loading-screen approach without enabling it in the current build.
     */

    // possible implementation... incomplete and unimplemented
    // although possible implementations are already added but still commmented

    private PreloadProgressBar progressBar;
    private JLabel statusLabel;
    private JLabel titleLabel;
    private JLabel subtitleLabel;

    public LoadingPanel() {
        setPreferredSize(new Dimension(Constants.screenWidth, Constants.screenHeight));
        setLayout(new BorderLayout());
        setBackground(new Color(15, 15, 20)); // Dark background
        
        // Title label - Upper
        titleLabel = new JLabel("Hawak ko ang Bit:", JLabel.CENTER);
        titleLabel.setForeground(new Color(153, 204, 255));
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        
        // Title label - Lower
        subtitleLabel = new JLabel("THE FINAL BIT", JLabel.CENTER);
        subtitleLabel.setForeground(new Color(255, 153, 51));
        subtitleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        
        // Status label
        statusLabel = new JLabel("Loading resources...", JLabel.CENTER);
        statusLabel.setForeground(new Color(200, 200, 200));
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        // Progress bar with custom styling
        progressBar = new PreloadProgressBar(0, 100);
        
        // Layout - Top panel with titles
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(15, 15, 20));
        topPanel.setLayout(new java.awt.GridLayout(2, 1));
        topPanel.add(titleLabel);
        topPanel.add(subtitleLabel);
        topPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(60, 0, 40, 0));
        
        // Center panel with progress
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(new Color(15, 15, 20));
        centerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(40, 80, 80, 80));
        centerPanel.add(statusLabel, BorderLayout.NORTH);
        centerPanel.add(progressBar, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Add subtle gradient effect in background
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    public void startLoading(Runnable onDone) {
        SwingWorker<Void, ProgressUpdate> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                ResourceCache.preloadAll((value, message) -> {
                    publish(new ProgressUpdate(value, message));
                });
                return null;
            }

            @Override
            protected void process(java.util.List<ProgressUpdate> chunks) {
                ProgressUpdate latest = chunks.get(chunks.size() - 1);

                progressBar.setValue(latest.value);
                statusLabel.setText(latest.message);
            }

            @Override
            protected void done() {
                 progressBar.setValue(100);
                statusLabel.setText("Loading complete!");

                javax.swing.Timer timer = new javax.swing.Timer(1000, e -> {
                    ((javax.swing.Timer) e.getSource()).stop();

                    if (onDone != null) {
                        onDone.run();
                    }
                });

                timer.setRepeats(false);
                timer.start();
            }
        };

        worker.execute();
    }

    private static class ProgressUpdate {
        int value;
        String message;

        ProgressUpdate(int value, String message) {
            this.value = value;
            this.message = message;
        }
    }
}
