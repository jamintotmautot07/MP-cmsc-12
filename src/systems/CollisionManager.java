
package systems;

import Tile.TileManager;
import entity.Enemy;
import entity.Entity;
import entity.Player;

import java.awt.Rectangle;
import java.util.List;
import util.Constants;

/*
 OWNER: Allan

 PURPOSE:
 - Handles collision detection

 TASKS:
 1. Implement AABB collision
 2. Check:
    - player vs enemy
    - attack vs enemy

 OPTIONAL:
 - Separate hitboxes
*/

/**
 * Shared collision helpers for tile checks and simple rectangle intersection tests.
 * Right now this is intentionally small and focused on map solidity.
 */
public class CollisionManager {

    /*
     * Checks whether an entity's future collision box would overlap any solid map tile.
     * This is tile-based collision, so the rectangle is converted into tile row/column ranges.
     */
    public static boolean willCollideWithSolidTile(TileManager tileM, Rectangle futureSolidArea) {
        if (tileM == null || futureSolidArea == null) {
            return false;
        }

        // Convert pixel bounds into inclusive tile indices.
        int leftTile = futureSolidArea.x / Constants.tileSize;
        int rightTile = (futureSolidArea.x + futureSolidArea.width - 1) / Constants.tileSize;
        int topTile = futureSolidArea.y / Constants.tileSize;
        int bottomTile = (futureSolidArea.y + futureSolidArea.height - 1) / Constants.tileSize;

        // If any tile under the rectangle is solid, movement should be blocked.
        for (int col = leftTile; col <= rightTile; col++) {
            for (int row = topTile; row <= bottomTile; row++) {
                if (tileM.isTileSolid(row, col)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean rectanglesIntersect(Rectangle a, Rectangle b) {
        if (a == null || b == null) {
            return false;
        }
        // BUG NOTE: callers must pass rectangles in the same coordinate space.
        // Several current enemy/player checks pass local solid-area rectangles directly,
        // so intersection results can be misleading even though this helper itself is correct.
        // A later improvement could add a helper that builds world-space rectangles from entity position + solidArea.
        // Uses Java's built-in rectangle overlap check for axis-aligned hitboxes.
        return a.intersects(b);
    }

    public static Rectangle getWorldSolidArea(Entity entity) {
        if (entity == null) {
            return new Rectangle();
        }

        return getWorldSolidArea(entity, entity.worldX, entity.worldY);
    }

    public static Rectangle getWorldSolidArea(Entity entity, int worldX, int worldY) {
        if (entity == null) {
            return new Rectangle();
        }

        if (entity.solidArea == null) {
            return new Rectangle(worldX, worldY, entity.renderWidth, entity.renderHeight);
        }

        return new Rectangle(
            worldX + entity.solidArea.x,
            worldY + entity.solidArea.y,
            entity.solidArea.width,
            entity.solidArea.height
        );
    }

    public static boolean willCollideWithEntity(Rectangle futureSolidArea, Entity entity) {
        if (futureSolidArea == null || entity == null || entity.solidArea == null) {
            return false;
        }

        return rectanglesIntersect(futureSolidArea, getWorldSolidArea(entity));
    }

    public static boolean willCollideWithAnyEnemy(Rectangle futureSolidArea, List<Enemy> enemies, Enemy ignoredEnemy) {
        if (futureSolidArea == null || enemies == null) {
            return false;
        }

        for (Enemy enemy : enemies) {
            if (enemy == null || enemy == ignoredEnemy || !enemy.isAlive()) {
                continue;
            }
            if (willCollideWithEntity(futureSolidArea, enemy)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if the player collides with an enemy.
     */
    // public static void checkPlayerEnemyCollision(Enemy enemy, Player player) {
    //     // Convert both player and enemy solidAreas to world coordinates for proper collision detection
    //     java.awt.Rectangle playerWorldSolid = new java.awt.Rectangle(
    //         player.worldX + player.solidArea.x,
    //         player.worldY + player.solidArea.y,
    //         player.solidArea.width,
    //         player.solidArea.height
    //     );

    //     java.awt.Rectangle enemyWorldSolid = new java.awt.Rectangle(
    //         enemy.worldX + enemy.solidArea.x,
    //         enemy.worldY + enemy.solidArea.y,
    //         enemy.solidArea.width,
    //         enemy.solidArea.height
    //     );

    //     if (CollisionManager.rectanglesIntersect(playerWorldSolid, enemyWorldSolid)) {
    //         String enemyName = UtilityTool.getEnemyName(enemy);
    //         enemy.damageReaction();
    //         player.takeDamage(enemy.getDamage());
    //     }
    // }

    // /**
    //  * Check if two enemies collide.
    //  */
    // public static void checkEnemyEnemyCollision(Enemy enemy1, Enemy enemy2) {
    //     java.awt.Rectangle enemy1WorldSolid = new java.awt.Rectangle(
    //         enemy1.worldX + enemy1.solidArea.x,
    //         enemy1.worldY + enemy1.solidArea.y,
    //         enemy1.solidArea.width,
    //         enemy1.solidArea.height
    //     );

    //     java.awt.Rectangle enemy2WorldSolid = new java.awt.Rectangle(
    //         enemy2.worldX + enemy2.solidArea.x,
    //         enemy2.worldY + enemy2.solidArea.y,
    //         enemy2.solidArea.width,
    //         enemy2.solidArea.height
    //     );

    //     if (CollisionManager.rectanglesIntersect(enemy1WorldSolid, enemy2WorldSolid)) {
    //         String enemy1Name = UtilityTool.getEnemyName(enemy1);
    //         String enemy2Name = UtilityTool.getEnemyName(enemy2);
    //     }
    // }

    /*
        Collision method that handles every type of interaction
    */
   public static void checkCollisionBetweenEntities(Entity entity1, Entity entity2) {
// Convert both player and enemy solidAreas to world coordinates for proper collision detection
        java.awt.Rectangle entity1SolidWorld = new java.awt.Rectangle(
            entity1.worldX + entity1.solidArea.x,
            entity1.worldY + entity1.solidArea.y,
            entity1.solidArea.width,
            entity1.solidArea.height
        );

        java.awt.Rectangle entity2SolidWorld = new java.awt.Rectangle(
            entity2.worldX + entity2.solidArea.x,
            entity2.worldY + entity2.solidArea.y,
            entity2.solidArea.width,
            entity2.solidArea.height
        );

        if (CollisionManager.rectanglesIntersect(entity1SolidWorld, entity2SolidWorld)) {

            if(entity1 instanceof Enemy && entity2 instanceof Player) {
                Enemy enemy = (Enemy)entity1;
                Player player = (Player)entity2;

                enemy.damageReaction();
                player.takeDamage(enemy.getDamage());
            }
            
        }
   }
}
