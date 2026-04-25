package panels;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

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
    private JLabel levelsClearedLabel;
    private JLabel totalScoreLabel;

    private Font textFont;

    /**
     * Builds the scoreboard dialog UI.
     */
    public ScoreboardDialog(java.awt.Frame parent) {
        super(parent, "Scoreboard", true);
        setSize(300, 200);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        textFont = MethodUtilities.getFont(16f);

        // Simple stacked layout for a compact score summary.
        RoundedPanel panel = new RoundedPanel(new GridLayout(4, 1, 10, 10), 10);
        panel.setColor(new Color(159, 188, 143).darker());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        timeScoreLabel = new JLabel("Time Score: 0");
        timeScoreLabel.setFont(textFont);
        timeScoreLabel.setForeground(Color.BLUE);

        enemyScoreLabel = new JLabel("Enemy Score: 0 (Placeholder)");
        enemyScoreLabel.setFont(textFont);

        levelsClearedLabel = new JLabel("Levels Cleared: 0");
        levelsClearedLabel.setFont(textFont);

        totalScoreLabel = new JLabel("Total Score: 0 (Placeholder)");
        totalScoreLabel.setFont(MethodUtilities.getFont(20f));
        totalScoreLabel.setForeground(Color.RED);

        panel.add(timeScoreLabel);
        panel.add(enemyScoreLabel);
        panel.add(levelsClearedLabel);
        panel.add(totalScoreLabel);

        add(panel, BorderLayout.CENTER);
    }

    /**
     * Refreshes every label before the dialog is shown.
     */
    public void updateScores(int timeScore, int enemyScore, int levelsCleared, int totalScore) {
        // Refresh all labels in one place before showing the dialog.
        timeScoreLabel.setText("Time Score: " + timeScore);
        enemyScoreLabel.setText("Enemy Score: " + enemyScore + " (Placeholder)");
        levelsClearedLabel.setText("Levels Cleared: " + levelsCleared);
        totalScoreLabel.setText("Total Score: " + totalScore + " (Placeholder)");
    }
}
