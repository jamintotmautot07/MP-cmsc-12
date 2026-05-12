package engine;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import entity.CoreBoss;
import entity.Dummy;
import entity.Enemy;
import entity.Laser;
import entity.Player;
import entity.Projectile;
import panels.VictoryPanel;
import systems.CombatResolver;
import systems.KeyHandler;
import systems.ScoreManager;
import systems.Timer;
import systems.TutorialManager;
import tile.TileManager;
import ui.HudRenderer;
import ui.IntroManager;
import ui.ObjectiveTextOverlay;
import ui.PauseMenu;
import ui.TransitionManager;
import util.Constants;
import util.MethodUtilities;
import util.UtilityTool;

/**
 * Main gameplay surface that owns the live world objects, game loop, and gameplay state transitions.
 */
public class GamePanel extends JPanel implements Runnable {
    private static final int FPS = 60;
    private static final boolean SHOW_FPS_IN_CONSOLE = false;
    private static final int LEVEL_3_MAX_WORMS = 15;
    private static final int LEVEL_3_MAX_VIRUSES = 10;

    private static final int LEVEL_1_INTRO_FRAMES = 152;
    private static final int LEVEL_2_INTRO_FRAMES = 111;
    private static final int BOSS_INTRO_FRAMES = 237;
    private static final int ENDING_OUTRO_FRAMES = 149;
    private static final int SCENE_FRAME_DELAY_MS = 55;


    // objects that are responsible for different features og the game
    private final KeyHandler keyHandler = new KeyHandler(this);
    private final Player player = new Player(this, keyHandler);
    private final Camera camera = new Camera(player);
    private final TileManager tileManager = new TileManager(this);
    private final LevelEnemyFactory levelEnemyFactory = new LevelEnemyFactory(this, tileManager);
    private final CombatResolver combatResolver = new CombatResolver(this);
    private final HudRenderer hudRenderer = new HudRenderer();
    private final PauseMenu pauseMenu = new PauseMenu(this, this::resumeGame, this::exitToHome);
    private final TutorialManager tutorialManager = new TutorialManager();
    private final ObjectiveTextOverlay objectiveOverlay = new ObjectiveTextOverlay();
    private final TransitionManager transitionManager = new TransitionManager();
    private final ScoreManager scoreManager = new ScoreManager();
    private final List<Enemy> enemies = new CopyOnWriteArrayList<>();
    private final List<Projectile> projectiles = new CopyOnWriteArrayList<>();
    private final List<Laser> lasers = new CopyOnWriteArrayList<>();
    private final Map<String, Integer> enemyTotals = new LinkedHashMap<>();

    private Thread gameThread;
    private volatile boolean running = false;
    private GameMode gameMode = GameMode.PLAYING;
    private Level currentLevel = Level.LEVEL_1;
    private Timer timer = new Timer(currentLevel.timeLimitSeconds);
    private int levelsCleared = 0;
    private boolean resolvingLevelOutcome = false;
    private boolean resolvingDefeat = false;
    private boolean currentLevelScoreCommitted = false;
    private boolean currentLevelProgressRecorded = false;
    private Runnable onCutsceneComplete;
    private ScoreManager.ScoreSnapshot levelStartScore = scoreManager.snapshot();
    private VictoryPanel victoryPanel;

    public final IntroManager sceneManager = new IntroManager();
    public Runnable onLevelComplete;

    /**
     * Creates the gameplay panel and registers keyboard input.
     * Most heavy assets are already cached by the loading screen before this panel is used.
     */
    public GamePanel() {
        setPreferredSize(new Dimension(Constants.screenWidth, Constants.screenHeight));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        setFocusable(true);
        setLayout(new BorderLayout());
        addKeyListener(keyHandler);
    }

    /**
     * Starts a fresh full run beginning with the tutorial.
     * Scores and cleared-level counters are reset because this is treated as a new playthrough.
     */
    public void startRunFromTutorial() {
        hideVictoryPanel();
        scoreManager.resetRunScores();
        levelsCleared = 0;
        loadLevel(Level.TUTORIAL, true);
    }

    /**
     * Starts the level chosen from the menu.
     * Non-tutorial levels play their intro scene before gameplay begins.
     */
    public void startLevelFromMenu(Level level) {
        hideVictoryPanel();
        scoreManager.resetRunScores();
        levelsCleared = 0;

        Level nextLevel = level != null ? level : Level.TUTORIAL;
        if (nextLevel == Level.TUTORIAL) {
            loadLevel(nextLevel, true);
        } else {
            startLevelIntro(nextLevel);
        }
    }

    /**
     * Development/helper shortcut for loading a level directly.
     */
    public void setLevel(Level level) {
        loadLevel(level, true);
    }

    /**
     * Rebuilds all runtime state for the requested level.
     * This includes map data, player spawn, enemy spawns, timer setup, camera position, and score snapshot.
     */
    private void loadLevel(Level level, boolean startTimer) {
        Level nextLevel = level != null ? level : Level.TUTORIAL;

        keyHandler.resetKeys();
        player.setDirection("idle");
        currentLevel = nextLevel;

        timer = new Timer(nextLevel.timeLimitSeconds);
        if (startTimer) {
            timer.startTimer();
        }
        tileManager.loadMap(nextLevel.mapPath);
        player.setLevelStartPosition(nextLevel.positionX, nextLevel.positionY);

        resetLevelEntities();
        if (nextLevel == Level.TUTORIAL) {
            tutorialManager.reset();
        }
        levelEnemyFactory.populate(nextLevel);
        camera.update(player);
        levelStartScore = scoreManager.snapshot();

        if (startTimer) {
            gameMode = GameMode.PLAYING;
        } else if (!isInCutscene() && gameMode != GameMode.VICTORY) {
            gameMode = GameMode.PLAYING;
        }
        if (startTimer) {
            showObjectiveForLevel(nextLevel);
        }
    }

    /**
     * Clears per-level objects so old projectiles or enemies cannot leak into the next level.
     */
    private void resetLevelEntities() {
        enemies.clear();
        projectiles.clear();
        lasers.clear();
        enemyTotals.clear();
        resolvingLevelOutcome = false;
        resolvingDefeat = false;
        currentLevelScoreCommitted = false;
        currentLevelProgressRecorded = false;
    }

    /**
     * Adds an enemy to the active world if the current level allows another enemy of that type.
     */
    public boolean addEnemy(Enemy enemy) {
        if (enemy == null) {
            return false;
        }

        String type = UtilityTool.getEntityName(enemy);
        if (!canAddEnemyType(type)) {
            return false;
        }

        enemies.add(enemy);
        enemyTotals.put(type, enemyTotals.getOrDefault(type, 0) + 1);
        return true;
    }

    /**
     * Applies Level 3 spawn caps so Trojan spawners cannot flood the boss room forever.
     */
    private boolean canAddEnemyType(String type) {
        if (currentLevel != Level.LEVEL_3) {
            return true;
        }

        int currentTotal = enemyTotals.getOrDefault(type, 0);
        if ("Worm".equals(type)) {
            return currentTotal < LEVEL_3_MAX_WORMS;
        }
        if ("Virus".equals(type)) {
            return currentTotal < LEVEL_3_MAX_VIRUSES;
        }
        return true;
    }

    /**
     * Registers a projectile so CombatResolver and rendering can process it each frame.
     */
    public void spawnProjectile(Projectile projectile) {
        if (projectile != null) {
            projectiles.add(projectile);
        }
    }

    /**
     * Registers a laser beam attack for timed update/render handling.
     */
    public void spawnLaser(Laser laser) {
        if (laser != null) {
            lasers.add(laser);
        }
    }

    /**
     * Starts the main game loop thread if it is not already running.
     */
    public void startGameThread() {
        if (gameThread != null && gameThread.isAlive()) {
            return;
        }

        running = true;
        if (!isInCutscene() && gameMode != GameMode.VICTORY) {
            gameMode = GameMode.PLAYING;
        }
        if (isPlaying()) {
            timer.startTimer();
        }

        gameThread = new Thread(this, "HawakKoAngBit-GameLoop");
        gameThread.start();
    }

    /**
     * Stops gameplay cleanly and resets volatile gameplay state before returning to menus or exiting.
     */
    public void stopGameThread() {
        hideVictoryPanel();
        player.setDefaultValues();
        keyHandler.resetKeys();
        timer.stopTimer();
        timer.resetTimer();
        pauseMenu.hide();

        running = false;
        if (gameThread != null) {
            gameThread.interrupt();
        }
    }

    /**
     * Enters the paused mode and opens the pause dialog.
     */
    public void pauseGame() {
        if (!isPlaying() || transitionManager.isActive()) {
            return;
        }

        keyHandler.resetKeys();
        timer.stopTimer();
        gameMode = GameMode.PAUSED;
        repaint();
        pauseMenu.show();
    }

    /**
     * Closes the pause dialog and resumes timer/gameplay updates.
     */
    public void resumeGame() {
        if (!isPaused()) {
            return;
        }

        gameMode = GameMode.PLAYING;
        timer.resumeTimer();
        pauseMenu.hide();
        requestFocusInWindow();
    }

    /**
     * Starts a cutscene from inside gameplay and pauses the timer while it plays.
     */
    public void startLevelScene(String sceneId, String filePattern, int frameCount, int frameDelayMs) {
        onCutsceneComplete = null;
        timer.stopTimer();
        if (sceneManager.startScene(sceneId, filePattern, frameCount, frameDelayMs)) {
            gameMode = GameMode.CUTSCENE;
        } else {
            beginGameplayAfterCutscene();
        }
    }

    /**
     * Requests that the current gameplay cutscene finish early.
     */
    public void skipScene() {
        sceneManager.skip();
        if (sceneManager.isFinished()) {
            finishCutscene();
        }
    }

    /**
     * Lets tutorial logic react to the first frame of a key press.
     */
    public void handleActionPressed(KeyHandler.Action action) {
        if (currentLevel == Level.TUTORIAL && isPlaying() && !transitionManager.isActive()) {
            tutorialManager.handleAction(action);
        }
    }

    /**
     * Debug shortcut used during testing to mark the current level as cleared.
     */
    public void forceFinishCurrentLevel() {
        if (!isPlaying() || transitionManager.isActive() || resolvingLevelOutcome || resolvingDefeat) {
            return;
        }

        keyHandler.resetKeys();
        for (Enemy enemy : enemies) {
            if (enemy != null && enemy.isAlive()) {
                enemy.defeat();
            }
        }
        processDefeatedEnemies();

        if (currentLevel == Level.TUTORIAL) {
            tutorialManager.complete();
            completeTutorial();
        } else if (currentLevel == Level.LEVEL_3) {
            handleBossCleared();
        } else {
            handleLevelCleared();
        }
    }

    @Override
    public void run() {
        double drawInterval = 1000000000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        // long fpsTimer = 0;
        // int drawCount = 0;

        while (running && !Thread.currentThread().isInterrupted()) {
            long currentTime = System.nanoTime();
            long elapsed = currentTime - lastTime;
            delta += elapsed / drawInterval;
            // fpsTimer += elapsed;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
                // drawCount++;
            }

            // showing fps in the terminal
            // if (SHOW_FPS_IN_CONSOLE && fpsTimer >= 1000000000L) {
            //     System.out.println("FPS: " + drawCount);
            //     drawCount = 0;
            //     fpsTimer = 0;
            // }
        }
    }

    /**
     * Runs one logical frame of the current game mode.
     * Rendering is intentionally separate and happens through paintComponent().
     */
    public void update() {
        objectiveOverlay.update();
        transitionManager.update();
        if (transitionManager.isActive()) {
            return;
        }

        if (isPlaying()) {
            updateGameplay();
        } else if (isInCutscene()) {
            updateCutscene();
        }
    }

    /**
     * Updates the active gameplay state: player, enemies, combat, camera, and win/loss checks.
     */
    private void updateGameplay() {
        player.update();
        clampPlayerToWorld();

        for (Enemy enemy : enemies) {
            if (enemy != null && enemy.isAlive()) {
                enemy.update();
                clampEnemyToWorld(enemy);
           }
        }

        if(allEnemiesKilled()) {
            ((CoreBoss)enemies.get(0)).canBeDamaged();
        }

        combatResolver.resolve();
        processDefeatedEnemies();
        camera.update(player);

        if (player.getHp() <= 0) {
            handleDefeat();
            return;
        }

        if (timer.isTimeUp()) {
            handleTimeOut();
            return;
        }

        if (currentLevel == Level.TUTORIAL) {
            tutorialManager.update(areTutorialDummiesEliminated());
            if (isTutorialExitReached()) {
                completeTutorial();
            }
            return;
        }

        timer.setTimeScore();

        if (currentLevel == Level.LEVEL_3) {
            if (isBossDefeated()) {
                handleBossCleared();
            }
        } else if (enemies.isEmpty()) {
            handleLevelCleared();
        }
    }

    /**
     * Removes defeated enemies and counts eligible kills for score.
     */
    private void processDefeatedEnemies() {
        for (Enemy enemy : enemies) {
            if (enemy != null && !enemy.isAlive() && shouldCountEnemyDefeat(enemy)) {
                scoreManager.addEnemyEliminated();
            }
        }
        enemies.removeIf(enemy -> enemy == null || !enemy.isAlive());
    }

    /**
     * Keeps tutorial dummy defeats out of the real score totals.
     */
    private boolean shouldCountEnemyDefeat(Enemy enemy) {
        return currentLevel != Level.TUTORIAL && !(enemy instanceof Dummy);
    }

    /**
     * Advances the active cutscene and finishes it when the frame sequence is done.
     */
    private void updateCutscene() {
        sceneManager.update();
        if (sceneManager.isFinished()) {
            finishCutscene();
        }
    }

    /**
     * Runs the cutscene completion callback, or resumes normal gameplay.
     */
    private void finishCutscene() {
        Runnable callback = onCutsceneComplete;
        onCutsceneComplete = null;

        if (callback != null) {
            callback.run();
        } else {
            beginGameplayAfterCutscene();
        }
    }

    /**
     * Switches from cutscene mode into normal play and restarts the timer.
     */
    private void beginGameplayAfterCutscene() {
        keyHandler.resetKeys();
        gameMode = GameMode.PLAYING;
        timer.startTimer();
        showObjectiveForLevel(currentLevel);
        requestFocusInWindow();
    }

    /**
     * Prevents the player from moving outside the map rectangle.
     */
    private void clampPlayerToWorld() {
        player.worldX = Math.max(0, Math.min(player.worldX, Constants.maxWorldWidth - Constants.tileSize));
        player.worldY = Math.max(0, Math.min(player.worldY, Constants.maxWorldHeight - Constants.tileSize));
    }

    /**
     * Keeps enemy world positions within the playable map bounds.
     */
    private void clampEnemyToWorld(Enemy enemy) {
        enemy.worldX = Math.max(0, Math.min(enemy.worldX, Constants.maxWorldWidth - Constants.tileSize));
        enemy.worldY = Math.max(0, Math.min(enemy.worldY, Constants.maxWorldHeight - Constants.tileSize));
    }

    /**
     * Checks the tutorial objective that requires all dummy enemies to be defeated.
     */
    private boolean areTutorialDummiesEliminated() {
        for (Enemy enemy : enemies) {
            if (enemy instanceof Dummy && enemy.isAlive()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Detects the tutorial door tile after TutorialManager says the door is usable.
     */
    private boolean isTutorialExitReached() {
        if (currentLevel != Level.TUTORIAL || !tutorialManager.canUseDoor()) {
            return false;
        }

        int playerCol = player.worldX / Constants.tileSize;
        int playerRow = player.worldY / Constants.tileSize;
        return playerCol == 0 && (playerRow == 47 || playerRow == 48);
    }

    /**
     * Finishes tutorial progression and transitions into Level 1.
     */
    private void completeTutorial() {
        if (resolvingLevelOutcome) {
            return;
        }

        resolvingLevelOutcome = true;
        keyHandler.resetKeys();
        timer.stopTimer();
        tutorialManager.complete();
        gameMode = GameMode.LEVEL_CLEAR;
        recordProgressForCurrentLevel();
        transitionManager.start(() -> startLevelIntro(Level.LEVEL_1));
    }

    /**
     * Checks whether the CoreBoss is gone from the remaining enemy list.
     */
    private boolean isBossDefeated() {
        for (Enemy enemy : enemies) {
            if ((enemy instanceof CoreBoss && enemy.isAlive()) || enemy.isAlive()) {
                return false;
            }
        }
        return true;
    }

    public boolean allEnemiesKilled() {
        if(enemies.get(0) instanceof CoreBoss && enemies.size() == 1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Handles normal level completion for Levels 1 and 2.
     */
    private void handleLevelCleared() {
        if (resolvingLevelOutcome) {
            return;
        }

        resolvingLevelOutcome = true;
        keyHandler.resetKeys();
        timer.stopTimer();
        timer.setTimeScore();
        commitCurrentLevelScore();
        recordProgressForCurrentLevel();
        gameMode = GameMode.LEVEL_CLEAR;
        repaint();

        SwingUtilities.invokeLater(this::showLevelClearedDialog);
    }

    /**
     * Shows the continue/exit prompt after a non-boss level is cleared.
     */
    private void showLevelClearedDialog() {
        Object[] options = {"Continue", "Exit"};
        int result = JOptionPane.showOptionDialog(
            this,
            getLevelClearMessage(),
            getLevelClearTitle(),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            options,
            options[0]
        );

        if (result == JOptionPane.YES_OPTION) {
            Level nextLevel = currentLevel.nextLevel;
            if (nextLevel != null) {
                transitionManager.start(() -> startLevelIntro(nextLevel));
            } else {
                exitToHome();
            }
        } else {
            exitToHome();
        }
    }

    /**
     * Returns a level-specific dialog title for the clear prompt.
     */
    private String getLevelClearTitle() {
        if (currentLevel == Level.LEVEL_1) {
            return "BOOT SECTOR CLEARED";
        }
        if (currentLevel == Level.LEVEL_2) {
            return "MEMORY CORE CLEARED";
        }
        return "SECTOR CLEARED";
    }

    /**
     * Returns the level-specific prompt text after clearing a level.
     */
    private String getLevelClearMessage() {
        if (currentLevel == Level.LEVEL_1) {
            return "Proceed to the next sector?";
        }
        if (currentLevel == Level.LEVEL_2) {
            return "Proceed to the Core System?";
        }
        return "Proceed?";
    }

    /**
     * Handles final boss completion and starts the ending sequence.
     */
    private void handleBossCleared() {
        if (resolvingLevelOutcome) {
            return;
        }

        resolvingLevelOutcome = true;
        keyHandler.resetKeys();
        timer.stopTimer();
        timer.setTimeScore();
        commitCurrentLevelScore();
        projectiles.clear();
        lasers.clear();
        gameMode = GameMode.LEVEL_CLEAR;
        transitionManager.start(this::startEndingScene);
    }

    /**
     * Handles player HP reaching zero.
     */
    private void handleDefeat() {
        if (resolvingDefeat) {
            return;
        }

        resolvingDefeat = true;
        keyHandler.resetKeys();
        timer.stopTimer();
        gameMode = GameMode.DEFEAT;
        repaint();

        SwingUtilities.invokeLater(() -> showRestartExitDialog("SYSTEM FAILURE", getDefeatMessage()));
    }

    /**
     * Handles timed levels when the countdown reaches zero.
     */
    private void handleTimeOut() {
        if (resolvingDefeat) {
            return;
        }

        resolvingDefeat = true;
        keyHandler.resetKeys();
        timer.stopTimer();
        gameMode = GameMode.OUT_OF_TIME;
        repaint();

        SwingUtilities.invokeLater(() -> showRestartExitDialog("OUT OF TIME", getOutOfTimeMessage()));
    }

    /**
     * Returns defeat text that matches the current level context.
     */
    private String getDefeatMessage() {
        if (currentLevel == Level.LEVEL_3) {
            return "The Core has rejected the Bit.";
        }
        return "You have been eliminated.";
    }

    /**
     * Returns timeout text that matches the current level context.
     */
    private String getOutOfTimeMessage() {
        if (currentLevel == Level.LEVEL_1) {
            return "The corruption has overwhelmed this sector.";
        }
        if (currentLevel == Level.LEVEL_2) {
            return "The corruption has consumed the Memory Core.";
        }
        if (currentLevel == Level.LEVEL_3) {
            return "The Core corruption has become permanent.";
        }
        return "The system timed out.";
    }

    /**
     * Shows the restart-or-exit prompt shared by defeat and timeout.
     */
    private void showRestartExitDialog(String title, String message) {
        Object[] options = {"Restart", "Exit"};
        int result = JOptionPane.showOptionDialog(
            this,
            message,
            title,
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null,
            options,
            options[0]
        );

        if (result == JOptionPane.YES_OPTION) {
            restartCurrentLevel();
        } else {
            exitToHome();
        }
    }

    /**
     * Reloads the current level and restores the score snapshot from before the attempt.
     */
    private void restartCurrentLevel() {
        scoreManager.restore(levelStartScore);
        loadLevel(currentLevel, true);
        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    /**
     * Adds the current level's earned score exactly once.
     */
    private void commitCurrentLevelScore() {
        if (currentLevelScoreCommitted || currentLevel == Level.TUTORIAL) {
            return;
        }

        timer.setFinalTimeScore();
        currentLevel.setMaxTimeScore(timer.getTimeScore());
        scoreManager.addTimeScore(timer.getTimeScore());
        scoreManager.addLevelCleared();
        levelsCleared = scoreManager.getLevelsCleared();
        currentLevelScoreCommitted = true;
        levelStartScore = scoreManager.snapshot();
    }

    /**
     * Notifies BaseFrame that a level has been cleared, but only once per clear event.
     */
    private void recordProgressForCurrentLevel() {
        if (currentLevelProgressRecorded || onLevelComplete == null) {
            return;
        }

        currentLevelProgressRecorded = true;
        onLevelComplete.run();
    }

    /**
     * Loads the next level, then plays its intro scene before enabling gameplay.
     */
    private void startLevelIntro(Level level) {
        Level nextLevel = level != null ? level : Level.LEVEL_1;
        loadLevel(nextLevel, false);
        SceneSpec spec = getIntroSpec(nextLevel);
        if (spec == null) {
            beginGameplayAfterCutscene();
            return;
        }

        onCutsceneComplete = this::beginGameplayAfterCutscene;
        timer.stopTimer();
        if (sceneManager.startScene(spec.sceneId, spec.filePattern, spec.frameCount, SCENE_FRAME_DELAY_MS)) {
            gameMode = GameMode.CUTSCENE;
        } else {
            beginGameplayAfterCutscene();
        }
    }

    /**
     * Maps a level to the cutscene frame sequence that introduces it.
     */
    private SceneSpec getIntroSpec(Level level) {
        if (level == Level.LEVEL_1) {
            return new SceneSpec("level1Intro", "res/Lvl1Intro/Lvl1_Intro_%04d.png", LEVEL_1_INTRO_FRAMES);
        }
        if (level == Level.LEVEL_2) {
            return new SceneSpec("level2Intro", "res/Lvl2Intro/Lvl2_Intro_%04d.png", LEVEL_2_INTRO_FRAMES);
        }
        if (level == Level.LEVEL_3) {
            return new SceneSpec("bossIntro", "res/Lvl3Intro/Lvl3_Intro_%04d.png", BOSS_INTRO_FRAMES);
        }
        return null;
    }

    /**
     * Plays the final outro, then opens the victory score screen.
     */
    private void startEndingScene() {
        projectiles.clear();
        lasers.clear();
        enemies.clear();
        enemyTotals.clear();
        onCutsceneComplete = () -> {
            recordProgressForCurrentLevel();
            transitionManager.start(this::showVictoryScreen);
        };

        if (sceneManager.startScene("endingOutro", "res/Outro/Outro_%04d.png", ENDING_OUTRO_FRAMES, SCENE_FRAME_DELAY_MS)) {
            gameMode = GameMode.CUTSCENE;
        } else {
            recordProgressForCurrentLevel();
            showVictoryScreen();
        }
    }

    /**
     * Displays a short objective overlay for levels that need one.
     */
    private void showObjectiveForLevel(Level level) {
        if (level == Level.LEVEL_1 || level == Level.LEVEL_2) {
            objectiveOverlay.show("ELIMINATE ALL ENEMIES!!!");
        } else if (level == Level.LEVEL_3) {
            objectiveOverlay.show("DEFEAT THE CORE VIRUS!!!");
        }
    }

    public void showObjectForBoss() {
        if(this.getCurrentLevel() == Level.LEVEL_3) {
            objectiveOverlay.show("DEFEAT ALL ENEMIES FIRST!!!", 500, 1500, 300);
        }
    }

    /**
     * Mounts the victory panel and passes the final ScoreManager values into it.
     */
    private void showVictoryScreen() {
        gameMode = GameMode.VICTORY;
        timer.stopTimer();
        keyHandler.resetKeys();
        if (victoryPanel == null) {
            victoryPanel = new VictoryPanel(
                this::returnToMenuFromVictory,
                this::playAgainFromVictory,
                this::exitApplicationFromVictory
            );
        }
        if (victoryPanel.getParent() != this) {
            add(victoryPanel, BorderLayout.CENTER);
        }
        victoryPanel.showScores(scoreManager);
        revalidate();
        repaint();
    }

    /**
     * Removes the victory overlay if it is currently attached to the gameplay panel.
     */
    private void hideVictoryPanel() {
        if (victoryPanel != null) {
            victoryPanel.stopAnimation();
            if (victoryPanel.getParent() == this) {
                remove(victoryPanel);
                revalidate();
                repaint();
            }
        }
    }

    /**
     * Victory button callback for returning to the main menu.
     */
    private void returnToMenuFromVictory() {
        hideVictoryPanel();
        exitToHome();
    }

    /**
     * Victory button callback for starting a fresh run immediately.
     */
    private void playAgainFromVictory() {
        hideVictoryPanel();
        startRunFromTutorial();
        if (!running) {
            startGameThread();
        }
        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    /**
     * Victory button callback for closing the application.
     */
    private void exitApplicationFromVictory() {
        Window ownerFrame = SwingUtilities.getWindowAncestor(this);
        stopGameThread();
        if (ownerFrame != null) {
            ownerFrame.dispose();
        }
        System.exit(0);
    }

    /**
     * Shared exit path from gameplay back to the opening menu.
     */
    private void exitToHome() {
        stopGameThread();
        Window ownerFrame = SwingUtilities.getWindowAncestor(this);
        if (ownerFrame instanceof main.BaseFrame) {
            ((main.BaseFrame) ownerFrame).showOpeningScreen();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (gameMode == GameMode.VICTORY) {
            transitionManager.draw(g2);
            return;
        }

        tileManager.draw(g2);

        if (isInCutscene()) {
            drawCutscene(g2);
            transitionManager.draw(g2);
            return;
        }

        drawActiveGameplay(g2);
        hudRenderer.draw(g2, currentLevel, timer, player, enemies, enemyTotals);

        if (currentLevel == Level.TUTORIAL) {
            drawTutorialInstruction(g2);
        }

        objectiveOverlay.draw(g2);
        transitionManager.draw(g2);
    }

    /**
     * Draws the active cutscene and the skip hint.
     */
    private void drawCutscene(Graphics2D g2) {
        sceneManager.render(g2);
        g2.setColor(Color.WHITE);
        g2.setFont(MethodUtilities.getFont(20f));
        String text = "Press ESC to skip";
        int textWidth = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, (getWidth() - textWidth) / 2, getHeight() - 40);
    }

    /**
     * Draws gameplay objects in the order they should appear on screen.
     */
    private void drawActiveGameplay(Graphics2D g2) {
        for (Projectile projectile : projectiles) {
            if (projectile != null && projectile.isAlive()) {
                projectile.draw(g2, camera.getWorldX(), camera.getWorldY());
            }
        }

        for (Laser laser : lasers) {
            if (laser != null && laser.isAlive()) {
                laser.draw(g2, camera.getWorldX(), camera.getWorldY());
            }
        }

        for (Enemy enemy : enemies) {
            if (enemy != null && enemy.isAlive()) {
                enemy.render(g2);
            }
        }

        if (isPlaying()) {
            drawDarkOverlay(g2, 55);
        }

        player.draw(g2);

        if (isDialogMode()) {
            drawDarkOverlay(g2, 145);
        }
    }

    /**
     * Draws the tutorial instruction panel near the bottom of the screen.
     */
    private void drawTutorialInstruction(Graphics2D g2) {
        String instruction = tutorialManager.getCurrentInstruction();
        g2.setFont(MethodUtilities.getFont(18f));
        FontMetrics metrics = g2.getFontMetrics();
        int panelWidth = Constants.screenWidth - 96;
        int panelHeight = 82;
        int panelX = 48;
        int panelY = Constants.screenHeight - panelHeight - 28;
        int textX = panelX + 24;
        int textY = panelY + 48;

        g2.setColor(new Color(0, 0, 0, 190));
        g2.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 18, 18);
        g2.setColor(new Color(60, 230, 150, 160));
        g2.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 18, 18);
        g2.setColor(new Color(220, 255, 235));
        if (metrics.stringWidth(instruction) > panelWidth - 48) {
            g2.setFont(MethodUtilities.getFont(15f));
        }
        g2.drawString(instruction, textX, textY);
    }

    /**
     * Draws a translucent black overlay used for focus and dialog states.
     */
    private void drawDarkOverlay(Graphics2D g2, int alpha) {
        g2.setColor(new Color(0, 0, 0, alpha));
        g2.fillRect(0, 0, Constants.screenWidth, Constants.screenHeight);
    }

    /**
     * Checks if gameplay should be visually dimmed because a modal state is active.
     */
    private boolean isDialogMode() {
        return isPaused()
            || gameMode == GameMode.LEVEL_CLEAR
            || gameMode == GameMode.DEFEAT
            || gameMode == GameMode.OUT_OF_TIME;
    }

    /**
     * Returns true only when player/enemy updates should run.
     */
    public boolean isPlaying() {
        return gameMode == GameMode.PLAYING;
    }

    /**
     * Returns true while the pause menu owns the current gameplay state.
     */
    public boolean isPaused() {
        return gameMode == GameMode.PAUSED;
    }

    /**
     * Returns true while gameplay is showing a cutscene frame sequence.
     */
    public boolean isInCutscene() {
        return gameMode == GameMode.CUTSCENE;
    }

    /**
     * Returns the currently loaded level.
     */
    public Level getCurrentLevel() {
        return currentLevel;
    }

    /**
     * Returns the active level timer.
     */
    public Timer getTimer() {
        return timer;
    }

    /**
     * Returns how many levels have been cleared in this run.
     */
    public int getLevelsCleared() {
        return scoreManager.getLevelsCleared();
    }

    /**
     * Returns the score manager used by HUD/victory screens.
     */
    public ScoreManager getScoreManager() {
        return scoreManager;
    }

    /**
     * Returns the single player entity.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the tile manager for collision and rendering.
     */
    public TileManager getTileManager() {
        return tileManager;
    }

    /**
     * Returns the live enemy list.
     */
    public List<Enemy> getEnemies() {
        return enemies;
    }

    /**
     * Returns the live projectile list.
     */
    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    /**
     * Returns the live laser list.
     */
    public List<Laser> getLasers() {
        return lasers;
    }

    /**
     * Returns the player's current screen x-position.
     */
    public int getCameraX() {
        return camera.getScreenX();
    }

    /**
     * Returns the player's current screen y-position.
     */
    public int getCameraY() {
        return camera.getScreenY();
    }

    /**
     * Returns the world x-coordinate at the left edge of the camera.
     */
    public int getCameraWorldX() {
        return camera.getWorldX();
    }

    /**
     * Returns the world y-coordinate at the top edge of the camera.
     */
    public int getCameraWorldY() {
        return camera.getWorldY();
    }

    private static class SceneSpec {
        private final String sceneId;
        private final String filePattern;
        private final int frameCount;

        /**
         * Stores the scene id, filename pattern, and number of frames for one intro scene.
         */
        private SceneSpec(String sceneId, String filePattern, int frameCount) {
            this.sceneId = sceneId;
            this.filePattern = filePattern;
            this.frameCount = frameCount;
        }
    }
}
