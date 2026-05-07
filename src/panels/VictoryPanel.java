package panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import systems.ScoreManager;
import util.Constants;
import util.MethodUtilities;
import util.MethodUtilities.CustomButton;

/**
 * Custom ending screen with a restored-system background, pixel confetti, and animated total score.
 */
public class VictoryPanel extends JPanel {
    private static final int CONFETTI_COUNT = 72;
    private static final int TOTAL_SCORE_DURATION_MS = 2200;

    private final Pixel[] pixels = new Pixel[CONFETTI_COUNT];
    private final Random random = new Random();
    private final JLabel timeScoreLabel = new JLabel();
    private final JLabel enemyScoreLabel = new JLabel();
    private final JLabel enemiesEliminatedLabel = new JLabel();
    private final JLabel levelsClearedLabel = new JLabel();
    private final JLabel totalScoreLabel = new JLabel("Total Score: 0");
    private final JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 12, 12));
    private final Timer animationTimer;

    private ScoreManager scoreManager;
    private long totalScoreStartTime;
    private boolean buttonsShown;

    public VictoryPanel(Runnable onReturnToMenu, Runnable onPlayAgain, Runnable onExit) {
        setOpaque(true);
        setPreferredSize(new Dimension(Constants.screenWidth, Constants.screenHeight));
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(46, 70, 46, 70));
        createPixels();
        createContent(onReturnToMenu, onPlayAgain, onExit);

        animationTimer = new Timer(16, e -> {
            updatePixels();
            updateTotalScore();
            repaint();
        });
    }

    public void showScores(ScoreManager scoreManager) {
        this.scoreManager = scoreManager;
        this.totalScoreStartTime = System.currentTimeMillis();
        this.buttonsShown = false;
        buttonPanel.setVisible(false);

        timeScoreLabel.setText("Time Score: " + scoreManager.getTimeScore());
        enemyScoreLabel.setText("Enemy Score: " + scoreManager.getEnemyScore());
        enemiesEliminatedLabel.setText("Enemies Eliminated: " + scoreManager.getEnemiesEliminated());
        levelsClearedLabel.setText("Levels Cleared: " + scoreManager.getLevelsCleared());
        totalScoreLabel.setText("Total Score: 0");

        if (!animationTimer.isRunning()) {
            animationTimer.start();
        }
    }

    public void stopAnimation() {
        if (animationTimer.isRunning()) {
            animationTimer.stop();
        }
    }

    private void createContent(Runnable onReturnToMenu, Runnable onPlayAgain, Runnable onExit) {
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("SYSTEM RESTORED", JLabel.CENTER);
        title.setAlignmentX(CENTER_ALIGNMENT);
        title.setForeground(new Color(120, 255, 190));
        title.setFont(MethodUtilities.getFont(42f));

        JLabel subtitle = new JLabel("The Final Bit has restored the system.", JLabel.CENTER);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);
        subtitle.setForeground(new Color(220, 245, 255));
        subtitle.setFont(MethodUtilities.getFont(18f));

        JPanel scorePanel = new JPanel(new GridLayout(5, 1, 8, 8));
        scorePanel.setOpaque(false);
        scorePanel.setBorder(BorderFactory.createEmptyBorder(38, 120, 24, 120));

        JLabel[] labels = {
            timeScoreLabel,
            enemyScoreLabel,
            enemiesEliminatedLabel,
            levelsClearedLabel,
            totalScoreLabel
        };
        for (JLabel label : labels) {
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setForeground(new Color(235, 250, 255));
            label.setFont(MethodUtilities.getFont(label == totalScoreLabel ? 25f : 18f));
            scorePanel.add(label);
        }
        totalScoreLabel.setForeground(new Color(255, 235, 120));

        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 120, 0, 120));
        CustomButton menuButton = new CustomButton("Return to Menu");
        CustomButton playAgainButton = new CustomButton("Play Again");
        CustomButton exitButton = new CustomButton("Exit");
        menuButton.addActionListener(e -> onReturnToMenu.run());
        playAgainButton.addActionListener(e -> onPlayAgain.run());
        exitButton.addActionListener(e -> onExit.run());
        buttonPanel.add(menuButton);
        buttonPanel.add(playAgainButton);
        buttonPanel.add(exitButton);
        buttonPanel.setVisible(false);

        content.add(Box.createVerticalGlue());
        content.add(title);
        content.add(Box.createRigidArea(new Dimension(0, 8)));
        content.add(subtitle);
        content.add(scorePanel);
        content.add(buttonPanel);
        content.add(Box.createVerticalGlue());
        add(content, BorderLayout.CENTER);
    }

    private void createPixels() {
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = new Pixel();
            resetPixel(pixels[i], true);
        }
    }

    private void updatePixels() {
        for (Pixel pixel : pixels) {
            pixel.y += pixel.speed;
            pixel.drift += pixel.driftSpeed;
            if (pixel.y > Constants.screenHeight + 12) {
                resetPixel(pixel, false);
            }
        }
    }

    private void updateTotalScore() {
        if (scoreManager == null) {
            return;
        }

        int finalTotal = scoreManager.calculateTotalScore();
        long elapsed = System.currentTimeMillis() - totalScoreStartTime;
        float progress = Math.min(1.0f, elapsed / (float) TOTAL_SCORE_DURATION_MS);
        float eased = 1.0f - (float) Math.pow(1.0f - progress, 3);
        int displayedScore = Math.round(finalTotal * eased);
        totalScoreLabel.setText("Total Score: " + displayedScore);

        if (progress >= 1.0f && !buttonsShown) {
            buttonsShown = true;
            buttonPanel.setVisible(true);
            revalidate();
        }
    }

    private void resetPixel(Pixel pixel, boolean anywhere) {
        Color[] colors = {
            new Color(90, 255, 150),
            new Color(75, 190, 255),
            new Color(255, 245, 150),
            new Color(245, 245, 255),
            new Color(255, 95, 95)
        };

        pixel.x = random.nextInt(Constants.screenWidth);
        pixel.y = anywhere ? random.nextInt(Constants.screenHeight) : -random.nextInt(90) - 8;
        pixel.size = 3 + random.nextInt(5);
        pixel.speed = 1 + random.nextInt(3);
        pixel.drift = random.nextFloat() * 6.28f;
        pixel.driftSpeed = 0.015f + random.nextFloat() * 0.035f;
        pixel.color = colors[random.nextInt(colors.length)];
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawRestoredBackground(g2);
        drawConfetti(g2);
        g2.dispose();
    }

    private void drawRestoredBackground(Graphics2D g2) {
        g2.setColor(new Color(5, 18, 28));
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(new Color(14, 95, 118, 90));
        for (int x = 0; x < getWidth(); x += 32) {
            g2.drawLine(x, 0, x, getHeight());
        }
        for (int y = 0; y < getHeight(); y += 32) {
            g2.drawLine(0, y, getWidth(), y);
        }

        g2.setColor(new Color(60, 245, 160, 70));
        g2.drawRoundRect(48, 38, getWidth() - 96, getHeight() - 76, 18, 18);

        g2.setFont(MethodUtilities.getFont(13f));
        g2.setColor(new Color(125, 235, 220, 95));
        String footer = "CORE STATUS: STABLE";
        FontMetrics metrics = g2.getFontMetrics();
        g2.drawString(footer, (getWidth() - metrics.stringWidth(footer)) / 2, getHeight() - 24);
    }

    private void drawConfetti(Graphics2D g2) {
        for (Pixel pixel : pixels) {
            int drawX = Math.round(pixel.x + (float) Math.sin(pixel.drift) * 10);
            g2.setColor(pixel.color);
            g2.fillRect(drawX, pixel.y, pixel.size, pixel.size);
        }
    }

    private static class Pixel {
        private int x;
        private int y;
        private int size;
        private int speed;
        private float drift;
        private float driftSpeed;
        private Color color;
    }
}
