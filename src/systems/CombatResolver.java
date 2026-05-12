package systems;

import java.awt.Rectangle;

import engine.GamePanel;
import entity.CoreBoss;
import entity.Dummy;
import entity.Enemy;
import entity.Laser;
import entity.Player;
import entity.Worm;
import util.Constants;
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
                if(enemy instanceof CoreBoss) {
                    if(((CoreBoss)enemy).getCanBeDamaged()) {
                        enemy.takeDamage(projectile.getDamage());
                    } else {
                        gamePanel.showObjectForBoss();
                        continue;
                    }
                } else {
                    enemy.takeDamage(projectile.getDamage());
                }
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
                int damage = enemy.getHp() - 1;
                if(enemy instanceof CoreBoss) {
                    if(((CoreBoss)enemy).getCanBeDamaged()) {
                        enemy.takeDamage(damage - 3);
                    } else {
                        gamePanel.showObjectForBoss();
                        continue;
                    }
                } else {
                    if(enemy instanceof Worm || enemy instanceof Dummy) {
                        damage = enemy.getHp();
                    }
                    enemy.takeDamage(damage); //almost one shots the enemy unless it is a worm or dummy
                    player.stealLifeFromMelee(damage);
                }
                
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
        dashBounds.setSize(Constants.tileSize, Constants.tileSize);
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
