
package systems;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.IOException;

import exception.GameException;

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

public final class FileManager {
    // TO DO: file read/write
    private static final String FILE_NAME = "save_data.txt";

    private FileManager() {
    }

    public static void createSaveFile() throws GameException {
        try {
            File file = new File(FILE_NAME);

            if(!file.exists()) {
                try(PrintWriter writer = new PrintWriter(file)) {
                    writer.println("highscore=0");
                    writer.println("introPlayed=false");
                }
            }
        } catch(IOException e) {
            throw new GameException("Unable to create save file.");
        }
    }

    public static void saveData(int highScore, boolean introPlayed) throws GameException {
        try(PrintWriter writer = new PrintWriter(FILE_NAME)) {
            writer.println("highscore=" + highScore);
            writer.println("introPlayed=" + introPlayed);

        } catch(IOException e) {
            throw new GameException("Unable to save game data.");
        }
    }

    public static int loadHighScore() throws GameException {
        createSaveFile();

        try(BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;

            while((line = reader.readLine()) != null) {
                if(line.startsWith("highscore=")) {
                    reader.close();
                    return Integer.parseInt(line.split("=")[1]);
                }
            }

        } catch(IOException exception) {
            throw new GameException("Unable to load high score.");
        }

        return 0;
    }

    public static boolean loadIntroPlayed() throws GameException {
        createSaveFile();

        try(BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;

            while((line = reader.readLine()) != null) {
                if(line.startsWith("introPlayed=")) {
                    reader.close();
                    return Boolean.parseBoolean(line.split("=")[1]);
                }
            }

        } catch(IOException exception) {
            throw new GameException("Unable to load intro status.");
        }

        return false;
    }
}

