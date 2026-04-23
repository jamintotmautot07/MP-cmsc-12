package panels;

import javax.swing.JDialog;
import javax.swing.BorderFactory;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import engine.Level;
import util.MethodUtilities;
import util.MethodUtilities.CustomButton;

public class LevelSelectionDialog extends JDialog {

    public Level selected = null;
    private CustomButton[] levelButtons;

    public LevelSelectionDialog(java.awt.Frame parent, int maxLevelReached) {
        super(parent, "Select Level", true);
        setSize(300, 250);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        MethodUtilities.RoundedPanel panel = new MethodUtilities.RoundedPanel(new GridLayout(Level.LEVELS.length, 1, 10, 10), 15);
        panel.setColor(new Color(159, 188, 143).darker());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        levelButtons = new CustomButton[Level.LEVELS.length];

        for (int i = 0; i < Level.LEVELS.length; i++) {
            levelButtons[i] = new CustomButton(Level.LEVELS[i].name);
            // levelButtons[i].setEnabled(i <= maxLevelReached);
            levelButtons[i].setEnabled(true);
            final int index = i;
            levelButtons[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selected = Level.LEVELS[index];
                    setVisible(false);
                }
            });
            panel.add(levelButtons[i]);
        }

        add(panel, BorderLayout.CENTER);
    }
}