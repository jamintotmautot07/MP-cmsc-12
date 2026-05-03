package entity;

import engine.GamePanel;
import util.Constants;

/**
 * CoreBoss enemy - final boss with custom size and simple boss behavior.
 */
/**
 * Large boss enemy with oversized rendering and simple placeholder behavior.
 */
public class CoreBoss extends Enemy {

    public CoreBoss(GamePanel gp) {
        super(gp);
        setDefaultValues();
        loadSprites();
    }

    @Override
    public void setDefaultValues() {
        super.setDefaultValues();
        speed = 0; // Mostly stationary
        hp = 15;
        damage = 2;
        renderWidth = Constants.tileSize * 6;
        renderHeight = Constants.tileSize * 6;
    }

    @Override
    protected void loadSprites() {
        idleFrames = loadCachedSpriteArray("boss", "idle", 6);

        upFrames = idleFrames;
        downFrames = idleFrames;
        leftFrames = idleFrames;
        rightFrames = idleFrames;
        damagedFrames = idleFrames;

        idleFrames = resizeSpriteArray(idleFrames, renderWidth, renderHeight);

        upFrames = idleFrames;
        downFrames = idleFrames;
        leftFrames = idleFrames;
        rightFrames = idleFrames;
        damagedFrames = idleFrames;
    }

    @Override
    public void setAction() {
        // Boss is mostly stationary but may shift phase for animation.
        actionLockCounter++;
        if (actionLockCounter >= 120) {
            actionLockCounter = 0;
            int i = random.nextInt(4);
            switch (i) {
                case 0: direction = "up"; break;
                case 1: direction = "down"; break;
                case 2: direction = "left"; break;
                case 3: direction = "right"; break;
            }
        }
    }

    @Override
    public void damageReaction() {
        // Boss should feel heavy when hit.
        actionLockCounter = 0;
        System.out.println("CoreBoss has been hit and roars.");
    }
}
