
package engine;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;

import util.Constants;
import util.MethodUtilities.CustomButton;
import util.MethodUtilities;
import util.UtilityTool;
import ui.IntroManager;
import systems.KeyHandler;
import entity.Enemy;
import entity.Player;
import entity.Worm;
import entity.Trojan;
import entity.VirusDrone;
import entity.CoreBoss;
import entity.Projectile;
import entity.Laser;
import systems.Timer;
import systems.CollisionManager;
import Tile.TileManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 OWNER: Jamin

 PURPOSE:
 - Main game surface
 - Handles game loop + rendering

 TASKS:
 1. Implement Runnable
 2. Create game loop (while running)
 3. Separate:
    - update() → logic
    - render() → drawing
 4. Call entity updates

 OPTIONAL ADDITION:
 - FPS counter
*/

public class GamePanel extends JPanel implements Runnable {

    KeyHandler keyH = new KeyHandler(this);
    Thread gameThread;
    private volatile boolean running = false;

    // Scenes
    public IntroManager sceneManager;

    // Timer
    private Level currentLevel = Level.LEVEL_1;
    public Timer timer = new Timer(currentLevel.timeLimitSeconds);
    private int levelsCleared = 0;
    public Runnable onLevelComplete;
    private JDialog pauseDialog;

    // Game State
    public int gameState;
    public final int playState = 0;
    public final int pausedState = 1;
    public final int cutsceneState = 2;
    public final int defeatState = 3;

    //Entities
    public Player player = new Player(this, keyH);
    public TileManager tileM;
    public List<Enemy> enemies = new ArrayList<>();
    public List<Projectile> projectiles = new ArrayList<>();
    public List<Laser> lasers = new ArrayList<>();
    private int cameraX;
    private int cameraY;
    private int cameraWorldX;
    private int cameraWorldY;
    private final Map<String, Integer> enemyTotals = new LinkedHashMap<>();
    private boolean resolvingLevelOutcome = false;
    private boolean resolvingDefeat = false;

    //FPS
    final int fps = 60; //60 frames per second

    private JPanel header = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    public GamePanel() {
        setPreferredSize(new Dimension(Constants.screenWidth, Constants.screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);
        this.setLayout(new BorderLayout());

        //Entities
        tileM = new TileManager(this);
        cameraX = player.screenX;
        cameraY = player.screenY;
        sceneManager = new IntroManager();

        header.setFocusable(false);
        header.setOpaque(false);

        add(header, BorderLayout.NORTH);   
    }

    public void setLevel(Level level) {
        if (level == null) {
            level = Level.TUTORIAL;
        }

        if (level == Level.TUTORIAL) {
            levelsCleared = 0;
        }

        player.setDirection("idle");
        keyH.resetKeys();
        this.currentLevel = level;
        this.timer = new Timer(level.timeLimitSeconds);
        this.timer.startTimer();
        tileM.loadMap(level.mapPath);
        this.player.setLevelStartPosition(level.positionX, level.positionY);
        
        // Initialize enemies for the level
        enemies.clear();
        projectiles.clear();
        lasers.clear();
        enemyTotals.clear();
        resolvingLevelOutcome = false;
        resolvingDefeat = false;
        initializeEnemiesForLevel(level);
        
        updateCamera();
        if (gameState != cutsceneState) {
            gameState = playState;
        }
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    public TileManager getTileManager() {
        return tileM;
    }

    public void addEnemy(Enemy enemy) {
        // Enemy list management is now wired into this GamePanel version.
        if (enemy != null) {
            enemies.add(enemy);
            String type = UtilityTool.getEntityName(enemy);
            enemyTotals.put(type, enemyTotals.getOrDefault(type, 0) + 1);
        }
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

    /**
     * Initialize enemies based on the current level.
     */
    private void initializeEnemiesForLevel(Level level) {
        if (level == Level.LEVEL_1) {
            initializeLevel1();
        } else if (level == Level.LEVEL_2) {
            initializeLevel2();
        } else if (level == Level.LEVEL_3) {
            initializeLevel3();
        }
        // Tutorial level has no enemies
    }

    /**
     * Level 1: 20 worms with predefined positions.
     */
    private void initializeLevel1() {
        for (int i = 0; i < 15; i++) {
            Worm worm = new Worm(this);
            UtilityTool.setRandomEnemyPosition(worm, tileM);
            addEnemy(worm);
        }
    }

    /**
     * Level 2: 15 worms, 1 trojan, 6 virus enemies with random positions.
     */
    private void initializeLevel2() {
        // Add 15 worms
        for (int i = 0; i < 10; i++) {
            Worm worm = new Worm(this);
            UtilityTool.setRandomEnemyPosition(worm, tileM);
            addEnemy(worm);
        }

        // Add 1 trojan
        Trojan trojan = new Trojan(this);
        UtilityTool.setRandomEnemyPosition(trojan, tileM);
        addEnemy(trojan);

        // Add 6 virus enemies
        for (int i = 0; i < 6; i++) {
            VirusDrone virus = new VirusDrone(this);
            UtilityTool.setRandomEnemyPosition(virus, tileM);
            addEnemy(virus);
        }
    }

    /**
     * Level 3: 10 worms, 5 virus, 3 trojans, and the boss.
     */
    private void initializeLevel3() {
        // Add 10 worms
        for (int i = 0; i < 10; i++) {
            Worm worm = new Worm(this);
            UtilityTool.setRandomEnemyPosition(worm, tileM);
            addEnemy(worm);
        }

        // Add 5 virus enemies
        for (int i = 0; i < 5; i++) {
            VirusDrone virus = new VirusDrone(this);
            UtilityTool.setRandomEnemyPosition(virus, tileM);
            addEnemy(virus);
        }

        // Add 3 trojans
        for (int i = 0; i < 3; i++) {
            Trojan trojan = new Trojan(this);
            UtilityTool.setRandomEnemyPosition(trojan, tileM);
            addEnemy(trojan);
        }

        // Add the boss
        CoreBoss boss = new CoreBoss(this);
        boss.setStartTilePosition(24, 21);
        addEnemy(boss);
    }

    public int getLevelsCleared() {
        return levelsCleared;
    }

    private void handleLevelCleared() {
        if (resolvingLevelOutcome) {
            return;
        }

        resolvingLevelOutcome = true;
        keyH.resetKeys();
        timer.stopTimer();
        timer.setFinalTimeScore();
        currentLevel.setMaxTimeScore(timer.getTimeScore());
        gameState = pausedState;
        repaint();

        boolean hasNextLevel = currentLevel.nextLevel != null;
        String message = hasNextLevel
            ? "All enemies eliminated. Proceed to the next level?"
            : "All enemies eliminated. Return to home screen?";
        int result = JOptionPane.showConfirmDialog(this, message, "Level Cleared", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            player.setDirection("idle");
            levelsCleared++;
            if (onLevelComplete != null) onLevelComplete.run();

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
        keyH.resetKeys();
        timer.stopTimer();
        gameState = defeatState;
        repaint();

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

    public void startGameThread() {
        running = true;
        gameThread = new Thread(this);
        if (gameState != cutsceneState) {
            gameState = playState;
        }

        // initialize timer
        if (gameState == playState) {
            timer.startTimer();
        }

        gameThread.start();
    }

    public void startLevelScene(String sceneId, String filePattern, int frameCount, int frameDelayMs) {
        if (sceneManager.startScene(sceneId, filePattern, frameCount, frameDelayMs)) {
            gameState = cutsceneState;
        } else {
            gameState = playState;
        }
    }

    public void skipScene() {
        sceneManager.skip();
        if (sceneManager.isFinished()) {
            gameState = playState;
            timer.startTimer();
        }
    }

    public void stopGameThread() {

        //reset player defualt values
        player.setDefaultValues();

        // stop timer
        timer.stopTimer();
        timer.resetTimer();

        if (pauseDialog != null && pauseDialog.isVisible()) {
            pauseDialog.setVisible(false);
        }

        running = false;
        if(gameThread != null) {
            gameThread.interrupt();
        }
    }

    public void pauseGame() {
        if (gameState != playState) {
            return;
        }

        keyH.resetKeys();
        timer.stopTimer();
        gameState = pausedState;
        repaint();
        showPauseDialog();
    }

    public void resumeGame() {
        if (gameState != pausedState) {
            return;
        }

        gameState = playState;
        timer.resumeTimer();

        if (pauseDialog != null && pauseDialog.isVisible()) {
            pauseDialog.setVisible(false);
        }
    }

    private void showPauseDialog() {
        if (pauseDialog == null) {
            createPauseDialog();
        }

        pauseDialog.pack();
        pauseDialog.setLocationRelativeTo(this);
        pauseDialog.setVisible(true);
    }

    private void createPauseDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        pauseDialog = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        pauseDialog.setUndecorated(true);
        pauseDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(16, 16));
        content.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 16, 16));
        content.setBackground(Color.DARK_GRAY);

        JLabel title = new JLabel("Game Paused", JLabel.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(MethodUtilities.getFont(25f, this));
        content.add(title, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 12, 12));
        buttonPanel.setBackground(Color.DARK_GRAY);

        CustomButton resumeButton = new CustomButton("Resume");
        CustomButton homeButton = new CustomButton("Home");
        CustomButton settingsButton = new CustomButton("Settings");
        CustomButton mapButton = new CustomButton("Map");

        resumeButton.addActionListener(e -> resumeGame());
        homeButton.addActionListener(e -> {

            int choice = JOptionPane.showConfirmDialog(
                pauseDialog,
                "Are you sure you want to exit?",
                "BACK TO HOME SCREEN",
                JOptionPane.WARNING_MESSAGE,
                JOptionPane.YES_NO_OPTION
            );

            // if confirmed, back to home screen/ parent windows
            if(choice == JOptionPane.YES_OPTION) {
                pauseDialog.setVisible(false);
                gameState = playState;
                stopGameThread();

                Window ownerFrame = SwingUtilities.getWindowAncestor(this);
                if (ownerFrame instanceof main.BaseFrame) {
                    ((main.BaseFrame) ownerFrame).showOpeningScreen();
                }
            }
            
        });
        settingsButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(pauseDialog, "Settings are coming soon.", "Settings", JOptionPane.INFORMATION_MESSAGE);
        });
        mapButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(pauseDialog,
                String.format("Player position: (%d, %d)\nMap preview not implemented yet.", player.worldX, player.worldY),
                "Map", JOptionPane.INFORMATION_MESSAGE);
        });

        buttonPanel.add(resumeButton);
        buttonPanel.add(homeButton);
        buttonPanel.add(settingsButton);
        buttonPanel.add(mapButton);

        content.add(buttonPanel, BorderLayout.CENTER);
        pauseDialog.setContentPane(content);
        pauseDialog.pack();
    }

    @Override
    public void run(){
        // Game loop
        // while(running){
        //   update();
        //   repaint();
        // }

        // simulate a frame by frame running
        // using the "Delta/Accumulated" method (Fixed Timestep)
        double drawInterval = 1000000000 / fps; //0.01666 seconds
        double delta = 0;
        long LastTime = System.nanoTime();
        long currentTime;
        long timer = 0;
        int drawCount = 0;

        while (running && !Thread.currentThread().isInterrupted()) {
            currentTime = System.nanoTime();
            delta += (currentTime - LastTime) / drawInterval;
            timer += (currentTime - LastTime);
            LastTime = currentTime;

            if(delta >= 1) {
                update();
                repaint();
                delta--;
                drawCount++;
            }

            if(timer >= 1000000000) {
                System.out.println("FPS: " + drawCount);
                drawCount = 0;
                timer = 0;
            }
        }
    }

    public void update(){
        if (gameState == playState) {
            player.update();

            // clamp player world position to the tilemap bounds
            player.worldX = Math.max(0, Math.min(player.worldX, Constants.maxWorldWidth - Constants.tileSize));
            player.worldY = Math.max(0, Math.min(player.worldY, Constants.maxWorldHeight - Constants.tileSize));

            // Update all enemies
            for (int i = 0; i < enemies.size(); i++) {
                Enemy enemy = enemies.get(i);
                if (enemy.isAlive()) {
                    enemy.update();
                }
            }

            // Update projectiles and resolve their collisions.
            Iterator<Projectile> projectileIterator = projectiles.iterator();
            while (projectileIterator.hasNext()) {
                Projectile projectile = projectileIterator.next();
                projectile.update();
                if (!projectile.isAlive()) {
                    projectileIterator.remove();
                    continue;
                }

                Rectangle projectileBounds = projectile.getBounds();
                if (projectile.getOwnerType() == Projectile.OwnerType.PLAYER) {
                    for (Enemy enemy : enemies) {
                        if (!enemy.isAlive()) {
                            continue;
                        }
                        Rectangle enemyBounds = new Rectangle(
                            enemy.worldX + enemy.solidArea.x,
                            enemy.worldY + enemy.solidArea.y,
                            enemy.solidArea.width,
                            enemy.solidArea.height
                        );
                        if (CollisionManager.rectanglesIntersect(projectileBounds, enemyBounds)) {
                            enemy.takeDamage(projectile.getDamage());
                            enemy.damageReaction();
                            projectile.kill();
                            projectileIterator.remove();
                            break;
                        }
                    }
                    continue;
                }

                Rectangle playerBounds = new Rectangle(
                    player.worldX + player.solidArea.x,
                    player.worldY + player.solidArea.y,
                    player.solidArea.width,
                    player.solidArea.height
                );
                if (CollisionManager.rectanglesIntersect(projectileBounds, playerBounds)) {
                    player.takeDamage(projectile.getDamage());
                    projectile.kill();
                    projectileIterator.remove();
                }
            }

            // Update lasers and resolve laser hits.
            Iterator<Laser> laserIterator = lasers.iterator();
            while (laserIterator.hasNext()) {
                Laser laser = laserIterator.next();
                laser.update();
                if (!laser.isAlive()) {
                    laserIterator.remove();
                    continue;
                }

                Rectangle playerBounds = new Rectangle(
                    player.worldX + player.solidArea.x,
                    player.worldY + player.solidArea.y,
                    player.solidArea.width,
                    player.solidArea.height
                );
                if (laser.getOwnerType() == Laser.OwnerType.ENEMY && CollisionManager.rectanglesIntersect(laser.getBounds(), playerBounds)) {
                    player.takeDamage(laser.getDamage());
                }
            }

            // Player melee attack hits enemies.
            if (player.isAttackActive()) {
                Rectangle playerAttack = player.getAttackHitbox();
                for (Enemy enemy : enemies) {
                    if (!enemy.isAlive()) {
                        continue;
                    }
                    Rectangle enemyBounds = new Rectangle(
                        enemy.worldX + enemy.solidArea.x,
                        enemy.worldY + enemy.solidArea.y,
                        enemy.solidArea.width,
                        enemy.solidArea.height
                    );
                    if (CollisionManager.rectanglesIntersect(playerAttack, enemyBounds)) {
                        enemy.takeDamage(1);
                        enemy.damageReaction();
                    }
                }
            }

            // Enemy melee attack boxes hit the player.
            Rectangle playerBounds = new Rectangle(
                player.worldX + player.solidArea.x,
                player.worldY + player.solidArea.y,
                player.solidArea.width,
                player.solidArea.height
            );
            for (Enemy enemy : enemies) {
                if (!enemy.isAlive()) {
                    continue;
                }
                if (enemy.isAttackActive() && CollisionManager.rectanglesIntersect(enemy.getAttackHitbox(), playerBounds)) {
                    player.takeDamage(enemy.getDamage());
                }
            }

            // Player dash collision with enemies.
            if (player.isDashing()) {
                Rectangle dashBounds = new Rectangle(
                    player.worldX + player.solidArea.x,
                    player.worldY + player.solidArea.y,
                    player.solidArea.width,
                    player.solidArea.height
                );
                for (Enemy enemy : enemies) {
                    if (!enemy.isAlive()) {
                        continue;
                    }
                    Rectangle enemyBounds = new Rectangle(
                        enemy.worldX + enemy.solidArea.x,
                        enemy.worldY + enemy.solidArea.y,
                        enemy.solidArea.width,
                        enemy.solidArea.height
                    );
                    if (CollisionManager.rectanglesIntersect(dashBounds, enemyBounds)) {
                        enemy.takeDamage(1);
                        enemy.damageReaction();
                    }
                }
            }

            // Enemy body collisions against player.
            for (Enemy enemy : enemies) {
                if (!enemy.isAlive()) {
                    continue;
                }
                Rectangle enemyBounds = new Rectangle(
                    enemy.worldX + enemy.solidArea.x,
                    enemy.worldY + enemy.solidArea.y,
                    enemy.solidArea.width,
                    enemy.solidArea.height
                );
                if (CollisionManager.rectanglesIntersect(playerBounds, enemyBounds)) {
                    player.takeDamage(enemy.getDamage());
                }
            }

            // Remove dead enemies
            enemies.removeIf(enemy -> !enemy.isAlive());

            updateCamera();

            if (player.getHp() <= 0) {
                handleDefeat();
                return;
            }

            if(currentLevel == Level.TUTORIAL){    
                if(this.player.worldX/Constants.tileSize == 0
                    && (this.player.worldY/Constants.tileSize == 47 || this.player.worldY/Constants.tileSize == 48)) 
                {
                    player.setDirection("idle");
                    levelsCleared++;
                    if(onLevelComplete != null) onLevelComplete.run();
                    setLevel(Level.LEVEL_1);
                }
            }

            if (timer != null) {
                timer.setTimeScore(); // time based score
            }

            if (currentLevel != Level.TUTORIAL && enemies.isEmpty()) {
                handleLevelCleared();
            }
        } else if (gameState == pausedState) {
            // Pause state: game logic is frozen and timer is already stopped.
            return;
        } else if (gameState == cutsceneState) {
            sceneManager.update();
            if (sceneManager.isFinished()) {
                gameState = playState;
                timer.startTimer();
            }
            return;
        }
    }

    // for the control of the camera movement
    // so that when the edge of the map is reached, it allows the player to move out the default center position
    private void updateCamera() {
        if (player.worldX < player.screenX) {
            cameraX = player.worldX;
            cameraWorldX = 0;
        } else if (player.worldX > Constants.maxWorldWidth - (Constants.screenWidth - player.screenX)) {
            cameraX = Constants.screenWidth - (Constants.maxWorldWidth - player.worldX);
            cameraWorldX = Constants.maxWorldWidth - Constants.screenWidth;
        } else {
            cameraX = player.screenX;
            cameraWorldX = player.worldX - player.screenX;
        }

        if (player.worldY < player.screenY) {
            cameraY = player.worldY;
            cameraWorldY = 0;
        } else if (player.worldY > Constants.maxWorldHeight - (Constants.screenHeight - player.screenY)) {
            cameraY = Constants.screenHeight - (Constants.maxWorldHeight - player.worldY);
            cameraWorldY = Constants.maxWorldHeight - Constants.screenHeight;
        } else {
            cameraY = player.screenY;
            cameraWorldY = player.worldY - player.screenY;
        }
    }

    public int getCameraX() {
        return cameraX;
    }

    public int getCameraY() {
        return cameraY;
    }

    public int getCameraWorldX() {
        return cameraWorldX;
    }

    public int getCameraWorldY() {
        return cameraWorldY;
    }

    public void paintComponent(Graphics g){
        // Draw everything

        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // DRAW THE COMPONENTS

        // tiles
        tileM.draw(g2);

        if (gameState == cutsceneState) {
            sceneManager.render(g2);
            g2.setColor(Color.WHITE);
            g2.setFont(MethodUtilities.getFont(20f));
            String text = "Press ESC to skip";
            int textWidth = g2.getFontMetrics().stringWidth(text);
            g2.drawString(text, (getWidth() - textWidth) / 2, getHeight() - 40);
            return;
        }

        // Draw projectiles and lasers before characters so effects appear under the player/enemies.
        for (Projectile projectile : projectiles) {
            if (projectile.isAlive()) {
                projectile.draw(g2, cameraWorldX, cameraWorldY);
            }
        }

        for (Laser laser : lasers) {
            if (laser.isAlive()) {
                laser.draw(g2, cameraWorldX, cameraWorldY);
            }
        }

        // Draw enemies
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                enemy.render(g2);
            }
        }

        if (gameState == playState) {
            drawDarkOverlay(g2, 55);
        }

        // player
        player.draw(g2);

        if (gameState == pausedState || gameState == defeatState) {
            drawDarkOverlay(g2, 145);
        }

        g2.setColor(Color.WHITE);
        g2.drawString("Level: " + (currentLevel != null ? currentLevel.name : "Unknown"), 20, 20);
    
        if (timer != null) {
            timer.show(g2, 20, 40);
        }

        drawPlayerLife(g2);
        drawEnemyCounter(g2);
    }

    private void drawPlayerLife(Graphics2D g2) {
        int heartSize = 20;
        int spacing = 27;
        int startX = 20;
        int startY = 60;
        int hp = player.getHp();
        int maxHp = player.getMaxHp();
        int heartCount = (maxHp + 1) / 2;
        int panelWidth = 48 + heartCount * spacing;
        int panelHeight = 34;

        g2.setColor(new Color(0, 0, 0, 145));
        g2.fillRoundRect(startX - 8, startY - 8, panelWidth, panelHeight, 14, 14);
        g2.setColor(new Color(255, 255, 255, 45));
        g2.drawRoundRect(startX - 8, startY - 8, panelWidth, panelHeight, 14, 14);

        g2.setColor(Color.WHITE);
        g2.setFont(MethodUtilities.getFont(14f, this));
        g2.drawString("HP", startX, startY + 14);

        int heartX = startX + 30;
        for (int i = 0; i < heartCount; i++) {
            int heartLife = hp - (i * 2);
            drawHeart(g2, heartX + (i * spacing), startY, heartSize, heartLife);
        }
    }

    private void drawHeart(Graphics2D g2, int x, int y, int size, int heartLife) {
        if (heartLife > 0) {
            g2.setColor(new Color(255, 70, 90, 85));
            fillHeart(g2, x - 3, y - 3, size + 6);
        }

        g2.setColor(new Color(45, 45, 45, 190));
        fillHeart(g2, x, y, size);

        if (heartLife > 0) {
            Graphics2D clipped = (Graphics2D) g2.create();
            int fillWidth = heartLife >= 2 ? size : size / 2;
            clipped.setClip(x, y, fillWidth, size);
            clipped.setColor(new Color(220, 30, 45));
            fillHeart(clipped, x, y, size);
            clipped.dispose();
        }

        g2.setColor(Color.BLACK);
        drawHeartOutline(g2, x, y, size);
    }

    private void fillHeart(Graphics2D g2, int x, int y, int size) {
        int half = size / 2;
        g2.fillOval(x, y, half, half);
        g2.fillOval(x + half, y, half, half);

        int[] xPoints = {x, x + size, x + half};
        int[] yPoints = {y + half / 2, y + half / 2, y + size};
        g2.fillPolygon(xPoints, yPoints, 3);
    }

    private void drawHeartOutline(Graphics2D g2, int x, int y, int size) {
        int half = size / 2;
        g2.drawOval(x, y, half, half);
        g2.drawOval(x + half, y, half, half);

        int[] xPoints = {x, x + size, x + half};
        int[] yPoints = {y + half / 2, y + half / 2, y + size};
        g2.drawPolygon(xPoints, yPoints, 3);
    }

    private void drawEnemyCounter(Graphics2D g2) {
        if (enemyTotals.isEmpty()) {
            return;
        }

        int aliveTotal = 0;
        Map<String, Integer> aliveByType = new LinkedHashMap<>();
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) {
                continue;
            }
            String type = UtilityTool.getEntityName(enemy);
            aliveByType.put(type, aliveByType.getOrDefault(type, 0) + 1);
            aliveTotal++;
        }

        int width = 210;
        int lineHeight = 18;
        int rows = Math.max(1, enemyTotals.size());
        int height = 34 + rows * lineHeight;
        int x = Constants.screenWidth - width - 18;
        int y = 18;

        g2.setColor(new Color(0, 0, 0, 155));
        g2.fillRoundRect(x, y, width, height, 16, 16);
        g2.setColor(new Color(255, 255, 255, 55));
        g2.drawRoundRect(x, y, width, height, 16, 16);

        g2.setFont(MethodUtilities.getFont(14f, this));
        g2.setColor(new Color(245, 245, 245));
        g2.drawString("Enemies: " + aliveTotal, x + 12, y + 20);

        int textY = y + 40;
        for (Map.Entry<String, Integer> entry : enemyTotals.entrySet()) {
            int alive = aliveByType.getOrDefault(entry.getKey(), 0);
            g2.setColor(new Color(210, 230, 255));
            g2.drawString(entry.getKey() + ": " + alive + "/" + entry.getValue(), x + 12, textY);
            textY += lineHeight;
        }
    }

    private void drawDarkOverlay(Graphics2D g2, int alpha) {
        g2.setColor(new Color(0, 0, 0, alpha));
        g2.fillRect(0, 0, Constants.screenWidth, Constants.screenHeight);
    }
}
