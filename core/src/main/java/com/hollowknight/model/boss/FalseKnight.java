package com.hollowknight.model.boss;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.hollowknight.model.world.PlatformWorld;

/**
 * The False Knight boss.
 *
 * <p>Behaviour model:
 * <ul>
 *   <li>In IDLE the knight holds its ground and simply faces the player. It
 *       does not chase to a fixed stand-off distance, so the real distance to
 *       the player keeps varying and actually drives move selection.</li>
 *   <li>Move selection is distance-weighted, randomised and anti-spam
 *       protected (never the same move twice in a row).</li>
 *   <li>Mace Slam is favoured heavily when the player is close; Charge Run is
 *       favoured when far; Offensive Leap covers medium/long range and arcs
 *       toward the player's position; Power Mace Slam is a phase-two only
 *       leaping slam that releases an accelerating shockwave.</li>
 *   <li>Defensive Leap is reactive: taking heavy damage in a short window makes
 *       the knight hop backwards (with a random element), and it can interrupt
 *       wind-up / recovery states.</li>
 * </ul>
 */
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

    private static final int MAX_HEALTH = 80;
    private static final int STUN_HEALTH_THRESHOLD = MAX_HEALTH / 2;

    private static final float GRAVITY = -1800f;

    /*
     * Idle walk speeds. Only used for a slow "menacing advance" when the player
     * is very far, and for a small anti-overlap step-back. The knight relies on
     * Charge Run / leaps to actually cross distance, which keeps the distance
     * genuinely varied for the AI.
     */
    private static final float PHASE_ONE_WALK_SPEED = 120f;
    private static final float PHASE_TWO_WALK_SPEED = 175f;

    private static final float CHARGE_SPEED_PHASE_ONE = 360f;
    private static final float CHARGE_SPEED_PHASE_TWO = 470f;

    private static final float LEAP_VERTICAL_SPEED = 760f;
    private static final float LEAP_MAX_HORIZONTAL = 380f;

    private static final float DEFENSIVE_LEAP_HORIZONTAL_SPEED = 300f;
    private static final float DEFENSIVE_LEAP_VERTICAL_SPEED = 640f;

    private static final float POWER_JUMP_VERTICAL_SPEED = 900f;
    private static final float POWER_MAX_HORIZONTAL = 320f;

    /*
     * Distance thresholds, measured centre-to-centre horizontally.
     * CLOSE is roughly the reach at which a Mace Slam actually connects.
     */
    private static final float CLOSE_DISTANCE = 240f;
    private static final float FAR_DISTANCE = 560f;

    private static final float AI_DELAY_PHASE_ONE = 0.80f;
    private static final float AI_DELAY_PHASE_TWO = 0.36f;

    private static final float STUN_DURATION = 3.5f;

    private static final float HEAVY_DAMAGE_WINDOW = 1.3f;
    private static final int HEAVY_DAMAGE_TRIGGER = 3;
    private static final float DEFENSIVE_LEAP_REACTION_CHANCE = 0.85f;

    private static final float MACE_SLAM_ANTIC_TIME = 0.51f;
    private static final float MACE_SLAM_IMPACT_TIME = 0.20f;
    private static final float MACE_SLAM_HIT_TIME = 0.34f;
    private static final float MACE_SLAM_RECOVER_TIME = 0.50f;

    private static final float CHARGE_ANTIC_TIME = 0.32f;
    private static final float CHARGE_DURATION = 1.4f;

    private static final float POWER_SLAM_IMPACT_TIME = 0.12f;
    private static final float POWER_SLAM_HIT_END_TIME = 0.30f;
    private static final float POWER_SLAM_GROUND_TIME = 0.56f;

    private static final int BODY_DAMAGE = 1;
    private static final int MACE_DAMAGE = 1;
    private static final int SHOCKWAVE_DAMAGE = 2;

    /*
     * Idle spacing. The knight only nudges back if it is basically standing on
     * top of the player, and only advances slowly if the player is very far.
     */
    private static final float MIN_STANDOFF = 110f;
    private static final float IDLE_ADVANCE_DISTANCE = FAR_DISTANCE;

    /*
     * Camera shake intensities (roughly 0..1.4). These are deliberately punchy
     * so heavy strikes and landings read as weighty.
     */
    private static final float SHAKE_MACE_SLAM = 0.60f;
    private static final float SHAKE_CHARGE_WALL = 0.64f;
    private static final float SHAKE_CHARGE_TIMEOUT = 0.38f;
    private static final float SHAKE_OFFENSIVE_LAND = 0.64f;
    private static final float SHAKE_DEFENSIVE_LAND = 0.47f;
    private static final float SHAKE_POWER_SLAM = 1.05f;
    private static final float SHAKE_STUN = 0.76f;
    private static final float SHAKE_STUN_LAND = 0.60f;
    private static final float SHAKE_DEATH = 1.18f;

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

    private float stunGroundTimer;

    private boolean maceHitActive;
    private boolean maceSlamImpactTriggered;
    private boolean powerSlamImpactTriggered;
    private boolean shockwaveActive;
    private boolean corpseGrounded;
    private boolean shouldShakeCamera;
    private float pendingShakeIntensity;

    private float shockwaveSpeed;
    private int shockwaveDirection;
    private float shockwaveTime;

    private float bodyContactCooldown;
    private static final float BODY_CONTACT_COOLDOWN = 0.45f;

    public FalseKnight(
        float x,
        float y,
        float width,
        float height,
        float arenaMinX,
        float arenaMaxX,
        float groundY
    ) {
        this.bounds = new Rectangle(x, y, width, height);

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
        if (!alive && state != State.DEAD) {
            return;
        }

        animationTime += delta;
        stateTime += delta;

        if (bodyContactCooldown > 0f) {
            bodyContactCooldown -= delta;
        }

        if (state != State.DEAD) {
            updateHeavyDamageTimer(delta);
        }

        switch (state) {
            case IDLE ->
                updateIdle(delta, playerBounds);

            case MACE_SLAM_ANTIC ->
                updateMaceSlamAntic();

            case MACE_SLAM ->
                updateMaceSlam();

            case MACE_SLAM_RECOVER ->
                updateMaceSlamRecover();

            case CHARGE_RUN_ANTIC ->
                updateChargeAntic(playerBounds);

            case CHARGE_RUN ->
                updateChargeRun(delta, platformWorld);

            case OFFENSIVE_LEAP ->
                updateLeap(delta, platformWorld, false);

            case DEFENSIVE_LEAP ->
                updateLeap(delta, platformWorld, true);

            case POWER_JUMP ->
                updatePowerJump(delta, playerBounds, platformWorld);

            case POWER_SLAM ->
                updatePowerSlam();

            case STUNNED ->
                updateStunned(delta);

            case STUN_RECOVER ->
                updateStunRecover();

            case DEAD ->
                updateDead(delta);
        }

        updateShockwave(delta);
        updateHitboxes();
        clampToArena();
    }

    /* ----------------------------------------------------------------- */
    /* IDLE + decision making                                            */
    /* ----------------------------------------------------------------- */

    private void updateIdle(
        float delta,
        Rectangle playerBounds
    ) {
        maceHitActive = false;
        velocityX = 0f;
        velocityY = 0f;

        facePlayer(playerBounds);

        float distance = horizontalDistanceTo(playerBounds);

        /*
         * Hold ground by default. Only nudge back if we are basically standing
         * on top of the Knight, and only advance slowly if the player is very
         * far away. Everything else (closing gaps) is the job of Charge Run and
         * the leaps, which keeps the real distance varied for the AI below.
         */
        if (distance < MIN_STANDOFF) {
            float away = isPlayerOnRight(playerBounds) ? -1f : 1f;
            bounds.x += away * currentWalkSpeed() * delta;
        } else if (distance > IDLE_ADVANCE_DISTANCE) {
            float toward = isPlayerOnRight(playerBounds) ? 1f : -1f;
            bounds.x += toward * currentWalkSpeed() * 0.7f * delta;
        }

        aiTimer -= delta;

        if (aiTimer > 0f) {
            return;
        }

        startMove(chooseMove(playerBounds), playerBounds);
    }

    private Move chooseMove(
        Rectangle playerBounds
    ) {
        float distance = horizontalDistanceTo(playerBounds);

        for (int attempt = 0; attempt < 10; attempt++) {
            float roll = MathUtils.random();

            Move chosen;

            if (distance <= CLOSE_DISTANCE) {
                chosen = chooseCloseMove(roll);
            } else if (distance >= FAR_DISTANCE) {
                chosen = chooseFarMove(roll);
            } else {
                chosen = chooseMediumMove(roll);
            }

            if (isMoveAllowed(chosen)) {
                return chosen;
            }
        }

        return getFallbackMove(distance);
    }

    /*
     * Close range: Mace Slam is the dominant choice (rule: its probability must
     * greatly increase when the player is close). Phase two adds Power Mace
     * Slam, and an occasional Offensive Leap lets the knight reposition.
     */
    private Move chooseCloseMove(float roll) {
        if (phaseTwo && roll < 0.20f) {
            return Move.POWER_MACE_SLAM;
        }

        if (roll < 0.80f) {
            return Move.MACE_SLAM;
        }

        return Move.OFFENSIVE_LEAP;
    }

    /*
     * Medium range: too far for a mace to connect, so close the gap. Charge Run
     * and Offensive Leap share the load, with Power Mace Slam in phase two.
     */
    private Move chooseMediumMove(float roll) {
        if (phaseTwo && roll < 0.22f) {
            return Move.POWER_MACE_SLAM;
        }

        if (roll < 0.55f) {
            return Move.CHARGE_RUN;
        }

        return Move.OFFENSIVE_LEAP;
    }

    /*
     * Long range: the knight becomes enraged and mostly charges, with leaps as
     * the alternative and Power Mace Slam in phase two.
     */
    private Move chooseFarMove(float roll) {
        if (phaseTwo && roll < 0.25f) {
            return Move.POWER_MACE_SLAM;
        }

        if (roll < 0.68f) {
            return Move.CHARGE_RUN;
        }

        return Move.OFFENSIVE_LEAP;
    }

    private boolean isMoveAllowed(Move move) {
        if (move == Move.POWER_MACE_SLAM && !phaseTwo) {
            return false;
        }

        /* Anti-spam: never the same move twice in a row. */
        return move != lastMove;
    }

    private Move getFallbackMove(float distance) {
        if (distance <= CLOSE_DISTANCE && lastMove != Move.MACE_SLAM) {
            return Move.MACE_SLAM;
        }

        if (lastMove != Move.CHARGE_RUN) {
            return Move.CHARGE_RUN;
        }

        if (lastMove != Move.OFFENSIVE_LEAP) {
            return Move.OFFENSIVE_LEAP;
        }

        return Move.MACE_SLAM;
    }

    /* ----------------------------------------------------------------- */
    /* Move start                                                        */
    /* ----------------------------------------------------------------- */

    private void startMove(
        Move move,
        Rectangle playerBounds
    ) {
        lastMove = move;
        stateTime = 0f;
        animationTime = 0f;
        maceHitActive = false;
        maceSlamImpactTriggered = false;
        powerSlamImpactTriggered = false;

        switch (move) {
            case MACE_SLAM -> {
                facePlayer(playerBounds);
                state = State.MACE_SLAM_ANTIC;
                velocityX = 0f;
            }

            case CHARGE_RUN -> {
                facePlayer(playerBounds);
                state = State.CHARGE_RUN_ANTIC;
                velocityX = 0f;
            }

            case OFFENSIVE_LEAP ->
                startOffensiveLeap(playerBounds);

            case DEFENSIVE_LEAP ->
                startDefensiveLeap();

            case POWER_MACE_SLAM ->
                startPowerJump(playerBounds);
        }
    }

    private void startOffensiveLeap(Rectangle playerBounds) {
        facingRight = isPlayerOnRight(playerBounds);
        state = State.OFFENSIVE_LEAP;
        velocityY = LEAP_VERTICAL_SPEED;
        velocityX = computeArcVelocityX(
            playerBounds,
            LEAP_VERTICAL_SPEED,
            LEAP_MAX_HORIZONTAL
        );
    }

    private void startPowerJump(Rectangle playerBounds) {
        facingRight = isPlayerOnRight(playerBounds);
        state = State.POWER_JUMP;
        powerSlamImpactTriggered = false;
        velocityY = POWER_JUMP_VERTICAL_SPEED;
        velocityX = computeArcVelocityX(
            playerBounds,
            POWER_JUMP_VERTICAL_SPEED,
            POWER_MAX_HORIZONTAL
        );
    }

    private void startDefensiveLeap() {
        lastMove = Move.DEFENSIVE_LEAP;
        state = State.DEFENSIVE_LEAP;
        stateTime = 0f;
        animationTime = 0f;
        maceHitActive = false;

        velocityY = DEFENSIVE_LEAP_VERTICAL_SPEED;

        /* Leap backwards, i.e. away from the player. */
        velocityX = facingRight
            ? -DEFENSIVE_LEAP_HORIZONTAL_SPEED
            : DEFENSIVE_LEAP_HORIZONTAL_SPEED;
    }

    /*
     * Horizontal launch speed that lands a symmetric jump near the player's
     * current position, clamped so the arc stays believable.
     */
    private float computeArcVelocityX(
        Rectangle playerBounds,
        float verticalSpeed,
        float maxHorizontal
    ) {
        float flightTime = 2f * verticalSpeed / Math.abs(GRAVITY);

        float dx = getPlayerCenterX(playerBounds) - getCenterX();

        float vx = dx / flightTime;

        return MathUtils.clamp(vx, -maxHorizontal, maxHorizontal);
    }

    /* ----------------------------------------------------------------- */
    /* Mace Slam                                                         */
    /* ----------------------------------------------------------------- */

    private void updateMaceSlamAntic() {
        velocityX = 0f;

        if (stateTime >= MACE_SLAM_ANTIC_TIME) {
            state = State.MACE_SLAM;
            stateTime = 0f;
            animationTime = 0f;
            maceHitActive = false;
            maceSlamImpactTriggered = false;
        }
    }

    private void updateMaceSlam() {
        if (
            !maceSlamImpactTriggered
                && stateTime >= MACE_SLAM_IMPACT_TIME
        ) {
            maceSlamImpactTriggered = true;
            maceHitActive = true;
            requestCameraShake(SHAKE_MACE_SLAM);
        }

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

    /* ----------------------------------------------------------------- */
    /* Charge Run                                                        */
    /* ----------------------------------------------------------------- */

    private void updateChargeAntic(Rectangle playerBounds) {
        velocityX = 0f;

        if (stateTime >= CHARGE_ANTIC_TIME) {
            /* Commit to the player's position at the moment we launch. */
            facePlayer(playerBounds);

            state = State.CHARGE_RUN;
            stateTime = 0f;
            animationTime = 0f;

            velocityX = facingRight
                ? getChargeSpeed()
                : -getChargeSpeed();
        }
    }

    private void updateChargeRun(
        float delta,
        PlatformWorld platformWorld
    ) {
        Rectangle nextBounds = new Rectangle(bounds);
        nextBounds.x += velocityX * delta;

        boolean hitWall =
            nextBounds.x <= arenaMinX
                || nextBounds.x + nextBounds.width >= arenaMaxX
                || platformWorld.overlapsSolid(nextBounds);

        /*
         * The charge commits fully: it does not brake next to the player, it
         * barrels through until it slams a wall or the charge times out. Body
         * contact damage is handled by the controller.
         */
        if (hitWall) {
            requestCameraShake(SHAKE_CHARGE_WALL);
            returnToIdle();
            return;
        }

        if (stateTime >= CHARGE_DURATION) {
            requestCameraShake(SHAKE_CHARGE_TIMEOUT);
            returnToIdle();
            return;
        }

        bounds.x = nextBounds.x;
    }

    public boolean canDealBodyDamage() {
        return state == State.CHARGE_RUN
            || state == State.OFFENSIVE_LEAP
            || state == State.DEFENSIVE_LEAP
            || state == State.POWER_JUMP;
    }

    public boolean canApplyBodyContactNow() {
        return canDealBodyDamage()
            && bodyContactCooldown <= 0f;
    }

    public void registerBodyContact() {
        bodyContactCooldown = BODY_CONTACT_COOLDOWN;
    }

    /* ----------------------------------------------------------------- */
    /* Leaps                                                             */
    /* ----------------------------------------------------------------- */

    private void updateLeap(
        float delta,
        PlatformWorld platformWorld,
        boolean defensive
    ) {
        velocityY += GRAVITY * delta;

        Rectangle nextBounds = new Rectangle(bounds);
        nextBounds.x += velocityX * delta;
        nextBounds.y += velocityY * delta;

        if (
            nextBounds.x < arenaMinX
                || nextBounds.x + nextBounds.width > arenaMaxX
        ) {
            /* Bounce off the arena wall instead of clipping through it. */
            velocityX *= -0.4f;
        } else {
            bounds.x = nextBounds.x;
        }

        if (nextBounds.y <= groundY) {
            bounds.y = groundY;
            velocityY = 0f;

            requestCameraShake(
                defensive ? SHAKE_DEFENSIVE_LAND : SHAKE_OFFENSIVE_LAND
            );

            returnToIdle();
            return;
        }

        if (!platformWorld.overlapsSolid(nextBounds)) {
            bounds.y = nextBounds.y;
        }
    }

    /* ----------------------------------------------------------------- */
    /* Power Mace Slam (phase two)                                       */
    /* ----------------------------------------------------------------- */

    private void updatePowerJump(
        float delta,
        Rectangle playerBounds,
        PlatformWorld platformWorld
    ) {
        velocityY += GRAVITY * delta;

        Rectangle nextBounds = new Rectangle(bounds);
        nextBounds.x += velocityX * delta;
        nextBounds.y += velocityY * delta;

        if (
            nextBounds.x >= arenaMinX
                && nextBounds.x + nextBounds.width <= arenaMaxX
                && !platformWorld.overlapsSolid(nextBounds)
        ) {
            bounds.x = nextBounds.x;
        }

        if (nextBounds.y <= groundY) {
            bounds.y = groundY;
            velocityY = 0f;
            velocityX = 0f;

            /* Face the player so the shockwave travels toward them. */
            facePlayer(playerBounds);

            state = State.POWER_SLAM;
            stateTime = 0f;
            animationTime = 0f;

            maceHitActive = false;
            powerSlamImpactTriggered = false;
            return;
        }

        bounds.y = nextBounds.y;
    }

    private void updatePowerSlam() {
        if (
            !powerSlamImpactTriggered
                && stateTime >= POWER_SLAM_IMPACT_TIME
        ) {
            powerSlamImpactTriggered = true;
            maceHitActive = true;

            /*
             * The wave begins on the exact animation frame where the mace
             * reaches the floor, rather than when the boss body lands.
             */
            startShockwave();
            requestCameraShake(SHAKE_POWER_SLAM);
        }

        if (
            powerSlamImpactTriggered
                && stateTime >= POWER_SLAM_HIT_END_TIME
        ) {
            maceHitActive = false;
        }

        if (stateTime >= POWER_SLAM_GROUND_TIME) {
            maceHitActive = false;
            returnToIdle();
        }
    }

    /* ----------------------------------------------------------------- */
    /* Stun + phase transition                                           */
    /* ----------------------------------------------------------------- */

    private void updateStunned(float delta) {
        maceHitActive = false;
        velocityX = 0f;

        /*
         * If the stun was triggered mid-air (for example during a leap when HP
         * crosses 50%), the knight must fall to the ground before the stun
         * window starts, so he never freezes floating in the air.
         */
        if (bounds.y > groundY) {
            velocityY += GRAVITY * delta;
            bounds.y += velocityY * delta;

            if (bounds.y <= groundY) {
                bounds.y = groundY;
                velocityY = 0f;
                requestCameraShake(SHAKE_STUN_LAND);
            }

            return;
        }

        bounds.y = groundY;
        velocityY = 0f;

        stunGroundTimer += delta;

        if (stunGroundTimer >= STUN_DURATION) {
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
        maceSlamImpactTriggered = false;
        powerSlamImpactTriggered = false;

        aiTimer = phaseTwo
            ? AI_DELAY_PHASE_TWO
            : AI_DELAY_PHASE_ONE;
    }

    /* ----------------------------------------------------------------- */
    /* Damage                                                            */
    /* ----------------------------------------------------------------- */

    public void takeDamage(
        int damage,
        boolean hitVulnerableBody
    ) {
        if (!alive || state == State.DEAD) {
            return;
        }

        if (state == State.STUNNED && !hitVulnerableBody) {
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

        if (!stunTriggered && health <= STUN_HEALTH_THRESHOLD) {
            triggerStun();
            return;
        }

        /*
         * Reactive Defensive Leap: sustained heavy damage in a short window
         * makes the knight hop backwards. It has a random element and can
         * interrupt wind-up / recovery, but never chains into itself.
         */
        if (
            recentHits >= HEAVY_DAMAGE_TRIGGER
                && lastMove != Move.DEFENSIVE_LEAP
                && canInterruptForDefensiveLeap()
                && MathUtils.random() < DEFENSIVE_LEAP_REACTION_CHANCE
        ) {
            recentHits = 0;
            startDefensiveLeap();
        }
    }

    private boolean canInterruptForDefensiveLeap() {
        return state == State.IDLE
            || state == State.MACE_SLAM_ANTIC
            || state == State.MACE_SLAM_RECOVER
            || state == State.CHARGE_RUN_ANTIC;
    }

    private void triggerStun() {
        stunTriggered = true;
        state = State.STUNNED;
        stateTime = 0f;
        animationTime = 0f;
        stunGroundTimer = 0f;

        velocityX = 0f;
        velocityY = 0f;
        maceHitActive = false;
        maceSlamImpactTriggered = false;
        powerSlamImpactTriggered = false;
        shockwaveActive = false;

        requestCameraShake(SHAKE_STUN);
    }

    private void die() {
        alive = false;
        state = State.DEAD;
        stateTime = 0f;
        animationTime = 0f;

        maceHitActive = false;
        maceSlamImpactTriggered = false;
        powerSlamImpactTriggered = false;
        shockwaveActive = false;

        velocityX = 0f;
        corpseGrounded = bounds.y <= groundY + 0.5f;

        if (corpseGrounded) {
            bounds.y = groundY;
            velocityY = 0f;
        }

        requestCameraShake(SHAKE_DEATH);
    }

    private void updateDead(float delta) {
        maceHitActive = false;
        velocityX = 0f;

        if (corpseGrounded) {
            bounds.y = groundY;
            velocityY = 0f;
            return;
        }

        velocityY += GRAVITY * delta;
        bounds.y += velocityY * delta;

        if (bounds.y <= groundY) {
            bounds.y = groundY;
            velocityY = 0f;
            corpseGrounded = true;
        }
    }

    /* ----------------------------------------------------------------- */
    /* Shockwave                                                         */
    /* ----------------------------------------------------------------- */

    private void startShockwave() {
        shockwaveActive = true;
        shockwaveDirection = facingRight ? 1 : -1;
        shockwaveSpeed = 260f;
        shockwaveTime = 0f;

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

    private void updateShockwave(float delta) {
        if (!shockwaveActive) {
            return;
        }

        shockwaveTime += delta;

        /* Gradually gains more speed as it travels. */
        shockwaveSpeed += 520f * delta;

        shockwaveHitbox.x +=
            shockwaveDirection * shockwaveSpeed * delta;

        if (
            shockwaveHitbox.x < arenaMinX
                || shockwaveHitbox.x > arenaMaxX
        ) {
            shockwaveActive = false;
        }
    }

    private void updateHeavyDamageTimer(float delta) {
        if (heavyDamageTimer <= 0f) {
            recentHits = 0;
            return;
        }

        heavyDamageTimer -= delta;

        if (heavyDamageTimer <= 0f) {
            recentHits = 0;
        }
    }

    /* ----------------------------------------------------------------- */
    /* Hitboxes / geometry                                               */
    /* ----------------------------------------------------------------- */

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

        /*
         * The exposed maggot is drawn outside most of the armour body while
         * stunned. Keep this hitbox aligned with the visible creature and
         * mirror it when the boss turns. The previous centered hitbox sat on
         * the armour, making direct nail hits on the creature miss.
         */
        float vulnerableWidth = bounds.width * 0.47f;
        float vulnerableHeight = bounds.height * 0.50f;

        float vulnerableX = facingRight
            ? bounds.x + bounds.width * 0.76f
            : bounds.x - bounds.width * 0.24f;

        vulnerableHitbox.set(
            vulnerableX,
            bounds.y + bounds.height * 0.06f,
            vulnerableWidth,
            vulnerableHeight
        );
    }

    private void facePlayer(Rectangle playerBounds) {
        facingRight = getPlayerCenterX(playerBounds) > getCenterX();
    }

    private boolean isPlayerOnRight(Rectangle playerBounds) {
        return getPlayerCenterX(playerBounds) >= getCenterX();
    }

    private float horizontalDistanceTo(Rectangle playerBounds) {
        return Math.abs(getCenterX() - getPlayerCenterX(playerBounds));
    }

    private float getPlayerCenterX(Rectangle playerBounds) {
        return playerBounds.x + playerBounds.width / 2f;
    }

    private float getCenterX() {
        return bounds.x + bounds.width / 2f;
    }

    private float currentWalkSpeed() {
        return phaseTwo ? PHASE_TWO_WALK_SPEED : PHASE_ONE_WALK_SPEED;
    }

    private float getChargeSpeed() {
        return phaseTwo ? CHARGE_SPEED_PHASE_TWO : CHARGE_SPEED_PHASE_ONE;
    }

    private void clampToArena() {
        if (bounds.x < arenaMinX) {
            bounds.x = arenaMinX;
        }

        if (bounds.x + bounds.width > arenaMaxX) {
            bounds.x = arenaMaxX - bounds.width;
        }
    }

    /* ----------------------------------------------------------------- */
    /* Camera shake plumbing                                             */
    /* ----------------------------------------------------------------- */

    private void requestCameraShake(float intensity) {
        shouldShakeCamera = true;

        /* Keep the strongest pending request so a small hit can't weaken it. */
        if (intensity > pendingShakeIntensity) {
            pendingShakeIntensity = intensity;
        }
    }

    public boolean consumeCameraShakeRequest() {
        if (!shouldShakeCamera) {
            return false;
        }

        shouldShakeCamera = false;
        return true;
    }

    public float getPendingShakeIntensity() {
        float intensity = pendingShakeIntensity;
        pendingShakeIntensity = 0f;
        return intensity;
    }

    /* ----------------------------------------------------------------- */
    /* Accessors                                                         */
    /* ----------------------------------------------------------------- */

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

    public float getShockwaveAnimationTime() {
        return shockwaveTime;
    }

    public boolean isShockwaveMovingRight() {
        return shockwaveDirection >= 0;
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

    public boolean isCorpseGrounded() {
        return corpseGrounded;
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
        stunGroundTimer = 0f;

        maceHitActive = false;
        maceSlamImpactTriggered = false;
        powerSlamImpactTriggered = false;
        shockwaveActive = false;
        corpseGrounded = false;
        shouldShakeCamera = false;
        pendingShakeIntensity = 0f;
        shockwaveTime = 0f;
    }
}
