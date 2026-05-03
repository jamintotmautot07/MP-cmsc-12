package entity;

import java.awt.image.BufferedImage;

import engine.GamePanel;

/**
 * Worm enemy - organic, slime-like enemy with simple movement.
 * Subclass of Enemy, uses random wandering behavior.
 */
/**
 * Slow wandering enemy used as a basic ground threat.
 */
public class Worm extends Enemy {

    public Worm(GamePanel gp) {
        super(gp);
        setDefaultValues();
        loadSprites();
    }

    @Override
    public void setDefaultValues() {
        super.setDefaultValues();
        speed = 1; // Slow, organic movement
        hp = 2;    // Weaker health
        damage = 1;
    }

    @Override
    protected void loadSprites() {

        // Load sprite arrays with appropriate frame counts
        idleFrames = loadCachedSpriteArray("worm", "idle", 6);
        upFrames = loadCachedSpriteArray("worm", "up", 6);
        downFrames = loadCachedSpriteArray("worm", "down", 6);
        leftFrames = loadCachedSpriteArray("worm", "left", 10);
        rightFrames = loadCachedSpriteArray("worm", "right", 10);
        damagedFrames = loadCachedSpriteArray("worm", "damaged", 10);

        // Fallback: if no sprites loaded, use parent's fallback
        if (idleFrames == null && upFrames == null && downFrames == null &&
            leftFrames == null && rightFrames == null) {
            super.loadSprites();
        }
    }

    @Override
    public void setAction() {
        actionLockCounter++;

        // More frequent direction changes for organic feel
        if (actionLockCounter >= 90) {
            int i = random.nextInt(100) + 1;

            if (i <= 30) {
                direction = "up";
            } else if (i <= 60) {
                direction = "down";
            } else if (i <= 80) {
                direction = "left";
            } else {
                direction = "right";
            }

            actionLockCounter = 0;
        }
    }

    @Override
    protected void updateAnimation() {
        // Slightly slower animation for organic feel
        spriteCounter++;
        if (spriteCounter > 15) {
            spriteNum++;
            spriteCounter = 0;

            BufferedImage[] currentFrames = getCurrentFrameArray();
            if (currentFrames != null && spriteNum >= currentFrames.length) {
                spriteNum = 0;
            }
        }
    }

    @Override
    public void damageReaction() {
        // Worms react by moving away more erratically
        actionLockCounter = 0;

        // Random direction change on damage
        int i = random.nextInt(4);
        switch (i) {
            case 0: direction = "up"; break;
            case 1: direction = "down"; break;
            case 2: direction = "left"; break;
            case 3: direction = "right"; break;
        }
    }
}
