package entity;

/**
 * State machine states for the Trojan enemy.
 * The Trojan uses a state machine to control its spawning behavior.
 */
public enum TrojanState {
    IDLE,       // Waiting to activate
    ACTIVATING, // Transitioning to active state
    PRODUCING,  // Spawning enemies
    COOLDOWN,   // Cooling down after spawning
    DESTROYED   // Dead/destroyed state
}
