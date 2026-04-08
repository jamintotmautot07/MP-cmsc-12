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

    public BaseFrame() {
        setTitle("Hawak ko ang Bit: The Final Bit");
        setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

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
            Object[] options = new Object[] {
                Level.TUTORIAL.name,
                Level.LEVEL_1.name,
                Level.LEVEL_2.name,
                Level.LEVEL_3.name
            };

            int choice = JOptionPane.showOptionDialog(
                this,
                "Choose a level:",
                "Select Level",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                0
            );

            if (choice >= 0 && choice < Level.LEVELS.length) {
                selectedLevel = Level.LEVELS[choice];
                openPanel.setSelectedLevelIndex(choice, selectedLevel.name);
            }
        });

        openPanel.playButton.addActionListener(e -> {
            gamePanel.setLevel(selectedLevel);
            cardLayout.show(container, "Game");
            gamePanel.requestFocusInWindow();
            gamePanel.startGameThread();
        });

        openPanel.creditsButton.addActionListener(e -> {
            cardLayout.show(container, "Credits");
            credits.startTimer();
        });

        gamePanel.backButton.addActionListener(e -> {

            int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to quit? ",
                "EXIT",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if(choice == JOptionPane.YES_OPTION) {
                cardLayout.show(container, "Openning");
                gamePanel.stopGameThread();
            }
        });

        addWindowListener(new MethodUtilities.exitAction(this));

        this.pack();
        setLocationRelativeTo(null);
    }

    public CreditScroller getCredits() {
        return credits;
    }
}
