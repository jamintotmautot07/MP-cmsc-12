package systems;

import exception.GameException;

/**
 * Helper for persisting and loading the last saved score breakdown.
 */
public final class ScoreFileManager {

    private ScoreFileManager() {
        // Utility class
    }

    public static void saveScores(ScoreManager scoreManager) throws GameException {
        if (scoreManager == null) {
            throw new IllegalArgumentException("scoreManager cannot be null");
        }

        saveScores(
            scoreManager.getTimeScore(),
            scoreManager.getEnemyScore(),
            scoreManager.getEnemiesEliminated(),
            scoreManager.getLevelsCleared(),
            scoreManager.calculateTotalScore()
        );
    }

    public static void saveScores(int timeScore, int enemyScore, int enemiesEliminated, int levelsCleared, int totalScore) throws GameException {
        FileManager.saveData(
            Math.max(FileManager.loadHighScore(), totalScore),
            FileManager.loadTutorialPlayed(),
            FileManager.loadMaxLevelReached(),
            FileManager.loadSelectedLevel(),
            timeScore,
            enemyScore,
            enemiesEliminated,
            levelsCleared,
            totalScore
        );
    }

    public static ScoreManager loadScores() throws GameException {
        ScoreManager loaded = new ScoreManager();
        loaded.setRunScores(
            FileManager.loadLastTimeScore(),
            FileManager.loadLastEnemyScore(),
            FileManager.loadLastEnemiesEliminated(),
            FileManager.loadLastLevelsCleared()
        );
        return loaded;
    }
}
