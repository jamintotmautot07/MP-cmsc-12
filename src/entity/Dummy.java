package entity;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import engine.GamePanel;
import util.Constants;

/**
 * Tutorial-only practice enemy. It drifts toward the player, deals no damage, and never attacks.
 */
public class Dummy extends Enemy {
    /**
     * Builds a harmless training target for the tutorial map.
     */
    public Dummy(GamePanel gp) {
        super(gp);
        setDefaultValues();
        loadSprites();
    }

    @Override
    /**
     * Gives the dummy low health and zero damage so it teaches combat without punishing the player.
     */
    public void setDefaultValues() {
        super.setDefaultValues();
        speed = 1;
        hp = 2;
        maxHp = 2;
        damage = 0;
        renderWidth = Constants.tileSize;
        renderHeight = Constants.tileSize;

        solidArea = new Rectangle(8, 12, 32, 32);
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }

    @Override
    /**
     * Loads the dummy animation set and scales it to one tile.
     */
    protected void loadSprites() {
        idleFrames = loadCachedSpriteArray("dummy", "idle", 10);
        upFrames = loadCachedSpriteArray("dummy", "up", 5);
        downFrames = loadCachedSpriteArray("dummy", "down", 5);
        leftFrames = loadCachedSpriteArray("dummy", "left", 6);
        rightFrames = loadCachedSpriteArray("dummy", "right", 6);
        damagedFrames = idleFrames;

        idleFrames = resizeSpriteArray(idleFrames, renderWidth, renderHeight);
        upFrames = resizeSpriteArray(upFrames, renderWidth, renderHeight);
        downFrames = resizeSpriteArray(downFrames, renderWidth, renderHeight);
        leftFrames = resizeSpriteArray(leftFrames, renderWidth, renderHeight);
        rightFrames = resizeSpriteArray(rightFrames, renderWidth, renderHeight);
        damagedFrames = resizeSpriteArray(damagedFrames, renderWidth, renderHeight);
    }

    @Override
    /**
     * Makes the dummy approach the player only while the player is far enough away.
     */
    public void setAction() {
        updatePathState(18, 22);

        if (!onPath || getTileDistanceToPlayer() <= 1) {
            direction = "idle";
            return;
        }

        int goalCol = (gp.getPlayer().worldX + gp.getPlayer().solidArea.x) / Constants.tileSize;
        int goalRow = (gp.getPlayer().worldY + gp.getPlayer().solidArea.y) / Constants.tileSize;
        searchPath(goalCol, goalRow);
    }

    @Override
    protected void startEnemyAttack(AttackType type, String direction) {
        // Tutorial dummies never attack.
    }
}
