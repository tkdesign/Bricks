package com.example.bricks;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import java.util.HashMap;

/**
 * SoundManager is a singleton class responsible for managing short sound effects in the application using SoundPool.
 * It provides methods to load and play sound resources efficiently, suitable for games or apps with frequent sound effects.
 */
public class SoundManager {
    private static SoundManager instance; // Singleton instance
    private final SoundPool soundPool; // SoundPool for managing sound effects
    private final HashMap<Integer, Integer> soundMap = new HashMap<>(); // Maps resource IDs to sound IDs

    /**
     * Private constructor to initialize SoundPool with appropriate audio attributes for game sound effects.
     */
    private SoundManager() {
        AudioAttributes attrs = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
        soundPool = new SoundPool.Builder().setMaxStreams(5).setAudioAttributes(attrs).build();
    }

    /**
     * Returns the singleton instance of SoundManager, creating it if necessary.
     *
     * @return The singleton SoundManager instance.
     */
    public static synchronized SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }

        return instance;
    }

    /**
     * Loads a sound resource into the SoundPool if it has not been loaded yet.
     *
     * @param context Application context.
     * @param resId   Resource ID of the sound to load (e.g., R.raw.sound_effect).
     */
    public void loadSound(Context context, int resId) {
        if (!soundMap.containsKey(resId)) {
            int soundId = soundPool.load(context, resId, 1);
            soundMap.put(resId, soundId);
        }
    }

    /**
     * Plays a loaded sound effect by its resource ID. If the sound is not loaded, nothing happens.
     *
     * @param resId Resource ID of the sound to play.
     */
    public void play(int resId) {
        Integer soundId = soundMap.get(resId);

        if (soundId != null) {
            soundPool.play(soundId, 1, 1, 1, 0, 1f);
        }
    }
}
