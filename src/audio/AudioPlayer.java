
package audio;

import java.util.Map;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import util.ResourceCache;

/*
 OWNER: Inoy

 PURPOSE:
 - Plays music and sound

 TASKS:
 1. Load audio file
 2. Play background music
 3. Play sound effects

 THREADING:
 - Run audio in separate thread

 OPTIONAL:
 - Volume control
*/

// all

/**
 * A singleton class that acts as am audio service for background music and sound-effect playback.
 */
public class AudioPlayer {
    private static AudioPlayer instance;

    private final Map<String, Clip> soundClips;

    private Clip currentMusic;
    private String currentMusicKey;

    private float masterVolume = 1.0f;
    private float musicVolume = 1.0f;
    private float soundVolume = 1.0f;
    private boolean muted = false;

    private AudioPlayer() {
        soundClips = ResourceCache.getSoundClips();
    }

    public static AudioPlayer getInstance() {
        if (instance == null) {
            instance = new AudioPlayer();
        }

        return instance;
    }

    public void playSound(String key) {
        Clip clip = soundClips.get(key);

        if (clip == null) {
            System.err.println("Missing sound effect: " + key);
            return;
        }

        synchronized (clip) {
            if (clip.isRunning()) {
                clip.stop();
            }

            clip.setFramePosition(0);
            applyVolume(clip, soundVolume);
            clip.start();
        }
    }

    public void playMusic(String key) {
        playMusic(key, true);
    }

    public void playMusic(String key, boolean loop) {
        Clip clip = soundClips.get(key);

        if (clip == null) {
            System.err.println("Missing music: " + key);
            return;
        }

        stopMusic();

        currentMusic = clip;
        currentMusicKey = key;

        synchronized (currentMusic) {
            currentMusic.setFramePosition(0);
            applyVolume(currentMusic, musicVolume);

            if (loop) {
                currentMusic.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                currentMusic.start();
            }
        }
    }

    public void stopMusic() {
        if (currentMusic == null) {
            return;
        }

        synchronized (currentMusic) {
            if (currentMusic.isRunning()) {
                currentMusic.stop();
            }

            currentMusic.setFramePosition(0);
        }

        currentMusic = null;
        currentMusicKey = null;
    }

    public void pauseMusic() {
        if (currentMusic != null && currentMusic.isRunning()) {
            currentMusic.stop();
        }
    }

    public void resumeMusic() {
        if (currentMusic != null && !currentMusic.isRunning()) {
            applyVolume(currentMusic, musicVolume);
            currentMusic.start();
        }
    }

    public void restartMusic() {
        if (currentMusic == null) {
            return;
        }

        synchronized (currentMusic) {
            currentMusic.stop();
            currentMusic.setFramePosition(0);
            applyVolume(currentMusic, musicVolume);
            currentMusic.start();
        }
    }

    public void setMasterVolume(float volume) {
        masterVolume = clamp(volume);
        refreshCurrentMusicVolume();
    }

    public void setMusicVolume(float volume) {
        musicVolume = clamp(volume);
        refreshCurrentMusicVolume();
    }

    public void setSoundVolume(float volume) {
        soundVolume = clamp(volume);
    }

    public void mute() {
        muted = true;
        refreshCurrentMusicVolume();
    }

    public void unmute() {
        muted = false;
        refreshCurrentMusicVolume();
    }

    public boolean isMuted() {
        return muted;
    }

    public String getCurrentMusicKey() {
        return currentMusicKey;
    }

    private void refreshCurrentMusicVolume() {
        if (currentMusic != null) {
            applyVolume(currentMusic, musicVolume);
        }
    }

    private void applyVolume(Clip clip, float categoryVolume) {
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }

        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

        if (muted) {
            gainControl.setValue(gainControl.getMinimum());
            return;
        }

        float finalVolume = clamp(masterVolume) * clamp(categoryVolume);

        if (finalVolume <= 0.0f) {
            gainControl.setValue(gainControl.getMinimum());
            return;
        }

        float decibels = (float) (20.0 * Math.log10(finalVolume));
        decibels = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), decibels));

        gainControl.setValue(decibels);
    }

    private float clamp(float value) {
        if (value < 0.0f) {
            return 0.0f;
        }

        if (value > 1.0f) {
            return 1.0f;
        }

        return value;
    }
}

//?