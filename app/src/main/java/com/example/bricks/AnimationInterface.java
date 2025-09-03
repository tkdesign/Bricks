package com.example.bricks;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Interface providing a default animation method for buttons.
 * This interface can be implemented by classes to add button animation functionality.
 */
public interface AnimationInterface {

    /**
     * Animates a button by scaling it up and down.
     * The animation creates a "pulsing" effect using scale transformations.
     *
     * @param button The button view to be animated.
     */
    default void animateButton(View button) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 1.1f, 1f);
        scaleX.setDuration(150); // Duration of the X-axis scaling animation in milliseconds
        scaleY.setDuration(150); // Duration of the Y-axis scaling animation in milliseconds
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());// Smooth acceleration and deceleration
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator()); // Smooth acceleration and deceleration
        scaleX.start(); // Start the X-axis animation
        scaleY.start(); // Start the Y-axis animation
    }

}
