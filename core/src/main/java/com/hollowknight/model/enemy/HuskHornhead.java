package com.hollowknight.model.enemy;

import com.badlogic.gdx.math.Rectangle;
import com.hollowknight.model.combat.Damageable;
import com.hollowknight.model.combat.Pogoable;
import com.hollowknight.model.world.PlatformWorld;

/**
 * Ground enemy that patrols, rests, detects the player
 * in front, and performs a blind charge.
 */
public final class HuskHornhead
    implements Damageable, Pogoable {

    private static final float BODY_WIDTH = 72f;
    private static final float BODY_HEIGHT = 88f;

    private static final int MAX_HEALTH = 4;
    private static final int CONTACT_DAMAGE = 1;

    private static final float WALK_SPEED = 70f;
    private static final float WALK_DURATION = 2.4f;
    private static final float REST_DURATION = 0.8f;
    private static final float TURN_DURATION = 0.20f;

    private static final float
        ANTICIPATION_DURATION = 0.45f;

    private static final float CHARGE_SPEED = 330f;

    private static final float
        CHARGE_COOLDOWN_DURATION = 0.65f;

    private static final float DEATH_DURATION = 0.80f;
    private static final float FLASH_DURATION = 0.12f;

    private static final float VISION_WIDTH = 360f;
    private static final float VISION_HEIGHT = 115f;
    private static final float VISION_Y_OFFSET = 4f;

    private static final float
        GROUND_LOOK_AHEAD = 10f;

    private static final float
        GROUND_PROBE_DEPTH = 14f;

    private static final float
        RESPAWN_DISTANCE = 900f;

    private static final float
        RESPAWN_DISTANCE_SQUARED =
        RESPAWN_DISTANCE * RESPAWN_DISTANCE;

    private final Rectangle bounds;
    private final Rectangle visionBounds;
    private final Rectangle nextBounds;

    private final float spawnX;
    private final float spawnY;

    private final boolean initialFacingRight;

    private HuskHornheadState state;

    private int health;
    private int chargeDirection;

    private float stateTime;
    private float flashTimeRemaining;

    private boolean facingRight;

    public HuskHornhead(
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

        visionBounds = new Rectangle();
        nextBounds = new Rectangle();

        respawn();
    }

    public void update(
        float delta,
        Rectangle playerBounds,
        boolean playerAlive,
        PlatformWorld platformWorld
    ) {
        float safeDelta = Math.min(
            Math.max(delta, 0f),
            1f / 30f
        );

        updateFlash(safeDelta);

        if (
            state
                == HuskHornheadState.CORPSE
        ) {
            if (shouldRespawn(playerBounds)) {
                respawn();
            }

            updateVisionBounds();
            return;
        }

        if (
            state
                == HuskHornheadState.DYING
        ) {
            stateTime += safeDelta;

            if (
                stateTime >= DEATH_DURATION
            ) {
                state =
                    HuskHornheadState.CORPSE;

                /*
                 * Keeping this at the animation's
                 * duration makes the final corpse
                 * frame remain visible.
                 */
                stateTime = DEATH_DURATION;
            }

            updateVisionBounds();
            return;
        }

        stateTime += safeDelta;

        updateVisionBounds();

        switch (state) {
            case WALKING -> updateWalking(
                safeDelta,
                playerBounds,
                playerAlive,
                platformWorld
            );

            case RESTING -> updateResting(
                playerBounds,
                playerAlive
            );

            case TURNING ->
                updateTurning();

            case ATTACK_ANTICIPATE ->
                updateAttackAnticipation();

            case CHARGING -> updateCharge(
                safeDelta,
                platformWorld
            );

            case ATTACK_COOLDOWN ->
                updateAttackCooldown();

            case DYING, CORPSE -> {
                // Already handled above.
            }
        }

        updateVisionBounds();
    }

    private void updateWalking(
        float delta,
        Rectangle playerBounds,
        boolean playerAlive,
        PlatformWorld platformWorld
    ) {
        if (
            playerAlive
                && visionBounds.overlaps(
                playerBounds
            )
        ) {
            beginAttack();
            return;
        }

        if (stateTime >= WALK_DURATION) {
            changeState(
                HuskHornheadState.RESTING
            );

            return;
        }

        int direction =
            getFacingDirection();

        if (
            !canMoveForward(
                direction,
                WALK_SPEED,
                delta,
                platformWorld
            )
        ) {
            changeState(
                HuskHornheadState.TURNING
            );

            return;
        }

        bounds.x +=
            direction
                * WALK_SPEED
                * delta;
    }

    private void updateResting(
        Rectangle playerBounds,
        boolean playerAlive
    ) {
        if (
            playerAlive
                && visionBounds.overlaps(
                playerBounds
            )
        ) {
            beginAttack();
            return;
        }

        if (
            stateTime >= REST_DURATION
        ) {
            changeState(
                HuskHornheadState.TURNING
            );
        }
    }

    private void updateTurning() {
        if (
            stateTime < TURN_DURATION
        ) {
            return;
        }

        facingRight = !facingRight;

        changeState(
            HuskHornheadState.WALKING
        );
    }

    private void beginAttack() {
        /*
         * Capture the direction now. The player can
         * move afterward, but the charge remains blind.
         */
        chargeDirection =
            getFacingDirection();

        changeState(
            HuskHornheadState
                .ATTACK_ANTICIPATE
        );
    }

    private void
    updateAttackAnticipation() {
        if (
            stateTime
                < ANTICIPATION_DURATION
        ) {
            return;
        }

        facingRight =
            chargeDirection > 0;

        changeState(
            HuskHornheadState.CHARGING
        );
    }

    private void updateCharge(
        float delta,
        PlatformWorld platformWorld
    ) {
        if (
            !canMoveForward(
                chargeDirection,
                CHARGE_SPEED,
                delta,
                platformWorld
            )
        ) {
            changeState(
                HuskHornheadState
                    .ATTACK_COOLDOWN
            );

            return;
        }

        bounds.x +=
            chargeDirection
                * CHARGE_SPEED
                * delta;
    }

    private void updateAttackCooldown() {
        if (
            stateTime
                < CHARGE_COOLDOWN_DURATION
        ) {
            return;
        }

        changeState(
            HuskHornheadState.TURNING
        );
    }

    private boolean canMoveForward(
        int direction,
        float speed,
        float delta,
        PlatformWorld platformWorld
    ) {
        nextBounds.set(bounds);

        nextBounds.x +=
            direction * speed * delta;

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
         * Detect a wall or other solid obstacle.
         */
        if (
            platformWorld.overlapsSolid(
                nextBounds
            )
        ) {
            return false;
        }

        /*
         * Detect whether the floor continues in
         * front of the enemy.
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

    private void updateVisionBounds() {
        if (!isAlive()) {
            visionBounds.set(
                0f,
                0f,
                0f,
                0f
            );

            return;
        }

        float visionX =
            facingRight
                ? bounds.x + bounds.width
                : bounds.x - VISION_WIDTH;

        visionBounds.set(
            visionX,
            bounds.y + VISION_Y_OFFSET,
            VISION_WIDTH,
            VISION_HEIGHT
        );
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

        float deltaX =
            playerCenterX - spawnCenterX;

        float deltaY =
            playerCenterY - spawnCenterY;

        return deltaX * deltaX
            + deltaY * deltaY
            >= RESPAWN_DISTANCE_SQUARED;
    }

    private int getFacingDirection() {
        return facingRight ? 1 : -1;
    }

    private void changeState(
        HuskHornheadState newState
    ) {
        if (state == newState) {
            return;
        }

        state = newState;
        stateTime = 0f;
    }

    /**
     * The future room manager can call this whenever
     * the player enters a room containing this enemy.
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

        chargeDirection =
            getFacingDirection();

        state =
            HuskHornheadState.WALKING;

        stateTime = 0f;
        flashTimeRemaining = 0f;

        updateVisionBounds();
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
            state =
                HuskHornheadState.DYING;

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

    public HuskHornheadAnimationType
    getAnimationType() {
        return switch (state) {
            case WALKING ->
                HuskHornheadAnimationType.WALK;

            case RESTING ->
                HuskHornheadAnimationType.IDLE;

            case TURNING ->
                HuskHornheadAnimationType.TURN;

            case ATTACK_ANTICIPATE ->
                HuskHornheadAnimationType
                    .ATTACK_ANTICIPATE;

            case CHARGING ->
                HuskHornheadAnimationType
                    .ATTACK_LUNGE;

            case ATTACK_COOLDOWN ->
                HuskHornheadAnimationType
                    .ATTACK_COOLDOWN;

            case DYING, CORPSE ->
                HuskHornheadAnimationType
                    .DEATH_LAND;
        };
    }

    public HuskHornheadState getState() {
        return state;
    }

    public Rectangle getVisionBounds() {
        return visionBounds;
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
        return state
            == HuskHornheadState.CORPSE;
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
}
