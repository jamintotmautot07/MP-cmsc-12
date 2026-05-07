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

    public CombatResolver(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void resolve() {
        updateProjectiles();
        updateLasers();
        resolvePlayerMelee();
        resolveEnemyMelee();
        resolvePlayerDash();
        resolveEnemyBodyContact();
    }

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

    private boolean hitPlayer(Projectile projectile) {
        if (CollisionManager.rectanglesIntersect(projectile.getBounds(), playerBounds())) {
            gamePanel.getPlayer().takeDamage(projectile.getDamage());
            return true;
        }
        return false;
    }

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

    private Rectangle playerBounds() {
        return CollisionManager.getWorldSolidArea(gamePanel.getPlayer());
    }
}
