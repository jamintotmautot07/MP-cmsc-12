package tile;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Represents one loaded tile definition and whether that tile blocks movement.
 */
public class Tile {
    public Color color;
    public BufferedImage image;
    public boolean collision = false;
}
