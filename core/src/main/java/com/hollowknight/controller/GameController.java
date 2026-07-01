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
import com.hollowknight.model.player.PlayerHealth;
import com.hollowknight.model.player.PlayerMovement;
import com.hollowknight.model.player.PlayerMovementState;

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
    private final PlayerCheckpoint checkpoint;

    private final PracticeEnemy practiceEnemy;
    private final SpikeHazard spikeHazard;

    private final KeyBindings keyBindings;

    private PlayerInput currentInput;
    private final Rectangle checkpointDangerZone;

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

        checkpoint =
            new PlayerCheckpoint(
                SPAWN_X,
                GROUND_Y
            );

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
        checkpointDangerZone = new Rectangle();
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

        /*
         * Recalculate the body after respawning.
         */
        playerBody.update(
            player,
            knightDrawWidth,
            knightDrawHeight
        );

        /*
         * Older checkpoints may already have been
         * saved too close to the spikes. Fall back to
         * the original spawn point in that case.
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

        /*
         * Do not save checkpoints directly beside the
         * spikes. The Knight's sprite is wider than its
         * physical body, so a safety margin is needed.
         */
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
            movement.isOnGround()
                && currentInput
                .isFocusPressed()
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

        combat.registerHit();

        practiceEnemy.takeDamage(
            combat.getDamage()
        );

        if (
            combat.isDownwardAttack()
                && !movement.isOnGround()
                && practiceEnemy
                .canBePogoed()
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
