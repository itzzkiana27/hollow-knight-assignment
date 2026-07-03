package com.hollowknight.model.player;

import com.hollowknight.model.input.PlayerInput;
import com.hollowknight.model.world.PlatformWorld;

public final class PlayerMovement {

    private static final float MOVE_SPEED = 230f;

    private static final float GRAVITY = -1200f;
    private static final float JUMP_SPEED = 680f;
    private static final float DOUBLE_JUMP_SPEED = 650f;
    private static final float JUMP_CUTOFF_MULTIPLIER = 0.40f;
    private static final float MAX_FALL_SPEED = -850f;

    private static final float DASH_DURATION = 0.54f;
    private static final float DASH_SPEED = 390f;
    private static final float DASH_COOLDOWN = 0.30f;

    private static final float WALL_SLIDE_SPEED = -170f;

    private static final float WALL_JUMP_VERTICAL_SPEED = 680f;
    private static final float WALL_JUMP_HORIZONTAL_SPEED = 340f;
    private static final float WALL_JUMP_PUSH_DURATION = 0.18f;
    private static final float WALL_JUMP_DETACH_DISTANCE = 5f;

    private static final float KNOCKBACK_HORIZONTAL_SPEED = 330f;
    private static final float KNOCKBACK_VERTICAL_SPEED = 300f;
    private static final float KNOCKBACK_DURATION = 0.20f;

    private final Player player;
    private final PlatformWorld platformWorld;

    private final float spawnX;
    private final float spawnY;

    private float verticalVelocity;

    private boolean jumpCutAvailable;
    private int jumpsUsed;
    private boolean onGround;
    private boolean airDashUsed;

    private float dashTimeRemaining;
    private float dashCooldownRemaining;
    private int dashDirection;

    private float wallJumpPushTimeRemaining;
    private int wallJumpDirection;

    private float knockbackTimeRemaining;
    private int knockbackDirection;

    public PlayerMovement(
        Player player,
        float spawnX,
        float spawnY,
        PlatformWorld platformWorld
    ) {
        this.player = player;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.platformWorld = platformWorld;

        resetState();
    }

    public void resetPlayer() {
        player.reset(
            spawnX,
            spawnY
        );

        resetState();
    }

    public void respawnAt(
        float x,
        float y
    ) {
        player.reset(x, y);
        resetState();
    }

    private void resetState() {
        verticalVelocity = 0f;

        jumpsUsed = 0;
        jumpCutAvailable = false;
        onGround = true;
        airDashUsed = false;

        dashTimeRemaining = 0f;
        dashCooldownRemaining = 0f;
        dashDirection = 1;

        wallJumpPushTimeRemaining = 0f;
        wallJumpDirection = 1;

        knockbackTimeRemaining = 0f;
        knockbackDirection = 1;
    }

    public void prepareForDeath() {
        player.getPosition().set(
            spawnX,
            spawnY
        );

        verticalVelocity = 0f;

        jumpsUsed = 0;
        jumpCutAvailable = false;
        onGround = true;
        airDashUsed = false;

        dashTimeRemaining = 0f;
        wallJumpPushTimeRemaining = 0f;

        knockbackTimeRemaining = 0f;
    }

    public void updateDashCooldown(
        float delta
    ) {
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

    public void startDash(
        PlayerInput input
    ) {
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

    public boolean updateDash(
        float delta,
        PlayerBody playerBody,
        float drawWidth,
        float drawHeight
    ) {
        float previousX =
            player.getPosition().x;

        player.getPosition().x +=
            dashDirection
                * DASH_SPEED
                * delta;

        playerBody.update(
            player,
            drawWidth,
            drawHeight
        );

        boolean hitWall =
            platformWorld.resolveHorizontal(
                player,
                playerBody,
                previousX,
                dashDirection,
                drawWidth,
                drawHeight
            );

        dashTimeRemaining -= delta;

        if (
            hitWall
                || dashTimeRemaining <= 0f
        ) {
            dashTimeRemaining = 0f;
            return true;
        }

        return false;
    }

    public void stopDash() {
        dashTimeRemaining = 0f;
    }

    public void finishDash(
        PlayerInput input
    ) {
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

    public boolean handleJumpInput(
        PlayerInput input,
        PlayerBody playerBody,
        float drawWidth,
        float drawHeight
    ) {
        if (!input.isJumpPressed()) {
            return false;
        }

        int heldWallSide =
            platformWorld.getHeldWallSide(
                playerBody.getBounds(),
                input
            );

        if (
            !onGround
                && verticalVelocity <= 0f
                && heldWallSide != 0
        ) {
            startWallJump(
                heldWallSide,
                playerBody,
                drawWidth,
                drawHeight
            );

            return true;
        }

        if (onGround) {
            onGround = false;
            jumpsUsed = 1;
            airDashUsed = false;
            jumpCutAvailable = true;

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

        if (jumpsUsed < 2) {
            jumpsUsed = 2;
            jumpCutAvailable = true;

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

    public void applyJumpCutoff( PlayerInput input ) {
        if (!jumpCutAvailable) {
            return;
        }

        /*
         * Dash pauses the vertical movement. The jump
         * cutoff will be checked after the dash finishes.
         */
        if (isDashing()) {
            return;
        }

        /*
         * Once the Knight reaches the top of the jump,
         * there is no remaining upward velocity to cut.
         */
        if (verticalVelocity <= 0f) {
            jumpCutAvailable = false;
            return;
        }

        if (input.isJumpHeld()) {
            return;
        }

        /*
         * The key was released while travelling upward.
         * Immediately remove most of the remaining
         * upward velocity.
         */
        verticalVelocity *=
            JUMP_CUTOFF_MULTIPLIER;

        jumpCutAvailable = false;
    }

    public void applyKnockback(
        int direction
    ) {
        knockbackDirection =
            direction >= 0 ? 1 : -1;

        knockbackTimeRemaining =
            KNOCKBACK_DURATION;

        dashTimeRemaining = 0f;
        wallJumpPushTimeRemaining = 0f;

        onGround = false;
        jumpCutAvailable = false;

        verticalVelocity =
            KNOCKBACK_VERTICAL_SPEED;

        /*
         * Face the enemy while moving away from it.
         */
        player.setFacingRight(
            knockbackDirection < 0
        );

        player.setMovementState(
            PlayerMovementState.AIRBORNE
        );

        player.setAnimation(
            PlayerAnimationType.IDLE_HURT
        );
    }

    public boolean isKnockbackActive() {
        return knockbackTimeRemaining > 0f;
    }

    public void updateKnockback(
        float delta,
        PlayerBody playerBody,
        float drawWidth,
        float drawHeight
    ) {
        if (!isKnockbackActive()) {
            return;
        }

        float previousX =
            player.getPosition().x;

        player.getPosition().x +=
            knockbackDirection
                * KNOCKBACK_HORIZONTAL_SPEED
                * delta;

        playerBody.update(
            player,
            drawWidth,
            drawHeight
        );

        boolean hitWall =
            platformWorld.resolveHorizontal(
                player,
                playerBody,
                previousX,
                knockbackDirection,
                drawWidth,
                drawHeight
            );

        knockbackTimeRemaining -= delta;

        if (
            hitWall
                || knockbackTimeRemaining < 0f
        ) {
            knockbackTimeRemaining = 0f;
        }
    }

    private void startWallJump(
        int wallSide,
        PlayerBody playerBody,
        float drawWidth,
        float drawHeight
    ) {
        wallJumpDirection = -wallSide;

        wallJumpPushTimeRemaining =
            WALL_JUMP_PUSH_DURATION;

        verticalVelocity =
            WALL_JUMP_VERTICAL_SPEED;

        jumpCutAvailable = true;

        onGround = false;
        jumpsUsed = 1;

        player.getPosition().x +=
            wallJumpDirection
                * WALL_JUMP_DETACH_DISTANCE;

        playerBody.update(
            player,
            drawWidth,
            drawHeight
        );

        platformWorld.clampPlayerInsideWorld(
            player,
            playerBody,
            drawWidth,
            drawHeight
        );

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
        PlayerBody playerBody,
        float drawWidth,
        float drawHeight
    ) {
        float previousX =
            player.getPosition().x;

        player.getPosition().x +=
            wallJumpDirection
                * WALL_JUMP_HORIZONTAL_SPEED
                * delta;

        playerBody.update(
            player,
            drawWidth,
            drawHeight
        );

        platformWorld.resolveHorizontal(
            player,
            playerBody,
            previousX,
            wallJumpDirection,
            drawWidth,
            drawHeight
        );

        wallJumpPushTimeRemaining -= delta;

        if (wallJumpPushTimeRemaining < 0f) {
            wallJumpPushTimeRemaining = 0f;
        }
    }

    public void updateHorizontalMovement(
        float delta,
        PlayerInput input,
        PlayerBody playerBody,
        float drawWidth,
        float drawHeight
    ) {
        int direction =
            input.getHorizontalDirection();

        if (direction == 0) {
            return;
        }

        float previousX =
            player.getPosition().x;

        player.getPosition().x +=
            direction
                * MOVE_SPEED
                * delta;

        playerBody.update(
            player,
            drawWidth,
            drawHeight
        );

        platformWorld.resolveHorizontal(
            player,
            playerBody,
            previousX,
            direction,
            drawWidth,
            drawHeight
        );

        player.setFacingRight(
            direction > 0
        );
    }

    public void updateVerticalMovement(
        float delta,
        boolean wallSliding,
        boolean movementLocked,
        PlayerBody playerBody,
        float drawWidth,
        float drawHeight
    ) {
        if (onGround) {
            if (
                platformWorld.hasGroundSupport(
                    playerBody.getBounds()
                )
            ) {
                verticalVelocity = 0f;
                return;
            }

            /*
             * The Knight walked off a platform.
             */
            onGround = false;
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

        float previousBottom =
            playerBody.getBounds().y;

        float previousTop =
            playerBody.getBounds().y
                + playerBody.getBounds().height;

        player.getPosition().y +=
            verticalVelocity * delta;

        playerBody.update(
            player,
            drawWidth,
            drawHeight
        );

        PlatformWorld.VerticalCollision collision =
            platformWorld.resolveVertical(
                player,
                playerBody,
                previousBottom,
                previousTop,
                verticalVelocity,
                drawWidth,
                drawHeight
            );

        if (
            collision
                == PlatformWorld
                .VerticalCollision.LANDED
        ) {
            verticalVelocity = 0f;

            jumpsUsed = 0;
            onGround = true;
            airDashUsed = false;
            jumpCutAvailable = false;

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

        if (
            collision
                == PlatformWorld
                .VerticalCollision.HIT_CEILING
        ) {
            verticalVelocity = 0f;
            jumpCutAvailable = false;
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
        PlayerBody playerBody
    ) {
        if (
            onGround
                || verticalVelocity > 0f
        ) {
            return false;
        }

        return platformWorld.getHeldWallSide(
            playerBody.getBounds(),
            input
        ) != 0;
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

        PlayerAnimationType current =
            player.getAnimationType();

        if (wallSliding) {
            player.setAnimation(
                PlayerAnimationType.WALL_SLIDE
            );

            return;
        }

        if (!onGround) {
            if (
                current
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
                current
                    != PlayerAnimationType.AIRBORNE
                    && current
                    != PlayerAnimationType.DOUBLE_JUMP
            ) {
                player.setAnimation(
                    PlayerAnimationType.AIRBORNE
                );
            }

            return;
        }

        if (
            input.getHorizontalDirection()
                != 0
        ) {
            player.setAnimation(
                PlayerAnimationType.RUN
            );

            return;
        }

        if (
            current
                == PlayerAnimationType.LANDING
                || current
                == PlayerAnimationType.RUN_TO_IDLE
        ) {
            return;
        }

        if (
            current == PlayerAnimationType.RUN
                || current
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

    public void pogoBounce() {
        onGround = false;

        verticalVelocity =
            JUMP_SPEED;

        jumpsUsed = 1;
        jumpCutAvailable = false;

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

    public boolean isOnGround() {
        return onGround;
    }

    public float getVerticalVelocity() {
        return verticalVelocity;
    }
}
