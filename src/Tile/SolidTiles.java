package Tile;

/**
 * Tile type used for walls and any map cell that should block movement.
 */
public class SolidTiles extends Tiles {
    
    public SolidTiles() {
        // Solid tiles are walls/obstacles that block entity movement.
        this.Collision = true;
    }
}
