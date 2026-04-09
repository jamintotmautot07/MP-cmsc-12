package systems;

import java.awt.Color;
import java.awt.Graphics2D;

public class Timer {
    private long startTime;
    private boolean running;
    private long pausedTime;
    private int timeScore;
    private int inGameTimeScore;
    private int timeLimit;
    private boolean paused;

    public Timer(int timeLimit) {
        this.timeLimit = timeLimit;
        timeScore = 0;
        inGameTimeScore = 0;
        pausedTime = 0;
        running = false;
        paused = false;
    }

    public void stopTimer() {
        if (running && !paused) {
            pausedTime += System.currentTimeMillis() - startTime;
            paused = true;
        }
    }

    public void resumeTimer() {
        if (paused) {
            startTime = System.currentTimeMillis();
            paused = false;
        }
    }

    public void resetTimer() {
        startTime = 0;
        pausedTime = 0;
        running = false;
        paused = false;
    }

    public void startTimer() {
        startTime = System.currentTimeMillis();
        pausedTime = 0;
        running = true;
        paused = false;
    }

    public long getElapsedTime() {
        if (!running) return 0;

        if (paused) {
            return pausedTime;
        } else {
            return pausedTime + (System.currentTimeMillis() - startTime);
        }
    }

    public String getFormattedTime() {
        long seconds = getElapsedTime() / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    public boolean hasTimeLimit() {
        return timeLimit > 0;
    }

    public int getRemainingTime() {
        if (!hasTimeLimit()) {
            return 0;
        }

        int elapsedSeconds = (int)(getElapsedTime() / 1000);
        int remaining = timeLimit - elapsedSeconds;

        return Math.max(remaining, 0); // prevents negative
    }

    public String getRemainingTimeFormatted() {
        if (!hasTimeLimit()) {
            return "--:--";
        }

        int totalSeconds = getRemainingTime();
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    public boolean isTimeUp() {
        return hasTimeLimit() && getRemainingTime() <= 0;
    }

    public int getTimeScore() {
        return timeScore;
    }

    public int getInGameTimeScore() {
        return inGameTimeScore;
    }

    public void showTimeScore() {
        System.out.println("Time Score: " + inGameTimeScore);
    }

    public void setTimeScore() {
        int remaining = getRemainingTime();
        inGameTimeScore = remaining * 5; // reward faster clears
    }

    public void setFinalTimeScore() {
        // records only the maximum point per game
        if(inGameTimeScore > timeScore) {
            timeScore = inGameTimeScore;
        }
    }
    
    public void show(Graphics2D g2, int x, int y) {

        if (getRemainingTime() <= 10) {
            g2.setColor(Color.RED);
        } else {
            g2.setColor(Color.WHITE);
        }

        g2.drawString("Time Left: " + getRemainingTimeFormatted(), x, y);
    }
}
