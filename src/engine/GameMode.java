package engine;

/**
 * Describes the one active panel mode so input, update, and rendering code do not compare magic numbers.
 */
public enum GameMode {
    PLAYING,
    PAUSED,
    CUTSCENE,
    DEFEAT
}
