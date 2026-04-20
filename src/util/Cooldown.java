package util;

/**
 * A simple cooldown system for game entities.
 * Tracks remaining time in frames for various actions.
 */
public class Cooldown {
    // Counts down once per update until the action becomes available again.
    private int remainingFrames;

    public Cooldown() {
        this.remainingFrames = 0;
    }

    /**
     * Start a cooldown with the specified duration in frames.
     * @param frames The number of frames this cooldown should last
     */
    public void start(int frames) {
        // The caller decides the cooldown duration in update frames, not milliseconds.
        this.remainingFrames = frames;
    }

    /**
     * Update the cooldown by decrementing remaining frames.
     * Call this once per frame in the entity's update method.
     */
    public void update() {
        if (remainingFrames > 0) {
            remainingFrames--;
        }
    }

    /**
     * Check if the cooldown is currently active.
     * @return true if cooldown is active, false if ready to use
     */
    public boolean isActive() {
        return remainingFrames > 0;
    }

    /**
     * Get the remaining frames.
     * @return remaining frames, 0 if not active
     */
    public int getRemainingFrames() {
        return remainingFrames;
    }

    /**
     * Force reset the cooldown.
     */
    public void reset() {
        // Immediate clear, useful for debugging or forced state resets.
        remainingFrames = 0;
    }
}
