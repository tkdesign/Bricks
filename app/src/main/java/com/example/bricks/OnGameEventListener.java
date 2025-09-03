package com.example.bricks;

/**
 * Interface for handling game events.
 * Implementations of this interface define actions to be taken when specific game events occur.
 */
public interface OnGameEventListener {

    /**
     * Called when a game event is triggered.
     *
     * @param eventType The type of game event that occurred.
     */
    void onEvent(GameEventType eventType);

}
