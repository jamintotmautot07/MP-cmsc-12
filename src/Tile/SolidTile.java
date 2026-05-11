package tile;

/**
 * Tile type used for walls and any map cell that should block movement.
 */
public class SolidTile extends Tile {
    /**
     * Creates a tile that blocks movement.
     */
    public SolidTile() {
        collision = true;
    }
}
