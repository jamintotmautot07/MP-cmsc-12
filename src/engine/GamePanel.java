
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
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Random;

import util.Constants;
import systems.CollisionManager;
import util.MethodUtilities.CustomButton;
import util.MethodUtilities;
import ui.IntroManager;
import systems.KeyHandler;
import entity.Enemy;
import entity.Player;
import systems.Timer;
import Tile.TileManager;

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

    /*
     * Input handler for this panel.
     * It listens for keyboard events and translates them into game actions.
     */
    KeyHandler keyH = new KeyHandler(this);

    /*
     * The thread that drives the fixed-timestep game loop.
     * Keeping it separate from Swing's painting flow helps the game update at a steady pace.
     */
    Thread gameThread;
    private volatile boolean running = false;

    // Scene/cutscene manager used when the game temporarily leaves normal gameplay.
    public IntroManager sceneManager;

    // Level and timer state.
    // `currentLevel` decides which map and time limit are active.
    // `timer` tracks the player's survival time / remaining time for the current level.
    private Level currentLevel = Level.LEVEL_1;
    public Timer timer = new Timer(currentLevel.timeLimitSeconds);

    // Simple progression tracking used by the UI and scoreboard.
    private int levelsCleared = 0;

    // Optional callback that outside classes can assign when a level ends.
    public Runnable onLevelComplete;

    // Pause menu dialog shown on top of the game panel.
    private JDialog pauseDialog;

    // Very simple state machine for the panel.
    // Only one of these states should be active at a time.
    public int gameState;
    public final int playState = 0;
    public final int pausedState = 1;
    public final int cutsceneState = 2;

    /*
     * Core world objects owned by the panel.
     * `player` stores the controllable character.
     * `tileM` handles map loading, tile lookup, and tile rendering.
     */
    public Player player = new Player(this, keyH);
    public Enemy sample_enemy = new Enemy(Constants.maxScreenRow + (2 * Constants.tileSize), Constants.maxScreenCol + (2 * Constants.tileSize));
    public TileManager tileM;

    // Tutorial-only target box used to teach or test the attack mechanic.
    private Rectangle tutorialTargetBox;
    private boolean tutorialTargetActive = false;

    /*
     * Camera values split into two ideas:
     * - `cameraX/cameraY`: where the player is drawn on screen
     * - `cameraWorldX/cameraWorldY`: which world coordinate the camera is centered around
     */
    private int cameraX;
    private int cameraY;
    private int cameraWorldX;
    private int cameraWorldY;

    // Target update/render rate for the game loop.
    final int fps = 60; //60 frames per second

    // Lightweight header container reserved for HUD-like widgets.
    private JPanel header = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    public GamePanel() {
        // Match the panel size to the game's designed resolution.
        setPreferredSize(new Dimension(Constants.screenWidth, Constants.screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);
        this.setLayout(new BorderLayout());

        // Create the tile system before gameplay starts so maps can be loaded immediately.
        tileM = new TileManager(this);

        // The default camera starts centered on the player's screen anchor.
        cameraX = player.screenX;
        cameraY = player.screenY;
        sceneManager = new IntroManager();

        header.setFocusable(false);
        header.setOpaque(false);

        add(header, BorderLayout.NORTH);   
    }

    public void setLevel(Level level) {
        // If something passes in null, fall back to the safest possible level.
        if (level == null) {
            level = Level.TUTORIAL;
        }

        // Entering the tutorial resets progression for a clean run.
        if (level == Level.TUTORIAL) {
            levelsCleared = 0;
        }

        // Reset input-facing state before switching map data.
        player.setDirection("idle");
        keyH.resetKeys();
        this.currentLevel = level;

        // Rebuild timer and map data around the new level.
        this.timer = new Timer(level.timeLimitSeconds);
        this.timer.startTimer();
        tileM.loadMap(level.mapPath);

        // Place the player back at the default spawn and refresh tutorial helpers.
        this.player.setDefaultValues();
        resetTutorialAttackBox(level);
        updateCamera();
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    public TileManager getTileManager() {
        return tileM;
    }

    private void resetTutorialAttackBox(Level level) {
        if (level == Level.TUTORIAL) {
            int maxCol = Constants.worldMaxCol;
            int maxRow = Constants.worldMaxRow;
            int tileSize = Constants.tileSize;
            int boxCol = 2;
            int boxRow = 2;

            // Try a few random spots until a non-solid tile is found.
            // This keeps the target from spawning inside a wall.
            for (int tries = 0; tries < 20; tries++) {
                int nextCol = 2 + (int) (Math.random() * Math.max(1, maxCol - 4));
                int nextRow = 2 + (int) (Math.random() * Math.max(1, maxRow - 4));
                if (!tileM.isTileSolid(nextRow, nextCol)) {
                    boxCol = nextCol;
                    boxRow = nextRow;
                    break;
                }
            }

            // Store the box in world coordinates so camera movement can affect how it is drawn.
            tutorialTargetBox = new Rectangle(boxCol * tileSize, boxRow * tileSize, tileSize, tileSize);
            tutorialTargetActive = true;
        } else {
            // Outside the tutorial, the helper box should not exist at all.
            tutorialTargetActive = false;
            tutorialTargetBox = null;
        }
    }

    public int getLevelsCleared() {
        return levelsCleared;
    }

    public void startGameThread() {
        // Starting a new thread lets the panel update independently from Swing event callbacks.
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
        // If the scene starts successfully, gameplay is temporarily suspended.
        if (sceneManager.startScene(sceneId, filePattern, frameCount, frameDelayMs)) {
            gameState = cutsceneState;
        } else {
            gameState = playState;
        }
    }

    public void skipScene() {
        // The scene manager handles whether this means "start fading" or "finish instantly".
        sceneManager.skip();
        if (sceneManager.isFinished()) {
            gameState = playState;
            timer.startTimer();
        }
    }

    public void stopGameThread() {

        // Reset player state so the next launch does not inherit half-finished movement or attack state.
        player.setDefaultValues();

        // Stop and clear timer state before leaving the game screen.
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
        // Only active gameplay can be paused.
        if (gameState != playState) {
            return;
        }

        // Clear pressed keys so the player does not keep moving after resume.
        keyH.resetKeys();
        timer.stopTimer();
        gameState = pausedState;
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
        // Use the containing window as owner so the dialog stays centered and modal to the game.
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

    // MAIN LOOP OF THE GAME
    @Override
    public void run(){
        /*
         * Fixed timestep loop:
         * `delta` collects how much update-time has built up.
         * Once it reaches 1, the game processes one frame of logic and one repaint request.
         * This keeps game speed more stable than "update as fast as possible".
         */
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
                this.timer.showTimeScore();
                drawCount = 0;
                timer = 0;
            }
        }
    }

    public void update(){
        if (gameState == playState) {
            // The player owns its own movement, cooldown, and attack logic.
            player.update();
            sample_enemy.update();

            // Tutorial-specific check: the red box disappears once the player attack overlaps it.
            if (currentLevel == Level.TUTORIAL && tutorialTargetActive && player.isAttackActive()) {
                if (CollisionManager.rectanglesIntersect(player.getAttackHitbox(), tutorialTargetBox)) {
                    System.out.println("box hit");
                    tutorialTargetActive = false;
                }
            }

            // Clamp the player to the map boundaries so they cannot walk beyond the world.
            player.worldX = Math.max(0, Math.min(player.worldX, Constants.maxWorldWidth - Constants.tileSize));
            player.worldY = Math.max(0, Math.min(player.worldY, Constants.maxWorldHeight - Constants.tileSize));
            updateCamera();

            if (timer != null) {
                timer.setTimeScore(); // time based score

                // Current level progression rule: survive until the timer reaches zero.
                if (timer.isTimeUp() && currentLevel.nextLevel != null) {
                    int result = JOptionPane.showConfirmDialog(this, "Time's up! Proceed to next level?", "Level Complete", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        player.setDirection("idle");
                        levelsCleared++;
                        if (onLevelComplete != null) onLevelComplete.run();
                        timer.setFinalTimeScore();
                        currentLevel.setMaxTimeScore(timer.getTimeScore());
                        setLevel(currentLevel.nextLevel);
                    } else {
                        // For now this simply ends the loop instead of routing to a fail screen.
                        running = false;
                    }
                }
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

    /*
     * Camera logic:
     * Most of the time, the player stays centered on screen.
     * Near the map edges, the camera stops scrolling further, so the player appears to move away from center.
     */
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
        // Swing always expects custom painting to start with the superclass call.

        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Draw the world first so the player and overlays appear on top.
        tileM.draw(g2);

        if (gameState == cutsceneState) {
            // Cutscenes temporarily take over the entire panel.
            sceneManager.render(g2);
            g2.setColor(Color.WHITE);
            g2.setFont(MethodUtilities.getFont(20f));
            String text = "Press ESC to skip";
            int textWidth = g2.getFontMetrics().stringWidth(text);
            g2.drawString(text, (getWidth() - textWidth) / 2, getHeight() - 40);
            return;
        }

        // Draw the player after the tile map so they appear in front of the floor/walls.
        player.draw(g2);
        sample_enemy.render(g2);

        // The tutorial target is stored in world space, so convert it back to screen space before drawing.
        if (tutorialTargetActive && currentLevel == Level.TUTORIAL) {
            int screenX = tutorialTargetBox.x - cameraWorldX;
            int screenY = tutorialTargetBox.y - cameraWorldY;
            g2.setColor(new Color(255, 0, 0, 120));
            g2.fillRect(screenX, screenY, tutorialTargetBox.width, tutorialTargetBox.height);
            g2.setColor(Color.RED);
            g2.drawRect(screenX, screenY, tutorialTargetBox.width, tutorialTargetBox.height);
        }

        // Small HUD text for debugging / gameplay feedback.
        g2.setColor(Color.WHITE);
        g2.drawString("Level: " + (currentLevel != null ? currentLevel.name : "Unknown"), 20, 20);
    
        if (timer != null) {
            timer.show(g2, 20, 40);
        }
    }
}
