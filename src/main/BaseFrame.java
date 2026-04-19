package main;

import java.awt.CardLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
// import javax.swing.SwingWorker; // COMMENTED OUT - Cache system disabled

import engine.GamePanel;
import engine.Level;
import panels.CreditScroller;
// import panels.LoadingPanel; // COMMENTED OUT - Cache system disabled
import panels.OpeningPanel;
import util.Constants;
import util.MethodUtilities;
// import util.ResourceCache; // COMMENTED OUT - Cache system disabled

public class BaseFrame extends JFrame{

    private CardLayout cardLayout;
    private JPanel container;

    public OpeningPanel openPanel;
    public GamePanel gamePanel;
    private CreditScroller credits;
    // private LoadingPanel loadingPanel; // COMMENTED OUT - Cache system disabled
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
        // loadingPanel = new LoadingPanel(); // COMMENTED OUT - Cache system disabled

        cardLayout = new CardLayout();
        container = new JPanel(cardLayout);
        container.setPreferredSize(new Dimension(Constants.screenWidth, Constants.screenHeight));

        // Add all panels to container
        // container.add(loadingPanel, "Loading"); // COMMENTED OUT - Cache system disabled
        container.add(openPanel, "Openning");
        container.add(gamePanel, "Game");
        container.add(credits, "Credits");

        add(container);

        // Show loading panel initially
        // cardLayout.show(container, "Loading"); // COMMENTED OUT - Cache system disabled
        cardLayout.show(container, "Openning");

        // Setup all button listeners BEFORE loading resources
        setupButtonListeners();

        // COMMENTED OUT - Cache system disabled
        // Preload all resources in background
        // startResourceLoading();

        this.pack();
        setLocationRelativeTo(null);
    }

    private void setupButtonListeners() {
        openPanel.exitButton.addActionListener(new MethodUtilities.exitAction(this));

        credits.getBackButton().addActionListener(e -> {
            credits.stopTimer();
            openPanel.startBackgroundAnimation();
            cardLayout.show(container, "Openning");
        });

        openPanel.levelButton.addActionListener(e -> {
            panels.LevelSelectionDialog dialog = new panels.LevelSelectionDialog(this, maxLevelReached);
            dialog.setVisible(true);
            if (dialog.selected != null) {
                openPanel.stopBackgroundAnimation();
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
            selectedLevel = Level.TUTORIAL;
            openPanel.setSelectedLevelIndex(Level.getIndex(selectedLevel), selectedLevel.name);
            gamePanel.setLevel(selectedLevel);
            openPanel.stopBackgroundAnimation();
            cardLayout.show(container, "Game");
            gamePanel.requestFocusInWindow();
            gamePanel.startGameThread();
        });

        openPanel.creditsButton.addActionListener(e -> {
            openPanel.stopBackgroundAnimation();
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

        addWindowListener(new MethodUtilities.exitAction(this));

        // Placeholder for progress setup
        // gamePanel.onLevelComplete = this::updateProgress;
        openPanel.setContinueVisible(tutorialPlayed && maxLevelReached >= 2);
    }

    // COMMENTED OUT - Cache system disabled
    // private void startResourceLoading() {
    //     new SwingWorker<Void, String>() {
    //         @Override
    //         protected Void doInBackground() throws Exception {
    //             ResourceCache.preloadAll();
    //             publish("Loading resources...");
    //             Thread.sleep(500);
    //             return null;
    //         }
    //
    //         @Override
    //         protected void process(java.util.List<String> chunks) {
    //             String message = chunks.get(chunks.size() - 1);
    //             loadingPanel.setProgress(75, message);
    //         }
    //
    //         @Override
    //         protected void done() {
    //             loadingPanel.setProgress(100, "Ready!");
    //             try {
    //                 Thread.sleep(300);
    //             } catch (InterruptedException e) {
    //             }
    //             openPanel.loadBackgroundFrames();
    //             openPanel.startBackgroundAnimation();
    //             cardLayout.show(container, "Openning");
    //             openPanel.requestFocusInWindow();
    //         }
    //     }.execute();
    // }

    public void showOpeningScreen() {
        openPanel.startBackgroundAnimation();
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
