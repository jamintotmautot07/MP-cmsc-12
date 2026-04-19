
package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import engine.GamePanel;
import systems.KeyHandler;
import util.Constants;
// import util.ResourceCache; // COMMENTED OUT - Cache system disabled

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

    // for motion variables
    private int idleCounter = 0;
    private int downCounter = 0;
    private int upCounter = 0;
    private int leftCounter = 0;
    private int rightCounter = 0;
    private String previousDirection = "idle";
    private int movementAnimationSpeed = 8;
    private int idleAnimationSpeed = 13; // Slower for idle

    public enum AttackType {
        NONE,
        NORMAL,
        FORWARD,
        SIDE,
        DOWN
    }

    private AttackType attackType = AttackType.NONE;
    private int attackCounter = 0;
    private final int attackDuration = 18;

    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;

        screenX = (Constants.screenWidth/2) - (Constants.tileSize/2);
        screenY = (Constants.screenHeight/2) - (Constants.tileSize/2);

        solidArea = new Rectangle(8, 16, 30, 32);

        setDefaultValues();
        getPlayerImages();
    }

    public void setDefaultValues() {
        worldX = screenX;
        worldY = screenY;
        speed = 3;
        direction = "idle";
    }

    public void getPlayerImages() {
        //initialize lengths
        idle = new BufferedImage[7];
        down = new BufferedImage[4];
        up = new BufferedImage[6];
        left = new BufferedImage[6];
        right = new BufferedImage[6];

        try {
            
            // idle assets
            for(int i = 0; i < 7; i++) {
                idle[i] = ImageIO.read(new File(String.format("res/PlayerAssets/idle%d.png", i + 1)));
            }
            // down assets
            for(int i = 0; i < 4; i++) {
                down[i] = ImageIO.read(new File(String.format("res/PlayerAssets/down%d.png", i + 1)));
            }
            // up assets
            for(int i = 0; i < 6; i++) {
                up[i] = ImageIO.read(new File(String.format("res/PlayerAssets/up%d.png", i + 1)));
            }
            // left assets
            for(int i = 0; i < 6; i++) {
                left[i] = ImageIO.read(new File(String.format("res/PlayerAssets/left%d.png", i + 1)));
            }
            // right assets
            for(int i = 0; i < 6; i++) {
                right[i] = ImageIO.read(new File(String.format("res/PlayerAssets/right%d.png", i + 1)));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update(){
        if (!direction.equals(previousDirection)) {
            idleCounter = 0;
            upCounter = 0;
            downCounter = 0;
            leftCounter = 0;
            rightCounter = 0;
            spriteCounter = 0; 
        }
        previousDirection = direction;
        String oldDirection = direction;
        // TODO: Movement logic
        // update attack state first so movement can still happen while an attack button is held
        AttackType requestedAttack = AttackType.NONE;
        if (keyH.isActionPressed(KeyHandler.Action.ATTACK)) {
            if (keyH.isActionPressed(KeyHandler.Action.MOVE_UP)) {
                requestedAttack = AttackType.FORWARD;
                System.out.println("Attack: FORWARD");
            } else if (keyH.isActionPressed(KeyHandler.Action.MOVE_DOWN)) {
                requestedAttack = AttackType.DOWN;
                System.out.println("Attack: DOWN");
            } else if (keyH.isActionPressed(KeyHandler.Action.MOVE_LEFT) || keyH.isActionPressed(KeyHandler.Action.MOVE_RIGHT)) {
                requestedAttack = AttackType.SIDE;
                System.out.println("Attack: SIDE");
            } else {
                requestedAttack = AttackType.NORMAL;
                System.out.println("Attack: NORMAL");
            }
        }

        if (requestedAttack != AttackType.NONE) {
            if (attackType != requestedAttack) {
                attackType = requestedAttack;
                attackCounter = 0;
            }
            attackCounter++;
            if (attackCounter > attackDuration) {
                attackCounter = 0;
                attackType = AttackType.NONE;
            }
            direction = "idle"; // Keep animation idle during attacks
        } else {
            attackType = AttackType.NONE;
            attackCounter = 0;
        }

        if(keyH.isActionPressed(KeyHandler.Action.MOVE_UP) || keyH.isActionPressed(KeyHandler.Action.MOVE_RIGHT) ||
            keyH.isActionPressed(KeyHandler.Action.MOVE_LEFT) || keyH.isActionPressed(KeyHandler.Action.MOVE_DOWN)
        ) {

            if(keyH.isActionPressed(KeyHandler.Action.MOVE_UP)) {
                direction = "up";
                System.out.println("Moving: UP");
            } else if (keyH.isActionPressed(KeyHandler.Action.MOVE_LEFT)) {
                direction = "left";
                System.out.println("Moving: LEFT");
            } else if (keyH.isActionPressed(KeyHandler.Action.MOVE_RIGHT)) {
                direction = "right";
                System.out.println("Moving: RIGHT");
            } else if (keyH.isActionPressed(KeyHandler.Action.MOVE_DOWN)) {
                direction = "down";
                System.out.println("Moving: DOWN");
            }

            if (!oldDirection.equals(direction)) {
                spriteCounter = movementAnimationSpeed; // Trigger animation immediately on direction change
            }

            // Collision feature code here...
            // ... //

            switch (direction) {
                case "up": worldY -= speed; break;
                case "right": worldX += speed; break;
                case "left": worldX -= speed; break;
                case "down": worldY += speed; break;
            }

            //for the rate at which the image is changing for the motion effect
            spriteCounter++;
            if(spriteCounter > movementAnimationSpeed) {
                if(direction.equals("up")) {
                    if(upCounter == 0) {
                        upCounter = 1;
                    } else if (upCounter == 1) {
                        upCounter = 2;
                    } else if (upCounter == 2) {
                        upCounter = 3;
                    } else if (upCounter == 3) {
                        upCounter = 4;
                    } else if (upCounter == 4) {
                        upCounter = 5;
                    } else if (upCounter == 5) {
                        upCounter = 0;
                    }

                    System.out.println("UP");
                } else if (direction.equals("down")) {
                    if(downCounter == 0) {
                        downCounter = 1;
                    } else if (downCounter == 1) {
                        downCounter = 2;
                    } else if (downCounter == 2) {
                        downCounter = 3;
                    } else if (downCounter == 3) {
                        downCounter = 0;
                    }

                    System.out.println("DOWN");
                } else if (direction.equals("left")) {
                    if(leftCounter == 0) {
                        leftCounter = 1;
                    } else if (leftCounter == 1) {
                        leftCounter = 2;
                    } else if (leftCounter == 2) {
                        leftCounter = 3;
                    } else if (leftCounter == 3) {
                        leftCounter = 4;
                    } else if (leftCounter == 4) {
                        leftCounter = 5;
                    } else if (leftCounter == 5) {
                        leftCounter = 0;
                    }

                    System.out.println("LEFT");
                } else if (direction.equals("right")) {
                    if(rightCounter == 0) {
                        rightCounter = 1;
                    } else if (rightCounter == 1) {
                        rightCounter = 2;
                    } else if (rightCounter == 2) {
                        rightCounter = 3;
                    } else if (rightCounter == 3) {
                        rightCounter = 4;
                    } else if (rightCounter == 4) {
                        rightCounter = 5;
                    } else if (rightCounter == 5) {
                        rightCounter = 0;
                    }

                    System.out.println("RIGHT");
                } else if (direction.equalsIgnoreCase("attack")) {
                    System.out.println("ATTACK");
                }


                spriteCounter = 0;
            }
        } else {
            direction = "idle";

            if (!oldDirection.equals("idle")) {
                spriteCounter = idleAnimationSpeed; // Trigger idle animation immediately
            }

            spriteCounter++;
            if(spriteCounter > idleAnimationSpeed) {
                if(idleCounter == 0) {
                    idleCounter = 1;
                } else if (idleCounter == 1) {
                    idleCounter = 2;
                } else if (idleCounter == 2) {
                    idleCounter = 3;
                } else if (idleCounter == 3) {
                    idleCounter = 4;
                } else if (idleCounter == 4) {
                    idleCounter = 5;
                } else if (idleCounter == 5) {
                    idleCounter = 6;
                } else if (idleCounter == 6) {
                    idleCounter = 0;
                }

                spriteCounter = 0;

                System.out.println("IDLE");
            }
        }
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void draw(Graphics2D g2){
        // TODO: Draw player (box for now)

        BufferedImage image = null;

        switch (direction) {
            case "idle":
                image = idle[idleCounter];
                break;
            case "up":
                image = up[upCounter];
                break;
            case "up1":
                image = up[upCounter];
                break;
            case "attack":
                image = idle[idleCounter];
                break;
            case "down":
                image = down[downCounter];
                break;
            case "left":
                image = left[leftCounter];
                break;
            case "right":
                image = right[rightCounter];
                break;
        }

        g2.drawImage(image, gp.getCameraX(), gp.getCameraY(), Constants.tileSize, Constants.tileSize, null);

        //Initial character
        // g2.setColor(Color.WHITE);
        // g2.fillRect(worldX, worldY, Constants.tileSize, Constants.tileSize);
    }
}
