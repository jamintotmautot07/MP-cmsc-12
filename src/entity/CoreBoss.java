package entity;

import java.awt.Rectangle;

import engine.GamePanel;
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

    public CoreBoss(GamePanel gp) {
        super(gp);
        setDefaultValues();
        loadSprites();
    }

    @Override
    public void setDefaultValues() {
        speed = 0;
        hp = 15;
        maxHp = 15;
        damage = 2;
        renderWidth = Constants.tileSize * 6;
        renderHeight = Constants.tileSize * 6;
        direction = "down";
        alive = true;
        dying = false;

        int padding = Constants.tileSize / 2;
        solidArea = new Rectangle(
            padding,
            padding,
            renderWidth - (padding * 4),
            renderHeight - (padding * 4)
        );
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }

    @Override
    protected void loadSprites() {
        String basePath = "res/EnemyAssets/Boss/";

        idleFrames = loadSpriteArray(basePath, "idle", 6);
        upFrames = idleFrames;
        downFrames = idleFrames;
        leftFrames = idleFrames;
        rightFrames = idleFrames;
        damagedFrames = idleFrames;

        if (idleFrames != null) {
            idleFrames = resizeSpriteArray(idleFrames, renderWidth, renderHeight);
            upFrames = resizeSpriteArray(upFrames, renderWidth, renderHeight);
            downFrames = resizeSpriteArray(downFrames, renderWidth, renderHeight);
            leftFrames = resizeSpriteArray(leftFrames, renderWidth, renderHeight);
            rightFrames = resizeSpriteArray(rightFrames, renderWidth, renderHeight);
            damagedFrames = resizeSpriteArray(damagedFrames, renderWidth, renderHeight);
        } else {
            super.loadSprites();
        }
    }

    @Override
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
    public void update() {
        if (!alive) return;

        initializeHomePosition();

        if (hp <= 7) {
            aggro = true;
        }

        setAction();
        updateAggroMovement();
        updateBossAttack();
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

    private void initializeHomePosition() {
        if (homeInitialized) {
            return;
        }

        homeX = worldX;
        homeY = worldY;
        homeInitialized = true;
    }

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

    private Rectangle buildHorizontalLaser(boolean toRight) {
        int y = clamp(getCenterY() - LASER_THICKNESS / 2, 0, Constants.maxWorldHeight - LASER_THICKNESS);

        if (toRight) {
            int startX = worldX + renderWidth;
            int maxX = Math.min(Constants.maxWorldWidth, startX + Constants.screenWidth);
            int endX = startX;

            while (endX < maxX) {
                int segmentWidth = Math.min(Constants.tileSize, maxX - endX);
                Rectangle segment = new Rectangle(endX, y, segmentWidth, LASER_THICKNESS);
                if (CollisionManager.willCollideWithSolidTile(gp.tileM, segment)) {
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
            if (CollisionManager.willCollideWithSolidTile(gp.tileM, segment)) {
                break;
            }
            currentX = segmentX;
        }

        return new Rectangle(currentX, y, bossLeft - currentX, LASER_THICKNESS);
    }

    private Rectangle buildVerticalLaser(boolean downward) {
        int x = clamp(getCenterX() - LASER_THICKNESS / 2, 0, Constants.maxWorldWidth - LASER_THICKNESS);

        if (downward) {
            int startY = worldY + renderHeight;
            int maxY = Math.min(Constants.maxWorldHeight, startY + Constants.screenHeight);
            int endY = startY;

            while (endY < maxY) {
                int segmentHeight = Math.min(Constants.tileSize, maxY - endY);
                Rectangle segment = new Rectangle(x, endY, LASER_THICKNESS, segmentHeight);
                if (CollisionManager.willCollideWithSolidTile(gp.tileM, segment)) {
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
            if (CollisionManager.willCollideWithSolidTile(gp.tileM, segment)) {
                break;
            }
            currentY = segmentY;
        }

        return new Rectangle(x, currentY, LASER_THICKNESS, bossTop - currentY);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public void takeDamage(int amount) {
        boolean canReactToHit = !invincible;
        super.takeDamage(amount);

        if (!canReactToHit) {
            return;
        }

        boolean playerTouchedBoss = CollisionManager.rectanglesIntersect(getPlayerWorldSolidArea(), getWorldSolidArea());
        boolean playerMeleeHitBoss = gp.player.isAttackActive()
            && CollisionManager.rectanglesIntersect(gp.player.getAttackHitbox(), getWorldSolidArea());

        if (playerTouchedBoss || playerMeleeHitBoss) {
            gp.player.takeDamage(damage);
        }
    }

    @Override
    public void damageReaction() {
        actionLockCounter = 0;
        aggro = true;
    }
}
