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

    public GameController(HollowKnightGame game) {
        this.game = game;

        player = new Player(
            SPAWN_X,
            GROUND_Y
        );

        verticalVelocity = 0f;
        jumpsUsed = 0;
        onGround = true;
    }

    public void update(
        float delta,
        float worldWidth,
        float knightDrawWidth
    ) {
        /*
         * Prevent unusually large physics steps if the
         * window freezes or is being resized.
         */
        float safeDelta = Math.min(
            delta,
            1f / 30f
        );

        if (player.isDead()) {
            updateDeadPlayer(delta);
            return;
        }

        handleFocusRelease();
        handleActionInput();

        if (player.isDead()) {
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
         * Death can interrupt any other animation.
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

            /*
             * Place the Knight on the temporary floor
             * for the death-animation test.
             */
            player.getPosition().y = GROUND_Y;

            verticalVelocity = 0f;
            onGround = true;

            return;
        }

        /*
         * Do not begin another action while an action
         * animation is already playing.
         */
        if (
            ACTION_ANIMATIONS.contains(
                player.getAnimationType()
            )
        ) {
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
         * Focusing currently starts only while standing
         * on the ground.
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

            if (
                !isMovementLocked()
            ) {
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
         * Allow movement to interrupt landing.
         */
        if (horizontalDirection != 0) {
            boolean walking =
                Gdx.input.isKeyPressed(
                    Input.Keys.CONTROL_LEFT
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
                 * Keep the final upward frame until the
                 * Knight begins falling.
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
                player.setAnimation(
                    PlayerAnimationType.AIRBORNE
                );
            } else {
                player.setAnimation(
                    PlayerAnimationType.FALL
                );
            }

            return;
        }

        int horizontalDirection =
            getHorizontalDirection();

        if (horizontalDirection != 0) {
            boolean walking =
                Gdx.input.isKeyPressed(
                    Input.Keys.CONTROL_LEFT
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
