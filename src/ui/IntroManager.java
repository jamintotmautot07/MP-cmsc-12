package ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import util.Constants;

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
public class IntroManager {

    private String activeSceneId;
    private BufferedImage[] frames;
    private int currentFrame;
    private long lastFrameTime;
    private int frameDelay;
    private boolean finished;
    private final Set<String> completedScenes;

    // Fade effect variables
    private boolean isFading;
    private float fadeProgress;
    private long fadeStartTime;
    private int fadeDurationMs;
    private Color fadeColor;

    public IntroManager() {
        this.currentFrame = 0;
        this.lastFrameTime = System.currentTimeMillis();
        this.frameDelay = 80;
        this.finished = true;
        this.completedScenes = new HashSet<>();
        this.isFading = false;
        this.fadeProgress = 0.0f;
        this.fadeDurationMs = 1000; // 0.5 seconds
        this.fadeColor = Color.BLACK;
    }

    public boolean hasPlayed(String sceneId) {
        return completedScenes.contains(sceneId);
    }

    public boolean startScene(String sceneId, String filePattern, int frameCount, int frameDelayMs) {
        if (sceneId == null || filePattern == null || frameCount <= 0) {
            return false;
        }

        if (completedScenes.contains(sceneId)) {
            this.activeSceneId = sceneId;
            this.finished = true;
            return false;
        }

        this.activeSceneId = sceneId;
        this.frameDelay = Math.max(frameDelayMs, 16);
        this.currentFrame = 0;
        this.finished = false;
        this.isFading = false;
        this.fadeProgress = 0.0f;
        this.lastFrameTime = System.currentTimeMillis();
        loadSceneFrames(filePattern, frameCount);
        return frames != null && frames.length > 0;
    }

    private void loadSceneFrames(String filePattern, int frameCount) {
        frames = new BufferedImage[frameCount];
        int loadedFrames = 0;

        try {
            for (int i = 0; i < frameCount; i++) {
                String path = String.format(filePattern, i);
                BufferedImage image = ImageIO.read(new File(path));
                if (image != null) {
                    frames[loadedFrames++] = image;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (loadedFrames != frameCount) {
            BufferedImage[] trimmed = new BufferedImage[loadedFrames];
            System.arraycopy(frames, 0, trimmed, 0, loadedFrames);
            frames = trimmed;
        }

        if (frames.length == 0) {
            finished = true;
        }
    }

    public void update() {
        if (finished || frames == null || frames.length == 0) {
            return;
        }

        long now = System.currentTimeMillis();

        if (isFading) {
            long elapsed = now - fadeStartTime;
            fadeProgress = Math.min(1.0f, (float) elapsed / fadeDurationMs);
            if (fadeProgress >= 1.0f) {
                finished = true;
                if (activeSceneId != null) {
                    completedScenes.add(activeSceneId);
                }
            }
        } else {
            if (now - lastFrameTime >= frameDelay) {
                currentFrame++;
                lastFrameTime = now;

                if (currentFrame >= frames.length) {
                    // Start fading instead of finishing immediately
                    isFading = true;
                    fadeStartTime = now;
                    fadeProgress = 0.0f;
                    currentFrame = Math.max(0, frames.length - 1); // Stay on last frame
                }
            }
        }
    }

    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Draw the current frame
        if (frames != null && frames.length > 0 && currentFrame < frames.length && frames[currentFrame] != null) {
            g2d.drawImage(frames[currentFrame], 0, 0, Constants.screenWidth, Constants.screenHeight, null);
        } else {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, Constants.screenWidth, Constants.screenHeight);
        }

        // Draw fade overlay if fading
        if (isFading) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeProgress));
            g2d.setColor(fadeColor);
            g2d.fillRect(0, 0, Constants.screenWidth, Constants.screenHeight);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)); // Reset composite
        }
    }

    public void skip() {
        if (finished) {
            return;
        }

        if (!isFading) {
            // First skip: start fading
            isFading = true;
            fadeStartTime = System.currentTimeMillis();
            fadeProgress = 0.0f;
            currentFrame = Math.max(0, frames.length - 1); // Jump to last frame
        } else {
            // Second skip: skip the fade entirely
            fadeProgress = 1.0f;
            finished = true;
            if (activeSceneId != null) {
                completedScenes.add(activeSceneId);
            }
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isRunning() {
        return !finished && (frames != null && frames.length > 0 || isFading);
    }

    public void reset() {
        this.currentFrame = 0;
        this.lastFrameTime = System.currentTimeMillis();
        this.finished = false;
        this.isFading = false;
        this.fadeProgress = 0.0f;
    }
}
