
package util;

/*
 OWNER: ALL

 PURPOSE:
 - Store global constants
*/

/**
 * Central place for the project's shared sizing and world-dimension constants.
 */
public class Constants {
    // Base art size before scaling.
    public static final int originalTileSize = 16; //16x16 tiles

    // Uniform scale factor applied across the whole project.
    public static final int scale = 3;   

    // Final on-screen tile size after scaling.
    public static final int tileSize = originalTileSize * scale; //42x42 tiles

    // How many tiles fit on screen horizontally and vertically.
    public static final int maxScreenCol = 20;   
    public static final int maxScreenRow = 12;

    // Pixel dimensions of the game window.
    public static final int screenWidth = tileSize * maxScreenCol; //768 pixels
    public static final int screenHeight = tileSize * maxScreenRow; //576 pixels


    // World size measured in tiles.
    public static final int worldMaxCol = 50;
    public static final int worldMaxRow = 50;

    // World size converted into pixels.
    public static final int maxWorldWidth = tileSize * worldMaxCol;
    public static final int maxWorldHeight = tileSize * worldMaxRow;
}
