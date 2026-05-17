package entity;

import engine.GamePanel;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import systems.CollisionManager;
import util.Constants;

/**
 * Trojan enemy - stationary spawner that produces other enemies.
 * Uses a state machine to control spawning behavior.
 * Subclass of Enemy (doesn't need pathfinding).
 */
public class Trojan extends Enemy {
    /*
     * NORMAL means the Trojan stays where the level placed it.
     * PROTECT_BOSS means it follows a fixed offset around the CoreBoss during the final level.
     */
    public enum TrojanMode {
        NORMAL,
        PROTECT_BOSS
    }

    // State machine
    private TrojanState currentState = TrojanState.IDLE;
    private int stateTimer = 0;

    // Spawning configuration
    private int maxActiveChildren = 3;
    private int totalSpawnLimit = 10; // Optional limit
    private int currentSpawnCount = 0;
    private int nextSpawnType = 0;

    // State durations (in frames, assuming 60 FPS)
    private final int IDLE_DURATION = 300;        // 5 seconds
    private final int ACTIVATING_DURATION = 120;  // 2 seconds
    private final int PRODUCING_DURATION = 180;   // 3 seconds
    private final int COOLDOWN_DURATION = 240;    // 4 seconds

    private boolean spawnAttemptedDuringProducing = false;

    // Sprite arrays for different states
    private BufferedImage[] activatingFrames;
    private BufferedImage[] producingFrames;
    private BufferedImage[] cooldownFrames;
    // private BufferedImage[] recoveryFrames; // For emerge animations

    // Track spawned enemies (optional, for limiting active count)
    private List<Entity> spawnedChildren = new ArrayList<>();

    private TrojanMode mode = TrojanMode.NORMAL;
    private CoreBoss bossTarget;
    private int guardOffsetX;
    private int guardOffsetY;

    /**
     * Creates a stationary spawner enemy with its own state machine and animation sets.
     */
    public Trojan(GamePanel gp) {
        super(gp);
        setDefaultValues();
        loadSprites();
    }

    @Override
    /**
     * Sets the Trojan up as a large stationary spawner instead of a moving attacker.
     */
    public void setDefaultValues() {
        super.setDefaultValues();
        speed = 0; // Stationary
        hp = 5;    // Tougher than basic enemies
        maxHp = 5;
        damage = 0; // Doesn't attack directly
        renderWidth = Constants.tileSize * 3;
        renderHeight = Constants.tileSize * 3;

        int padding = Constants.tileSize / 4;
        solidArea = new Rectangle(
            padding,
            padding,
            renderWidth - (padding * 2),
            renderHeight - (padding * 2)
        );
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }

    @Override
    /**
     * Loads the animation frames for every state-machine phase.
     */
    protected void loadSprites() {

        // Load state-specific sprite arrays
        idleFrames = loadCachedSpriteArray("trojan", "idle", 12);
        activatingFrames = loadCachedSpriteArray("trojan", "recovery", 35);
        producingFrames = loadCachedSpriteArray("trojan", "producing", 21);
        cooldownFrames = loadCachedSpriteArray("trojan", "cooldown", 7);

        // No directional movement sprites needed since stationary
        upFrames = idleFrames;
        downFrames = idleFrames;
        leftFrames = idleFrames;
        rightFrames = idleFrames;
        damagedFrames = idleFrames; // Use idle for damaged

        renderWidth = Constants.tileSize * 3;
        renderHeight = Constants.tileSize * 3;

        idleFrames = resizeSpriteArray(idleFrames, renderWidth, renderHeight);
        activatingFrames = resizeSpriteArray(activatingFrames, renderWidth, renderHeight);
        producingFrames = resizeSpriteArray(producingFrames, renderWidth, renderHeight);
        cooldownFrames = resizeSpriteArray(cooldownFrames, renderWidth, renderHeight);
        damagedFrames = resizeSpriteArray(damagedFrames, renderWidth, renderHeight);

        // Fallback
        if (idleFrames == null) {
            super.loadSprites();
        }
    }

    @Override
    /**
     * Intentionally does nothing because the Trojan's behavior is handled by updateStateMachine().
     */
    public void setAction() {
        // Stationary - no movement
        // State machine handles behavior
    }

    @Override
    /**
     * Trojan updates are state-machine driven because it does not move like the other enemies.
     */
    public void update() {
        if (!alive) return;

        updateGuardPosition();

        // Update state machine
        updateStateMachine();

        // Update animation
        updateAnimation();

        // Handle invincibility
        if (invincible) {
            invincibleCounter++;
            if (invincibleCounter > 40) {
                invincible = false;
                invincibleCounter = 0;
            }
        }

        updateCooldowns();

        // Clean up dead children (optional)
        cleanupDeadChildren();
    }

    /**
     * Update the state machine logic.
     */
    /**
     * Advances the Trojan through idle, activating, producing, and cooldown phases.
     */
    private void updateStateMachine() {
        stateTimer++;

        switch (currentState) {
            case IDLE:
                if (stateTimer >= IDLE_DURATION) {
                    changeState(TrojanState.ACTIVATING);
                }
                break;

            case ACTIVATING:
                if (stateTimer >= ACTIVATING_DURATION) {
                    changeState(TrojanState.PRODUCING);
                }
                break;

            case PRODUCING:
                if (stateTimer >= PRODUCING_DURATION) {
                    changeState(TrojanState.COOLDOWN);
                }
                break;

            case COOLDOWN:
                if (stateTimer >= COOLDOWN_DURATION) {
                    changeState(TrojanState.IDLE);
                }
                break;

            case DESTROYED:
                // Handle destruction animation if needed
                break;
        }
    }

    /**
     * Change to a new state and reset timer.
     */
    /**
     * Switches the active phase and rewinds animation playback for the new phase.
     */
    private void changeState(TrojanState newState) {
        currentState = newState;
        stateTimer = 0;
        spriteCounter = 0;
        spriteNum = 0;
        spawnAttemptedDuringProducing = false;
    }

    /**
     * Converts this Trojan into a boss guard that follows the boss using a fixed offset.
     */
    public void setProtectBossMode(CoreBoss bossTarget, int guardOffsetX, int guardOffsetY) {
        this.mode = TrojanMode.PROTECT_BOSS;
        this.bossTarget = bossTarget;
        this.guardOffsetX = guardOffsetX;
        this.guardOffsetY = guardOffsetY;
        this.maxActiveChildren = 2;
        this.totalSpawnLimit = 8;
        updateGuardPosition();
    }

    /**
     * Returns whether this Trojan is normal or attached to the boss.
     */
    public TrojanMode getMode() {
        return mode;
    }

    /**
     * Repositions boss-guard Trojans around the current boss position.
     */
    private void updateGuardPosition() {
        if (mode != TrojanMode.PROTECT_BOSS || bossTarget == null || !bossTarget.isAlive()) {
            return;
        }

        worldX = clamp(bossTarget.worldX + guardOffsetX, 0, Constants.maxWorldWidth - renderWidth);
        worldY = clamp(bossTarget.worldY + guardOffsetY, 0, Constants.maxWorldHeight - renderHeight);
    }

    /**
     * Keeps a guard Trojan inside the playable map bounds.
     */
    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Spawn an enemy during the PRODUCING state.
     */
    private void spawnEnemy() {
        // Check spawn limits
        if (currentSpawnCount >= totalSpawnLimit) {
            return; // Reached total limit
        }

        // Count active children
        int activeCount = 0;
        for (Entity child : spawnedChildren) {
            if (child instanceof Enemy && ((Enemy) child).alive) {
                activeCount++;
            }
        }

        if (activeCount >= maxActiveChildren) {
            return; // Too many active children
        }

        // Decide what to spawn (can be randomized or based on logic)
        Enemy newEnemy;
        int spawnType = nextSpawnType;
        nextSpawnType = (nextSpawnType + 1) % 3;

        switch (spawnType) {
            case 0:
                newEnemy = new Worm(gp);
                break;
            case 1:
                newEnemy = new VirusDrone(gp);
                break;
            default:
                newEnemy = new Worm(gp); // Default to worm
                break;
        }

        if (!placeAtRandomSpawnPosition(newEnemy)) {
            return;
        }

        if (gp.addEnemy(newEnemy)) {
            spawnedChildren.add(newEnemy);
            currentSpawnCount++;
        }
    }

    /**
     * Tries nearby positions around the Trojan until a valid spawn spot is found.
     */
    private boolean placeAtRandomSpawnPosition(Enemy enemy) {
        List<int[]> spawnPositions = new ArrayList<>();
        spawnPositions.add(new int[] {worldX + Constants.tileSize, worldY - Constants.tileSize}); // up
        spawnPositions.add(new int[] {worldX + Constants.tileSize, worldY + renderHeight}); // down
        spawnPositions.add(new int[] {worldX - Constants.tileSize, worldY + Constants.tileSize}); // left
        spawnPositions.add(new int[] {worldX + renderWidth, worldY + Constants.tileSize}); // right
        Collections.shuffle(spawnPositions, random);

        for (int[] position : spawnPositions) {
            if (canSpawnEnemyAt(enemy, position[0], position[1])) {
                enemy.setStartPosition(position[0], position[1]);
                return true;
            }
        }

        return false;
    }

    /**
     * Verifies that a spawned enemy would not appear outside the map or inside another blocking object.
     */
    private boolean canSpawnEnemyAt(Enemy enemy, int spawnX, int spawnY) {
        if (spawnX < 0 || spawnY < 0
            || spawnX + enemy.renderWidth > Constants.maxWorldWidth
            || spawnY + enemy.renderHeight > Constants.maxWorldHeight) {
            return false;
        }

        Rectangle futureSolidArea = CollisionManager.getWorldSolidArea(enemy, spawnX, spawnY);
        return !CollisionManager.willCollideWithSolidTile(gp.getTileManager(), futureSolidArea)
            && !CollisionManager.willCollideWithEntity(futureSolidArea, gp.getPlayer())
            && !CollisionManager.willCollideWithAnyEnemy(futureSolidArea, gp.getEnemies(), this);
    }

    /**
     * Clean up references to dead children.
     */
    private void cleanupDeadChildren() {
        spawnedChildren.removeIf(entity -> {
            if (entity instanceof Enemy) {
                return !((Enemy) entity).alive;
            }
            return false; // Keep non-enemy entities
        });
    }

    @Override
    protected void updateAnimation() {
        BufferedImage[] currentFrames = getCurrentFrameArray();
        if (currentFrames == null || currentFrames.length == 0) {
            spriteNum = 0;
            return;
        }

        if (currentState == TrojanState.IDLE) {
            updateLoopingAnimation(currentFrames);
            return;
        }

        int duration = getCurrentStateDuration();
        if (duration <= 0 || currentFrames.length == 1) {
            spriteNum = 0;
            return;
        }

        int clampedTimer = Math.max(0, Math.min(stateTimer, duration - 1));
        spriteNum = (int) ((long) clampedTimer * currentFrames.length / duration);
        if (spriteNum >= currentFrames.length) {
            spriteNum = currentFrames.length - 1;
        }

        if (currentState == TrojanState.PRODUCING
            && !spawnAttemptedDuringProducing
            && spriteNum >= getProducingSpawnFrame()) {
            spawnEnemy();
            spawnAttemptedDuringProducing = true;
        }
    }

    /**
     * Loops the idle animation at a fixed frame speed.
     */
    private void updateLoopingAnimation(BufferedImage[] currentFrames) {
        spriteCounter++;
        if (spriteCounter > 12) {
            spriteNum++;
            spriteCounter = 0;

            if (spriteNum >= currentFrames.length) {
                spriteNum = 0;
            }
        }
    }

    /**
     * Returns the configured duration for the current state-machine phase.
     */
    private int getCurrentStateDuration() {
        switch (currentState) {
            case ACTIVATING:
                return ACTIVATING_DURATION;
            case PRODUCING:
                return PRODUCING_DURATION;
            case COOLDOWN:
                return COOLDOWN_DURATION;
            case IDLE:
                return IDLE_DURATION;
            default:
                return 0;
        }
    }

    /**
     * Chooses the animation frame where the actual enemy spawn should happen.
     */
    private int getProducingSpawnFrame() {
        if (producingFrames == null || producingFrames.length == 0) {
            return 0;
        }
        return producingFrames.length / 2;
    }

    @Override
    /**
     * Picks the animation frames that match the Trojan's current state-machine phase.
     */
    protected BufferedImage[] getCurrentFrameArray() {
        if (invincible && damagedFrames != null && damagedFrames.length > 0) {
            return damagedFrames;
        }

        switch (currentState) {
            case IDLE:
                return idleFrames;
            case ACTIVATING:
                return activatingFrames;
            case PRODUCING:
                return producingFrames;
            case COOLDOWN:
                return cooldownFrames;
            case DESTROYED:
                return damagedFrames;
            default:
                return idleFrames;
        }
    }

    @Override
    /**
     * Minimal hit reaction for now because the Trojan does not move.
     */
    public void damageReaction() {
        // Trojans do not move; damage should not desync their current state animation.
    }

    @Override
    /**
     * Damages the Trojan and switches it into DESTROYED state when health reaches zero.
     */
    public void takeDamage(int amount) {
        if (!invincible) {
            hp -= amount;
            invincible = true;
            damageReaction();

            audioPlayer.playSound(getHitSoundKey());

            if (hp <= 0) {
                dying = true;
                alive = false;
                changeState(TrojanState.DESTROYED);
            }
        }
    }

    // Getters/setters are currently placeholders for balancing tools or editor/debug UI.
    // They are not heavily used in the current project, but they were added to keep later expansion easier.
    /**
     * Returns how many spawned children this Trojan may keep alive at once.
     */
    public int getMaxActiveChildren() { return maxActiveChildren; }

    /**
     * Changes the active-child cap for balancing or future editor tools.
     */
    public void setMaxActiveChildren(int max) { this.maxActiveChildren = max; }

    /**
     * Returns the lifetime spawn limit for this Trojan.
     */
    public int getTotalSpawnLimit() { return totalSpawnLimit; }

    /**
     * Changes the lifetime spawn limit for balancing or future editor tools.
     */
    public void setTotalSpawnLimit(int limit) { this.totalSpawnLimit = limit; }

    /**
     * Returns the current state-machine phase.
     */
    public TrojanState getCurrentState() { return currentState; }

    /**
     * Returns how many enemies this Trojan has successfully spawned so far.
     */
    public int getCurrentSpawnCount() { return currentSpawnCount; }
}
