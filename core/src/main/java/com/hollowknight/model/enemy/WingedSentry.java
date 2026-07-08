package com.hollowknight.model.enemy;

import com.badlogic.gdx.math.Rectangle;
import com.hollowknight.model.combat.Damageable;
import com.hollowknight.model.combat.Pogoable;
import com.hollowknight.model.world.PlatformWorld;
import com.hollowknight.model.combat.Knockbackable;

/**
 * Flying City of Tears enemy.
 *
 * When the Knight is detected, the Sentry remembers
 * the Knight's vertical position. It moves to that
 * locked height, prepares, and then performs a purely
 * horizontal charge.
 */
public final class WingedSentry
    implements Damageable, Pogoable, Knockbackable {

    private static final float BODY_WIDTH = 84f;
    private static final float BODY_HEIGHT = 72f;

    private static final int MAX_HEALTH = 5;
    private static final int CONTACT_DAMAGE = 1;

    private static final float DETECTION_WIDTH = 620f;
    private static final float DETECTION_HEIGHT = 380f;

    private static final float POSITIONING_SPEED = 220f;

    private static final float
        MIN_POSITIONING_DURATION = 0.30f;

    private static final float
        POSITION_TOLERANCE = 2f;

    private static final float
        CHARGE_ANTICIPATION_DURATION = 0.40f;

    private static final float CHARGE_SPEED = 470f;

    private static final float
        MAX_CHARGE_DURATION = 1.15f;

    private static final float RETURN_SPEED = 230f;

    private static final float
        HOME_TOLERANCE = 3f;

    private static final float
        ATTACK_COOLDOWN_DURATION = 0.65f;

    private static final float
        FLASH_DURATION = 0.12f;

    private static final float
        DEATH_GRAVITY = -950f;

    private static final float
        MAX_DEATH_FALL_SPEED = -700f;

    private static final float
        DEATH_LAND_DURATION = 0.40f;

    private static final float
        RESPAWN_DISTANCE = 900f;
    private static final float KNOCKBACK_SPEED = 520f;
    private static final float KNOCKBACK_DURATION = 0.12f;

    private static final float
        RESPAWN_DISTANCE_SQUARED =
        RESPAWN_DISTANCE * RESPAWN_DISTANCE;

    private final Rectangle bounds;
    private final Rectangle nextBounds;
    private final Rectangle detectionBounds;

    private final float spawnX;
    private final float spawnY;

    private final boolean initialFacingRight;

    private WingedSentryState state;

    private int health;
    private int chargeDirection;

    private float stateTime;
    private float flashTimeRemaining;
    private float detectionCooldownRemaining;
    private float knockbackTimeRemaining;
    private int knockbackDirection;

    /*
     * This is captured only once, at detection time.
     */
    private float lockedChargeY;

    private float deathVerticalVelocity;

    private boolean facingRight;

    public WingedSentry(
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
        detectionBounds = new Rectangle();

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
        updateDetectionCooldown(safeDelta);

        if (
            state
                == WingedSentryState.CORPSE
        ) {
            if (shouldRespawn(playerBounds)) {
                respawn();
            }

            updateDetectionBounds();
            return;
        }

        if (
            state
                == WingedSentryState.DYING_AIR
        ) {
            updateDeathAir(
                safeDelta,
                platformWorld
            );

            updateDetectionBounds();
            return;
        }

        if (
            state
                == WingedSentryState.DYING_LAND
        ) {
            updateDeathLand(safeDelta);

            updateDetectionBounds();
            return;
        }

        stateTime += safeDelta;

        if (knockbackTimeRemaining > 0f) {
            updateKnockback(
                safeDelta,
                platformWorld
            );

            updateDetectionBounds();
            return;
        }

        updateDetectionBounds();

        switch (state) {
            case IDLE_HOVERING ->
                updateIdle(
                    playerBounds,
                    playerAlive
                );

            case POSITIONING ->
                updatePositioning(safeDelta);

            case CHARGE_ANTICIPATE ->
                updateChargeAnticipation();

            case CHARGING ->
                updateCharge(
                    safeDelta,
                    platformWorld
                );

            case RETURNING_HOME ->
                updateReturningHome(
                    safeDelta
                );

            case DYING_AIR,
                 DYING_LAND,
                 CORPSE -> {
                // Handled before the switch.
            }
        }

        updateDetectionBounds();
    }

    private void updateIdle(
        Rectangle playerBounds,
        boolean playerAlive
    ) {
        if (
            !playerAlive
                || playerBounds == null
                || detectionCooldownRemaining > 0f
        ) {
            return;
        }

        if (
            !detectionBounds.overlaps(
                playerBounds
            )
        ) {
            return;
        }

        lockAttackFromPlayer(
            playerBounds
        );

        changeState(
            WingedSentryState.POSITIONING
        );
    }

    private void lockAttackFromPlayer(
        Rectangle playerBounds
    ) {
        float playerCenterX =
            playerBounds.x
                + playerBounds.width / 2f;

        float playerCenterY =
            playerBounds.y
                + playerBounds.height / 2f;

        float enemyCenterX =
            bounds.x + bounds.width / 2f;

        chargeDirection =
            playerCenterX >= enemyCenterX
                ? 1
                : -1;

        /*
         * The center of the Sentry will match the
         * center height of the Knight at detection.
         */
        lockedChargeY =
            playerCenterY
                - BODY_HEIGHT / 2f;

        facingRight =
            chargeDirection > 0;
    }

    private void updatePositioning(
        float delta
    ) {
        float difference =
            lockedChargeY - bounds.y;

        float maximumMovement =
            POSITIONING_SPEED * delta;

        if (
            Math.abs(difference)
                <= maximumMovement
        ) {
            bounds.y = lockedChargeY;
        } else {
            bounds.y +=
                Math.signum(difference)
                    * maximumMovement;
        }

        boolean correctlyPositioned =
            Math.abs(
                lockedChargeY - bounds.y
            ) <= POSITION_TOLERANCE;

        if (
            correctlyPositioned
                && stateTime
                >= MIN_POSITIONING_DURATION
        ) {
            bounds.y = lockedChargeY;

            changeState(
                WingedSentryState
                    .CHARGE_ANTICIPATE
            );
        }
    }

    private void updateChargeAnticipation() {
        /*
         * Never track the Knight again here.
         */
        bounds.y = lockedChargeY;

        if (
            stateTime
                < CHARGE_ANTICIPATION_DURATION
        ) {
            return;
        }

        changeState(
            WingedSentryState.CHARGING
        );
    }
    private void updateKnockback(
        float delta,
        PlatformWorld platformWorld
    ) {
        float moveAmount =
            KNOCKBACK_SPEED * delta;

        nextBounds.set(bounds);

        nextBounds.x +=
            knockbackDirection * moveAmount;

        if (
            nextBounds.x < 0f
                || nextBounds.x
                + nextBounds.width
                > platformWorld.getWorldWidth()
        ) {
            knockbackTimeRemaining = 0f;
            return;
        }

        /*
         * Winged Sentry is flying, so it does not need
         * ground checking. It only stops at walls.
         */
        if (
            platformWorld.overlapsSolid(
                nextBounds
            )
        ) {
            knockbackTimeRemaining = 0f;
            return;
        }

        bounds.x = nextBounds.x;

        knockbackTimeRemaining -= delta;

        if (knockbackTimeRemaining < 0f) {
            knockbackTimeRemaining = 0f;
        }
    }

    private void updateCharge(
        float delta,
        PlatformWorld platformWorld
    ) {
        /*
         * This guarantees a completely horizontal
         * attack at the originally captured height.
         */
        bounds.y = lockedChargeY;

        if (
            stateTime >= MAX_CHARGE_DURATION
                || !canChargeForward(
                delta,
                platformWorld
            )
        ) {
            changeState(
                WingedSentryState.RETURNING_HOME
            );

            return;
        }

        bounds.x +=
            chargeDirection
                * CHARGE_SPEED
                * delta;
    }

    private boolean canChargeForward(
        float delta,
        PlatformWorld platformWorld
    ) {
        nextBounds.set(bounds);

        nextBounds.x +=
            chargeDirection
                * CHARGE_SPEED
                * delta;

        if (
            nextBounds.x < 0f
                || nextBounds.x
                + nextBounds.width
                > platformWorld.getWorldWidth()
        ) {
            return false;
        }

        /*
         * Flying enemies ignore pits, but still stop
         * when colliding with walls or platforms.
         */
        return !platformWorld.overlapsSolid(
            nextBounds
        );
    }

    private void updateReturningHome(
        float delta
    ) {
        float distanceX =
            spawnX - bounds.x;

        float distanceY =
            spawnY - bounds.y;

        float distance =
            (float) Math.sqrt(
                distanceX * distanceX
                    + distanceY * distanceY
            );

        if (distance <= HOME_TOLERANCE) {
            finishReturningHome();
            return;
        }

        float movement =
            RETURN_SPEED * delta;

        if (movement >= distance) {
            finishReturningHome();
            return;
        }

        float normalizedX =
            distanceX / distance;

        float normalizedY =
            distanceY / distance;

        bounds.x +=
            normalizedX * movement;

        bounds.y +=
            normalizedY * movement;

        if (Math.abs(distanceX) > 0.5f) {
            facingRight =
                distanceX > 0f;
        }
    }

    private void finishReturningHome() {
        bounds.setPosition(
            spawnX,
            spawnY
        );

        facingRight =
            initialFacingRight;

        chargeDirection =
            facingRight ? 1 : -1;

        detectionCooldownRemaining =
            ATTACK_COOLDOWN_DURATION;

        changeState(
            WingedSentryState.IDLE_HOVERING
        );
    }

    private void updateDeathAir(
        float delta,
        PlatformWorld platformWorld
    ) {
        stateTime += delta;

        float previousBottom =
            bounds.y;

        deathVerticalVelocity +=
            DEATH_GRAVITY * delta;

        deathVerticalVelocity =
            Math.max(
                deathVerticalVelocity,
                MAX_DEATH_FALL_SPEED
            );

        float nextBottom =
            bounds.y
                + deathVerticalVelocity
                * delta;

        float landingSurface =
            platformWorld.findLandingSurfaceY(
                bounds,
                previousBottom,
                nextBottom
            );

        if (!Float.isNaN(landingSurface)) {
            bounds.y = landingSurface;

            deathVerticalVelocity = 0f;

            changeState(
                WingedSentryState.DYING_LAND
            );

            return;
        }

        bounds.y = nextBottom;
    }

    private void updateDeathLand(
        float delta
    ) {
        stateTime += delta;

        if (
            stateTime < DEATH_LAND_DURATION
        ) {
            return;
        }

        state = WingedSentryState.CORPSE;

        /*
         * Keep the final death-land frame visible.
         */
        stateTime = DEATH_LAND_DURATION;
    }

    private void updateDetectionBounds() {
        if (
            !isAlive()
                || state
                != WingedSentryState
                .IDLE_HOVERING
        ) {
            detectionBounds.set(
                0f,
                0f,
                0f,
                0f
            );

            return;
        }

        float centerX =
            bounds.x + bounds.width / 2f;

        float centerY =
            bounds.y + bounds.height / 2f;

        detectionBounds.set(
            centerX - DETECTION_WIDTH / 2f,
            centerY - DETECTION_HEIGHT / 2f,
            DETECTION_WIDTH,
            DETECTION_HEIGHT
        );
    }

    private void updateFlash(float delta) {
        if (
            flashTimeRemaining <= 0f
        ) {
            return;
        }

        flashTimeRemaining -= delta;

        if (flashTimeRemaining < 0f) {
            flashTimeRemaining = 0f;
        }
    }

    private void updateDetectionCooldown(
        float delta
    ) {
        if (
            detectionCooldownRemaining <= 0f
        ) {
            return;
        }

        detectionCooldownRemaining -= delta;

        if (
            detectionCooldownRemaining < 0f
        ) {
            detectionCooldownRemaining = 0f;
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

    private void changeState(
        WingedSentryState newState
    ) {
        if (state == newState) {
            return;
        }

        state = newState;
        stateTime = 0f;
    }

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
            facingRight ? 1 : -1;

        lockedChargeY = spawnY;

        state =
            WingedSentryState.IDLE_HOVERING;

        stateTime = 0f;
        flashTimeRemaining = 0f;
        detectionCooldownRemaining = 0f;
        deathVerticalVelocity = 0f;
        knockbackTimeRemaining = 0f;
        knockbackDirection = 0;

        updateDetectionBounds();
    }
    @Override
    public void applyKnockback(
        int direction,
        PlatformWorld platformWorld
    ) {
        if (!isAlive()) {
            return;
        }

        if (direction == 0) {
            return;
        }

        knockbackDirection =
            direction < 0 ? -1 : 1;

        knockbackTimeRemaining =
            KNOCKBACK_DURATION;

        flashTimeRemaining =
            FLASH_DURATION;
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

        flashTimeRemaining =
            FLASH_DURATION;

        if (health == 0) {
            knockbackTimeRemaining = 0f;

            deathVerticalVelocity = 0f;

            state =
                WingedSentryState.DYING_AIR;

            stateTime = 0f;

            updateDetectionBounds();
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

    public WingedSentryAnimationType
    getAnimationType() {
        return switch (state) {
            case IDLE_HOVERING ->
                WingedSentryAnimationType.IDLE;

            case POSITIONING,
                 RETURNING_HOME ->
                WingedSentryAnimationType
                    .TURN_TO_FLY;

            case CHARGE_ANTICIPATE ->
                WingedSentryAnimationType
                    .CHARGE_ANTICIPATE;

            case CHARGING ->
                WingedSentryAnimationType.CHARGE;

            case DYING_AIR ->
                WingedSentryAnimationType
                    .DEATH_AIR;

            case DYING_LAND,
                 CORPSE ->
                WingedSentryAnimationType
                    .DEATH_LAND;
        };
    }

    public Rectangle getDetectionBounds() {
        return detectionBounds;
    }

    public WingedSentryState getState() {
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
        return state
            == WingedSentryState.CORPSE;
    }

    public boolean isAttackHeightLocked() {
        return state
            == WingedSentryState.POSITIONING
            || state
            == WingedSentryState
            .CHARGE_ANTICIPATE
            || state
            == WingedSentryState.CHARGING;
    }

    public float getLockedChargeCenterY() {
        return lockedChargeY
            + BODY_HEIGHT / 2f;
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
