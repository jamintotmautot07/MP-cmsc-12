package main;

import java.awt.CardLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import engine.GamePanel;
import engine.Level;
import panels.CreditScroller;
import panels.OpeningPanel;
import util.Constants;
import util.MethodUtilities;

public class BaseFrame extends JFrame{

    private CardLayout cardLayout;
    private JPanel container;

    private OpeningPanel openPanel;
    public GamePanel gamePanel;
    private CreditScroller credits;
    private Level selectedLevel = Level.TUTORIAL;
    private int maxLevelReached = 3;
    private boolean tutorialPlayed = true;

    public BaseFrame() {
        setTitle("Hawak ko ang Bit: The Final Bit");
        setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Placeholder for loading progress
        // maxLevelReached = FileManager.loadMaxLevelReached();
        // tutorialPlayed = FileManager.loadTutorialPlayed();

        openPanel = new OpeningPanel();
        gamePanel = new GamePanel();
        credits = new CreditScroller();

        cardLayout = new CardLayout();
        container = new JPanel(cardLayout);
        container.setPreferredSize(new Dimension(Constants.screenWidth, Constants.screenHeight));

        container.add(openPanel, "Openning");
        container.add(gamePanel, "Game");
        container.add(credits, "Credits");

        add(container);

        cardLayout.show(container, "Openning");

        openPanel.exitButton.addActionListener(new MethodUtilities.exitAction(this));

        credits.getBackButton().addActionListener(e -> {
            credits.stopTimer();
            cardLayout.show(container, "Openning");
        });

        openPanel.levelButton.addActionListener(e -> {
            panels.LevelSelectionDialog dialog = new panels.LevelSelectionDialog(this, maxLevelReached);
            dialog.setVisible(true);
            if (dialog.selected != null) {
                tutorialPlayed = true;
                selectedLevel = dialog.selected;
                openPanel.setSelectedLevelIndex(Level.getIndex(selectedLevel), selectedLevel.name);
                gamePanel.setLevel(selectedLevel);
                cardLayout.show(container, "Game");
                gamePanel.requestFocusInWindow();
                gamePanel.startGameThread();
            }
        });

        openPanel.playButton.addActionListener(e -> {
            // Sample code when the player already has progress,
            // pressing play resets the progress and let's them start anew
            // ...
            // if (maxLevelReached > 0) {
            //     int confirm = JOptionPane.showConfirmDialog(this, "This will reset your progress. Continue?", "Reset Progress", JOptionPane.YES_NO_OPTION);
            //     if (confirm == JOptionPane.YES_OPTION) {
            //         maxLevelReached = 0;
            //         tutorialPlayed = false;
            //         // FileManager.saveProgress(maxLevelReached, tutorialPlayed);
            //         openPanel.setContinueVisible(false);
            //     } else {
            //         return;
            //     }
            // }
            selectedLevel = Level.TUTORIAL;
            openPanel.setSelectedLevelIndex(Level.getIndex(selectedLevel), selectedLevel.name);
            gamePanel.setLevel(selectedLevel);
            cardLayout.show(container, "Game");
            gamePanel.requestFocusInWindow();
            gamePanel.startGameThread();
        });

        openPanel.creditsButton.addActionListener(e -> {
            cardLayout.show(container, "Credits");
            credits.startTimer();
        });

        openPanel.scoreButton.addActionListener(e -> {
            panels.ScoreboardDialog dialog = new panels.ScoreboardDialog(this);
            int timeScore = gamePanel.timer != null ? gamePanel.timer.getTimeScore() : 0;
            int enemyScore = 0; // placeholder
            int levelsCleared = gamePanel.getLevelsCleared();
            int totalScore = timeScore + enemyScore + levelsCleared * 100; // placeholder calculation
            dialog.updateScores(timeScore, enemyScore, levelsCleared, totalScore);
            dialog.setVisible(true);
        });

        // This button is mainly for saving,
        // when the player already had progress like level progress
        // they can just click this to continue where they left out
        // ....
        // openPanel.continueButton.addActionListener(e -> {
        //     // selectedLevel = Level.LEVEL_1; // continue from first level after tutorial
        //     // selectedLevel = (example code) FileManager.getMaxLevelReached(); - returns a level variable
        //     openPanel.setSelectedLevelIndex(Level.getIndex(selectedLevel), selectedLevel.name);
        //     gamePanel.setLevel(selectedLevel);
        //     cardLayout.show(container, "Game");
        //     gamePanel.requestFocusInWindow();
        //     gamePanel.startGameThread();
        // });

        addWindowListener(new MethodUtilities.exitAction(this));

        // Placeholder for progress setup
        // gamePanel.onLevelComplete = this::updateProgress;
        openPanel.setContinueVisible(tutorialPlayed && maxLevelReached >= 2);

        this.pack();
        setLocationRelativeTo(null);
    }

    public void showOpeningScreen() {
        cardLayout.show(container, "Openning");
    }

    // Placeholder for updateProgress method
    // private void updateProgress() {
    //     Level current = gamePanel.getCurrentLevel();
    //     if (current == Level.TUTORIAL) {
    //         tutorialPlayed = true;
    //     }
    //     if (current.nextLevel != null) {
    //         maxLevelReached = Math.max(maxLevelReached, Level.getIndex(current.nextLevel));
    //     }
    //     FileManager.saveProgress(maxLevelReached, tutorialPlayed);
    // }

    public CreditScroller getCredits() {
        return credits;
    }
}
