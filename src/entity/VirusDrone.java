package entity;

import java.awt.image.BufferedImage;

import engine.GamePanel;
import util.Constants;

/**
 * VirusDrone enemy - floating surveillance drone with pathfinding chase behavior.
 * Subclass of EnemyPath, actively pursues the player when in range.
 */
/**
 * Flying chase enemy that extends the path-oriented enemy base.
 */
public class VirusDrone extends EnemyPath {

    public VirusDrone(GamePanel gp) {
        super(gp);
        setDefaultValues();
        loadSprites();
    }

    @Override
    public void setDefaultValues() {
        super.setDefaultValues();
        speed = 2; // Faster than basic enemies
        hp = 3;
        damage = 1;
    }

    @Override
    protected void loadSprites() {
        String basePath = "res/EnemyAssets/virus/";

        // Load sprite arrays with appropriate frame counts
        idleFrames = loadCachedSpriteArray("virus", "idle", 13);
        upFrames = loadCachedSpriteArray("virus", "up", 11);
        downFrames = loadCachedSpriteArray("virus", "down", 13);
        leftFrames = loadCachedSpriteArray("virus", "left", 11);
        rightFrames = loadCachedSpriteArray("virus", "right", 11);

        damagedFrames = idleFrames;

        // Fallback: if no sprites loaded, use parent's fallback
        if (idleFrames == null && upFrames == null && downFrames == null &&
            leftFrames == null && rightFrames == null) {
            super.loadSprites();
        }
    }

    @Override
    public void setAction() {
        int distanceX = Math.abs(worldX - gp.player.worldX);
        int distanceY = Math.abs(worldY - gp.player.worldY);

        int tileDistance = (distanceX + distanceY) / Constants.tileSize;

        // Start chasing at longer range than default
        if (tileDistance < 8) {
            onPath = true;
        } else if (tileDistance > 12) {
            onPath = false;
        }

        if (onPath) {
            searchPath(
                gp.player.worldX / Constants.tileSize,
                gp.player.worldY / Constants.tileSize
            );
        } else {
            // When not chasing, hover in place or move slowly
            actionLockCounter++;

            if (actionLockCounter >= 180) { // Less frequent movement when idle
                int i = random.nextInt(100) + 1;

                if (i <= 20) {
                    direction = "up";
                } else if (i <= 40) {
                    direction = "down";
                } else if (i <= 60) {
                    direction = "left";
                } else if (i <= 80) {
                    direction = "right";
                } else {
                    direction = "idle"; // Sometimes just hover
                }

                actionLockCounter = 0;
            }
        }
    }

    @Override
    protected void updateAnimation() {
        // Faster animation for mechanical feel
        spriteCounter++;
        if (spriteCounter > 10) {
            spriteNum++;
            spriteCounter = 0;

            BufferedImage[] currentFrames = getCurrentFrameArray();
            if (currentFrames != null && spriteNum >= currentFrames.length) {
                spriteNum = 0;
            }
        }
    }

    @Override
    protected BufferedImage[] getCurrentFrameArray() {
        if (invincible && damagedFrames != null && damagedFrames.length > 0) {
            return damagedFrames;
        }

        if (direction.equals("idle") || (!onPath && actionLockCounter > 100)) {
            return idleFrames; // Use idle animation when hovering
        }

        switch (direction) {
            case "up": return upFrames;
            case "down": return downFrames;
            case "left": return leftFrames;
            case "right": return rightFrames;
            default: return idleFrames;
        }
    }

    @Override
    public void damageReaction() {
        actionLockCounter = 0;
        // On damage, briefly stop chasing to react
        onPath = false;
    }
}
