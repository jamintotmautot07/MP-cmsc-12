package systems;

/**
 * Tracks the scoring values for one gameplay run.
 */
public class ScoreManager {
    private static final int ENEMY_POINTS = 100;
    private static final int LEVEL_CLEAR_POINTS = 500;

    private int timeScore;
    private int enemyScore;
    private int enemiesEliminated;
    private int levelsCleared;

    public void resetRunScores() {
        timeScore = 0;
        enemyScore = 0;
        enemiesEliminated = 0;
        levelsCleared = 0;
    }

    public void addEnemyEliminated() {
        enemiesEliminated++;
        enemyScore += ENEMY_POINTS;
    }

    public void addLevelCleared() {
        levelsCleared++;
    }

    public void addTimeScore(int score) {
        timeScore += Math.max(0, score);
    }

    public int calculateTotalScore() {
        return timeScore + enemyScore + (levelsCleared * LEVEL_CLEAR_POINTS);
    }

    public int getTimeScore() {
        return timeScore;
    }

    public int getEnemyScore() {
        return enemyScore;
    }

    public int getEnemiesEliminated() {
        return enemiesEliminated;
    }

    public int getLevelsCleared() {
        return levelsCleared;
    }

    public ScoreSnapshot snapshot() {
        return new ScoreSnapshot(timeScore, enemyScore, enemiesEliminated, levelsCleared);
    }

    public void restore(ScoreSnapshot snapshot) {
        if (snapshot == null) {
            resetRunScores();
            return;
        }

        timeScore = snapshot.timeScore;
        enemyScore = snapshot.enemyScore;
        enemiesEliminated = snapshot.enemiesEliminated;
        levelsCleared = snapshot.levelsCleared;
    }

    public static class ScoreSnapshot {
        private final int timeScore;
        private final int enemyScore;
        private final int enemiesEliminated;
        private final int levelsCleared;

        private ScoreSnapshot(int timeScore, int enemyScore, int enemiesEliminated, int levelsCleared) {
            this.timeScore = timeScore;
            this.enemyScore = enemyScore;
            this.enemiesEliminated = enemiesEliminated;
            this.levelsCleared = levelsCleared;
        }
    }
}
