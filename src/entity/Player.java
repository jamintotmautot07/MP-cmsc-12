
package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import engine.GamePanel;
import systems.KeyHandler;
import util.Constants;

/*
 OWNER: Jamin

 PURPOSE:
 - Main controllable character

 TASKS:
 1. Add movement (WASD)
 2. Add speed variable
 3. Add attack() method
 4. Add animation states (idle, move, attack)

 OPTIONAL:
 - Add health system
*/

public class Player extends Entity{

    GamePanel gp;
    KeyHandler keyH;

    public final int screenX;
    public final int screenY;

    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;

        screenX = (Constants.screenWidth/2) - (Constants.tileSize/2);
        screenY = (Constants.screenHeight/2) - (Constants.tileSize/2);

        solidArea = new Rectangle(8, 16, 30, 32);

        setDefaultValues();
    }

    public void setDefaultValues() {
        worldX = screenX;
        worldY = screenY;
        speed = 3;
        direction = "down";
    }

    public void update(){
        // TODO: Movement logic
        if(keyH.upPressed == true || keyH.rightPressed == true ||
            keyH.leftPressed == true || keyH.downPressed == true
        ) {

            if(keyH.upPressed == true) {
                direction = "up";
            } else if (keyH.leftPressed == true) {
                direction = "left";
            } else if (keyH.rightPressed == true) {
                direction = "right";
            } else if (keyH.downPressed == true) {
                direction = "down";
            }

            // Collision feature code here...
            // ... //

            switch (direction) {
                case "up": worldY -= speed; break;
                case "right": worldX += speed; break;
                case "left": worldX -= speed; break;
                case "down": worldY += speed; break;
            }

        }
    }

    public void draw(Graphics2D g2){
        // TODO: Draw player (box for now)

        //Initial character
        g2.setColor(Color.WHITE);
        g2.fillRect(worldX, worldY, Constants.tileSize, Constants.tileSize);
    }
}
