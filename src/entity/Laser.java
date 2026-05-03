package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Laser {

    public enum OwnerType {
        PLAYER,
        ENEMY
    }

    private final OwnerType ownerType;
    private final Rectangle bounds;
    private final int damage;
    private final int duration;
    private int timer = 0;
    private boolean alive = true;

    public Laser(OwnerType ownerType, Rectangle bounds, int damage, int duration) {
        this.ownerType = ownerType;
        this.bounds = bounds;
        this.damage = damage;
        this.duration = duration;
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
        return bounds;
    }

    public void update() {
        if (!alive) {
            return;
        }

        timer++;
        if (timer >= duration) {
            alive = false;
        }
    }

    public void draw(Graphics2D g2, int cameraWorldX, int cameraWorldY) {
        if (!alive) {
            return;
        }

        g2.setColor(new Color(255, 0, 0, 140));
        g2.fillRect(bounds.x - cameraWorldX, bounds.y - cameraWorldY, bounds.width, bounds.height);
        g2.setColor(Color.RED);
        g2.drawRect(bounds.x - cameraWorldX, bounds.y - cameraWorldY, bounds.width, bounds.height);
    }
}
