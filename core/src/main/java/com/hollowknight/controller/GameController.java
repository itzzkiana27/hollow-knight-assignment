package com.hollowknight.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.hollowknight.HollowKnightGame;
import com.hollowknight.model.GameSettings;
import com.hollowknight.model.combat.PlayerCombat;
import com.hollowknight.model.combat.PracticeEnemy;
import com.hollowknight.model.combat.SpikeHazard;
import com.hollowknight.model.input.KeyBindings;
import com.hollowknight.model.input.PlayerInput;
import com.hollowknight.model.player.Player;
import com.hollowknight.model.player.PlayerAnimationType;
import com.hollowknight.model.player.PlayerBody;
import com.hollowknight.model.player.PlayerCheckpoint;
import com.hollowknight.model.player.PlayerFocus;
import com.hollowknight.model.player.PlayerHealth;
import com.hollowknight.model.player.PlayerMovement;
import com.hollowknight.model.player.PlayerMovementState;
import com.hollowknight.model.player.PlayerSoul;

import java.util.EnumSet;

public class GameController {

    private static final float SPAWN_X = 20f;
    private static final float GROUND_Y = 100f;

    private static final float
        POGO_SPIKE_GRACE_DURATION = 0.12f;

    private static final float
        CHECKPOINT_HAZARD_MARGIN = 110f;

    private static final EnumSet<PlayerAnimationType>
        LOCKING_NON_COMBAT_ANIMATIONS =
        EnumSet.of(
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
    private final PlayerBody playerBody;

    private final PlayerHealth health;
    private final PlayerSoul soul;
    private final PlayerFocus focus;

    private final PlayerCheckpoint checkpoint;

    private final PracticeEnemy practiceEnemy;
    private final SpikeHazard spikeHazard;

    private final Rectangle checkpointDangerZone;

    private final KeyBindings keyBindings;

    private PlayerInput currentInput;

    private float
        pogoSpikeGraceTimeRemaining;

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
        playerBody = new PlayerBody();

        health = new PlayerHealth();
        soul = new PlayerSoul();
        focus = new PlayerFocus();

        checkpoint =
            new PlayerCheckpoint(
                SPAWN_X,
                GROUND_Y
            );

        checkpointDangerZone =
            new Rectangle();

        practiceEnemy =
            new PracticeEnemy();

        spikeHazard =
            new SpikeHazard();

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

        pogoSpikeGraceTimeRemaining = 0f;
    }

    public void update(
        float delta,
        float worldWidth,
        float knightDrawWidth,
        float knightDrawHeight
    ) {
        currentInput = readPlayerInput();

        float safeDelta = Math.min(
            delta,
            1f / 30f
        );

        updateTimers(safeDelta);

        updateCombatObjects(
            safeDelta,
            worldWidth
        );

        movement.updateDashCooldown(
            safeDelta
        );

        playerBody.update(
            player,
            knightDrawWidth,
            knightDrawHeight
        );

        if (player.isDead()) {
            updateDeadPlayer(delta);
            return;
        }

        updateActiveFocus(safeDelta);

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

            finishGameplayFrame(
                delta,
                knightDrawWidth,
                knightDrawHeight
            );

            return;
        }

        handleActionInput();

        if (player.isDead()) {
            player.updateAnimationTime(delta);
            return;
        }

        combat.update(
            safeDelta,
            playerBody
        );

        resolveCombatHits();

        if (movement.isDashing()) {
            updateDash(
                safeDelta,
                maximumX
            );

            finishGameplayFrame(
                delta,
                knightDrawWidth,
                knightDrawHeight
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

            finishGameplayFrame(
                delta,
                knightDrawWidth,
                knightDrawHeight
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

            finishGameplayFrame(
                delta,
                knightDrawWidth,
                knightDrawHeight
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

        finishGameplayFrame(
            delta,
            knightDrawWidth,
            knightDrawHeight
        );
    }

    private void updateTimers(float delta) {
        health.update(delta);

        if (
            pogoSpikeGraceTimeRemaining > 0f
        ) {
            pogoSpikeGraceTimeRemaining -= delta;

            if (
                pogoSpikeGraceTimeRemaining < 0f
            ) {
                pogoSpikeGraceTimeRemaining = 0f;
            }
        }
    }

    private void updateCombatObjects(
        float delta,
        float worldWidth
    ) {
        practiceEnemy.update(
            delta,
            worldWidth,
            GROUND_Y
        );

        spikeHazard.update(
            delta,
            worldWidth,
            GROUND_Y
        );
    }

    private void updateActiveFocus(
        float delta
    ) {
        if (!focus.isActive()) {
            return;
        }

        boolean cancelRequested =
            currentInput
                .getHorizontalDirection() != 0
                || currentInput
                .isJumpPressed()
                || currentInput
                .isDashPressed()
                || currentInput
                .isAttackPressed()
                || currentInput
                .isAlternateAttackPressed();

        PlayerFocus.UpdateResult result =
            focus.update(
                delta,
                currentInput.isFocusHeld(),
                cancelRequested,
                health,
                soul
            );

        if (
            result
                == PlayerFocus
                .UpdateResult.CANCELLED
        ) {
            if (
                player.getAnimationType()
                    == PlayerAnimationType.FOCUS
                    || player.getAnimationType()
                    == PlayerAnimationType
                    .FOCUS_START
            ) {
                player.setAnimation(
                    PlayerAnimationType.FOCUS_END
                );
            }
        } else if (
            result
                == PlayerFocus
                .UpdateResult.HEALED
        ) {
            player.setAnimation(
                PlayerAnimationType.FOCUS_GET
            );
        }
    }

    private void finishGameplayFrame(
        float delta,
        float knightDrawWidth,
        float knightDrawHeight
    ) {
        playerBody.update(
            player,
            knightDrawWidth,
            knightDrawHeight
        );

        boolean touchedSpike =
            handleSpikeContact(
                knightDrawWidth,
                knightDrawHeight
            );

        if (!touchedSpike) {
            updateSafeCheckpoint();
        }

        player.updateAnimationTime(delta);
    }

    private boolean handleSpikeContact(
        float knightDrawWidth,
        float knightDrawHeight
    ) {
        if (
            pogoSpikeGraceTimeRemaining > 0f
        ) {
            return false;
        }

        if (
            !playerBody.getBounds().overlaps(
                spikeHazard.getBounds()
            )
        ) {
            return false;
        }

        PlayerHealth.DamageResult result =
            health.takeDamage(1);

        combat.finishAttack();
        focus.cancel();

        if (
            result
                == PlayerHealth
                .DamageResult.DEFEATED
        ) {
            health.restoreFullHealth();

            checkpoint.save(
                SPAWN_X,
                GROUND_Y
            );

            movement.respawnAt(
                SPAWN_X,
                GROUND_Y
            );

            player.setAnimation(
                PlayerAnimationType.IDLE_HURT
            );

            return true;
        }

        movement.respawnAt(
            checkpoint.getX(),
            checkpoint.getY()
        );

        playerBody.update(
            player,
            knightDrawWidth,
            knightDrawHeight
        );

        /*
         * Protect against a checkpoint that was saved
         * inside or directly beside the spikes.
         */
        if (
            playerBody.getBounds().overlaps(
                spikeHazard.getBounds()
            )
        ) {
            checkpoint.save(
                SPAWN_X,
                GROUND_Y
            );

            movement.respawnAt(
                SPAWN_X,
                GROUND_Y
            );

            playerBody.update(
                player,
                knightDrawWidth,
                knightDrawHeight
            );
        }

        if (
            result
                == PlayerHealth
                .DamageResult.DAMAGED
        ) {
            player.setAnimation(
                PlayerAnimationType.IDLE_HURT
            );
        }

        return true;
    }

    private void updateSafeCheckpoint() {
        if (!movement.isOnGround()) {
            return;
        }

        Rectangle spikes =
            spikeHazard.getBounds();

        checkpointDangerZone.set(
            spikes.x
                - CHECKPOINT_HAZARD_MARGIN,

            spikes.y,

            spikes.width
                + CHECKPOINT_HAZARD_MARGIN
                * 2f,

            spikes.height + 30f
        );

        if (
            playerBody.getBounds().overlaps(
                checkpointDangerZone
            )
        ) {
            return;
        }

        checkpoint.save(
            player.getPosition().x,
            player.getPosition().y
        );
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
                keyBindings
                    .getAlternateAttack()
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
            focus.cancel();
            movement.resetPlayer();
        }

        player.updateAnimationTime(delta);
    }

    private void handleActionInput() {
        if (currentInput.isDeathPressed()) {
            combat.finishAttack();
            focus.cancel();

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

        /*
         * Temporary Soul test key.
         */
        if (
            currentInput.isSoulGainPressed()
        ) {
            soul.gainFromNailHit();

            player.setAnimation(
                PlayerAnimationType.FOCUS_GET
            );

            return;
        }

        if (
            currentInput.isFireballPressed()
        ) {
            player.setAnimation(
                PlayerAnimationType
                    .FIREBALL_CAST
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
            currentInput.isFocusPressed()
        ) {
            boolean stationary =
                currentInput
                    .getHorizontalDirection() == 0;

            boolean started =
                focus.tryStart(
                    movement.isOnGround(),
                    stationary,
                    health,
                    soul
                );

            if (started) {
                player.setAnimation(
                    PlayerAnimationType
                        .FOCUS_START
                );
            }

            return;
        }

        combat.tryStartAttack(
            currentInput,
            player
        );
    }

    private void resolveCombatHits() {
        if (!combat.canRegisterHit()) {
            return;
        }

        if (tryHitPracticeEnemy()) {
            return;
        }

        tryPogoSpike();
    }

    private boolean tryHitPracticeEnemy() {
        if (!practiceEnemy.isAlive()) {
            return false;
        }

        if (
            !combat.getAttackHitbox()
                .overlaps(
                    practiceEnemy.getBounds()
                )
        ) {
            return false;
        }

        boolean canPogo =
            practiceEnemy.canBePogoed();

        combat.registerHit();

        practiceEnemy.takeDamage(
            combat.getDamage()
        );

        /*
         * Every successful nail hit grants 11 Soul.
         */
        soul.gainFromNailHit();

        if (
            combat.isDownwardAttack()
                && !movement.isOnGround()
                && canPogo
        ) {
            practiceEnemy.onPogo();
            applySuccessfulPogo();
        }

        return true;
    }

    private boolean tryPogoSpike() {
        if (
            !combat.isDownwardAttack()
                || movement.isOnGround()
                || !spikeHazard
                .canBePogoed()
        ) {
            return false;
        }

        if (
            !combat.getAttackHitbox()
                .overlaps(
                    spikeHazard
                        .getPogoBounds()
                )
        ) {
            return false;
        }

        combat.registerHit();
        spikeHazard.onPogo();

        applySuccessfulPogo();

        return true;
    }

    private boolean isMovementLocked() {
        return focus.isActive()
            || combat.isAttacking()
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
                if (focus.isActive()) {
                    player.setAnimation(
                        PlayerAnimationType.FOCUS
                    );
                } else {
                    player.setAnimation(
                        PlayerAnimationType
                            .FOCUS_END
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
                        PlayerAnimationType
                            .LANDING
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

    public void applySuccessfulPogo() {
        combat.finishAttack();
        movement.pogoBounce();

        pogoSpikeGraceTimeRemaining =
            POGO_SPIKE_GRACE_DURATION;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean shouldDrawPlayer() {
        return health.shouldDrawPlayer();
    }

    public int getCurrentMasks() {
        return health.getCurrentMasks();
    }

    public int getMaximumMasks() {
        return health.getMaximumMasks();
    }

    public int getCurrentSoul() {
        return soul.getCurrentSoul();
    }

    public int getMaximumSoul() {
        return soul.getMaximumSoul();
    }

    public float getSoulFillRatio() {
        return soul.getFillRatio();
    }

    public boolean isFocusing() {
        return focus.isActive();
    }

    public float getFocusProgress() {
        return focus.getProgress();
    }

    public boolean isAttackHitboxActive() {
        return combat.isHitboxActive();
    }

    public Rectangle getAttackHitbox() {
        return combat.getAttackHitbox();
    }

    public Rectangle getPracticeEnemyBounds() {
        return practiceEnemy.getBounds();
    }

    public boolean isPracticeEnemyAlive() {
        return practiceEnemy.isAlive();
    }

    public boolean isPracticeEnemyFlashing() {
        return practiceEnemy.isFlashing();
    }

    public int getPracticeEnemyHealth() {
        return practiceEnemy.getHealth();
    }

    public int getPracticeEnemyMaxHealth() {
        return practiceEnemy.getMaxHealth();
    }

    public Rectangle getSpikeBounds() {
        return spikeHazard.getBounds();
    }

    public boolean isSpikeFlashing() {
        return spikeHazard.isFlashing();
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
