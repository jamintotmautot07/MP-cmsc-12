package engine;

import entity.CoreBoss;
import entity.Enemy;
import entity.Trojan;
import entity.VirusDrone;
import entity.Worm;
import tile.TileManager;
import util.UtilityTool;

/**
 * Creates and registers the enemy roster required by each level definition.
 */
public class LevelEnemyFactory {
    private final GamePanel gamePanel;
    private final TileManager tileManager;

    // defined level position
    private final int[][] wormPosLvl_1 = new int[][] {
        {14, 4}, {25, 4}, {38, 4}, {15, 12}, {28, 6},
        {29, 12}, {38, 16}, {47, 18}, {42, 20}, {36, 27},
        {48, 29}, {11, 37}, {22, 38}, {32, 35}, {37, 43}
    };
    private final int[][] wormPosLvl_2 = new int[][] {
        {10, 8}, {38, 8}, {7, 17}, {42, 17}, {17, 18},
        {31, 18}, {14, 30}, {34, 30}, {10, 39}, {29, 41}
    };
    private final int[][] wormPosLvl_3 = new int[][] {
        {6, 24}, {20, 13}, {12, 35}, {20, 17}, {20, 31},
        {33, 13}, {36, 18}, {30, 30}, {27, 39}, {39, 24}
    };
    private final int[][] virusPosLvl_2 = new int[][] {
        {22, 14}, {28, 14}, {20, 24},
        {31, 24}, {16, 35}, {36, 35}
    };
    private final int[][]  virusPosLvl_3 = new int[][] {
        {18, 24}, {27, 14}, {34, 21},
        {27, 34}, {31, 38}
    };
    private final int[][] trojanLvl_3 = new int[][] {
        {12, 11}, {35, 13}, {30, 32}
    };

    public LevelEnemyFactory(GamePanel gamePanel, TileManager tileManager) {
        this.gamePanel = gamePanel;
        this.tileManager = tileManager;
    }

    public void populate(Level level) {
        if (level == Level.LEVEL_1) {
            addWorms(15, wormPosLvl_1);
        } else if (level == Level.LEVEL_2) {
            addWorms(10, wormPosLvl_2);
            addTrojans(1, new int[][] {{24, 44}});
            addViruses(6, virusPosLvl_2);
        } else if (level == Level.LEVEL_3) {
            addWorms(10, wormPosLvl_3);
            addViruses(5, virusPosLvl_3);
            addTrojans(3, trojanLvl_3);
            CoreBoss boss = new CoreBoss(gamePanel);
            boss.setStartTilePosition(24, 21);
            gamePanel.addEnemy(boss);
        }
    }

    private void addWorms(int count, int[][] pos) {
        for (int i = 0; i < count && i < pos.length; i++) {
            Worm enemy = new Worm(gamePanel);
            enemy.setStartTilePosition(pos[i][0], pos[i][1]);
            gamePanel.addEnemy(enemy);
        }
    }

    private void addViruses(int count, int[][] pos) {
        for (int i = 0; i < count && i < pos.length; i++) {
            VirusDrone enemy = new VirusDrone(gamePanel);
            enemy.setStartTilePosition(pos[i][0], pos[i][1]);
            gamePanel.addEnemy(enemy);
        }
    }

    private void addTrojans(int count, int[][] pos) {
        for (int i = 0; i < count && i < pos.length; i++) {
            Trojan enemy = new Trojan(gamePanel);
            enemy.setStartTilePosition(pos[i][0], pos[i][1]);
            gamePanel.addEnemy(enemy);
        }
    }

    private void addRandomly(Enemy enemy) {
        UtilityTool.setRandomEnemyPosition(enemy, tileManager);
        gamePanel.addEnemy(enemy);
    }
}
