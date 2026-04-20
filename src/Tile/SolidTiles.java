package Tile;

public class SolidTiles extends Tiles {
    
    public SolidTiles() {
        // Solid tiles are walls/obstacles that block entity movement.
        this.Collision = true;
    }
}
