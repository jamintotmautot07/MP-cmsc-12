package entity;

import engine.GamePanel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;
import javax.imageio.ImageIO;
import systems.CollisionManager;
import util.Constants;

/**
 * Enemy variant intended for chase behavior.
 * It currently uses proximity checks plus a simple fallback chase routine instead of a true pathfinder.
 */
public class EnemyPath extends Enemy {

    protected Random random = new Random();

    protected int actionLockCounter = 0;
    protected boolean onPath = false;

    // Sprite arrays for different states
    protected BufferedImage[] idleFrames;
    protected BufferedImage[] upFrames;
    protected BufferedImage[] downFrames;
    protected BufferedImage[] leftFrames;
    protected BufferedImage[] rightFrames;
    protected BufferedImage[] damagedFrames;

    /**
     * Builds a path-capable enemy with the same shared hitbox layout as the base enemy.
     */
    public EnemyPath(GamePanel gp) {
        super(gp);

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
     * Slightly faster defaults than the generic wandering enemy.
     */
    public void setDefaultValues() {
        speed = 2; // Slightly faster than basic enemy
        direction = "down";
        hp = 3;
        damage = 1;
        alive = true;
        dying = false;
        onPath = false;
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
            idleFrames[0] = ImageIO.read(new File("res/EnemyAssets/default.png"));
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
     * @param basePath Base path like "res/EnemyAssets/virus/"
     * @param state State like "idle", "up", "down", etc.
     * @param maxFrames Maximum frames to load
     * @return Array of loaded images, or null if none found
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
     * Switches between wandering and chase mode based on distance to the player.
     */
    public void setAction() {

        int distanceX = Math.abs(worldX - gp.player.worldX);
        int distanceY = Math.abs(worldY - gp.player.worldY);

        int tileDistance = (distanceX + distanceY) / Constants.tileSize;

        if (tileDistance < 6) {
            onPath = true;
        } else if (tileDistance > 10) {
            onPath = false;
        }

        if (onPath) {
            searchPath(
                gp.player.worldX / Constants.tileSize,
                gp.player.worldY / Constants.tileSize
            );
        } else {
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
    }

    public void searchPath(int goalCol, int goalRow) {
        // BUG NOTE: there is no real path graph or occupancy-aware pathfinding yet.
        // All pathing currently funnels into `fallbackChase`, so enemies pick the same obvious route,
        // stack on top of each other, and do not negotiate around one another.
        // If you later fix the crowding issue, this is the main entry point to replace with A*, BFS,
        // or any tile-based planner that also considers dynamic enemy occupancy / separation.
        // Related follow-up spots: `fallbackChase()`, `checkCollision()`, and `CollisionManager`.
        // TODO: Implement pathfinding system in GamePanel
        // For now, always use fallback chase
        fallbackChase(goalCol, goalRow);
    }

    /**
     * Fallback chase method when pathfinding is not available.
     * Uses simple directional logic toward the player.
     */
    protected void fallbackChase(int goalCol, int goalRow) {
        int startCol = (worldX + solidArea.x) / Constants.tileSize;
        int startRow = (worldY + solidArea.y) / Constants.tileSize;

        // Simple directional chase.
        // BUG NOTE: because every chaser uses the same greedy rule and only reasons about tiles,
        // multiple enemies often choose the exact same step and visually collapse into one cluster.
        // A later fix likely needs one or more of:
        // 1. enemy-vs-enemy collision/avoidance,
        // 2. per-tile occupancy reservation,
        // 3. steering/separation forces layered on top of the path.
        if (Math.abs(startCol - goalCol) > Math.abs(startRow - goalRow)) {
            // Move horizontally first
            if (startCol < goalCol) {
                direction = "right";
            } else {
                direction = "left";
            }
        } else {
            // Move vertically first
            if (startRow < goalRow) {
                direction = "down";
            } else {
                direction = "up";
            }
        }

        // Check collision for the chosen direction
        checkCollision();
        if (collisionOn) {
            // If blocked, try the other direction
            if (direction.equals("right") || direction.equals("left")) {
                if (startRow < goalRow) {
                    direction = "down";
                } else {
                    direction = "up";
                }
            } else {
                if (startCol < goalCol) {
                    direction = "right";
                } else {
                    direction = "left";
                }
            }
            checkCollision();
            if (collisionOn) {
                // If still blocked, stop chasing
                onPath = false;
            }
        }
    }

    /**
     * Tests tile collision for the current direction choice.
     */
    private void checkCollision() {
        collisionOn = false;
        // Check tile collision
        Rectangle futureSolidArea = new Rectangle(
            solidArea.x + worldX,
            solidArea.y + worldY,
            solidArea.width,
            solidArea.height
        );
        // BUG NOTE: this helper only checks solid map tiles, not other enemies or the player.
        // That is why chase enemies can overlap one another even when they should feel physically separate.
        // If collision is expanded later, this method is one of the best places to add enemy-body checks.
        if (CollisionManager.willCollideWithSolidTile(gp.tileM, futureSolidArea)) {
            collisionOn = true;
        }
    }

    /**
     * Runs one frame of chase-enemy logic.
     */
    public void update() {

        if (!alive) return;

        setAction();

        collisionOn = false;
        // Check tile collision
        Rectangle futureSolidArea = new Rectangle(
            solidArea.x + worldX,
            solidArea.y + worldY,
            solidArea.width,
            solidArea.height
        );
        // Same caveat as the base Enemy: this rectangle is not offset by the candidate movement step yet.
        if (CollisionManager.willCollideWithSolidTile(gp.tileM, futureSolidArea)) {
            collisionOn = true;
        }

        // Check player collision
        // BUG NOTE: still comparing local-space hitboxes rather than world-space rectangles.
        // This prevents reliable touch damage and also makes later enemy/player collision rules hard to trust.
        if (CollisionManager.rectanglesIntersect(gp.player.solidArea, solidArea)) {
            // Handle player collision (damage player, etc.)
            // TODO: Implement player damage
        }

        if (!collisionOn) {
            switch (direction) {
                case "up":    worldY -= speed; break;
                case "down":  worldY += speed; break;
                case "left":  worldX -= speed; break;
                case "right": worldX += speed; break;
            }
        }

        // Update animation
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
        
        int screenX = worldX - gp.getCameraWorldX();
        int screenY = worldY - gp.getCameraWorldY();

        if (image == null) {
            // Fallback: draw a colored rectangle
            g2.setColor(Color.BLUE);
            g2.fillRect(screenX, screenY,
                       Constants.tileSize, Constants.tileSize);
            return;
        }

        // BUG NOTE: like Enemy.render(), this still uses a player-relative transform.
        // If chase enemies appear to warp near map edges, the later rendering fix should mirror
        // the camera-based conversion used elsewhere in GamePanel and TileManager.

        if (invincible) {
            g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.5f));
        }

        g2.drawImage(image, screenX, screenY, renderWidth, renderHeight, null);

        if (invincible) {
            g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1f));
        }
    }

    /**
     * On damage, force the enemy back into chase mode.
     */
    public void damageReaction() {
        actionLockCounter = 0;
        onPath = true;
    }

    /**
     * Applies damage with the same invincibility rule as the base enemy.
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
}
