package Tile;

import engine.GamePanel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import util.Constants;
import util.UtilityTool;

/**
 * Loads tile art, loads map text files, and renders the visible slice of the world grid.
 */
public class TileManager {
    // Folder and expected tile count for the current asset naming scheme.
    private static final String TILE_FOLDER = "res/TILES/";
    private static final int GROUND_TILE_COUNT = 180;

    // The manager needs the panel mainly for camera values during drawing.
    private final GamePanel gamePanel;

    // `tiles` is the palette of loaded tile definitions.
    // `mapTileNum` is the world grid that says which tile appears at each row/column.
    public final List<Tiles> tiles;
    public final int[][] mapTileNum;

    /**
     * Creates the tile palette and allocates the world map grid.
     */
    public TileManager(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        this.tiles = new ArrayList<>();
        this.mapTileNum = new int[Constants.worldMaxRow][Constants.worldMaxCol];
        this.loadAllTiles();
    }

    /**
     * Loads the full tile set expected by the current asset naming convention.
     */
    private void loadAllTiles() {
        // Ground tiles are loaded first, then solid tiles are appended after them.
        this.loadTilesByPrefix("ground", false, GROUND_TILE_COUNT);
        this.loadTilesByPrefix("solid", true, Integer.MAX_VALUE);
    }

    /**
     * Loads one family of tiles that share a filename prefix.
     */
    private void loadTilesByPrefix(String prefix, boolean solid, int maxTiles) {
        int tileIndex = 1;

        while (tileIndex <= maxTiles) {
            // Expected file format: prefix001.png, prefix002.png, and so on.
            String filePath = TILE_FOLDER + prefix + String.format("%03d", tileIndex) + ".png";
            BufferedImage image = this.loadImage(filePath);

            if (image == null) {
                // Ground tiles tolerate gaps because the count is fixed.
                // Solid tiles stop at the first missing file because they are treated as an open-ended list.
                if (maxTiles == Integer.MAX_VALUE) {
                    break;
                }
                tileIndex++;
                continue;
            }

            Tiles tile = solid ? new SolidTiles() : new GroundTiles();
            // Resize here once so draw calls stay lightweight later.
            tile.image = UtilityTool.resizeImage(image, Constants.tileSize, Constants.tileSize);
            this.tiles.add(tile);
            tileIndex++;
        }
    }

    /**
     * Loads one tile image from disk or classpath.
     */
    private BufferedImage loadImage(String resourcePath) {
        try {
            // First try plain file-system loading. This is convenient during development.
            File file = new File(resourcePath);
            if (file.exists()) {
                return ImageIO.read(file);
            }

            // Fallback to classpath loading for packaged runs.
            InputStream inputStream = this.getClass().getResourceAsStream("/" + resourcePath.replace("\\", "/"));
            if (inputStream != null) {
                return ImageIO.read(inputStream);
            }
        } catch (IOException ignored) {
        }

        return null;
    }

    /**
     * Reads a text map file into the 2D tile-number grid.
     */
    public void loadMap(String filePath) {
        try {
            InputStream resourceStream = this.getClass().getResourceAsStream(filePath);
            BufferedReader reader;

            // Like image loading, map loading supports both bundled resources and plain files.
            if (resourceStream != null) {
                reader = new BufferedReader(new InputStreamReader(resourceStream));
            } else {
                File mapFile = new File(filePath.startsWith("/") ? filePath.substring(1) : filePath);
                reader = new BufferedReader(new FileReader(mapFile));
            }

            String line;
            for (int row = 0; row < Constants.worldMaxRow && (line = reader.readLine()) != null; row++) {
                String[] tokens = line.trim().split("\\s+");
                for (int col = 0; col < Constants.worldMaxCol && col < tokens.length; col++) {
                    // Map files use zero-based tile ids, so the code shifts them by +1 for its internal lookup.
                    this.mapTileNum[row][col] = Integer.parseInt(tokens[col]) + 1;
                }
            }

            reader.close();
        } catch (Exception e) {
        }
    }

    /**
     * Draws only the tiles near the camera view.
     */
    public void draw(Graphics2D g2) {
        int cameraWorldX = this.gamePanel.getCameraWorldX();
        int cameraWorldY = this.gamePanel.getCameraWorldY();

        // Walk the full map grid, but only draw the tiles that are near the camera view.
        for (int row = 0; row < Constants.worldMaxRow; row++) {
            for (int col = 0; col < Constants.worldMaxCol; col++) {
                int tileNum = this.mapTileNum[row][col];
                int worldX = Constants.tileSize * col;
                int worldY = Constants.tileSize * row;
                int screenX = worldX - cameraWorldX;
                int screenY = worldY - cameraWorldY;

                // Simple visibility culling so off-screen tiles do not get drawn every frame.
                if (worldX + Constants.tileSize > cameraWorldX - Constants.screenWidth &&
                    worldX - Constants.tileSize < cameraWorldX + Constants.screenWidth &&
                    worldY + Constants.tileSize > cameraWorldY - Constants.screenHeight &&
                    worldY - Constants.tileSize < cameraWorldY + Constants.screenHeight) {

                    Tiles tile = this.getTileByMapNumber(tileNum);
                    if (tile != null && tile.image != null) {
                        g2.drawImage(tile.image, screenX, screenY, (ImageObserver) null);
                    } else {
                        // Missing tile data is rendered as black so the bug is visible instead of failing silently.
                        g2.setColor(Color.BLACK);
                        g2.fillRect(screenX, screenY, Constants.tileSize, Constants.tileSize);
                    }
                }
            }
        }
    }

    /**
     * Resolves one map number into a loaded tile definition.
     */
    private Tiles getTileByMapNumber(int tileNum) {
        if (tileNum <= 0) {
            // Fall back to the first loaded tile if the map contains an invalid or empty value.
            return this.tiles.isEmpty() ? null : this.tiles.get(0);
        }

        int tileIndex = tileNum - 1;
        if (tileIndex >= 0 && tileIndex < this.tiles.size()) {
            return this.tiles.get(tileIndex);
        }

        return this.tiles.isEmpty() ? null : this.tiles.get(0);
    }

    /**
     * Checks whether the tile at a grid position blocks movement.
     */
    public boolean isTileSolid(int row, int col) {
        // Out-of-bounds is treated as non-solid here.
        // That keeps this method simple, while world clamping is handled elsewhere.
        if (row < 0 || row >= Constants.worldMaxRow || col < 0 || col >= Constants.worldMaxCol) {
            return false;
        }

        Tiles tile = this.getTileByMapNumber(this.mapTileNum[row][col]);
        return tile != null && tile.Collision;
    }

    /**
     * Returns the full tile object at the requested map cell.
     */
    public Tiles getTileAt(int row, int col) {
        // Convenience helper for any future logic that needs the full tile object at a grid position.
        if (row < 0 || row >= Constants.worldMaxRow || col < 0 || col >= Constants.worldMaxCol) {
            return null;
        }

        return this.getTileByMapNumber(this.mapTileNum[row][col]);
    }
}
