package com.example.bricks;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.annotation.NonNull;

/**
 * Represents the platform object in the game.
 * The platform is controlled by the player and is used to bounce the ball.
 */
public class Platform {

    private final RectF rect; // Platform position and size
    private final Paint paint; // Paint object for drawing the platform

//    private final float cornerRadius; // Corner radius for rounded platform (currently unused)

    /**
     * Constructs a new Platform object.
     *
     * @param x      The x-coordinate of the platform's top-left corner.
     * @param y      The y-coordinate of the platform's top-left corner.
     * @param width  The width of the platform.
     * @param height The height of the platform.
     */
    public Platform(float x, float y, float width, float height) {
        rect = new RectF(x, y, x + width, y + height);
        paint = new Paint();
        paint.setColor(0xFFFF8C00); // Sets the platform color to orange
//        cornerRadius = height / 2; // Calculates the corner radius (currently unused)
    }

    /**
     * Draws the platform on the provided canvas.
     *
     * @param canvas The canvas on which to draw the platform.
     */
    public void draw(@NonNull Canvas canvas) {
        canvas.drawRect(rect, paint);
//        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint); // Draws a rounded rectangle (currently unused)
    }

    /**
     * Gets the rectangle representing the platform's position and size.
     *
     * @return A RectF object representing the platform.
     */
    public RectF getRect() {
        return rect;
    }

    /**
     * Gets the current x-coordinate of the platform.
     *
     * @return The x-coordinate of the platform's left edge.
     */
    public float getX() {
        return rect.left;
    }

    /**
     * Updates the x-coordinate of the platform.
     *
     * @param x The new x-coordinate for the platform's top-left corner.
     */
    public void setX(float x) {
        rect.offsetTo(x, rect.top);
    }

}
