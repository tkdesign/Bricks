package com.example.bricks;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * A custom view representing a settings panel in the game.
 * The panel includes buttons for toggling sound effects, music, and gameplay, as well as a settings
 * button. It supports auto-hide functionality and animations.
 */
public class SettingsPanelView extends LinearLayout {

    private final Runnable autoHideRunnable = this::hide; // Runnable for auto-hide functionality
    private final Handler handler = new Handler(Looper.getMainLooper()); // Handler for managing auto-hide
    private final int GEAR_DELAY = 2000; // Auto-hide delay (ms) in gear-only mode
    private final int EXPANDED_DELAY = 3500; // Auto-hide delay (ms) in expanded mode
    private final ImageView btnSettings; // Settings button (gear icon)
    private final ImageView btnSfx; // Sound effects toggle button
    private final ImageView btnMusic; // Music toggle button
    private final ImageView btnPlay; // Play toggle button
    private final View stack; // Container for non-gear buttons
    private boolean settingsOn = true, sfxOn = true, musicOn = true, playOn = true; // Button states
    private enum State {HIDDEN, GEAR_ONLY, EXPANDED,} // Enumeration representing the panel's visibility states.
    private State state = State.HIDDEN; // Current state of the panel
    private final SoundManager soundManager; // SoundManager instance for managing sound effects

    /**
     * Interface defining actions for the settings panel buttons.
     */
    public interface Actions {

        /**
         * Called when the sound effects toggle button is clicked.
         */
        void onToggleSfx();

        /**
         * Called when the music toggle button is clicked.
         */
        void onToggleMusic();

        /**
         * Called when the play toggle button is clicked.
         */
        void onTogglePlay();

    }

    private Actions actions; // Actions interface implementation

    /**
     * Constructs a new SettingsPanelView.
     *
     * @param context The application context.
     */
    public SettingsPanelView(Context context) {
        super(context);

        setOrientation(VERTICAL);
        setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        setClickable(true); // Catch clicks into panel
        setFocusable(true);

        int button_side_size = dp2px(72); // Width and height of buttons
        int gap_between_buttons = dp2px(8);
        setPadding(0, gap_between_buttons, 0, gap_between_buttons);

        // Stack of sound/music/play-pause buttons
        LinearLayout column = new LinearLayout(context);
        column.setOrientation(VERTICAL);
        LayoutParams lpCol = new LayoutParams(button_side_size, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpCol.topMargin = gap_between_buttons;
        column.setLayoutParams(lpCol);

        soundManager = SoundManager.getInstance();
        soundManager.loadSound(this.getContext(), R.raw.button_click);

        btnSfx = createIcon(button_side_size, button_side_size, R.drawable.ic_sfx_on);
        btnMusic = createIcon(button_side_size, button_side_size, R.drawable.ic_music_on);
        btnPlay = createIcon(button_side_size, button_side_size, R.drawable.ic_play_on);

        addGap(column, gap_between_buttons);
        column.addView(btnSfx);
        addGap(column, gap_between_buttons);
        column.addView(btnMusic);
        addGap(column, gap_between_buttons);
        column.addView(btnPlay);

        addView(column);
        stack = column;

        // Settings button (gear)
        btnSettings = createIcon(button_side_size, button_side_size, R.drawable.ic_settings_on);
        addView(btnSettings);

        initClickListeners();
        setTranslationX(button_side_size); // Move out of screen to the right
        setAlpha(0f);
    }

    /**
     * Sets the actions interface implementation for handling button clicks.
     *
     * @param actions The implementation of the Actions interface.
     */
    public void setActions(Actions actions) {
        this.actions = actions;
    }

    /**
     * Updates the icon states for all buttons.
     *
     * @param settingsOn Whether the settings button is active.
     * @param sfxOn      Whether the sound effects button is active.
     * @param musicOn    Whether the music button is active.
     * @param playOn     Whether the play button is active.
     */
    public void setIconStates(boolean settingsOn, boolean sfxOn, boolean musicOn, boolean playOn) {
        this.settingsOn = settingsOn;
        this.sfxOn = sfxOn;
        this.musicOn = musicOn;
        this.playOn = playOn;

        btnSettings.setImageResource(settingsOn ? R.drawable.ic_settings_on : R.drawable.ic_settings_off);
        btnSfx.setImageResource(sfxOn ? R.drawable.ic_sfx_on : R.drawable.ic_sfx_off);
        btnMusic.setImageResource(musicOn ? R.drawable.ic_music_on : R.drawable.ic_music_off);
        btnPlay.setImageResource(playOn ? R.drawable.ic_play_on : R.drawable.ic_play_off);
    }

    /**
     * Shows only the gear icon with auto-hide functionality.
     */
    public void showGear() {
        showPanel();

        stack.setVisibility(GONE);
        state = State.GEAR_ONLY;

        schedule(GEAR_DELAY);
    }

    /**
     * Expands the panel to show all buttons with auto-hide functionality.
     */
    public void expand() {
        showPanel();

        stack.setVisibility(VISIBLE);
        state = State.EXPANDED;

        schedule(EXPANDED_DELAY);
    }

    /**
     * Hides the panel (any state) with an animation.
     */
    public void hide() {
        cancel();

        animate()
                .translationX(getWidth()) // Slide out to the right
                .alpha(0f)
                .setDuration(160)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> state = State.HIDDEN)
                .start();
    }

    /**
     * Resets the auto-hide timer and keeps the panel visible if it is already shown.
     */
    public void onUserAction() {
        if (state == State.EXPANDED) {
            schedule(EXPANDED_DELAY);
        }
        else if (state == State.GEAR_ONLY) {
            schedule(GEAR_DELAY);
        }
    }

    /**
     * Returns whether the settings button is active.
     *
     * @return True if the settings button is active, false otherwise.
     */
    public boolean isSettingsOn() {
        return settingsOn;
    }

    /**
     * Sets the state of the music button.
     *
     * @param musicOn True to activate the music button, false to deactivate it.
     */
    public void setMusicOn(boolean musicOn) {
        this.musicOn = musicOn;
    }

    /**
     * Returns whether the music button is active.
     *
     * @return True if the music button is active, false otherwise.
     */
    public boolean isMusicOn() {
        return musicOn;
    }

    /**
     * Sets the state of the sound effects button.
     *
     * @param sfxOn True to activate the sound effects button, false to deactivate it.
     */
    public void setSfxOn(boolean sfxOn) {
        this.sfxOn = sfxOn;
    }

    /**
     * Returns whether the sound effects button is active.
     *
     * @return True if the sound effects button is active, false otherwise.
     */
    public boolean isSfxOn() {
        return sfxOn;
    }

    /**
     * Returns whether the play button is active.
     *
     * @return True if the play button is active, false otherwise.
     */
    public boolean isPlayOn() {
        return playOn;
    }

    public void toggleGameStarted(boolean gameStarted) {
        btnPlay.setVisibility(gameStarted ? VISIBLE : GONE);
    }

    /**
     * Shows the panel with an animation.
     * If the layout is not yet complete, the method is re-posted to the UI thread.
     */
    private void showPanel() {
        // Wait until layout is done
        if (getWidth() == 0) {
            post(this::showPanel);
            return;
        }

        cancel();

        if (getTranslationX() != 0f || getAlpha() < 1f) {
            animate().translationX(0f)
                    .alpha(1f)
                    .setDuration(160)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }

    /**
     * Schedules the auto-hide timer for the specified delay.
     *
     * @param delay The delay in milliseconds.
     */
    private void schedule(int delay) {
        cancel();
        handler.postDelayed(autoHideRunnable, delay);
    }

    /**
     * Cancels the auto-hide timer.
     */
    private void cancel() {
        handler.removeCallbacks(autoHideRunnable);
    }

    /**
     * Creates an ImageView with the specified dimensions and drawable resource.
     *
     * @param width      The width of the ImageView.
     * @param height     The height of the ImageView.
     * @param resourceId The drawable resource ID for the icon.
     * @return The created ImageView.
     */
    private ImageView createIcon(int width, int height, int resourceId) {
        ImageView iv = new ImageView(getContext());
        LayoutParams lp = new LayoutParams(width, height);
        iv.setLayoutParams(lp);
        iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        iv.setImageResource(resourceId);
        iv.setClickable(true);
        iv.setFocusable(true);
        iv.setSoundEffectsEnabled(false);
        return iv;
    }

    /**
     * Adds a gap (empty space) to the specified parent layout.
     *
     * @param parent The parent layout.
     * @param gap    The height of the gap in pixels.
     */
    private void addGap(LinearLayout parent, int gap) {
        View v = new View(getContext());
        v.setLayoutParams(new LayoutParams(1, gap));
        parent.addView(v);
    }

    /**
     * Converts a value in density-independent pixels (dp) to pixels.
     *
     * @param v The value in dp.
     * @return The equivalent value in pixels.
     */
    private int dp2px(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }

    /**
     * Adds click listeners to the buttons in the settings panel.
     */
    private void initClickListeners() {
        btnSettings.setOnClickListener(v -> {
            soundManager.play(R.raw.button_click);

            if (state == State.GEAR_ONLY) {
                expand();
            } else if (state == State.EXPANDED) {
                stack.setVisibility(GONE);
                state = State.GEAR_ONLY;
                schedule(GEAR_DELAY);
            } else {
                showGear();
            }
        });

        btnSfx.setOnClickListener(v -> {
            soundManager.play(R.raw.button_click);

            if (actions != null) {
                actions.onToggleSfx();
            }

            onUserAction();
        });

        btnMusic.setOnClickListener(v -> {
            soundManager.play(R.raw.button_click);

            if (actions != null) {
                actions.onToggleMusic();
            }

            onUserAction();
        });

        btnPlay.setOnClickListener(v -> {
            soundManager.play(R.raw.button_click);

            if (actions != null) {
                actions.onTogglePlay();
            }

            onUserAction();
        });
    }

}