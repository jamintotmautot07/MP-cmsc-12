package engine;

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

    // Placeholder for future features
    // public final boolean hasEnemies;
    // public final boolean hasPowerUps;
    // public final boolean hasBoss;
    // public final String objective;

    // Static level definitions keep setup simple for a small project.
    public static final Level LEVEL_3 = new Level("Level 3 (Boss)", 1 * 20, "/res/mapLvl3.txt", null);
    public static final Level LEVEL_2 = new Level("Level 2", 2 * 20, "/res/mapLvl2.txt", LEVEL_3);
    public static final Level LEVEL_1 = new Level("Level 1", 3 * 20, "/res/mapLvl1.txt", LEVEL_2);
    public static final Level TUTORIAL = new Level("Tutorial", 0, "/res/mapTutorial.txt", LEVEL_1);

    public static final Level[] LEVELS = new Level[] {
        TUTORIAL,
        LEVEL_1,
        LEVEL_2,
        LEVEL_3
    };

    public static int getIndex(Level level) {
        // Returns the position inside `LEVELS`, useful for menus and progression tracking.
        for (int i = 0; i < LEVELS.length; i++) {
            if (LEVELS[i] == level) return i;
        }
        return 0;
    }

    public Level(String name, int timeLimitSeconds, String mapPath, Level nextLevel) {
        this.name = name;
        this.timeLimitSeconds = timeLimitSeconds;
        this.mapPath = mapPath;
        this.nextLevel = nextLevel;
        maxTimeScore = 0;
    }

    public boolean hasTimeLimit() {
        return timeLimitSeconds > 0;
    }

    public void setMaxTimeScore(int timeScore) {
        this.maxTimeScore = timeScore;
    }

    public String getTimeLabel() {
        // Formats the configured limit for UI display, not the live remaining time.
        if (!hasTimeLimit()) {
            return "NO LIMIT";
        }

        int minutes = timeLimitSeconds / 60;
        int seconds = timeLimitSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public boolean isCompleted() {
        // Placeholder method kept for future win-condition logic.
        return true; // Always true for progression, but check timer in GamePanel
    }
}
