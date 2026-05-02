package systems;

import exception.GameException;

public class TestFileManager {

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
