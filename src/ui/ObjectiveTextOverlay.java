package ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import util.Constants;
import util.MethodUtilities;

/**
 * Displays a short center-screen objective with fade-in, hold, and fade-out timing.
 */
public class ObjectiveTextOverlay {
    private static final int FADE_IN_MS = 500;
    private static final int HOLD_MS = 1000;
    private static final int FADE_OUT_MS = 500;
    private static final int TOTAL_MS = FADE_IN_MS + HOLD_MS + FADE_OUT_MS;

    private String text;
    private long startTime;
    private boolean active;

    /**
     * Starts a new objective message and resets its fade timer.
     */
    public void show(String text) {
        this.text = text;
        this.startTime = System.currentTimeMillis();
        this.active = text != null && !text.isEmpty();
    }

    /**
     * Deactivates the overlay once its full fade/hold duration has passed.
     */
    public void update() {
        if (active && System.currentTimeMillis() - startTime >= TOTAL_MS) {
            active = false;
        }
    }

    /**
     * Draws the objective text with a time-based alpha fade.
     */
    public void draw(Graphics2D g2) {
        if (!active || text == null) {
            return;
        }

        long elapsed = System.currentTimeMillis() - startTime;
        float alpha;
        if (elapsed < FADE_IN_MS) {
            alpha = elapsed / (float) FADE_IN_MS;
        } else if (elapsed < FADE_IN_MS + HOLD_MS) {
            alpha = 1.0f;
        } else {
            alpha = 1.0f - ((elapsed - FADE_IN_MS - HOLD_MS) / (float) FADE_OUT_MS);
        }

        alpha = Math.max(0.0f, Math.min(1.0f, alpha));
        Composite oldComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        g2.setFont(MethodUtilities.getFont(38f));
        FontMetrics metrics = g2.getFontMetrics();
        int textWidth = metrics.stringWidth(text);
        int x = (Constants.screenWidth - textWidth) / 2;
        int y = Constants.screenHeight / 2;

        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRoundRect(x - 28, y - metrics.getAscent() - 18, textWidth + 56, metrics.getHeight() + 36, 18, 18);
        g2.setColor(new Color(70, 245, 155));
        g2.drawString(text, x, y);
        g2.setColor(new Color(180, 245, 255, 180));
        g2.drawRoundRect(x - 28, y - metrics.getAscent() - 18, textWidth + 56, metrics.getHeight() + 36, 18, 18);

        g2.setComposite(oldComposite);
    }
}
