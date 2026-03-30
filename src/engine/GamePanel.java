
package engine;

import javax.swing.JPanel;
import java.awt.Graphics;

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

    public void run(){
        // TODO: Game loop
        // while(running){
        //   update();
        //   repaint();
        // }
    }

    public void update(){
        // TODO: Update player and entities
    }

    public void render(Graphics g){
        // TODO: Draw everything
    }
}
