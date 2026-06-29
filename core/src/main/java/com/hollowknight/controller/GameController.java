package com.hollowknight.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.hollowknight.HollowKnightGame;
import com.hollowknight.model.player.Player;
import com.hollowknight.model.player.PlayerAnimationType;
import com.hollowknight.model.player.PlayerMovementState;

import java.util.EnumSet;

public class GameController {

    private static final float SPAWN_X = 180f;
    private static final float GROUND_Y = 100f;

    private static final float WALK_SPEED = 110f;
    private static final float RUN_SPEED = 230f;

    private static final float GRAVITY = -1400f;
    private static final float JUMP_SPEED = 560f;
    private static final float DOUBLE_JUMP_SPEED = 520f;
    private static final float MAX_FALL_SPEED = -850f;

    private static final float DASH_DURATION = 0.54f;
    private static final float DASH_SPEED = 390f;
    private static final float DASH_COOLDOWN = 0.30f;

    /*
     * Wall mechanics
     */
    private static final float WALL_CONTACT_TOLERANCE = 3f;
    private static final float WALL_SLIDE_SPEED = -170f;

    private static final float WALL_JUMP_VERTICAL_SPEED = 560f;
    private static final float WALL_JUMP_HORIZONTAL_SPEED = 340f;
    private static final float WALL_JUMP_PUSH_DURATION = 0.18f;
    private static final float WALL_JUMP_DETACH_DISTANCE = 5f;

    private static final EnumSet<PlayerAnimationType>
        ACTION_ANIMATIONS = EnumSet.of(
        PlayerAnimationType.DASH,

        PlayerAnimationType.SLASH,
        PlayerAnimationType.SLASH_ALT,
        PlayerAnimationType.UP_SLASH,
        PlayerAnimationType.DOWN_SLASH,

        PlayerAnimationType.FOCUS_START,
        PlayerAnimationType.FOCUS,
        PlayerAnimationType.FOCUS_END,
        PlayerAnimationType.FOCUS_GET,

        PlayerAnimationType.FIREBALL_CAST,
        PlayerAnimationType.SCREAM,
        PlayerAnimationType.IDLE_HURT,
        PlayerAnimationType.DEATH
    );

    private final HollowKnightGame game;
    private final Player player;

    private float verticalVelocity;

    private int jumpsUsed;
    private boolean onGround;

    private float dashTimeRemaining;
    private float dashCooldownRemaining;
    private int dashDirection;

    private float wallJumpPushTimeRemaining;
    private int wallJumpDirection;

    public GameController(HollowKnightGame game) {
        this.game = game;

        player = new Player(
            SPAWN_X,
            GROUND_Y
        );

        verticalVelocity = 0f;

        jumpsUsed = 0;
        onGround = true;

        dashTimeRemaining = 0f;
        dashCooldownRemaining = 0f;
        dashDirection = 1;

        wallJumpPushTimeRemaining = 0f;
        wallJumpDirection = 1;
    }

    public void update(
        float delta,
        float worldWidth,
        float knightDrawWidth
    ) {
        float safeDelta = Math.min(
            delta,
            1f / 30f
        );

        updateDashCooldown(safeDelta);

        if (player.isDead()) {
            updateDeadPlayer(delta);
            return;
        }

        handleFocusRelease();

        /*
         * Dash temporarily controls all movement.
         */
        if (isDashing()) {
            updateDash(
                safeDelta,
                worldWidth,
                knightDrawWidth
            );

            player.updateAnimationTime(delta);
            return;
        }

        handleActionInput();

        if (player.isDead()) {
            player.updateAnimationTime(delta);
            return;
        }

        /*
         * handleActionInput() may have started a dash.
         */
        if (isDashing()) {
            updateDash(
                safeDelta,
                worldWidth,
                knightDrawWidth
            );

            player.updateAnimationTime(delta);
            return;
        }

        float maximumX = getMaximumX(
            worldWidth,
            knightDrawWidth
        );

        /*
         * During the first part of a wall jump, the
         * Knight is pushed away from the wall and the
         * player cannot immediately reverse direction.
         */
        if (isWallJumpPushActive()) {
            updateWallJumpPush(
                safeDelta,
                maximumX
            );

            updateVerticalMovement(
                safeDelta,
                false
            );

            player.updateAnimationTime(delta);
            return;
        }

        boolean movementLocked =
            isMovementLocked();

        int horizontalDirection =
            movementLocked
                ? 0
                : getHorizontalDirection();

        /*
         * handleJumpInput returns true only when a wall
         * jump has just started.
         */
        boolean startedWallJump = false;

        if (!movementLocked) {
            startedWallJump =
                handleJumpInput(maximumX);
        }

        if (startedWallJump) {
            updateWallJumpPush(
                safeDelta,
                maximumX
            );

            updateVerticalMovement(
                safeDelta,
                false
            );

            player.updateAnimationTime(delta);
            return;
        }

        updateHorizontalMovement(
            safeDelta,
            horizontalDirection,
            maximumX
        );

        boolean wallSliding =
            !movementLocked
                && shouldWallSlide(maximumX);

        updateVerticalMovement(
            safeDelta,
            wallSliding
        );

        updateMovementAnimation(
            horizontalDirection,
            wallSliding
        );

        player.updateAnimationTime(delta);
    }

    private void updateDashCooldown(float delta) {
        if (dashCooldownRemaining <= 0f) {
            return;
        }

        dashCooldownRemaining -= delta;

        if (dashCooldownRemaining < 0f) {
            dashCooldownRemaining = 0f;
        }
    }

    private void updateDeadPlayer(float delta) {
        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ENTER
            )
        ) {
            resetPlayer();
        }

        player.updateAnimationTime(delta);
    }

    private void resetPlayer() {
        player.reset(
            SPAWN_X,
            GROUND_Y
        );

        verticalVelocity = 0f;

        jumpsUsed = 0;
        onGround = true;

        dashTimeRemaining = 0f;
        dashCooldownRemaining = 0f;
        dashDirection = 1;

        wallJumpPushTimeRemaining = 0f;
        wallJumpDirection = 1;
    }

    private void handleFocusRelease() {
        if (
            player.getAnimationType()
                == PlayerAnimationType.FOCUS
                && !Gdx.input.isKeyPressed(
                Input.Keys.Q
            )
        ) {
            player.setAnimation(
                PlayerAnimationType.FOCUS_END
            );
        }
    }

    private void handleActionInput() {
        /*
         * Death can interrupt every other action.
         */
        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.X
            )
        ) {
            player.setDead(true);

            player.setMovementState(
                PlayerMovementState.DEAD
            );

            player.setAnimation(
                PlayerAnimationType.DEATH
            );

            player.getPosition().y =
                GROUND_Y;

            verticalVelocity = 0f;

            jumpsUsed = 0;
            onGround = true;

            dashTimeRemaining = 0f;
            wallJumpPushTimeRemaining = 0f;

            return;
        }

        /*
         * Do not begin another action while an action
         * animation is running.
         */
        if (
            ACTION_ANIMATIONS.contains(
                player.getAnimationType()
            )
        ) {
            return;
        }

        /*
         * Dash works on the ground, in the air, and
         * while wall sliding.
         */
        if (
            isDashKeyJustPressed()
                && dashCooldownRemaining <= 0f
        ) {
            startDash();
            return;
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.H
            )
        ) {
            player.setAnimation(
                PlayerAnimationType.IDLE_HURT
            );

            return;
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.G
            )
        ) {
            player.setAnimation(
                PlayerAnimationType.FOCUS_GET
            );

            return;
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.E
            )
        ) {
            player.setAnimation(
                PlayerAnimationType.FIREBALL_CAST
            );

            return;
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.R
            )
        ) {
            player.setAnimation(
                PlayerAnimationType.SCREAM
            );

            return;
        }

        if (
            onGround
                && Gdx.input.isKeyJustPressed(
                Input.Keys.Q
            )
        ) {
            player.setAnimation(
                PlayerAnimationType.FOCUS_START
            );

            return;
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.J
            )
        ) {
            if (
                Gdx.input.isKeyPressed(
                    Input.Keys.W
                )
            ) {
                player.setAnimation(
                    PlayerAnimationType.UP_SLASH
                );
            } else if (
                Gdx.input.isKeyPressed(
                    Input.Keys.S
                )
            ) {
                player.setAnimation(
                    PlayerAnimationType.DOWN_SLASH
                );
            } else {
                player.setAnimation(
                    PlayerAnimationType.SLASH
                );
            }

            return;
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.K
            )
        ) {
            player.setAnimation(
                PlayerAnimationType.SLASH_ALT
            );
        }
    }

    private boolean isDashKeyJustPressed() {
        return Gdx.input.isKeyJustPressed(
            Input.Keys.SHIFT_LEFT
        ) || Gdx.input.isKeyJustPressed(
            Input.Keys.SHIFT_RIGHT
        );
    }

    private void startDash() {
        int requestedDirection =
            getHorizontalDirection();

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

    private boolean isDashing() {
        return dashTimeRemaining > 0f;
    }

    private void updateDash(
        float delta,
        float worldWidth,
        float knightDrawWidth
    ) {
        float maximumX = getMaximumX(
            worldWidth,
            knightDrawWidth
        );

        player.getPosition().x +=
            dashDirection
                * DASH_SPEED
                * delta;

        player.getPosition().x =
            MathUtils.clamp(
                player.getPosition().x,
                0f,
                maximumX
            );

        /*
         * Gravity is intentionally paused during dash.
         */
        dashTimeRemaining -= delta;

        if (dashTimeRemaining <= 0f) {
            dashTimeRemaining = 0f;
            finishDash();
        }
    }

    private void finishDash() {
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

        selectAnimationAfterAction();
    }

    /**
     * @return true only when this input started a wall
     * jump.
     */
    private boolean handleJumpInput(
        float maximumX
    ) {
        if (
            !Gdx.input.isKeyJustPressed(
                Input.Keys.SPACE
            )
        ) {
            return false;
        }

        /*
         * Wall jump takes priority over normal and
         * double jumps.
         */
        int heldWallSide =
            getHeldWallSide(maximumX);

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
         * Normal ground jump.
         */
        if (onGround) {
            onGround = false;
            jumpsUsed = 1;

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
         * A wall jump refreshes the double jump.
         *
         * Setting this to 1 means the player can still
         * perform one double jump afterward.
         */
        jumpsUsed = 1;

        /*
         * Move slightly away from the wall immediately
         * so the Knight does not remain attached.
         */
        player.getPosition().x +=
            wallJumpDirection
                * WALL_JUMP_DETACH_DISTANCE;

        player.getPosition().x =
            MathUtils.clamp(
                player.getPosition().x,
                0f,
                maximumX
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

    private boolean isWallJumpPushActive() {
        return wallJumpPushTimeRemaining > 0f;
    }

    private void updateWallJumpPush(
        float delta,
        float maximumX
    ) {
        player.getPosition().x +=
            wallJumpDirection
                * WALL_JUMP_HORIZONTAL_SPEED
                * delta;

        player.getPosition().x =
            MathUtils.clamp(
                player.getPosition().x,
                0f,
                maximumX
            );

        wallJumpPushTimeRemaining -= delta;

        if (wallJumpPushTimeRemaining < 0f) {
            wallJumpPushTimeRemaining = 0f;
        }
    }

    private void updateHorizontalMovement(
        float delta,
        int horizontalDirection,
        float maximumX
    ) {
        if (horizontalDirection == 0) {
            return;
        }

        boolean walking =
            Gdx.input.isKeyPressed(
                Input.Keys.CONTROL_LEFT
            ) || Gdx.input.isKeyPressed(
                Input.Keys.CONTROL_RIGHT
            );

        float movementSpeed =
            walking
                ? WALK_SPEED
                : RUN_SPEED;

        player.getPosition().x +=
            horizontalDirection
                * movementSpeed
                * delta;

        player.getPosition().x =
            MathUtils.clamp(
                player.getPosition().x,
                0f,
                maximumX
            );

        player.setFacingRight(
            horizontalDirection > 0
        );
    }

    private void updateVerticalMovement(
        float delta,
        boolean wallSliding
    ) {
        if (onGround) {
            player.getPosition().y =
                GROUND_Y;

            verticalVelocity = 0f;
            return;
        }

        verticalVelocity +=
            GRAVITY * delta;

        if (wallSliding) {
            /*
             * Limit downward velocity while sliding.
             */
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
                <= GROUND_Y
        ) {
            player.getPosition().y =
                GROUND_Y;

            verticalVelocity = 0f;

            jumpsUsed = 0;
            onGround = true;

            wallJumpPushTimeRemaining = 0f;

            player.setMovementState(
                PlayerMovementState.GROUNDED
            );

            if (!isMovementLocked()) {
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

    private boolean shouldWallSlide(
        float maximumX
    ) {
        if (
            onGround
                || verticalVelocity > 0f
        ) {
            return false;
        }

        return getHeldWallSide(maximumX) != 0;
    }

    /**
     * Returns:
     *
     * -1 when touching and holding toward the left wall
     *  1 when touching and holding toward the right wall
     *  0 otherwise
     */
    private int getHeldWallSide(
        float maximumX
    ) {
        int wallSide =
            getTouchingWallSide(maximumX);

        if (
            wallSide == -1
                && Gdx.input.isKeyPressed(
                Input.Keys.A
            )
        ) {
            return -1;
        }

        if (
            wallSide == 1
                && Gdx.input.isKeyPressed(
                Input.Keys.D
            )
        ) {
            return 1;
        }

        return 0;
    }

    /**
     * Returns:
     *
     * -1 for the left wall
     *  1 for the right wall
     *  0 when not touching a wall
     */
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

    private void updateMovementAnimation(
        int horizontalDirection,
        boolean wallSliding
    ) {
        if (
            player.isDead()
                || isMovementLocked()
        ) {
            return;
        }

        PlayerAnimationType currentAnimation =
            player.getAnimationType();

        /*
         * Wall slide has priority over normal fall.
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
            /*
             * Preserve the wall-jump animation while
             * moving upward.
             */
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

        /*
         * Ground movement can interrupt landing.
         */
        if (horizontalDirection != 0) {
            boolean walking =
                Gdx.input.isKeyPressed(
                    Input.Keys.CONTROL_LEFT
                ) || Gdx.input.isKeyPressed(
                    Input.Keys.CONTROL_RIGHT
                );

            player.setAnimation(
                walking
                    ? PlayerAnimationType.WALK
                    : PlayerAnimationType.RUN
            );

            return;
        }

        /*
         * Wait for ground transition animations.
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

    private boolean isMovementLocked() {
        return ACTION_ANIMATIONS.contains(
            player.getAnimationType()
        );
    }

    private int getHorizontalDirection() {
        int direction = 0;

        if (
            Gdx.input.isKeyPressed(
                Input.Keys.A
            )
        ) {
            direction--;
        }

        if (
            Gdx.input.isKeyPressed(
                Input.Keys.D
            )
        ) {
            direction++;
        }

        return direction;
    }

    private float getMaximumX(
        float worldWidth,
        float knightDrawWidth
    ) {
        return Math.max(
            0f,
            worldWidth - knightDrawWidth
        );
    }

    public void onAnimationFinished(
        PlayerAnimationType finishedAnimation
    ) {
        if (
            player.getAnimationType()
                != finishedAnimation
        ) {
            return;
        }

        switch (finishedAnimation) {
            case FOCUS_START -> {
                if (
                    Gdx.input.isKeyPressed(
                        Input.Keys.Q
                    )
                ) {
                    player.setAnimation(
                        PlayerAnimationType.FOCUS
                    );
                } else {
                    player.setAnimation(
                        PlayerAnimationType.FOCUS_END
                    );
                }
            }

            case DASH -> {
                dashTimeRemaining = 0f;
                finishDash();
            }

            case DEATH -> {
                /*
                 * Keep the final death frame.
                 * Enter resets the player.
                 */
            }

            case AIRBORNE,
                 DOUBLE_JUMP,
                 WALL_JUMP -> {
                if (onGround) {
                    player.setAnimation(
                        PlayerAnimationType.LANDING
                    );
                } else if (
                    verticalVelocity <= 0f
                ) {
                    player.setAnimation(
                        PlayerAnimationType.FALL
                    );
                }
            }

            default ->
                selectAnimationAfterAction();
        }
    }

    private void selectAnimationAfterAction() {
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

        int horizontalDirection =
            getHorizontalDirection();

        if (horizontalDirection != 0) {
            boolean walking =
                Gdx.input.isKeyPressed(
                    Input.Keys.CONTROL_LEFT
                ) || Gdx.input.isKeyPressed(
                    Input.Keys.CONTROL_RIGHT
                );

            player.setAnimation(
                walking
                    ? PlayerAnimationType.WALK
                    : PlayerAnimationType.RUN
            );
        } else {
            player.setAnimation(
                PlayerAnimationType.IDLE
            );
        }
    }

    public Player getPlayer() {
        return player;
    }

    public String text(String key) {
        return game.getLocalization().get(key);
    }

    public void returnToMainMenu() {
        game.showMainMenu();
    }
}
