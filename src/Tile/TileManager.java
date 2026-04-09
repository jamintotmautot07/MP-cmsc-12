package Tile;

import engine.GamePanel;

import java.awt.Graphics2D;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import util.Constants;

public class TileManager {
    
    GamePanel gp;
    public Tiles[] tile;
    public int mapTileNum[][];

    public TileManager(GamePanel gp) {
        this.gp = gp;

        tile = new Tiles[10];

        mapTileNum = new int[Constants.maxWorldHeight][Constants.maxWorldWidth];

        getTileImage();
    }

    public void getTileImage() {
        try {

            tile[0] = new Tiles();
            tile[0].color = Color.GRAY;

            tile[1] = new Tiles();
            tile[1].color = Color.BLUE;

            tile[2] = new Tiles();
            tile[2].color = Color.BLACK;

            tile[3] = new Tiles();
            tile[3].color = Color.RED;

        } catch (Exception e) {
            e.printStackTrace();
        }

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

            int col = 0;
            int row = 0;

            while(col < Constants.worldMaxCol && row < Constants.worldMaxRow) {
                String line = br.readLine();
                if (line == null) break;

                String numbers[] = line.split(" ");
                while(col < Constants.worldMaxCol && col < numbers.length) {
                    int num = Integer.parseInt(numbers[col]);
                    mapTileNum[col][row] = num;
                    col++;
                }

                col = 0;
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

        int worldCol = 0;
        int worldRow = 0;

        while(worldCol < Constants.worldMaxCol && worldRow < Constants.worldMaxRow) {

            int tileNum = mapTileNum[worldCol][worldRow];

            int worldX = Constants.tileSize * worldCol;
            int worldY = Constants.tileSize * worldRow;
            int screenX = worldX - cameraWorldX;
            int screenY = worldY - cameraWorldY;

            if(worldX + Constants.tileSize > cameraWorldX - (Constants.screenWidth) &&
                worldX - Constants.tileSize < cameraWorldX + (Constants.screenWidth) &&
                worldY + Constants.tileSize > cameraWorldY - (Constants.screenHeight) &&
                worldY - Constants.tileSize < cameraWorldY + (Constants.screenHeight)
            ) {
                g2.setColor(tile[tileNum].color);
                g2.fillRect(screenX, screenY, Constants.tileSize, Constants.tileSize);
            }

            worldCol++;

            if(worldCol == Constants.worldMaxCol) {
                worldCol = 0;
                worldRow++;
            }
        }
    }

}
