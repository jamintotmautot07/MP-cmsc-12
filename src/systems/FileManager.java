
package systems;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

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
    private static final String APP_DIRECTORY_NAME = "HawakKoAngBit";
    private static final String UNIX_APP_DIRECTORY_NAME = "hawak-ko-ang-bit";
    private static final String FILE_NAME = "save_data.txt";
    private static final int TUTORIAL_INDEX = 0;
    private static final int FINAL_LEVEL_INDEX = 3;
    private static final int FINAL_STORY_PROGRESS_INDEX = FINAL_LEVEL_INDEX + 1;

    /**
     * Prevents creating FileManager objects because all save helpers are static utility methods.
     */
    private FileManager() {
    }

    /**
     * Ensures the platform-specific save file exists and has a valid parent directory.
     */
    public static void createSaveFile() throws GameException {
        try {
            Path saveFile = getSaveFilePath();
            Files.createDirectories(saveFile.getParent());

            if(Files.exists(saveFile)) {
                if(Files.isDirectory(saveFile)) {
                    throw new GameException("Save path is a folder instead of a file: " + saveFile);
                }
                return;
            }

            migrateLegacySaveFile(saveFile);

            if(!Files.exists(saveFile)) {
                writeSaveFile(0, false, TUTORIAL_INDEX, TUTORIAL_INDEX);
            }
        } catch(IOException e) {
            throw new GameException("Unable to create save file.");
        }
    }

    /**
     * Saves the complete progress record.
     */
    public static void saveData(int highScore, boolean tutorialPlayed, int maxLevelReached, int selectedLevel) throws GameException {
        createSaveFile();
        writeSaveFile(highScore, tutorialPlayed, maxLevelReached, selectedLevel);
    }

    // for type of saving method that is the last level reached
    /**
     * Saves progress when the selected level is also the maximum unlocked level.
     */
    public static void saveData(int highScore, boolean tutorialPlayed, int selectedLevel) throws GameException {
        saveData(highScore, tutorialPlayed, selectedLevel, selectedLevel);
    }

    // for type of saving method that assumes that the player is in either level 1 or tutorial
    /**
     * Legacy overload used by older code that only knew high score and tutorial status.
     */
    public static void saveData(int highScore, boolean tutorialPlayed) throws GameException {
        int level = tutorialPlayed ? 1 : TUTORIAL_INDEX;
        saveData(highScore, tutorialPlayed, level, level);
    }

    /**
     * Saves level progress while preserving the current high score value.
     */
    public static void saveProgress(int maxLevelReached, boolean tutorialPlayed, int selectedLevel) throws GameException {
        saveData(loadHighScore(), tutorialPlayed, maxLevelReached, selectedLevel);
    }

    /**
     * Loads the saved high score, defaulting to zero if the value is absent or invalid.
     */
    public static int loadHighScore() throws GameException {
        return loadInt("highscore", 0);
    }

    /**
     * Legacy name kept for compatibility; intro progress now maps to tutorial progress.
     */
    public static boolean loadIntroPlayed() throws GameException {
        return loadTutorialPlayed();
    }

    /**
     * Loads whether the tutorial has been completed before.
     */
    public static boolean loadTutorialPlayed() throws GameException {
        createSaveFile();

        try {
            Map<String, String> values = loadValues();
            return Boolean.parseBoolean(values.getOrDefault("tutorialPlayed", values.getOrDefault("introPlayed", "false")));
        } catch(GameException e) {
            throw new GameException("Unable to load intro status.");
        }
    }

    /**
     * Loads the menu-selected level and clamps it to a valid playable level index.
     */
    public static int loadSelectedLevel() throws GameException {
        return clampLevel(loadInt("selectedLevel", TUTORIAL_INDEX));
    }

    /**
     * Loads the highest unlocked progress point.
     */
    public static int loadMaxLevelReached() throws GameException {
        return clampProgress(loadInt("maxLevelReached", TUTORIAL_INDEX));
    }

    /**
     * Loads one integer value from the save file with a fallback when parsing fails.
     */
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

    /**
     * Reads the save file as Java Properties and exposes the values as a simple map.
     */
    private static Map<String, String> loadValues() throws GameException {
        Map<String, String> values = new HashMap<>();

        try(BufferedReader reader = Files.newBufferedReader(getSaveFilePath(), StandardCharsets.UTF_8)) {
            Properties properties = new Properties();
            properties.load(reader);

            for(String key : properties.stringPropertyNames()) {
                values.put(key, properties.getProperty(key));
            }
        } catch(IOException | IllegalArgumentException e) {
            throw new GameException("Unable to load save file.");
        }

        return values;
    }

    /**
     * Writes the complete save file atomically from the values passed by the caller.
     */
    private static void writeSaveFile(int highScore, boolean tutorialPlayed, int maxLevelReached, int selectedLevel) throws GameException {
        try {
            Path saveFile = getSaveFilePath();
            Files.createDirectories(saveFile.getParent());

            Properties values = new Properties();
            values.setProperty("highscore", Integer.toString(highScore));
            values.setProperty("tutorialPlayed", Boolean.toString(tutorialPlayed));
            values.setProperty("maxLevelReached", Integer.toString(clampProgress(maxLevelReached)));
            values.setProperty("selectedLevel", Integer.toString(clampLevel(selectedLevel)));

            try(BufferedWriter writer = Files.newBufferedWriter(saveFile, StandardCharsets.UTF_8)) {
                values.store(writer, "Hawak Ko Ang Bit save data");
            }
        } catch(IOException e) {
            throw new GameException("Unable to save game data.");
        }
    }

    /**
     * Copies an old root-level save file into the new platform-specific save directory if one exists.
     */
    private static void migrateLegacySaveFile(Path saveFile) throws IOException {
        Path legacySaveFile = Paths.get(FILE_NAME);

        if(Files.isRegularFile(legacySaveFile)) {
            Files.copy(legacySaveFile, saveFile);
        }
    }

    /**
     * Returns the full path to the save data file.
     */
    private static Path getSaveFilePath() {
        return getSaveDirectory().resolve(FILE_NAME);
    }

    /**
     * Chooses a normal app-data directory for Windows, macOS, or Linux.
     */
    private static Path getSaveDirectory() {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String userHome = System.getProperty("user.home", ".");

        if(osName.contains("win")) {
            String appData = System.getenv("APPDATA");
            if(appData != null && !appData.trim().isEmpty()) {
                return Paths.get(appData, APP_DIRECTORY_NAME);
            }
            return Paths.get(userHome, "AppData", "Roaming", APP_DIRECTORY_NAME);
        }

        if(osName.contains("mac")) {
            return Paths.get(userHome, "Library", "Application Support", APP_DIRECTORY_NAME);
        }

        String xdgDataHome = System.getenv("XDG_DATA_HOME");
        if(xdgDataHome != null && !xdgDataHome.trim().isEmpty()) {
            return Paths.get(xdgDataHome, UNIX_APP_DIRECTORY_NAME);
        }
        return Paths.get(userHome, ".local", "share", UNIX_APP_DIRECTORY_NAME);
    }

    /**
     * Keeps a selected playable level within the valid LEVELS array range.
     */
    private static int clampLevel(int level) {
        if(level < TUTORIAL_INDEX) {
            return TUTORIAL_INDEX;
        } 
        if(level > FINAL_LEVEL_INDEX) {
            return FINAL_LEVEL_INDEX;
        }
        return level;
    }

    /**
     * Keeps story/progression unlock values within the valid range, including the ending unlock slot.
     */
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

