package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import systems.CollisionManager;
import engine.GamePanel;

public class Projectile extends Entity {

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

    public boolean isAlive() {
        return alive;
    }

    public int getDamage() {
        return damage;
    }

    public OwnerType getOwnerType() {
        return ownerType;
    }

    public Rectangle getBounds() {
        return new Rectangle(worldX, worldY, renderWidth, renderHeight);
    }

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
        if (CollisionManager.willCollideWithSolidTile(gp.tileM, futureBounds)) {
            alive = false;
        }
    }

    public void kill() {
        alive = false;
    }

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
