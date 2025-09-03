package com.example.bricks;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Represents the ball object in the game.
 * The ball moves around the screen, bounces off walls, the platform, and bricks.
 */
public class Ball {

    private final float radius; // Radius of the ball
    private final Paint paint; // Paint object for drawing the ball
    private float x; // X-coordinate of the ball's center
    private float y; // Y-coordinate of the ball's center
    private float xSpeed; // Speed of the ball in the X direction
    private float ySpeed; // Speed of the ball in the Y direction

    /**
     * Constructs a new Ball object.
     *
     * @param x      The initial x-coordinate of the ball's center.
     * @param y      The initial y-coordinate of the ball's center.
     * @param radius The radius of the ball.
     * @param xSpeed The initial horizontal speed of the ball.
     * @param ySpeed The initial vertical speed of the ball.
     */
    public Ball(float x, float y, float radius, float xSpeed, float ySpeed) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;
        paint = new Paint();
        paint.setColor(Color.rgb(255, 105, 180)); // Sets the ball color to pink
    }

    /**
     * Updates the ball's position and handles collisions with walls.
     */
    public void update() {
        // Update ball position
        x += xSpeed;
        y += ySpeed;

        // Check for collisions with walls
        if (x - radius <= 0 || x + radius >= GameView.screenWidth) {
            // Bounce off wall
            xSpeed *= -1; // Reverse horizontal direction
        }
        if (y - radius <= 0) {
            // Bounce off ceiling
            ySpeed *= -1; // Reverse vertical direction
        }
    }

    /**
     * Checks if the ball is out of the screen bounds.
     *
     * @return True if the ball is out of the screen, false otherwise.
     */
    public boolean checkOutOfScreen() {
        boolean isOutOfScreen = x - radius <= 0 || x + radius >= GameView.screenWidth;

        // Check for collisions with walls
        if (y - radius <= 0) {
            isOutOfScreen = true;
        }

        return isOutOfScreen;
    }

    /**
     * Bounces the ball off the platform.
     *
     * @param platform The platform object to bounce off.
     */
    public void bounceOffPlatform(Platform platform) {
        float ballCenterX = x;
        float platformCenterX = platform.getRect().centerX();

        xSpeed = (ballCenterX - platformCenterX) / 10; // Adjust horizontal speed based on collision point
        ySpeed *= -1; // Reverse vertical direction
    }

    /**
     * Bounces the ball off a brick.
     */
    public void bounceOffBrick() {
        ySpeed *= -1; // Reverse vertical direction
    }

    /**
     * Gets the rectangle representing the ball's bounds.
     *
     * @return A RectF object representing the ball's bounds.
     */
    public RectF getRect() {
        return new RectF(x - radius, y - radius, x + radius, y + radius);
    }

    /**
     * Draws the ball on the given canvas.
     *
     * @param canvas The canvas on which to draw the ball.
     */
    public void draw(Canvas canvas) {
        canvas.drawCircle(x, y, radius, paint);
    }

}