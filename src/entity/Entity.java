
package entity;

import java.awt.image.BufferedImage;
import java.awt.Rectangle;;

/*
 OWNER: Jamin

 PURPOSE:
 - Base class for ALL objects

 TASKS:
 1. Add positions (x, y)

 NOTE:
 - All entities MUST extend this
*/

public class Entity {
    
    public int worldX, worldY;
    public int speed;

    public BufferedImage up[], right[], left[], down[], idle[];
    public String direction;

    public int spriteCounter = 0;
    public int spriteNum = 1;

    public Rectangle solidArea;
    public boolean collisionOn = false;

}
