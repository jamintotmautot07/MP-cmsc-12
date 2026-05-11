
package entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import javax.imageio.ImageIO;

import engine.GamePanel;
import util.Constants;
import systems.CollisionManager;
import util.ResourceCache;

/*
 OWNER: Allan

 PURPOSE:
 - Base enemy behavior

 TASKS:
 1. Make enemy follow player
 2. Add speed
 3. Add simple AI
 4. Add attack system using general Entity methods

 OPTIONAL:
 - Different enemy types
*/

/**
 * Base implementation for simple enemies that wander, animate, render, and react to damage.
 * Subclasses mainly customize sprite loading, default stats, and decision-making in `setAction()`.
 */
public class Enemy extends Entity {

    protected GamePanel gp;
    protected Random random = new Random();

    protected int actionLockCounter = 0;
    protected boolean onPath = false;
    protected boolean alive = true;
    protected boolean dying = false;
    protected int hp = 3;
    protected int maxHp = 3;
    protected int damage = 1;

    protected boolean attackActive = false;
    protected int attackCounter = 0;
    protected int attackDuration = 18;
    protected String attackDirection = "down";

    private static final int PATH_REFRESH_FRAMES = 15;
    private static final int PATH_SEARCH_PADDING_TILES = 12;
    private static final int[][] PATH_DIRECTIONS = {
        {0, -1},
        {0, 1},
        {-1, 0},
        {1, 0}
    };
    private List<PathNode> currentPath = new ArrayList<>();
    private int pathRefreshCounter = 0;
    private int pathGoalCol = -1;
    private int pathGoalRow = -1;
    private final int[][] pathBestCost = new int[Constants.worldMaxRow][Constants.worldMaxCol];
    private final int[][] pathSeenSearch = new int[Constants.worldMaxRow][Constants.worldMaxCol];
    private final int[][] pathClosedSearch = new int[Constants.worldMaxRow][Constants.worldMaxCol];
    private int pathSearchId = 0;

    // Sprite arrays for different states
    protected BufferedImage[] idleFrames;
    protected BufferedImage[] upFrames;
    protected BufferedImage[] downFrames;
    protected BufferedImage[] leftFrames;
    protected BufferedImage[] rightFrames;
    protected BufferedImage[] damagedFrames;

    /**
     * Initializes shared enemy collision bounds and default state.
     */
    public Enemy(GamePanel gp) {
        this.gp = gp;

        solidArea = new Rectangle();
        solidArea.x = 8;
        solidArea.y = 16;
        solidArea.width = 32;
        solidArea.height = 24;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;

        setDefaultValues();
        loadSprites();
    }

    /**
     * Resets the enemy to its baseline combat and movement values.
     */
    public void setDefaultValues() {
        speed = 1;
        direction = "down";
        hp = 3;
        maxHp = 3;
        damage = 1;
        alive = true;
        dying = false;
        onPath = false;
        currentPath.clear();
        pathRefreshCounter = 0;
        pathGoalCol = -1;
        pathGoalRow = -1;
    }

    /**
     * Pulls a sequence of enemy animation frames from ResourceCache using the shared naming pattern.
     */
    protected BufferedImage[] loadCachedSpriteArray(String enemyKey, String state, int frameCount) {
        BufferedImage[] frames = new BufferedImage[frameCount];

        for (int i = 0; i < frameCount; i++) {
            frames[i] = ResourceCache.getImage("enemy_" + enemyKey + "_" + state + "_" + i);
        }

        return frames;
    }

    /**
     * Places an enemy at an exact pixel position in the world.
     */
    public void setStartPosition(int worldX, int worldY) {
        this.worldX = worldX;
        this.worldY = worldY;
    }

    /**
     * Places an enemy at a tile position by converting tile coordinates into pixels.
     */
    public void setStartTilePosition(int col, int row) {
        setStartPosition(col * Constants.tileSize, row * Constants.tileSize);
    }

    /**
     * Load enemy sprites. Subclasses should override this to load their specific assets.
     * This base implementation provides fallback behavior.
     */
    protected void loadSprites() {
        // Default implementation - subclasses should override
        // For now, create placeholder arrays
        idleFrames = new BufferedImage[1];
        upFrames = new BufferedImage[1];
        downFrames = new BufferedImage[1];
        leftFrames = new BufferedImage[1];
        rightFrames = new BufferedImage[1];
        damagedFrames = new BufferedImage[1];

        // Try to load a default sprite if available
        try {
            idleFrames[0] = ResourceCache.getImage("enemy_default");
            upFrames[0] = idleFrames[0];
            downFrames[0] = idleFrames[0];
            leftFrames[0] = idleFrames[0];
            rightFrames[0] = idleFrames[0];
            damagedFrames[0] = idleFrames[0];
        } catch (Exception e) {
            // No default sprite available
        }
    }

    /**
     * Safely load a sprite array from the given path pattern.
     */
    protected BufferedImage[] loadSpriteArray(String basePath, String state, int maxFrames) {
        BufferedImage[] frames = new BufferedImage[maxFrames];
        int loaded = 0;

        for (int i = 1; i <= maxFrames; i++) {
            try {
                String path = basePath + state + i + ".png";
                BufferedImage img = ImageIO.read(new File(path));
                if (img != null) {
                    frames[loaded] = img;
                    loaded++;
                } else {
                    break; // Stop if a frame is missing
                }
            } catch (Exception e) {
                break; // Stop on any error
            }
        }

        if (loaded == 0) {
            return null; // No frames loaded
        }

        // Trim array to actual loaded frames
        BufferedImage[] result = new BufferedImage[loaded];
        System.arraycopy(frames, 0, result, 0, loaded);
        return result;
    }

    /**
     * Default idle AI: randomly pick a direction every few seconds.
     * Pathfinding enemies override this with chase logic.
     */
    public void setAction() {
        actionLockCounter++;

        if (actionLockCounter >= 120) {
            int i = random.nextInt(100) + 1;

            if (i <= 25) {
                direction = "up";
            } else if (i <= 50) {
                direction = "down";
            } else if (i <= 75) {
                direction = "left";
            } else {
                direction = "right";
            }

            actionLockCounter = 0;
        }
    }

    /**
     * Returns this enemy's collision body in world coordinates.
     */
    protected Rectangle getWorldSolidArea() {
        return new Rectangle(
            worldX + solidArea.x,
            worldY + solidArea.y,
            solidArea.width,
            solidArea.height
        );
    }

    /**
     * Returns the player's collision body in world coordinates.
     */
    protected Rectangle getPlayerWorldSolidArea() {
        Player player = gp.getPlayer();
        return new Rectangle(
            player.worldX + player.solidArea.x,
            player.worldY + player.solidArea.y,
            player.solidArea.width,
            player.solidArea.height
        );
    }

    /**
     * Returns the enemy's horizontal center in world pixels.
     */
    protected int getCenterX() {
        return worldX + renderWidth / 2;
    }

    /**
     * Returns the enemy's vertical center in world pixels.
     */
    protected int getCenterY() {
        return worldY + renderHeight / 2;
    }

    /**
     * Returns the player's horizontal center in world pixels.
     */
    protected int getPlayerCenterX() {
        return gp.getPlayer().worldX + Constants.tileSize / 2;
    }

    /**
     * Returns the player's vertical center in world pixels.
     */
    protected int getPlayerCenterY() {
        return gp.getPlayer().worldY + Constants.tileSize / 2;
    }

    /**
     * Estimates distance to the player in tile units using Manhattan distance.
     */
    protected int getTileDistanceToPlayer() {
        int distanceX = Math.abs(getCenterX() - getPlayerCenterX());
        int distanceY = Math.abs(getCenterY() - getPlayerCenterY());
        return (distanceX + distanceY) / Constants.tileSize;
    }

    /**
     * Picks the main up/down/left/right direction that points toward the player.
     */
    protected String getCardinalDirectionTowardPlayer() {
        int distanceX = getPlayerCenterX() - getCenterX();
        int distanceY = getPlayerCenterY() - getCenterY();

        if (Math.abs(distanceX) > Math.abs(distanceY)) {
            return distanceX < 0 ? "left" : "right";
        }

        return distanceY < 0 ? "up" : "down";
    }

    /**
     * Picks an eight-direction aim string, used mainly for projectile firing.
     */
    protected String getEightWayDirectionTowardPlayer() {
        int distanceX = getPlayerCenterX() - getCenterX();
        int distanceY = getPlayerCenterY() - getCenterY();
        int deadZone = Constants.tileSize / 3;

        boolean horizontal = Math.abs(distanceX) > deadZone;
        boolean vertical = Math.abs(distanceY) > deadZone;

        if (horizontal && vertical) {
            if (distanceX < 0 && distanceY < 0) return "up-left";
            if (distanceX > 0 && distanceY < 0) return "up-right";
            if (distanceX < 0) return "down-left";
            return "down-right";
        }

        if (horizontal) {
            return distanceX < 0 ? "left" : "right";
        }

        return distanceY < 0 ? "up" : "down";
    }

    /**
     * A melee attack is allowed only when the attack box itself reaches the player.
     */
    protected boolean canHitPlayerWithMelee(String attackDirection) {
        Rectangle candidateHitbox = calculateAttackHitbox(attackDirection);
        return CollisionManager.rectanglesIntersect(candidateHitbox, getPlayerWorldSolidArea());
    }

    /**
     * Checks whether this enemy can step to a future position without hitting tiles, the player, or other enemies.
     */
    protected boolean canMoveTo(int nextX, int nextY) {
        Rectangle futureSolidArea = CollisionManager.getWorldSolidArea(this, nextX, nextY);
        return !CollisionManager.willCollideWithSolidTile(gp.getTileManager(), futureSolidArea)
            && !CollisionManager.willCollideWithEntity(futureSolidArea, gp.getPlayer())
            && !CollisionManager.willCollideWithAnyEnemy(futureSolidArea, gp.getEnemies(), this);
    }

    /**
     * Creates a projectile aimed toward the player's current position.
     */
    protected void fireProjectileAtPlayer(int damage, int projectileSpeed, int rangeTiles, int size) {
        String projectileDirection = getEightWayDirectionTowardPlayer();
        int startX = getCenterX() - size / 2;
        int startY = getCenterY() - size / 2;

        gp.spawnProjectile(new Projectile(
            gp,
            Projectile.OwnerType.ENEMY,
            projectileDirection,
            startX,
            startY,
            damage,
            projectileSpeed,
            rangeTiles * Constants.tileSize,
            size,
            size
        ));
    }

    /**
     * Turns player-following behavior on and off with hysteresis so enemies do not flicker between states.
     */
    protected void updatePathState(int startChaseTiles, int stopChaseTiles) {
        int tileDistance = getTileDistanceToPlayer();
        if (tileDistance < startChaseTiles) {
            onPath = true;
        } else if (tileDistance > stopChaseTiles) {
            onPath = false;
        }
    }

    /**
     * Chooses the next chase step using A* over the current tile map.
     */
    protected void searchPath(int goalCol, int goalRow) {
        int startCol = getPathCol();
        int startRow = getPathRow();

        if (!isInWorld(startCol, startRow) || !isInWorld(goalCol, goalRow)) {
            fallbackChase(goalCol, goalRow);
            return;
        }

        if (startCol == goalCol && startRow == goalRow) {
            direction = getCardinalDirectionTowardPlayer();
            return;
        }

        pruneReachedPathNodes(startCol, startRow);

        boolean targetMoved = goalCol != pathGoalCol || goalRow != pathGoalRow;
        boolean pathNeedsRefresh = targetMoved
            || currentPath.isEmpty()
            || pathRefreshCounter <= 0
            || !isNextPathStepAdjacent(startCol, startRow);

        if (pathNeedsRefresh) {
            currentPath = findAStarPath(startCol, startRow, goalCol, goalRow);
            pathRefreshCounter = PATH_REFRESH_FRAMES;
            pathGoalCol = goalCol;
            pathGoalRow = goalRow;
        } else {
            pathRefreshCounter--;
        }

        pruneReachedPathNodes(startCol, startRow);

        if (currentPath.isEmpty()) {
            fallbackChase(goalCol, goalRow);
            return;
        }

        PathNode nextStep = currentPath.get(0);
        String nextDirection = getDirectionToTile(startCol, startRow, nextStep.col, nextStep.row);
        if (nextDirection == null) {
            fallbackChase(goalCol, goalRow);
            return;
        }

        direction = nextDirection;
    }

    /**
     * Converts the enemy's collision center into a tile column for pathfinding.
     */
    private int getPathCol() {
        return (worldX + solidArea.x + solidArea.width / 2) / Constants.tileSize;
    }

    /**
     * Converts the enemy's collision center into a tile row for pathfinding.
     */
    private int getPathRow() {
        return (worldY + solidArea.y + solidArea.height / 2) / Constants.tileSize;
    }

    /**
     * Checks whether a tile coordinate is inside the configured world grid.
     */
    private boolean isInWorld(int col, int row) {
        return col >= 0 && col < Constants.worldMaxCol
            && row >= 0 && row < Constants.worldMaxRow;
    }

    /**
     * Checks if pathfinding is allowed to route through the requested tile.
     */
    private boolean canPathThrough(int col, int row) {
        if (!isInWorld(col, row)) {
            return false;
        }

        if (renderWidth <= Constants.tileSize && renderHeight <= Constants.tileSize) {
            return !gp.getTileManager().isTileSolid(row, col);
        }

        Rectangle candidateArea = CollisionManager.getWorldSolidArea(
            this,
            col * Constants.tileSize,
            row * Constants.tileSize
        );
        return !CollisionManager.willCollideWithSolidTile(gp.getTileManager(), candidateArea);
    }

    /**
     * Runs a bounded A* search between two tile positions.
     * The search is padded around the enemy/player area so it stays cheap enough for repeated gameplay use.
     */
    private List<PathNode> findAStarPath(int startCol, int startRow, int goalCol, int goalRow) {
        if (!canPathThrough(startCol, startRow) || !canPathThrough(goalCol, goalRow)) {
            return Collections.emptyList();
        }

        int searchId = nextPathSearchId();
        int minCol = Math.max(0, Math.min(startCol, goalCol) - PATH_SEARCH_PADDING_TILES);
        int maxCol = Math.min(Constants.worldMaxCol - 1, Math.max(startCol, goalCol) + PATH_SEARCH_PADDING_TILES);
        int minRow = Math.max(0, Math.min(startRow, goalRow) - PATH_SEARCH_PADDING_TILES);
        int maxRow = Math.min(Constants.worldMaxRow - 1, Math.max(startRow, goalRow) + PATH_SEARCH_PADDING_TILES);

        PriorityQueue<PathNode> open = new PriorityQueue<>(
            Comparator.comparingInt(PathNode::getFCost)
                .thenComparingInt(node -> node.hCost)
        );

        PathNode start = new PathNode(
            startCol,
            startRow,
            0,
            getManhattanDistance(startCol, startRow, goalCol, goalRow),
            null
        );
        setBestPathCost(startCol, startRow, 0, searchId);
        open.add(start);

        while (!open.isEmpty()) {
            PathNode current = open.poll();
            if (isClosedPathNode(current.col, current.row, searchId)) {
                continue;
            }

            closePathNode(current.col, current.row, searchId);
            if (current.col == goalCol && current.row == goalRow) {
                return buildPath(current);
            }

            for (int[] offset : PATH_DIRECTIONS) {
                addNeighbor(
                    open,
                    current,
                    current.col + offset[0],
                    current.row + offset[1],
                    goalCol,
                    goalRow,
                    minCol,
                    maxCol,
                    minRow,
                    maxRow,
                    searchId
                );
            }
        }

        return Collections.emptyList();
    }

    /**
     * Evaluates one neighboring tile during A* and queues it if it improves the known path cost.
     */
    private void addNeighbor(
        PriorityQueue<PathNode> open,
        PathNode current,
        int col,
        int row,
        int goalCol,
        int goalRow,
        int minCol,
        int maxCol,
        int minRow,
        int maxRow,
        int searchId
    ) {
        if (col < minCol || col > maxCol || row < minRow || row > maxRow) {
            return;
        }

        if (!canPathThrough(col, row) || isClosedPathNode(col, row, searchId)) {
            return;
        }

        int candidateCost = current.gCost + 1;
        if (candidateCost >= getBestPathCost(col, row, searchId)) {
            return;
        }

        setBestPathCost(col, row, candidateCost, searchId);
        open.add(new PathNode(
            col,
            row,
            candidateCost,
            getManhattanDistance(col, row, goalCol, goalRow),
            current
        ));
    }

    /**
     * Produces a fresh search id so path arrays can be reused without clearing the whole grid every time.
     */
    private int nextPathSearchId() {
        pathSearchId++;
        if (pathSearchId == Integer.MAX_VALUE) {
            for (int row = 0; row < Constants.worldMaxRow; row++) {
                Arrays.fill(pathSeenSearch[row], 0);
                Arrays.fill(pathClosedSearch[row], 0);
            }
            pathSearchId = 1;
        }
        return pathSearchId;
    }

    /**
     * Reads the best known cost for a tile during the current A* search.
     */
    private int getBestPathCost(int col, int row, int searchId) {
        if (pathSeenSearch[row][col] != searchId) {
            return Integer.MAX_VALUE;
        }
        return pathBestCost[row][col];
    }

    /**
     * Stores the best known cost for a tile during the current A* search.
     */
    private void setBestPathCost(int col, int row, int cost, int searchId) {
        pathSeenSearch[row][col] = searchId;
        pathBestCost[row][col] = cost;
    }

    /**
     * Checks whether A* already finished processing this tile in the current search.
     */
    private boolean isClosedPathNode(int col, int row, int searchId) {
        return pathClosedSearch[row][col] == searchId;
    }

    /**
     * Marks a tile as fully processed for the current A* search.
     */
    private void closePathNode(int col, int row, int searchId) {
        pathClosedSearch[row][col] = searchId;
    }

    /**
     * A* heuristic: grid distance without diagonal movement.
     */
    private int getManhattanDistance(int startCol, int startRow, int goalCol, int goalRow) {
        return Math.abs(startCol - goalCol) + Math.abs(startRow - goalRow);
    }

    /**
     * Rebuilds the final path by following parent links from the goal back to the start.
     */
    private List<PathNode> buildPath(PathNode goal) {
        List<PathNode> path = new ArrayList<>();
        PathNode current = goal;

        while (current != null) {
            path.add(current);
            current = current.parent;
        }

        Collections.reverse(path);
        if (!path.isEmpty()) {
            path.remove(0);
        }
        return path;
    }

    /**
     * Removes path nodes that the enemy has already reached.
     */
    private void pruneReachedPathNodes(int currentCol, int currentRow) {
        while (!currentPath.isEmpty()) {
            PathNode nextStep = currentPath.get(0);
            if (nextStep.col != currentCol || nextStep.row != currentRow) {
                return;
            }
            currentPath.remove(0);
        }
    }

    /**
     * Verifies that the next cached path step is still next to the enemy.
     */
    private boolean isNextPathStepAdjacent(int currentCol, int currentRow) {
        if (currentPath.isEmpty()) {
            return false;
        }

        PathNode nextStep = currentPath.get(0);
        return getManhattanDistance(currentCol, currentRow, nextStep.col, nextStep.row) == 1;
    }

    /**
     * Converts a one-tile path step into a movement direction string.
     */
    private String getDirectionToTile(int currentCol, int currentRow, int nextCol, int nextRow) {
        if (nextCol < currentCol) return "left";
        if (nextCol > currentCol) return "right";
        if (nextRow < currentRow) return "up";
        if (nextRow > currentRow) return "down";
        return null;
    }

    private static class PathNode {
        private final int col;
        private final int row;
        private final int gCost;
        private final int hCost;
        private final PathNode parent;

        /**
         * Stores one tile considered by A* plus the link used to rebuild the final route.
         */
        private PathNode(int col, int row, int gCost, int hCost, PathNode parent) {
            this.col = col;
            this.row = row;
            this.gCost = gCost;
            this.hCost = hCost;
            this.parent = parent;
        }

        /**
         * Total A* priority cost: cost so far plus estimated remaining distance.
         */
        private int getFCost() {
            return gCost + hCost;
        }
    }

    /**
     * Simple chase routine that first tries the dominant axis, then falls back to the other axis if blocked.
     */
    protected void fallbackChase(int goalCol, int goalRow) {
        int startCol = (worldX + solidArea.x) / Constants.tileSize;
        int startRow = (worldY + solidArea.y) / Constants.tileSize;

        String primaryDirection;
        String fallbackDirection;
        if (Math.abs(startCol - goalCol) > Math.abs(startRow - goalRow)) {
            primaryDirection = startCol < goalCol ? "right" : "left";
            fallbackDirection = startRow < goalRow ? "down" : "up";
        } else {
            primaryDirection = startRow < goalRow ? "down" : "up";
            fallbackDirection = startCol < goalCol ? "right" : "left";
        }

        direction = primaryDirection;
        if (isBlockedInDirection(direction)) {
            direction = fallbackDirection;
            if (isBlockedInDirection(direction)) {
                onPath = false;
            }
        }
    }

    /**
     * Tests whether moving one step in a candidate direction would be blocked.
     */
    private boolean isBlockedInDirection(String candidateDirection) {
        int nextX = worldX;
        int nextY = worldY;

        switch (candidateDirection) {
            case "up": nextY -= speed; break;
            case "down": nextY += speed; break;
            case "left": nextX -= speed; break;
            case "right": nextX += speed; break;
            default: break;
        }

        return !canMoveTo(nextX, nextY);
    }

    /**
     * Runs one frame of enemy logic: choose an action, test collisions, move, animate, and update timers.
     */
    public void update() {
        if (!alive) return;

        setAction();

        collisionOn = false;
        int nextX = worldX;
        int nextY = worldY;
        if (!attackActive) {
            switch (direction) {
                case "up":    nextY -= speed; break;
                case "down":  nextY += speed; break;
                case "left":  nextX -= speed; break;
                case "right": nextX += speed; break;
            }
        }

        if (!attackActive && !canMoveTo(nextX, nextY)) {
            collisionOn = true;
        }

        if (!collisionOn && !attackActive) {
            worldX = nextX;
            worldY = nextY;
        }

        // Update animation
        updateAnimation();
        updateAttackState();

        if (invincible) {
            invincibleCounter++;
            if (invincibleCounter > 40) {
                invincible = false;
                invincibleCounter = 0;
            }
        }

        updateCooldowns();
    }

    /**
     * Update animation counters. Subclasses can override for custom animation logic.
     */
    protected void updateAnimation() {
        spriteCounter++;
        if (spriteCounter > 12) {
            spriteNum++;
            spriteCounter = 0;

            // Reset to first frame if we've gone through all frames
            BufferedImage[] currentFrames = getCurrentFrameArray();
            if (currentFrames != null && spriteNum >= currentFrames.length) {
                spriteNum = 0;
            }
        }
    }

    /**
     * Starts an enemy melee attack or directional attack.
     */
    protected void startEnemyAttack(AttackType type, String direction) {
        if (!attackActive) {
            attackType = type;
            attackDirection = direction;
            attackActive = true;
            attackCounter = 0;
            attackHitbox.setBounds(0, 0, 0, 0);
        }
    }

    /**
     * Advances an enemy attack window and clears the hitbox when the attack expires.
     */
    protected void updateAttackState() {
        if (!attackActive) {
            return;
        }

        attackCounter++;
        attackHitbox = calculateAttackHitbox(attackDirection);

        if (attackCounter > attackDuration) {
            attackActive = false;
            attackCounter = 0;
            attackType = AttackType.NONE;
            attackHitbox.setBounds(0, 0, 0, 0);
        }
    }

    /**
     * Tells CombatResolver whether this enemy currently has a live melee hitbox.
     */
    public boolean isAttackActive() {
        return attackActive;
    }

    /**
     * Returns this enemy's current attack hitbox in world coordinates.
     */
    public Rectangle getAttackHitbox() {
        return attackHitbox;
    }

    /**
     * Returns how much damage this enemy deals on contact or melee hit.
     */
    public int getDamage() {
        return damage;
    }

    /**
     * Get the current frame array based on state and direction.
     */
    protected BufferedImage[] getCurrentFrameArray() {
        if (invincible && damagedFrames != null && damagedFrames.length > 0) {
            return damagedFrames;
        }

        switch (direction) {
            case "up": return upFrames;
            case "down": return downFrames;
            case "left": return leftFrames;
            case "right": return rightFrames;
            default: return idleFrames;
        }
    }

    /**
     * Render the enemy. Should be called from GamePanel's render method.
     */
    public void render(Graphics2D g2) {
        if (!alive) return;

        BufferedImage[] currentFrames = getCurrentFrameArray();
        BufferedImage image = null;

        if (currentFrames != null && currentFrames.length > 0) {
            image = currentFrames[Math.min(spriteNum, currentFrames.length - 1)];
        }

        if (image == null) {
            // Fallback: draw a colored rectangle
            g2.setColor(Color.RED);
            g2.fillRect(worldX - gp.getCameraWorldX(),
                       worldY - gp.getCameraWorldY(),
                       renderWidth, renderHeight);
            renderHealthBar(g2, worldX - gp.getCameraWorldX(), worldY - gp.getCameraWorldY());
            return;
        }

        // Draw with the same camera transform used by tiles and player hitboxes.
        int screenX = worldX - gp.getCameraWorldX();
        int screenY = worldY - gp.getCameraWorldY();

        if (invincible) {
            g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.5f));
        }

        g2.drawImage(image, screenX, screenY, renderWidth, renderHeight, null);
        renderHealthBar(g2, screenX, screenY);

        if (attackActive && attackHitbox.width > 0 && attackHitbox.height > 0) {
            int hitX = attackHitbox.x - gp.getCameraWorldX();
            int hitY = attackHitbox.y - gp.getCameraWorldY();
            g2.setColor(new Color(255, 0, 0, 120));
            g2.fillRect(hitX, hitY, attackHitbox.width, attackHitbox.height);
            g2.setColor(Color.RED);
            g2.drawRect(hitX, hitY, attackHitbox.width, attackHitbox.height);
        }

        if (invincible) {
            g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1f));
        }
    }

    /**
     * Draws a compact health bar above the enemy sprite.
     */
    protected void renderHealthBar(Graphics2D g2, int screenX, int screenY) {
        if (maxHp <= 0 || hp <= 0) {
            return;
        }

        int barWidth = Math.max(34, Math.min(renderWidth, Constants.tileSize * 3));
        int barHeight = 7;
        int barX = screenX + (renderWidth - barWidth) / 2;
        int barY = screenY - 14;
        int fillWidth = Math.max(0, Math.min(barWidth, (int) Math.round((hp / (double) maxHp) * barWidth)));

        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(barX - 4, barY - 4, barWidth + 8, barHeight + 8, 8, 8);
        g2.setColor(new Color(80, 10, 10, 230));
        g2.fillRoundRect(barX, barY, barWidth, barHeight, 6, 6);
        g2.setColor(new Color(45, 245, 105, 240));
        g2.fillRoundRect(barX, barY, fillWidth, barHeight, 6, 6);
        g2.setColor(new Color(190, 255, 205, 170));
        g2.drawLine(barX + 1, barY + 1, barX + Math.max(1, fillWidth - 2), barY + 1);
        g2.setColor(new Color(15, 15, 15, 240));
        g2.drawRoundRect(barX - 1, barY - 1, barWidth + 1, barHeight + 1, 7, 7);
    }

    /**
     * Compatibility wrapper for code that calls draw() instead of render().
     */
    public void draw(Graphics2D g2) {
        render(g2);
    }

    /**
     * Default knockback reaction: turn away from the player's location.
     */
    public void damageReaction() {
        actionLockCounter = 0;

        // simple reaction: move away or change direction
        Player player = gp.getPlayer();
        if (player.worldX < worldX) {
            direction = "right";
        } else if (player.worldX > worldX) {
            direction = "left";
        }

        if (player.worldY < worldY) {
            direction = "down";
        } else if (player.worldY > worldY) {
            direction = "up";
        }
    }

    /**
     * Applies incoming damage once per invincibility window.
     */
    public void takeDamage(int amount) {
        if (!invincible) {
            hp -= amount;
            invincible = true;
            damageReaction();

            if (hp <= 0) {
                dying = true;
                alive = false;
            }
        }
    }

    /**
     * Instantly defeats the enemy, used by debug level-clear logic.
     */
    public void defeat() {
        hp = 0;
        dying = true;
        alive = false;
    }

    /**
     * Small helper used by update/render loops to skip dead enemies.
     */
    public boolean isAlive() {
        return alive;
    }

    /**
     * Returns current enemy health.
     */
    public int getHp() {
        return hp;
    }

    /**
     * Returns maximum enemy health.
     */
    public int getMaxHp() {
        return maxHp;
    }

}
