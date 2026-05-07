package panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import engine.Level;
import util.Constants;
import util.MethodUtilities;
import util.ResourceCache;
import util.MethodUtilities.CustomButton;

/**
 * Story gallery for replaying cutscenes that the save file says the player has reached.
 */
public class StoryPanel extends JPanel {
    private static final int SCENE_FRAME_DELAY_MS = 55;
    private static final int CARD_WIDTH = 300;
    private static final int CARD_HEIGHT = 218;
    private static final int THUMB_WIDTH = 272;
    private static final int THUMB_HEIGHT = 150;
    private static final Color DARK_GREEN = new Color(47, 55, 47);
    private static final Color CARD_GREEN = new Color(129, 167, 109);
    private static final Color CARD_HOVER = new Color(147, 186, 126);
    private static final Color CARD_PRESSED = new Color(77, 104, 62);

    public static final int ENDING_UNLOCK_PROGRESS = Level.LEVELS.length;

    private static final List<StoryScene> STORY_SCENES = Arrays.asList(
        new StoryScene(
            "gameIntro",
            "Opening Intro",
            "res/IntroSeq/Intro%04d.png",
            221,
            100,
            0,
            30
        ),
        new StoryScene(
            "level1Intro",
            "Level 1 Intro",
            "res/Lvl1Intro/Lvl1_Intro_%04d.png",
            152,
            SCENE_FRAME_DELAY_MS,
            1,
            36
        ),
        new StoryScene(
            "level2Intro",
            "Level 2 Intro",
            "res/Lvl2Intro/Lvl2_Intro_%04d.png",
            111,
            SCENE_FRAME_DELAY_MS,
            2,
            28
        ),
        new StoryScene(
            "bossIntro",
            "Boss Intro",
            "res/Lvl3Intro/Lvl3_Intro_%04d.png",
            237,
            SCENE_FRAME_DELAY_MS,
            3,
            48
        ),
        new StoryScene(
            "endingOutro",
            "Ending Outro",
            "res/Outro/Outro_%04d.png",
            149,
            SCENE_FRAME_DELAY_MS,
            ENDING_UNLOCK_PROGRESS,
            34
        )
    );

    private final CustomButton backButton;
    private final JPanel storyGrid;
    private final JLabel emptyLabel;
    private Consumer<StoryScene> sceneSelectionListener;
    private int maxLevelReached;

    public StoryPanel(int maxLevelReached) {
        this.maxLevelReached = Math.max(0, maxLevelReached);

        setPreferredSize(new Dimension(Constants.screenWidth, Constants.screenHeight));
        setLayout(new BorderLayout());
        setOpaque(false);

        backButton = new CustomButton("BACK");
        backButton.setPreferredSize(new Dimension(110, 36));

        JLabel titleLabel = new JLabel("STORY ARCHIVE", SwingConstants.CENTER);
        titleLabel.setForeground(new Color(220, 255, 225));
        titleLabel.setFont(MethodUtilities.getFont(26f));

        JLabel hintLabel = new JLabel("Replay recovered intros and outros", SwingConstants.CENTER);
        hintLabel.setForeground(new Color(220, 255, 225, 190));
        hintLabel.setFont(MethodUtilities.getFont(14f));

        JPanel titleStack = new JPanel();
        titleStack.setOpaque(false);
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        hintLabel.setAlignmentX(CENTER_ALIGNMENT);
        titleStack.add(titleLabel);
        titleStack.add(Box.createRigidArea(new Dimension(0, 4)));
        titleStack.add(hintLabel);

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(18, 22, 12, 22));
        header.add(backButton, BorderLayout.WEST);
        header.add(titleStack, BorderLayout.CENTER);
        header.add(Box.createRigidArea(new Dimension(110, 1)), BorderLayout.EAST);

        storyGrid = new JPanel(new GridLayout(0, 2, 18, 18));
        storyGrid.setOpaque(false);
        storyGrid.setBorder(BorderFactory.createEmptyBorder(14, 24, 24, 24));

        JPanel storyContent = new JPanel(new BorderLayout());
        storyContent.setOpaque(false);
        storyContent.add(storyGrid, BorderLayout.NORTH);

        emptyLabel = new JLabel("No recovered story scenes yet.", SwingConstants.CENTER);
        emptyLabel.setForeground(new Color(235, 255, 235));
        emptyLabel.setFont(MethodUtilities.getFont(18f));
        emptyLabel.setVisible(false);
        storyContent.add(emptyLabel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(storyContent);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        rebuildStoryButtons();
    }

    public JButton getBackButton() {
        return backButton;
    }

    public void setSceneSelectionListener(Consumer<StoryScene> listener) {
        this.sceneSelectionListener = listener;
    }

    public void setMaxLevelReached(int maxLevelReached) {
        int safeProgress = Math.max(0, maxLevelReached);
        if (this.maxLevelReached == safeProgress) {
            return;
        }

        this.maxLevelReached = safeProgress;
        rebuildStoryButtons();
    }

    private void rebuildStoryButtons() {
        storyGrid.removeAll();

        int visibleScenes = 0;
        for (StoryScene scene : STORY_SCENES) {
            if (!scene.isUnlocked(maxLevelReached)) {
                continue;
            }

            StoryButton button = new StoryButton(scene);
            button.addActionListener(e -> {
                if (sceneSelectionListener != null) {
                    sceneSelectionListener.accept(scene);
                }
            });
            storyGrid.add(button);
            visibleScenes++;
        }

        emptyLabel.setVisible(visibleScenes == 0);
        storyGrid.revalidate();
        storyGrid.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        BufferedImage background = ResourceCache.getImage("background");
        g2.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        g2.setColor(new Color(0, 0, 0, 145));
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.dispose();
    }

    public static class StoryScene {
        private final String sceneId;
        private final String title;
        private final String filePattern;
        private final int frameCount;
        private final int frameDelayMs;
        private final int unlockProgress;
        private final int thumbnailFrame;

        private StoryScene(
            String sceneId,
            String title,
            String filePattern,
            int frameCount,
            int frameDelayMs,
            int unlockProgress,
            int thumbnailFrame
        ) {
            this.sceneId = sceneId;
            this.title = title;
            this.filePattern = filePattern;
            this.frameCount = frameCount;
            this.frameDelayMs = frameDelayMs;
            this.unlockProgress = unlockProgress;
            this.thumbnailFrame = thumbnailFrame;
        }

        public String getSceneId() {
            return sceneId;
        }

        public String getTitle() {
            return title;
        }

        public String getFilePattern() {
            return filePattern;
        }

        public int getFrameCount() {
            return frameCount;
        }

        public int getFrameDelayMs() {
            return frameDelayMs;
        }

        private boolean isUnlocked(int maxLevelReached) {
            return maxLevelReached >= unlockProgress;
        }

        private String getThumbnailPath() {
            int safeFrame = Math.max(0, Math.min(thumbnailFrame, frameCount - 1));
            return String.format(filePattern, safeFrame);
        }
    }

    private static class StoryButton extends JButton {
        private Color fillColor = CARD_GREEN;

        private StoryButton(StoryScene scene) {
            setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
            setMinimumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
            setMaximumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setText(scene.getTitle());
            setIcon(createThumbnail(scene));
            setForeground(DARK_GREEN);
            setFont(MethodUtilities.getFont(19f));
            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalAlignment(SwingConstants.CENTER);
            setHorizontalTextPosition(SwingConstants.CENTER);
            setVerticalTextPosition(SwingConstants.BOTTOM);
            setIconTextGap(12);
            setMargin(new Insets(14, 14, 14, 14));
            setBorder(BorderFactory.createBevelBorder(
                BevelBorder.RAISED,
                new Color(184, 212, 168),
                new Color(51, 69, 41)
            ));
            setToolTipText("Play " + scene.getTitle());

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    fillColor = CARD_HOVER;
                    setBorder(BorderFactory.createBevelBorder(
                        BevelBorder.LOWERED,
                        new Color(205, 231, 191),
                        new Color(51, 69, 41)
                    ));
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    fillColor = CARD_GREEN;
                    setBorder(BorderFactory.createBevelBorder(
                        BevelBorder.RAISED,
                        new Color(184, 212, 168),
                        new Color(51, 69, 41)
                    ));
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    fillColor = CARD_PRESSED;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    fillColor = contains(e.getPoint()) ? CARD_HOVER : CARD_GREEN;
                    repaint();
                }
            });
        }

        private static ImageIcon createThumbnail(StoryScene scene) {
            BufferedImage source = ResourceCache.getSceneFrame(
                "story_thumb_" + scene.getSceneId(),
                scene.getThumbnailPath()
            );
            Image scaled = source.getScaledInstance(THUMB_WIDTH, THUMB_HEIGHT, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fillColor);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(223, 233, 218, 90));
            g2.drawRect(8, 8, getWidth() - 17, getHeight() - 17);
            g2.dispose();

            super.paintComponent(g);
        }
    }
}
