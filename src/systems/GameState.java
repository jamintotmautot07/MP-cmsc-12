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

public class GameState {
    private int score;
    private int level;
    private int health;

    private boolean running;
    private boolean paused;

    public GameState() {
        this(0);
    }
    public GameState(int selectedLevel) {
        score = 0;
        level = selectedLevel;
        health = 100;

        running = true;
        paused = false;
    }

    public synchronized void addScore(int points) {
        score += points;
    }

    public synchronized void damagePlayer(int damage) {
        health -= damage;

        if(health <= 0) {
            running = false;
        }
    }

    public synchronized int getScore() {
        return score;
    }

    public synchronized int getLevel() {
        return level;
    }

    public synchronized int getHealth() {
        return health;
    }

    public synchronized boolean isRunning() {
        return running;
    }

    public synchronized void stopGame() {
        running = false;
    }

    public synchronized boolean isPaused() {
        return paused;
    }

    public synchronized void setPaused(boolean paused) {
        this.paused = paused;
    }   

    public synchronized void resetGame() {
        score = 0;
        health = 100;
        running = true;
        paused = false;
    }
}
