
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
    private static final int FINAL_STORY_PROGRESS_INDEX = FINAL_LEVEL_INDEX + 1;

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
                    writer.println("level0Time=0");
                    writer.println("level1Time=0");
                    writer.println("level2Time=0");
                    writer.println("level3Time=0");
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
            writer.println("maxLevelReached=" + clampProgress(maxLevelReached));
            writer.println("selectedLevel=" + clampLevel(selectedLevel));
            writeLevelTimes(writer, loadLevelTimes());

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

    public static void saveLevelTime(int levelIndex, int elapsedSeconds) throws GameException {
        int[] levelTimes = loadLevelTimes();
        levelTimes[clampLevel(levelIndex)] = Math.max(0, elapsedSeconds);

        try(PrintWriter writer = new PrintWriter(FILE_NAME)) {
            writer.println("highscore=" + loadHighScore());
            writer.println("tutorialPlayed=" + loadTutorialPlayed());
            writer.println("maxLevelReached=" + loadMaxLevelReached());
            writer.println("selectedLevel=" + loadSelectedLevel());
            writeLevelTimes(writer, levelTimes);

        } catch(IOException e) {
            throw new GameException("Unable to save level time.");
        }
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
        return clampProgress(loadInt("maxLevelReached", TUTORIAL_INDEX));
    }

    public static int loadLevelTime(int levelIndex) throws GameException {
        return loadInt("level" + clampLevel(levelIndex) + "Time", 0);
    }

    public static int[] loadLevelTimes() throws GameException {
        int[] levelTimes = new int[FINAL_LEVEL_INDEX + 1];

        for(int i = TUTORIAL_INDEX; i <= FINAL_LEVEL_INDEX; i++) {
            levelTimes[i] = loadLevelTime(i);
        }

        return levelTimes;
    }

    public static int loadTotalLevelTime() throws GameException {
        int total = 0;
        int[] levelTimes = loadLevelTimes();

        for(int time : levelTimes) {
            total += time;
        }

        return total;
    }

    public static String formatTime(int totalSeconds) {
        int safeSeconds = Math.max(0, totalSeconds);
        int minutes = safeSeconds / 60;
        int seconds = safeSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
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

    private static void writeLevelTimes(PrintWriter writer, int[] levelTimes) {
        for(int i = TUTORIAL_INDEX; i <= FINAL_LEVEL_INDEX; i++) {
            writer.println("level" + i + "Time=" + Math.max(0, levelTimes[i]));
        }
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

    private static int clampProgress(int level) {
        if(level < TUTORIAL_INDEX) {
            return TUTORIAL_INDEX;
        }
        if(level > FINAL_STORY_PROGRESS_INDEX) {
            return FINAL_STORY_PROGRESS_INDEX;
        }
        return level;
    }
}

