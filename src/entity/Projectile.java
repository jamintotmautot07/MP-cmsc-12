package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import systems.CollisionManager;
import engine.GamePanel;

/**
 * Moving ranged attack entity that belongs to either the player or an enemy.
 */
public class Projectile extends Entity {

    /*
     * OwnerType matters because the same projectile class is used for player shots and enemy shots.
     * CombatResolver uses this value to decide whether the projectile can damage enemies or the player.
     */
    public enum OwnerType {
        PLAYER,
        ENEMY
    }

    private final GamePanel gp;
    private final OwnerType ownerType;
    private final int damage;
    private final int maxDistance;
    private final int speed;
    private int distanceTraveled = 0;
    private boolean alive = true;

    /**
     * Creates a projectile at a world position with fixed movement and lifetime settings.
     */
    public Projectile(GamePanel gp, OwnerType ownerType, String direction, int startX, int startY, int damage, int speed, int maxDistance, int width, int height) {
        this.gp = gp;
        this.ownerType = ownerType;
        this.direction = direction;
        this.worldX = startX;
        this.worldY = startY;
        this.damage = damage;
        this.speed = speed;
        this.maxDistance = maxDistance;
        this.renderWidth = width;
        this.renderHeight = height;
        this.solidArea = new Rectangle(0, 0, width, height);
    }

    /**
     * Returns whether the projectile should still update, draw, and collide.
     */
    public boolean isAlive() {
        return alive;
    }

    /**
     * Returns the damage applied when this projectile hits a valid target.
     */
    public int getDamage() {
        return damage;
    }

    /**
     * Returns who fired this projectile.
     */
    public OwnerType getOwnerType() {
        return ownerType;
    }

    /**
     * Returns the current projectile hitbox in world coordinates.
     */
    public Rectangle getBounds() {
        return new Rectangle(worldX, worldY, renderWidth, renderHeight);
    }

    /**
     * Moves the projectile, tracks distance, and kills it when it hits a wall or outranges itself.
     */
    public void update() {
        if (!alive) {
            return;
        }

        int dx = 0;
        int dy = 0;
        switch (direction) {
            case "up": dy = -speed; break;
            case "down": dy = speed; break;
            case "left": dx = -speed; break;
            case "right": dx = speed; break;
            case "up-left": dx = -speed; dy = -speed; break;
            case "up-right": dx = speed; dy = -speed; break;
            case "down-left": dx = -speed; dy = speed; break;
            case "down-right": dx = speed; dy = speed; break;
            default: dx = speed; break;
        }

        worldX += dx;
        worldY += dy;
        distanceTraveled += Math.abs(dx) + Math.abs(dy);

        if (distanceTraveled >= maxDistance) {
            alive = false;
            return;
        }

        Rectangle futureBounds = getBounds();
        if (CollisionManager.willCollideWithSolidTile(gp.getTileManager(), futureBounds)) {
            alive = false;
        }
    }

    /**
     * Manually marks the projectile dead after a combat hit.
     */
    public void kill() {
        alive = false;
    }

    /**
     * Draws the projectile relative to the camera.
     */
    public void draw(Graphics2D g2, int cameraWorldX, int cameraWorldY) {
        if (!alive) {
            return;
        }

        int screenX = worldX - cameraWorldX;
        int screenY = worldY - cameraWorldY;
        Color fill = ownerType == OwnerType.PLAYER ? new Color(0, 120, 255, 180) : new Color(255, 0, 0, 180);

        g2.setColor(fill);
        g2.fillRect(screenX, screenY, renderWidth, renderHeight);
        g2.setColor(Color.BLACK);
        g2.drawRect(screenX, screenY, renderWidth, renderHeight);
    }
}
