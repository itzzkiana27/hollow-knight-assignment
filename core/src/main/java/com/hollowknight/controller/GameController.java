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

    /*
     * The current dash animation contains 12 frames,
     * each lasting 0.045 seconds:
     *
     * 12 × 0.045 = 0.54 seconds
     */
    private static final float DASH_DURATION = 0.54f;
    private static final float DASH_SPEED = 390f;
    private static final float DASH_COOLDOWN = 0.30f;

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
    }

    public void update(
        float delta,
        float worldWidth,
        float knightDrawWidth
    ) {
        /*
         * Prevent a large physics jump if the window
         * temporarily freezes or is resized.
         */
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
         * While dashing, normal movement, jumping,
         * actions, and gravity are temporarily paused.
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
         * handleActionInput() may have just started
         * a new dash.
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

        boolean movementLocked =
            isMovementLocked();

        int horizontalDirection =
            movementLocked
                ? 0
                : getHorizontalDirection();

        if (!movementLocked) {
            handleJumpInput();
        }

        updateHorizontalMovement(
            safeDelta,
            horizontalDirection,
            worldWidth,
            knightDrawWidth
        );

        updateVerticalMovement(safeDelta);

        updateMovementAnimation(
            horizontalDirection
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

            onGround = true;
            jumpsUsed = 0;

            dashTimeRemaining = 0f;

            return;
        }

        /*
         * Do not begin another action while an action
         * animation is already running.
         */
        if (
            ACTION_ANIMATIONS.contains(
                player.getAnimationType()
            )
        ) {
            return;
        }

        /*
         * Dash can be performed on the ground or
         * in the air.
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

        /*
         * Focusing currently starts only while
         * standing on the floor.
         */
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

        /*
         * A or D decides the dash direction.
         *
         * When neither is held, dash toward the
         * direction the Knight is already facing.
         */
        if (requestedDirection == 0) {
            requestedDirection =
                player.isFacingRight()
                    ? 1
                    : -1;
        }

        dashDirection = requestedDirection;
        dashTimeRemaining = DASH_DURATION;

        /*
         * This includes the dash duration plus a
         * short recovery period after the dash.
         */
        dashCooldownRemaining =
            DASH_DURATION + DASH_COOLDOWN;

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
        player.getPosition().x +=
            dashDirection
                * DASH_SPEED
                * delta;

        float maximumX = Math.max(
            0f,
            worldWidth - knightDrawWidth
        );

        player.getPosition().x =
            MathUtils.clamp(
                player.getPosition().x,
                0f,
                maximumX
            );

        /*
         * We intentionally do not update the vertical
         * position here. This briefly pauses gravity.
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

    private void handleJumpInput() {
        if (
            !Gdx.input.isKeyJustPressed(
                Input.Keys.SPACE
            )
        ) {
            return;
        }

        /*
         * First jump.
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

            return;
        }

        /*
         * Second jump.
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
    }

    private void updateHorizontalMovement(
        float delta,
        int horizontalDirection,
        float worldWidth,
        float knightDrawWidth
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

        float maximumX = Math.max(
            0f,
            worldWidth - knightDrawWidth
        );

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
        float delta
    ) {
        if (onGround) {
            player.getPosition().y =
                GROUND_Y;

            verticalVelocity = 0f;
            return;
        }

        verticalVelocity +=
            GRAVITY * delta;

        verticalVelocity = Math.max(
            verticalVelocity,
            MAX_FALL_SPEED
        );

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

        if (verticalVelocity > 0f) {
            player.setMovementState(
                PlayerMovementState.AIRBORNE
            );
        } else {
            player.setMovementState(
                PlayerMovementState.FALLING
            );
        }
    }

    private void updateMovementAnimation(
        int horizontalDirection
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
         * Air animations.
         */
        if (!onGround) {
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
         * Movement may interrupt landing.
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
         * Wait for these ground transitions to finish.
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
                 * Keep the final death frame visible.
                 * Enter resets the player.
                 */
            }

            case AIRBORNE,
                 DOUBLE_JUMP,
                 WALL_JUMP -> {
                /*
                 * Keep the final upward frame until
                 * the Knight begins falling.
                 */
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
