package systems;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Lightweight gameplay timer that supports pause/resume and time-based scoring.
 */
public class Timer {
    // `startTime` stores when the current running segment began.
    // `pausedTime` accumulates all finished running segments.
    private long startTime;
    private boolean running;
    private long pausedTime;

    // `timeScore` is the best locked-in score for the level/session.
    // `inGameTimeScore` is the live score that changes while the timer runs.
    private int timeScore;
    private int inGameTimeScore;

    // Time limit is in seconds. A value of 0 means no limit.
    private int timeLimit;
    private boolean paused;

    /**
     * Creates a timer for a level. A limit of 0 means open-ended.
     */
    public Timer(int timeLimit) {
        this.timeLimit = timeLimit;
        timeScore = 0;
        inGameTimeScore = 0;
        pausedTime = 0;
        running = false;
        paused = false;
    }

    /**
     * Pauses elapsed-time accumulation without destroying the current run.
     */
    public void stopTimer() {
        // When pausing, fold the time since the last resume into `pausedTime`.
        if (running && !paused) {
            pausedTime += System.currentTimeMillis() - startTime;
            paused = true;
        }
    }

    /**
     * Continues a previously paused timer.
     */
    public void resumeTimer() {
        // Resume starts a fresh running segment but keeps previously accumulated time.
        if (paused) {
            startTime = System.currentTimeMillis();
            paused = false;
        }
    }

    /**
     * Clears all runtime timer state.
     */
    public void resetTimer() {
        startTime = 0;
        pausedTime = 0;
        running = false;
        paused = false;
    }

    /**
     * Starts timing from a fresh zero state.
     */
    public void startTimer() {
        startTime = System.currentTimeMillis();
        pausedTime = 0;
        running = true;
        paused = false;
    }

    /**
     * Returns elapsed time in milliseconds.
     */
    public long getElapsedTime() {
        if (!running) return 0;

        if (paused) {
            // While paused, elapsed time should stay frozen.
            return pausedTime;
        } else {
            return pausedTime + (System.currentTimeMillis() - startTime);
        }
    }

    /**
     * Formats elapsed time as `MM:SS`.
     */
    public String getFormattedTime() {
        long seconds = getElapsedTime() / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Indicates whether countdown rules should apply.
     */
    public boolean hasTimeLimit() {
        return timeLimit > 0;
    }

    /**
     * Returns remaining time in seconds for timed levels.
     */
    public int getRemainingTime() {
        if (!hasTimeLimit()) {
            return 0;
        }

        int elapsedSeconds = (int)(getElapsedTime() / 1000);
        int remaining = timeLimit - elapsedSeconds;

        return Math.max(remaining, 0); // Keeps the HUD clean once the timer reaches zero.
    }

    /**
     * Formats the remaining time as `MM:SS`.
     */
    public String getRemainingTimeFormatted() {
        if (!hasTimeLimit()) {
            return "--:--";
        }

        int totalSeconds = getRemainingTime();
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * True once the countdown reaches zero.
     */
    public boolean isTimeUp() {
        return hasTimeLimit() && getRemainingTime() <= 0;
    }

    /**
     * Returns the best locked-in time score.
     */
    public int getTimeScore() {
        return timeScore;
    }

    /**
     * Returns the live time score before it is finalized.
     */
    public int getInGameTimeScore() {
        return inGameTimeScore;
    }

    /**
     * Debug helper for printing the live score.
     */
    public void showTimeScore() {
    }

    /**
     * Recomputes the live score from the current remaining time.
     */
    public void setTimeScore() {
        int remaining = getRemainingTime();
        inGameTimeScore = remaining * 5; // Simple reward model: more time left means more score.
    }

    /**
     * Commits the highest live score seen this run.
     */
    public void setFinalTimeScore() {
        // Stores only the highest captured score instead of replacing it blindly.
        if(inGameTimeScore > timeScore) {
            timeScore = inGameTimeScore;
        }
    }
    
    /**
     * Draws the timer HUD text.
     */
    public void show(Graphics2D g2, int x, int y) {

        // Turn red near the end to make the time pressure obvious.
        if (getRemainingTime() <= 10 && hasTimeLimit()) {
            g2.setColor(Color.RED);
        } else {
            g2.setColor(Color.WHITE);
        }

        g2.drawString("Time Left: " + getRemainingTimeFormatted(), x, y);
    }
}
