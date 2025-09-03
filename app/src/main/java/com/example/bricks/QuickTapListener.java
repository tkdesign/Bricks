package com.example.bricks;

/**
 * Interface for listening to quick tap events during gameplay.
 * Implementations of this interface define actions to be taken when a quick tap is detected.
 */
public interface QuickTapListener {

    /**
     * Called when a quick tap event occurs.
     */
    void onQuickTap();

}
