
package main;

/*
 OWNER: Jamin (Benjamin)

 PURPOSE:
 - Entry point of the game
 - Creates window and starts everything

 TASKS:
 1. Create JFrame
 2. Add GamePanel
 3. Set size using Constants
 4. Make visible
 5. Start game thread

 NOTE:
 - Do NOT put game logic here
*/

import javax.swing.SwingUtilities;

//main class that calls the other operations

public class GameLauncher {
    public static void main(String[] args) {
        // Create JFrame
        // Add GamePanel
        // Set size, close operation
        // Start game

        SwingUtilities.invokeLater(() -> {
            BaseFrame frame = new BaseFrame();
            frame.setVisible(true);
        });
    }
}
