package systems;

import exception.GameException;

/**
 * Manual smoke-test entry point for save-file behavior during development.
 */
public class TestFileManager {

    /**
     * Runs a tiny manual save-file smoke test from the command line.
     */
    public static void main(String[] args) {

        try {
            // Step 1: create file
            FileManager.createSaveFile();

            // Step 2: save data
            FileManager.saveData(999, true);


        } catch (GameException e) {
        }
    }
}
