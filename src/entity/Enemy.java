
package entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

import javax.imageio.ImageIO;

import engine.GamePanel;
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
    protected int maxHp = 3;
    protected int damage = 1;

    protected boolean attackActive = false;
    protected int attackCounter = 0;
    protected int attackDuration = 18;
    protected String attackDirection = "down";

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
        maxHp = 3;
        damage = 1;
        alive = true;
        dying = false;
    }

    protected BufferedImage[] loadCachedSpriteArray(String enemyKey, String state, int frameCount) {
        BufferedImage[] frames = new BufferedImage[frameCount];

        for (int i = 0; i < frameCount; i++) {
            frames[i] = ResourceCache.getImage("enemy_" + enemyKey + "_" + state + "_" + i);
        }

        return frames;
    }

    public void setStartPosition(int worldX, int worldY) {
        this.worldX = worldX;
        this.worldY = worldY;
    }

    public void setStartTilePosition(int col, int row) {
        setStartPosition(col * Constants.tileSize, row * Constants.tileSize);
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
     * Returns this enemy's collision body in world coordinates.
     */
    protected Rectangle getWorldSolidArea() {
        return new Rectangle(
            worldX + solidArea.x,
            worldY + solidArea.y,
            solidArea.width,
            solidArea.height
        );
    }

    /**
     * Returns the player's collision body in world coordinates.
     */
    protected Rectangle getPlayerWorldSolidArea() {
        return new Rectangle(
            gp.player.worldX + gp.player.solidArea.x,
            gp.player.worldY + gp.player.solidArea.y,
            gp.player.solidArea.width,
            gp.player.solidArea.height
        );
    }

    protected int getCenterX() {
        return worldX + renderWidth / 2;
    }

    protected int getCenterY() {
        return worldY + renderHeight / 2;
    }

    protected int getPlayerCenterX() {
        return gp.player.worldX + Constants.tileSize / 2;
    }

    protected int getPlayerCenterY() {
        return gp.player.worldY + Constants.tileSize / 2;
    }

    protected int getTileDistanceToPlayer() {
        int distanceX = Math.abs(getCenterX() - getPlayerCenterX());
        int distanceY = Math.abs(getCenterY() - getPlayerCenterY());
        return (distanceX + distanceY) / Constants.tileSize;
    }

    protected String getCardinalDirectionTowardPlayer() {
        int distanceX = getPlayerCenterX() - getCenterX();
        int distanceY = getPlayerCenterY() - getCenterY();

        if (Math.abs(distanceX) > Math.abs(distanceY)) {
            return distanceX < 0 ? "left" : "right";
        }

        return distanceY < 0 ? "up" : "down";
    }

    protected String getEightWayDirectionTowardPlayer() {
        int distanceX = getPlayerCenterX() - getCenterX();
        int distanceY = getPlayerCenterY() - getCenterY();
        int deadZone = Constants.tileSize / 3;

        boolean horizontal = Math.abs(distanceX) > deadZone;
        boolean vertical = Math.abs(distanceY) > deadZone;

        if (horizontal && vertical) {
            if (distanceX < 0 && distanceY < 0) return "up-left";
            if (distanceX > 0 && distanceY < 0) return "up-right";
            if (distanceX < 0) return "down-left";
            return "down-right";
        }

        if (horizontal) {
            return distanceX < 0 ? "left" : "right";
        }

        return distanceY < 0 ? "up" : "down";
    }

    /**
     * A melee attack is allowed only when the attack box itself reaches the player.
     */
    protected boolean canHitPlayerWithMelee(String attackDirection) {
        Rectangle candidateHitbox = calculateAttackHitbox(attackDirection);
        return CollisionManager.rectanglesIntersect(candidateHitbox, getPlayerWorldSolidArea());
    }

    protected boolean canMoveTo(int nextX, int nextY) {
        Rectangle futureSolidArea = CollisionManager.getWorldSolidArea(this, nextX, nextY);
        return !CollisionManager.willCollideWithSolidTile(gp.tileM, futureSolidArea)
            && !CollisionManager.willCollideWithEntity(futureSolidArea, gp.player)
            && !CollisionManager.willCollideWithAnyEnemy(futureSolidArea, gp.enemies, this);
    }

    protected void fireProjectileAtPlayer(int damage, int projectileSpeed, int rangeTiles, int size) {
        String projectileDirection = getEightWayDirectionTowardPlayer();
        int startX = getCenterX() - size / 2;
        int startY = getCenterY() - size / 2;

        gp.spawnProjectile(new Projectile(
            gp,
            Projectile.OwnerType.ENEMY,
            projectileDirection,
            startX,
            startY,
            damage,
            projectileSpeed,
            rangeTiles * Constants.tileSize,
            size,
            size
        ));
    }

    /**
     * Runs one frame of enemy logic: choose an action, test collisions, move, animate, and update timers.
     */
    public void update() {
        if (!alive) return;

        setAction();

        collisionOn = false;
        int nextX = worldX;
        int nextY = worldY;
        if (!attackActive) {
            switch (direction) {
                case "up":    nextY -= speed; break;
                case "down":  nextY += speed; break;
                case "left":  nextX -= speed; break;
                case "right": nextX += speed; break;
            }
        }

        if (!attackActive && !canMoveTo(nextX, nextY)) {
            collisionOn = true;
        }

        if (!collisionOn && !attackActive) {
            worldX = nextX;
            worldY = nextY;
        }

        // Update animation
        updateAnimation();
        updateAttackState();

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
     * Starts an enemy melee attack or directional attack.
     */
    protected void startEnemyAttack(AttackType type, String direction) {
        if (!attackActive) {
            attackType = type;
            attackDirection = direction;
            attackActive = true;
            attackCounter = 0;
            attackHitbox.setBounds(0, 0, 0, 0);
        }
    }

    protected void updateAttackState() {
        if (!attackActive) {
            return;
        }

        attackCounter++;
        attackHitbox = calculateAttackHitbox(attackDirection);

        if (attackCounter > attackDuration) {
            attackActive = false;
            attackCounter = 0;
            attackType = AttackType.NONE;
            attackHitbox.setBounds(0, 0, 0, 0);
        }
    }

    public boolean isAttackActive() {
        return attackActive;
    }

    public Rectangle getAttackHitbox() {
        return attackHitbox;
    }

    public int getDamage() {
        return damage;
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

        if (image == null) {
            // Fallback: draw a colored rectangle
            g2.setColor(Color.RED);
            g2.fillRect(worldX - gp.getCameraWorldX(),
                       worldY - gp.getCameraWorldY(),
                       renderWidth, renderHeight);
            renderHealthBar(g2, worldX - gp.getCameraWorldX(), worldY - gp.getCameraWorldY());
            return;
        }

        // Draw with the same camera transform used by tiles and player hitboxes.
        int screenX = worldX - gp.getCameraWorldX();
        int screenY = worldY - gp.getCameraWorldY();

        if (invincible) {
            g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.5f));
        }

        g2.drawImage(image, screenX, screenY, renderWidth, renderHeight, null);
        renderHealthBar(g2, screenX, screenY);

        if (attackActive && attackHitbox.width > 0 && attackHitbox.height > 0) {
            int hitX = attackHitbox.x - gp.getCameraWorldX();
            int hitY = attackHitbox.y - gp.getCameraWorldY();
            g2.setColor(new Color(255, 0, 0, 120));
            g2.fillRect(hitX, hitY, attackHitbox.width, attackHitbox.height);
            g2.setColor(Color.RED);
            g2.drawRect(hitX, hitY, attackHitbox.width, attackHitbox.height);
        }

        if (invincible) {
            g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1f));
        }
    }

    protected void renderHealthBar(Graphics2D g2, int screenX, int screenY) {
        if (maxHp <= 0 || hp <= 0) {
            return;
        }

        int barWidth = Math.max(34, Math.min(renderWidth, Constants.tileSize * 3));
        int barHeight = 7;
        int barX = screenX + (renderWidth - barWidth) / 2;
        int barY = screenY - 14;
        int fillWidth = Math.max(0, Math.min(barWidth, (int) Math.round((hp / (double) maxHp) * barWidth)));

        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(barX - 4, barY - 4, barWidth + 8, barHeight + 8, 8, 8);
        g2.setColor(new Color(80, 10, 10, 230));
        g2.fillRoundRect(barX, barY, barWidth, barHeight, 6, 6);
        g2.setColor(new Color(45, 245, 105, 240));
        g2.fillRoundRect(barX, barY, fillWidth, barHeight, 6, 6);
        g2.setColor(new Color(190, 255, 205, 170));
        g2.drawLine(barX + 1, barY + 1, barX + Math.max(1, fillWidth - 2), barY + 1);
        g2.setColor(new Color(15, 15, 15, 240));
        g2.drawRoundRect(barX - 1, barY - 1, barWidth + 1, barHeight + 1, 7, 7);
    }

    public void draw(Graphics2D g2) {
        render(g2);
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

            if (hp <= 0) {
                dying = true;
                alive = false;
            }
        }
    }

    /**
     * Small helper used by update/render loops to skip dead enemies.
     */
    public boolean isAlive() {
        return alive;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

}
