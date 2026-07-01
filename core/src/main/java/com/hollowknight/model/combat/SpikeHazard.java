package com.hollowknight.model.combat;

import com.badlogic.gdx.math.Rectangle;

/**
 * Temporary spike surface used to test pogo collision.
 *
 * Player damage from touching spikes will be connected
 * when the health and invincibility system is added.
 */
public final class SpikeHazard
    implements Pogoable {

    private static final float WIDTH = 120f;
    private static final float HEIGHT = 32f;

    private static final float FLASH_DURATION =
        0.10f;

    private final Rectangle bounds;

    private float flashTimeRemaining;
    private int pogoCount;

    public SpikeHazard() {
        bounds = new Rectangle(
            0f,
            0f,
            WIDTH,
            HEIGHT
        );

        flashTimeRemaining = 0f;
        pogoCount = 0;
    }

    public void update(
        float delta,
        float worldWidth,
        float groundY
    ) {
        updatePosition(
            worldWidth,
            groundY
        );

        if (flashTimeRemaining > 0f) {
            flashTimeRemaining -= delta;

            if (flashTimeRemaining < 0f) {
                flashTimeRemaining = 0f;
            }
        }
    }

    private void updatePosition(
        float worldWidth,
        float groundY
    ) {
        float preferredX =
            worldWidth * 0.43f
                - WIDTH / 2f;

        float maximumX =
            Math.max(
                30f,
                worldWidth
                    - WIDTH
                    - 30f
            );

        float spikeX =
            Math.max(
                30f,
                Math.min(
                    preferredX,
                    maximumX
                )
            );

        bounds.setPosition(
            spikeX,
            groundY
        );
    }

    @Override
    public Rectangle getPogoBounds() {
        return bounds;
    }

    @Override
    public boolean canBePogoed() {
        return true;
    }

    @Override
    public void onPogo() {
        pogoCount++;

        flashTimeRemaining =
            FLASH_DURATION;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isFlashing() {
        return flashTimeRemaining > 0f;
    }

    public int getPogoCount() {
        return pogoCount;
    }
}
