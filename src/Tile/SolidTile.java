package tile;

/**
 * Tile type used for walls and any map cell that should block movement.
 */
public class SolidTile extends Tile {
    public SolidTile() {
        collision = true;
    }
}
