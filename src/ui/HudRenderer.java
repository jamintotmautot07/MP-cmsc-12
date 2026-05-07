package ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import engine.Level;
import entity.Enemy;
import entity.Player;
import systems.Timer;
import util.Constants;
import util.MethodUtilities;
import util.UtilityTool;

/**
 * Draws gameplay HUD elements such as level text, timer, player life, and enemy counters.
 */
public class HudRenderer {
    public void draw(
        Graphics2D g2,
        Level currentLevel,
        Timer timer,
        Player player,
        List<Enemy> enemies,
        Map<String, Integer> enemyTotals
    ) {
        g2.setFont(MethodUtilities.getFont(13f));
        g2.setColor(Color.WHITE);
        g2.drawString("Level: " + (currentLevel != null ? currentLevel.name : "Unknown"), 20, 20);

        if (timer != null) {
            timer.show(g2, 20, 40);
        }

        drawPlayerLife(g2, player);
        drawEnemyCounter(g2, enemies, enemyTotals);
    }

    private void drawPlayerLife(Graphics2D g2, Player player) {
        int heartSize = 20;
        int spacing = 27;
        int startX = 20;
        int startY = 60;
        int hp = player.getHp();
        int maxHp = player.getMaxHp();
        int heartCount = (maxHp + 1) / 2;
        int panelWidth = 48 + heartCount * spacing;
        int panelHeight = 34;

        g2.setColor(new Color(0, 0, 0, 145));
        g2.fillRoundRect(startX - 8, startY - 8, panelWidth, panelHeight, 14, 14);
        g2.setColor(new Color(255, 255, 255, 45));
        g2.drawRoundRect(startX - 8, startY - 8, panelWidth, panelHeight, 14, 14);

        g2.setColor(Color.WHITE);
        g2.setFont(MethodUtilities.getFont(14f));
        g2.drawString("HP", startX, startY + 14);

        int heartX = startX + 30;
        for (int i = 0; i < heartCount; i++) {
            int heartLife = hp - (i * 2);
            drawHeart(g2, heartX + (i * spacing), startY, heartSize, heartLife);
        }
    }

    private void drawHeart(Graphics2D g2, int x, int y, int size, int heartLife) {
        if (heartLife > 0) {
            g2.setColor(new Color(255, 70, 90, 85));
            fillHeart(g2, x - 3, y - 3, size + 6);
        }

        g2.setColor(new Color(45, 45, 45, 190));
        fillHeart(g2, x, y, size);

        if (heartLife > 0) {
            Graphics2D clipped = (Graphics2D) g2.create();
            int fillWidth = heartLife >= 2 ? size : size / 2;
            clipped.setClip(x, y, fillWidth, size);
            clipped.setColor(new Color(220, 30, 45));
            fillHeart(clipped, x, y, size);
            clipped.dispose();
        }

        g2.setColor(Color.BLACK);
        drawHeartOutline(g2, x, y, size);
    }

    private void fillHeart(Graphics2D g2, int x, int y, int size) {
        int half = size / 2;
        g2.fillOval(x, y, half, half);
        g2.fillOval(x + half, y, half, half);

        int[] xPoints = {x, x + size, x + half};
        int[] yPoints = {y + half / 2, y + half / 2, y + size};
        g2.fillPolygon(xPoints, yPoints, 3);
    }

    private void drawHeartOutline(Graphics2D g2, int x, int y, int size) {
        int half = size / 2;
        g2.drawOval(x, y, half, half);
        g2.drawOval(x + half, y, half, half);

        int[] xPoints = {x, x + size, x + half};
        int[] yPoints = {y + half / 2, y + half / 2, y + size};
        g2.drawPolygon(xPoints, yPoints, 3);
    }

    private void drawEnemyCounter(Graphics2D g2, List<Enemy> enemies, Map<String, Integer> enemyTotals) {
        if (enemyTotals.isEmpty()) {
            return;
        }

        int aliveTotal = 0;
        Map<String, Integer> aliveByType = new LinkedHashMap<>();
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) {
                continue;
            }
            String type = UtilityTool.getEntityName(enemy);
            aliveByType.put(type, aliveByType.getOrDefault(type, 0) + 1);
            aliveTotal++;
        }

        int width = 210;
        int lineHeight = 18;
        int rows = Math.max(1, enemyTotals.size());
        int height = 34 + rows * lineHeight;
        int x = Constants.screenWidth - width - 18;
        int y = 18;

        g2.setColor(new Color(0, 0, 0, 155));
        g2.fillRoundRect(x, y, width, height, 16, 16);
        g2.setColor(new Color(255, 255, 255, 55));
        g2.drawRoundRect(x, y, width, height, 16, 16);

        g2.setFont(MethodUtilities.getFont(14f));
        g2.setColor(new Color(245, 245, 245));
        g2.drawString("Enemies: " + aliveTotal, x + 12, y + 20);

        int textY = y + 40;
        for (Map.Entry<String, Integer> entry : enemyTotals.entrySet()) {
            int alive = aliveByType.getOrDefault(entry.getKey(), 0);
            g2.setColor(new Color(210, 230, 255));
            g2.drawString(entry.getKey() + ": " + alive + "/" + entry.getValue(), x + 12, textY);
            textY += lineHeight;
        }
    }
}
