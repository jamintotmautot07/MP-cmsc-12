package panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import systems.ScoreManager;
import util.MethodUtilities;
import util.MethodUtilities.RoundedPanel;

/**
 * Small summary panel for time score, enemy score, and level progress.
 * (Converted from JDialog to JPanel for CardLayout-based navigation.)
 */
public class ScoreboardDialog extends JPanel {

    // Score labels are stored as fields so they can be updated whenever the panel is shown.
    private JLabel timeScoreLabel;
    private JLabel enemyScoreLabel;
    private JLabel enemiesEliminatedLabel;
    private JLabel levelsClearedLabel;
    private JLabel totalScoreLabel;

    private Font textFont;

    /**
     * Listener so BaseFrame can control navigation (back to menu, etc.)
     */
    public interface ScoreboardListener {
        void onBack();
    }

    private ScoreboardListener listener;

    public void setListener(ScoreboardListener listener) {
        this.listener = listener;
    }

    /**
     * Builds the scoreboard panel UI.
     */
    public ScoreboardDialog() {

        setLayout(new BorderLayout());

        textFont = MethodUtilities.getFont(16f);

        // Simple stacked layout for a compact score summary.
        RoundedPanel panel = new RoundedPanel(new GridLayout(5, 1, 10, 10), 10);
        panel.setColor(new Color(159, 188, 143).darker());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        timeScoreLabel = new JLabel("Time Score: 0");
        timeScoreLabel.setFont(textFont);
        timeScoreLabel.setForeground(Color.BLUE);

        enemyScoreLabel = new JLabel("Enemy Score: 0");
        enemyScoreLabel.setFont(textFont);

        enemiesEliminatedLabel = new JLabel("Enemies Eliminated: 0");
        enemiesEliminatedLabel.setFont(textFont);

        levelsClearedLabel = new JLabel("Levels Cleared: 0");
        levelsClearedLabel.setFont(textFont);

        totalScoreLabel = new JLabel("Total Score: 0");
        totalScoreLabel.setFont(MethodUtilities.getFont(20f));
        totalScoreLabel.setForeground(Color.RED);

        panel.add(timeScoreLabel);
        panel.add(enemyScoreLabel);
        panel.add(enemiesEliminatedLabel);
        panel.add(levelsClearedLabel);
        panel.add(totalScoreLabel);

        add(panel, BorderLayout.CENTER);

        // Back button (replaces dialog close behavior)
        MethodUtilities.CustomButton backButton =
                new MethodUtilities.CustomButton("Back");

        backButton.addActionListener(e -> {
            if (listener != null) {
                listener.onBack();
            }
        });

        // Bottom container for navigation buttons
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(backButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Refreshes every label before the panel is shown.
     */
    public void updateScores(ScoreManager scoreManager) {
        // Refresh all labels in one place before showing the panel.
        timeScoreLabel.setText("Time Score: " + scoreManager.getTimeScore());
        enemyScoreLabel.setText("Enemy Score: " + scoreManager.getEnemyScore());
        enemiesEliminatedLabel.setText("Enemies Eliminated: " + scoreManager.getEnemiesEliminated());
        levelsClearedLabel.setText("Levels Cleared: " + scoreManager.getLevelsCleared());
        totalScoreLabel.setText("Total Score: " + scoreManager.calculateTotalScore());
    }
}