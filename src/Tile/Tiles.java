package Tile;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class Tiles {
    
    // Optional fallback color if a tile is ever rendered without an image.
    public Color color;

    // Sprite image for this tile type.
    public BufferedImage image;

    // Whether the tile blocks movement.
    public boolean Collision = false;

}
