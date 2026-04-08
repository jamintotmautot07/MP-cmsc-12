package engine;

public class Level {
    public final String name;
    public final int timeLimitSeconds;
    public final String mapPath;
    public final Level nextLevel;

    // Placeholder for future features
    // public final boolean hasEnemies;
    // public final boolean hasPowerUps;
    // public final boolean hasBoss;
    // public final String objective;

    public static final Level LEVEL_3 = new Level("Level 3 (Boss)", 1 * 60, "/res/mapLvl3.txt", null);
    public static final Level LEVEL_2 = new Level("Level 2", 2 * 60, "/res/mapLvl2.txt", LEVEL_3);
    public static final Level LEVEL_1 = new Level("Level 1", 3 * 60, "/res/mapLvl1.txt", LEVEL_2);
    public static final Level TUTORIAL = new Level("Tutorial", 0, "/res/mapTutorial.txt", LEVEL_1);

    public static final Level[] LEVELS = new Level[] {
        TUTORIAL,
        LEVEL_1,
        LEVEL_2,
        LEVEL_3
    };

    public Level(String name, int timeLimitSeconds, String mapPath, Level nextLevel) {
        this.name = name;
        this.timeLimitSeconds = timeLimitSeconds;
        this.mapPath = mapPath;
        this.nextLevel = nextLevel;
    }

    public boolean hasTimeLimit() {
        return timeLimitSeconds > 0;
    }

    public String getTimeLabel() {
        if (!hasTimeLimit()) {
            return "NO LIMIT";
        }

        int minutes = timeLimitSeconds / 60;
        int seconds = timeLimitSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public boolean isCompleted() {
        // Placeholder: For now, completed when timer runs out (survived)
        return true; // Always true for progression, but check timer in GamePanel
    }
}
