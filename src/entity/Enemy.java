
package entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import engine.GamePanel;
import util.UtilityTool;
import util.Constants;
import systems.CollisionManager;
import util.ResourceCache;

/*
 OWNER: Allan

 PURPOSE:
 - Base enemy behavior

 TASKS:
 1. Make enemy follow player
 2. Add speed
 3. Add simple AI
 4. Add attack system using general Entity methods

 OPTIONAL:
 - Different enemy types
*/

/**
 * Base implementation for simple enemies that wander, animate, render, and react to damage.
 * Subclasses mainly customize sprite loading, default stats, and decision-making in `setAction()`.
 */
public class Enemy extends Entity {

    protected GamePanel gp;
    protected Random random = new Random();

    protected int actionLockCounter = 0;
    protected boolean alive = true;
    protected boolean dying = false;
    protected int hp = 3;
    protected int damage = 1;

    // Sprite arrays for different states
    protected BufferedImage[] idleFrames;
    protected BufferedImage[] upFrames;
    protected BufferedImage[] downFrames;
    protected BufferedImage[] leftFrames;
    protected BufferedImage[] rightFrames;
    protected BufferedImage[] damagedFrames;

    /**
     * Initializes shared enemy collision bounds and default state.
     */
    public Enemy(GamePanel gp) {
        this.gp = gp;

        solidArea = new Rectangle();
        solidArea.x = 8;
        solidArea.y = 16;
        solidArea.width = 32;
        solidArea.height = 24;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;

        setDefaultValues();
        loadSprites();
    }

    /**
     * Resets the enemy to its baseline combat and movement values.
     */
    public void setDefaultValues() {
        speed = 1;
        direction = "down";
        hp = 3;
        damage = 1;
        alive = true;
        dying = false;
    }

    /**
     * Load enemy sprites. Subclasses should override this to load their specific assets.
     * This base implementation provides fallback behavior.
     */
    protected void loadSprites() {
        // Default implementation - subclasses should override
        // For now, create placeholder arrays
        idleFrames = new BufferedImage[1];
        upFrames = new BufferedImage[1];
        downFrames = new BufferedImage[1];
        leftFrames = new BufferedImage[1];
        rightFrames = new BufferedImage[1];
        damagedFrames = new BufferedImage[1];

        // Try to load a default sprite if available
        try {
            idleFrames[0] = ResourceCache.getImage("enemy_default");
            upFrames[0] = idleFrames[0];
            downFrames[0] = idleFrames[0];
            leftFrames[0] = idleFrames[0];
            rightFrames[0] = idleFrames[0];
            damagedFrames[0] = idleFrames[0];
        } catch (Exception e) {
            // No default sprite available
        }
    }

    /**
     * Safely load a sprite array from the given path pattern.
     */
    protected BufferedImage[] loadSpriteArray(String basePath, String state, int maxFrames) {
        BufferedImage[] frames = new BufferedImage[maxFrames];
        int loaded = 0;

        for (int i = 1; i <= maxFrames; i++) {
            try {
                String path = basePath + state + i + ".png";
                BufferedImage img = ImageIO.read(new File(path));
                if (img != null) {
                    frames[loaded] = img;
                    loaded++;
                } else {
                    break; // Stop if a frame is missing
                }
            } catch (Exception e) {
                break; // Stop on any error
            }
        }

        if (loaded == 0) {
            return null; // No frames loaded
        }

        // Trim array to actual loaded frames
        BufferedImage[] result = new BufferedImage[loaded];
        System.arraycopy(frames, 0, result, 0, loaded);
        return result;
    }

    protected BufferedImage[] loadCachedSpriteArray(String enemyKey, String state, int frameCount) {
        BufferedImage[] frames = new BufferedImage[frameCount];

        for (int i = 0; i < frameCount; i++) {
            frames[i] = ResourceCache.getImage("enemy_" + enemyKey + "_" + state + "_" + i);
        }

        return frames;
    }


    /**
     * Default idle AI: randomly pick a direction every few seconds.
     * Pathfinding enemies override this with chase logic.
     */
    public void setAction() {
        actionLockCounter++;

        if (actionLockCounter >= 120) {
            int i = random.nextInt(100) + 1;

            if (i <= 25) {
                direction = "up";
            } else if (i <= 50) {
                direction = "down";
            } else if (i <= 75) {
                direction = "left";
            } else {
                direction = "right";
            }

            actionLockCounter = 0;
        }
    }

    /**
     * Runs one frame of enemy logic: choose an action, test collisions, move, animate, and update timers.
     */
    public void update() {
        if (!alive) return;

        setAction();

        collisionOn = false;
        // Check tile collision
        Rectangle futureSolidArea = new Rectangle(
            solidArea.x + worldX,
            solidArea.y + worldY,
            solidArea.width,
            solidArea.height
        );
        // NOTE: `futureSolidArea` is currently built from the current world position only.
        // Because the next movement step is not applied to this rectangle before collision testing,
        // tile collision can miss or feel late by one frame.
        // If this is revisited, compute the rectangle from the predicted next X/Y for the current direction.
        if (CollisionManager.willCollideWithSolidTile(gp.tileM, futureSolidArea)) {
            collisionOn = true;
        }

        // Check player collision
        // NOTE: This compares local solid-area rectangles instead of world-space hitboxes.
        // That means enemy-vs-player contact is not being tested in the same coordinate system,
        // which is one likely reason enemy attacks / damage registration still do not work.
        // The place to fix that later is here or in CollisionManager by converting both hitboxes to world coordinates first.
        if (CollisionManager.rectanglesIntersect(gp.player.solidArea, solidArea)) {
            // Handle player collision (damage player, etc.)
            // TODO: Implement player damage
        }

        if (!collisionOn) {
            switch (direction) {
                case "up":    worldY -= speed; break;
                case "down":  worldY += speed; break;
                case "left":  worldX -= speed; break;
                case "right": worldX += speed; break;
            }
        }

        // Update animation
        updateAnimation();

        if (invincible) {
            invincibleCounter++;
            if (invincibleCounter > 40) {
                invincible = false;
                invincibleCounter = 0;
            }
        }

        updateCooldowns();
    }

    /**
     * Update animation counters. Subclasses can override for custom animation logic.
     */
    protected void updateAnimation() {
        spriteCounter++;
        if (spriteCounter > 12) {
            spriteNum++;
            spriteCounter = 0;

            // Reset to first frame if we've gone through all frames
            BufferedImage[] currentFrames = getCurrentFrameArray();
            if (currentFrames != null && spriteNum >= currentFrames.length) {
                spriteNum = 0;
            }
        }
    }

    /**
     * Get the current frame array based on state and direction.
     */
    protected BufferedImage[] getCurrentFrameArray() {
        if (invincible && damagedFrames != null && damagedFrames.length > 0) {
            return damagedFrames;
        }

        switch (direction) {
            case "up": return upFrames;
            case "down": return downFrames;
            case "left": return leftFrames;
            case "right": return rightFrames;
            default: return idleFrames;
        }
    }

    /**
     * Render the enemy. Should be called from GamePanel's render method.
     */
    public void render(Graphics2D g2) {
        if (!alive) return;

        BufferedImage[] currentFrames = getCurrentFrameArray();
        BufferedImage image = null;

        if (currentFrames != null && currentFrames.length > 0) {
            image = currentFrames[Math.min(spriteNum, currentFrames.length - 1)];
        }
        
        int screenX = worldX - gp.getCameraWorldX();
        int screenY = worldY - gp.getCameraWorldY();

        if (image == null) {
            // Fallback: draw a colored rectangle
            g2.setColor(Color.RED);
            g2.fillRect(screenX, screenY, renderWidth, renderHeight);
            return;
        }

        // Draw the sprite.
        // BUG NOTE: enemy rendering is still tied to player-relative conversion here:
        // `world - player.world + player.screen`.
        // That usually matches the camera while the player is centered, but it can drift or "warp"
        // near map edges where GamePanel deliberately stops centering the player.
        // If the visual warping near the end of the map gets fixed later, start here and switch to the
        // same camera transform used by tiles and the player attack box:
        // `screen = world - gp.getCameraWorldX/Y()`.

        if (invincible) {
            g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.5f));
        }

        g2.drawImage(image, screenX, screenY, renderWidth, renderHeight, null);

        if (invincible) {
            g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1f));
        }
    }

    /**
     * Default knockback reaction: turn away from the player's location.
     */
    public void damageReaction() {
        actionLockCounter = 0;

        // simple reaction: move away or change direction
        if (gp.player.worldX < worldX) {
            direction = "right";
        } else if (gp.player.worldX > worldX) {
            direction = "left";
        }

        if (gp.player.worldY < worldY) {
            direction = "down";
        } else if (gp.player.worldY > worldY) {
            direction = "up";
        }
    }

    /**
     * Applies incoming damage once per invincibility window.
     */
    public void takeDamage(int amount) {
        if (!invincible) {
            hp -= amount;
            invincible = true;
            damageReaction();
            System.out.println(getClass().getSimpleName() + " damaged by " + amount + ". HP left: " + Math.max(hp, 0));

            if (hp <= 0) {
                dying = true;
                alive = false;
                System.out.println(getClass().getSimpleName() + " destroyed.");
            }
        }
    }

    /**
     * Small helper used by update/render loops to skip dead enemies.
     */
    public boolean isAlive() {
        return alive;
    }
}
