package com.example.bricks;

/**
 * Enum representing different types of game events.
 * These events are triggered during gameplay based on specific interactions.
 */
public enum GameEventType {
    BORDER_HIT, // Event triggered when the ball hits the border of the game area
    BRICK_HIT, // Event triggered when the ball hits a brick
    PLATFORM_HIT, // Event triggered when the ball hits the platform
    FLOOR_HIT, // Event triggered when the ball hits the floor
}
