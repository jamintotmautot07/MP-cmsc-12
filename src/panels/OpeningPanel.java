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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import util.Constants;

public class OpeningPanel extends JPanel {

    //background of this panel
    private Image backgroundImage;

    private JPanel main;
    private RoundedPanel centerPanel;
    private JPanel header;
    private JPanel instructions;
    private TitlePanel titlePanel;

    private JLabel headerLabel1;
    private JLabel headerLabel2;
    private JLabel instructionsLabel;
    
    public JButton playButton;
    public JButton exitButton;
    public JButton creditsButton;

    public OpeningPanel() {
        setPreferredSize(new Dimension(Constants.screenWidth, Constants.screenHeight));
        this.backgroundImage = new ImageIcon("src/res/Circuito.jpg").getImage();

        setLayout(new BorderLayout());

        //Fonts
        // Font titleUpperFont = new Font("Brush Script MT", Font.ITALIC, 20);
        // Font titleLowerFont = new Font("Papyrus", Font.BOLD, 35);

        headerLabel1 = new JLabel(String.format("Hawak ko ang Bit:"));
        headerLabel2 = new JLabel(String.format("THE FINAL BIT"));

        try {
            Font titleLowerFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/Font/TITLE_FONT.ttf"));
            Font titleUpperFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/Font/TopTitle_Font.ttf"));

            //set sizes
            titleLowerFont = titleLowerFont.deriveFont(Font.BOLD, 100f);
            titleUpperFont = titleUpperFont.deriveFont(Font.BOLD, 35f);

            headerLabel1.setForeground(Color.BLUE);
            headerLabel1.setFont(titleUpperFont);
            headerLabel1.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            headerLabel2.setForeground(Color.green.darker());
            headerLabel2.setFont(titleLowerFont);
            headerLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //buttons
        playButton = new JButton("PLAY");
        exitButton = new JButton("EXIT");
        creditsButton = new JButton("CREDITS");
        playButton.setFocusPainted(false);
        exitButton.setFocusPainted(false);
        creditsButton.setFocusPainted(false);

        instructionsLabel = new JLabel("\"The system is failing... but you're still running.\"");
        instructionsLabel.setFont(
            new Font("Papyrus", Font.BOLD, 18)
        );
        instructionsLabel.setForeground(Color.WHITE);

        main = new JPanel();
        main.setBackground(null);
        main.setOpaque(false);

        titlePanel = new TitlePanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        centerPanel = new RoundedPanel(new GridLayout(3, 1, 20, 20), 20);
        centerPanel.setPreferredSize(new Dimension(280, 220));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        header = new JPanel();
        header.setBackground(null);
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(25, 0, 20, 0));

        instructions =  new JPanel();
        instructions.setOpaque(false);
        instructions.setBorder(BorderFactory.createEmptyBorder(20, 10, 35, 10));

        titlePanel.add(headerLabel1);

        // space in-between texts
        titlePanel.add(Box.createRigidArea(new Dimension(0, -40)));

        titlePanel.add(headerLabel2);

        header.add(titlePanel);

        instructions.add(instructionsLabel);

        centerPanel.add(playButton);
        centerPanel.add(creditsButton);
        centerPanel.add(exitButton);

        main.setLayout(new GridBagLayout());
        main.add(centerPanel);

        add(main, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);
        add(instructions, BorderLayout.SOUTH);
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
            this.backImage = new ImageIcon("src/res/backdrop.jpg").getImage();
            setBackground(null);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.drawImage(backImage, 0, 0, this.getWidth(), this.getHeight() + 10, this);
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
