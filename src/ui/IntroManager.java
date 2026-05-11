package ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import util.Constants;
import util.ResourceCache;

/*
 OWNER: Jamin

 PURPOSE:
 - Handles a scene sequence of frames/images
 - Supports intro, cutscene, or any cinematic scene
 - Plays each frame on a timer and allows skipping
 - Tracks scenes played once per app session

 USAGE:
 - startScene(sceneId, pattern, count, delayMs)
 - update() every frame
 - render(g) every paint
 - skip() when player requests it
 - isFinished() to detect completion
*/
/**
 * Scene player for intro/cutscene image sequences, including once-per-session playback tracking.
 */
public class IntroManager {

    // Scene playback state.
    private String activeSceneId;
    private String filePattern;
    private int frameCount;
    private int currentFrame;
    private long lastFrameTime;
    private int frameDelay;
    private boolean finished;

    // Tracks which named scenes were already played during this app session.
    private final Set<String> completedScenes;

    // Fade-to-color values used when the sequence ends or gets skipped.
    private boolean isFading;
    private float fadeProgress;
    private long fadeStartTime;
    private int fadeDurationMs;
    private Color fadeColor;

    /**
     * Creates a manager with no active scene and default fade settings.
     */
    public IntroManager() {
        this.currentFrame = 0;
        this.frameCount = 0;
        this.lastFrameTime = System.currentTimeMillis();
        this.frameDelay = 80;
        this.finished = true;
        this.completedScenes = new HashSet<>();
        this.isFading = false;
        this.fadeProgress = 0.0f;
        this.fadeDurationMs = 1000; // 0.5 seconds
        this.fadeColor = Color.BLACK;
    }

    /**
     * Returns whether the named scene already finished once this app session.
     */
    public boolean hasPlayed(String sceneId) {
        return completedScenes.contains(sceneId);
    }

    /**
     * Starts loading and playing a scene sequence.
     */
    public boolean startScene(String sceneId, String filePattern, int frameCount, int frameDelayMs) {
        if (sceneId == null || filePattern == null || frameCount <= 0) {
            return false;
        }

        // Reset playback state for a fresh run.
        this.activeSceneId = sceneId;
        this.filePattern = filePattern;
        this.frameCount = frameCount;
        this.frameDelay = Math.max(frameDelayMs, 16);
        this.currentFrame = 0;
        this.finished = false;
        this.isFading = false;
        this.fadeProgress = 0.0f;
        this.lastFrameTime = System.currentTimeMillis();
        ResourceCache.clearSceneFrames();
        return true;
    }

    /**
     * Advances playback or fade state by one frame.
     */
    public void update() {
        if (finished || frameCount <= 0) {
            return;
        }

        long now = System.currentTimeMillis();

        if (isFading) {
            // Fade progress moves from 0.0 to 1.0 over `fadeDurationMs`.
            long elapsed = now - fadeStartTime;
            fadeProgress = Math.min(1.0f, (float) elapsed / fadeDurationMs);
            if (fadeProgress >= 1.0f) {
                finishScene();
            }
        } else {
            if (now - lastFrameTime >= frameDelay) {
                currentFrame++;
                lastFrameTime = now;

                if (currentFrame >= frameCount) {
                    // When the last frame ends, switch into fade mode.
                    isFading = true;
                    fadeStartTime = now;
                    fadeProgress = 0.0f;
                    currentFrame = Math.max(0, frameCount - 1); // Stay on last frame
                }
            }
        }
    }

    /**
     * Draws the current frame plus fade overlay.
     */
    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        BufferedImage frame = getCurrentSceneFrame();
        if (frame != null) {
            g2d.drawImage(frame, 0, 0, Constants.screenWidth, Constants.screenHeight, null);
        } else {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, Constants.screenWidth, Constants.screenHeight);
        }

        // Overlay the fade after the frame so it darkens the whole scene evenly.
        if (isFading) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeProgress));
            g2d.setColor(fadeColor);
            g2d.fillRect(0, 0, Constants.screenWidth, Constants.screenHeight);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)); // Reset composite
        }
    }

    /**
     * First call starts the end fade; second call finishes immediately.
     */
    public void skip() {
        if (finished) {
            return;
        }

        if (!isFading) {
            // First skip jumps to the end frame and begins the fade.
            isFading = true;
            fadeStartTime = System.currentTimeMillis();
            fadeProgress = 0.0f;
            currentFrame = Math.max(0, frameCount - 1); // Jump to last frame
        } else {
            // A second skip means "don't even wait for the fade".
            fadeProgress = 1.0f;
            finishScene();
        }
    }

    /**
     * Indicates whether the current scene is fully done.
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Indicates whether a scene is actively playing or fading out.
     */
    public boolean isRunning() {
        return !finished && (frameCount > 0 || isFading);
    }

    /**
     * Rewinds the current scene state without reloading the frame assets.
     */
    public void reset() {
        // Reset keeps the loaded frames but rewinds playback values.
        this.currentFrame = 0;
        this.lastFrameTime = System.currentTimeMillis();
        this.finished = false;
        this.isFading = false;
        this.fadeProgress = 0.0f;
    }

    /**
     * Resolves the current frame number into a cached image.
     */
    private BufferedImage getCurrentSceneFrame() {
        if (activeSceneId == null || filePattern == null || frameCount <= 0) {
            return null;
        }

        int safeFrame = Math.max(0, Math.min(currentFrame, frameCount - 1));
        String key = "intro_" + activeSceneId + "_" + safeFrame;
        String path = String.format(filePattern, safeFrame);
        return ResourceCache.getSceneFrame(key, path);
    }

    /**
     * Marks the scene completed and clears the rolling frame cache.
     */
    private void finishScene() {
        finished = true;
        if (activeSceneId != null) {
            completedScenes.add(activeSceneId);
        }
        ResourceCache.clearSceneFrames();
    }
}
