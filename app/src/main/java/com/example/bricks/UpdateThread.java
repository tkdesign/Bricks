package com.example.bricks;

import android.os.Handler;

/**
 * A thread responsible for periodically sending update messages to a handler.
 * This is used to manage game updates at a fixed interval.
 */
public class UpdateThread implements Runnable {

    private static final int UPDATE_INTERVAL = 20; // Interval in milliseconds between updates
    private final Handler updateHandler; // Handler to schedule and execute tasks
    private boolean running = false; // Flag to indicate whether the updates are active

    /**
     * Constructs an UpdateThread with the specified handler.
     *
     * @param handler The `Handler` used to schedule and execute tasks.
     */
    public UpdateThread(Handler handler) {
        this.updateHandler = handler;
    }

    /**
     * Starts the periodic updates. This method is idempotent, meaning it has no effect if the
     * updates are already running.
     */
    public synchronized void start() {
        if (running) {
            return;
        }

        running = true;
        updateHandler.post(this);
    }

    /**
     * Stops the periodic updates. This method is idempotent, meaning it has no effect if the
     * updates are already stopped.
     */
    public synchronized void stop() {
        running = false;
        updateHandler.removeCallbacks(this);
    }

    /**
     * Interrupts the update thread, stopping periodic updates.
     */
    public void interrupt() {
        stop();
    }

    /**
     * Checks if the update thread is currently running.
     *
     * @return true if the thread is running, false otherwise.
     */
    public boolean isAlive() {
        return running;
    }

    /**
     * Runs the update loop, sending messages to the handler at fixed intervals.
     * The loop continues until the thread is stopped.
     */
    @Override
    public void run() {
        if (!running) {
            return;
        }

        updateHandler.sendEmptyMessage(0); // Send an update message
        updateHandler.postDelayed(this, UPDATE_INTERVAL); // Schedule the next execution
    }

}
