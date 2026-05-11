package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import engine.GamePanel;
import util.MethodUtilities;
import util.MethodUtilities.CustomButton;

/**
 * Owns the modal pause menu UI and delegates gameplay actions back to GamePanel.
 */
public class PauseMenu {
    private final GamePanel gamePanel;
    private final Runnable onResume;
    private final Runnable onHome;
    private JDialog dialog;

    /**
     * Stores the gameplay callbacks that the pause menu buttons should trigger.
     */
    public PauseMenu(GamePanel gamePanel, Runnable onResume, Runnable onHome) {
        this.gamePanel = gamePanel;
        this.onResume = onResume;
        this.onHome = onHome;
    }

    /**
     * Opens the pause dialog, creating it lazily the first time it is needed.
     */
    public void show() {
        if (dialog == null) {
            createDialog();
        }

        dialog.pack();
        dialog.setLocationRelativeTo(gamePanel);
        dialog.setVisible(true);
    }

    /**
     * Hides the pause dialog if it is currently visible.
     */
    public void hide() {
        if (dialog != null && dialog.isVisible()) {
            dialog.setVisible(false);
        }
    }

    /**
     * Builds the modal Swing dialog and wires its four buttons.
     */
    private void createDialog() {
        Window owner = SwingUtilities.getWindowAncestor(gamePanel);
        dialog = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(16, 16));
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        content.setBackground(Color.DARK_GRAY);

        JLabel title = new JLabel("Game Paused", JLabel.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(MethodUtilities.getFont(25f, gamePanel));
        content.add(title, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 12, 12));
        buttonPanel.setBackground(Color.DARK_GRAY);

        CustomButton resumeButton = new CustomButton("Resume");
        CustomButton homeButton = new CustomButton("Home");
        CustomButton settingsButton = new CustomButton("Settings");
        CustomButton mapButton = new CustomButton("Map");

        resumeButton.addActionListener(e -> onResume.run());
        homeButton.addActionListener(e -> confirmHomeExit());
        settingsButton.addActionListener(e -> JOptionPane.showMessageDialog(
            dialog,
            "Settings are coming soon.",
            "Settings",
            JOptionPane.INFORMATION_MESSAGE
        ));
        mapButton.addActionListener(e -> JOptionPane.showMessageDialog(
            dialog,
            String.format(
                "Player position: (%d, %d)\nMap preview not implemented yet.",
                gamePanel.getPlayer().worldX,
                gamePanel.getPlayer().worldY
            ),
            "Map",
            JOptionPane.INFORMATION_MESSAGE
        ));

        buttonPanel.add(resumeButton);
        buttonPanel.add(homeButton);
        buttonPanel.add(settingsButton);
        buttonPanel.add(mapButton);

        content.add(buttonPanel, BorderLayout.CENTER);
        dialog.setContentPane(content);
        dialog.pack();
    }

    /**
     * Confirms before leaving the current level and returning to the opening screen.
     */
    private void confirmHomeExit() {
        int choice = JOptionPane.showConfirmDialog(
            dialog,
            "Are you sure you want to exit?",
            "BACK TO HOME SCREEN",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            hide();
            onHome.run();
        }
    }
}
