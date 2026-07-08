package com.hollowknight.model.boss;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.hollowknight.model.world.PlatformWorld;

public class FalseKnight {

    public enum State {
        IDLE,
        MACE_SLAM_ANTIC,
        MACE_SLAM,
        MACE_SLAM_RECOVER,
        CHARGE_RUN_ANTIC,
        CHARGE_RUN,
        OFFENSIVE_LEAP,
        DEFENSIVE_LEAP,
        POWER_JUMP,
        POWER_SLAM,
        STUNNED,
        STUN_RECOVER,
        DEAD
    }

    public enum Move {
        MACE_SLAM,
        CHARGE_RUN,
        OFFENSIVE_LEAP,
        DEFENSIVE_LEAP,
        POWER_MACE_SLAM
    }

    private static final int MAX_HEALTH = 160;
    private static final int STUN_HEALTH_THRESHOLD = MAX_HEALTH / 2;

    private static final float GRAVITY = -1800f;

    private static final float PHASE_ONE_WALK_SPEED = 130f;
    private static final float PHASE_TWO_WALK_SPEED = 185f;

    private static final float CHARGE_SPEED_PHASE_ONE = 330f;
    private static final float CHARGE_SPEED_PHASE_TWO = 430f;

    private static final float LEAP_HORIZONTAL_SPEED = 230f;
    private static final float LEAP_VERTICAL_SPEED = 720f;

    private static final float DEFENSIVE_LEAP_HORIZONTAL_SPEED = 260f;
    private static final float DEFENSIVE_LEAP_VERTICAL_SPEED = 620f;

    private static final float POWER_JUMP_VERTICAL_SPEED = 820f;

    private static final float CLOSE_DISTANCE = 260f;
    private static final float FAR_DISTANCE = 620f;

    private static final float AI_DELAY_PHASE_ONE = 0.75f;
    private static final float AI_DELAY_PHASE_TWO = 0.45f;

    private static final float STUN_DURATION = 4.0f;

    private static final float HEAVY_DAMAGE_WINDOW = 1.25f;
    private static final int HEAVY_DAMAGE_TRIGGER = 3;

    private static final float MACE_SLAM_ANTIC_TIME = 0.45f;
    private static final float MACE_SLAM_HIT_TIME = 0.18f;
    private static final float MACE_SLAM_RECOVER_TIME = 0.45f;

    private static final float CHARGE_ANTIC_TIME = 0.35f;
    private static final float CHARGE_DURATION = 1.2f;

    private static final float POWER_SLAM_GROUND_TIME = 0.28f;

    private static final int BODY_DAMAGE = 1;
    private static final int MACE_DAMAGE = 1;
    private static final int SHOCKWAVE_DAMAGE = 2;



    private final Rectangle bounds;
    private final Rectangle maceHitbox;
    private final Rectangle vulnerableHitbox;
    private final Rectangle shockwaveHitbox;

    private final float spawnX;
    private final float spawnY;

    private final float arenaMinX;
    private final float arenaMaxX;
    private final float groundY;

    private State state = State.IDLE;
    private Move lastMove = null;

    private int health = MAX_HEALTH;

    private boolean facingRight = false;
    private boolean phaseTwo = false;
    private boolean stunTriggered = false;
    private boolean alive = true;

    private float stateTime;
    private float animationTime;
    private float aiTimer;

    private float velocityX;
    private float velocityY;

    private float heavyDamageTimer;
    private int recentHits;

    private boolean maceHitActive;
    private boolean shockwaveActive;
    private boolean shouldShakeCamera;
    private float pendingShakeIntensity;

    private float shockwaveSpeed;
    private int shockwaveDirection;

    public FalseKnight(
        float x,
        float y,
        float width,
        float height,
        float arenaMinX,
        float arenaMaxX,
        float groundY
    ) {
        this.bounds = new Rectangle(
            x,
            y,
            width,
            height
        );

        this.spawnX = x;
        this.spawnY = y;

        this.arenaMinX = arenaMinX;
        this.arenaMaxX = arenaMaxX;
        this.groundY = groundY;

        this.maceHitbox = new Rectangle();
        this.vulnerableHitbox = new Rectangle();
        this.shockwaveHitbox = new Rectangle();

        aiTimer = 1.0f;

        updateHitboxes();
    }

    public void update(
        float delta,
        Rectangle playerBounds,
        PlatformWorld platformWorld
    ) {
        if (
            !alive
                && state != State.DEAD
        ) {
            return;
        }

        animationTime += delta;
        stateTime += delta;

        if (state == State.DEAD) {
            return;
        }

        updateHeavyDamageTimer(delta);

        switch (state) {
            case IDLE ->
                updateIdle(
                    delta,
                    playerBounds
                );

            case MACE_SLAM_ANTIC ->
                updateMaceSlamAntic();

            case MACE_SLAM ->
                updateMaceSlam();

            case MACE_SLAM_RECOVER ->
                updateMaceSlamRecover();

            case CHARGE_RUN_ANTIC ->
                updateChargeAntic();

            case CHARGE_RUN ->
                updateChargeRun(
                    delta,
                    playerBounds,
                    platformWorld
                );

            case OFFENSIVE_LEAP ->
                updateLeap(
                    delta,
                    platformWorld,
                    false
                );

            case DEFENSIVE_LEAP ->
                updateLeap(
                    delta,
                    platformWorld,
                    true
                );

            case POWER_JUMP ->
                updatePowerJump(
                    delta,
                    playerBounds,
                    platformWorld
                );

            case POWER_SLAM ->
                updatePowerSlam(
                    delta
                );

            case STUNNED ->
                updateStunned();

            case STUN_RECOVER ->
                updateStunRecover();

            case DEAD -> {
                velocityX = 0f;
                velocityY = 0f;
            }
        }

        updateShockwave(delta);
        updateHitboxes();
        clampToArena();
    }

    private void updateIdle(
        float delta,
        Rectangle playerBounds
    ) {
        maceHitActive = false;

        facePlayer(playerBounds);

        aiTimer -= delta;

        if (aiTimer > 0f) {
            return;
        }

        Move move =
            chooseMove(playerBounds);

        startMove(move);
    }

    private Move chooseMove(
        Rectangle playerBounds
    ) {
        float distance =
            Math.abs(
                getCenterX()
                    - getPlayerCenterX(playerBounds)
            );

        boolean heavyDamageRecently =
            recentHits >= HEAVY_DAMAGE_TRIGGER
                && heavyDamageTimer > 0f;

        if (
            heavyDamageRecently
                && lastMove != Move.DEFENSIVE_LEAP
        ) {
            recentHits = 0;
            return Move.DEFENSIVE_LEAP;
        }

        Move chosen;

        for (int attempt = 0; attempt < 8; attempt++) {
            float roll =
                MathUtils.random();

            if (distance <= CLOSE_DISTANCE) {
                chosen = chooseCloseMove(roll);
            } else if (distance >= FAR_DISTANCE) {
                chosen = chooseFarMove(roll);
            } else {
                chosen = chooseMediumMove(roll);
            }

            if (
                chosen != lastMove
                    && isMoveAllowed(chosen)
            ) {
                return chosen;
            }
        }

        return getFallbackMove();
    }

    private Move chooseCloseMove(
        float roll
    ) {
        if (phaseTwo && roll < 0.18f) {
            return Move.POWER_MACE_SLAM;
        }

        if (roll < 0.62f) {
            return Move.MACE_SLAM;
        }

        if (roll < 0.82f) {
            return Move.OFFENSIVE_LEAP;
        }

        return Move.CHARGE_RUN;
    }

    private Move chooseMediumMove(
        float roll
    ) {
        if (phaseTwo && roll < 0.25f) {
            return Move.POWER_MACE_SLAM;
        }

        if (roll < 0.34f) {
            return Move.OFFENSIVE_LEAP;
        }

        if (roll < 0.62f) {
            return Move.CHARGE_RUN;
        }

        return Move.MACE_SLAM;
    }

    private Move chooseFarMove(
        float roll
    ) {
        if (phaseTwo && roll < 0.28f) {
            return Move.POWER_MACE_SLAM;
        }

        if (roll < 0.50f) {
            return Move.CHARGE_RUN;
        }

        if (roll < 0.84f) {
            return Move.OFFENSIVE_LEAP;
        }

        return Move.MACE_SLAM;
    }

    private boolean isMoveAllowed(
        Move move
    ) {
        if (
            move == Move.POWER_MACE_SLAM
                && !phaseTwo
        ) {
            return false;
        }

        return move != lastMove;
    }

    private Move getFallbackMove() {
        if (lastMove != Move.MACE_SLAM) {
            return Move.MACE_SLAM;
        }

        if (lastMove != Move.OFFENSIVE_LEAP) {
            return Move.OFFENSIVE_LEAP;
        }

        return Move.CHARGE_RUN;
    }

    private void startMove(
        Move move
    ) {
        lastMove = move;
        stateTime = 0f;
        animationTime = 0f;
        maceHitActive = false;

        switch (move) {
            case MACE_SLAM -> {
                state = State.MACE_SLAM_ANTIC;
                velocityX = 0f;
            }

            case CHARGE_RUN -> {
                state = State.CHARGE_RUN_ANTIC;
                velocityX = 0f;
            }

            case OFFENSIVE_LEAP -> {
                state = State.OFFENSIVE_LEAP;
                velocityY = LEAP_VERTICAL_SPEED;
                velocityX =
                    facingRight
                        ? LEAP_HORIZONTAL_SPEED
                        : -LEAP_HORIZONTAL_SPEED;
            }

            case DEFENSIVE_LEAP -> {
                state = State.DEFENSIVE_LEAP;
                velocityY = DEFENSIVE_LEAP_VERTICAL_SPEED;
                velocityX =
                    facingRight
                        ? -DEFENSIVE_LEAP_HORIZONTAL_SPEED
                        : DEFENSIVE_LEAP_HORIZONTAL_SPEED;
            }

            case POWER_MACE_SLAM -> {
                state = State.POWER_JUMP;
                velocityY = POWER_JUMP_VERTICAL_SPEED;
                velocityX =
                    facingRight
                        ? 120f
                        : -120f;
            }
        }
    }

    private void updateMaceSlamAntic() {
        if (stateTime >= MACE_SLAM_ANTIC_TIME) {
            state = State.MACE_SLAM;
            stateTime = 0f;
            animationTime = 0f;
            maceHitActive = true;

            requestCameraShake(0.45f);
        }
    }

    private void updateMaceSlam() {
        if (stateTime >= MACE_SLAM_HIT_TIME) {
            maceHitActive = false;
            state = State.MACE_SLAM_RECOVER;
            stateTime = 0f;
            animationTime = 0f;
        }
    }

    private void updateMaceSlamRecover() {
        if (stateTime >= MACE_SLAM_RECOVER_TIME) {
            returnToIdle();
        }
    }

    private void updateChargeAntic() {
        if (stateTime >= CHARGE_ANTIC_TIME) {
            state = State.CHARGE_RUN;
            stateTime = 0f;
            animationTime = 0f;

            velocityX =
                facingRight
                    ? getChargeSpeed()
                    : -getChargeSpeed();
        }
    }

    private void updateChargeRun(
        float delta,
        Rectangle playerBounds,
        PlatformWorld platformWorld
    ) {
        facePlayer(playerBounds);

        Rectangle nextBounds =
            new Rectangle(bounds);

        nextBounds.x +=
            velocityX * delta;

        if (
            nextBounds.x <= arenaMinX
                || nextBounds.x
                + nextBounds.width >= arenaMaxX
                || platformWorld.overlapsSolid(nextBounds)
                || stateTime >= CHARGE_DURATION
        ) {
            requestCameraShake(0.32f);
            returnToIdle();
            return;
        }

        bounds.x = nextBounds.x;
    }

    private void updateLeap(
        float delta,
        PlatformWorld platformWorld,
        boolean defensive
    ) {
        velocityY +=
            GRAVITY * delta;

        Rectangle nextBounds =
            new Rectangle(bounds);

        nextBounds.x +=
            velocityX * delta;

        nextBounds.y +=
            velocityY * delta;

        if (
            nextBounds.x < arenaMinX
                || nextBounds.x
                + nextBounds.width > arenaMaxX
        ) {
            velocityX *= -0.35f;
        } else {
            bounds.x = nextBounds.x;
        }

        if (nextBounds.y <= groundY) {
            bounds.y = groundY;
            velocityY = 0f;

            requestCameraShake(
                defensive ? 0.30f : 0.50f
            );

            returnToIdle();
            return;
        }

        if (!platformWorld.overlapsSolid(nextBounds)) {
            bounds.y = nextBounds.y;
        }
    }

    private void updatePowerJump(
        float delta,
        Rectangle playerBounds,
        PlatformWorld platformWorld
    ) {
        facePlayer(playerBounds);

        velocityY +=
            GRAVITY * delta;

        Rectangle nextBounds =
            new Rectangle(bounds);

        nextBounds.x +=
            velocityX * delta;

        nextBounds.y +=
            velocityY * delta;

        if (
            nextBounds.x >= arenaMinX
                && nextBounds.x
                + nextBounds.width <= arenaMaxX
                && !platformWorld.overlapsSolid(nextBounds)
        ) {
            bounds.x = nextBounds.x;
        }

        if (nextBounds.y <= groundY) {
            bounds.y = groundY;
            velocityY = 0f;
            velocityX = 0f;

            state = State.POWER_SLAM;
            stateTime = 0f;
            animationTime = 0f;

            maceHitActive = true;
            startShockwave();

            requestCameraShake(0.85f);
            return;
        }

        bounds.y = nextBounds.y;
    }

    private void updatePowerSlam(
        float delta
    ) {
        if (stateTime >= POWER_SLAM_GROUND_TIME) {
            maceHitActive = false;
            returnToIdle();
        }
    }

    private void updateStunned() {
        velocityX = 0f;
        velocityY = 0f;
        maceHitActive = false;

        if (stateTime >= STUN_DURATION) {
            phaseTwo = true;
            state = State.STUN_RECOVER;
            stateTime = 0f;
            animationTime = 0f;
        }
    }

    private void updateStunRecover() {
        if (stateTime >= 1.0f) {
            returnToIdle();
        }
    }

    private void returnToIdle() {
        state = State.IDLE;
        stateTime = 0f;
        animationTime = 0f;
        velocityX = 0f;
        velocityY = 0f;
        maceHitActive = false;

        aiTimer =
            phaseTwo
                ? AI_DELAY_PHASE_TWO
                : AI_DELAY_PHASE_ONE;
    }

    public void takeDamage(
        int damage,
        boolean hitVulnerableBody
    ) {
        if (!alive || state == State.DEAD) {
            return;
        }

        if (
            state == State.STUNNED
                && !hitVulnerableBody
        ) {
            return;
        }

        health -= damage;

        if (health < 0) {
            health = 0;
        }

        recentHits++;
        heavyDamageTimer = HEAVY_DAMAGE_WINDOW;

        if (health <= 0) {
            die();
            return;
        }

        if (
            !stunTriggered
                && health <= STUN_HEALTH_THRESHOLD
        ) {
            triggerStun();
        }
    }

    private void triggerStun() {
        stunTriggered = true;
        state = State.STUNNED;
        stateTime = 0f;
        animationTime = 0f;

        velocityX = 0f;
        velocityY = 0f;
        maceHitActive = false;

        requestCameraShake(0.65f);
    }

    private void die() {
        alive = false;
        state = State.DEAD;
        stateTime = 0f;
        animationTime = 0f;

        maceHitActive = false;
        shockwaveActive = false;

        velocityX = 0f;
        velocityY = 0f;

        requestCameraShake(0.9f);
    }

    private void startShockwave() {
        shockwaveActive = true;
        shockwaveDirection =
            facingRight ? 1 : -1;

        shockwaveSpeed = 260f;

        if (facingRight) {
            shockwaveHitbox.set(
                bounds.x + bounds.width,
                groundY + 8f,
                50f,
                42f
            );
        } else {
            shockwaveHitbox.set(
                bounds.x - 50f,
                groundY + 8f,
                50f,
                42f
            );
        }
    }

    private void updateShockwave(
        float delta
    ) {
        if (!shockwaveActive) {
            return;
        }

        shockwaveSpeed +=
            520f * delta;

        shockwaveHitbox.x +=
            shockwaveDirection
                * shockwaveSpeed
                * delta;

        if (
            shockwaveHitbox.x < arenaMinX
                || shockwaveHitbox.x
                > arenaMaxX
        ) {
            shockwaveActive = false;
        }
    }

    private void updateHeavyDamageTimer(
        float delta
    ) {
        if (heavyDamageTimer <= 0f) {
            recentHits = 0;
            return;
        }

        heavyDamageTimer -= delta;

        if (heavyDamageTimer <= 0f) {
            recentHits = 0;
        }
    }

    private void updateHitboxes() {
        if (facingRight) {
            maceHitbox.set(
                bounds.x + bounds.width - 10f,
                bounds.y + 16f,
                120f,
                84f
            );
        } else {
            maceHitbox.set(
                bounds.x - 110f,
                bounds.y + 16f,
                120f,
                84f
            );
        }

        vulnerableHitbox.set(
            bounds.x + bounds.width * 0.28f,
            bounds.y + bounds.height * 0.38f,
            bounds.width * 0.44f,
            bounds.height * 0.30f
        );
    }

    private void facePlayer(
        Rectangle playerBounds
    ) {
        facingRight =
            getPlayerCenterX(playerBounds)
                > getCenterX();
    }

    private float getPlayerCenterX(
        Rectangle playerBounds
    ) {
        return playerBounds.x
            + playerBounds.width / 2f;
    }

    private float getCenterX() {
        return bounds.x
            + bounds.width / 2f;
    }

    private float getChargeSpeed() {
        return phaseTwo
            ? CHARGE_SPEED_PHASE_TWO
            : CHARGE_SPEED_PHASE_ONE;
    }

    private void clampToArena() {
        if (bounds.x < arenaMinX) {
            bounds.x = arenaMinX;
        }

        if (
            bounds.x + bounds.width
                > arenaMaxX
        ) {
            bounds.x =
                arenaMaxX - bounds.width;
        }
    }

    private void requestCameraShake(
        float intensity
    ) {
        shouldShakeCamera = true;
        pendingShakeIntensity = intensity;
    }

    public boolean consumeCameraShakeRequest() {
        if (!shouldShakeCamera) {
            return false;
        }

        shouldShakeCamera = false;
        return true;
    }

    public float getPendingShakeIntensity() {
        return pendingShakeIntensity;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public Rectangle getMaceHitbox() {
        return maceHitbox;
    }

    public Rectangle getVulnerableHitbox() {
        return vulnerableHitbox;
    }

    public Rectangle getShockwaveHitbox() {
        return shockwaveHitbox;
    }

    public boolean isMaceHitActive() {
        return maceHitActive;
    }

    public boolean isShockwaveActive() {
        return shockwaveActive;
    }

    public int getMaceDamage() {
        return MACE_DAMAGE;
    }

    public int getShockwaveDamage() {
        return SHOCKWAVE_DAMAGE;
    }

    public int getBodyDamage() {
        return BODY_DAMAGE;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return MAX_HEALTH;
    }

    public boolean isPhaseTwo() {
        return phaseTwo;
    }

    public boolean isAlive() {
        return alive;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public State getState() {
        return state;
    }

    public float getAnimationTime() {
        return animationTime;
    }

    public boolean isStunned() {
        return state == State.STUNNED;
    }

    public void respawn() {
        bounds.x = spawnX;
        bounds.y = spawnY;

        health = MAX_HEALTH;
        alive = true;
        phaseTwo = false;
        stunTriggered = false;

        state = State.IDLE;
        lastMove = null;

        stateTime = 0f;
        animationTime = 0f;
        aiTimer = 1.0f;

        velocityX = 0f;
        velocityY = 0f;

        recentHits = 0;
        heavyDamageTimer = 0f;

        maceHitActive = false;
        shockwaveActive = false;
        shouldShakeCamera = false;
    }
}
