package systems;

/**
 * Owns tutorial instruction progress and required action checks.
 */
public class TutorialManager {
    public enum TutorialStep {
        START,
        MOVE_UP,
        MOVE_DOWN,
        MOVE_LEFT,
        MOVE_RIGHT,
        DASH,
        NORMAL_ATTACK,
        FIRE_ATTACK,
        ELIMINATE_DUMMIES,
        GO_TO_DOOR,
        COMPLETE
    }

    private static final int START_HOLD_FRAMES = 90;

    private final String[] tutorialInstructions = {
        "SYSTEM INITIALIZATION STARTED.",
        "Use W to move upward.",
        "Good. Use S to move downward.",
        "Use A to move left.",
        "Use D to move right.",
        "Dash module detected. Press LEFT ARROW to dash.",
        "Combat module unlocked. Press ENTER to use a normal Bit attack.",
        "Fire module unlocked. Press BACKSPACE to fire a Bit projectile.",
        "Training viruses detected. Eliminate all dummy enemies.",
        "Tutorial complete. Proceed to the main door."
    };

    private TutorialStep currentStep = TutorialStep.START;
    private int instructionIndex = 0;
    private int startFrameCounter = 0;

    public void reset() {
        currentStep = TutorialStep.START;
        instructionIndex = 0;
        startFrameCounter = 0;
    }

    public void update(boolean allDummiesEliminated) {
        if (currentStep == TutorialStep.START) {
            startFrameCounter++;
            if (startFrameCounter >= START_HOLD_FRAMES) {
                advanceTo(TutorialStep.MOVE_UP);
            }
            return;
        }

        if (currentStep == TutorialStep.ELIMINATE_DUMMIES && allDummiesEliminated) {
            advanceTo(TutorialStep.GO_TO_DOOR);
        }
    }

    public void handleAction(KeyHandler.Action action) {
        if (action == null) {
            return;
        }

        switch (currentStep) {
            case MOVE_UP:
                if (action == KeyHandler.Action.MOVE_UP) advanceTo(TutorialStep.MOVE_DOWN);
                break;
            case MOVE_DOWN:
                if (action == KeyHandler.Action.MOVE_DOWN) advanceTo(TutorialStep.MOVE_LEFT);
                break;
            case MOVE_LEFT:
                if (action == KeyHandler.Action.MOVE_LEFT) advanceTo(TutorialStep.MOVE_RIGHT);
                break;
            case MOVE_RIGHT:
                if (action == KeyHandler.Action.MOVE_RIGHT) advanceTo(TutorialStep.DASH);
                break;
            case DASH:
                if (action == KeyHandler.Action.DASH) advanceTo(TutorialStep.NORMAL_ATTACK);
                break;
            case NORMAL_ATTACK:
                if (action == KeyHandler.Action.ATTACK) advanceTo(TutorialStep.FIRE_ATTACK);
                break;
            case FIRE_ATTACK:
                if (action == KeyHandler.Action.FIRE) advanceTo(TutorialStep.ELIMINATE_DUMMIES);
                break;
            default:
                break;
        }
    }

    public void complete() {
        currentStep = TutorialStep.COMPLETE;
    }

    public boolean canUseDoor() {
        return currentStep == TutorialStep.GO_TO_DOOR;
    }

    public String getCurrentInstruction() {
        return tutorialInstructions[Math.min(instructionIndex, tutorialInstructions.length - 1)];
    }

    public TutorialStep getCurrentStep() {
        return currentStep;
    }

    private void advanceTo(TutorialStep nextStep) {
        currentStep = nextStep;
        instructionIndex = Math.min(nextStep.ordinal(), tutorialInstructions.length - 1);
    }
}
