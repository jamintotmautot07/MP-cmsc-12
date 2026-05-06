package engine;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import entity.Enemy;
import entity.Laser;
import entity.Player;
import entity.Projectile;
import systems.CombatResolver;
import systems.KeyHandler;
import systems.Timer;
import tile.TileManager;
import ui.HudRenderer;
import ui.IntroManager;
import ui.PauseMenu;
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

    private final KeyHandler keyHandler = new KeyHandler(this);
    private final Player player = new Player(this, keyHandler);
    private final Camera camera = new Camera(player);
    private final TileManager tileManager = new TileManager(this);
    private final LevelEnemyFactory levelEnemyFactory = new LevelEnemyFactory(this, tileManager);
    private final CombatResolver combatResolver = new CombatResolver(this);
    private final HudRenderer hudRenderer = new HudRenderer();
    private final PauseMenu pauseMenu = new PauseMenu(this, this::resumeGame, this::exitToHome);
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

    public final IntroManager sceneManager = new IntroManager();
    public Runnable onLevelComplete;

    public GamePanel() {
        setPreferredSize(new Dimension(Constants.screenWidth, Constants.screenHeight));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        setFocusable(true);
        setLayout(new BorderLayout());
        addKeyListener(keyHandler);
    }

    public void setLevel(Level level) {
        Level nextLevel = level != null ? level : Level.TUTORIAL;

        keyHandler.resetKeys();
        player.setDirection("idle");
        currentLevel = nextLevel;

        timer = new Timer(nextLevel.timeLimitSeconds);
        timer.startTimer();
        tileManager.loadMap(nextLevel.mapPath);
        player.setLevelStartPosition(nextLevel.positionX, nextLevel.positionY);

        resetLevelEntities();
        levelEnemyFactory.populate(nextLevel);
        camera.update(player);

        if (!isInCutscene()) {
            gameMode = GameMode.PLAYING;
        }
    }

    private void resetLevelEntities() {
        enemies.clear();
        projectiles.clear();
        lasers.clear();
        enemyTotals.clear();
        resolvingLevelOutcome = false;
        resolvingDefeat = false;
    }

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

    public void spawnProjectile(Projectile projectile) {
        if (projectile != null) {
            projectiles.add(projectile);
        }
    }

    public void spawnLaser(Laser laser) {
        if (laser != null) {
            lasers.add(laser);
        }
    }

    public void startGameThread() {
        if (gameThread != null && gameThread.isAlive()) {
            return;
        }

        running = true;
        if (!isInCutscene()) {
            gameMode = GameMode.PLAYING;
        }
        if (isPlaying()) {
            timer.startTimer();
        }

        gameThread = new Thread(this, "HawakKoAngBit-GameLoop");
        gameThread.start();
    }

    public void stopGameThread() {
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

    public void pauseGame() {
        if (!isPlaying()) {
            return;
        }

        keyHandler.resetKeys();
        timer.stopTimer();
        gameMode = GameMode.PAUSED;
        repaint();
        pauseMenu.show();
    }

    public void resumeGame() {
        if (!isPaused()) {
            return;
        }

        gameMode = GameMode.PLAYING;
        timer.resumeTimer();
        pauseMenu.hide();
        requestFocusInWindow();
    }

    public void startLevelScene(String sceneId, String filePattern, int frameCount, int frameDelayMs) {
        if (sceneManager.startScene(sceneId, filePattern, frameCount, frameDelayMs)) {
            gameMode = GameMode.CUTSCENE;
        } else {
            gameMode = GameMode.PLAYING;
        }
    }

    public void skipScene() {
        sceneManager.skip();
        if (sceneManager.isFinished()) {
            gameMode = GameMode.PLAYING;
            timer.startTimer();
        }
    }

    @Override
    public void run() {
        double drawInterval = 1000000000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long fpsTimer = 0;
        int drawCount = 0;

        while (running && !Thread.currentThread().isInterrupted()) {
            long currentTime = System.nanoTime();
            long elapsed = currentTime - lastTime;
            delta += elapsed / drawInterval;
            fpsTimer += elapsed;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
                drawCount++;
            }

            if (SHOW_FPS_IN_CONSOLE && fpsTimer >= 1000000000L) {
                System.out.println("FPS: " + drawCount);
                drawCount = 0;
                fpsTimer = 0;
            }
        }
    }

    public void update() {
        if (isPlaying()) {
            updateGameplay();
        } else if (isInCutscene()) {
            updateCutscene();
        }
    }

    private void updateGameplay() {
        player.update();
        clampPlayerToWorld();

        for (Enemy enemy : enemies) {
            if (enemy != null && enemy.isAlive()) {
                enemy.update();
                clampEnemyToWorld(enemy);
           }
        }

        combatResolver.resolve();
        enemies.removeIf(enemy -> !enemy.isAlive());
        camera.update(player);

        if (player.getHp() <= 0) {
            handleDefeat();
            return;
        }

        if (isTutorialExitReached()) {
            completeTutorial();
            return;
        }

        timer.setTimeScore();

        if (currentLevel != Level.TUTORIAL && enemies.isEmpty()) {
            handleLevelCleared();
        }
    }

    private void updateCutscene() {
        sceneManager.update();
        if (sceneManager.isFinished()) {
            gameMode = GameMode.PLAYING;
            timer.startTimer();
        }
    }

    private void clampPlayerToWorld() {
        player.worldX = Math.max(0, Math.min(player.worldX, Constants.maxWorldWidth - Constants.tileSize));
        player.worldY = Math.max(0, Math.min(player.worldY, Constants.maxWorldHeight - Constants.tileSize));
    }

    private void clampEnemyToWorld(Enemy enemy) {
        enemy.worldX = Math.max(0, Math.min(enemy.worldX, Constants.maxWorldWidth - Constants.tileSize));
        enemy.worldY = Math.max(0, Math.min(enemy.worldY, Constants.maxWorldHeight - Constants.tileSize));
    }

    private boolean isTutorialExitReached() {
        if (currentLevel != Level.TUTORIAL) {
            return false;
        }

        int playerCol = player.worldX / Constants.tileSize;
        int playerRow = player.worldY / Constants.tileSize;
        return playerCol == 0 && (playerRow == 47 || playerRow == 48);
    }

    private void completeTutorial() {
        player.setDirection("idle");
        levelsCleared++;
        if (onLevelComplete != null) {
            onLevelComplete.run();
        }
        setLevel(Level.LEVEL_1);
    }

    private void handleLevelCleared() {
        if (resolvingLevelOutcome) {
            return;
        }

        resolvingLevelOutcome = true;
        keyHandler.resetKeys();
        timer.stopTimer();
        timer.setFinalTimeScore();
        currentLevel.setMaxTimeScore(timer.getTimeScore());
        gameMode = GameMode.PAUSED;
        repaint();

        SwingUtilities.invokeLater(this::showLevelClearedDialog);
    }

    private void showLevelClearedDialog() {
        boolean hasNextLevel = currentLevel.nextLevel != null;
        String message = hasNextLevel
            ? "All enemies eliminated. Proceed to the next level?"
            : "All enemies eliminated. Return to home screen?";
        int result = JOptionPane.showConfirmDialog(this, message, "Level Cleared", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            player.setDirection("idle");
            levelsCleared++;
            if (onLevelComplete != null) {
                onLevelComplete.run();
            }

            if (hasNextLevel) {
                setLevel(currentLevel.nextLevel);
            } else {
                exitToHome();
            }
        } else {
            exitToHome();
        }
    }

    private void handleDefeat() {
        if (resolvingDefeat) {
            return;
        }

        resolvingDefeat = true;
        keyHandler.resetKeys();
        timer.stopTimer();
        gameMode = GameMode.DEFEAT;
        repaint();

        SwingUtilities.invokeLater(this::showDefeatDialog);
    }

    private void showDefeatDialog() {
        Object[] options = {"Restart", "Exit"};
        int result = JOptionPane.showOptionDialog(
            this,
            "You were defeated.",
            "Defeat",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null,
            options,
            options[0]
        );

        if (result == JOptionPane.YES_OPTION) {
            setLevel(currentLevel);
        } else {
            exitToHome();
        }
    }

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

        tileManager.draw(g2);

        if (isInCutscene()) {
            drawCutscene(g2);
            return;
        }

        drawActiveGameplay(g2);
        hudRenderer.draw(g2, currentLevel, timer, player, enemies, enemyTotals);
    }

    private void drawCutscene(Graphics2D g2) {
        sceneManager.render(g2);
        g2.setColor(Color.WHITE);
        g2.setFont(MethodUtilities.getFont(20f));
        String text = "Press ESC to skip";
        int textWidth = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, (getWidth() - textWidth) / 2, getHeight() - 40);
    }

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

        if (isPaused() || gameMode == GameMode.DEFEAT) {
            drawDarkOverlay(g2, 145);
        }
    }

    private void drawDarkOverlay(Graphics2D g2, int alpha) {
        g2.setColor(new Color(0, 0, 0, alpha));
        g2.fillRect(0, 0, Constants.screenWidth, Constants.screenHeight);
    }

    public boolean isPlaying() {
        return gameMode == GameMode.PLAYING;
    }

    public boolean isPaused() {
        return gameMode == GameMode.PAUSED;
    }

    public boolean isInCutscene() {
        return gameMode == GameMode.CUTSCENE;
    }


    // Getters
    // -----------------------------------
    public Level getCurrentLevel() {
        return currentLevel;
    }

    public Timer getTimer() {
        return timer;
    }

    public int getLevelsCleared() {
        return levelsCleared;
    }

    public Player getPlayer() {
        return player;
    }

    public TileManager getTileManager() {
        return tileManager;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    public List<Laser> getLasers() {
        return lasers;
    }

    public int getCameraX() {
        return camera.getScreenX();
    }

    public int getCameraY() {
        return camera.getScreenY();
    }

    public int getCameraWorldX() {
        return camera.getWorldX();
    }

    public int getCameraWorldY() {
        return camera.getWorldY();
    }
    // --------------------------------------
}
