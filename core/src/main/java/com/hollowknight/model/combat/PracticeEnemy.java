package com.hollowknight.model.combat;

import com.badlogic.gdx.math.Rectangle;

/**
 * Temporary enemy used to test the reusable combat system.
 *
 * Later, real enemy classes can implement Damageable
 * and Pogoable in exactly the same way.
 */
public final class PracticeEnemy
    implements Damageable, Pogoable {

    private static final float WIDTH = 72f;
    private static final float HEIGHT = 86f;

    private static final int MAX_HEALTH = 4;

    private static final float FLASH_DURATION =
        0.12f;

    private static final float RESPAWN_DELAY =
        1.50f;

    private final Rectangle bounds;

    private int health;
    private int totalHits;

    private float flashTimeRemaining;
    private float respawnTimeRemaining;

    public PracticeEnemy() {
        bounds = new Rectangle(
            0f,
            0f,
            WIDTH,
            HEIGHT
        );

        health = MAX_HEALTH;
        totalHits = 0;

        flashTimeRemaining = 0f;
        respawnTimeRemaining = 0f;
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

        updateFlash(delta);
        updateRespawn(delta);
    }

    private void updatePosition(
        float worldWidth,
        float groundY
    ) {
        float preferredX =
            worldWidth * 0.73f
                - WIDTH / 2f;

        float maximumX =
            Math.max(
                30f,
                worldWidth
                    - WIDTH
                    - 30f
            );

        float enemyX =
            Math.max(
                30f,
                Math.min(
                    preferredX,
                    maximumX
                )
            );

        bounds.setPosition(
            enemyX,
            groundY
        );
    }

    private void updateFlash(float delta) {
        if (flashTimeRemaining <= 0f) {
            return;
        }

        flashTimeRemaining -= delta;

        if (flashTimeRemaining < 0f) {
            flashTimeRemaining = 0f;
        }
    }

    private void updateRespawn(float delta) {
        if (isAlive()) {
            return;
        }

        respawnTimeRemaining -= delta;

        if (respawnTimeRemaining <= 0f) {
            respawn();
        }
    }

    @Override
    public void takeDamage(int damage) {
        if (
            !isAlive()
                || damage <= 0
        ) {
            return;
        }

        health = Math.max(
            0,
            health - damage
        );

        totalHits++;

        flashTimeRemaining =
            FLASH_DURATION;

        if (!isAlive()) {
            respawnTimeRemaining =
                RESPAWN_DELAY;
        }
    }

    @Override
    public void onPogo() {
        if (!isAlive()) {
            return;
        }

        flashTimeRemaining =
            FLASH_DURATION;
    }

    private void respawn() {
        health = MAX_HEALTH;

        flashTimeRemaining = 0f;
        respawnTimeRemaining = 0f;
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public Rectangle getPogoBounds() {
        return bounds;
    }

    @Override
    public boolean canBePogoed() {
        return isAlive();
    }

    @Override
    public boolean isAlive() {
        return health > 0;
    }

    public boolean isFlashing() {
        return flashTimeRemaining > 0f;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return MAX_HEALTH;
    }

    public int getTotalHits() {
        return totalHits;
    }
}
