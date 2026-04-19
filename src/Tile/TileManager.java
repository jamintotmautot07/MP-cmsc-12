package Tile;

import engine.GamePanel;
import util.Constants;
import util.UtilityTool;
// import util.ResourceCache; // COMMENTED OUT - Cache system disabled

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TileManager {

    private static final String TILE_FOLDER = "res/TILES/";
    private static final int GROUND_TILE_COUNT = 180;

    GamePanel gp;
    public List<Tiles> tiles;
    public int[][] mapTileNum;

    public TileManager(GamePanel gp) {
        this.gp = gp;

        tiles = new ArrayList<>();
        mapTileNum = new int[Constants.worldMaxRow][Constants.worldMaxCol];

        loadAllTiles();
    }

    private void loadAllTiles() {
        loadTilesByPrefix("ground", false, GROUND_TILE_COUNT);
        loadTilesByPrefix("solid", true, Integer.MAX_VALUE);
    }

    private void loadTilesByPrefix(String prefix, boolean solid, int maxTiles) {
        int index = 1;

        while (index <= maxTiles) {
            String fileName = TILE_FOLDER + prefix + String.format("%03d", index) + ".png";
            BufferedImage image = loadImage(fileName);

            if (image == null) {
                if (maxTiles == Integer.MAX_VALUE) {
                    break;
                }
                index++;
                continue;
            }

            Tiles tile = solid ? new SolidTiles() : new GroundTiles();
            tile.image = UtilityTool.resizeImage(image, Constants.tileSize, Constants.tileSize);
            tiles.add(tile);
            index++;
        }
    }

    private BufferedImage loadImage(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                return ImageIO.read(file);
            }

            InputStream is = getClass().getResourceAsStream("/" + path.replace("\\", "/"));
            if (is != null) {
                return ImageIO.read(is);
            }
        } catch (IOException ignored) {
        }

        return null;
    }

    public void loadMap(String filePath) {
        try {
            BufferedReader br;
            InputStream is = getClass().getResourceAsStream(filePath);
            if (is != null) {
                br = new BufferedReader(new InputStreamReader(is));
            } else {
                File mapFile = new File(filePath.startsWith("/") ? filePath.substring(1) : filePath);
                br = new BufferedReader(new FileReader(mapFile));
            }

            String line;
            int row = 0;

            while (row < Constants.worldMaxRow && (line = br.readLine()) != null) {
                String[] numbers = line.trim().split("\\s+");
                for (int col = 0; col < Constants.worldMaxCol && col < numbers.length; col++) {
                    mapTileNum[row][col] = Integer.parseInt(numbers[col]) + 1;
                }
                row++;
            }

            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g2) {
        int cameraWorldX = gp.getCameraWorldX();
        int cameraWorldY = gp.getCameraWorldY();

        for (int worldRow = 0; worldRow < Constants.worldMaxRow; worldRow++) {
            for (int worldCol = 0; worldCol < Constants.worldMaxCol; worldCol++) {
                int tileNum = mapTileNum[worldRow][worldCol];
                int worldX = Constants.tileSize * worldCol;
                int worldY = Constants.tileSize * worldRow;
                int screenX = worldX - cameraWorldX;
                int screenY = worldY - cameraWorldY;

                if (worldX + Constants.tileSize > cameraWorldX - Constants.screenWidth &&
                    worldX - Constants.tileSize < cameraWorldX + Constants.screenWidth &&
                    worldY + Constants.tileSize > cameraWorldY - Constants.screenHeight &&
                    worldY - Constants.tileSize < cameraWorldY + Constants.screenHeight) {

                    Tiles currentTile = getTileByMapNumber(tileNum);
                    if (currentTile != null && currentTile.image != null) {
                        g2.drawImage(currentTile.image, screenX, screenY, null);
                    } else {
                        g2.setColor(Color.BLACK);
                        g2.fillRect(screenX, screenY, Constants.tileSize, Constants.tileSize);
                    }
                }
            }
        }
    }

    private Tiles getTileByMapNumber(int tileNum) {
        if (tileNum <= 0) {
            return tiles.isEmpty() ? null : tiles.get(0);
        }

        int index = tileNum - 1;
        if (index >= 0 && index < tiles.size()) {
            return tiles.get(index);
        }

        return tiles.isEmpty() ? null : tiles.get(0);
    }
}
