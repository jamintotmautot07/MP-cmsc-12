
package systems;

/*
 OWNER: Thea

 PURPOSE:
 - Handles saving/loading

 TASKS:
 1. Create save file if missing
 2. Save:
    - highscore
    - introPlayed
 3. Load data safely

 NOTE:
 - Handle errors (no crash)

 OPTIONAL:
 - JSON format
*/
import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;

import exception.GameException;

public final class FileManager {
    // TODO: file read/write
    private static final String FILE_NAME = "save_data.txt";

    private FileManager() {
    }

    public static void createSaveFile() throws GameException {
        try {
            File file = new File(FILE_NAME);

            if(!file.exists()) {
                file.createNewFile();

                PrintWriter writer = new PrintWriter(file);
                writer.println("highscore=0");
                writer.println("introPlayed=false");
                writer.close();
            }
        } catch(IOException e) {
            throw new GameException("Unable to create save file.");
        }
    }

    public static void saveData(int highScore, boolean introPlayed) throws GameException {
        try {
            PrintWriter writer = new PrintWriter(FILE_NAME);

            writer.println("highscore=" + highScore);
            writer.println("introPlayed=" + introPlayed);

            writer.close();
        } catch(IOException e) {
            throw new GameException("Unable to save game data.");
        }
    }
}
