package com.hollowknight.model.enemy;

import com.badlogic.gdx.math.Rectangle;
import com.hollowknight.model.combat.Damageable;
import com.hollowknight.model.combat.Pogoable;
import com.hollowknight.model.world.PlatformWorld;

/**
 * Simple ground enemy used in the Forgotten
 * Crossroads environment.
 *
 * It walks at a constant speed and does not react
 * to the player's position. It turns around when
 * encountering a wall, world boundary, or pit.
 */
public final class Crawlid
    implements Damageable, Pogoable {

    /*
     * The source frame is 301 x 149, but most of it
     * is transparent. These are the physical body
     * dimensions used for gameplay collision.
     */
    private static final float BODY_WIDTH = 82f;
    private static final float BODY_HEIGHT = 58f;

    /*
     * A simple enemy should have less health and
     * speed than the more advanced Husk Hornhead.
     */
    private static final int MAX_HEALTH = 2;
    private static final int CONTACT_DAMAGE = 1;

    private static final float MOVE_SPEED = 60f;

    private static final float TURN_DURATION =
        0.20f;

    /*
     * Two death-land frames at 0.12 seconds each.
     */
    private static final float DEATH_DURATION =
        0.24f;

    private static final float FLASH_DURATION =
        0.12f;

    private static final float
        GROUND_LOOK_AHEAD = 8f;

    private static final float
        GROUND_PROBE_DEPTH = 14f;

    private static final float
        RESPAWN_DISTANCE = 900f;

    private static final float
        RESPAWN_DISTANCE_SQUARED =
        RESPAWN_DISTANCE * RESPAWN_DISTANCE;

    private final Rectangle bounds;
    private final Rectangle nextBounds;

    private final float spawnX;
    private final float spawnY;

    private final boolean initialFacingRight;

    private CrawlidState state;

    private int health;

    private float stateTime;
    private float flashTimeRemaining;

    private boolean facingRight;

    public Crawlid(
        float spawnX,
        float spawnY,
        boolean initialFacingRight
    ) {
        this.spawnX = spawnX;
        this.spawnY = spawnY;

        this.initialFacingRight =
            initialFacingRight;

        bounds = new Rectangle(
            spawnX,
            spawnY,
            BODY_WIDTH,
            BODY_HEIGHT
        );

        nextBounds = new Rectangle();

        respawn();
    }

    public void update(
        float delta,
        Rectangle playerBounds,
        PlatformWorld platformWorld
    ) {
        float safeDelta = Math.min(
            Math.max(delta, 0f),
            1f / 30f
        );

        updateFlash(safeDelta);

        if (state == CrawlidState.CORPSE) {
            if (
                shouldRespawn(
                    playerBounds
                )
            ) {
                respawn();
            }

            return;
        }

        if (state == CrawlidState.DYING) {
            updateDeath(safeDelta);
            return;
        }

        stateTime += safeDelta;

        switch (state) {
            case WALKING ->
                updateWalking(
                    safeDelta,
                    platformWorld
                );

            case TURNING ->
                updateTurning();

            case DYING, CORPSE -> {
                /*
                 * These states were handled above.
                 */
            }
        }
    }

    private void updateWalking(
        float delta,
        PlatformWorld platformWorld
    ) {
        int direction =
            getFacingDirection();

        if (
            !canMoveForward(
                direction,
                delta,
                platformWorld
            )
        ) {
            changeState(
                CrawlidState.TURNING
            );

            return;
        }

        bounds.x +=
            direction
                * MOVE_SPEED
                * delta;
    }

    private void updateTurning() {
        if (
            stateTime < TURN_DURATION
        ) {
            return;
        }

        facingRight = !facingRight;

        changeState(
            CrawlidState.WALKING
        );
    }

    private void updateDeath(
        float delta
    ) {
        stateTime += delta;

        if (
            stateTime < DEATH_DURATION
        ) {
            return;
        }

        state = CrawlidState.CORPSE;

        /*
         * Freezing the animation time at the end keeps
         * the final corpse frame visible permanently.
         */
        stateTime = DEATH_DURATION;
    }

    private boolean canMoveForward(
        int direction,
        float delta,
        PlatformWorld platformWorld
    ) {
        nextBounds.set(bounds);

        nextBounds.x +=
            direction
                * MOVE_SPEED
                * delta;

        /*
         * World boundaries are treated like walls.
         */
        if (
            nextBounds.x < 0f
                || nextBounds.x
                + nextBounds.width
                > platformWorld
                .getWorldWidth()
        ) {
            return false;
        }

        /*
         * Check for a wall or solid object directly
         * in the next position.
         */
        if (
            platformWorld.overlapsSolid(
                nextBounds
            )
        ) {
            return false;
        }

        /*
         * Check that solid ground continues slightly
         * ahead of the Crawlid.
         */
        return platformWorld.hasGroundAhead(
            nextBounds,
            direction,
            GROUND_LOOK_AHEAD,
            GROUND_PROBE_DEPTH
        );
    }

    private void updateFlash(
        float delta
    ) {
        if (
            flashTimeRemaining <= 0f
        ) {
            return;
        }

        flashTimeRemaining -= delta;

        if (
            flashTimeRemaining < 0f
        ) {
            flashTimeRemaining = 0f;
        }
    }

    private boolean shouldRespawn(
        Rectangle playerBounds
    ) {
        if (playerBounds == null) {
            return false;
        }

        float playerCenterX =
            playerBounds.x
                + playerBounds.width / 2f;

        float playerCenterY =
            playerBounds.y
                + playerBounds.height / 2f;

        float spawnCenterX =
            spawnX + BODY_WIDTH / 2f;

        float spawnCenterY =
            spawnY + BODY_HEIGHT / 2f;

        float distanceX =
            playerCenterX - spawnCenterX;

        float distanceY =
            playerCenterY - spawnCenterY;

        return distanceX * distanceX
            + distanceY * distanceY
            >= RESPAWN_DISTANCE_SQUARED;
    }

    private int getFacingDirection() {
        return facingRight ? 1 : -1;
    }

    private void changeState(
        CrawlidState newState
    ) {
        if (state == newState) {
            return;
        }

        state = newState;
        stateTime = 0f;
    }

    /**
     * Called later by the room system when entering
     * a room containing this Crawlid.
     */
    public void respawnForRoomEntry() {
        respawn();
    }

    private void respawn() {
        bounds.set(
            spawnX,
            spawnY,
            BODY_WIDTH,
            BODY_HEIGHT
        );

        health = MAX_HEALTH;

        facingRight =
            initialFacingRight;

        state = CrawlidState.WALKING;

        stateTime = 0f;
        flashTimeRemaining = 0f;
    }

    @Override
    public void takeDamage(
        int damage
    ) {
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

        flashTimeRemaining =
            FLASH_DURATION;

        if (health == 0) {
            state = CrawlidState.DYING;
            stateTime = 0f;
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

    public CrawlidAnimationType
    getAnimationType() {
        return switch (state) {
            case WALKING ->
                CrawlidAnimationType.WALK;

            case TURNING ->
                CrawlidAnimationType.TURN;

            case DYING, CORPSE ->
                CrawlidAnimationType.DEATH_LAND;
        };
    }

    public CrawlidState getState() {
        return state;
    }

    public float getAnimationTime() {
        return stateTime;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public boolean isFlashing() {
        return flashTimeRemaining > 0f;
    }

    public boolean isCorpse() {
        return state == CrawlidState.CORPSE;
    }

    public int getHealth() {
        return health;
    }

    public int getMaximumHealth() {
        return MAX_HEALTH;
    }

    public int getContactDamage() {
        return CONTACT_DAMAGE;
    }

    public float getMovementSpeed() {
        return MOVE_SPEED;
    }
}
