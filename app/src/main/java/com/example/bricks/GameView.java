package com.example.bricks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom view for the Bricks game.
 * Handles game logic, rendering, and user interactions.
 */
public class GameView extends View {

    private static final int BRICK_PER_ROW = 10; // Number of bricks per row
    private static final int SPACE_BETWEEN_BRICKS = 5; // Space between bricks in pixels
    private final int MAX_LEVELS = 3; // Total number of levels in the game
    private final int MAX_ATTEMPTS = 3; // Maximum number of attempts per game
    private int currentLevel = 1; // Current game level
    private int attempts; // Remaining attempts
    private boolean isPlaying = false; // Indicates if the game is currently running
    private boolean levelCleared = false; // Indicates if the current level is cleared
    private boolean gameCompleted = false; // Indicates if the game is completed
    private boolean gameOvered = false; // Indicates if the game is over

    private boolean musicOn = true; // Flag to toggle background music
    private boolean sfxOn = true; // Flag to toggle sound effects

    private int score = 0; // Player's score (count of destroyed bricks)
    private final Paint paint; // Paint object for drawing
    private Ball ball; // Ball object
    private Platform platform; // Platform object
    private final List<Brick> bricks; // List of bricks in the current level

    public static int screenWidth; // Screen width in pixels
    public int screenHeight; // Screen height in pixels

    private float startTouchX; // X-coordinate of the initial touch
    private final int PLATFORM_WIDTH_BASE = 70; // Base width of the platform
    private final int PLATFORM_HEIGHT_BASE = 15; // Base height of the platform
    private final int BALL_X_SPEED = 10; // Initial horizontal speed of the ball
    private final int BALL_Y_SPEED = 10; // Initial vertical speed of the ball
    private final PlayListController playListController; // Custom controller for background music
    private OnGameEventListener onGameEventListener; // Listener for game events (for sound effects)
    private final SoundManager soundManager; // SoundManager instance for managing sound effects
    private QuickTapListener quickTapListener; // Listener for quick tap events (for opening settings panel)
    private float downX, downY; // Coordinates of the initial touch down event for quick tap detection
    private long downTime; // Timestamp of the touch down event for quick tap detection

    /**
     * Constructor for initializing the GameView.
     *
     * @param context The context of the application.
     * @param attrs   The attribute set for the view.
     */
    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        bricks = new ArrayList<>();

        playListController = new PlayListController(this.getContext());

        // Initialize SoundPool for sound effects
        soundManager = SoundManager.getInstance();

        // Load sound effects
        soundManager.loadSound(context, R.raw.border_hit);
        soundManager.loadSound(context, R.raw.brick_hit);
        soundManager.loadSound(context, R.raw.platform_hit);
        soundManager.loadSound(context, R.raw.floor_hit);

        // Set up the listener for handling sound effects on game events
        this.setOnGameEventListener(eventType -> {
            if (!sfxOn) {
                return;
            }

            switch (eventType) {
                case PLATFORM_HIT:
                    soundManager.play(R.raw.platform_hit);
                    break;
                case BRICK_HIT:
                    soundManager.play(R.raw.brick_hit);
                    break;
                case BORDER_HIT:
                    soundManager.play(R.raw.border_hit);
                    break;
                case FLOOR_HIT:
                    soundManager.play(R.raw.floor_hit);
                    break;
            }
        });

        initialize();
    }

    /**
     * Initializes the game by setting screen dimensions and resetting attempts.
     */
    private void initialize() {
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        attempts = MAX_ATTEMPTS;
    }

    /**
     * Updates the game state, including ball movement and collision detection.
     */
    public void update() {
        if (!isPlaying) {
            return;
        }

        if (ball.checkOutOfScreen()) {
            onGameEventListener.onEvent(GameEventType.BORDER_HIT);
        }

        ball.update();
        checkCollisions();
    }

    /**
     * Draws the game objects on the canvas.
     *
     * @param canvas The canvas on which to draw.
     */
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // Save old paint settings
        int oldColor = paint.getColor();
        Paint.Align oldAlign = paint.getTextAlign();
        float oldTextSize = paint.getTextSize();

        if (!isPlaying) {
            // Draw pause screen
            canvas.drawColor(Color.BLACK);
            paint.setColor(Color.WHITE);
            paint.setTextSize((float) screenWidth / 10);
            String pauseMsg = getContext().getString(R.string.game_paused_msg);
            float x_pos = (float) screenWidth / 2 - paint.measureText(pauseMsg) / 2;
            canvas.drawText(pauseMsg, x_pos, (float) screenHeight / 2, paint);


            // Draw bottom-centered hint
            paint.setTextAlign(Paint.Align.CENTER);
            float textPx = 16f * getResources().getDisplayMetrics().scaledDensity; // 16sp
            paint.setTextSize(textPx);
            Paint.FontMetrics fm = paint.getFontMetrics();
            float marginPx = getResources().getDisplayMetrics().density * 12f; // 12dp
            float baselineY = screenHeight - marginPx - fm.bottom;
            paint.setColor(getThemeDefaultTextColor()); // Use theme default text color
            String tapToResumeMsg = getContext().getString(R.string.tap_to_resume_text);
            canvas.drawText(tapToResumeMsg, (float) screenWidth / 2f, baselineY, paint);

            // Restore old paint settings
            paint.setColor(oldColor);
            paint.setTextAlign(oldAlign);
            paint.setTextSize(oldTextSize);

            return;
        }

        // Draw game objects
        canvas.drawColor(Color.BLACK);
        ball.draw(canvas);
        platform.draw(canvas);

        for (Brick brick : bricks) {
            brick.draw(canvas);
        }

        // Draw score and attempts
        paint.setColor(Color.WHITE);
        paint.setTextSize((float) screenWidth / 20);

        String scoreText = getContext().getString(R.string.score_metric) + score;
        String attemptsText = getContext().getString(R.string.attempts_metric) + attempts;

        canvas.drawText(scoreText.toUpperCase(), 10, 80, paint);
        float x_pos = (float) screenWidth - paint.measureText(attemptsText.toUpperCase()) - 10;

        canvas.drawText(attemptsText.toUpperCase(), x_pos, 80, paint);

        // Restore old paint settings
        paint.setColor(oldColor);
        paint.setTextAlign(oldAlign);
        paint.setTextSize(oldTextSize);
    }

    private int getThemeDefaultTextColor() {
        TextView tv = new TextView(getContext());
        return tv.getTextColors().getDefaultColor();
    }

    /**
     * Checks for collisions between the ball, platform, bricks, and screen borders.
     */
    private void checkCollisions() {
        if (!isPlaying) {
            return;
        }

        if (RectF.intersects(ball.getRect(), platform.getRect())) {
            if (onGameEventListener != null) {
                onGameEventListener.onEvent(GameEventType.PLATFORM_HIT);
            }
            ball.bounceOffPlatform(platform);
        } else if (ball.getRect().top > getHeight()) {
            // Ball hits the floor
            if (onGameEventListener != null) {
                onGameEventListener.onEvent(GameEventType.FLOOR_HIT);
            }

            attempts = attempts - 1;

            if (attempts == 0) {
                gameOvered = true;
                stopGame();
            } else {
                resetLevel(false); // Reset level without reloading level map
            }
        } else {
            for (int i = 0; i < bricks.size(); i++) {
                if (ball.getRect().intersect(bricks.get(i).getRect())) {
                    if (onGameEventListener != null) {
                        onGameEventListener.onEvent(GameEventType.BRICK_HIT);
                    }
                    ball.bounceOffBrick();
                    bricks.remove(i);
                    score += 10;
                    break;
                }
            }

            // Check if all bricks are destroyed
            if (bricks.isEmpty()) {
                if (currentLevel == MAX_LEVELS) {
                    gameCompleted = true; // Game completed if all levels are cleared
                    stopGame();
                } else {
                    levelCleared = true; // Level cleared
                    isPlaying = false;

                    if (playListController.isPlaying()) {
                        playListController.stop();
                    }
                }
            }
        }
    }

    /**
     * Start next level.
     */
    public void startNextLevel() {
        if (currentLevel < MAX_LEVELS) {
            // Load the next level
            currentLevel++;
            resetLevel(true);
            isPlaying = true;

            if (musicOn && !playListController.isPlaying()) {
                playListController.playShuffle();
            }
        } else {
            // Reset the game if all levels are cleared
            stopGame();
        }
    }

    /**
     * Loads the level map from the assets folder.
     *
     * @param level The level number to load.
     */
    private void loadLevel(int level) {
        bricks.clear();
        String levelPath = String.format(getContext().getString(R.string.level_d_txt), level); // Path to level map file

        try (
                InputStream inputStream = getContext().getAssets().open(levelPath);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))
        ) {
            String line;
            int row = 0;

            while ((line = reader.readLine()) != null) {
                for (int col = 0; col < line.length(); col++) {
                    char currentChar = line.charAt(col);

                    if (currentChar != ' ') {
                        int colorCode = Character.getNumericValue(currentChar);

                        // Calculate brick width and height based on screen width and height
                        Brick brick = getBrick(col, row, colorCode);
                        bricks.add(brick);
                    }
                }

                row++;
            }
        } catch (IOException e) {
            Log.e("GameView", "Error loading level", e);
        }
    }

    /**
     * Creates a brick object based on its column, row, and color code.
     *
     * @param col The column of the brick.
     * @param row The row of the brick.
     * @param colorCode The color code of the brick.
     * @return A new Brick object.
     */
    @NonNull
    private static Brick getBrick(int col, int row, int colorCode) {
        float brick_width = ((float) (screenWidth - BRICK_PER_ROW * SPACE_BETWEEN_BRICKS)) / (float) BRICK_PER_ROW;
        int BRICK_WIDTH_BASE = 40;
        int BRICK_HEIGHT_BASE = 20;
        float brick_height = brick_width * ((float) BRICK_HEIGHT_BASE / (float) BRICK_WIDTH_BASE);

        // Create and return brick object
        return new Brick((int) (col * (brick_width + SPACE_BETWEEN_BRICKS)), (int) (row * (brick_height + SPACE_BETWEEN_BRICKS)), (int) brick_width, (int) brick_height, colorCode);
    }

    /**
     * Resets the current level.
     *
     * @param reloadLevelMap If true, reloads the level map; otherwise, keeps the current map.
     */
    private void resetLevel(boolean reloadLevelMap) {
        ball = new Ball((float) screenWidth / 2, (float) screenHeight / 2, (float) screenWidth / 50, BALL_X_SPEED, BALL_Y_SPEED); // Create new ball

        float platform_width = (float) screenWidth * (PLATFORM_WIDTH_BASE / 400.0f);
        float platform_height = (float) screenHeight * (PLATFORM_HEIGHT_BASE / 600.0f);
        platform = new Platform((float) screenWidth / 2 - platform_width / 2, screenHeight - platform_height - 80, platform_width, platform_height); // Create new platform

        if (reloadLevelMap) {
            loadLevel(currentLevel); // Reload level map or load next level map
        }

        // Reset flags
        isPlaying = true;
        levelCleared = false;
        gameCompleted = false;
        gameOvered = false;
    }

    /**
     * Starts a new game by resetting all counters and loading the first level.
     */
    public void startGame() {
        // Reset game counters
        currentLevel = 1;
        attempts = MAX_ATTEMPTS;
        score = 0;

        // Load first level map
        resetLevel(true);

        if (musicOn && !playListController.isPlaying()) {
            playListController.playShuffle();
        }
    }

    /**
     * Stops the game and music playback.
     */
    public void stopGame() {
        isPlaying = false;
        levelCleared = false;

        if (playListController.isPlaying()) {
            playListController.stop();
        }
    }

    /**
     * Gets the current level.
     *
     * @return The current level number.
     */
    public int getCurrentLevel() {
        return currentLevel;
    }

    /**
     * Checks if the game is currently playing.
     *
     * @return True if the game is playing, false otherwise.
     */
    public boolean isPlaying() {
        return isPlaying;
    }

    /**
     * Sets the playing state of the game.
     *
     * @param isPlaying True to start playing, false to pause.
     */
    public void setIsPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    /**
     * Whether the current level is cleared.
     *
     * @return True if the level is cleared, false otherwise.
     */
    public boolean isLevelCleared() {
        return levelCleared;
    }

    /**
     * Whether the game is completed.
     *
     * @return True if the game is completed, false otherwise.
     */
    public boolean isGameCompleted() {
        return gameCompleted;
    }

    /**
     * Whether the game is overed.
     *
     * @return True if the game is overed, false otherwise.
     */
    public boolean isGameOvered() {
        return gameOvered;
    }

    /**
     * Sets the game event listener.
     *
     * @param listener The listener to handle game events for sound effects.
     */
    public void setOnGameEventListener(OnGameEventListener listener) {
        this.onGameEventListener = listener;
    }

    /**
     * Toggles sound effects state on or off.
     *
     * @param sfxOn True to enable sound effects, false to disable.
     */
    public void setSfxOn(boolean sfxOn) {
        this.sfxOn = sfxOn;
    }

    /**
     * Toggles background music state on or off.
     *
     * @param musicOn True to enable music, false to disable.
     */
    public void setMusicOn(boolean musicOn) {
        this.musicOn = musicOn;
    }

    /**
     * Toggles background music state on or off and starts/stops playback accordingly.
     *
     * @param musicOn True to enable music, false to disable.
     */
    public void switchMusicPlayback(boolean musicOn) {
        if (playListController == null) {
            return;
        }

        this.musicOn = musicOn;

        if (musicOn) {
            playListController.resume();
        } else {
            playListController.pause();
        }
    }

    /**
     * Resumes background music playback.
     */
    public void pauseMusicPlayback() {
        if (playListController != null) {
            playListController.pause();
        }
    }

    /**
     * Sets the quick tap listener.
     *
     * @param l The listener to handle quick tap events.
     */
    public void setQuickTapListener(QuickTapListener l) {
        this.quickTapListener = l;
    }

    /**
     * Handles touch events for user interaction.
     *
     * @param event The motion event.
     * @return True if the event was handled, false otherwise.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                downTime = event.getEventTime();
                startTouchX = downX;
                break;
            case MotionEvent.ACTION_MOVE:
                float currentTouchX = event.getX();
                float deltaX = currentTouchX - startTouchX;

                if (isPlaying) {
                    platform.setX(platform.getX() + deltaX);
                }

                startTouchX = currentTouchX;
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
    }

}
