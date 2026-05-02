package entity;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;


import engine.GamePanel;
import util.Constants;

/**
 * Trojan enemy - stationary spawner that produces other enemies.
 * Uses a state machine to control spawning behavior.
 * Subclass of Enemy (doesn't need pathfinding).
 */
public class Trojan extends Enemy {

    // State machine
    private TrojanState currentState = TrojanState.IDLE;
    private int stateTimer = 0;

    // Spawning configuration
    private int maxActiveChildren = 3;
    private int totalSpawnLimit = 10; // Optional limit
    private int currentSpawnCount = 0;

    // State durations (in frames, assuming 60 FPS)
    private final int IDLE_DURATION = 300;        // 5 seconds
    private final int ACTIVATING_DURATION = 120;  // 2 seconds
    private final int PRODUCING_DURATION = 180;   // 3 seconds
    private final int COOLDOWN_DURATION = 240;    // 4 seconds

    // Spawn timing within PRODUCING state
    private final int SPAWN_TIME = 60; // Spawn at 1 second into producing

    // Sprite arrays for different states
    private BufferedImage[] activatingFrames;
    private BufferedImage[] producingFrames;
    private BufferedImage[] cooldownFrames;
    private BufferedImage[] recoveryFrames; // For emerge animations

    // Track spawned enemies (optional, for limiting active count)
    private List<Entity> spawnedChildren = new ArrayList<>();

    /**
     * Creates a stationary spawner enemy with its own state machine and animation sets.
     */
    public Trojan(GamePanel gp) {
        super(gp);
        setDefaultValues();
        loadSprites();
    }

    @Override
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
    protected void loadSprites() {
        String basePath = "res/EnemyAssets/trojan/";

        // Load state-specific sprite arrays
        idleFrames = loadSpriteArray(basePath, "idle", 12);
        activatingFrames = loadSpriteArray(basePath, "recovery", 35); // Use recovery for activating
        producingFrames = loadSpriteArray(basePath, "producing", 21);
        cooldownFrames = loadSpriteArray(basePath, "cooldown", 7);

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
                if (stateTimer == SPAWN_TIME) {
                    spawnEnemy();
                }
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
        spriteNum = 0; // Reset animation
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
        Entity newEnemy;
        int spawnType = currentSpawnCount % 3; // Cycle through types

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

        // Position the enemy near the Trojan.
        // BUG NOTE: children currently spawn essentially on the same exit point with no spacing logic.
        // Combined with missing enemy-vs-enemy collision, that makes spawned enemies clump together quickly.
        // If this gets fixed later, check this spawn placement plus EnemyPath.searchPath/checkCollision.
        newEnemy.worldX = worldX + (Constants.tileSize / 2) - (Constants.tileSize / 2);
        newEnemy.worldY = worldY + Constants.tileSize; // Spawn below

        // Add to spawned children list (for tracking)
        spawnedChildren.add(newEnemy);

        // Register the new enemy with the main game panel so it will update and render.
        if (newEnemy instanceof Enemy) {
            gp.addEnemy((Enemy) newEnemy);
        }

        currentSpawnCount++;
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
        spriteCounter++;
        if (spriteCounter > 12) {
            spriteNum++;
            spriteCounter = 0;

            BufferedImage[] currentFrames = getCurrentFrameArray();
            // BUG NOTE: the Trojan uses fixed state durations and independently looping animations.
            // If the sequence looks off, the later fix most likely belongs here and in `updateStateMachine()`:
            // map animation progress to the state's elapsed fraction instead of looping every 12 ticks.
            if (currentFrames != null && spriteNum >= currentFrames.length) {
                spriteNum = 0;
            }
        }
    }

    @Override
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
        // Trojans don't move, but could change state or behavior
        // For now, just reset state timer
        stateTimer = 0;
    }

    @Override
    public void takeDamage(int amount) {
        // BUG NOTE: this method is ready for damage, but the current gameplay loop never reliably reaches it
        // because player attack-vs-enemy collision registration is not wired up in world space yet.
        // The later fix likely starts in Player.update()/getAttackHitbox(), GamePanel.updateEnemies(), and CollisionManager.
        if (!invincible) {
            hp -= amount;
            invincible = true;
            damageReaction();

            if (hp <= 0) {
                dying = true;
                alive = false;
                changeState(TrojanState.DESTROYED);
            }
        }
    }

    // Getters/setters are currently placeholders for balancing tools or editor/debug UI.
    // They are not heavily used in the current project, but they were added to keep later expansion easier.
    public int getMaxActiveChildren() { return maxActiveChildren; }
    public void setMaxActiveChildren(int max) { this.maxActiveChildren = max; }

    public int getTotalSpawnLimit() { return totalSpawnLimit; }
    public void setTotalSpawnLimit(int limit) { this.totalSpawnLimit = limit; }

    public TrojanState getCurrentState() { return currentState; }
    public int getCurrentSpawnCount() { return currentSpawnCount; }
}
