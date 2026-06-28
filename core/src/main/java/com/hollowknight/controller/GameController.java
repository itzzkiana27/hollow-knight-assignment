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
    private static final float SPAWN_Y = 100f;

    private static final float WALK_SPEED = 110f;
    private static final float RUN_SPEED = 230f;

    private static final EnumSet<PlayerAnimationType>
        BLOCKING_ANIMATIONS = EnumSet.of(
        PlayerAnimationType.RUN_TO_IDLE,
        PlayerAnimationType.AIRBORNE,
        PlayerAnimationType.LANDING,
        PlayerAnimationType.DASH,
        PlayerAnimationType.DOUBLE_JUMP,
        PlayerAnimationType.WALL_JUMP,

        PlayerAnimationType.SLASH,
        PlayerAnimationType.SLASH_ALT,
        PlayerAnimationType.UP_SLASH,
        PlayerAnimationType.DOWN_SLASH,

        PlayerAnimationType.FOCUS_START,
        PlayerAnimationType.FOCUS_END,
        PlayerAnimationType.FOCUS_GET,
        PlayerAnimationType.FIREBALL_CAST,
        PlayerAnimationType.SCREAM,

        PlayerAnimationType.IDLE_HURT,
        PlayerAnimationType.DEATH
    );

    private final HollowKnightGame game;
    private final Player player;

    public GameController(HollowKnightGame game) {
        this.game = game;
        player = new Player(SPAWN_X, SPAWN_Y);
    }

    public void update(
        float delta,
        float worldWidth,
        float knightDrawWidth
    ) {
        if (player.isDead()) {
            if (
                Gdx.input.isKeyJustPressed(
                    Input.Keys.ENTER
                )
            ) {
                player.reset(SPAWN_X, SPAWN_Y);
            }

            player.updateAnimationTime(delta);
            return;
        }

        /*
         * Focus is a three-part sequence:
         *
         * FOCUS_START → FOCUS → FOCUS_END
         */
        if (
            player.getAnimationType()
                == PlayerAnimationType.FOCUS
        ) {
            if (
                !Gdx.input.isKeyPressed(
                    Input.Keys.Q
                )
            ) {
                player.setAnimation(
                    PlayerAnimationType.FOCUS_END
                );
            }

            player.updateAnimationTime(delta);
            return;
        }

        /*
         * Do not interrupt animations that must finish.
         */
        if (
            BLOCKING_ANIMATIONS.contains(
                player.getAnimationType()
            )
        ) {
            player.updateAnimationTime(delta);
            return;
        }

        if (startTriggeredAction()) {
            player.updateAnimationTime(delta);
            return;
        }

        handleContinuousAnimation(
            delta,
            worldWidth,
            knightDrawWidth
        );

        player.updateAnimationTime(delta);
    }

    private boolean startTriggeredAction() {
        /*
         * Death
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

            return true;
        }

        /*
         * Hurt
         */
        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.H
            )
        ) {
            player.setAnimation(
                PlayerAnimationType.IDLE_HURT
            );

            return true;
        }

        /*
         * Focus/Soul gain
         */
        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.G
            )
        ) {
            player.setAnimation(
                PlayerAnimationType.FOCUS_GET
            );

            return true;
        }

        /*
         * Fireball spell
         */
        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.E
            )
        ) {
            player.setAnimation(
                PlayerAnimationType.FIREBALL_CAST
            );

            return true;
        }

        /*
         * Scream spell
         */
        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.R
            )
        ) {
            player.setAnimation(
                PlayerAnimationType.SCREAM
            );

            return true;
        }

        /*
         * Start focusing.
         */
        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.Q
            )
        ) {
            player.setAnimation(
                PlayerAnimationType.FOCUS_START
            );

            return true;
        }

        /*
         * Dash
         */
        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.SHIFT_LEFT
            )
        ) {
            updateFacingDirection();

            player.setMovementState(
                PlayerMovementState.DASHING
            );

            player.setAnimation(
                PlayerAnimationType.DASH
            );

            return true;
        }

        /*
         * Double jump test
         */
        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.B
            )
        ) {
            player.setMovementState(
                PlayerMovementState.AIRBORNE
            );

            player.setAnimation(
                PlayerAnimationType.DOUBLE_JUMP
            );

            return true;
        }

        /*
         * Wall jump test
         */
        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.N
            )
        ) {
            player.setMovementState(
                PlayerMovementState.AIRBORNE
            );

            player.setAnimation(
                PlayerAnimationType.WALL_JUMP
            );

            return true;
        }

        /*
         * Landing test
         */
        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.L
            )
        ) {
            player.setMovementState(
                PlayerMovementState.GROUNDED
            );

            player.setAnimation(
                PlayerAnimationType.LANDING
            );

            return true;
        }

        /*
         * Normal jump animation test
         */
        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.SPACE
            )
        ) {
            player.setMovementState(
                PlayerMovementState.AIRBORNE
            );

            player.setAnimation(
                PlayerAnimationType.AIRBORNE
            );

            return true;
        }

        /*
         * Nail attack:
         *
         * W + J = up slash
         * S + J = down slash
         * J     = normal slash
         */
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

            return true;
        }

        /*
         * Alternate slash
         */
        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.K
            )
        ) {
            player.setAnimation(
                PlayerAnimationType.SLASH_ALT
            );

            return true;
        }

        return false;
    }

    private void handleContinuousAnimation(
        float delta,
        float worldWidth,
        float knightDrawWidth
    ) {
        /*
         * Wall-slide preview
         */
        if (
            Gdx.input.isKeyPressed(
                Input.Keys.V
            )
        ) {
            player.setMovementState(
                PlayerMovementState.WALL_SLIDING
            );

            player.setAnimation(
                PlayerAnimationType.WALL_SLIDE
            );

            return;
        }

        /*
         * Fall preview
         */
        if (
            Gdx.input.isKeyPressed(
                Input.Keys.F
            )
        ) {
            player.setMovementState(
                PlayerMovementState.FALLING
            );

            player.setAnimation(
                PlayerAnimationType.FALL
            );

            return;
        }

        int horizontalDirection = getHorizontalDirection();

        if (horizontalDirection != 0) {
            boolean walking =
                Gdx.input.isKeyPressed(
                    Input.Keys.CONTROL_LEFT
                );

            float speed =
                walking
                    ? WALK_SPEED
                    : RUN_SPEED;

            player.getPosition().x +=
                horizontalDirection
                    * speed
                    * delta;

            player.getPosition().x =
                MathUtils.clamp(
                    player.getPosition().x,
                    0f,
                    Math.max(
                        0f,
                        worldWidth - knightDrawWidth
                    )
                );

            player.setFacingRight(
                horizontalDirection > 0
            );

            player.setMovementState(
                PlayerMovementState.GROUNDED
            );

            player.setAnimation(
                walking
                    ? PlayerAnimationType.WALK
                    : PlayerAnimationType.RUN
            );

            return;
        }

        /*
         * Looking animations
         */
        if (
            Gdx.input.isKeyPressed(
                Input.Keys.W
            )
        ) {
            player.setAnimation(
                PlayerAnimationType.LOOK_UP
            );

            return;
        }

        if (
            Gdx.input.isKeyPressed(
                Input.Keys.S
            )
        ) {
            player.setAnimation(
                PlayerAnimationType.LOOK_DOWN
            );

            return;
        }

        /*
         * Play the transition after stopping.
         */
        if (
            player.getAnimationType()
                == PlayerAnimationType.RUN
                || player.getAnimationType()
                == PlayerAnimationType.WALK
        ) {
            player.setAnimation(
                PlayerAnimationType.RUN_TO_IDLE
            );

            return;
        }

        player.setMovementState(
            PlayerMovementState.GROUNDED
        );

        player.setAnimation(
            PlayerAnimationType.IDLE
        );
    }

    private void updateFacingDirection() {
        int direction = getHorizontalDirection();

        if (direction != 0) {
            player.setFacingRight(direction > 0);
        }
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
            case AIRBORNE,
                 DOUBLE_JUMP,
                 WALL_JUMP -> {
                player.setMovementState(
                    PlayerMovementState.FALLING
                );

                player.setAnimation(
                    PlayerAnimationType.FALL
                );
            }

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
                 * Keep the final death frame.
                 * Enter revives the Knight.
                 */
            }

            default -> {
                player.setMovementState(
                    PlayerMovementState.GROUNDED
                );

                player.setAnimation(
                    PlayerAnimationType.IDLE
                );
            }
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
