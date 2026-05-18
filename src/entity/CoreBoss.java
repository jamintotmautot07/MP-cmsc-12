package entity;

import engine.GamePanel;
import java.awt.Rectangle;
import systems.CollisionManager;
import util.Constants;

/**
 * Core boss with a large body, circular projectile bursts, periodic lasers, and an aggro phase.
 */
public class CoreBoss extends Enemy {

    private static final int NORMAL_ATTACK_COOLDOWN_FRAMES = 105;
    private static final int AGGRO_ATTACK_COOLDOWN_FRAMES = 70;
    private static final int LASER_COOLDOWN_FRAMES = 120;
    private static final int AGGRO_LASER_COOLDOWN_FRAMES = 85;
    private static final int CIRCLE_PROJECTILE_RANGE_TILES = 8;
    private static final int CIRCLE_PROJECTILE_SPEED = 5;
    private static final int AGGRO_CIRCLE_PROJECTILE_SPEED = 7;
    private static final int CIRCLE_PROJECTILE_SIZE = Constants.tileSize / 3;
    private static final int LASER_THICKNESS = Constants.tileSize * 3;
    private static final int LASER_DURATION_FRAMES = 60;

    private boolean homeInitialized = false;
    private int homeX;
    private int homeY;
    private boolean aggro = false;
    private int circularAttacksSinceLaser = 0;
    private int nextLaserThreshold = 4;
    private int aggroMoveCounter = 0;
    private boolean canBeDamaged = false;
    private int lifeRestoreCounter = 0;

    /**
     * Creates the final boss and loads its large idle sprite set.
     */
    public CoreBoss(GamePanel gp) {
        super(gp);
        setDefaultValues();
        loadSprites();
    }

    @Override
    /**
     * Configures the boss as a large, mostly stationary enemy with high health and damage.
     */
    public void setDefaultValues() {
        speed = 0;
        hp = 20;
        maxHp = 20;
        damage = 2;
        renderWidth = Constants.tileSize * 6;
        renderHeight = Constants.tileSize * 6;
        direction = "down";
        alive = true;
        dying = false;

        int padding = Constants.tileSize * 2;
        solidArea = new Rectangle(
            padding,
            padding,
            padding,
            padding
        );
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }

    @Override
    /**
     * Loads the boss idle animation and reuses it for all movement-facing states.
     */
    protected void loadSprites() {

        idleFrames = loadCachedSpriteArray("boss", "idle", 6);

        if (idleFrames != null) {
            idleFrames = resizeSpriteArray(idleFrames, renderWidth, renderHeight);
        } else {
            super.loadSprites();
        }

        upFrames = idleFrames;
        downFrames = idleFrames;
        leftFrames = idleFrames;
        rightFrames = idleFrames;
        damagedFrames = idleFrames;
    }

    @Override
    /**
     * Randomly changes facing direction so the boss does not look completely static.
     */
    public void setAction() {
        actionLockCounter++;
        if (actionLockCounter < 120) {
            return;
        }

        actionLockCounter = 0;
        int i = random.nextInt(4);
        switch (i) {
            case 0: direction = "up"; break;
            case 1: direction = "down"; break;
            case 2: direction = "left"; break;
            case 3: direction = "right"; break;
            default: direction = "down"; break;
        }
    }

    @Override
    /**
     * Runs one boss AI frame, including aggro movement, attacks, animation, and cooldowns.
     */
    public void update() {
        if (!alive) return;

        initializeHomePosition();

        if (hp <= 10) {
            aggro = true;
        }

        setAction();
        updateAggroMovement();
        updateBossAttack();
        updateAnimation();
        updateLife();

        if (invincible) {
            invincibleCounter++;
            if (invincibleCounter > 40) {
                invincible = false;
                invincibleCounter = 0;
            }
        }

        updateCooldowns();
    }

    public void addLife(int life) {
        this.hp += life;
    }

    public void updateLife() {
        lifeRestoreCounter++; 

        if(lifeRestoreCounter < 600) {
            return;
        }

        lifeRestoreCounter = 0;
        if(this.hp == maxHp) {
            return;
        } else {
            addLife(1);
        }
    }

    public void canBeDamaged() {
        this.canBeDamaged = true;
    }

    public boolean getCanBeDamaged() {
        return this.canBeDamaged;
    }

    /**
     * Stores the original boss position so aggro movement can stay near its arena center.
     */
    private void initializeHomePosition() {
        if (homeInitialized) {
            return;
        }

        homeX = worldX;
        homeY = worldY;
        homeInitialized = true;
    }

    /**
     * In aggro phase, nudges the boss toward the player while keeping it near its home position.
     */
    private void updateAggroMovement() {
        if (!aggro) {
            return;
        }

        aggroMoveCounter++;
        if (aggroMoveCounter < 12) {
            return;
        }
        aggroMoveCounter = 0;

        int step = 2;
        int maxOffset = Constants.tileSize / 2;
        int nextX = worldX + Integer.compare(getPlayerCenterX(), getCenterX()) * step;
        int nextY = worldY + Integer.compare(getPlayerCenterY(), getCenterY()) * step;

        nextX = clamp(nextX, homeX - maxOffset, homeX + maxOffset);
        nextY = clamp(nextY, homeY - maxOffset, homeY + maxOffset);

        if (canMoveTo(nextX, nextY)) {
            worldX = nextX;
            worldY = nextY;
        }
    }

    /**
     * Chooses between circular projectile bursts and a laser attack based on cooldown/threshold state.
     */
    private void updateBossAttack() {
        if (isOnCooldown("CoreBoss_attack")) {
            return;
        }

        if (circularAttacksSinceLaser >= nextLaserThreshold) {
            fireLaserAtPlayer();
            circularAttacksSinceLaser = 0;
            nextLaserThreshold = 4 + random.nextInt(2);
            startCooldown("CoreBoss_attack", aggro ? AGGRO_LASER_COOLDOWN_FRAMES : LASER_COOLDOWN_FRAMES);
            return;
        }

        fireCircularProjectiles();
        circularAttacksSinceLaser++;
        startCooldown("CoreBoss_attack", aggro ? AGGRO_ATTACK_COOLDOWN_FRAMES : NORMAL_ATTACK_COOLDOWN_FRAMES);
    }

    /**
     * Fires projectiles in all eight directions around the boss.
     */
    private void fireCircularProjectiles() {
        String[] directions = {
            "up", "down", "left", "right",
            "up-left", "up-right", "down-left", "down-right"
        };

        int projectileSpeed = aggro ? AGGRO_CIRCLE_PROJECTILE_SPEED : CIRCLE_PROJECTILE_SPEED;
        for (String projectileDirection : directions) {
            spawnCircularProjectile(projectileDirection, projectileSpeed);
        }
    }

    /**
     * Spawns one projectile at the edge or corner of the boss body for a radial burst.
     */
    private void spawnCircularProjectile(String projectileDirection, int projectileSpeed) {
        int size = CIRCLE_PROJECTILE_SIZE;
        int startX = getCenterX() - size / 2;
        int startY = getCenterY() - size / 2;

        switch (projectileDirection) {
            case "up":
                startY = worldY - size;
                break;
            case "down":
                startY = worldY + renderHeight;
                break;
            case "left":
                startX = worldX - size;
                break;
            case "right":
                startX = worldX + renderWidth;
                break;
            case "up-left":
                startX = worldX - size;
                startY = worldY - size;
                break;
            case "up-right":
                startX = worldX + renderWidth;
                startY = worldY - size;
                break;
            case "down-left":
                startX = worldX - size;
                startY = worldY + renderHeight;
                break;
            case "down-right":
                startX = worldX + renderWidth;
                startY = worldY + renderHeight;
                break;
            default:
                break;
        }

        gp.spawnProjectile(new Projectile(
            gp,
            Projectile.OwnerType.ENEMY,
            projectileDirection,
            startX,
            startY,
            damage,
            projectileSpeed,
            CIRCLE_PROJECTILE_RANGE_TILES * Constants.tileSize,
            size,
            size
        ));
    }

    /**
     * Builds a horizontal or vertical laser based on the player's dominant relative direction.
     */
    private void fireLaserAtPlayer() {
        int dx = getPlayerCenterX() - getCenterX();
        int dy = getPlayerCenterY() - getCenterY();
        Rectangle laserBounds;

        if (Math.abs(dx) >= Math.abs(dy)) {
            laserBounds = buildHorizontalLaser(dx >= 0);
        } else {
            laserBounds = buildVerticalLaser(dy >= 0);
        }

        if (laserBounds.width > 0 && laserBounds.height > 0) {
            gp.spawnLaser(new Laser(Laser.OwnerType.ENEMY, laserBounds, damage + 1, LASER_DURATION_FRAMES));
        }
    }

    /**
     * Builds a horizontal laser rectangle until it hits a solid tile or reaches screen-range length.
     */
    private Rectangle buildHorizontalLaser(boolean toRight) {
        int y = clamp(getCenterY() - LASER_THICKNESS / 2, 0, Constants.maxWorldHeight - LASER_THICKNESS);

        if (toRight) {
            int startX = worldX + renderWidth;
            int maxX = Math.min(Constants.maxWorldWidth, startX + Constants.screenWidth);
            int endX = startX;

            while (endX < maxX) {
                int segmentWidth = Math.min(Constants.tileSize, maxX - endX);
                Rectangle segment = new Rectangle(endX, y, segmentWidth, LASER_THICKNESS);
                if (CollisionManager.willCollideWithSolidTile(gp.getTileManager(), segment)) {
                    break;
                }
                endX += segmentWidth;
            }

            return new Rectangle(startX, y, endX - startX, LASER_THICKNESS);
        }

        int bossLeft = worldX;
        int minX = Math.max(0, bossLeft - Constants.screenWidth);
        int currentX = bossLeft;

        while (currentX > minX) {
            int segmentWidth = Math.min(Constants.tileSize, currentX - minX);
            int segmentX = currentX - segmentWidth;
            Rectangle segment = new Rectangle(segmentX, y, segmentWidth, LASER_THICKNESS);
            if (CollisionManager.willCollideWithSolidTile(gp.getTileManager(), segment)) {
                break;
            }
            currentX = segmentX;
        }

        return new Rectangle(currentX, y, bossLeft - currentX, LASER_THICKNESS);
    }

    /**
     * Builds a vertical laser rectangle until it hits a solid tile or reaches screen-range length.
     */
    private Rectangle buildVerticalLaser(boolean downward) {
        int x = clamp(getCenterX() - LASER_THICKNESS / 2, 0, Constants.maxWorldWidth - LASER_THICKNESS);

        if (downward) {
            int startY = worldY + renderHeight;
            int maxY = Math.min(Constants.maxWorldHeight, startY + Constants.screenHeight);
            int endY = startY;

            while (endY < maxY) {
                int segmentHeight = Math.min(Constants.tileSize, maxY - endY);
                Rectangle segment = new Rectangle(x, endY, LASER_THICKNESS, segmentHeight);
                if (CollisionManager.willCollideWithSolidTile(gp.getTileManager(), segment)) {
                    break;
                }
                endY += segmentHeight;
            }

            return new Rectangle(x, startY, LASER_THICKNESS, endY - startY);
        }

        int bossTop = worldY;
        int minY = Math.max(0, bossTop - Constants.screenHeight);
        int currentY = bossTop;

        while (currentY > minY) {
            int segmentHeight = Math.min(Constants.tileSize, currentY - minY);
            int segmentY = currentY - segmentHeight;
            Rectangle segment = new Rectangle(x, segmentY, LASER_THICKNESS, segmentHeight);
            if (CollisionManager.willCollideWithSolidTile(gp.getTileManager(), segment)) {
                break;
            }
            currentY = segmentY;
        }

        return new Rectangle(x, currentY, LASER_THICKNESS, bossTop - currentY);
    }

    /**
     * Keeps a value inside a min/max range.
     */
    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    protected String getHitSoundKey() {
        return "boss hit fx";
    }

    @Override
    /**
     * Damages the boss and punishes risky contact/melee hits by damaging the player too.
     */
    public void takeDamage(int amount) {
        boolean canReactToHit = !invincible;
        super.takeDamage(amount);

        if (!canReactToHit) {
            return;
        }

        boolean playerTouchedBoss = CollisionManager.rectanglesIntersect(getPlayerWorldSolidArea(), getWorldSolidArea());
        boolean playerMeleeHitBoss = gp.getPlayer().isAttackActive()
            && CollisionManager.rectanglesIntersect(gp.getPlayer().getAttackHitbox(), getWorldSolidArea());

        if (playerTouchedBoss || playerMeleeHitBoss) {
            gp.getPlayer().takeDamage(damage);
        }
    }

    @Override
    /**
     * Forces aggro mode after the boss is hit.
     */
    public void damageReaction() {
        actionLockCounter = 0;
        aggro = true;
    }
}
