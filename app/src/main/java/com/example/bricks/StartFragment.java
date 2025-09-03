package com.example.bricks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
 * A fragment representing the start screen of the Bricks game.
 * It includes a start button that plays a sound and triggers an animation before starting the game.
 */
public class StartFragment extends Fragment implements AnimationInterface {

    private StartFragmentListener listener; // Listener for handling start game events
    private SoundManager soundManager; // SoundManager instance for managing sound effects
    private QuickTapListener quickTapListener; // Listener for quick tap events (for opening settings panel)
    private float downX, downY; // Coordinates of the initial touch down event for quick tap detection
    private long downTime; // Timestamp of the touch down event for quick tap detection

    /**
     * Sets the listener for start game events.
     *
     * @param listener The listener to be notified when the start button is clicked.
     */
    public void setStartFragmentListener(StartFragmentListener listener) {
        this.listener = listener;
    }

    /**
     * Inflates the layout for this fragment and sets up the start button with its click listener.
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
        View view = inflater.inflate(R.layout.fragment_start, container, false);

        // Display the current app version
        TextView tv = view.findViewById(R.id.versionTextView);
        Context context = getContext();
        if (context != null) {
            PackageManager pm = getContext().getPackageManager();
            PackageInfo p;

            try {
                p = pm.getPackageInfo(getContext().getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }

            String vName = p.versionName;
            long vCode = p.getLongVersionCode();
            tv.setText(String.format(getString(R.string.current_version_name), vName, vCode));
        } else {
            tv.setText("");
        }

        // Set up the start button
        Button startButton = view.findViewById(R.id.startButton);
        startButton.setOnClickListener(v -> {
            if (listener != null) {
                // Play the button click sound
                soundManager.play(R.raw.button_click);

                // Animate the button
                animateButton(startButton);

                // Delay the start game event to allow the animation to complete
                new Handler(Looper.getMainLooper()).postDelayed(() -> listener.onStartGame(), 300);
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
     * Sets the quick tap listener.
     *
     * @param quickTapListener The listener to handle quick tap events.
     */
    public void setQuickTapListener(QuickTapListener quickTapListener) {
        this.quickTapListener = quickTapListener;
    }

    /**
     * Interface for handling start game events.
     */
    public interface StartFragmentListener {

        /**
         * Called when the start button is clicked to begin the game.
         */
        void onStartGame();

    }

}