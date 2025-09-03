package com.example.bricks;

/**
 * Configuration constants for quick tap detection.
 */
public final class QuickTapConfig {

    public static final int MAX_TAP_DURATION = 180; // Maximum duration for a quick tap in milliseconds
    public static final int MAX_TAP_DISTANCE = 20; // Maximum movement allowed for a quick tap in pixels

    /**
     * Private constructor to prevent instantiation
     */
    private QuickTapConfig() {
    }

}
