package com.example.bricks;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * A fragment representing the screen displayed after completing a level in the Bricks game.
 * It shows the current level cleared and provides a button to proceed to the next level.
 */
public class NextLevelFragment extends Fragment implements AnimationInterface {

    private NextLevelFragmentListener listener; // Listener for handling the next level event
    private int currentLevel; // The current level that was cleared
    private SoundManager soundManager; // SoundManager instance for managing sound effects
    private QuickTapListener quickTapListener; // Listener for quick tap events (for opening settings panel)
    private float downX, downY; // Coordinates of the initial touch down event for quick tap detection
    private long downTime; // Timestamp of the touch down event for quick tap detection

    /**
     * Inflates the layout for this fragment and sets up the UI elements, including the level text
     * and the next level button with its click listener.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous
     *                           saved state as given here.
     * @return The root view of the fragment's layout.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Initialize the SoundPool for playing sound effects
        soundManager = SoundManager.getInstance();
        soundManager.loadSound(this.getContext(), R.raw.button_click);

        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_next_level, container, false);

        // Set the level cleared text
        TextView levelText = view.findViewById(R.id.nextLevelText);
        levelText.setText(String.format(getString(R.string.level_d_cleared), currentLevel));

        // Set up the next level button
        Button nextButton = view.findViewById(R.id.continueButton);
        nextButton.setOnClickListener(v -> {
            if (listener != null) {
                // Play the button click sound
                soundManager.play(R.raw.button_click);

                // Animate the button click
                animateButton(nextButton);

                // Delay the next level event to allow the animation to complete
                new Handler(Looper.getMainLooper()).postDelayed(() -> listener.onNextLevel(), 300);
            }
        });

        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getX();
                    downY = event.getY();
                    downTime = event.getEventTime();
                    break;
                case MotionEvent.ACTION_UP:
                    float dx = Math.abs(event.getX() - downX);
                    float dy = Math.abs(event.getY() - downY);
                    long dt = event.getEventTime() - downTime;

                    int maxTapDistancePx = (int) (QuickTapConfig.MAX_TAP_DISTANCE * getResources().getDisplayMetrics().density);

                    if (dx < maxTapDistancePx && dy < maxTapDistancePx && dt < QuickTapConfig.MAX_TAP_DURATION) {
                        if (quickTapListener != null) {
                            quickTapListener.onQuickTap();
                        }
                    }

                    break;
            }

            return true;
        });

        return view;
    }

    /**
     * Sets the current level that was cleared.
     *
     * @param level The level number that was cleared
     */
    public void setCurrentLevel(int level) {
        this.currentLevel = level;
    }

    /**
     * Sets the listener for the next level event.
     *
     * @param listener The listener to be notified when the next level button is clicked.
     */
    public void setNextLevelFragmentListener(NextLevelFragmentListener listener) {
        this.listener = listener;
    }

    /**
     * Sets the quick tap listener.
     *
     * @param quickTapListener The listener to handle quick tap events.
     */
    public void setQuickTapListener(QuickTapListener quickTapListener) {
        this.quickTapListener = quickTapListener;
    }

    /**
     * Interface for handling the event of proceeding to the next level.
     */
    public interface NextLevelFragmentListener {

        /**
         * Called when the next level button is clicked.
         */
        void onNextLevel();

    }

}