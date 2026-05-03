
package entity;

import java.awt.image.BufferedImage;
import util.Constants;
import util.UtilityTool;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;
import util.Cooldown;

/*
 OWNER: Jamin

 PURPOSE:
 - Base class for ALL objects

 TASKS:
 1. Add positions (x, y)

 NOTE:
 - All entities MUST extend this
*/

/**
 * Root gameplay object for anything that occupies world space and may animate, collide, or attack.
 * Player and enemy types inherit common positioning, rendering, and cooldown helpers from here.
 */
public class Entity {

    /*
     * AttackType lets the rest of the code describe different attack shapes
     * without hard-coding direction checks everywhere.
     */
    public enum AttackType {
        NONE,
        NORMAL,
        FORWARD,
        SIDE,
        DOWN
    }

    // Shared combat state that child classes such as Player and Enemy can reuse.
    protected final int attackRangeTiles = 1;
    protected String facingDirection = "down";
    protected AttackType attackType = AttackType.NONE;
    protected Rectangle attackHitbox = new Rectangle();
    
    // Position and movement values in world coordinates, not screen coordinates.
    public int worldX, worldY;
    public int speed;

    // Render size can differ from the base tile size for larger enemies.
    public int renderWidth = Constants.tileSize;
    public int renderHeight = Constants.tileSize;

    // Animation frames grouped by direction/state.
    public BufferedImage up[], right[], left[], down[], idle[];
    public String direction;

    // Generic sprite counters used by subclasses for animation timing.
    public int spriteCounter = 0;
    public int spriteNum = 1;

    // Rectangle used for physical collisions against solid tiles.
    public Rectangle solidArea;
    public boolean collisionOn = false;

    public int solidAreaDefaultX, solidAreaDefaultY;

    public boolean invincible = false;
    public int invincibleCounter = 0;

    // Stores per-action cooldown timers like attack delays.
    protected Map<String, Cooldown> cooldowns = new HashMap<>();

    /**
     * Get or create a cooldown for a specific action.
     */
    protected Cooldown getCooldown(String actionName) {
        return cooldowns.computeIfAbsent(actionName, k -> new Cooldown());
    }

    /**
     * Start a cooldown for a specific action.
     */
    protected void startCooldown(String actionName, int frames) {
        getCooldown(actionName).start(frames);
    }

    // Check if a cooldown is active for a specific action.
    protected boolean isOnCooldown(String actionName) {
        return getCooldown(actionName).isActive();
    }

    /**
     * Update all cooldowns. Calling this in the entity's update method.
     */
    protected void updateCooldowns() {
        for (Cooldown cooldown : cooldowns.values()) {
            cooldown.update();
        }
    }

    /**
     * Calculate attack hitbox using the entity's current facing direction.
     * This is a convenience method that uses the entity's facingDirection.
     */
    protected Rectangle calculateAttackHitbox() {
        return calculateAttackHitbox(facingDirection);
    }

    /**
     * Calculate attack hitbox based on attack type and specified direction.
     * This is the general method that all entities can use.
     */
    protected Rectangle calculateAttackHitbox(String attackDirection) {
        // Base attack shape starts from the entity's current tile footprint.
        int tileSize = Constants.tileSize;
        int range = attackType == AttackType.NORMAL ? 1 : attackRangeTiles;
        int hitSize = tileSize - 8;
        int bodyWidth = renderWidth > 0 ? renderWidth : tileSize;
        int bodyHeight = renderHeight > 0 ? renderHeight : tileSize;
        int x = worldX;
        int y = worldY;
        int width = hitSize;
        int height = hitSize;

        // Some attack types override the requested direction to force a fixed shape.
        String directionForAttack = attackDirection;

        if (attackType == AttackType.FORWARD) {
            directionForAttack = "up";
        } else if (attackType == AttackType.DOWN) {
            directionForAttack = "down";
        } else if (attackType == AttackType.SIDE) {
            // For SIDE attacks, use the provided direction (facing direction)
            directionForAttack = attackDirection;
        }

        switch (directionForAttack) {
            case "left":
                x = worldX - (hitSize * range);
                y = worldY + (bodyHeight - hitSize) / 2;
                width = hitSize;
                height = hitSize;
                break;
            case "right":
                x = worldX + bodyWidth;
                y = worldY + (bodyHeight - hitSize) / 2;
                width = hitSize;
                height = hitSize;
                break;
            case "up":
                x = worldX + (bodyWidth - hitSize) / 2;
                y = worldY - (hitSize * range);
                width = hitSize;
                height = hitSize;
                break;
            case "down":
                x = worldX + (bodyWidth - hitSize) / 2;
                y = worldY + bodyHeight;
                width = hitSize;
                height = hitSize;
                break;
            default:
                x = worldX + bodyWidth;
                y = worldY + (bodyHeight - hitSize) / 2;
                width = hitSize;
                height = hitSize;
                break;
        }

        attackHitbox.setBounds(x, y, width, height);
        return attackHitbox;
    }

    /**
     * Resize a sprite array to a fixed render size.
     */
    protected BufferedImage[] resizeSpriteArray(BufferedImage[] frames, int width, int height) {
        if (frames == null || width <= 0 || height <= 0) {
            return frames;
        }

        BufferedImage[] result = new BufferedImage[frames.length];
        for (int i = 0; i < frames.length; i++) {
            if (frames[i] != null) {
                result[i] = UtilityTool.resizeImage(frames[i], width, height);
            }
        }
        return result;
    }
}
