package engine;

import util.Constants;

/**
 * Immutable-ish level descriptor used by menus and GamePanel to know which map and spawn data to load.
 */
public class Level {
    // Human-readable name for menus and HUD.
    public final String name;

    // Time limit in seconds. Zero means the level is open-ended.
    public final int timeLimitSeconds;

    // Path to the text file that describes the tile map.
    public final String mapPath;

    // Link to the next level in the progression chain.
    public final Level nextLevel;

    // Highest recorded time-based score for this level during the current run.
    public int maxTimeScore;
    

    // players initial position
    public int positionX;
    public int positionY;

    // Placeholder for future features
    // public final boolean hasEnemies;
    // public final boolean hasPowerUps;
    // public final boolean hasBoss;
    // public final String objective;

    // Static level definitions keep setup simple for a small project.
    public static final Level LEVEL_3 = new Level("Level 3 (Boss)", 2 * 60, "/res/mapLvl3.txt", null, 0, 24 * Constants.tileSize);
    public static final Level LEVEL_2 = new Level("Level 2", 3 * 60, "/res/mapLvl2.txt", LEVEL_3, 24* Constants.tileSize, 3 * Constants.tileSize);
    public static final Level LEVEL_1 = new Level("Level 1", 4 * 60, "/res/mapLvl1.txt", LEVEL_2, 2 * Constants.tileSize, 4 * Constants.tileSize);
    public static final Level TUTORIAL = new Level("Tutorial", 0, "/res/mapTutorial.txt", LEVEL_1, 1 * Constants.tileSize, 2 * Constants.tileSize);

    public static final Level[] LEVELS = new Level[] {
        TUTORIAL,
        LEVEL_1,
        LEVEL_2,
        LEVEL_3
    };

    /**
     * Finds a level's position in the predefined level list.
     */
    public static int getIndex(Level level) {
        // Returns the position inside `LEVELS`, useful for menus and progression tracking.
        for (int i = 0; i < LEVELS.length; i++) {
            if (LEVELS[i] == level) return i;
        }
        return 0;
    }

    /**
     * Creates one level definition entry.
     */
    public Level(String name, int timeLimitSeconds, String mapPath, Level nextLevel, int positionX, int positionY) {
        this.name = name;
        this.timeLimitSeconds = timeLimitSeconds;
        this.mapPath = mapPath;
        this.nextLevel = nextLevel;
        maxTimeScore = 0;
        this.positionX = positionX;
        this.positionY = positionY;
    }

    /**
     * Convenience check for timer-driven levels.
     */
    public boolean hasTimeLimit() {
        return timeLimitSeconds > 0;
    }

    /**
     * Stores the best recorded time-based score for this level in the current session.
     */
    public void setMaxTimeScore(int timeScore) {
        this.maxTimeScore = timeScore;
    }

    /**
     * Returns a UI-friendly label for the configured level time limit.
     */
    public String getTimeLabel() {
        // Formats the configured limit for UI display, not the live remaining time.
        if (!hasTimeLimit()) {
            return "NO LIMIT";
        }

        int minutes = timeLimitSeconds / 60;
        int seconds = timeLimitSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Placeholder completion hook kept for future win-condition expansion.
     */
    public boolean isCompleted() {
        // Placeholder method kept for future win-condition logic.
        return true; // Always true for progression, but check timer in GamePanel
    }
}
