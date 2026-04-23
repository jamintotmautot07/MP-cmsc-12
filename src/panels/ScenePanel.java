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

    private final IntroManager introManager;
    private Timer updateTimer;
    private Runnable onSceneComplete;

    public ScenePanel(IntroManager introManager) {
        this.introManager = introManager;
        setPreferredSize(new Dimension(Constants.screenWidth, Constants.screenHeight));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        updateTimer = new Timer(16, e -> {
            if (introManager.isRunning()) {
                introManager.update();
            }
            repaint();

            if (introManager.isFinished()) {
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
        introManager.render(g);

        if (introManager.isRunning()) {
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
        if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
            introManager.skip();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    public void stopScene() {
        if (updateTimer != null && updateTimer.isRunning()) {
            updateTimer.stop();
        }
    }
}
