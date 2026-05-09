
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

    private FileManager() {
    }

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

    public static void saveData(int highScore, boolean tutorialPlayed, int maxLevelReached, int selectedLevel) throws GameException {
        createSaveFile();
        writeSaveFile(highScore, tutorialPlayed, maxLevelReached, selectedLevel);
    }

    // for type of saving method that is the last level reached
    public static void saveData(int highScore, boolean tutorialPlayed, int selectedLevel) throws GameException {
        saveData(highScore, tutorialPlayed, selectedLevel, selectedLevel);
    }

    // for type of saving method that assumes that the player is in either level 1 or tutorial
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
        return clampProgress(loadInt("maxLevelReached", TUTORIAL_INDEX));
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

    private static void migrateLegacySaveFile(Path saveFile) throws IOException {
        Path legacySaveFile = Paths.get(FILE_NAME);

        if(Files.isRegularFile(legacySaveFile)) {
            Files.copy(legacySaveFile, saveFile);
        }
    }

    private static Path getSaveFilePath() {
        return getSaveDirectory().resolve(FILE_NAME);
    }

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

