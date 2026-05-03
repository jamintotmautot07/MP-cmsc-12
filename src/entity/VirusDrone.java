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

    private static final int AGGRO_START_TILES = 7;
    private static final int AGGRO_STOP_TILES = 12;
    private static final int NO_FIRE_RANGE_TILES = 5;
    private static final int MELEE_COOLDOWN_FRAMES = 60 * 2;
    private static final int FIRE_COOLDOWN_FRAMES = 60 * 2;
    private static final int PROJECTILE_RANGE_TILES = 7;
    private static final int PROJECTILE_SPEED = 2;
    private static final int PROJECTILE_SIZE = Constants.tileSize / 3;

    public VirusDrone(GamePanel gp) {
        super(gp);
        setDefaultValues();
        loadSprites();
    }

    @Override
    public void setDefaultValues() {
        super.setDefaultValues();
        speed = 1; // Faster than basic enemies
        hp = 3;
        maxHp = 3;
        damage = 1;
    }

    @Override
    protected void loadSprites() {
        String basePath = "res/EnemyAssets/virus/";

        // Load sprite arrays with appropriate frame counts
        idleFrames = loadSpriteArray(basePath, "idle", 13);
        upFrames = loadSpriteArray(basePath, "up", 11);
        downFrames = loadSpriteArray(basePath, "down", 13);
        leftFrames = loadSpriteArray(basePath, "left", 11);
        rightFrames = loadSpriteArray(basePath, "right", 11);
        // No damaged frames for virus, use idle as fallback

        // Fallback: if no sprites loaded, use parent's fallback
        if (idleFrames == null && upFrames == null && downFrames == null &&
            leftFrames == null && rightFrames == null) {
            super.loadSprites();
        }
    }

    @Override
    public void setAction() {
        int tileDistance = getTileDistanceToPlayer();
        String attackDirection = getCardinalDirectionTowardPlayer();

        if (!isOnCooldown("Virus_slash") && canHitPlayerWithMelee(attackDirection)) {
            direction = attackDirection;
            onPath = true;
            startEnemyAttack(AttackType.NORMAL, attackDirection);
            startCooldown("Virus_slash", MELEE_COOLDOWN_FRAMES);
            actionLockCounter = 0;
            return;
        }

        // Start chasing at longer range than default
        if (tileDistance < AGGRO_START_TILES) {
            onPath = true;
        } else if (tileDistance > AGGRO_STOP_TILES) {
            onPath = false;
        }

        if (onPath) {
            if (tileDistance > NO_FIRE_RANGE_TILES && !isOnCooldown("Virus_fire")) {
                fireProjectileAtPlayer(1, PROJECTILE_SPEED, PROJECTILE_RANGE_TILES, PROJECTILE_SIZE);
                startCooldown("Virus_fire", FIRE_COOLDOWN_FRAMES);
            }

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
