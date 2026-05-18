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

    /**
     * Clears all score values at the beginning of a new run.
     */
    public void resetRunScores() {
        timeScore = 0;
        enemyScore = 0;
        enemiesEliminated = 0;
        levelsCleared = 0;
    }

    /**
     * Records one enemy kill and adds its fixed point value.
     */
    public void addEnemyEliminated() {
        enemiesEliminated++;
        enemyScore += ENEMY_POINTS;
    }

    /**
     * Records one cleared non-tutorial level.
     */
    public void addLevelCleared() {
        levelsCleared++;
    }

    /**
     * Adds the time bonus from a completed timed level.
     */
    public void addTimeScore(int score) {
        timeScore += Math.max(0, score);
    }

    /**
     * Combines time, enemy, and level-clear bonuses into one total.
     */
    public int calculateTotalScore() {
        return timeScore + enemyScore + (levelsCleared * LEVEL_CLEAR_POINTS);
    }

    /**
     * Returns accumulated time bonus points.
     */
    public int getTimeScore() {
        return timeScore;
    }

    /**
     * Returns accumulated enemy-elimination points.
     */
    public int getEnemyScore() {
        return enemyScore;
    }

    /**
     * Returns how many score-counting enemies were defeated.
     */
    public int getEnemiesEliminated() {
        return enemiesEliminated;
    }

    /**
     * Returns how many levels have been cleared in the run.
     */
    public int getLevelsCleared() {
        return levelsCleared;
    }

    /**
     * Captures score state so it can be restored after a failed level attempt.
     */
    public ScoreSnapshot snapshot() {
        return new ScoreSnapshot(timeScore, enemyScore, enemiesEliminated, levelsCleared);
    }

    /**
     * Restores a previous score snapshot, or resets if no snapshot is available.
     */
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

        /**
         * Stores an immutable copy of the scoring fields.
         */
        private ScoreSnapshot(int timeScore, int enemyScore, int enemiesEliminated, int levelsCleared) {
            this.timeScore = timeScore;
            this.enemyScore = enemyScore;
            this.enemiesEliminated = enemiesEliminated;
            this.levelsCleared = levelsCleared;
        }
    }
}
