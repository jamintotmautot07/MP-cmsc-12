package Tile;

/**
 * Tile type used for walkable floor/background art.
 */
public class GroundTiles extends Tiles {
    public GroundTiles() {
        // Ground tiles are purely visual and do not block movement.
        this.Collision = false;
    }
}
