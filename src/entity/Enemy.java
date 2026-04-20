
package entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Color;

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

public class Enemy extends Entity {

    // Short attack cycle so the enemy can periodically threaten the player.
    private int attackCounter = 0;
    private final int attackDuration = 15; // Shorter attack duration for enemies
    private boolean attackActive = false;

    private int positionX;
    private int positionY;

    public Enemy(int positionX, int positionY) {
        // Enemies start with a simple default facing and movement speed.
        speed = 2;
        direction = "down";
        facingDirection = "down";

        this.positionX = positionX;
        this.positionY = positionY;
        /*
         * NOTE:
         * `solidArea` is inherited from Entity, but Entity never initializes it on its own.
         * This line assumes some earlier setup has already created the rectangle.
         */
        solidArea = new Rectangle(8, 16, 30, 32);
    }

    public void update(){
        updateCooldowns(); // Always update cooldowns first

        // Current AI is intentionally simple: wait, then perform one attack, then wait again.
        if (!isOnCooldown("Enemy_attack")) {
            performAttack();
            startCooldown("Enemy_attack", 180);
        }

        // Keep the attack alive for a short window, then clear the hitbox again.
        if (attackActive) {
            attackCounter++;
            if (attackCounter > attackDuration) {
                attackCounter = 0;
                attackType = AttackType.NONE;
                attackActive = false;
                attackHitbox.setBounds(positionX, positionY, 0, 0);
            } else {
                // Recalculate in case movement or facing logic is added later.
                attackHitbox = calculateAttackHitbox();
            }
        }
    }

    private void performAttack() {
        // Normal attack uses the shared Entity hitbox logic.
        attackType = AttackType.NORMAL; // Simple forward attack
        attackActive = true;
        attackCounter = 0;
        attackHitbox = calculateAttackHitbox();
    }

    public boolean isAttackActive() {
        return attackActive;
    }

    public void render(Graphics2D g){
        // Placeholder visuals until proper enemy sprites are added.
        g.setColor(Color.RED);
        g.fillRect(positionX, positionY, 32, 32);

        // Debug-friendly attack box so the hit area is easy to inspect.
        if (attackActive && attackHitbox.width > 0 && attackHitbox.height > 0) {
            g.setColor(new Color(255, 0, 0, 120));
            g.fillRect(attackHitbox.x, attackHitbox.y, attackHitbox.width, attackHitbox.height);
            g.setColor(Color.RED);
            g.drawRect(attackHitbox.x, attackHitbox.y, attackHitbox.width, attackHitbox.height);
        }
    }
}
