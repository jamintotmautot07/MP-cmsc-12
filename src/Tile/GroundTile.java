package tile;

/**
 * Tile type used for walkable floor/background art.
 */
public class GroundTile extends Tile {
    /**
     * Creates a tile that entities can walk over.
     */
    public GroundTile() {
        collision = false;
    }
}
