package util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Random;

import Tile.TileManager;
import entity.CoreBoss;
import entity.Enemy;
import entity.Trojan;
import entity.VirusDrone;
import entity.Worm;
import entity.Entity;
import entity.Player;
import systems.CollisionManager;

/**
 * Small image helper utilities shared across sprite/tile loading code.
 */
public class UtilityTool {

    private static Random random = new Random();

    /**
     * Resizes one image to the requested pixel size.
     */
    public static BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        // Null-safe guard so callers do not need to repeat this check themselves.
        if (originalImage == null) {
            return null;
        }

        // TYPE_INT_ARGB preserves transparency, which is important for sprites and tiles.
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImage.createGraphics();

        // Bilinear interpolation is a simple quality improvement over nearest-neighbor scaling.
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(originalImage, 0, 0, width, height, null);
        g2.dispose();
        return resizedImage;
    }

    /**
     * Set a random valid position for an enemy (not on solid tiles).
     */
    public static void setRandomEnemyPosition(Enemy enemy, TileManager tileM) {
        int maxAttempts = 50;
        boolean validPosition = false;

        while (!validPosition && maxAttempts > 0) {
            int tileX = random.nextInt(Constants.maxWorldWidth / Constants.tileSize);
            int tileY = random.nextInt(Constants.maxWorldHeight / Constants.tileSize);

            enemy.worldX = tileX * Constants.tileSize;
            enemy.worldY = tileY * Constants.tileSize;

            // Check if the position is not on a solid tile
            if (!CollisionManager.willCollideWithSolidTile(tileM, new java.awt.Rectangle(
                    enemy.worldX + enemy.solidArea.x,
                    enemy.worldY + enemy.solidArea.y,
                    enemy.solidArea.width,
                    enemy.solidArea.height))) {
                validPosition = true;
            }

            maxAttempts--;
        }

        // Fallback if no valid position found
        if (!validPosition) {
            enemy.worldX = 10 * Constants.tileSize;
            enemy.worldY = 10 * Constants.tileSize;
        }
    }

    /**
     * Get a human-readable name for an enemy.
     */
    public static String getEntityName(Entity entity) {
        if (entity instanceof Worm) {
            return "Worm";
        } else if (entity instanceof Trojan) {
            return "Trojan";
        } else if (entity instanceof VirusDrone) {
            return "Virus";
        } else if (entity instanceof CoreBoss) {
            return "CoreBoss";
        } else if (entity instanceof Player) {
            return "Player";
        }
        return "Entity";
    }

}
