
package engine;

import javax.swing.JButton;
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

import util.Constants;
import systems.KeyHandler;
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

    KeyHandler keyH = new KeyHandler(this);
    Thread gameThread;
    private volatile boolean running = false;

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

    //Entities
    public Player player = new Player(this, keyH);
    TileManager tileM;
    private int cameraX;
    private int cameraY;
    private int cameraWorldX;
    private int cameraWorldY;

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
        this.player.setDefaultValues();
        updateCamera();
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    public int getLevelsCleared() {
        return levelsCleared;
    }

    public void startGameThread() {
        running = true;
        gameThread = new Thread(this);
        gameState = playState;

        // initialize timer
        timer.startTimer();

        gameThread.start();
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
        title.setFont(title.getFont().deriveFont(18f));
        content.add(title, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 12, 12));
        buttonPanel.setBackground(Color.DARK_GRAY);

        JButton resumeButton = new JButton("Resume");
        JButton homeButton = new JButton("Home");
        JButton settingsButton = new JButton("Settings");
        JButton mapButton = new JButton("Map");

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
                this.timer.showTimeScore();
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
            updateCamera();

            if (timer != null) {
                timer.setTimeScore(); // time based score

                // for now, the goal first is to survive
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
                        // Stop the game or go back to menu
                        running = false;
                    }
                }
            }
        } else if (gameState == pausedState) {
            // Pause state: game logic is frozen and timer is already stopped.
            return;
        } else if (gameState == cutsceneState) {
            if(currentLevel == Level.LEVEL_1) {
                // show cutscene before level 1
            } else if (currentLevel == Level.LEVEL_2) {
                // show cutscene before level 2
            } else if (currentLevel == Level.LEVEL_3) {
                // show cutscene before level 3

                if(currentLevel.isCompleted()) {
                    // show ending scene after completion
                }
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

        // player
        player.draw(g2);

        g2.setColor(Color.WHITE);
        g2.drawString("Level: " + (currentLevel != null ? currentLevel.name : "Unknown"), 20, 20);
    
        if (timer != null) {
            timer.show(g2, 20, 40);
        }
    }
}
