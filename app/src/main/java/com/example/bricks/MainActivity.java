package com.example.bricks;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Main activity of the Bricks game.
 * Manages the game lifecycle, UI fragments, and settings panel interactions.
 */
public class MainActivity extends AppCompatActivity implements
        StartFragment.StartFragmentListener,
        NextLevelFragment.NextLevelFragmentListener,
        GameCompletedFragment.GameCompletedFragmentListener,
        GameOverFragment.GameOverFragmentListener {

    private UpdateThread updateThread; // Thread for updating the game state
    private Handler updateHandler; // Handler for processing game updates
    private GameView gameView; // Custom view for rendering the game

    private SettingsPanelView panel; // Settings panel for toggling game options

    /**
     * Called when the activity is created.
     * Initializes the game view, settings panel, and UI fragments.
     *
     * @param savedInstanceState If non-null, the activity is being re-created from a previous
     *                           state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Lock the screen orientation to portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Enable fullscreen immersive mode
        WindowInsetsController insetsController = getWindow().getInsetsController();

        if (insetsController != null) {
            getWindow().setDecorFitsSystemWindows(false);
            insetsController.hide(WindowInsets.Type.systemBars());
            insetsController.setSystemBarsBehavior(
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
        }

        gameView = new GameView(this, null); // Initialize the custom game view

        // Set up the settings panel
        FrameLayout main_activity = findViewById(R.id.main);
        panel = new SettingsPanelView(this);

        // Load saved preferences for sound and music settings
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean sfxOn = prefs.getBoolean("sfxOn", true);
        boolean musicOn = prefs.getBoolean("musicOn", true);

        // Apply preferences to the panel and game view
        panel.setSfxOn(sfxOn);
        panel.setMusicOn(musicOn);
        panel.setIconStates(true, sfxOn, musicOn, true);
        gameView.setSfxOn(sfxOn);
        gameView.setMusicOn(musicOn);

        // Add the settings panel to the main layout
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.END);
        main_activity.addView(panel, lp);

        // Set up quick tap listener for the settings panel
        gameView.setQuickTapListener(panel::showGear);

        // Define actions for the settings panel buttons
        panel.setActions(new SettingsPanelView.Actions() {

            /**
             * Toggles the sound effects setting.
             */
            @Override
            public void onToggleSfx() {
                boolean musicOn = panel.isMusicOn();
                boolean sfxOn = !panel.isSfxOn();
                boolean playOn = panel.isPlayOn();
                boolean settingsOn = panel.isSettingsOn();

                gameView.setSfxOn(sfxOn);

                panel.setIconStates(settingsOn, sfxOn, musicOn, playOn);
            }

            /**
             * Toggles the music setting.
             */
            @Override
            public void onToggleMusic() {
                boolean musicOn = !panel.isMusicOn();
                boolean sfxOn = panel.isSfxOn();
                boolean playOn = panel.isPlayOn();
                boolean settingsOn = panel.isSettingsOn();

                if (!playOn) {
                    gameView.setMusicOn(musicOn);
                } else {
                    gameView.switchMusicPlayback(musicOn);
                }

                panel.setIconStates(settingsOn, sfxOn, musicOn, playOn);
            }

            /**
             * Toggles the play/pause state of the game.
             */
            @Override
            public void onTogglePlay() {
                boolean playOn = !panel.isPlayOn();
                boolean musicOn = panel.isMusicOn();
                boolean sfxOn = panel.isSfxOn();
                boolean settingsOn = panel.isSettingsOn();

                gameView.setIsPlaying(playOn);

                if (!playOn) {
                    gameView.pauseMusicPlayback();
                } else {
                    gameView.switchMusicPlayback(musicOn);
                }

                panel.setIconStates(settingsOn, sfxOn, musicOn, playOn);
            }
        });

        // Add the game view to the game container
        FrameLayout gameContainer = findViewById(R.id.game_container);
        gameContainer.addView(gameView,
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

        // Create the handler for game updates
        createHandler();
        updateThread = new UpdateThread(updateHandler);

        setPanelEnabled(true);

        // Show the start fragment
        showFragment(StartFragment::new, fragment -> {
            fragment.setStartFragmentListener(MainActivity.this);
            fragment.setQuickTapListener(panel::showGear);
        }, false);
    }

    /**
     * Enables or disables the settings panel.
     *
     * @param enabled True to enable the panel, false to disable it.
     */
    private void setPanelEnabled(boolean enabled) {
        if (panel == null) {
            return;
        }

        if (enabled) {
            panel.setVisibility(View.VISIBLE);

        } else {
            panel.hide();
            panel.setVisibility(View.GONE);
        }
    }

    /**
     * Starts the game when the start button is clicked.
     */
    @Override
    public void onStartGame() {
        if (updateThread == null || !updateThread.isAlive()) {
            // Enable the settings panel and gameplay mode
            panel.toggleGameStarted(true);
            gameView.setIsPlaying(true);

            // Start the game update thread and the game
            updateThread = new UpdateThread(updateHandler);
            updateThread.start();
            gameView.startGame();

            // Hide the fragment container and show the game container
            FrameLayout fragmentContainer = findViewById(R.id.fragment_container);
            fragmentContainer.setVisibility(View.GONE);
            FrameLayout gameContainer = findViewById(R.id.game_container);
            gameContainer.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Proceeds to the next level when the next level button is clicked.
     */
    @Override
    public void onNextLevel() {
        // Enable the settings panel and start the next level
        panel.toggleGameStarted(true);
        gameView.startNextLevel();

        // Hide the fragment container and show the game container
        FrameLayout fragmentContainer = findViewById(R.id.fragment_container);
        fragmentContainer.setVisibility(View.GONE);
        FrameLayout gameContainer = findViewById(R.id.game_container);
        gameContainer.setVisibility(View.VISIBLE);

        // Restart the game update thread
        if (gameView.isPlaying() && (updateThread == null || !updateThread.isAlive())) {
            updateThread = new UpdateThread(updateHandler);
            updateThread.start();
        }
    }

    /**
     * Restarts the game when the restart game button is clicked.
     */
    @Override
    public void onRestartGame() {
        // Show the start fragment
        showFragment(StartFragment::new, fragment -> {
            fragment.setStartFragmentListener(MainActivity.this);
            fragment.setQuickTapListener(panel::showGear);
        }, false);
    }

    /**
     * Saves preferences and interrupts the update thread when the activity is paused.
     */
    @Override
    protected void onPause() {
        super.onPause();

        // Save sound and music settings to SharedPreferences
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        prefs.edit()
                .putBoolean("sfxOn", panel != null && panel.isSfxOn())
                .putBoolean("musicOn", panel != null && panel.isMusicOn())
                .apply();

        // Interrupt the update thread if the game is playing
        if (gameView.isPlaying() && updateThread != null && updateThread.isAlive()) {
            updateThread.interrupt();
        }
    }

    /**
     * Resumes the update thread when the activity is resumed.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Restart the update thread if the game is playing
        if (gameView.isPlaying() && (updateThread == null || !updateThread.isAlive())) {
            updateThread = new UpdateThread(updateHandler);
            updateThread.start();
        }
    }

    /**
     * Creates a handler for processing game updates.
     */
    private void createHandler() {
        updateHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                gameView.update();
                gameView.invalidate();

                // Check game state and show appropriate fragments
                if (gameView.isLevelCleared()) {
                    showFragment(NextLevelFragment::new, fragment -> {
                        fragment.setNextLevelFragmentListener(MainActivity.this);
                        fragment.setCurrentLevel(gameView.getCurrentLevel());
                        fragment.setQuickTapListener(panel::showGear);
                    }, true);
                } else if (gameView.isGameCompleted()) {
                    showFragment(GameCompletedFragment::new, fragment -> {
                        fragment.setGameCompletedFragmentListener(MainActivity.this);
                        fragment.setQuickTapListener(panel::showGear);
                    }, true);
                } else if (gameView.isGameOvered()) {
                    showFragment(GameOverFragment::new, fragment -> {
                        fragment.setGameOverFragmentListener(MainActivity.this);
                        fragment.setQuickTapListener(panel::showGear);
                    }, true);
                }

                super.handleMessage(msg);
            }
        };
    }

    /**
     * Displays a fragment in the fragment container.
     *
     * @param fragmentSupplier Supplier that provides a new instance of the fragment.
     * @param fragmentConsumer Consumer that sets up the fragment.
     * @param checkThread      If true, checks if the update thread is alive before showing the fragment.
     * @param <T>              Type of the fragment to be displayed.
     */
    private <T extends Fragment> void showFragment(Supplier<T> fragmentSupplier, Consumer<T> fragmentConsumer, boolean checkThread) {
        if (checkThread && (updateThread == null || !updateThread.isAlive())) {
            return; // Do not show the fragment if the update thread is not alive
        }

        updateThread.interrupt(); // Interrupt the game update thread

        panel.toggleGameStarted(false);

        // Create and set up the fragment
        T fragment = fragmentSupplier.get();
        fragmentConsumer.accept(fragment);

        // Hide the game container and show the fragment container
        FrameLayout gameContainer = findViewById(R.id.game_container);
        gameContainer.setVisibility(View.GONE);
        FrameLayout fragmentContainer = findViewById(R.id.fragment_container);
        fragmentContainer.setVisibility(View.VISIBLE);

        // Replace the current fragment with the new fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

}