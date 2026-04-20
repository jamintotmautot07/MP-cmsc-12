
package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import engine.GamePanel;
import systems.CollisionManager;
import systems.KeyHandler;
import util.Constants;
import util.UtilityTool;

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

    // Back-references the player needs to read input and query the world/camera.
    GamePanel gp;
    KeyHandler keyH;

    // Fixed on-screen anchor point. The camera moves the world around the player most of the time.
    public final int screenX;
    public final int screenY;

    // Separate counters per animation set keep the frame order simple and independent.
    private int idleCounter = 0;
    private int downCounter = 0;
    private int upCounter = 0;
    private int leftCounter = 0;
    private int rightCounter = 0;

    // Helps detect direction/state changes so animations can restart cleanly.
    private String previousDirection = "idle";
    private int movementAnimationSpeed = 8;
    private int idleAnimationSpeed = 13; // Slower for idle

    // Prevents one long key hold from repeatedly retriggering an attack.
    private boolean attackedPressed = false;

    // Attack timing/state.
    private int attackCounter = 0;
    private final int attackDuration = 18;
    private boolean attackActive = false;
    private String attackDirection = "right"; // Store the direction of the current attack

    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;

        screenX = (Constants.screenWidth/2) - (Constants.tileSize/2);
        screenY = (Constants.screenHeight/2) - (Constants.tileSize/2);

        // Slightly smaller than the sprite so the player does not feel like they collide on transparent pixels.
        solidArea = new Rectangle(8, 16, 30, 32);

        setDefaultValues();
        getPlayerImages();
    }

    public void setDefaultValues() {
        // Spawn the player at the screen anchor. Because the camera starts there too,
        // this feels like spawning at the center of the view.
        worldX = screenX;
        worldY = screenY;
        speed = 3;
        direction = "idle";
    }

    public void getPlayerImages() {
        // Allocate frame arrays based on how many sprite images exist per state.
        idle = new BufferedImage[7];
        down = new BufferedImage[4];
        up = new BufferedImage[6];
        left = new BufferedImage[6];
        right = new BufferedImage[6];

        try {
            
            // Load and resize every animation frame once at startup.
            for(int i = 0; i < 7; i++) {
                idle[i] = ImageIO.read(new File(String.format("res/PlayerAssets/idle%d.png", i + 1)));
                idle[i] = UtilityTool.resizeImage(idle[i], Constants.tileSize, Constants.tileSize);
            }
            // down assets
            for(int i = 0; i < 4; i++) {
                down[i] = ImageIO.read(new File(String.format("res/PlayerAssets/down%d.png", i + 1)));
                down[i] = UtilityTool.resizeImage(down[i], Constants.tileSize, Constants.tileSize);
            }
            // up assets
            for(int i = 0; i < 6; i++) {
                up[i] = ImageIO.read(new File(String.format("res/PlayerAssets/up%d.png", i + 1)));
                up[i] = UtilityTool.resizeImage(up[i], Constants.tileSize, Constants.tileSize);
            }
            // left assets
            for(int i = 0; i < 6; i++) {
                left[i] = ImageIO.read(new File(String.format("res/PlayerAssets/left%d.png", i + 1)));
                left[i] = UtilityTool.resizeImage(left[i], Constants.tileSize, Constants.tileSize);
            }
            // right assets
            for(int i = 0; i < 6; i++) {
                right[i] = ImageIO.read(new File(String.format("res/PlayerAssets/right%d.png", i + 1)));
                right[i] = UtilityTool.resizeImage(right[i], Constants.tileSize, Constants.tileSize);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update(){
        // Every frame, cooldown timers tick down first.
        updateCooldowns();

        // Once the cooldown is done, a fresh attack input is allowed again.
        if (!isOnCooldown("Player_attack")) {
            attackedPressed = false;
        }

        // If the visible state changed, restart the animation counters so the new state begins from frame 0.
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

        /*
         * Attack handling comes before movement resolution.
         * That way the attack state is decided for this frame first,
         * and movement can still happen in the same update if needed.
         */
        AttackType requestedAttack = AttackType.NONE;
        String requestedAttackDirection = facingDirection;
        if (keyH.isActionPressed(KeyHandler.Action.ATTACK) && !attackedPressed && !isOnCooldown("Player_attack")) {
            if (keyH.isActionPressed(KeyHandler.Action.MOVE_UP)) {
                requestedAttack = AttackType.FORWARD;
                requestedAttackDirection = "up";
            } else if (keyH.isActionPressed(KeyHandler.Action.MOVE_DOWN)) {
                requestedAttack = AttackType.DOWN;
                requestedAttackDirection = "down";
            } else if (keyH.isActionPressed(KeyHandler.Action.MOVE_LEFT)) {
                requestedAttack = AttackType.SIDE;
                requestedAttackDirection = "left";
            } else if (keyH.isActionPressed(KeyHandler.Action.MOVE_RIGHT)) {
                requestedAttack = AttackType.SIDE;
                requestedAttackDirection = "right";
            } else {
                requestedAttack = AttackType.NORMAL;
                requestedAttackDirection = facingDirection;
            }

            attackedPressed = true;
            startCooldown("Player_attack", 50); // Attack cannot be retriggered immediately.
        }

        if (requestedAttack != AttackType.NONE) {
            // Reset the timer only if the attack pattern itself changed.
            if (attackType != requestedAttack) {
                attackType = requestedAttack;
                attackDirection = requestedAttackDirection;
                attackCounter = 0;
            }
            attackActive = true;
            attackCounter++;
            if (attackCounter > attackDuration) {
                // Attack window has ended, so clear combat state.
                attackCounter = 0;
                attackType = AttackType.NONE;
                attackActive = false;
            }
            attackHitbox = calculateAttackHitbox(attackDirection);
            direction = "idle"; // Current art set keeps the player visually idle while attacking.
        } else {
            // No attack requested this frame, so make sure hitbox/combat state is fully cleared.
            attackType = AttackType.NONE;
            attackActive = false;
            attackCounter = 0;
            attackHitbox.setBounds(0, 0, 0, 0);
        }

        // Movement input uses simple "first matching direction wins" priority.
        if(keyH.isActionPressed(KeyHandler.Action.MOVE_UP) || keyH.isActionPressed(KeyHandler.Action.MOVE_RIGHT) ||
            keyH.isActionPressed(KeyHandler.Action.MOVE_LEFT) || keyH.isActionPressed(KeyHandler.Action.MOVE_DOWN)
        ) {

            if(keyH.isActionPressed(KeyHandler.Action.MOVE_UP)) {
                direction = "up";
            } else if (keyH.isActionPressed(KeyHandler.Action.MOVE_LEFT)) {
                direction = "left";
            } else if (keyH.isActionPressed(KeyHandler.Action.MOVE_RIGHT)) {
                direction = "right";
            } else if (keyH.isActionPressed(KeyHandler.Action.MOVE_DOWN)) {
                direction = "down";
            }

            if (!oldDirection.equals(direction)) {
                spriteCounter = movementAnimationSpeed; // Makes the first frame swap happen without delay.
            }

            // Remember the last non-idle direction for normal attacks.
            facingDirection = direction;
            int nextX = worldX;
            int nextY = worldY;
            switch (direction) {
                case "up": nextY -= speed; break;
                case "right": nextX += speed; break;
                case "left": nextX -= speed; break;
                case "down": nextY += speed; break;
            }

            // Collision is checked against the next predicted position before movement is committed.
            Rectangle futureSolidArea = new Rectangle(nextX + solidArea.x, nextY + solidArea.y, solidArea.width, solidArea.height);
            if (!CollisionManager.willCollideWithSolidTile(gp.getTileManager(), futureSolidArea)) {
                worldX = nextX;
                worldY = nextY;
            }

            // Advance the correct directional animation at a fixed rate.
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
                }


                spriteCounter = 0;
            }
        } else {
            // No movement keys pressed, so fall back to the idle animation loop.
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
            }
        }
    }

    @Override
    protected Rectangle calculateAttackHitbox() {
        return calculateAttackHitbox(attackDirection);
    }

    public boolean isAttackActive() {
        return attackActive;
    }

    public Rectangle getAttackHitbox() {
        return attackHitbox;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void draw(Graphics2D g2){
        // Pick the current sprite frame based on the active state and animation counters.

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

        // The player is drawn using camera-relative screen coordinates, not raw world coordinates.
        g2.drawImage(image, gp.getCameraX(), gp.getCameraY(), null);

        // Debug/feedback overlay to show the active attack area.
        if (attackActive && attackHitbox.width > 0 && attackHitbox.height > 0) {
            int screenX = attackHitbox.x - gp.getCameraWorldX();
            int screenY = attackHitbox.y - gp.getCameraWorldY();
            g2.setColor(new Color(255, 0, 0, 120));
            g2.fillRect(screenX, screenY, attackHitbox.width, attackHitbox.height);
            g2.setColor(Color.RED);
            g2.drawRect(screenX, screenY, attackHitbox.width, attackHitbox.height);
        }

        //Initial character
        // g2.setColor(Color.WHITE);
        // g2.fillRect(worldX, worldY, Constants.tileSize, Constants.tileSize);
    }
}
