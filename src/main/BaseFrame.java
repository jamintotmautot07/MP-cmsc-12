package main;

import java.awt.CardLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
// import javax.swing.SwingWorker; // COMMENTED OUT - Cache system disabled

import engine.GamePanel;
import engine.Level;
import exception.GameException;
import panels.CreditScroller;
import panels.LoadingPanel;
import panels.OpeningPanel;
import panels.ScenePanel;
import systems.FileManager;
import ui.IntroManager;
import util.Constants;
import util.MethodUtilities;

/**
 * Top-level application window that swaps between menu, cutscene, credits, and gameplay screens.
 */
public class BaseFrame extends JFrame{

    // CardLayout lets the app swap between menu, game, credits, and cutscene panels in one window.
    private CardLayout cardLayout;
    private JPanel container;

    // Main screens owned by the application frame.
    public OpeningPanel openPanel;
    public GamePanel gamePanel;
    private CreditScroller credits;
    public IntroManager sceneManager;
    public ScenePanel scenePanel;
    private LoadingPanel loadingPanel;
    // Basic progress / selection values shared between the menu and game screens.
    private Level selectedLevel = Level.TUTORIAL;
    private int maxLevelReached = 0;
    private boolean tutorialPlayed = false;

    /**
     * Builds all major screens once and wires the application flow between them.
     */
    public BaseFrame() {
        setTitle("Hawak ko ang Bit: The Final Bit");
        setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        loadProgress();

        cardLayout = new CardLayout();
        container = new JPanel(cardLayout);
        container.setPreferredSize(new Dimension(Constants.screenWidth, Constants.screenHeight));
        
        loadingPanel = new LoadingPanel(); 
        container.add(loadingPanel, "Loading");
        
        add(container);

        cardLayout.show(container, "Loading");

        loadingPanel.startLoading(() -> {
            createMainScreensAfterLoading();
            setupButtonListeners();
            startStartupScene();
        });
        
        pack();
        setLocationRelativeTo(null);
    }

    private void createMainScreensAfterLoading() {
        openPanel = new OpeningPanel();
        gamePanel = new GamePanel();
        credits = new CreditScroller();
        sceneManager = new IntroManager();
        scenePanel = new ScenePanel(sceneManager);

        container.add(scenePanel, "Scene");
        container.add(openPanel, "Openning");
        container.add(gamePanel, "Game");
        container.add(credits, "Credits");
    }

    /**
     * Plays the opening scene the first time the app is shown.
     */
    private void startStartupScene() {
        // The opening cinematic only plays once per app session.
        if (!sceneManager.hasPlayed("gameIntro")) {
            openPanel.stopBackgroundAnimation();
            scenePanel.setOnSceneComplete(() -> {
                // Return to the menu once the cutscene ends.
                openPanel.startBackgroundAnimation();
                cardLayout.show(container, "Openning");
                openPanel.requestFocusInWindow();
            });
            cardLayout.show(container, "Scene");
            scenePanel.startScene("gameIntro", "res/IntroSeq/Intro%04d.png", 221, 100);
        }
    }

    /**
     * Centralizes the menu button wiring so screen transitions stay in one place.
     */
    private void setupButtonListeners() {
        // Exit handling is shared between the exit button and the window close button.
        openPanel.exitButton.addActionListener(new MethodUtilities.exitAction(this));

        credits.getBackButton().addActionListener(e -> {
            credits.stopTimer();
            openPanel.startBackgroundAnimation();
            cardLayout.show(container, "Openning");
        });

        openPanel.levelButton.addActionListener(e -> {
            // Open a modal selector, then start the chosen level if the user picked one.
            panels.LevelSelectionDialog dialog = new panels.LevelSelectionDialog(this, maxLevelReached);
            dialog.setVisible(true);
            if (dialog.selected != null) {
                openPanel.stopBackgroundAnimation();
                selectedLevel = dialog.selected;
                openPanel.setSelectedLevelIndex(Level.getIndex(selectedLevel), selectedLevel.name);
                saveProgress(Level.getIndex(selectedLevel));
                gamePanel.setLevel(selectedLevel);
                cardLayout.show(container, "Game");
                SwingUtilities.invokeLater(() -> {
                    gamePanel.requestFocusInWindow();
                });

                gamePanel.startGameThread();
            }
        });

        openPanel.continueButton.addActionListener(e -> {
            try {
                int savedLevelIndex = Math.min(FileManager.loadSelectedLevel(), maxLevelReached);
                selectedLevel = Level.LEVELS[savedLevelIndex];
            } catch(GameException ex) {
                selectedLevel = Level.TUTORIAL;
            }

            openPanel.stopBackgroundAnimation();
            gamePanel.setLevel(selectedLevel);
            cardLayout.show(container, "Game");

            SwingUtilities.invokeLater(() -> {
                gamePanel.requestFocusInWindow();
            });

            gamePanel.startGameThread();
        });

        openPanel.playButton.addActionListener(e -> {
            // "Play" always starts from the tutorial in the current version.
            selectedLevel = Level.TUTORIAL;
            openPanel.setSelectedLevelIndex(Level.getIndex(selectedLevel), selectedLevel.name);
            gamePanel.setLevel(selectedLevel);
            openPanel.stopBackgroundAnimation();
            cardLayout.show(container, "Game");
            SwingUtilities.invokeLater(() -> {
                gamePanel.requestFocusInWindow();
            });

            gamePanel.startGameThread();
        });

        openPanel.creditsButton.addActionListener(e -> {
            openPanel.stopBackgroundAnimation();
            cardLayout.show(container, "Credits");
            credits.startTimer();
        });

        openPanel.scoreButton.addActionListener(e -> {
            // Scoreboard uses simple placeholder values for unfinished systems.
            panels.ScoreboardDialog dialog = new panels.ScoreboardDialog(this);
            int timeScore = gamePanel.timer != null ? gamePanel.timer.getTimeScore() : 0;
            int enemyScore = 0; // placeholder
            int levelsCleared = gamePanel.getLevelsCleared();
            int totalScore = timeScore + enemyScore + levelsCleared * 100; // placeholder calculation
            dialog.updateScores(timeScore, enemyScore, levelsCleared, totalScore);
            dialog.setVisible(true);
        });

        addWindowListener(new MethodUtilities.exitAction(this));

        gamePanel.onLevelComplete = this::updateProgress;
        openPanel.setContinueVisible(hasSavedProgress());
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

    /**
     * Returns the app to the opening menu card.
     */
    public void showOpeningScreen() {
        // Central helper used when backing out of gameplay to the main menu.
        openPanel.startBackgroundAnimation();
        cardLayout.show(container, "Openning");
    }

    private void loadProgress() {
        try {
            FileManager.createSaveFile();
            maxLevelReached = FileManager.loadMaxLevelReached();
            tutorialPlayed = FileManager.loadTutorialPlayed();
            int selectedLevelIndex = Math.min(FileManager.loadSelectedLevel(), maxLevelReached);
            selectedLevel = Level.LEVELS[selectedLevelIndex];
        } catch(GameException e) {
            maxLevelReached = 0;
            tutorialPlayed = false;
            selectedLevel = Level.TUTORIAL;
            JOptionPane.showMessageDialog(this, e.getMessage(), "Save File Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updateProgress() {
        Level clearedLevel = gamePanel.getCurrentLevel();
        int clearedIndex = Level.getIndex(clearedLevel);
        int nextIndex = clearedLevel.nextLevel != null ? Level.getIndex(clearedLevel.nextLevel) : clearedIndex;

        if(clearedLevel == Level.TUTORIAL) {
            tutorialPlayed = true;
        }

        maxLevelReached = Math.max(maxLevelReached, nextIndex);
        selectedLevel = Level.LEVELS[nextIndex];
        openPanel.setSelectedLevelIndex(nextIndex, selectedLevel.name);
        openPanel.setContinueVisible(hasSavedProgress());
        saveProgress(nextIndex);
    }

    private void saveProgress(int selectedLevelIndex) {
        try {
            FileManager.saveProgress(maxLevelReached, tutorialPlayed, selectedLevelIndex);
        } catch(GameException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Save File Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private boolean hasSavedProgress() {
        return tutorialPlayed || maxLevelReached >0;
    }

    /**
     * Exposes the credits panel for shutdown cleanup.
     */
    public CreditScroller getCredits() {
        return credits;
    }
}
