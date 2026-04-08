package Tile;

import engine.GamePanel;

import java.awt.Graphics2D;
import java.awt.Color;
import java.io.BufferedReader;
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

            InputStream is = getClass().getResourceAsStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            int col = 0;
            int row = 0;

            while(col < Constants.worldMaxRow && row < Constants.worldMaxCol) {

                String line = br.readLine();

                while(col < Constants.worldMaxCol) {
                    String numbers[] = line.split(" ");

                    int num = Integer.parseInt(numbers[col]);

                    mapTileNum[col][row] = num;
                    col++;
                }

                if(col == Constants.worldMaxRow) {
                    col = 0;
                    row++;
                }

            }

            br.close();
            
        } catch (Exception e) {
        }
    }
    
    public void draw(Graphics2D g2) {

        int worldCol = 0;
        int worldRow = 0;

        while(worldCol < Constants.worldMaxCol && worldRow < Constants.worldMaxRow) {

            int tileNum = mapTileNum[worldCol][worldRow];

            int worldX = Constants.tileSize * worldCol;
            int worldY = Constants.tileSize * worldRow;
            int screenX = worldX - gp.player.worldX + gp.player.screenX;
            int screenY = worldY - gp.player.worldY + gp.player.screenY;

            if(worldX + Constants.tileSize > gp.player.worldX - gp.player.screenX &&
                worldX - Constants.tileSize < gp.player.worldX + gp.player.screenX &&
                worldY + Constants.tileSize > gp.player.worldY - gp.player.screenY &&
                worldY - Constants.tileSize < gp.player.worldY + gp.player.screenY
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
