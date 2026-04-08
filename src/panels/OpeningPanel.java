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
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;

import util.Constants;
import util.MethodUtilities;
import util.MethodUtilities.GlowLabel;

public class OpeningPanel extends JPanel {

    //background of this panel
    private Image backgroundImage;

    private JPanel main;
    private TitlePanel centerPanel;
    private JPanel header;
    private JPanel instructions;
    private TitlePanel titlePanel;

    private MethodUtilities.GlowLabel headerLabel1;
    private MethodUtilities.GlowLabel headerLabel2;
    private JLabel instructionsLabel;
    private JLabel levelSelectionLabel;
    
    public JButton playButton;
    public JButton exitButton;
    public JButton creditsButton;
    public JButton levelButton;
    private int selectedLevelIndex = 0;

    public OpeningPanel() {
        setPreferredSize(new Dimension(Constants.screenWidth, Constants.screenHeight));
        this.backgroundImage = new ImageIcon("res/background.png").getImage();

        setLayout(new BorderLayout());

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
        playButton = new JButton("PLAY");
        exitButton = new JButton("EXIT");
        creditsButton = new JButton("CREDITS");
        levelButton = new JButton("LEVELS");
        playButton.setFocusPainted(false);
        exitButton.setFocusPainted(false);
        creditsButton.setFocusPainted(false);
        levelButton.setFocusPainted(false);

        instructionsLabel = new JLabel("\"The system is failing... but you're still running.\"");
        instructionsLabel.setFont(
            new Font("Papyrus", Font.BOLD, 18)
        );
        instructionsLabel.setForeground(Color.WHITE);

        levelSelectionLabel = new JLabel("Selected: Tutorial");
        levelSelectionLabel.setFont(new Font("Papyrus", Font.BOLD, 14));
        levelSelectionLabel.setForeground(Color.WHITE);

        main = new JPanel();
        main.setBackground(null);
        main.setOpaque(false);

        titlePanel = new TitlePanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        centerPanel = new TitlePanel(new GridLayout(3, 1, 10, 10));
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
        instructions.add(levelSelectionLabel, BorderLayout.CENTER);

        JPanel levelPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        levelPanel.setOpaque(false);
        levelPanel.add(levelButton);

        southPanel.add(instructions, BorderLayout.CENTER);
        southPanel.add(levelPanel, BorderLayout.EAST);

        titlePanel.add(headerLabel1);

        // space in-between texts
        titlePanel.add(Box.createRigidArea(new Dimension(0, -15)));

        titlePanel.add(headerLabel2);

        header.add(titlePanel);

        centerPanel.add(playButton);
        centerPanel.add(creditsButton);
        centerPanel.add(exitButton);

        main.setLayout(new GridBagLayout());
        main.add(centerPanel);

        add(main, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);
        add(southPanel, BorderLayout.SOUTH);
    }

    //overriding the default paint component
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        //for the background
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }

    //custom panel for effects
    public class TitlePanel extends JPanel {
        private Image backImage;

        public TitlePanel () {
            this.backImage = new ImageIcon("res/text_background.png").getImage();
            setBackground(null);
            setOpaque(false);
        }

        public TitlePanel (LayoutManager layout) {
            this.backImage = new ImageIcon("res/text_background.png").getImage();
            setBackground(null);
            setOpaque(false);
            setLayout(layout);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.drawImage(backImage, 0, 0, this.getWidth() + 25, this.getHeight() + 25, this);
        }

    }

    public int getSelectedLevelIndex() {
        return selectedLevelIndex;
    }

    public void setSelectedLevelIndex(int selectedLevelIndex, String levelName) {
        this.selectedLevelIndex = selectedLevelIndex;
        if (levelSelectionLabel != null) {
            levelSelectionLabel.setText("Selected: " + levelName);
        }
    }

    //custom panels for the buttons
    public class RoundedPanel extends JPanel {
        private int radius;

        public RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        public RoundedPanel(LayoutManager layout, int radius) {
            super(layout);
            this.radius = radius;
            setOpaque(false); 
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D)g;

            //makes the drawing smooth
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getBackground());

            //fill the entire panel with the rounded rect
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            //make a line border
            g2.setColor(Color.BLACK);
            g2.drawRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        }
    }
}
