package ui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

/*
 OWNER: Jamin

 PURPOSE:
 - Handles the intro cutscene (only shown once)
 - Plays a sequence of frames/images with timing
 - Transitions to gameplay after finishing

 HOW IT WORKS (BIG IDEA):
 - Store intro images in an array
 - Display one image at a time
 - Wait for a set duration
 - Move to next frame
 - When done → switch to PLAYING state

 IMPORTANT:
 - This class DOES NOT control the game state directly
 - It should SIGNAL GamePanel when it is finished
*/

public class IntroManager {

    // =========================
    // CORE VARIABLES
    // =========================

    // TODO: Store all intro frames/images here
    private BufferedImage[] frames;

    // TODO: Track which frame is currently shown
    private int currentFrame = 0;

    // TODO: Timer variables
    private long lastFrameTime;
    private int frameDelay = 2000; // 2 seconds per frame (adjustable)

    // TODO: Flag to check if intro is done
    private boolean finished = false;


    // =========================
    // CONSTRUCTOR
    // =========================
    public IntroManager() {

        /*
         TASKS:

         1. Load all intro images into "frames"
            Example:
            frames = new BufferedImage[3];

            frames[0] = ImageIO.read(...);
            frames[1] = ImageIO.read(...);

         2. Set initial time:
            lastFrameTime = System.currentTimeMillis();

         NOTE:
         - You can use a utility class later for loading images
         - For now, even NULL frames are fine (placeholder)
        */
    }


    // =========================
    // UPDATE METHOD
    // =========================
    public void update() {

        /*
         PURPOSE:
         - Controls timing of frame switching

         STEPS:

         1. Get current time:
            long now = System.currentTimeMillis();

         2. Check if enough time passed:
            if(now - lastFrameTime > frameDelay)

         3. If yes:
            - Move to next frame
            - Reset timer

         4. If currentFrame exceeds frames length:
            - Set finished = true

         IMPORTANT:
         - DO NOT reset currentFrame to 0
         - Intro should only play once
        */
    }


    // =========================
    // RENDER METHOD
    // =========================
    public void render(Graphics g) {

        /*
         PURPOSE:
         - Draw current frame on screen

         STEPS:

         1. Check if frames exist
         2. Draw current frame:
            g.drawImage(frames[currentFrame], 0, 0, null);

         OPTIONAL ADDITIONS:

         - Draw text overlay (story text)
         - Draw "Press any key to skip"
         - Add fade effect (advanced)

         IMPORTANT:
         - Always check array bounds before drawing
        */
    }


    // =========================
    // SKIP INTRO
    // =========================
    public void skip() {

        /*
         PURPOSE:
         - Allows player to skip intro

         IMPLEMENTATION:
         - Set finished = true

         NOTE:
         - Call this when any key is pressed
        */
    }


    // =========================
    // CHECK IF DONE
    // =========================
    public boolean isFinished() {

        /*
         PURPOSE:
         - Lets GamePanel know when to switch to gameplay

         USAGE (in GamePanel):

         if(introManager.isFinished()){
             currentState = GameState.PLAYING;
         }
        */

        return finished;
    }


    // =========================
    // RESET (OPTIONAL)
    // =========================
    public void reset() {

        /*
         PURPOSE:
         - Reset intro if needed (for testing)

         TASKS:
         - currentFrame = 0
         - finished = false
         - reset timer
        */
    }
}