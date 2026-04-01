
package engine;

import javax.swing.JButton;
import javax.swing.JPanel;

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


    //Entities
    Player player = new Player(this, keyH);

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

    public void startGameThread() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void stopGameThread() {

        //reset player defualt values
        player.setDefaultValues();

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
                drawCount = 0;
                timer = 0;
            }
        }
    }

    public void update(){
        player.update();
    }

    public void paintComponent(Graphics g){
        // Draw everything

        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;


        // DRAW THE COMPONENTS

        // player
        player.draw(g2);
    }
}
