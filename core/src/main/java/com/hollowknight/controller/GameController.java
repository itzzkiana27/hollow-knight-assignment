package com.hollowknight.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.hollowknight.HollowKnightGame;
import com.hollowknight.model.GameSettings;
import com.hollowknight.model.combat.AttackDirection;
import com.hollowknight.model.combat.PlayerCombat;
import com.hollowknight.model.combat.PracticeTarget;
import com.hollowknight.model.input.KeyBindings;
import com.hollowknight.model.input.PlayerInput;
import com.hollowknight.model.player.Player;
import com.hollowknight.model.player.PlayerAnimationType;
import com.hollowknight.model.player.PlayerMovement;
import com.hollowknight.model.player.PlayerMovementState;

import java.util.EnumSet;

public class GameController {

    private static final float SPAWN_X = 180f;
    private static final float GROUND_Y = 100f;

    private static final EnumSet<PlayerAnimationType>
        LOCKING_NON_COMBAT_ANIMATIONS = EnumSet.of(
        PlayerAnimationType.DASH,

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
    private final PlayerMovement movement;
    private final PlayerCombat combat;
    private final PracticeTarget practiceTarget;

    private final KeyBindings keyBindings;

    private PlayerInput currentInput;

    public GameController(
        HollowKnightGame game
    ) {
        this.game = game;

        player = new Player(
            SPAWN_X,
            GROUND_Y
        );

        movement = new PlayerMovement(
            player,
            SPAWN_X,
            GROUND_Y
        );

        combat = new PlayerCombat();
        practiceTarget =
            new PracticeTarget();

        GameSettings settings =
            game.getSettings();

        keyBindings = new KeyBindings(
            settings.getMoveLeftKey(),
            settings.getMoveRightKey(),
            settings.getJumpKey(),
            settings.getDashKey(),
            settings.getAttackKey()
        );

        currentInput =
            PlayerInput.empty();
    }

    public void update(
        float delta,
        float worldWidth,
        float knightDrawWidth,
        float knightDrawHeight
    ) {
        currentInput =
            readPlayerInput();

        float safeDelta = Math.min(
            delta,
            1f / 30f
        );

        practiceTarget.update(
            safeDelta,
            worldWidth,
            GROUND_Y
        );

        movement.updateDashCooldown(
            safeDelta
        );

        if (player.isDead()) {
            updateDeadPlayer(delta);
            return;
        }

        handleFocusRelease();

        float maximumX =
            movement.getMaximumX(
                worldWidth,
                knightDrawWidth
            );

        if (movement.isDashing()) {
            updateDash(
                safeDelta,
                maximumX
            );

            player.updateAnimationTime(
                delta
            );

            return;
        }

        handleActionInput();

        if (player.isDead()) {
            player.updateAnimationTime(
                delta
            );

            return;
        }

        combat.update(
            safeDelta,
            player,
            knightDrawWidth,
            knightDrawHeight
        );

        resolvePracticeTargetHit();

        if (movement.isDashing()) {
            updateDash(
                safeDelta,
                maximumX
            );

            player.updateAnimationTime(
                delta
            );

            return;
        }

        if (
            movement.isWallJumpPushActive()
        ) {
            movement.updateWallJumpPush(
                safeDelta,
                maximumX
            );

            movement.updateVerticalMovement(
                safeDelta,
                false,
                isMovementLocked()
            );

            player.updateAnimationTime(
                delta
            );

            return;
        }

        boolean movementLocked =
            isMovementLocked();

        boolean startedWallJump = false;

        if (!movementLocked) {
            startedWallJump =
                movement.handleJumpInput(
                    currentInput,
                    maximumX
                );
        }

        if (startedWallJump) {
            movement.updateWallJumpPush(
                safeDelta,
                maximumX
            );

            movement.updateVerticalMovement(
                safeDelta,
                false,
                isMovementLocked()
            );

            player.updateAnimationTime(
                delta
            );

            return;
        }

        if (!movementLocked) {
            movement.updateHorizontalMovement(
                safeDelta,
                currentInput,
                maximumX
            );
        }

        boolean wallSliding =
            !movementLocked
                && movement.shouldWallSlide(
                currentInput,
                maximumX
            );

        movement.updateVerticalMovement(
            safeDelta,
            wallSliding,
            movementLocked
        );

        movement.updateMovementAnimation(
            currentInput,
            movementLocked,
            wallSliding
        );

        player.updateAnimationTime(delta);
    }

    private PlayerInput readPlayerInput() {
        return new PlayerInput(
            Gdx.input.isKeyPressed(
                keyBindings.getMoveLeft()
            ),

            Gdx.input.isKeyPressed(
                keyBindings.getMoveRight()
            ),

            Gdx.input.isKeyJustPressed(
                keyBindings.getJump()
            ),

            Gdx.input.isKeyJustPressed(
                keyBindings.getDash()
            ),

            Gdx.input.isKeyJustPressed(
                keyBindings.getAttack()
            ),

            Gdx.input.isKeyJustPressed(
                keyBindings.getAlternateAttack()
            ),

            Gdx.input.isKeyPressed(
                keyBindings.getUp()
            ),

            Gdx.input.isKeyPressed(
                keyBindings.getDown()
            ),

            Gdx.input.isKeyPressed(
                keyBindings.getFocus()
            ),

            Gdx.input.isKeyJustPressed(
                keyBindings.getFocus()
            ),

            Gdx.input.isKeyJustPressed(
                keyBindings.getHurtTest()
            ),

            Gdx.input.isKeyJustPressed(
                keyBindings.getSoulGainTest()
            ),

            Gdx.input.isKeyJustPressed(
                keyBindings.getFireballTest()
            ),

            Gdx.input.isKeyJustPressed(
                keyBindings.getScreamTest()
            ),

            Gdx.input.isKeyJustPressed(
                keyBindings.getDeathTest()
            ),

            Gdx.input.isKeyJustPressed(
                keyBindings.getRevive()
            )
        );
    }

    private void updateDash(
        float delta,
        float maximumX
    ) {
        boolean finished =
            movement.updateDash(
                delta,
                maximumX
            );

        if (finished) {
            movement.finishDash(
                currentInput
            );
        }
    }

    private void updateDeadPlayer(
        float delta
    ) {
        if (
            currentInput.isRevivePressed()
        ) {
            combat.finishAttack();
            movement.resetPlayer();
        }

        player.updateAnimationTime(delta);
    }

    private void handleFocusRelease() {
        if (
            player.getAnimationType()
                == PlayerAnimationType.FOCUS
                && !currentInput.isFocusHeld()
        ) {
            player.setAnimation(
                PlayerAnimationType.FOCUS_END
            );
        }
    }

    private void handleActionInput() {
        /*
         * Temporary death-animation test.
         */
        if (currentInput.isDeathPressed()) {
            combat.finishAttack();

            player.setDead(true);

            player.setMovementState(
                PlayerMovementState.DEAD
            );

            player.setAnimation(
                PlayerAnimationType.DEATH
            );

            movement.prepareForDeath();

            return;
        }

        if (isMovementLocked()) {
            return;
        }

        if (
            currentInput.isDashPressed()
                && movement.canStartDash()
        ) {
            movement.startDash(
                currentInput
            );

            return;
        }

        if (currentInput.isHurtPressed()) {
            player.setAnimation(
                PlayerAnimationType.IDLE_HURT
            );

            return;
        }

        if (
            currentInput.isSoulGainPressed()
        ) {
            player.setAnimation(
                PlayerAnimationType.FOCUS_GET
            );

            return;
        }

        if (
            currentInput.isFireballPressed()
        ) {
            player.setAnimation(
                PlayerAnimationType.FIREBALL_CAST
            );

            return;
        }

        if (
            currentInput.isScreamPressed()
        ) {
            player.setAnimation(
                PlayerAnimationType.SCREAM
            );

            return;
        }

        if (
            movement.isOnGround()
                && currentInput.isFocusPressed()
        ) {
            player.setAnimation(
                PlayerAnimationType.FOCUS_START
            );

            return;
        }

        combat.tryStartAttack(
            currentInput,
            player
        );
    }

    private void resolvePracticeTargetHit() {
        if (!combat.canRegisterHit()) {
            return;
        }

        if (
            !combat.getAttackHitbox().overlaps(
                practiceTarget.getBounds()
            )
        ) {
            return;
        }

        combat.registerHit();
        practiceTarget.hit();

        if (
            combat.getAttackDirection()
                == AttackDirection.DOWN
                && !movement.isOnGround()
        ) {
            applySuccessfulPogo();
        }
    }

    private boolean isMovementLocked() {
        return combat.isAttacking()
            || LOCKING_NON_COMBAT_ANIMATIONS
            .contains(
                player.getAnimationType()
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
                    currentInput.isFocusHeld()
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
                movement.stopDash();

                movement.finishDash(
                    currentInput
                );
            }

            case SLASH,
                 SLASH_ALT,
                 UP_SLASH,
                 DOWN_SLASH -> {
                combat.finishAttack();

                movement
                    .selectAnimationAfterAction(
                        currentInput
                    );
            }

            case DEATH -> {
                /*
                 * Keep the final death frame.
                 */
            }

            case AIRBORNE,
                 DOUBLE_JUMP,
                 WALL_JUMP -> {
                if (movement.isOnGround()) {
                    player.setAnimation(
                        PlayerAnimationType.LANDING
                    );
                } else if (
                    movement
                        .getVerticalVelocity()
                        <= 0f
                ) {
                    player.setAnimation(
                        PlayerAnimationType.FALL
                    );
                }
            }

            default ->
                movement
                    .selectAnimationAfterAction(
                        currentInput
                    );
        }
    }

    /*
     * Later, real enemies and spikes can call this
     * method after a successful downward hit.
     */
    public void applySuccessfulPogo() {
        combat.finishAttack();
        movement.pogoBounce();
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isAttackHitboxActive() {
        return combat.isHitboxActive();
    }

    public Rectangle getAttackHitbox() {
        return combat.getAttackHitbox();
    }

    public Rectangle getPracticeTargetBounds() {
        return practiceTarget.getBounds();
    }

    public boolean isPracticeTargetFlashing() {
        return practiceTarget.isFlashing();
    }

    public int getPracticeTargetHitCount() {
        return practiceTarget.getHitCount();
    }

    public String text(String key) {
        return game
            .getLocalization()
            .get(key);
    }

    public void returnToMainMenu() {
        game.showMainMenu();
    }
}
