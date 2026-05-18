package util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/*
 PURPOSE:
 - Centralized resource caching system
 - Prevents repeated file I/O and improves performance
 - All resources loaded once, then retrieved from memory

 KEY BENEFITS:
 - Eliminates lag during gameplay
 - Reduces EDT blocking
 - Single source of truth for all resources
*/

/**
 * Optional startup cache for images and fonts.
 * The project currently keeps this available as infrastructure for future optimization work.
 */
public class ResourceCache {
    // Two separate caches keep image and font lookups straightforward.
    private static final Map<String, BufferedImage> imageCache = new HashMap<>();
    private static final Map<String, Font> fontCache = new HashMap<>();
    private static final Map<String, Clip> soundCache = new HashMap<>();
    private static final int SCENE_FRAME_CACHE_LIMIT = 8;
    private static final Map<String, BufferedImage> sceneFrameCache = new LinkedHashMap<String, BufferedImage>(
        SCENE_FRAME_CACHE_LIMIT,
        0.75f,
        true
    ) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> eldest) {
            return size() > SCENE_FRAME_CACHE_LIMIT;
        }
    };
    private static BufferedImage errorImage = createErrorImage(); // Default image sprite (in case a sprite call returns null) 
    private static final Font errorFont = new Font("Arial", Font.BOLD, 20); // Fallback font

    private static final int totalResources = 24 //background assets
                                            + 29 // player sprites
                                            + 1 // default enemy sprite
                                            + 48 // worm sprites
                                            + 59 // virus sprites
                                            + 75 // trojan main sprites
                                            + 6 // Boss sprites
                                            + 32 // tutorial dummy sprites
                                            + 180 // ground tile assets
                                            + 166 // solid tile assets
                                            + 3 // file-based font assets
                                            + 1 // system-based font assets
                                            + 8 // sound (music/sound effects) assets
                                            ; 
    private static int doneLoading = 0;
    private static ProgressListener listener;
    
    // Prevent instantiation
    private ResourceCache() {}

    public interface ProgressListener {
        void onProgress(int value, String message);
    }
    
    /**
     * Load a single image into cache
     */
    public static void loadImage(String key, String path) {
        // Load only once per key to avoid repeated disk access.
        if (!imageCache.containsKey(key)) {
            try (InputStream inputStream = openResourceStream(path)) {
                BufferedImage img = ImageIO.read(inputStream);
                imageCache.put(key, img);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Get cached image
     */
    public static BufferedImage getImage(String key) {
        BufferedImage image = imageCache.get(key);

        if (image == null) {
            System.err.println("Missing cached image: " + key);
            return errorImage;
        }

        return image;
    }

    /**
     * Loads large cutscene frames on demand and keeps only a small rolling window in memory.
     */
    public static synchronized BufferedImage getSceneFrame(String key, String path) {
        BufferedImage image = sceneFrameCache.get(key);
        if (image != null) {
            return image;
        }

        try (InputStream inputStream = openResourceStream(path)) {
            image = ImageIO.read(inputStream);
            if (image != null) {
                sceneFrameCache.put(key, image);
                return image;
            }
        } catch (Exception e) {
                e.printStackTrace();
        }

        System.err.println("Missing scene frame: " + key + " (" + path + ")");
        return errorImage;
    }

    public static synchronized void clearSceneFrames() {
        sceneFrameCache.clear();
    }
    
    /**
     * Load a font from file into cache (for TTF/OTF files)
     */
    public static void loadFont(String key, String path, int style, float size) {
        // Store the already-derived font so callers do not redo this work.
        if (!fontCache.containsKey(key)) {
            try (InputStream fontStream = openResourceStream(path)) {
                Font baseFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                Font derivedFont = baseFont.deriveFont(style, size);
                fontCache.put(key, derivedFont);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Opens a resource either from the development file system or from the packaged classpath.
     */
    private static InputStream openResourceStream(String path) throws Exception {
        File file = new File(path);
        if (file.exists()) {
            return new FileInputStream(file);
        }

        String resourcePath = "/" + path.replace("\\", "/");
        InputStream resourceStream = ResourceCache.class.getResourceAsStream(resourcePath);
        if (resourceStream != null) {
            return resourceStream;
        }

        throw new IllegalArgumentException("Resource not found: " + path);
    }
    
    /**
     * Load a system font into cache (for fonts like "Papyrus", "Arial", etc.)
     */
    public static void loadSystemFont(String key, String fontName, int style, float size) {
        // Useful when the project wants a quick built-in font without bundling a file.
        if (!fontCache.containsKey(key)) {
            try {
                Font font = new Font(fontName, style, (int)size);
                fontCache.put(key, font);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Get cached font
     */
    public static Font getFont(String key) {
        Font font = fontCache.get(key);

        if (font == null) {
            System.err.println("Missing cached font: " + key);
            return errorFont;
        }

        return font;
    }

    public static void loadSound(String key, String path) {
        if (!soundCache.containsKey(key)) {
            try {
                AudioInputStream inputStream =
                        AudioSystem.getAudioInputStream(new File(path));

                Clip clip = AudioSystem.getClip();
                clip.open(inputStream);
                soundCache.put(key, clip);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Clip getSound(String key) {
        Clip clip = soundCache.get(key);

        if (clip == null) {
            System.err.println("Missing cached font: " + key);
            return soundCache.get("silence");
        }

        return clip;
    }
    
    /**
     * Preload all game resources - called once at startup
     */
    public static void preloadAll(ProgressListener listener) {
        // This method centralizes startup loading so lag is moved away from gameplay/screens.
        ResourceCache.listener = listener;

        // Background assets
        loadBackgroundResource();

        // Player sprite assets
        loadPlayerResources();

        // All Enemy Sprites();
        loadEnemyResources();
        
        // Tile assets - Ground tiles (180 tiles max)
        loadTileAssets();
        
        // Fonts
        loadFontAssets();

        // Sound (music, sound effects)
        loadSoundAssets();

        listener.onProgress(100, "Loading complete!");
    }

    /**
     * Creates a bright fallback tile so missing images are obvious during testing.
     */
    private static BufferedImage createErrorImage() {
        BufferedImage image = new BufferedImage(
            Constants.tileSize,
            Constants.tileSize,
            BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g2 = image.createGraphics();
        g2.setColor(Color.RED);
        g2.fillRect(0, 0, Constants.tileSize, Constants.tileSize);
        g2.dispose();

        return image;
    }

    /**
     * Loads static and animated menu background assets.
     */
    private static void loadBackgroundResource() {
        // Background images
        loadImage("background", "res/background.png");
        doneLoading = report("Loaded background");
        loadImage("text_background", "res/text_background.png");
        doneLoading = report("Loaded text background");
        
        // Background animation frames (22 frames)
        for (int i = 0; i < 22; i++) {
            loadImage("bg_frame_" + i, String.format("res/BackGroundSeq/frame%04d.png", i));
            doneLoading = report("Loaded background frame " + (i + 1));
        }
    }

    /**
     * Loads all player animation frames.
     */
    private static void loadPlayerResources() {
        // Idle animations (7 frames)
        for (int i = 0; i < 7; i++) {
            loadImage("player_idle_" + i, String.format("res/PlayerAssets/idle%d.png", i + 1));
            doneLoading = report("Loaded idle player frame " + (i + 1));
        }
        // Down animations (4 frames)
        for (int i = 0; i < 4; i++) {
            loadImage("player_down_" + i, String.format("res/PlayerAssets/down%d.png", i + 1));
            doneLoading = report("Loaded down player frame " + (i + 1));
        }
        // Up animations (6 frames)
        for (int i = 0; i < 6; i++) {
            loadImage("player_up_" + i, String.format("res/PlayerAssets/up%d.png", i + 1));
            doneLoading = report("Loaded up player frame " + (i + 1));
        }
        // Left animations (6 frames)
        for (int i = 0; i < 6; i++) {
            loadImage("player_left_" + i, String.format("res/PlayerAssets/left%d.png", i + 1));
            doneLoading = report("Loaded left player frame " + (i + 1));
        }
        // Right animations (6 frames)
        for (int i = 0; i < 6; i++) {
            loadImage("player_right_" + i, String.format("res/PlayerAssets/right%d.png", i + 1));
            doneLoading = report("Loaded right player frame " + (i + 1));
        }
    }

    /**
     * Loads the sprite sets for every enemy type used by the levels.
     */
    private static void loadEnemyResources() {
        // default enemy sprite
        imageCache.put("enemy_default", errorImage);
        doneLoading = report("Loaded defult enemy sprite");

        loadEnemyAnimation("worm", "worm", "idle", 6);
        loadEnemyAnimation("worm", "worm", "up", 6);
        loadEnemyAnimation("worm", "worm", "down", 6);
        loadEnemyAnimation("worm", "worm", "left", 10);
        loadEnemyAnimation("worm", "worm", "right", 10);
        loadEnemyAnimation("worm", "worm", "damaged", 10);

        loadEnemyAnimation("virus", "virus", "idle", 13);
        loadEnemyAnimation("virus", "virus", "up", 11);
        loadEnemyAnimation("virus", "virus", "down", 13);
        loadEnemyAnimation("virus", "virus", "left", 11);
        loadEnemyAnimation("virus", "virus", "right", 11);

        loadEnemyAnimation("trojan", "trojan", "idle", 12);
        loadEnemyAnimation("trojan", "trojan", "recovery", 35);
        loadEnemyAnimation("trojan", "trojan", "producing", 21);
        loadEnemyAnimation("trojan", "trojan", "cooldown", 7);

        loadEnemyAnimation("boss", "Boss", "idle", 6);

        loadFlatEnemyAnimation("dummy", "res/DummyAssets", "idle", 10);
        loadFlatEnemyAnimation("dummy", "res/DummyAssets", "up", 5);
        loadFlatEnemyAnimation("dummy", "res/DummyAssets", "down", 5);
        loadFlatEnemyAnimation("dummy", "res/DummyAssets", "left", 6);
        loadFlatEnemyAnimation("dummy", "res/DummyAssets", "right", 6);
    }

    /**
     * Loads one enemy animation sequence from the nested EnemyAssets folder structure.
     */
    private static void loadEnemyAnimation(
        String enemyKey,
        String folder,
        String state,
        int frameCount) {
        for (int i = 0; i < frameCount; i++) {
            String key = "enemy_" + enemyKey + "_" + state + "_" + i;
            String path = String.format(
                "res/EnemyAssets/%s/%s%d.png",
                folder,
                state,
                i + 1
            );

            loadImage(key, path);
            doneLoading = report("Loaded " + enemyKey + " " + state + " frame " + (i + 1));
        }
    }

    /**
     * Loads one enemy animation sequence from a flat asset folder.
     */
    private static void loadFlatEnemyAnimation(String enemyKey, String folder, String state, int frameCount) {
        for (int i = 0; i < frameCount; i++) {
            String key = "enemy_" + enemyKey + "_" + state + "_" + i;
            String path = String.format("%s/%s%d.png", folder, state, i + 1);
            loadImage(key, path);
            doneLoading = report("Loaded " + enemyKey + " " + state + " frame " + (i + 1));
        }
    }

    /**
     * Loads ground and solid tile textures until the expected numbering sequence ends.
     */
    private static void loadTileAssets() {
        for (int i = 1; i <= 180; i++) {
            String path = String.format("res/TILES/ground%03d.png", i);
            java.io.File f = new java.io.File(path);
            if (f.exists()) {
                loadImage("tile_ground_" + i, path);
                doneLoading = report("Loaded ground tile "+ i);
            } else {
                break;
            }
        }
        // Tile assets - Solid tiles
        for (int i = 1; i <= 166; i++) {
            String path = String.format("res/TILES/solid%03d.png", i);
            java.io.File f = new java.io.File(path);
            if (f.exists()) {
                loadImage("tile_solid_" + i, path);
                doneLoading = report("Loaded solid tile "+ i);
            } else {
                break;
            }
        }
    }

    /**
     * Loads custom project fonts and one system fallback font.
     */
    private static void loadFontAssets() {
        loadFont("title_upper", "res/Font/TopTitle_Font.ttf", Font.BOLD, 35f);
        doneLoading = report( "Loaded title font");
        loadFont("title_lower", "res/Font/Those_Glitch_Regular.ttf", Font.BOLD, 50f);
        doneLoading = report("Loaded subtitle font");
        loadFont("button_text", "res/Font/texts.ttf", Font.BOLD, 20f);
        doneLoading = report("Loaded button font");
        // System fonts
        loadSystemFont("instruction_text", "Papyrus", Font.BOLD, 18);
        doneLoading = report("Loaded Papyrus font");
    }

    private static void loadSoundAssets() {
        // fallback audio
        loadSound("silence", "res/Music/silence.wav");
        doneLoading = report("Loaded Fallback sound");

        // Main menu music
        loadSound("homescreen bg music", "res/Music/homescreen bg music.wav");
        doneLoading = report("Loaded Main Menu Background Music");

        // cutscene musics
        loadSound("gameIntro music", "res/Music/gameIntro.wav");
        doneLoading = report("Loaded Animation Intro Music");
        loadSound("level1Intro music", "res/Music/level1Intro music.wav");
        doneLoading = report("Loaded Level 1 Intro Music");
        loadSound("level2Intro music", "res/Music/level2Intro music.wav");
        doneLoading = report("Loaded Level 2 Intro Music");
        loadSound("bossIntro music", "res/Music/bossIntro music.wav");
        doneLoading = report("Loaded Boss Intro Music");
        loadSound("endingOutro music", "res/Music/endingOutro music.wav");
        doneLoading = report("Loaded Outro Music");

        // gameplay musics
        loadSound("Level 1 and 2 music", "res/Music/Level 1 and 2 music.wav");
        doneLoading = report("Loaded Level 1 and 2 Gameplay Music");
        loadSound("Level 3 (Boss) music", "res/Music/Level 3 (Boss) music.wav");
        doneLoading = report("Loaded Boss Level Gameplay Music");

        // Gameplay sound effects
        loadSound("player bullet fx", "res/Music/player bullet fx.wav");
        doneLoading = report("Loaded Player Bullet Sound Effects");
        loadSound("player melee fx", "res/Music/player melee fx.wav");
        doneLoading = report("Loaded Player Melee Sound Effects");
        loadSound("player dash fx", "res/Music/player dash fx.wav");
        doneLoading = report("Loaded Player Dash Sound Effects");
        loadSound("enemy hit fx", "res/Music/enemy hit fx.wav");
        doneLoading = report("Loaded Enemy Hit Sound Effects");
        loadSound("boss hit fx", "res/Music/boss hit fx.wav");
        doneLoading = report("Loaded Boss Hit Sound Effects");

        // General sound effects
        loadSound("button fx", "res/Music/button fx.wav");
        doneLoading = report("Loaded Button Effects");
    }

    /**
     * Updates the loading progress listener after a resource is handled.
     */
    private static int report(String message) {
        doneLoading++;
        int percent = (int) ((doneLoading / (double) totalResources) * 100);
        listener.onProgress(percent, message);
        return doneLoading;
    }

    /**
     * Clear all cached resources (use when shutting down or for memory management)
     */
    public static void clear() {
        // Frees references so the JVM can reclaim memory later.
        imageCache.clear();
        fontCache.clear();
        sceneFrameCache.clear();
        soundCache.clear();
    }

    public static Map<String, Clip> getSoundClips() {
        return ResourceCache.soundCache;
    }
    
    /**
     * Get cache statistics for debugging
     */
    public static void printStats() {
    }

    public static int getDoneLoading() { return doneLoading; }
}
