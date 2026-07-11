package com.hollowknight.model.enemy;

import com.badlogic.gdx.math.Rectangle;
import com.hollowknight.model.combat.Damageable;
import com.hollowknight.model.combat.Pogoable;
import com.hollowknight.model.world.PlatformWorld;
import com.hollowknight.model.combat.Knockbackable;

/**
 * Stationary ranged enemy.
 *
 * It watches the area in front of itself, radiates a
 * continuous laser, performs a temporary enraged
 * charge, and then returns to its original position.
 */
public final class CrystalGuardian
    implements Damageable, Pogoable, Knockbackable {

    private static final float BODY_WIDTH = 92f;
    private static final float BODY_HEIGHT = 110f;

    private static final int MAX_HEALTH = 8;
    private static final int CONTACT_DAMAGE = 1;
    private static final int LASER_DAMAGE = 1;

    private static final float VISION_WIDTH = 480f;
    private static final float VISION_HEIGHT = 145f;
    private static final float VISION_Y_OFFSET = -8f;

    /*
     * The shooting animation reaches its firing pose,
     * then holds that pose while the beam is radiated.
     */
    private static final float SHOOT_DURATION = 1.50f;

    private static final float LASER_START_TIME = 0.45f;
    private static final float LASER_END_TIME = 1.45f;
    private static final float LASER_MAX_RANGE = 1100f;
    private static final float LASER_HEIGHT = 34f;
    private static final float LASER_Y_OFFSET = 71f;
    private static final float LASER_START_GAP = 4f;

    private static final float ENRAGED_SPEED = 240f;
    private static final float ENRAGED_DURATION = 1.60f;

    private static final float RETURN_SPEED = 180f;
    private static final float HOME_TOLERANCE = 2f;

    private static final float TURN_DURATION = 0.30f;
    private static final float DEATH_DURATION = 0.30f;
    private static final float FLASH_DURATION = 0.12f;

    private static final float GROUND_LOOK_AHEAD = 10f;
    private static final float GROUND_PROBE_DEPTH = 14f;

    private static final float RESPAWN_DISTANCE = 900f;
    private static final float KNOCKBACK_SPEED = 420f;
    private static final float KNOCKBACK_DURATION = 0.13f;
    private static final float DEATH_SIDE_FALL_DISTANCE = 52f;
    private static final float DEATH_SIDE_FALL_DURATION = 0.22f;
    private static final float
        DEATH_SIDE_FALL_SPEED =
        DEATH_SIDE_FALL_DISTANCE
            / DEATH_SIDE_FALL_DURATION;

    private static final float
        RESPAWN_DISTANCE_SQUARED =
        RESPAWN_DISTANCE * RESPAWN_DISTANCE;

    private final Rectangle bounds;
    private final Rectangle nextBounds;
    private final Rectangle visionBounds;
    private final Rectangle laserBounds;

    private final float spawnX;
    private final float spawnY;

    private final boolean initialFacingRight;

    private CrystalGuardianState state;

    private int health;
    private int attackDirection;
    private boolean turnTargetFacingRight;

    private float stateTime;
    private float flashTimeRemaining;
    private float knockbackTimeRemaining;
    private float knockbackStrengthMultiplier = 1f;
    private int knockbackDirection;
    private float deathSideFallRemaining;
    private int deathSideFallDirection;


    private boolean facingRight;

    public CrystalGuardian(
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
        visionBounds = new Rectangle();
        laserBounds = new Rectangle();

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
                == CrystalGuardianState.CORPSE
        ) {
            if (shouldRespawn(playerBounds)) {
                respawn();
            }

            clearLaser();
            updateVisionBounds();
            return;
        }

        if (
            state
                == CrystalGuardianState.DYING
        ) {
            updateDeath(safeDelta);
            clearLaser();
            updateVisionBounds();
            return;
        }
        stateTime += safeDelta;

        if (knockbackTimeRemaining > 0f) {
            updateKnockback(
                safeDelta,
                platformWorld
            );

            updateVisionBounds();
            updateLaserBounds(platformWorld);
            return;
        }

        updateVisionBounds();

        switch (state)  {
            case IDLE_WATCHING ->
                updateIdle(
                    playerBounds,
                    playerAlive
                );

            case SHOOTING ->
                updateShooting();

            case ENRAGED_CHARGING ->
                updateEnragedCharge(
                    safeDelta,
                    platformWorld
                );

            case RETURNING_HOME ->
                updateReturningHome(
                    safeDelta,
                    platformWorld
                );

            case TURNING_TO_PLAYER ->
                updateTurningToPlayer();

            case TURNING_HOME ->
                updateTurningHome();

            case DYING, CORPSE -> {
                // Handled before the switch.
            }
        }

        updateVisionBounds();
        updateLaserBounds(platformWorld);
    }

    private void updateIdle(
        Rectangle playerBounds,
        boolean playerAlive
    ) {
        if (
            !playerAlive
                || playerBounds == null
                || !isPlayerWithinBidirectionalVision(
                    playerBounds
                )
        ) {
            return;
        }

        boolean playerIsToTheRight =
            isPlayerToTheRight(playerBounds);

        if (playerIsToTheRight != facingRight) {
            turnTargetFacingRight =
                playerIsToTheRight;

            changeState(
                CrystalGuardianState
                    .TURNING_TO_PLAYER
            );
            return;
        }

        beginShooting();
    }

    private boolean isPlayerWithinBidirectionalVision(
        Rectangle playerBounds
    ) {
        float visionBottom =
            bounds.y + VISION_Y_OFFSET;

        float visionTop =
            visionBottom + VISION_HEIGHT;

        boolean overlapsVertically =
            playerBounds.y < visionTop
                && playerBounds.y
                + playerBounds.height
                > visionBottom;

        if (!overlapsVertically) {
            return false;
        }

        float detectionLeft =
            bounds.x - VISION_WIDTH;

        float detectionRight =
            bounds.x
                + bounds.width
                + VISION_WIDTH;

        return playerBounds.x < detectionRight
            && playerBounds.x
            + playerBounds.width
            > detectionLeft;
    }

    private boolean isPlayerToTheRight(
        Rectangle playerBounds
    ) {
        float guardianCenterX =
            bounds.x + bounds.width / 2f;

        float playerCenterX =
            playerBounds.x
                + playerBounds.width / 2f;

        if (Math.abs(playerCenterX - guardianCenterX) < 1f) {
            return facingRight;
        }

        return playerCenterX > guardianCenterX;
    }

    private void beginShooting() {
        /*
         * The laser and the following charge retain
         * this direction even if the player moves.
         */
        attackDirection =
            getFacingDirection();

        changeState(
            CrystalGuardianState.SHOOTING
        );
    }

    private void updateShooting() {
        if (
            stateTime < SHOOT_DURATION
        ) {
            return;
        }

        facingRight =
            attackDirection > 0;

        changeState(
            CrystalGuardianState
                .ENRAGED_CHARGING
        );
    }
    private void updateKnockback(
        float delta,
        PlatformWorld platformWorld
    ) {
        float moveAmount =
            KNOCKBACK_SPEED
                * knockbackStrengthMultiplier
                * delta;

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

        if (
            platformWorld.overlapsSolid(
                nextBounds
            )
        ) {
            knockbackTimeRemaining = 0f;
            return;
        }

        if (
            !platformWorld.hasGroundAhead(
                nextBounds,
                knockbackDirection,
                GROUND_LOOK_AHEAD,
                GROUND_PROBE_DEPTH
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
    private void updateEnragedCharge(
        float delta,
        PlatformWorld platformWorld
    ) {
        if (
            stateTime >= ENRAGED_DURATION
        ) {
            beginReturningHome();
            return;
        }

        if (
            !canMoveForward(
                attackDirection,
                ENRAGED_SPEED,
                delta,
                platformWorld
            )
        ) {
            beginReturningHome();
            return;
        }

        bounds.x +=
            attackDirection
                * ENRAGED_SPEED
                * delta;
    }

    private void beginReturningHome() {
        clearLaser();

        changeState(
            CrystalGuardianState
                .RETURNING_HOME
        );
    }

    private void updateReturningHome(
        float delta,
        PlatformWorld platformWorld
    ) {
        float distanceToHome =
            spawnX - bounds.x;

        if (
            Math.abs(distanceToHome)
                <= HOME_TOLERANCE
        ) {
            finishReturningHome();
            return;
        }

        int returnDirection =
            distanceToHome > 0f ? 1 : -1;

        facingRight =
            returnDirection > 0;

        float movement =
            returnDirection
                * RETURN_SPEED
                * delta;

        if (
            Math.abs(movement)
                >= Math.abs(distanceToHome)
        ) {
            bounds.x = spawnX;
            finishReturningHome();
            return;
        }

        if (
            !canMoveForward(
                returnDirection,
                RETURN_SPEED,
                delta,
                platformWorld
            )
        ) {
            /*
             * The return path should normally be the
             * reverse of the charge path. This fallback
             * prevents the enemy becoming permanently
             * stuck if the temporary map changes.
             */
            bounds.x = spawnX;
            finishReturningHome();
            return;
        }

        bounds.x += movement;
    }

    private void finishReturningHome() {
        bounds.x = spawnX;
        bounds.y = spawnY;

        if (
            facingRight
                != initialFacingRight
        ) {
            changeState(
                CrystalGuardianState
                    .TURNING_HOME
            );
        } else {
            changeState(
                CrystalGuardianState
                    .IDLE_WATCHING
            );
        }
    }

    private void updateTurningToPlayer() {
        if (stateTime < TURN_DURATION) {
            return;
        }

        facingRight =
            turnTargetFacingRight;

        attackDirection =
            getFacingDirection();

        beginShooting();
    }

    private void updateTurningHome() {
        if (
            stateTime < TURN_DURATION
        ) {
            return;
        }

        facingRight =
            initialFacingRight;

        attackDirection =
            getFacingDirection();

        changeState(
            CrystalGuardianState
                .IDLE_WATCHING
        );
    }

    private void updateDeath(
        float delta
    ) {
        stateTime += delta;

        updateDeathSideFall(delta);

        if (
            stateTime < DEATH_DURATION
        ) {
            return;
        }

        state =
            CrystalGuardianState.CORPSE;

        /*
         * Holding the time at the animation duration
         * keeps the last corpse frame visible.
         */
        stateTime = DEATH_DURATION;
    }


    private void startDeathSideFall() {
        deathSideFallDirection =
            knockbackDirection != 0
                ? knockbackDirection
                : getFacingDirection();

        deathSideFallRemaining =
            DEATH_SIDE_FALL_DISTANCE;
    }

    private void updateDeathSideFall(
        float delta
    ) {
        if (
            deathSideFallRemaining <= 0f
                || deathSideFallDirection == 0
        ) {
            return;
        }

        float amount = Math.min(
            deathSideFallRemaining,
            DEATH_SIDE_FALL_SPEED * delta
        );

        bounds.x +=
            deathSideFallDirection * amount;

        deathSideFallRemaining -= amount;
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

        if (
            platformWorld.overlapsSolid(
                nextBounds
            )
        ) {
            return false;
        }

        return platformWorld.hasGroundAhead(
            nextBounds,
            direction,
            GROUND_LOOK_AHEAD,
            GROUND_PROBE_DEPTH
        );
    }

    private void updateVisionBounds() {
        if (
            state
                != CrystalGuardianState
                .IDLE_WATCHING
                || !isAlive()
        ) {
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

    private void updateLaserBounds(
        PlatformWorld platformWorld
    ) {
        if (!isLaserActive()) {
            clearLaser();
            return;
        }

        float laserY =
            bounds.y + LASER_Y_OFFSET;

        float laserStartX;

        if (attackDirection > 0) {
            laserStartX =
                bounds.x
                    + bounds.width
                    + LASER_START_GAP;
        } else {
            laserStartX =
                bounds.x
                    - LASER_START_GAP;
        }

        float laserLength =
            platformWorld
                .getHorizontalRayLength(
                    laserStartX,
                    laserY,
                    LASER_HEIGHT,
                    attackDirection,
                    LASER_MAX_RANGE
                );

        if (attackDirection > 0) {
            laserBounds.set(
                laserStartX,
                laserY,
                laserLength,
                LASER_HEIGHT
            );
        } else {
            laserBounds.set(
                laserStartX - laserLength,
                laserY,
                laserLength,
                LASER_HEIGHT
            );
        }
    }

    private void clearLaser() {
        laserBounds.set(
            0f,
            0f,
            0f,
            0f
        );
    }

    private void updateFlash(float delta) {
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
        CrystalGuardianState newState
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

        attackDirection =
            getFacingDirection();

        turnTargetFacingRight =
            facingRight;

        state =
            CrystalGuardianState
                .IDLE_WATCHING;

        stateTime = 0f;
        flashTimeRemaining = 0f;
        knockbackTimeRemaining = 0f;
        knockbackDirection = 0;
        deathSideFallRemaining = 0f;
        deathSideFallDirection = 0;

        clearLaser();
        updateVisionBounds();
    }
    @Override
    public void applyKnockback(
        int direction,
        PlatformWorld platformWorld
    ) {
        applyKnockback(
            direction,
            platformWorld,
            1f
        );
    }

    @Override
    public void applyKnockback(
        int direction,
        PlatformWorld platformWorld,
        float strengthMultiplier
    ) {
        if (!isAlive()) {
            return;
        }

        if (direction == 0) {
            return;
        }

        knockbackDirection =
            direction < 0 ? -1 : 1;

        knockbackStrengthMultiplier =
            Math.max(
                1f,
                Math.min(2.75f, strengthMultiplier)
            );

        knockbackTimeRemaining =
            KNOCKBACK_DURATION
                * (0.88f
                + knockbackStrengthMultiplier * 0.12f);

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
            startDeathSideFall();
            knockbackTimeRemaining = 0f;
            clearLaser();

            state =
                CrystalGuardianState.DYING;

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

    public boolean isLaserActive() {
        return state
            == CrystalGuardianState.SHOOTING
            && stateTime >= LASER_START_TIME
            && stateTime <= LASER_END_TIME;
    }

    public CrystalGuardianAnimationType
    getAnimationType() {
        return switch (state) {
            case IDLE_WATCHING ->
                CrystalGuardianAnimationType.IDLE;

            case SHOOTING ->
                CrystalGuardianAnimationType.SHOOT;

            case ENRAGED_CHARGING ->
                CrystalGuardianAnimationType.RUN;

            case RETURNING_HOME ->
                CrystalGuardianAnimationType.EVADE;

            case TURNING_TO_PLAYER, TURNING_HOME ->
                CrystalGuardianAnimationType.TURN;

            case DYING, CORPSE ->
                CrystalGuardianAnimationType
                    .DEATH_LAND;
        };
    }

    public CrystalGuardianState getState() {
        return state;
    }

    public Rectangle getVisionBounds() {
        return visionBounds;
    }

    public Rectangle getLaserBounds() {
        return laserBounds;
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
            == CrystalGuardianState.CORPSE;
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

    public int getLaserDamage() {
        return LASER_DAMAGE;
    }

    public int getLaserDirection() {
        return attackDirection;
    }

    public float getLaserActiveTime() {
        if (!isLaserActive()) {
            return 0f;
        }

        return stateTime - LASER_START_TIME;
    }
}
