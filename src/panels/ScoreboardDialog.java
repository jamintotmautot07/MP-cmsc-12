package panels;

import javax.swing.JDialog;
import javax.swing.JLabel;

import util.MethodUtilities;
import util.MethodUtilities.RoundedPanel;

import javax.swing.BorderFactory;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Color;

/**
 * Small summary dialog for time score, enemy score, and level progress.
 */
public class ScoreboardDialog extends JDialog {

    // Score labels are stored as fields so they can be updated whenever the dialog is shown.
    private JLabel timeScoreLabel;
    private JLabel enemyScoreLabel;
    private JLabel enemiesEliminatedLabel;
    private JLabel levelsClearedLabel;
    private JLabel totalScoreLabel;

    private Font textFont;

    /**
     * Builds the scoreboard dialog UI.
     */
    public ScoreboardDialog(java.awt.Frame parent) {
        super(parent, "Scoreboard", true);
        setSize(340, 245);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

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
    }

    /**
     * Refreshes every label before the dialog is shown.
     */
    public void updateScores(int timeScore, int enemyScore, int enemiesEliminated, int levelsCleared, int totalScore) {
        // Refresh all labels in one place before showing the dialog.
        timeScoreLabel.setText("Time Score: " + timeScore);
        enemyScoreLabel.setText("Enemy Score: " + enemyScore);
        enemiesEliminatedLabel.setText("Enemies Eliminated: " + enemiesEliminated);
        levelsClearedLabel.setText("Levels Cleared: " + levelsCleared);
        totalScoreLabel.setText("Total Score: " + totalScore);
    }
}
