
package util;

/*
 OWNER: ALL

 PURPOSE:
 - Store global constants
*/

public class Constants {
    // Screen setting
    public static final int originalTileSize = 16; //16x16 tiles
    public static final int scale = 3;   
    public static final int tileSize = originalTileSize * scale; //42x42 tiles
    public static final int maxScreenCol = 20;   
    public static final int maxScreenRow = 12;
    public static final int screenWidth = tileSize * maxScreenCol; //768 pixels
    public static final int screenHeight = tileSize * maxScreenRow; //576 pixels


    // World Settings
    public static final int worldMaxCol = 50;
    public static final int worldMaxRow = 50;
    public static final int maxWorldWidth = tileSize * worldMaxCol;
    public static final int maxWorldHeight = tileSize * worldMaxRow;
}
