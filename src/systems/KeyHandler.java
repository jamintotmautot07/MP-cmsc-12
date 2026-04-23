package systems;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import engine.GamePanel;

public class KeyHandler implements KeyListener {
    public enum Action {
        MOVE_UP,
        MOVE_DOWN,
        MOVE_LEFT,
        MOVE_RIGHT,
        ATTACK
    }

    private final Map<Action, Integer> keyBindings = new EnumMap<>(Action.class);
    private final Map<Action, Boolean> keyStates = new EnumMap<>(Action.class);
    private final Map<Integer, Action> reverseBindings = new HashMap<>();

    GamePanel gp;

    public KeyHandler(GamePanel gp) {
        this.gp = gp;
        initDefaultBindings();
    }

    private void initDefaultBindings() {
        bindKey(Action.MOVE_UP, KeyEvent.VK_W);
        bindKey(Action.MOVE_DOWN, KeyEvent.VK_S);
        bindKey(Action.MOVE_LEFT, KeyEvent.VK_A);
        bindKey(Action.MOVE_RIGHT, KeyEvent.VK_D);

        bindKey(Action.ATTACK, KeyEvent.VK_ENTER);
    }

    public void bindKey(Action action, int keyCode) {
        Integer previous = keyBindings.put(action, keyCode);
        if (previous != null) {
            reverseBindings.remove(previous);
        }
        reverseBindings.put(keyCode, action);
        keyStates.put(action, false);
    }

    public int getBinding(Action action) {
        return keyBindings.getOrDefault(action, -1);
    }

    public boolean isActionPressed(Action action) {
        return keyStates.getOrDefault(action, false);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_SPACE) {
            if (gp.gameState == gp.playState) {
                gp.pauseGame();
            } else if (gp.gameState == gp.pausedState) {
                gp.resumeGame();
            }
            return;
        }

        if (code == KeyEvent.VK_ESCAPE) {
            if (gp.gameState == gp.cutsceneState) {
                gp.skipScene();
            }
            return;
        }

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

    public void resetKeys() {
        for (Action action : Action.values()) {
            keyStates.put(action, false);
        }
    }
}
