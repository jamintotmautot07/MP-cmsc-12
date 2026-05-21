package main;

import audio.AudioPlayer;
import engine.GamePanel;
import engine.Level;
import exception.GameException;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import panels.CreditScroller;
import panels.LevelSelectionDialog;
import panels.LoadingPanel;
import panels.OpeningPanel;
import panels.ScenePanel;
import panels.StoryPanel;
import systems.FileManager;
import systems.ScoreFileManager;
import systems.ScoreManager;
import ui.IntroManager;
import util.Constants;
import util.MethodUtilities;
import util.ResourceCache;

/**
 * Top-level application window that swaps between menu, cutscene, credits, and gameplay screens.
 */
public class BaseFrame extends JFrame {

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
    private StoryPanel storyPanel;
    // Basic progress / selection values shared between the menu and game screens.
    private Level selectedLevel = Level.TUTORIAL;
    private int maxLevelReached = 0;
    private boolean tutorialPlayed = false;
    private String lastSavedAtText = "Never";
    // Singleton Sound manager
    private AudioPlayer audioPlayer;
    // Resize handler for undecorated window
    private int resizeMargin = 5;
    private panels.ScoreboardDialog scoreboardDialog;

    /**
     * Builds all major screens once and wires the application flow between them.
     */
    public BaseFrame() {
        setTitle("Hawak ko ang Bit: The Final Bit");
        setMinimumSize(new Dimension(Constants.screenWidth / 2, Constants.screenHeight / 2));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setUndecorated(true);
        setResizable(true);

        loadProgress();

        cardLayout = new CardLayout();
        container = new JPanel(cardLayout);
        container.setPreferredSize(new Dimension(Constants.screenWidth, Constants.screenHeight));
        
        loadingPanel = new LoadingPanel(); 
        container.add(loadingPanel, "Loading");
        
        add(container);

        cardLayout.show(container, "Loading");

        loadingPanel.startLoading(() -> {
            audioPlayer = AudioPlayer.getInstance();
            createMainScreensAfterLoading();
            // Set the window icon after resources have been preloaded
            try {
                setIconImage(ResourceCache.getImage("icon"));
            } catch (Exception e) {
                // ignore - fallback icon will be used
            }
            setupButtonListeners();
            startStartupScene();
        });
        
        pack();
        setLocationRelativeTo(null);
        setupWindowResize();
    }

    /**
     * Builds every screen that depends on preloaded resources.
     * This runs after LoadingPanel finishes so panels can safely ask ResourceCache for images and fonts.
     */
    private void createMainScreensAfterLoading() {
        openPanel = new OpeningPanel();
        gamePanel = new GamePanel();
        credits = new CreditScroller();
        storyPanel = new StoryPanel(maxLevelReached);
        sceneManager = new IntroManager();
        scenePanel = new ScenePanel(sceneManager);

        openPanel.setLastSavedTime(lastSavedAtText);

        container.add(scenePanel, "Scene");
        container.add(openPanel, "Openning");
        container.add(gamePanel, "Game");
        container.add(credits, "Credits");
        container.add(storyPanel, "Story");

        scoreboardDialog = new panels.ScoreboardDialog();
        container.add(scoreboardDialog, "Scoreboard");
    }

    /**
     * Plays the opening scene the first time the app is shown.
     */
    private void startStartupScene() {
        // The opening cinematic only plays once per app session.
        if (!sceneManager.hasPlayed("gameIntro")) {
            // openPanel.stopBackgroundAnimation();
            scenePanel.setOnSceneComplete(() -> {
                audioPlayer.stopMusic();
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

        scoreboardDialog.setListener(() -> {
            openPanel.startBackgroundAnimation();
            cardLayout.show(container, "Openning");
        });

        openPanel.levelButton.addActionListener(e -> {
            // Open a modal selector, then start the chosen level if the user picked one.
            //panels.LevelSelectionDialog dialog = new panels.LevelSelectionDialog(maxLevelReached);
            //dialog.setVisible(true);
            /* if (dialog.selected != null) {
                openPanel.stopBackgroundAnimation();
                selectedLevel = dialog.selected;
                openPanel.setSelectedLevelIndex(Level.getIndex(selectedLevel), selectedLevel.name);
                saveProgress(Level.getIndex(selectedLevel));
                gamePanel.startLevelFromMenu(selectedLevel);
                cardLayout.show(container, "Game");
                SwingUtilities.invokeLater(() -> {
                    gamePanel.requestFocusInWindow();
                });

                gamePanel.startGameThread();
            } */

            LevelSelectionDialog levelPanel = new LevelSelectionDialog(maxLevelReached);

            openPanel.stopBackgroundAnimation();

            levelPanel.setListener(new LevelSelectionDialog.LevelSelectListener() {

                @Override
                public void onLevelSelected(Level level) {
                    openPanel.stopBackgroundAnimation();

                    selectedLevel = level;
                    openPanel.setSelectedLevelIndex(Level.getIndex(selectedLevel), selectedLevel.name);
                    saveProgress(Level.getIndex(selectedLevel));

                    gamePanel.startLevelFromMenu(selectedLevel);
                    cardLayout.show(container, "Game");

                    SwingUtilities.invokeLater(() -> {
                        gamePanel.requestFocusInWindow();
                    });

                    gamePanel.startGameThread();
                }

                @Override
                public void onBack() {
                    
                    openPanel.startBackgroundAnimation();
                    cardLayout.show(container, "Openning");
                    openPanel.requestFocusInWindow();
                }
            });

            // Add panel dynamically (only once ideally, but safe here)
            container.add(levelPanel, "LevelSelect");

            // Switch to it
            cardLayout.show(container, "LevelSelect");
        });

        openPanel.continueButton.addActionListener(e -> {
            try {
                int savedLevelIndex = Math.min(FileManager.loadSelectedLevel(), maxLevelReached);
                selectedLevel = Level.LEVELS[savedLevelIndex];
            } catch(GameException ex) {
                selectedLevel = Level.TUTORIAL;
            }

            openPanel.stopBackgroundAnimation();
            gamePanel.startLevelFromMenu(selectedLevel);
            cardLayout.show(container, "Game");

            SwingUtilities.invokeLater(() -> {
                gamePanel.requestFocusInWindow();
            });

            gamePanel.startGameThread();
        });

        openPanel.playButton.addActionListener(e -> {
            if (maxLevelReached >= 1) {
                Object[] options = {"Continue", "Cancel"};
                int choice = JOptionPane.showOptionDialog(
                    this,
                    "Start system recovery?",
                    "Start Game",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
                );
                if (choice != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            selectedLevel = Level.TUTORIAL;
            openPanel.setSelectedLevelIndex(Level.getIndex(selectedLevel), selectedLevel.name);
            saveProgress(Level.getIndex(selectedLevel));
            gamePanel.startRunFromTutorial();
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

        openPanel.cutScenesButton.addActionListener(e -> {
            storyPanel.setMaxLevelReached(maxLevelReached);
            openPanel.stopBackgroundAnimation();
            cardLayout.show(container, "Story");
            storyPanel.requestFocusInWindow();
        });

        storyPanel.getBackButton().addActionListener(e -> {
            openPanel.startBackgroundAnimation();
            cardLayout.show(container, "Openning");
            openPanel.requestFocusInWindow();
        });

        storyPanel.setSceneSelectionListener(scene -> {
            scenePanel.setOnSceneComplete(() -> {
                storyPanel.setMaxLevelReached(maxLevelReached);
                cardLayout.show(container, "Story");
                storyPanel.requestFocusInWindow();
            });
            cardLayout.show(container, "Scene");
            scenePanel.startScene(
                scene.getSceneId(),
                scene.getFilePattern(),
                scene.getFrameCount(),
                scene.getFrameDelayMs()
            );
        });

        

        openPanel.scoreButton.addActionListener(e -> {
            // Try to load the last saved scores; fall back to the current run if unavailable.
            ScoreManager displayScores;
            try {
                displayScores = ScoreFileManager.loadScores();
            } catch (GameException ex) {
                displayScores = gamePanel.getScoreManager();
            }

            scoreboardDialog.updateScores(displayScores);
            openPanel.stopBackgroundAnimation();
            cardLayout.show(container, "Scoreboard");
        });

        addWindowListener(new MethodUtilities.exitAction(this));

        gamePanel.onLevelComplete = this::updateProgress;
        openPanel.setContinueVisible(hasSavedProgress());
    }

    /**
     * Returns the app to the opening menu card.
     */
    public void showOpeningScreen() {
        // Central helper used when backing out of gameplay to the main menu.
        openPanel.startBackgroundAnimation();
        cardLayout.show(container, "Openning");
    }

    /**
     * Reads saved progression before the menu is shown.
     * If the save file is missing or invalid, the game falls back to a fresh tutorial-only state.
     */
    private void loadProgress() {
        try {
            FileManager.createSaveFile();
            maxLevelReached = FileManager.loadMaxLevelReached();
            tutorialPlayed = FileManager.loadTutorialPlayed();
            int selectedLevelIndex = Math.min(FileManager.loadSelectedLevel(), maxLevelReached);
            selectedLevel = Level.LEVELS[selectedLevelIndex];
            lastSavedAtText = FileManager.loadSavedAtText();
        } catch(GameException e) {
            maxLevelReached = 0;
            tutorialPlayed = false;
            selectedLevel = Level.TUTORIAL;
            JOptionPane.showMessageDialog(this, e.getMessage(), "Save File Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Records level unlocks after GamePanel reports a clear.
     * The method advances both the playable level selection and the story archive unlock progress.
     */
    private void updateProgress() {
        Level clearedLevel = gamePanel.getCurrentLevel();
        int clearedIndex = Level.getIndex(clearedLevel);
        int nextIndex = clearedLevel.nextLevel != null ? Level.getIndex(clearedLevel.nextLevel) : clearedIndex;
        int progressIndex = clearedLevel.nextLevel != null ? nextIndex : StoryPanel.ENDING_UNLOCK_PROGRESS;
        int selectedLevelIndex = Math.min(nextIndex, Level.LEVELS.length - 1);

        if(clearedLevel == Level.TUTORIAL) {
            tutorialPlayed = true;
        }

        maxLevelReached = Math.max(maxLevelReached, progressIndex);
        selectedLevel = Level.LEVELS[selectedLevelIndex];
        openPanel.setSelectedLevelIndex(selectedLevelIndex, selectedLevel.name);
        openPanel.setContinueVisible(hasSavedProgress());
        if (storyPanel != null) {
            storyPanel.setMaxLevelReached(maxLevelReached);
        }
        saveProgress(selectedLevelIndex);
    }

    /**
     * Persists the latest unlock state and selected level.
     * Save failures are shown as warnings instead of crashing the whole game.
     */
    private void saveProgress(int selectedLevelIndex) {
        try {
            int currentHighScore = FileManager.loadHighScore();
            int currentRunScore = gamePanel != null ? gamePanel.getScoreManager().calculateTotalScore() : 0;
            int highScoreToSave = Math.max(currentHighScore, currentRunScore);
            FileManager.saveData(highScoreToSave, tutorialPlayed, maxLevelReached, selectedLevelIndex);
            lastSavedAtText = FileManager.loadSavedAtText();
            if (openPanel != null) {
                openPanel.setLastSavedTime(lastSavedAtText);
            }
        } catch(GameException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Save File Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Checks whether the Continue button should be useful to the player.
     */
    private boolean hasSavedProgress() {
        return tutorialPlayed || maxLevelReached >0;
    }

    /**
     * Sets up mouse listeners to enable resizing of undecorated windows by dragging edges.
     */
    private void setupWindowResize() {
        MouseAdapter resizeAdapter = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int x = e.getLocationOnScreen().x - getLocationOnScreen().x;
                int y = e.getLocationOnScreen().y - getLocationOnScreen().y;
                int width = getWidth();
                int height = getHeight();

                // Determine which resize region the cursor is in
                boolean onLeft = x < resizeMargin;
                boolean onRight = x > width - resizeMargin;
                boolean onTop = y < resizeMargin;
                boolean onBottom = y > height - resizeMargin;

                if ((onLeft && onTop) || (onRight && onBottom)) {
                    setCursor(new Cursor(Cursor.NW_RESIZE_CURSOR));
                } else if ((onRight && onTop) || (onLeft && onBottom)) {
                    setCursor(new Cursor(Cursor.NE_RESIZE_CURSOR));
                } else if (onLeft || onRight) {
                    setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
                } else if (onTop || onBottom) {
                    setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
                } else {
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getLocationOnScreen().x - getLocationOnScreen().x;
                int y = e.getLocationOnScreen().y - getLocationOnScreen().y;
                int width = getWidth();
                int height = getHeight();

                boolean onLeft = x < resizeMargin;
                boolean onRight = x > width - resizeMargin;
                boolean onTop = y < resizeMargin;
                boolean onBottom = y > height - resizeMargin;

                int newX = getX();
                int newY = getY();
                int newWidth = width;
                int newHeight = height;

                // Handle edge resizing
                if (onLeft) {
                    newX = e.getLocationOnScreen().x;
                    newWidth = width + getX() - newX;
                }
                if (onRight) {
                    newWidth = e.getLocationOnScreen().x - getX();
                }
                if (onTop) {
                    newY = e.getLocationOnScreen().y;
                    newHeight = height + getY() - newY;
                }
                if (onBottom) {
                    newHeight = e.getLocationOnScreen().y - getY();
                }

                // Apply minimum size constraints
                Dimension minSize = getMinimumSize();
                if (newWidth < minSize.width) {
                    newWidth = minSize.width;
                    if (onLeft) newX = getX();
                }
                if (newHeight < minSize.height) {
                    newHeight = minSize.height;
                    if (onTop) newY = getY();
                }

                setBounds(newX, newY, newWidth, newHeight);
            }
        };

        addMouseListener(resizeAdapter);
        addMouseMotionListener(resizeAdapter);
    }

    /**
     * Stops any active background work before exiting the application.
     */
    public void cleanupBeforeExit() {
        if (loadingPanel != null) {
            loadingPanel.stopLoading();
        }
        if (scenePanel != null) {
            scenePanel.stopScene();
        }
        if (gamePanel != null) {
            gamePanel.stopGameThread();
        }
        if (credits != null) {
            credits.stopTimer();
        }
        if (openPanel != null) {
            openPanel.stopBackgroundAnimation();
        }
    }

    /**
     * Exposes the credits panel for shutdown cleanup.
     */
    public CreditScroller getCredits() {
        return credits;
    }
}
