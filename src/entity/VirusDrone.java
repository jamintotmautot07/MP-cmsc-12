package entity;

import java.awt.image.BufferedImage;

import engine.GamePanel;
import util.Constants;

/**
 * Flying enemy that uses the base enemy's shared chase/path behavior and ranged attacks.
 */
public class VirusDrone extends Enemy {

    private static final int AGGRO_START_TILES = 7;
    private static final int AGGRO_STOP_TILES = 12;
    private static final int NO_FIRE_RANGE_TILES = 4;
    private static final int MELEE_COOLDOWN_FRAMES = 60 * 2;
    private static final int FIRE_COOLDOWN_FRAMES = 60 * 2;
    private static final int PROJECTILE_RANGE_TILES = 7;
    private static final int PROJECTILE_SPEED = 3;
    private static final int PROJECTILE_SIZE = Constants.tileSize / 3;

    /**
     * Creates a ranged enemy that can chase, slash, and fire projectiles.
     */
    public VirusDrone(GamePanel gp) {
        super(gp);
        setDefaultValues();
        loadSprites();
    }

    @Override
    /**
     * Sets the virus drone's health, speed, and contact damage.
     */
    public void setDefaultValues() {
        super.setDefaultValues();
        speed = 1; // Faster than basic enemies
        hp = 3;
        maxHp = 3;
        damage = 1;
    }

    @Override
    /**
     * Loads the virus drone's directional animation frames.
     */
    protected void loadSprites() {

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
    /**
     * Chooses between melee, ranged fire, chase pathfinding, and idle hovering.
     */
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

        updatePathState(AGGRO_START_TILES, AGGRO_STOP_TILES);

        if (onPath) {
            if (tileDistance > NO_FIRE_RANGE_TILES && !isOnCooldown("Virus_fire")) {
                fireProjectileAtPlayer(1, PROJECTILE_SPEED, PROJECTILE_RANGE_TILES, PROJECTILE_SIZE);
                startCooldown("Virus_fire", FIRE_COOLDOWN_FRAMES);
            }

            searchPath(
                gp.getPlayer().worldX / Constants.tileSize,
                gp.getPlayer().worldY / Constants.tileSize
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
    /**
     * Advances animation a little faster to fit the drone's mechanical feel.
     */
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
    /**
     * Chooses idle frames while hovering and directional frames while moving.
     */
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
    /**
     * Briefly breaks chase state when damaged so hits create visible feedback.
     */
    public void damageReaction() {
        actionLockCounter = 0;
        // On damage, briefly stop chasing to react
        onPath = false;
    }
}
