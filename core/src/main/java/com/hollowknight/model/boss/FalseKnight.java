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

    /*
     * Defensive Leap remains a random reaction to sustained pressure, but a
     * cooldown and distance-scaled chance stop repeated hits from forcing the
     * boss into the same escape over and over.
     */
    private static final float DEFENSIVE_LEAP_CHANCE_CLOSE = 0.55f;
    private static final float DEFENSIVE_LEAP_CHANCE_FAR = 0.18f;
    private static final float DEFENSIVE_LEAP_COOLDOWN_PHASE_ONE = 4.25f;
    private static final float DEFENSIVE_LEAP_COOLDOWN_PHASE_TWO = 3.50f;

    private static final float RECENT_MOVE_REPEAT_WEIGHT = 0.24f;

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
    private Move previousMove = null;

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
    private float defensiveLeapCooldown;
    private float lastPlayerDistance = FAR_DISTANCE;

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

        if (defensiveLeapCooldown > 0f) {
            defensiveLeapCooldown = Math.max(
                0f,
                defensiveLeapCooldown - delta
            );
        }

        lastPlayerDistance = horizontalDistanceTo(playerBounds);

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

        float maceWeight;
        float chargeWeight;
        float offensiveLeapWeight;
        float powerSlamWeight;

        /*
         * Distance changes probabilities rather than selecting a fixed script.
         * Close range strongly favours the mace, medium range mixes gap-closing
         * moves, and long range strongly favours Charge Run.
         */
        if (distance <= CLOSE_DISTANCE) {
            maceWeight = 58f;
            chargeWeight = 8f;
            offensiveLeapWeight = 26f;
            powerSlamWeight = phaseTwo ? 18f : 0f;
        } else if (distance >= FAR_DISTANCE) {
            maceWeight = 4f;
            chargeWeight = 54f;
            offensiveLeapWeight = 34f;
            powerSlamWeight = phaseTwo ? 22f : 0f;
        } else {
            float rangeAlpha = (distance - CLOSE_DISTANCE)
                / (FAR_DISTANCE - CLOSE_DISTANCE);

            maceWeight = MathUtils.lerp(24f, 7f, rangeAlpha);
            chargeWeight = MathUtils.lerp(30f, 49f, rangeAlpha);
            offensiveLeapWeight = MathUtils.lerp(39f, 35f, rangeAlpha);
            powerSlamWeight = phaseTwo
                ? MathUtils.lerp(17f, 21f, rangeAlpha)
                : 0f;
        }

        maceWeight = applyMoveHistoryWeight(
            Move.MACE_SLAM,
            maceWeight
        );
        chargeWeight = applyMoveHistoryWeight(
            Move.CHARGE_RUN,
            chargeWeight
        );
        offensiveLeapWeight = applyMoveHistoryWeight(
            Move.OFFENSIVE_LEAP,
            offensiveLeapWeight
        );
        powerSlamWeight = applyMoveHistoryWeight(
            Move.POWER_MACE_SLAM,
            powerSlamWeight
        );

        float totalWeight = maceWeight
            + chargeWeight
            + offensiveLeapWeight
            + powerSlamWeight;

        if (totalWeight <= 0f) {
            return getFallbackMove(distance);
        }

        float roll = MathUtils.random(totalWeight);

        if ((roll -= maceWeight) < 0f) {
            return Move.MACE_SLAM;
        }

        if ((roll -= chargeWeight) < 0f) {
            return Move.CHARGE_RUN;
        }

        if ((roll -= offensiveLeapWeight) < 0f) {
            return Move.OFFENSIVE_LEAP;
        }

        return Move.POWER_MACE_SLAM;
    }

    private float applyMoveHistoryWeight(
        Move move,
        float baseWeight
    ) {
        if (baseWeight <= 0f) {
            return 0f;
        }

        /* Hard anti-spam: never repeat the immediately previous move. */
        if (move == lastMove) {
            return 0f;
        }

        /* Soft anti-pattern: discourage A-B-A-B loops without deadlocking AI. */
        if (move == previousMove) {
            return baseWeight * RECENT_MOVE_REPEAT_WEIGHT;
        }

        return baseWeight;
    }

    private Move getFallbackMove(float distance) {
        if (distance <= CLOSE_DISTANCE && lastMove != Move.MACE_SLAM) {
            return Move.MACE_SLAM;
        }

        if (distance >= FAR_DISTANCE && lastMove != Move.CHARGE_RUN) {
            return Move.CHARGE_RUN;
        }

        if (lastMove != Move.OFFENSIVE_LEAP) {
            return Move.OFFENSIVE_LEAP;
        }

        if (phaseTwo && lastMove != Move.POWER_MACE_SLAM) {
            return Move.POWER_MACE_SLAM;
        }

        return lastMove == Move.MACE_SLAM
            ? Move.CHARGE_RUN
            : Move.MACE_SLAM;
    }

    private void rememberMove(Move move) {
        previousMove = lastMove;
        lastMove = move;
    }

    /* ----------------------------------------------------------------- */
    /* Move start                                                        */
    /* ----------------------------------------------------------------- */

    private void startMove(
        Move move,
        Rectangle playerBounds
    ) {
        rememberMove(move);
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
        rememberMove(Move.DEFENSIVE_LEAP);
        defensiveLeapCooldown = phaseTwo
            ? DEFENSIVE_LEAP_COOLDOWN_PHASE_TWO
            : DEFENSIVE_LEAP_COOLDOWN_PHASE_ONE;
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
         * One random defensive decision is made per full damage-pressure
         * threshold. Resetting the counter even when the roll fails prevents
         * every later hit from rerolling until a jump becomes inevitable.
         */
        if (recentHits >= HEAVY_DAMAGE_TRIGGER) {
            recentHits = 0;

            if (
                defensiveLeapCooldown <= 0f
                    && lastMove != Move.DEFENSIVE_LEAP
                    && canInterruptForDefensiveLeap()
                    && MathUtils.random()
                        < defensiveLeapReactionChance()
            ) {
                startDefensiveLeap();
            }
        }
    }

    private float defensiveLeapReactionChance() {
        float distanceAlpha = MathUtils.clamp(
            (lastPlayerDistance - CLOSE_DISTANCE)
                / (FAR_DISTANCE - CLOSE_DISTANCE),
            0f,
            1f
        );

        float chance = MathUtils.lerp(
            DEFENSIVE_LEAP_CHANCE_CLOSE,
            DEFENSIVE_LEAP_CHANCE_FAR,
            distanceAlpha
        );

        /* Phase two reacts a little faster, but still respects the cooldown. */
        return phaseTwo
            ? Math.min(0.64f, chance + 0.07f)
            : chance;
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
        previousMove = null;

        stateTime = 0f;
        animationTime = 0f;
        aiTimer = 1.0f;

        velocityX = 0f;
        velocityY = 0f;

        recentHits = 0;
        heavyDamageTimer = 0f;
        defensiveLeapCooldown = 0f;
        lastPlayerDistance = FAR_DISTANCE;
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
