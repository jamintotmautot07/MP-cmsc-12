
package systems;

import Tile.TileManager;
import java.awt.Rectangle;
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
        // Uses Java's built-in rectangle overlap check for axis-aligned hitboxes.
        return a.intersects(b);
    }
}
