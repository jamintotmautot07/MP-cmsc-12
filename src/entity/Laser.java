package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Temporary rectangular beam attack that persists for a fixed number of frames.
 */
public class Laser {

    /*
     * Kept parallel with Projectile.OwnerType so future player lasers could reuse this class too.
     */
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

    /**
     * Creates a rectangular beam that stays active for a fixed number of update frames.
     */
    public Laser(OwnerType ownerType, Rectangle bounds, int damage, int duration) {
        this.ownerType = ownerType;
        this.bounds = bounds;
        this.damage = damage;
        this.duration = duration;
    }

    /**
     * Returns whether the beam should still be updated, drawn, and checked for hits.
     */
    public boolean isAlive() {
        return alive;
    }

    /**
     * Returns the damage applied while something overlaps this beam.
     */
    public int getDamage() {
        return damage;
    }

    /**
     * Returns the side that created this laser.
     */
    public OwnerType getOwnerType() {
        return ownerType;
    }

    /**
     * Returns the beam rectangle in world coordinates.
     */
    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * Counts down the beam lifetime.
     */
    public void update() {
        if (!alive) {
            return;
        }

        timer++;
        if (timer >= duration) {
            alive = false;
        }
    }

    /**
     * Draws the beam relative to the camera.
     */
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
