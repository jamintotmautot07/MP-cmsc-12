package systems;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import engine.GamePanel;

/**
 * Input adapter that translates raw keyboard events into logical gameplay actions.
 */
public class KeyHandler implements KeyListener {
    // Logical in-game actions. This makes the rest of the code care about intent, not raw key codes.
    public enum Action {
        MOVE_UP,
        MOVE_DOWN,
        MOVE_LEFT,
        MOVE_RIGHT,
        ATTACK,
        FIRE,
        DASH
    }

    // `keyBindings` maps game actions to physical keyboard keys.
    // `keyStates` stores whether each action is currently held down.
    // `reverseBindings` lets key events be translated back into actions quickly.
    private final Map<Action, Integer> keyBindings = new EnumMap<>(Action.class);
    private final Map<Action, Boolean> keyStates = new EnumMap<>(Action.class);
    private final Map<Integer, Action> reverseBindings = new HashMap<>();

    GamePanel gp;

    /**
     * Builds the handler and installs the default bindings.
     */
    public KeyHandler(GamePanel gp) {
        this.gp = gp;
        initDefaultBindings();
    }

    /**
     * Sets the default keyboard layout for the current build.
     */
    private void initDefaultBindings() {
        // Default movement scheme: WASD + Enter to attack.
        bindKey(Action.MOVE_UP, KeyEvent.VK_W);
        bindKey(Action.MOVE_DOWN, KeyEvent.VK_S);
        bindKey(Action.MOVE_LEFT, KeyEvent.VK_A);
        bindKey(Action.MOVE_RIGHT, KeyEvent.VK_D);

        bindKey(Action.ATTACK, KeyEvent.VK_ENTER);
        bindKey(Action.FIRE, KeyEvent.VK_BACK_SPACE);
        bindKey(Action.DASH, KeyEvent.VK_LEFT);
    }

    /**
     * Binds one logical action to one physical key.
     */
    public void bindKey(Action action, int keyCode) {
        // If the action was already bound, clear the old reverse mapping first.
        Integer previous = keyBindings.put(action, keyCode);
        if (previous != null) {
            reverseBindings.remove(previous);
        }
        reverseBindings.put(keyCode, action);
        keyStates.put(action, false);
    }

    /**
     * Returns the key code currently mapped to the requested action.
     */
    public int getBinding(Action action) {
        return keyBindings.getOrDefault(action, -1);
    }

    /**
     * Returns whether the action is currently being held down.
     */
    public boolean isActionPressed(Action action) {
        return keyStates.getOrDefault(action, false);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        // Space is handled as a global pause/resume shortcut, not a normal action binding.
        if (code == KeyEvent.VK_SPACE) {
            if (gp.gameState == gp.playState) {
                gp.pauseGame();
            } else if (gp.gameState == gp.pausedState) {
                gp.resumeGame();
            }
            return;
        }

        // Escape is reserved for skipping cutscenes.
        if (code == KeyEvent.VK_ESCAPE) {
            if (gp.gameState == gp.cutsceneState) {
                gp.skipScene();
            }
            return;
        }

        // For normal gameplay inputs, mark the action as currently pressed.
        Action action = reverseBindings.get(code);
        if (action != null) {
            keyStates.put(action, true);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        Action action = reverseBindings.get(code);
        if (action != null) {
            keyStates.put(action, false);
        }
    }

    /**
     * Clears all action states, usually when pausing or switching screens.
     */
    public void resetKeys() {
        // when switching screens or pausing so stale key holds do not leak into the next state.
        for (Action action : Action.values()) {
            keyStates.put(action, false);
        }
    }
}
