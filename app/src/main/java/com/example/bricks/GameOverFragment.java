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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * A fragment representing the game over screen.
 * It includes a button to restart the game and plays a sound effect when the button is clicked.
 */
public class GameOverFragment extends Fragment implements AnimationInterface {

    private GameOverFragmentListener listener; // Listener for handling the restart game event
    private SoundManager soundManager; // SoundManager instance for managing sound effects
    private QuickTapListener quickTapListener; // Listener for quick tap events (for opening settings panel)
    private float downX, downY; // Coordinates of the initial touch down event for quick tap detection
    private long downTime; // Timestamp of the touch down event for quick tap detection

    /**
     * Inflates the layout for this fragment and sets up the "Restart Game" button with its click
     * listener.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate views in the fragment.
     * @param container          The parent view that this fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous
     *                           saved state.
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
        View view = inflater.inflate(R.layout.fragment_game_over, container, false);

        // Set up the "Restart Game" button
        Button restartButton = view.findViewById(R.id.restartButton);
        restartButton.setOnClickListener(v -> {
            if (listener != null) {
                // Play the button click sound
                soundManager.play(R.raw.button_click);

                // Animate the button
                animateButton(restartButton);

                // Delay the restart game event to allow the animation to complete
                new Handler(Looper.getMainLooper()).postDelayed(() -> listener.onRestartGame(), 300);
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
     * Sets the listener for the restart game event.
     *
     * @param listener The listener to be notified when the restart game button is clicked.
     */
    public void setGameOverFragmentListener(GameOverFragmentListener listener) {
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
     * Interface for handling the restart game event.
     */
    public interface GameOverFragmentListener {

        /**
         * Called when the restart game button is clicked.
         */
        void onRestartGame();

    }

}