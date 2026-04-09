package panels;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Color;

public class ScoreboardDialog extends JDialog {

    private JLabel timeScoreLabel;
    private JLabel enemyScoreLabel;
    private JLabel levelsClearedLabel;
    private JLabel totalScoreLabel;

    public ScoreboardDialog(java.awt.Frame parent) {
        super(parent, "Scoreboard", true);
        setSize(300, 200);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        timeScoreLabel = new JLabel("Time Score: 0");
        timeScoreLabel.setFont(new Font("Arial", Font.BOLD, 14));
        timeScoreLabel.setForeground(Color.BLUE);

        enemyScoreLabel = new JLabel("Enemy Score: 0 (Placeholder)");
        enemyScoreLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        levelsClearedLabel = new JLabel("Levels Cleared: 0");
        levelsClearedLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        totalScoreLabel = new JLabel("Total Score: 0 (Placeholder)");
        totalScoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalScoreLabel.setForeground(Color.RED);

        panel.add(timeScoreLabel);
        panel.add(enemyScoreLabel);
        panel.add(levelsClearedLabel);
        panel.add(totalScoreLabel);

        add(panel, BorderLayout.CENTER);
    }

    public void updateScores(int timeScore, int enemyScore, int levelsCleared, int totalScore) {
        timeScoreLabel.setText("Time Score: " + timeScore);
        enemyScoreLabel.setText("Enemy Score: " + enemyScore + " (Placeholder)");
        levelsClearedLabel.setText("Levels Cleared: " + levelsCleared);
        totalScoreLabel.setText("Total Score: " + totalScore + " (Placeholder)");
    }
}