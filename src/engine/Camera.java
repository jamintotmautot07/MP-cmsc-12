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

    /**
     * Initializes the camera using the player's starting position.
     */
    public Camera(Player player) {
        update(player);
    }

    /**
     * Recomputes both camera world offset and player screen position.
     * Near map edges the player is allowed to move away from exact screen center so the camera never shows outside the world.
     */
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

    /**
     * Returns where the player should be drawn horizontally on screen.
     */
    public int getScreenX() {
        return screenX;
    }

    /**
     * Returns where the player should be drawn vertically on screen.
     */
    public int getScreenY() {
        return screenY;
    }

    /**
     * Returns the world x-coordinate at the left edge of the camera view.
     */
    public int getWorldX() {
        return worldX;
    }

    /**
     * Returns the world y-coordinate at the top edge of the camera view.
     */
    public int getWorldY() {
        return worldY;
    }
}
