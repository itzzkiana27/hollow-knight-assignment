package com.hollowknight.model.player;

import com.badlogic.gdx.math.MathUtils;
import com.hollowknight.model.input.PlayerInput;

/**
 * Owns all current movement state and movement rules for the Knight.
 *
 * This class does not read keyboard input directly. The controller supplies a
 * PlayerInput snapshot, while this model updates position, velocity, movement
 * state, and movement-related animations.
 */
public final class PlayerMovement {

    private static final float MOVE_SPEED = 230f;

    private static final float GRAVITY = -1400f;
    private static final float JUMP_SPEED = 560f;
    private static final float DOUBLE_JUMP_SPEED = 520f;
    private static final float MAX_FALL_SPEED = -850f;

    private static final float DASH_DURATION = 0.54f;
    private static final float DASH_SPEED = 390f;
    private static final float DASH_COOLDOWN = 0.30f;

    private static final float WALL_CONTACT_TOLERANCE = 3f;
    private static final float WALL_SLIDE_SPEED = -170f;

    private static final float WALL_JUMP_VERTICAL_SPEED = 560f;
    private static final float WALL_JUMP_HORIZONTAL_SPEED = 340f;
    private static final float WALL_JUMP_PUSH_DURATION = 0.18f;
    private static final float WALL_JUMP_DETACH_DISTANCE = 5f;

    private final Player player;
    private final float spawnX;
    private final float groundY;

    private float verticalVelocity;

    private int jumpsUsed;
    private boolean onGround;
    private boolean airDashUsed;

    private float dashTimeRemaining;
    private float dashCooldownRemaining;
    private int dashDirection;

    private float wallJumpPushTimeRemaining;
    private int wallJumpDirection;

    public PlayerMovement(
        Player player,
        float spawnX,
        float groundY
    ) {
        this.player = player;
        this.spawnX = spawnX;
        this.groundY = groundY;

        resetState();
    }

    public void resetPlayer() {
        player.reset(spawnX, groundY);
        resetState();
    }

    private void resetState() {
        verticalVelocity = 0f;

        jumpsUsed = 0;
        onGround = true;
        airDashUsed = false;

        dashTimeRemaining = 0f;
        dashCooldownRemaining = 0f;
        dashDirection = 1;

        wallJumpPushTimeRemaining = 0f;
        wallJumpDirection = 1;
    }

    public void prepareForDeath() {
        player.getPosition().y = groundY;

        verticalVelocity = 0f;
        jumpsUsed = 0;
        onGround = true;
        airDashUsed = false;

        dashTimeRemaining = 0f;
        wallJumpPushTimeRemaining = 0f;
    }

    public void updateDashCooldown(float delta) {
        if (dashCooldownRemaining <= 0f) {
            return;
        }

        dashCooldownRemaining -= delta;

        if (dashCooldownRemaining < 0f) {
            dashCooldownRemaining = 0f;
        }
    }

    public boolean canStartDash() {
        return dashCooldownRemaining <= 0f
            && (onGround || !airDashUsed);
    }

    public void startDash(PlayerInput input) {
        int requestedDirection =
            input.getHorizontalDirection();

        if (requestedDirection == 0) {
            requestedDirection =
                player.isFacingRight()
                    ? 1
                    : -1;
        }

        dashDirection = requestedDirection;
        dashTimeRemaining = DASH_DURATION;

        dashCooldownRemaining =
            DASH_DURATION + DASH_COOLDOWN;

        if (!onGround) {
            airDashUsed = true;
        }

        wallJumpPushTimeRemaining = 0f;

        player.setFacingRight(
            dashDirection > 0
        );

        player.setMovementState(
            PlayerMovementState.DASHING
        );

        player.setAnimation(
            PlayerAnimationType.DASH
        );
    }

    public boolean isDashing() {
        return dashTimeRemaining > 0f;
    }

    /**
     * Updates horizontal dash movement.
     *
     * @return true when this update finishes the dash.
     */
    public boolean updateDash(
        float delta,
        float maximumX
    ) {
        player.getPosition().x +=
            dashDirection
                * DASH_SPEED
                * delta;

        clampHorizontalPosition(maximumX);

        /*
         * Gravity intentionally remains paused
         * during the dash.
         */
        dashTimeRemaining -= delta;

        if (dashTimeRemaining <= 0f) {
            dashTimeRemaining = 0f;
            return true;
        }

        return false;
    }

    public void stopDash() {
        dashTimeRemaining = 0f;
    }

    public void finishDash(PlayerInput input) {
        if (onGround) {
            player.setMovementState(
                PlayerMovementState.GROUNDED
            );
        } else if (verticalVelocity > 0f) {
            player.setMovementState(
                PlayerMovementState.AIRBORNE
            );
        } else {
            player.setMovementState(
                PlayerMovementState.FALLING
            );
        }

        selectAnimationAfterAction(input);
    }

    /**
     * Handles normal jump, double jump, and wall jump.
     *
     * @return true only when a wall jump has just started.
     */
    public boolean handleJumpInput(
        PlayerInput input,
        float maximumX
    ) {
        if (!input.isJumpPressed()) {
            return false;
        }

        int heldWallSide =
            getHeldWallSide(
                input,
                maximumX
            );

        if (
            !onGround
                && verticalVelocity <= 0f
                && heldWallSide != 0
        ) {
            startWallJump(
                heldWallSide,
                maximumX
            );

            return true;
        }

        /*
         * First jump from the ground.
         */
        if (onGround) {
            onGround = false;
            jumpsUsed = 1;
            airDashUsed = false;

            verticalVelocity =
                JUMP_SPEED;

            player.setMovementState(
                PlayerMovementState.AIRBORNE
            );

            player.setAnimation(
                PlayerAnimationType.AIRBORNE
            );

            return false;
        }

        /*
         * Double jump.
         */
        if (jumpsUsed < 2) {
            jumpsUsed = 2;

            verticalVelocity =
                DOUBLE_JUMP_SPEED;

            player.setMovementState(
                PlayerMovementState.AIRBORNE
            );

            player.setAnimation(
                PlayerAnimationType.DOUBLE_JUMP
            );
        }

        return false;
    }

    private void startWallJump(
        int wallSide,
        float maximumX
    ) {
        /*
         * wallSide:
         *
         * -1 = left wall
         *  1 = right wall
         *
         * The jump direction is away from the wall.
         */
        wallJumpDirection = -wallSide;

        wallJumpPushTimeRemaining =
            WALL_JUMP_PUSH_DURATION;

        verticalVelocity =
            WALL_JUMP_VERTICAL_SPEED;

        onGround = false;

        /*
         * One double jump remains available
         * after the wall jump.
         */
        jumpsUsed = 1;

        player.getPosition().x +=
            wallJumpDirection
                * WALL_JUMP_DETACH_DISTANCE;

        clampHorizontalPosition(maximumX);

        player.setFacingRight(
            wallJumpDirection > 0
        );

        player.setMovementState(
            PlayerMovementState.AIRBORNE
        );

        player.setAnimation(
            PlayerAnimationType.WALL_JUMP
        );
    }

    public boolean isWallJumpPushActive() {
        return wallJumpPushTimeRemaining > 0f;
    }

    public void updateWallJumpPush(
        float delta,
        float maximumX
    ) {
        player.getPosition().x +=
            wallJumpDirection
                * WALL_JUMP_HORIZONTAL_SPEED
                * delta;

        clampHorizontalPosition(maximumX);

        wallJumpPushTimeRemaining -= delta;

        if (wallJumpPushTimeRemaining < 0f) {
            wallJumpPushTimeRemaining = 0f;
        }
    }

    public void updateHorizontalMovement(
        float delta,
        PlayerInput input,
        float maximumX
    ) {
        int horizontalDirection =
            input.getHorizontalDirection();

        if (horizontalDirection == 0) {
            return;
        }

        player.getPosition().x +=
            horizontalDirection
                * MOVE_SPEED
                * delta;

        clampHorizontalPosition(maximumX);

        player.setFacingRight(
            horizontalDirection > 0
        );
    }

    public void updateVerticalMovement(
        float delta,
        boolean wallSliding,
        boolean movementLocked
    ) {
        if (onGround) {
            player.getPosition().y =
                groundY;

            verticalVelocity = 0f;
            return;
        }

        verticalVelocity +=
            GRAVITY * delta;

        if (wallSliding) {
            verticalVelocity = Math.max(
                verticalVelocity,
                WALL_SLIDE_SPEED
            );
        } else {
            verticalVelocity = Math.max(
                verticalVelocity,
                MAX_FALL_SPEED
            );
        }

        player.getPosition().y +=
            verticalVelocity * delta;

        /*
         * The Knight reached the temporary floor.
         */
        if (
            player.getPosition().y
                <= groundY
        ) {
            player.getPosition().y =
                groundY;

            verticalVelocity = 0f;

            jumpsUsed = 0;
            onGround = true;
            airDashUsed = false;

            wallJumpPushTimeRemaining = 0f;

            player.setMovementState(
                PlayerMovementState.GROUNDED
            );

            if (!movementLocked) {
                player.setAnimation(
                    PlayerAnimationType.LANDING
                );
            }

            return;
        }

        if (wallSliding) {
            player.setMovementState(
                PlayerMovementState.WALL_SLIDING
            );
        } else if (verticalVelocity > 0f) {
            player.setMovementState(
                PlayerMovementState.AIRBORNE
            );
        } else {
            player.setMovementState(
                PlayerMovementState.FALLING
            );
        }
    }

    public boolean shouldWallSlide(
        PlayerInput input,
        float maximumX
    ) {
        if (
            onGround
                || verticalVelocity > 0f
        ) {
            return false;
        }

        return getHeldWallSide(
            input,
            maximumX
        ) != 0;
    }

    private int getHeldWallSide(
        PlayerInput input,
        float maximumX
    ) {
        int wallSide =
            getTouchingWallSide(maximumX);

        if (
            wallSide == -1
                && input.isMoveLeftHeld()
        ) {
            return -1;
        }

        if (
            wallSide == 1
                && input.isMoveRightHeld()
        ) {
            return 1;
        }

        return 0;
    }

    private int getTouchingWallSide(
        float maximumX
    ) {
        float playerX =
            player.getPosition().x;

        if (
            playerX
                <= WALL_CONTACT_TOLERANCE
        ) {
            return -1;
        }

        if (
            playerX
                >= maximumX
                - WALL_CONTACT_TOLERANCE
        ) {
            return 1;
        }

        return 0;
    }

    public void updateMovementAnimation(
        PlayerInput input,
        boolean movementLocked,
        boolean wallSliding
    ) {
        if (
            player.isDead()
                || movementLocked
        ) {
            return;
        }

        PlayerAnimationType currentAnimation =
            player.getAnimationType();

        /*
         * Wall slide has priority over falling.
         */
        if (wallSliding) {
            player.setAnimation(
                PlayerAnimationType.WALL_SLIDE
            );

            return;
        }

        /*
         * Air animations.
         */
        if (!onGround) {
            if (
                currentAnimation
                    == PlayerAnimationType.WALL_JUMP
                    && verticalVelocity > 0f
            ) {
                return;
            }

            if (verticalVelocity <= 0f) {
                player.setAnimation(
                    PlayerAnimationType.FALL
                );
            } else if (
                currentAnimation
                    != PlayerAnimationType.AIRBORNE
                    && currentAnimation
                    != PlayerAnimationType.DOUBLE_JUMP
            ) {
                player.setAnimation(
                    PlayerAnimationType.AIRBORNE
                );
            }

            return;
        }

        int horizontalDirection =
            input.getHorizontalDirection();

        /*
         * The same movement keys produce normal
         * running movement.
         */
        if (horizontalDirection != 0) {
            player.setAnimation(
                PlayerAnimationType.RUN
            );

            return;
        }

        /*
         * Allow transition animations to finish.
         */
        if (
            currentAnimation
                == PlayerAnimationType.LANDING
                || currentAnimation
                == PlayerAnimationType.RUN_TO_IDLE
        ) {
            return;
        }

        if (
            currentAnimation
                == PlayerAnimationType.RUN
                || currentAnimation
                == PlayerAnimationType.WALK
        ) {
            player.setAnimation(
                PlayerAnimationType.RUN_TO_IDLE
            );

            return;
        }

        player.setAnimation(
            PlayerAnimationType.IDLE
        );
    }

    public void selectAnimationAfterAction(
        PlayerInput input
    ) {
        if (!onGround) {
            if (verticalVelocity > 0f) {
                player.setMovementState(
                    PlayerMovementState.AIRBORNE
                );

                player.setAnimation(
                    PlayerAnimationType.AIRBORNE
                );
            } else {
                player.setMovementState(
                    PlayerMovementState.FALLING
                );

                player.setAnimation(
                    PlayerAnimationType.FALL
                );
            }

            return;
        }

        player.setMovementState(
            PlayerMovementState.GROUNDED
        );

        if (
            input.getHorizontalDirection()
                != 0
        ) {
            player.setAnimation(
                PlayerAnimationType.RUN
            );
        } else {
            player.setAnimation(
                PlayerAnimationType.IDLE
            );
        }
    }

    /**
     * Called after a successful downward nail hit
     * against an enemy or spike.
     *
     * The pogo:
     * - launches the Knight upward;
     * - restores one double jump;
     * - restores the aerial dash.
     */
    public void pogoBounce() {
        onGround = false;

        verticalVelocity =
            JUMP_SPEED;

        /*
         * One double jump remains available.
         */
        jumpsUsed = 1;

        /*
         * Restore the aerial dash.
         */
        dashCooldownRemaining = 0f;
        dashTimeRemaining = 0f;
        airDashUsed = false;

        wallJumpPushTimeRemaining = 0f;

        player.setMovementState(
            PlayerMovementState.AIRBORNE
        );

        player.setAnimation(
            PlayerAnimationType.AIRBORNE
        );
    }

    private void clampHorizontalPosition(
        float maximumX
    ) {
        player.getPosition().x =
            MathUtils.clamp(
                player.getPosition().x,
                0f,
                maximumX
            );
    }

    public float getMaximumX(
        float worldWidth,
        float playerDrawWidth
    ) {
        return Math.max(
            0f,
            worldWidth - playerDrawWidth
        );
    }

    public boolean isOnGround() {
        return onGround;
    }

    public float getVerticalVelocity() {
        return verticalVelocity;
    }
}
