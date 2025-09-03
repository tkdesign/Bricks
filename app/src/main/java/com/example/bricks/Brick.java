package com.example.bricks;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Represents a brick in the game.
 * Bricks are obstacles that the ball can collide with and destroy.
 */
public class Brick {

    private final RectF rect; // Rectangle representing the brick's position and size
    private final Paint paint; // Paint object for drawing the brick

    /**
     * Constructs a new Brick object.
     *
     * @param x      The x-coordinate of the brick's top-left corner.
     * @param y      The y-coordinate of the brick's top-left corner.
     * @param width  The width of the brick.
     * @param height The height of the brick.
     * @param color  The color code of the brick.
     */
    public Brick(float x, float y, float width, float height, int color) {
        rect = new RectF(x, y, x + width, y + height);
        paint = new Paint();

        // Set the brick color based on the provided color code
        switch (color) {
            case 0:
                paint.setColor(0xFF708090); // Slate gray
                break;
            case 1:
                paint.setColor(0xFFB8860B); // Dark goldenrod
                break;
            case 2:
                paint.setColor(0xFFCD5C5C); // Indian red
                break;
            case 3:
                paint.setColor(0xFF228B22); // Forest green
                break;
            case 4:
                paint.setColor(0xFF5F9EA0); // Cadet blue
                break;
            case 5:
                paint.setColor(0xFFF4A460); // Sandy brown
                break;
            case 6:
                paint.setColor(0xFFFF8C00); // Dark orange
                break;
            default:
                paint.setColor(0xFF708090); // Default to slate gray
                break;
        }
    }

    /**
     * Draws the brick on the given canvas.
     *
     * @param canvas The canvas on which to draw the brick.
     */
    public void draw(Canvas canvas) {
        canvas.drawRect(rect, paint);
    }

    /**
     * Returns the rectangle representing the brick's position and size.
     *
     * @return A RectF object representing the brick.
     */
    public RectF getRect() {
        return rect;
    }

}