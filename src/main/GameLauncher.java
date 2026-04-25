
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

/**
 * Application entry point.
 * Keeps startup logic minimal so the rest of the program stays inside dedicated classes.
 */
public class GameLauncher {
    public static void main(String[] args) {
        // Optional command-line hook for opening a save file or passing future startup arguments.

        if (args.length > 0) {
            String filePath = args[0];
            System.out.println("Opening save file: " + filePath);

            // TODO: load save file here
        }

        // Handy startup log so the running build is obvious in the console.
        System.out.println("VERSION: UPDATED BUILD");

        SwingUtilities.invokeLater(() -> {
            // Swing UI creation should happen on the Event Dispatch Thread.
            BaseFrame frame = new BaseFrame();
            frame.setVisible(true);
        });
    }
}
