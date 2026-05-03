
package systems;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
    private static final String FILE_NAME = "save_data.txt";
    private static final int TUTORIAL_INDEX = 0;
    private static final int FINAL_LEVEL_INDEX = 3;

    private FileManager() {
    }

    public static void createSaveFile() throws GameException {
        try {
            File file = new File(FILE_NAME);

            if(!file.exists()) {
                try(PrintWriter writer = new PrintWriter(file)) {
                    writer.println("highscore=0");
                    writer.println("tutorialPlayed=false");
                    writer.println("maxLevelReached=0");
                    writer.println("selectedLevel=0");
                }
            }
        } catch(IOException e) {
            throw new GameException("Unable to create save file.");
        }
    }

    public static void saveData(int highScore, boolean tutorialPlayed, int maxLevelReached, int selectedLevel) throws GameException {
        try(PrintWriter writer = new PrintWriter(FILE_NAME)) {
            writer.println("highscore=" + highScore);
            writer.println("tutorialPlayed=" + tutorialPlayed);
            writer.println("maxLevelReached=" + clampLevel(maxLevelReached));
            writer.println("selectedLevel=" + clampLevel(selectedLevel));

        } catch(IOException e) {
            throw new GameException("Unable to save game data.");
        }
    }

    public static void saveData(int highScore, boolean tutorialPlayed, int selectedLevel) throws GameException {
        saveData(highScore, tutorialPlayed, selectedLevel, selectedLevel);
    }

    public static void saveData(int highScore, boolean tutorialPlayed) throws GameException {
        int level = tutorialPlayed ? 1 : TUTORIAL_INDEX;
        saveData(highScore, tutorialPlayed, level, level);
    }

    public static void saveProgress(int maxLevelReached, boolean tutorialPlayed, int selectedLevel) throws GameException {
        saveData(loadHighScore(), tutorialPlayed, maxLevelReached, selectedLevel);
    }

    public static int loadHighScore() throws GameException {
        return loadInt("highscore", 0);
    }

    public static boolean loadIntroPlayed() throws GameException {
        return loadTutorialPlayed();
    }

    public static boolean loadTutorialPlayed() throws GameException {
        createSaveFile();

        try {
            Map<String, String> values = loadValues();
            return Boolean.parseBoolean(values.getOrDefault("tutorialPlayed", values.getOrDefault("introPlayed", "false")));
        } catch(GameException e) {
            throw new GameException("Unable to load intro status.");
        }
    }

    public static int loadSelectedLevel() throws GameException {
        return clampLevel(loadInt("selectedLevel", TUTORIAL_INDEX));
    }

    public static int loadMaxLevelReached() throws GameException {
        return clampLevel(loadInt("maxLevelReached", TUTORIAL_INDEX));
    }

    private static int loadInt(String key, int defaultValue) throws GameException {
        createSaveFile();

        try {
            String value = loadValues().get(key);
            if(value == null) {
                return defaultValue;
            }
            return Integer.parseInt(value);
        } catch(NumberFormatException e) {
            return defaultValue;
        }
    }

    private static Map<String, String> loadValues() throws GameException {
        Map<String, String> values = new HashMap<>();

        try(BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;

            while((line = reader.readLine()) != null) {
                int separator = line.indexOf('=');
                if(separator > 0 && separator < line.length() - 1) {
                    values.put(line.substring(0, separator).trim(), line.substring(separator + 1).trim());
                }
            }
        } catch(IOException e) {
            throw new GameException("Unable to load save file.");
        }

        return values;
    }

    private static int clampLevel(int level) {
        if(level < TUTORIAL_INDEX) {
            return TUTORIAL_INDEX;
        } 
        if(level > FINAL_LEVEL_INDEX) {
            return FINAL_LEVEL_INDEX;
        }
        return level;
    }
}

