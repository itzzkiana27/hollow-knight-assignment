package com.hollowknight.model.combat;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

/*
 * Temporary target used until the real enemies
 * and spike collision system are added.
 */
public final class PracticeTarget {

    private static final float WIDTH = 72f;
    private static final float HEIGHT = 86f;

    private static final float FLASH_DURATION =
        0.12f;

    private final Rectangle bounds;

    private float flashTimeRemaining;
    private int hitCount;
    private boolean positioned;

    public PracticeTarget() {
        bounds = new Rectangle(
            0f,
            0f,
            WIDTH,
            HEIGHT
        );

        flashTimeRemaining = 0f;
        hitCount = 0;
        positioned = false;
    }

    public void update(
        float delta,
        float worldWidth,
        float groundY
    ) {
        if (!positioned) {
            float preferredX =
                worldWidth * 0.68f;

            float maximumX = Math.max(
                20f,
                worldWidth - WIDTH - 30f
            );

            float minimumX = Math.min(
                320f,
                maximumX
            );

            bounds.x = MathUtils.clamp(
                preferredX,
                minimumX,
                maximumX
            );

            bounds.y = groundY;
            positioned = true;
        } else if (
            bounds.x + bounds.width
                > worldWidth - 20f
        ) {
            bounds.x = Math.max(
                20f,
                worldWidth
                    - bounds.width
                    - 20f
            );
        }

        if (flashTimeRemaining > 0f) {
            flashTimeRemaining -= delta;

            if (flashTimeRemaining < 0f) {
                flashTimeRemaining = 0f;
            }
        }
    }

    public void hit() {
        hitCount++;
        flashTimeRemaining =
            FLASH_DURATION;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isFlashing() {
        return flashTimeRemaining > 0f;
    }

    public int getHitCount() {
        return hitCount;
    }
}
