package panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;
import javax.swing.Timer;

import ui.IntroManager;
import util.Constants;

public class ScenePanel extends JPanel implements KeyListener {
    private static final long serialVersionUID = 1L;

    // ScenePanel is a lightweight wrapper around IntroManager that gives it a Swing panel and timer.
    private final IntroManager introManager;
    private Timer updateTimer;
    private Runnable onSceneComplete;

    public ScenePanel(IntroManager introManager) {
        this.introManager = introManager;
        setPreferredSize(new Dimension(Constants.screenWidth, Constants.screenHeight));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        // A Swing timer is enough here because cutscenes are mostly timed image swaps.
        updateTimer = new Timer(16, e -> {
            if (introManager.isRunning()) {
                introManager.update();
            }
            repaint();

            if (introManager.isFinished()) {
                // Once done, stop polling and notify whoever launched the scene.
                updateTimer.stop();
                if (onSceneComplete != null) {
                    onSceneComplete.run();
                }
            }
        });
    }

    public void setOnSceneComplete(Runnable callback) {
        this.onSceneComplete = callback;
    }

    public void startScene(String sceneId, String filePattern, int frameCount, int frameDelayMs) {
        boolean started = introManager.startScene(sceneId, filePattern, frameCount, frameDelayMs);
        if (!started) {
            // If the scene was skipped because it already played, continue immediately.
            if (onSceneComplete != null) {
                onSceneComplete.run();
            }
            return;
        }

        if (!updateTimer.isRunning()) {
            updateTimer.start();
        }

        requestFocusInWindow();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Let IntroManager render the actual scene frame first.
        introManager.render(g);

        if (introManager.isRunning()) {
            // Small hint so the user knows cutscenes are skippable.
            g.setColor(new Color(255, 255, 255, 200));
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            String text = "Press ESC to skip";
            int textWidth = g.getFontMetrics().stringWidth(text);
            g.drawString(text, (getWidth() - textWidth) / 2, getHeight() - 40);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        // A few common "continue/skip" keys all map to the same behavior.
        if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
            introManager.skip();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    public void stopScene() {
        // Safe cleanup helper used when the whole app is closing or switching away abruptly.
        if (updateTimer != null && updateTimer.isRunning()) {
            updateTimer.stop();
        }
    }
}
