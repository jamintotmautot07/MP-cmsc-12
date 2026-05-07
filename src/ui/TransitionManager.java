package ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;

import util.Constants;

/**
 * Lightweight fade-to-black transition with a callback at full opacity.
 */
public class TransitionManager {
    private enum FadeState {
        NONE,
        FADE_OUT,
        FADE_IN
    }

    private static final int FADE_OUT_MS = 450;
    private static final int FADE_IN_MS = 450;

    private FadeState state = FadeState.NONE;
    private long stateStartTime;
    private float alpha;
    private Runnable onFadeOutComplete;

    public void start(Runnable onFadeOutComplete) {
        this.onFadeOutComplete = onFadeOutComplete;
        this.state = FadeState.FADE_OUT;
        this.stateStartTime = System.currentTimeMillis();
        this.alpha = 0.0f;
    }

    public void update() {
        if (state == FadeState.NONE) {
            return;
        }

        long elapsed = System.currentTimeMillis() - stateStartTime;
        if (state == FadeState.FADE_OUT) {
            alpha = Math.min(1.0f, elapsed / (float) FADE_OUT_MS);
            if (alpha >= 1.0f) {
                Runnable callback = onFadeOutComplete;
                onFadeOutComplete = null;
                if (callback != null) {
                    callback.run();
                }
                state = FadeState.FADE_IN;
                stateStartTime = System.currentTimeMillis();
            }
        } else if (state == FadeState.FADE_IN) {
            alpha = 1.0f - Math.min(1.0f, elapsed / (float) FADE_IN_MS);
            if (alpha <= 0.0f) {
                alpha = 0.0f;
                state = FadeState.NONE;
            }
        }
    }

    public boolean isActive() {
        return state != FadeState.NONE;
    }

    public void draw(Graphics2D g2) {
        if (alpha <= 0.0f) {
            return;
        }

        java.awt.Composite oldComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, Constants.screenWidth, Constants.screenHeight);
        g2.setComposite(oldComposite);
    }
}
