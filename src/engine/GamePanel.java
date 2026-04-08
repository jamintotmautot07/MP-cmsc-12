
package engine;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JOptionPane;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
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

    KeyHandler keyH = new KeyHandler();
    Thread gameThread;
    private volatile boolean running = false;

    // Timer
    private Level currentLevel = Level.TUTORIAL;
    private Timer timer = new Timer(currentLevel.timeLimitSeconds);


    //Entities
    public Player player = new Player(this, keyH);
    TileManager tileM;

    //FPS
    final int fps = 60; //60 frames per second

    private JPanel header = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    public JButton backButton = new JButton("Home");

    public GamePanel() {
        setPreferredSize(new Dimension(Constants.screenWidth, Constants.screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);
        this.setLayout(new BorderLayout());

        //Entities
        tileM = new TileManager(this);

        backButton.setFocusPainted(false);
        backButton.setFocusable(true);
        header.setFocusable(false);
        header.add(backButton);
        header.setOpaque(false);

        add(header, BorderLayout.NORTH);

        backButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                backButton.setFocusable(true);
                backButton.requestFocusInWindow();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                backButton.setFocusable(false);
            }
        });
        
    }

    public void setLevel(Level level) {
        if (level == null) {
            level = Level.TUTORIAL;
        }

        this.currentLevel = level;
        this.timer = new Timer(level.timeLimitSeconds);
        this.timer.startTimer();
        tileM.loadMap(level.mapPath);
        this.player.setDefaultValues();
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    public void startGameThread() {
        running = true;
        gameThread = new Thread(this);

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

        running = false;
        if(gameThread != null) {
            gameThread.interrupt();
        }
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
        player.update();
        if (timer != null) {
            timer.setTimeScore(); // time based score
            if (timer.isTimeUp() && currentLevel.nextLevel != null) {
                int result = JOptionPane.showConfirmDialog(this, "Time's up! Proceed to next level?", "Level Complete", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    setLevel(currentLevel.nextLevel);
                } else {
                    // Stop the game or go back to menu
                    running = false;
                }
            }
        }
    }

    public void paintComponent(Graphics g){
        // Draw everything

        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(Color.WHITE);
        g2.drawString("Level: " + (currentLevel != null ? currentLevel.name : "Unknown"), 20, 20);

        if (timer != null) {
            timer.show(g2, 20, 40);
        }

        // DRAW THE COMPONENTS

        // tiles
        tileM.draw(g2);

        // player
        player.draw(g2);
    }
}
