
package entity;

import audio.AudioPlayer;
import engine.GamePanel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import systems.CollisionManager;
import systems.KeyHandler;
import util.Constants;
import util.ResourceCache;
import util.UtilityTool;

/*
 OWNER: Jamin

 PURPOSE:
 - Main controllable character

 TASKS:
 1. Add movement (WASD)
 2. Add speed variable
 3. Add attack() method
 4. Add animation states (idle, move, attack)

 OPTIONAL:
 - Add health system
*/

/**
 * Player-controlled entity.
 * Handles input-driven movement, attack timing, animation state, and player rendering.
 */
public class Player extends Entity {

    // Back-references the player needs to read input and query the world/camera.
    GamePanel gp;
    KeyHandler keyH;

    // Fixed on-screen anchor point. The camera moves the world around the player most of the time.
    public final int screenX;
    public final int screenY;

    // Separate counters per animation set keep the frame order simple and independent.
    private int idleCounter = 0;
    private int downCounter = 0;
    private int upCounter = 0;
    private int leftCounter = 0;
    private int rightCounter = 0;

    // Helps detect direction/state changes so animations can restart cleanly.
    private String previousDirection = "idle";
    private int movementAnimationSpeed = 8;
    private int idleAnimationSpeed = 13; // Slower for idle

    // Prevents one long key hold from repeatedly retriggering an attack.
    private boolean attackedPressed = false;
    private boolean firePressed = false;
    private boolean dashPressed = false;

    // Attack timing/state.
    private int attackCounter = 0;
    private final int attackDuration = 18;
    private boolean attackActive = false;
    private String attackDirection = "right"; // Store the direction of the current attack

    // Dash state
    private boolean dashing = false;
    private int dashCounter = 0;
    private final int dashDuration = 12;
    private final int dashDistanceTiles = 5;
    private float dashPrevProgress = 0f;
    private String dashDirection = "right";

    // Health system
    private int hp = 10;
    private int maxHp = 10;
    private int invincibilityFrames = 0;
    private final int invincibilityDuration = 120; // 2 seconds at 60 FPS

    // Audio playing manager
    private AudioPlayer audioPlayer = AudioPlayer.getInstance();

    /**
     * Creates the player and loads all animation frames up front.
     */
    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;

        screenX = (Constants.screenWidth/2) - (Constants.tileSize/2);
        screenY = (Constants.screenHeight/2) - (Constants.tileSize/2);

        // Slightly smaller than the sprite so the player does not feel like they collide on transparent pixels.
        solidArea = new Rectangle(8, 16, 30, 32);

        setDefaultValues();
        getPlayerImages();
    }

    /**
     * Resets the player to default movement values.
     */
    public void setDefaultValues() {
        // Spawn the player at the screen anchor. Because the camera starts there too,
        // this feels like spawning at the center of the view.
        worldX = screenX;
        worldY = screenY;
        speed = 3;
        direction = "idle";
        maxHp = 10;
        hp = maxHp;
    }

    /**
     * Sets the spawn point chosen by the current level definition.
     */
    public void setLevelStartPosition(int x, int y) {
        worldX = x;
        worldY = y;

        // redundancy for safety
        speed = 3;
        direction = "idle";
        maxHp = 10;
        hp = maxHp;
        invincibilityFrames = 0;
    }

    /**
     * Checks whether the player can move to a future position without hitting walls or enemies.
     */
    private boolean canOccupyPosition(int nextX, int nextY) {
        Rectangle futureSolidArea = CollisionManager.getWorldSolidArea(this, nextX, nextY);
        return !CollisionManager.willCollideWithSolidTile(gp.getTileManager(), futureSolidArea)
            && !CollisionManager.willCollideWithAnyEnemy(futureSolidArea, gp.getEnemies(), null);
    }

    /**
     * Loads and scales the player's sprite sheets from disk.
     */
    public void getPlayerImages() {
        // Allocate frame arrays based on how many sprite images exist per state.
        idle = new BufferedImage[7];
        down = new BufferedImage[4];
        up = new BufferedImage[6];
        left = new BufferedImage[6];
        right = new BufferedImage[6];

        // Load and resize every animation frame once at startup.
        for(int i = 0; i < 7; i++) {
            idle[i] = ResourceCache.getImage("player_idle_" + i);
            idle[i] = UtilityTool.resizeImage(idle[i], Constants.tileSize, Constants.tileSize);
        }
        // down assets
        for(int i = 0; i < 4; i++) {
            down[i] = ResourceCache.getImage("player_down_" + i);
            down[i] = UtilityTool.resizeImage(down[i], Constants.tileSize, Constants.tileSize);
        }
        // up assets
        for(int i = 0; i < 6; i++) {
            up[i] = ResourceCache.getImage("player_up_" + i);
            up[i] = UtilityTool.resizeImage(up[i], Constants.tileSize, Constants.tileSize);
        }
        // left assets
        for(int i = 0; i < 6; i++) {
            left[i] = ResourceCache.getImage("player_left_" + i);
            left[i] = UtilityTool.resizeImage(left[i], Constants.tileSize, Constants.tileSize);
        }
        // right assets
        for(int i = 0; i < 6; i++) {
            right[i] = ResourceCache.getImage("player_right_" + i);
            right[i] = UtilityTool.resizeImage(right[i], Constants.tileSize, Constants.tileSize);
        }
    }

    /**
     * One frame of player input, movement, attack, and animation logic.
     */
    public void update(){
        // Every frame, cooldown timers tick down first.
        updateCooldowns();

        // Reset held-button state flags when the keys are released.
        if (!keyH.isActionPressed(KeyHandler.Action.FIRE)) {
            firePressed = false;
        }
        if (!keyH.isActionPressed(KeyHandler.Action.DASH)) {
            dashPressed = false;
        }

        // Update invincibility frames
        if (invincibilityFrames > 0) {
            invincibilityFrames--;
        }

        // Once the cooldown is done, a fresh attack input is allowed again.
        if (!isOnCooldown("Player_attack")) {
            attackedPressed = false;
        }

        // If the visible state changed, restart the animation counters so the new state begins from frame 0.
        if (!direction.equals(previousDirection)) {
            idleCounter = 0;
            upCounter = 0;
            downCounter = 0;
            leftCounter = 0;
            rightCounter = 0;
            spriteCounter = 0; 
        }

        previousDirection = direction;
        String oldDirection = direction;

        tryStartAttack();
        updateAttackWindow();

        // Ability input: projectile fire and dash.
        if (keyH.isActionPressed(KeyHandler.Action.FIRE) && !firePressed && !isOnCooldown("Player_fire")) {
            firePressed = true;
            startCooldown("Player_fire", 40);
            String fireDirection = facingDirection;
            if (keyH.isActionPressed(KeyHandler.Action.MOVE_UP)) {
                fireDirection = "up";
            } else if (keyH.isActionPressed(KeyHandler.Action.MOVE_DOWN)) {
                fireDirection = "down";
            } else if (keyH.isActionPressed(KeyHandler.Action.MOVE_LEFT)) {
                fireDirection = "left";
            } else if (keyH.isActionPressed(KeyHandler.Action.MOVE_RIGHT)) {
                fireDirection = "right";
            }

            int projectileSize = Constants.tileSize - 12;
            int startX = worldX + (Constants.tileSize / 2) - (projectileSize / 2);
            int startY = worldY + (Constants.tileSize / 2) - (projectileSize / 2);
            Projectile projectile = new Projectile(
                gp,
                Projectile.OwnerType.PLAYER,
                fireDirection,
                startX,
                startY,
                1,
                6,
                5 * Constants.tileSize,
                projectileSize,
                projectileSize
            );
            gp.spawnProjectile(projectile);
            audioPlayer.playSound("player bullet fx");
        }

        if (!dashing && keyH.isActionPressed(KeyHandler.Action.DASH) && !dashPressed && !isOnCooldown("Player_dash")) {
            dashPressed = true;
            startCooldown("Player_dash", 30);
            dashing = true;
            dashCounter = 0;
            dashPrevProgress = 0f;
            dashDirection = facingDirection;
            audioPlayer.playSound("player dash fx");
        }

        // Handle dash motion first if dashing.
        if (dashing) {
            direction = dashDirection;
            facingDirection = dashDirection;
            float nextProgress = Math.min(1f, (dashCounter + 1) / (float) dashDuration);
            float eased = nextProgress * nextProgress * (3 - 2 * nextProgress);
            float previous = dashPrevProgress;
            float totalDistance = dashDistanceTiles * Constants.tileSize;
            float delta = totalDistance * eased - totalDistance * previous;
            dashPrevProgress = eased;
            dashCounter++;

            int nextX = worldX;
            int nextY = worldY;
            int move = Math.round(delta);
            switch (dashDirection) {
                case "up": nextY -= move; break;
                case "down": nextY += move; break;
                case "left": nextX -= move; break;
                case "right": nextX += move; break;
            }

            if (canOccupyPosition(nextX, nextY)) {
                worldX = nextX;
                worldY = nextY;
            } else {
                dashing = false;
            }

            if (dashCounter >= dashDuration) {
                dashing = false;
            }

            advanceDirectionalAnimation();
        } else if(keyH.isActionPressed(KeyHandler.Action.MOVE_UP) || keyH.isActionPressed(KeyHandler.Action.MOVE_RIGHT) ||
                keyH.isActionPressed(KeyHandler.Action.MOVE_LEFT) || keyH.isActionPressed(KeyHandler.Action.MOVE_DOWN)
        ) {

            if(keyH.isActionPressed(KeyHandler.Action.MOVE_UP)) {
                direction = "up";
            } else if (keyH.isActionPressed(KeyHandler.Action.MOVE_LEFT)) {
                direction = "left";
            } else if (keyH.isActionPressed(KeyHandler.Action.MOVE_RIGHT)) {
                direction = "right";
            } else if (keyH.isActionPressed(KeyHandler.Action.MOVE_DOWN)) {
                direction = "down";
            }

            if (!oldDirection.equals(direction)) {
                spriteCounter = movementAnimationSpeed; // Makes the first frame swap happen without delay.
            }

            // Remember the last non-idle direction for normal attacks.
            facingDirection = direction;
            int nextX = worldX;
            int nextY = worldY;
            switch (direction) {
                case "up": nextY -= speed; break;
                case "right": nextX += speed; break;
                case "left": nextX -= speed; break;
                case "down": nextY += speed; break;
            }

            // Collision is checked against the next predicted position before movement is committed.
            if (canOccupyPosition(nextX, nextY)) {
                worldX = nextX;
                worldY = nextY;
            }

            advanceDirectionalAnimation();
        } else {
            // No movement keys pressed, so fall back to the idle animation loop.
            direction = "idle";

            if (!oldDirection.equals("idle")) {
                spriteCounter = idleAnimationSpeed; // Trigger idle animation immediately
            }

            advanceIdleAnimation();
        }

    }

    /**
     * Advances the movement animation that matches the player's current direction.
     */
    private void advanceDirectionalAnimation() {
        spriteCounter++;
        if (spriteCounter <= movementAnimationSpeed) {
            return;
        }

        if (direction.equals("up")) {
            upCounter = nextFrame(upCounter, up);
        } else if (direction.equals("down")) {
            downCounter = nextFrame(downCounter, down);
        } else if (direction.equals("left")) {
            leftCounter = nextFrame(leftCounter, left);
        } else if (direction.equals("right")) {
            rightCounter = nextFrame(rightCounter, right);
        }

        spriteCounter = 0;
    }

    /**
     * Advances the idle animation while the player is standing still.
     */
    private void advanceIdleAnimation() {
        spriteCounter++;
        if (spriteCounter <= idleAnimationSpeed) {
            return;
        }

        idleCounter = nextFrame(idleCounter, idle);
        spriteCounter = 0;
    }

    /**
     * Moves to the next sprite frame and wraps back to zero at the end of the array.
     */
    private int nextFrame(int currentFrame, BufferedImage[] frames) {
        if (frames == null || frames.length == 0) {
            return 0;
        }
        return (currentFrame + 1) % frames.length;
    }

    /**
     * Starts a melee attack when the attack key is newly pressed and the cooldown allows it.
     */
    private void tryStartAttack() {
        if (!keyH.isActionPressed(KeyHandler.Action.ATTACK) || attackedPressed || isOnCooldown("Player_attack")) {
            return;
        }

        if (keyH.isActionPressed(KeyHandler.Action.MOVE_UP)) {
            beginAttack(AttackType.FORWARD, "up");
        } else if (keyH.isActionPressed(KeyHandler.Action.MOVE_DOWN)) {
            beginAttack(AttackType.DOWN, "down");
        } else if (keyH.isActionPressed(KeyHandler.Action.MOVE_LEFT)) {
            beginAttack(AttackType.SIDE, "left");
        } else if (keyH.isActionPressed(KeyHandler.Action.MOVE_RIGHT)) {
            beginAttack(AttackType.SIDE, "right");
        } else {
            beginAttack(AttackType.NORMAL, facingDirection);
        }
    }

    /**
     * Opens the short attack window and stores what shape/direction the hitbox should use.
     */
    private void beginAttack(AttackType requestedAttack, String requestedAttackDirection) {
        attackType = requestedAttack;
        attackDirection = requestedAttackDirection;
        attackCounter = 0;
        attackActive = true;
        attackedPressed = true;
        startCooldown("Player_attack", 50);
        audioPlayer.playSound("player melee fx");
    }

    /**
     * Keeps the attack hitbox active for a fixed number of frames, then clears it.
     */
    private void updateAttackWindow() {
        if (!attackActive) {
            attackHitbox.setBounds(0, 0, 0, 0);
            return;
        }

        attackCounter++;
        attackHitbox = calculateAttackHitbox(attackDirection);

        if (attackCounter > attackDuration) {
            attackCounter = 0;
            attackType = AttackType.NONE;
            attackActive = false;
            attackHitbox.setBounds(0, 0, 0, 0);
        }
    }

    @Override
    /**
     * Uses the locked attack direction instead of live movement direction while an attack is active.
     */
    protected Rectangle calculateAttackHitbox() {
        return calculateAttackHitbox(attackDirection);
    }

    /**
     * Exposes whether the attack hitbox should currently be considered live.
     */
    public boolean isAttackActive() {
        return attackActive;
    }

    /**
     * Returns the current attack box in world coordinates.
     */
    public Rectangle getAttackHitbox() {
        return attackHitbox;
    }

    /**
     * Tells CombatResolver whether dash collision damage should be checked this frame.
     */
    public boolean isDashing() {
        return dashing;
    }

    /**
     * Utility setter used by panel-level resets.
     */
    public void setDirection(String direction) {
        this.direction = direction;
    }

    /**
     * Apply damage to the player if not currently invincible.
     */
    public void takeDamage(int damage) {
        if (invincibilityFrames <= 0) {
            hp -= damage;
            invincibilityFrames = invincibilityDuration;
            if (hp < 0) {
                hp = 0;
            }
        }
    }

    /**
     * Get the player's current health.
     */
    public int getHp() {
        return hp;
    }

    /**
     * Get the player's maximum health.
     */
    public int getMaxHp() {
        return maxHp;
    }

    /**
     * Check if the player is currently invincible.
     */
    public boolean isInvincible() {
        return invincibilityFrames > 0;
    }

    /**
     * Draws the player at the camera-relative anchor point and optionally shows the debug attack box.
     */
    public void draw(Graphics2D g2){
        // Pick the current sprite frame based on the active state and animation counters.

        BufferedImage image = null;

        switch (direction) {
            case "idle":
                image = idle[idleCounter];
                break;
            case "up":
                image = up[upCounter];
                break;
            case "up1":
                image = up[upCounter];
                break;
            case "attack":
                image = idle[idleCounter];
                break;
            case "down":
                image = down[downCounter];
                break;
            case "left":
                image = left[leftCounter];
                break;
            case "right":
                image = right[rightCounter];
                break;
        }

        // The player is drawn using camera-relative screen coordinates, not raw world coordinates.
        drawGlow(g2, gp.getCameraX(), gp.getCameraY());
        g2.drawImage(image, gp.getCameraX(), gp.getCameraY(), null);

        // Flash effect when invincible
        if (isInvincible() && (invincibilityFrames / 10) % 2 == 0) {
            // Draw semi-transparent white flash
            g2.setColor(new Color(255, 255, 255, 100));
            g2.fillRect(gp.getCameraX(), gp.getCameraY(), Constants.tileSize, Constants.tileSize);
        }

        // Debug/feedback overlay to show the active attack area.
        if (attackActive && attackHitbox.width > 0 && attackHitbox.height > 0) {
            int screenX = attackHitbox.x - gp.getCameraWorldX();
            int screenY = attackHitbox.y - gp.getCameraWorldY();
            g2.setColor(new Color(0, 120, 255, 150));
            g2.fillRect(screenX, screenY, attackHitbox.width, attackHitbox.height);
            g2.setColor(new Color(0, 80, 220));
            g2.drawRect(screenX, screenY, attackHitbox.width, attackHitbox.height);
        }

        //Initial character
        // g2.setColor(Color.WHITE);
        // g2.fillRect(worldX, worldY, Constants.tileSize, Constants.tileSize);
    }

    /**
     * Draws a soft glow behind the player so the character stays visible over busy tile art.
     */
    private void drawGlow(Graphics2D g2, int screenX, int screenY) {
        java.awt.Composite oldComposite = g2.getComposite();
        int centerX = screenX + Constants.tileSize / 2;
        int centerY = screenY + Constants.tileSize / 2;

        g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.22f));
        g2.setColor(new Color(80, 190, 255));
        g2.fillOval(centerX - 34, centerY - 34, 68, 68);

        g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.18f));
        g2.setColor(new Color(180, 245, 255));
        g2.fillOval(centerX - 24, centerY - 24, 48, 48);

        g2.setComposite(oldComposite);
    }
}
