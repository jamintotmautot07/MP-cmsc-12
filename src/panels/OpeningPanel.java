package panels;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.Box;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.io.FileInputStream;
import java.io.InputStream;

import util.Constants;
import util.MethodUtilities;
import util.MethodUtilities.CustomButton;
import util.MethodUtilities.GlowLabel;
import util.MethodUtilities.RoundedPanel;
// import util.ResourceCache; // COMMENTED OUT - Cache system disabled

public class OpeningPanel extends JPanel {

    //background of this panel
    private Image backgroundImage;

    // Animated background for this panel
    private Image[] backgroundFrames;
    private int currentBackgroundFrame;
    private javax.swing.Timer backgroundTimer;

    private JPanel main;
    private MethodUtilities.RoundedPanel centerPanel;
    private JPanel header;
    private JPanel instructions;
    private MethodUtilities.RoundedPanel titlePanel;

    private MethodUtilities.GlowLabel headerLabel1;
    private MethodUtilities.GlowLabel headerLabel2;
    private JLabel instructionsLabel;
    
    public CustomButton playButton;
    public CustomButton exitButton;
    public CustomButton creditsButton;
    public CustomButton levelButton;
    public CustomButton scoreButton;
    public CustomButton continueButton;
    public CustomButton cutScenesButton;
    private int selectedLevelIndex = 0;

    public OpeningPanel() {
        setPreferredSize(new Dimension(Constants.screenWidth, Constants.screenHeight));
        this.backgroundImage = new ImageIcon("res/background.png").getImage();

        setLayout(new BorderLayout());
        loadBackgroundFrames();
        startBackgroundAnimation();

        //Fonts
        // Font titleUpperFont = new Font("Brush Script MT", Font.ITALIC, 20);
        // Font titleLowerFont = new Font("Papyrus", Font.BOLD, 35);

        headerLabel1 = new GlowLabel(String.format("Hawak ko ang Bit:"));
        headerLabel2 = new GlowLabel(String.format("THE FINAL BIT"), new Color(255, 153, 51));

        try (InputStream titleLowerStream = new FileInputStream("res/Font/Those_Glitch_Regular.ttf");
             InputStream titleUpperStream = new FileInputStream("res/Font/TopTitle_Font.ttf")) {
            Font titleLowerFont = Font.createFont(Font.TRUETYPE_FONT, titleLowerStream);
            Font titleUpperFont = Font.createFont(Font.TRUETYPE_FONT, titleUpperStream);

            //set sizes
            titleLowerFont = titleLowerFont.deriveFont(Font.BOLD, 50f);
            titleUpperFont = titleUpperFont.deriveFont(Font.BOLD, 35f);

            headerLabel1.setForeground(new Color(153, 204, 255));
            headerLabel1.setFont(titleUpperFont);
            headerLabel1.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            headerLabel2.setForeground(new Color(255, 51, 0));
            headerLabel2.setFont(titleLowerFont);
            headerLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //buttons
        playButton = new CustomButton("PLAY");
        exitButton = new CustomButton("EXIT");
        creditsButton = new CustomButton("CREDITS");
        levelButton = new CustomButton("LEVELS");
        scoreButton = new CustomButton("SCORES");
        continueButton = new CustomButton("CONTINUE");
        cutScenesButton = new CustomButton("STORY");
        playButton.setFocusPainted(false);
        exitButton.setFocusPainted(false);
        creditsButton.setFocusPainted(false);
        levelButton.setFocusPainted(false);
        scoreButton.setFocusPainted(false);
        continueButton.setFocusPainted(false);
        cutScenesButton.setFocusPainted(false);

        instructionsLabel = new JLabel("\"The system is failing... but you're still running.\"");
        instructionsLabel.setFont(
            new Font("Papyrus", Font.BOLD, 18)
        );
        instructionsLabel.setForeground(Color.WHITE);

        main = new JPanel();
        main.setBackground(null);
        main.setOpaque(false);

        titlePanel = new RoundedPanel(15);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10,12));

        centerPanel = new RoundedPanel(new GridLayout(4, 1, 10, 10), 15);
        centerPanel.setPreferredSize(new Dimension(280, 220));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        header = new JPanel();
        header.setBackground(null);
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(25, 0, 20, 0));

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);
        southPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 35, 10));

        instructions = new JPanel(new BorderLayout());
        instructions.setOpaque(false);
        instructions.add(instructionsLabel, BorderLayout.WEST);

        JPanel levelPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        levelPanel.setOpaque(false);
        levelPanel.add(cutScenesButton);
        levelPanel.add(scoreButton);
        levelPanel.add(levelButton);

        southPanel.add(instructions, BorderLayout.CENTER);
        southPanel.add(levelPanel, BorderLayout.EAST);

        titlePanel.add(headerLabel1);

        // space in-between texts
        titlePanel.add(Box.createRigidArea(new Dimension(0, -15)));

        titlePanel.add(headerLabel2);

        header.add(titlePanel);

        centerPanel.add(continueButton);
        centerPanel.add(playButton);
        centerPanel.add(creditsButton);
        centerPanel.add(exitButton);

        main.setLayout(new GridBagLayout());
        main.add(centerPanel);

        add(main, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);
        add(southPanel, BorderLayout.SOUTH);
    }

    // method for loading the frames of the background animation
    private void loadBackgroundFrames() {
        int frameCount = 22;
        backgroundFrames = new Image[frameCount];
        for(int i = 0; i < frameCount; i++) {
            backgroundFrames[i] = new ImageIcon(String.format("res/BackGroundSeq/frame%04d.png", i)).getImage();
        }
    }

    // method for starting background animation
    public void startBackgroundAnimation() {
        backgroundTimer = new javax.swing.Timer(100, e -> {
            currentBackgroundFrame = (currentBackgroundFrame + 1) % backgroundFrames.length; // this makes sure it loops forever
            repaint();
        });
        backgroundTimer.start();
    }

    public void stopBackgroundAnimation() {
        if(backgroundTimer != null && backgroundTimer.isRunning()) {
            this.backgroundTimer.stop();
        }
    }

    //overriding the default paint component
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        //for the background
        if(backgroundFrames != null && backgroundFrames.length > 0) {
            g.drawImage(backgroundFrames[currentBackgroundFrame], 0, 0, getWidth(), getHeight(), this);
        } else {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);           
        }
    }

    public int getSelectedLevelIndex() {
        return selectedLevelIndex;
    }

    public void setContinueVisible(boolean visible) {
        continueButton.setVisible(visible);
    }

    public void setSelectedLevelIndex(int selectedLevelIndex, String levelName) {
        this.selectedLevelIndex = selectedLevelIndex;
    }
}
