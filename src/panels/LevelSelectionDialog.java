package panels;

import engine.Level;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import util.MethodUtilities;
import util.MethodUtilities.CustomButton;

public class LevelSelectionDialog extends JPanel {

    public Level selected = null;
    private CustomButton[] levelButtons;

    //Display names for each level
    private final String[] levelDisplayNames = {
        "Tutorial Sector",
        "Boot Sector - Level 1",
        "Memory Core - Level 2",
        "System Kernel - Level 3 (BOSS)"
    };

    //Listener for navigation
    public interface LevelSelectListener {
        void onLevelSelected(Level level);
        void onBack();
    }

    private LevelSelectListener listener;

    public void setListener(LevelSelectListener listener) {
        this.listener = listener;
    }

    public LevelSelectionDialog(int maxLevelReached) {

        setLayout(new BorderLayout());

        //LEVEL BUTTON GRID
        MethodUtilities.RoundedPanel panel = new MethodUtilities.RoundedPanel(
                new GridLayout(Level.LEVELS.length, 1, 10, 10), 15
        );

        panel.setColor(new Color(159, 188, 143).darker());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        levelButtons = new CustomButton[Level.LEVELS.length];

        for (int i = 0; i < Level.LEVELS.length; i++) {

            String label;

            //Custom names with fallback safety
            if (i < levelDisplayNames.length) {
                label = levelDisplayNames[i];
            } else {
                label = Level.LEVELS[i].name;
            }

            levelButtons[i] = new CustomButton(label);

            //Lock system
            levelButtons[i].setEnabled(i <= maxLevelReached);

            final int index = i;

            //Level selection
            levelButtons[i].addActionListener(e -> {
                selected = Level.LEVELS[index];

                if (listener != null) {
                    listener.onLevelSelected(selected);
                }
            });

            panel.add(levelButtons[i]);
        }

        //BACK BUTTON
        CustomButton backButton = new CustomButton("Back");

        backButton.addActionListener(e -> {
            if (listener != null) {
                listener.onBack();
            }
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(backButton);

        //Layout placement
        add(panel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}