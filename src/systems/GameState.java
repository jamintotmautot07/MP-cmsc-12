package systems;

/*
    OWNER: Thea 

    PURPOSE:
    - Stores shared game data

    TASK:
    1. Manage score
    2. Manage running state
    3. Manage pause state
*/

/**
 * Future shared state model for score, health, run, and pause flags outside the Swing panel.
 */
public class GameState {
    private int score;
    private int level;
    private int health;

    private boolean running;
    private boolean paused;

    /**
     * Creates a default game state starting at level zero.
     */
    public GameState() {
        this(0);
    }

    /**
     * Creates a basic state container for a selected level.
     */
    public GameState(int selectedLevel) {
        score = 0;
        level = selectedLevel;
        health = 100;

        running = true;
        paused = false;
    }

    /**
     * Adds score in a synchronized block so future threads cannot race on this value.
     */
    public synchronized void addScore(int points) {
        score += points;
    }

    /**
     * Reduces health and stops the run when health reaches zero.
     */
    public synchronized void damagePlayer(int damage) {
        health -= damage;

        if(health <= 0) {
            running = false;
        }
    }

    /**
     * Returns the stored score.
     */
    public synchronized int getScore() {
        return score;
    }

    /**
     * Returns the selected/current level index.
     */
    public synchronized int getLevel() {
        return level;
    }

    /**
     * Returns the remaining health value.
     */
    public synchronized int getHealth() {
        return health;
    }

    /**
     * Returns whether this state thinks the game is still active.
     */
    public synchronized boolean isRunning() {
        return running;
    }

    /**
     * Marks the state as no longer running.
     */
    public synchronized void stopGame() {
        running = false;
    }

    /**
     * Returns whether this state is paused.
     */
    public synchronized boolean isPaused() {
        return paused;
    }

    /**
     * Updates the paused flag.
     */
    public synchronized void setPaused(boolean paused) {
        this.paused = paused;
    }   

    /**
     * Restores score, health, running, and pause flags to their initial values.
     */
    public synchronized void resetGame() {
        score = 0;
        health = 100;
        running = true;
        paused = false;
    }
}
