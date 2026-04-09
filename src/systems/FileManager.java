
package systems;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/*
 OWNER: Thea

 PURPOSE:
 - Handles saving/loading

 TASKS:
 1. Create save file if missing
 2. Save:
    - maxLevelReached
    - tutorialPlayed
 3. Load data safely

 NOTE:
 - Handle errors (no crash)

 OPTIONAL:
 - JSON format
*/

public class FileManager {
    private static final String SAVE_FILE = "res/save.txt";

    // This is a sample code, you can either reuse this or have a similar structure...
    // public static void saveProgress(int maxLevelReached, boolean tutorialPlayed) {
    //     // Placeholder for saving progress
    //     // try (BufferedWriter writer = new BufferedWriter(new FileWriter(SAVE_FILE))) {
    //     //     writer.write(maxLevelReached + "\n");
    //     //     writer.write(tutorialPlayed ? "1" : "0");
    //     // } catch (IOException e) {
    //     //     e.printStackTrace();
    //     // }
    // }

    // public static int loadMaxLevelReached() {
    //     // Placeholder for loading max level reached
    //     // try (BufferedReader reader = new BufferedReader(new FileReader(SAVE_FILE))) {
    //     //     String line = reader.readLine();
    //     //     return Integer.parseInt(line.trim());
    //     // } catch (Exception e) {
    //     //     return 0; // default
    //     // }
    //     return 0; // default
    // }

    // public static boolean loadTutorialPlayed() {
    //     // Placeholder for loading tutorial played
    //     // try (BufferedReader reader = new BufferedReader(new FileReader(SAVE_FILE))) {
    //     //     reader.readLine(); // skip maxLevel
    //     //     String line = reader.readLine();
    //     //     return "1".equals(line.trim());
    //     // } catch (Exception e) {
    //     //     return false; // default
    //     // }
    //     return false; // default
    // }
}
