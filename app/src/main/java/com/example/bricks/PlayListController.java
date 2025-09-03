package com.example.bricks;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Controls the playlist and background music playback during gameplay.
 * Handles shuffling, playing, and stopping the music tracks.
 */
public class PlayListController {

    private final Context context; // Application context for accessing resources
    private final List<Integer> playlist; // Current shuffled playlist
    private final ArrayList<Integer> allTracks; // List of all available music tracks
    private final Random random; // Random instance for shuffling the playlist
    private MediaPlayer mediaPlayer; // MediaPlayer instance for playing music
    private boolean isPaused = false; // Flag to track if playback is paused

    /**
     * Constructs a new PlayListController.
     *
     * @param context The application context used to access resources.
     */
    public PlayListController(Context context) {
        this.context = context;
        this.allTracks = new ArrayList<>();
        this.playlist = new ArrayList<>();
        this.random = new Random();

        // Add all available music tracks to the list
        this.allTracks.add(R.raw.bg_music_01);
        this.allTracks.add(R.raw.bg_music_02);
        this.allTracks.add(R.raw.bg_music_03);
        this.allTracks.add(R.raw.bg_music_04);
    }

    /**
     * Shuffles the playlist by randomizing the order of all available tracks.
     */
    public void shufflePlayList() {
        playlist.clear();
        playlist.addAll(allTracks);
        Collections.shuffle(playlist, random);
    }

    /**
     * Plays the next track in the shuffled playlist.
     * If the playlist is empty, it reshuffles before playing.
     */
    public void playShuffle() {
        if (mediaPlayer != null) {
            mediaPlayer.release(); // Release the current MediaPlayer instance
        }

        if (playlist.isEmpty()) {
            shufflePlayList(); // Reshuffle if the playlist is empty
        }

        isPaused = false; // Reset paused state

        int rawResourceId = playlist.remove(0); // Get the next track from the playlist
        mediaPlayer = MediaPlayer.create(context, rawResourceId);
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
        mediaPlayer.setOnCompletionListener(mp -> playShuffle()); // Play the next track on completion
        mediaPlayer.start();
    }

    /**
     * Pauses current playback without releasing resources.
     */
    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPaused = true;
        }
    }

    /**
     * Resumes playback if previously paused. If there is no player, starts shuffled playback.
     */
    public void resume() {
        if (mediaPlayer != null && isPaused) {
            mediaPlayer.start();
            isPaused = false;
        } else if (mediaPlayer == null) {
            // No current player – start a new shuffled track
            playShuffle();
        }
    }

    /**
     * Stops the current music playback and releases resources.
     */
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        isPaused = false; // Reset paused state
    }

    /**
     * Checks if music is currently playing.
     *
     * @return True if music is playing, false otherwise.
     */
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    /**
     * Checks if playback is currently paused.
     *
     * @return True if paused, false otherwise.
     */
    public boolean isPaused() {
        return mediaPlayer != null && isPaused && !mediaPlayer.isPlaying();
    }

}
