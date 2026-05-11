package systems;

import java.awt.Rectangle;

import engine.GamePanel;
import entity.Enemy;
import entity.Laser;
import entity.Player;
import entity.Projectile;

/**
 * Resolves projectile, laser, melee, dash, and body-contact combat interactions for one gameplay tick.
 */
public class CombatResolver {
    private final GamePanel gamePanel;

    /**
     * Stores the active GamePanel so combat can read and mutate the current world lists.
     */
    public CombatResolver(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    /**
     * Runs every combat interaction once for the current frame.
     */
    public void resolve() {
        updateProjectiles();
        updateLasers();
        resolvePlayerMelee();
        resolveEnemyMelee();
        resolvePlayerDash();
        resolveEnemyBodyContact();
    }

    /**
     * Moves projectiles, removes expired ones, and applies projectile hits.
     */
    private void updateProjectiles() {
        for (Projectile projectile : gamePanel.getProjectiles()) {
            if (projectile == null) {
                gamePanel.getProjectiles().remove(projectile);
                continue;
            }

            projectile.update();

            if (!projectile.isAlive()) {
                gamePanel.getProjectiles().remove(projectile);
                continue;
            }

            boolean hit = projectile.getOwnerType() == Projectile.OwnerType.PLAYER
                ? hitEnemy(projectile)
                : hitPlayer(projectile);

            if (hit) {
                projectile.kill();
                gamePanel.getProjectiles().remove(projectile);
            }
        }
    }

    /**
     * Checks whether a player-owned projectile hits any living enemy.
     */
    private boolean hitEnemy(Projectile projectile) {
        for (Enemy enemy : gamePanel.getEnemies()) {
            if (enemy == null || !enemy.isAlive()) {
                continue;
            }
            if (CollisionManager.rectanglesIntersect(projectile.getBounds(), CollisionManager.getWorldSolidArea(enemy))) {
                enemy.takeDamage(projectile.getDamage());
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether an enemy-owned projectile hits the player.
     */
    private boolean hitPlayer(Projectile projectile) {
        if (CollisionManager.rectanglesIntersect(projectile.getBounds(), playerBounds())) {
            gamePanel.getPlayer().takeDamage(projectile.getDamage());
            return true;
        }
        return false;
    }

    /**
     * Updates beam lifetimes and applies enemy laser damage to the player.
     */
    private void updateLasers() {
        for (Laser laser : gamePanel.getLasers()) {
            if (laser == null) {
                gamePanel.getLasers().remove(laser);
                continue;
            }

            laser.update();

            if (!laser.isAlive()) {
                gamePanel.getLasers().remove(laser);
                continue;
            }

            if (laser.getOwnerType() == Laser.OwnerType.ENEMY
                && CollisionManager.rectanglesIntersect(laser.getBounds(), playerBounds())) {
                gamePanel.getPlayer().takeDamage(laser.getDamage());
            }
        }
    }

    /**
     * Applies the player's active melee hitbox to enemies.
     */
    private void resolvePlayerMelee() {
        Player player = gamePanel.getPlayer();
        if (!player.isAttackActive()) {
            return;
        }

        for (Enemy enemy : gamePanel.getEnemies()) {
            if (enemy == null || !enemy.isAlive()) {
                continue;
            }
            if (CollisionManager.rectanglesIntersect(player.getAttackHitbox(), CollisionManager.getWorldSolidArea(enemy))) {
                enemy.takeDamage(1);
            }
        }
    }

    /**
     * Applies every active enemy melee hitbox to the player.
     */
    private void resolveEnemyMelee() {
        Rectangle playerBounds = playerBounds();
        for (Enemy enemy : gamePanel.getEnemies()) {
            if (enemy == null || !enemy.isAlive()) {
                continue;
            }
            if (enemy.isAttackActive()
                && CollisionManager.rectanglesIntersect(enemy.getAttackHitbox(), playerBounds)) {
                gamePanel.getPlayer().takeDamage(enemy.getDamage());
            }
        }
    }

    /**
     * Lets the player's dash damage enemies it overlaps.
     */
    private void resolvePlayerDash() {
        Player player = gamePanel.getPlayer();
        if (!player.isDashing()) {
            return;
        }

        Rectangle dashBounds = CollisionManager.getWorldSolidArea(player);
        for (Enemy enemy : gamePanel.getEnemies()) {
            if (enemy == null || !enemy.isAlive()) {
                continue;
            }
            if (CollisionManager.rectanglesIntersect(dashBounds, CollisionManager.getWorldSolidArea(enemy))) {
                enemy.takeDamage(1);
            }
        }
    }

    /**
     * Applies body-contact damage from enemies that deal contact damage.
     */
    private void resolveEnemyBodyContact() {
        Rectangle playerBounds = playerBounds();
        for (Enemy enemy : gamePanel.getEnemies()) {
            if (enemy == null || !enemy.isAlive()) {
                continue;
            }
            if (enemy.getDamage() <= 0) {
                continue;
            }
            if (CollisionManager.rectanglesIntersect(playerBounds, CollisionManager.getWorldSolidArea(enemy))) {
                gamePanel.getPlayer().takeDamage(enemy.getDamage());
            }
        }
    }

    /**
     * Rebuilds the player's world-space collision rectangle for the current frame.
     */
    private Rectangle playerBounds() {
        return CollisionManager.getWorldSolidArea(gamePanel.getPlayer());
    }
}
