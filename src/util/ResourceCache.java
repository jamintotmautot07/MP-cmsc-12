package util;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

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

public class ResourceCache {
    private static final Map<String, BufferedImage> imageCache = new HashMap<>();
    private static final Map<String, Font> fontCache = new HashMap<>();
    
    // Prevent instantiation
    private ResourceCache() {}
    
    /**
     * Load a single image into cache
     */
    public static void loadImage(String key, String path) {
        if (!imageCache.containsKey(key)) {
            try {
                BufferedImage img = ImageIO.read(new java.io.File(path));
                imageCache.put(key, img);
            } catch (Exception e) {
                System.err.println("Failed to load image: " + path);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Get cached image
     */
    public static BufferedImage getImage(String key) {
        return imageCache.get(key);
    }
    
    /**
     * Load a font from file into cache (for TTF/OTF files)
     */
    public static void loadFont(String key, String path, int style, float size) {
        if (!fontCache.containsKey(key)) {
            try (InputStream fontStream = new FileInputStream(path)) {
                Font baseFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                Font derivedFont = baseFont.deriveFont(style, size);
                fontCache.put(key, derivedFont);
                System.out.println("Loaded font from file: " + key + " (" + path + ")");
            } catch (Exception e) {
                System.err.println("Failed to load font from file: " + path);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Load a system font into cache (for fonts like "Papyrus", "Arial", etc.)
     */
    public static void loadSystemFont(String key, String fontName, int style, float size) {
        if (!fontCache.containsKey(key)) {
            try {
                Font font = new Font(fontName, style, (int)size);
                fontCache.put(key, font);
                System.out.println("Loaded system font: " + key + " (" + fontName + ")");
            } catch (Exception e) {
                System.err.println("Failed to load system font: " + fontName);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Get cached font
     */
    public static Font getFont(String key) {
        return fontCache.get(key);
    }
    
    /**
     * Preload all game resources - called once at startup
     */
    public static void preloadAll() {
        // Background images
        loadImage("background", "res/background.png");
        loadImage("text_background", "res/text_background.png");
        
        // Background animation frames (22 frames)
        for (int i = 0; i < 22; i++) {
            loadImage("bg_frame_" + i, String.format("res/BackGroundSeq/frame%04d.png", i));
        }
        
        // Player sprite assets
        // Idle animations (7 frames)
        for (int i = 0; i < 7; i++) {
            loadImage("player_idle_" + i, String.format("res/PlayerAssets/idle%d.png", i + 1));
        }
        
        // Down animations (4 frames)
        for (int i = 0; i < 4; i++) {
            loadImage("player_down_" + i, String.format("res/PlayerAssets/down%d.png", i + 1));
        }
        
        // Up animations (6 frames)
        for (int i = 0; i < 6; i++) {
            loadImage("player_up_" + i, String.format("res/PlayerAssets/up%d.png", i + 1));
        }
        
        // Left animations (6 frames)
        for (int i = 0; i < 6; i++) {
            loadImage("player_left_" + i, String.format("res/PlayerAssets/left%d.png", i + 1));
        }
        
        // Right animations (6 frames)
        for (int i = 0; i < 6; i++) {
            loadImage("player_right_" + i, String.format("res/PlayerAssets/right%d.png", i + 1));
        }
        
        // Tile assets - Ground tiles (180 tiles max)
        for (int i = 1; i <= 180; i++) {
            String path = String.format("res/TILES/ground%03d.png", i);
            java.io.File f = new java.io.File(path);
            if (f.exists()) {
                loadImage("tile_ground_" + i, path);
            } else {
                break;
            }
        }
        
        // Tile assets - Solid tiles
        for (int i = 1; i <= 50; i++) {
            String path = String.format("res/TILES/solid%03d.png", i);
            java.io.File f = new java.io.File(path);
            if (f.exists()) {
                loadImage("tile_solid_" + i, path);
            } else {
                break;
            }
        }
        
        // Fonts
        loadFont("title_upper", "res/Font/TopTitle_Font.ttf", Font.BOLD, 35f);
        loadFont("title_lower", "res/Font/Those_Glitch_Regular.ttf", Font.BOLD, 50f);
        loadFont("button_text", "res/Font/texts.ttf", Font.BOLD, 20f);
        // System fonts
        loadSystemFont("instruction_text", "Papyrus", Font.BOLD, 18);
    }
    
    /**
     * Clear all cached resources (use when shutting down or for memory management)
     */
    public static void clear() {
        imageCache.clear();
        fontCache.clear();
    }
    
    /**
     * Get cache statistics for debugging
     */
    public static void printStats() {
        System.out.println("ResourceCache loaded: " + imageCache.size() + " images, " + fontCache.size() + " fonts");
    }
}
