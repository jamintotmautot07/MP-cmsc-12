package engine;

import entity.Player;
import util.Constants;

/**
 * Tracks the camera offset needed to render the player-centered world view.
 */
public class Camera {
    private int screenX;
    private int screenY;
    private int worldX;
    private int worldY;

    public Camera(Player player) {
        update(player);
    }

    public void update(Player player) {
        if (player.worldX < player.screenX) {
            screenX = player.worldX;
            worldX = 0;
        } else if (player.worldX > Constants.maxWorldWidth - (Constants.screenWidth - player.screenX)) {
            screenX = Constants.screenWidth - (Constants.maxWorldWidth - player.worldX);
            worldX = Constants.maxWorldWidth - Constants.screenWidth;
        } else {
            screenX = player.screenX;
            worldX = player.worldX - player.screenX;
        }

        if (player.worldY < player.screenY) {
            screenY = player.worldY;
            worldY = 0;
        } else if (player.worldY > Constants.maxWorldHeight - (Constants.screenHeight - player.screenY)) {
            screenY = Constants.screenHeight - (Constants.maxWorldHeight - player.worldY);
            worldY = Constants.maxWorldHeight - Constants.screenHeight;
        } else {
            screenY = player.screenY;
            worldY = player.worldY - player.screenY;
        }
    }

    public int getScreenX() {
        return screenX;
    }

    public int getScreenY() {
        return screenY;
    }

    public int getWorldX() {
        return worldX;
    }

    public int getWorldY() {
        return worldY;
    }
}
