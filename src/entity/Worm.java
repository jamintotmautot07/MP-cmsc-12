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

    private static final int AGGRO_RANGE_TILES = 3;
    private static final int BITE_COOLDOWN_FRAMES = 60 * 3;

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
        maxHp = 2;
        damage = 1;
    }

    @Override
    protected void loadSprites() {
        String basePath = "res/EnemyAssets/worm/";

        // Load sprite arrays with appropriate frame counts
        idleFrames = loadSpriteArray(basePath, "idle", 6);
        upFrames = loadSpriteArray(basePath, "up", 6);
        downFrames = loadSpriteArray(basePath, "down", 6);
        leftFrames = loadSpriteArray(basePath, "left", 10);
        rightFrames = loadSpriteArray(basePath, "right", 10);
        damagedFrames = loadSpriteArray(basePath, "damaged", 10);

        // Fallback: if no sprites loaded, use parent's fallback
        if (idleFrames == null && upFrames == null && downFrames == null &&
            leftFrames == null && rightFrames == null) {
            super.loadSprites();
        }
    }

    @Override
    public void setAction() {
        String attackDirection = getCardinalDirectionTowardPlayer();
        if (!isOnCooldown("Worm_bite") && canHitPlayerWithMelee(attackDirection)) {
            direction = attackDirection;
            startEnemyAttack(AttackType.NORMAL, attackDirection);
            startCooldown("Worm_bite", BITE_COOLDOWN_FRAMES);
            actionLockCounter = 0;
            return;
        }

        if (getTileDistanceToPlayer() <= AGGRO_RANGE_TILES) {
            direction = attackDirection;
            actionLockCounter = 0;
            return;
        }

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
