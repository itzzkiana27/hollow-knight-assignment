package com.hollowknight.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.hollowknight.HollowKnightGame;
import com.hollowknight.model.GameSettings;
import com.hollowknight.model.combat.PlayerCombat;
import com.hollowknight.model.combat.SpikeHazard;
import com.hollowknight.model.enemy.Crawlid;
import com.hollowknight.model.enemy.CrystalGuardian;
import com.hollowknight.model.enemy.HuskHornhead;
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
import com.hollowknight.model.world.Platform;
import com.hollowknight.model.world.PlatformWorld;
import com.hollowknight.model.world.CrackedWall;
import com.hollowknight.model.world.TiledWorld;
import com.hollowknight.model.enemy.WingedSentry;

import java.util.EnumSet;

public class GameController {

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

    private final TiledWorld world;
    private final float spawnX;
    private final float spawnY;
    private final Vector2 returnFromCitySpawn;

    private final Player player;
    private final PlayerBody playerBody;
    private final PlayerMovement movement;
    private final PlayerCombat combat;

    private final PlayerHealth health;
    private final PlayerSoul soul;
    private final PlayerFocus focus;

    private final PlayerCheckpoint checkpoint;
    private final PlatformWorld platformWorld;

    private final HuskHornhead huskHornhead;
    private final Crawlid crawlid;
    private final CrystalGuardian crystalGuardian;
    private final WingedSentry wingedSentry;

    private final Array<SpikeHazard> spikeHazards;

    private final CrackedWall crackedWall;
    private final Platform crackedWallPlatform;

    private final Rectangle checkpointDangerZone;
    private final KeyBindings keyBindings;

    private PlayerInput currentInput;

    private float pogoSpikeGraceTimeRemaining;

    public GameController(
        HollowKnightGame game
    ) {
        this.game = game;

        world = new TiledWorld();

        Vector2 startSpawn =
            world.getCrossroadsStart();

        spawnX = startSpawn.x;
        spawnY = startSpawn.y;

        returnFromCitySpawn = new Vector2(
            world.getCrossroadsReturnFromCity()
        );

        player = new Player(
            spawnX,
            spawnY
        );

        playerBody = new PlayerBody();

        platformWorld =
            new PlatformWorld();

        platformWorld.configure(
            world.getCrossroadsBounds(),
            world.getCollisionPlatforms()
        );

        crackedWall = world.getCrackedWall();

        if (crackedWall != null) {
            Rectangle wallBounds =
                crackedWall.getBounds();

            crackedWallPlatform =
                new Platform(
                    wallBounds.x,
                    wallBounds.y,
                    wallBounds.width,
                    wallBounds.height
                );

            platformWorld.addPlatform(
                crackedWallPlatform
            );
        } else {
            crackedWallPlatform = null;
        }

        movement = new PlayerMovement(
            player,
            spawnX,
            spawnY,
            platformWorld
        );

        combat = new PlayerCombat();

        health = new PlayerHealth();
        soul = new PlayerSoul();
        focus = new PlayerFocus();

        checkpoint =
            new PlayerCheckpoint(
                spawnX,
                spawnY
            );

        checkpointDangerZone =
            new Rectangle();

        TiledWorld.EnemySpawn huskSpawn =
            world.findEnemySpawn(
                "HUSK_HORNHEAD"
            );

        huskHornhead = huskSpawn == null
            ? null
            : new HuskHornhead(
                huskSpawn.getX(),
                huskSpawn.getY(),
                huskSpawn.isFacingRight()
            );

        TiledWorld.EnemySpawn crawlidSpawn =
            world.findEnemySpawn("CRAWLID");

        crawlid = crawlidSpawn == null
            ? null
            : new Crawlid(
                crawlidSpawn.getX(),
                crawlidSpawn.getY(),
                crawlidSpawn.isFacingRight()
            );

        TiledWorld.EnemySpawn crystalSpawn =
            world.findEnemySpawn(
                "CRYSTAL_GUARDIAN"
            );

        crystalGuardian = crystalSpawn == null
            ? null
            : new CrystalGuardian(
                crystalSpawn.getX(),
                crystalSpawn.getY(),
                crystalSpawn.isFacingRight()
            );

        TiledWorld.EnemySpawn wingedSpawn =
            world.findEnemySpawn(
                "WINGED_SENTRY"
            );

        wingedSentry = wingedSpawn == null
            ? null
            : new WingedSentry(
                wingedSpawn.getX(),
                wingedSpawn.getY(),
                wingedSpawn.isFacingRight()
            );

        spikeHazards =
            world.getSpikeHazards();

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
        float knightDrawWidth,
        float knightDrawHeight
    ) {
        currentInput =
            readPlayerInput();

        float safeDelta = Math.min(
            delta,
            1f / 30f
        );

        updateTimers(safeDelta);

        movement.updateDashCooldown(
            safeDelta
        );

        playerBody.update(
            player,
            knightDrawWidth,
            knightDrawHeight
        );

        updateWorldObjects(
            safeDelta
        );

        if (player.isDead()) {
            updateDeadPlayer(delta);
            return;
        }

        movement.applyJumpCutoff(
            currentInput
        );

        updateActiveFocus(safeDelta);

        if (movement.isKnockbackActive()) {
            movement.updateKnockback(
                safeDelta,
                playerBody,
                knightDrawWidth,
                knightDrawHeight
            );

            movement.updateVerticalMovement(
                safeDelta,
                false,
                true,
                playerBody,
                knightDrawWidth,
                knightDrawHeight
            );

            finishGameplayFrame(
                delta,
                knightDrawWidth,
                knightDrawHeight
            );

            return;
        }

        if (movement.isDashing()) {
            updateDash(
                safeDelta,
                knightDrawWidth,
                knightDrawHeight
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
                knightDrawWidth,
                knightDrawHeight
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
                playerBody,
                knightDrawWidth,
                knightDrawHeight
            );

            movement.updateVerticalMovement(
                safeDelta,
                false,
                isMovementLocked(),
                playerBody,
                knightDrawWidth,
                knightDrawHeight
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
                    playerBody,
                    knightDrawWidth,
                    knightDrawHeight
                );
        }

        if (startedWallJump) {
            movement.updateWallJumpPush(
                safeDelta,
                playerBody,
                knightDrawWidth,
                knightDrawHeight
            );

            movement.updateVerticalMovement(
                safeDelta,
                false,
                isMovementLocked(),
                playerBody,
                knightDrawWidth,
                knightDrawHeight
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
                playerBody,
                knightDrawWidth,
                knightDrawHeight
            );
        }

        boolean wallSliding =
            !movementLocked
                && movement.shouldWallSlide(
                currentInput,
                playerBody
            );

        movement.updateVerticalMovement(
            safeDelta,
            wallSliding,
            movementLocked,
            playerBody,
            knightDrawWidth,
            knightDrawHeight
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
            pogoSpikeGraceTimeRemaining <= 0f
        ) {
            return;
        }

        pogoSpikeGraceTimeRemaining -= delta;

        if (
            pogoSpikeGraceTimeRemaining < 0f
        ) {
            pogoSpikeGraceTimeRemaining = 0f;
        }
    }

    private void updateWorldObjects(
        float delta
    ) {
        if (huskHornhead != null) {
            huskHornhead.update(
                delta,
                playerBody.getBounds(),
                !player.isDead(),
                platformWorld
            );
        }

        if (crawlid != null) {
            crawlid.update(
                delta,
                playerBody.getBounds(),
                platformWorld
            );
        }

        if (crystalGuardian != null) {
            crystalGuardian.update(
                delta,
                playerBody.getBounds(),
                !player.isDead(),
                platformWorld
            );
        }

        if (wingedSentry != null) {
            wingedSentry.update(
                delta,
                playerBody.getBounds(),
                !player.isDead(),
                platformWorld
            );
        }

        for (SpikeHazard hazard : spikeHazards) {
            hazard.update(delta);
        }

        if (crackedWall != null) {
            crackedWall.update(delta);
        }
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
                || currentInput.isJumpPressed()
                || currentInput.isDashPressed()
                || currentInput.isAttackPressed()
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

            return;
        }

        if (
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

        if (
            handleUnbuiltCityTransition(
                knightDrawWidth,
                knightDrawHeight
            )
        ) {
            player.updateAnimationTime(delta);
            return;
        }

        boolean touchedHazard =
            handleSpikeContact(
                knightDrawWidth,
                knightDrawHeight
            );

        if (!touchedHazard) {
            touchedHazard =
                handleCrystalLaserContact();
        }

        if (!touchedHazard) {
            touchedHazard =
                handleHuskHornheadContact();
        }

        if (!touchedHazard) {
            touchedHazard =
                handleCrawlidContact();
        }

        if (!touchedHazard) {
            touchedHazard =
                handleCrystalGuardianContact();
        }

        if (!touchedHazard) {
            touchedHazard =
                handleWingedSentryContact();
        }

        if (!touchedHazard) {
            updateSafeCheckpoint();
        }

        player.updateAnimationTime(delta);
    }

    /**
     * City of Tears has not been built yet. Until it is, reaching the
     * transition returns the player to the safe Crossroads-side spawn.
     */
    private boolean handleUnbuiltCityTransition(
        float knightDrawWidth,
        float knightDrawHeight
    ) {
        Rectangle transition =
            world.getCityTransitionBounds();

        if (
            transition == null
                || !playerBody
                .getBounds()
                .overlaps(transition)
        ) {
            return false;
        }

        combat.finishAttack();
        focus.cancel();

        movement.respawnAt(
            returnFromCitySpawn.x,
            returnFromCitySpawn.y
        );

        checkpoint.save(
            returnFromCitySpawn.x,
            returnFromCitySpawn.y
        );

        playerBody.update(
            player,
            knightDrawWidth,
            knightDrawHeight
        );

        return true;
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

        SpikeHazard touchedHazard = null;

        for (SpikeHazard hazard : spikeHazards) {
            if (
                playerBody
                    .getBounds()
                    .overlaps(
                        hazard.getBounds()
                    )
            ) {
                touchedHazard = hazard;
                break;
            }
        }

        if (touchedHazard == null) {
            return false;
        }

        PlayerHealth.DamageResult result =
            health.takeDamage(
                touchedHazard.getDamage()
            );

        combat.finishAttack();
        focus.cancel();

        if (
            result
                == PlayerHealth
                .DamageResult.DEFEATED
        ) {
            resetPlayerAfterDefeat();
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

        if (overlapsAnySpike()) {
            checkpoint.save(
                spawnX,
                spawnY
            );

            movement.respawnAt(
                spawnX,
                spawnY
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

    private boolean overlapsAnySpike() {
        for (SpikeHazard hazard : spikeHazards) {
            if (
                playerBody
                    .getBounds()
                    .overlaps(hazard.getBounds())
            ) {
                return true;
            }
        }

        return false;
    }

    private boolean handleCrystalLaserContact() {
        if (
            crystalGuardian == null
                || !crystalGuardian.isLaserActive()
        ) {
            return false;
        }

        if (
            !playerBody
                .getBounds()
                .overlaps(
                    crystalGuardian
                        .getLaserBounds()
                )
        ) {
            return false;
        }

        PlayerHealth.DamageResult result =
            health.takeDamage(
                crystalGuardian
                    .getLaserDamage()
            );

        if (
            result
                == PlayerHealth
                .DamageResult.IGNORED
        ) {
            return true;
        }

        combat.finishAttack();
        focus.cancel();

        if (
            result
                == PlayerHealth
                .DamageResult.DEFEATED
        ) {
            resetPlayerAfterDefeat();
            return true;
        }

        movement.applyKnockback(
            crystalGuardian
                .getLaserDirection()
        );

        return true;
    }

    private boolean
    handleHuskHornheadContact() {
        if (
            huskHornhead == null
                || !huskHornhead.isAlive()
        ) {
            return false;
        }

        if (
            !playerBody
                .getBounds()
                .overlaps(
                    huskHornhead.getBounds()
                )
        ) {
            return false;
        }

        return applyEnemyContact(
            huskHornhead.getBounds(),
            huskHornhead.getContactDamage()
        );
    }

    private boolean handleCrawlidContact() {
        if (
            crawlid == null
                || !crawlid.isAlive()
        ) {
            return false;
        }

        if (
            !playerBody
                .getBounds()
                .overlaps(
                    crawlid.getBounds()
                )
        ) {
            return false;
        }

        return applyEnemyContact(
            crawlid.getBounds(),
            crawlid.getContactDamage()
        );
    }

    private boolean
    handleCrystalGuardianContact() {
        if (
            crystalGuardian == null
                || !crystalGuardian.isAlive()
        ) {
            return false;
        }

        if (
            !playerBody
                .getBounds()
                .overlaps(
                    crystalGuardian.getBounds()
                )
        ) {
            return false;
        }

        return applyEnemyContact(
            crystalGuardian.getBounds(),
            crystalGuardian.getContactDamage()
        );
    }

    private boolean
    handleWingedSentryContact() {
        if (
            wingedSentry == null
                || !wingedSentry.isAlive()
        ) {
            return false;
        }

        if (
            !playerBody
                .getBounds()
                .overlaps(
                    wingedSentry.getBounds()
                )
        ) {
            return false;
        }

        return applyEnemyContact(
            wingedSentry.getBounds(),
            wingedSentry.getContactDamage()
        );
    }

    private boolean applyEnemyContact(
        Rectangle enemyBounds,
        int damage
    ) {
        PlayerHealth.DamageResult result =
            health.takeDamage(damage);

        if (
            result
                == PlayerHealth
                .DamageResult.IGNORED
        ) {
            return true;
        }

        combat.finishAttack();
        focus.cancel();

        if (
            result
                == PlayerHealth
                .DamageResult.DEFEATED
        ) {
            resetPlayerAfterDefeat();
            return true;
        }

        float playerCenterX =
            playerBody.getBounds().x
                + playerBody
                .getBounds().width / 2f;

        float enemyCenterX =
            enemyBounds.x
                + enemyBounds.width / 2f;

        int knockbackDirection =
            playerCenterX < enemyCenterX
                ? -1
                : 1;

        movement.applyKnockback(
            knockbackDirection
        );

        return true;
    }

    private void resetPlayerAfterDefeat() {
        health.restoreFullHealth();

        checkpoint.save(
            spawnX,
            spawnY
        );

        movement.respawnAt(
            spawnX,
            spawnY
        );

        player.setAnimation(
            PlayerAnimationType.IDLE_HURT
        );
    }

    private void updateSafeCheckpoint() {
        if (!movement.isOnGround()) {
            return;
        }

        for (SpikeHazard hazard : spikeHazards) {
            Rectangle spikes =
                hazard.getBounds();

            checkpointDangerZone.set(
                spikes.x
                    - CHECKPOINT_HAZARD_MARGIN,
                spikes.y,
                spikes.width
                    + CHECKPOINT_HAZARD_MARGIN * 2f,
                spikes.height + 30f
            );

            if (
                playerBody
                    .getBounds()
                    .overlaps(
                        checkpointDangerZone
                    )
            ) {
                return;
            }
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
            Gdx.input.isKeyPressed(
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
        float knightDrawWidth,
        float knightDrawHeight
    ) {
        boolean finished =
            movement.updateDash(
                delta,
                playerBody,
                knightDrawWidth,
                knightDrawHeight
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
                    PlayerAnimationType.FOCUS_START
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

        if (tryHitHuskHornhead()) {
            return;
        }

        if (tryHitCrawlid()) {
            return;
        }

        if (tryHitCrystalGuardian()) {
            return;
        }

        if (tryHitWingedSentry()) {
            return;
        }

        if (tryHitCrackedWall()) {
            return;
        }

        tryPogoSpike();
    }

    private boolean tryHitHuskHornhead() {
        if (
            huskHornhead == null
                || !huskHornhead.isAlive()
        ) {
            return false;
        }

        return applyNailHit(
            huskHornhead.getBounds(),
            huskHornhead,
            huskHornhead
        );
    }

    private boolean tryHitCrawlid() {
        if (
            crawlid == null
                || !crawlid.isAlive()
        ) {
            return false;
        }

        return applyNailHit(
            crawlid.getBounds(),
            crawlid,
            crawlid
        );
    }

    private boolean
    tryHitCrystalGuardian() {
        if (
            crystalGuardian == null
                || !crystalGuardian.isAlive()
        ) {
            return false;
        }

        return applyNailHit(
            crystalGuardian.getBounds(),
            crystalGuardian,
            crystalGuardian
        );
    }

    private boolean tryHitWingedSentry() {
        if (
            wingedSentry == null
                || !wingedSentry.isAlive()
        ) {
            return false;
        }

        return applyNailHit(
            wingedSentry.getBounds(),
            wingedSentry,
            wingedSentry
        );
    }

    private boolean tryHitCrackedWall() {
        if (
            crackedWall == null
                || crackedWall.isDestroyed()
                || !combat
                .getAttackHitbox()
                .overlaps(
                    crackedWall.getBounds()
                )
        ) {
            return false;
        }

        combat.registerHit();

        CrackedWall.HitResult result =
            crackedWall.hit();

        if (
            result
                == CrackedWall.HitResult.DESTROYED
                && crackedWallPlatform != null
        ) {
            platformWorld.removePlatform(
                crackedWallPlatform
            );
        }

        return true;
    }

    private boolean applyNailHit(
        Rectangle enemyBounds,
        com.hollowknight.model.combat.Damageable
            damageable,
        com.hollowknight.model.combat.Pogoable
            pogoable
    ) {
        if (
            !combat
                .getAttackHitbox()
                .overlaps(enemyBounds)
        ) {
            return false;
        }

        boolean canPogo =
            pogoable.canBePogoed();

        combat.registerHit();

        damageable.takeDamage(
            combat.getDamage()
        );

        soul.gainFromNailHit();

        if (
            combat.isDownwardAttack()
                && !movement.isOnGround()
                && canPogo
        ) {
            pogoable.onPogo();
            applySuccessfulPogo();
        }

        return true;
    }

    private boolean tryPogoSpike() {
        if (
            !combat.isDownwardAttack()
                || movement.isOnGround()
        ) {
            return false;
        }

        for (SpikeHazard hazard : spikeHazards) {
            if (
                !hazard.canBePogoed()
                    || !combat
                    .getAttackHitbox()
                    .overlaps(
                        hazard.getPogoBounds()
                    )
            ) {
                continue;
            }

            combat.registerHit();
            hazard.onPogo();
            applySuccessfulPogo();

            return true;
        }

        return false;
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
                // Keep final death frame.
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

    public void applySuccessfulPogo() {
        combat.finishAttack();
        movement.pogoBounce();

        pogoSpikeGraceTimeRemaining =
            POGO_SPIKE_GRACE_DURATION;
    }

    public Player getPlayer() {
        return player;
    }

    public Array<Platform> getPlatforms() {
        return platformWorld.getPlatforms();
    }

    public HuskHornhead getHuskHornhead() {
        return huskHornhead;
    }

    public Crawlid getCrawlid() {
        return crawlid;
    }

    public CrystalGuardian getCrystalGuardian() {
        return crystalGuardian;
    }

    public WingedSentry getWingedSentry() {
        return wingedSentry;
    }

    public void respawnEnemiesForRoomEntry() {
        if (huskHornhead != null) {
            huskHornhead.respawnForRoomEntry();
        }

        if (crawlid != null) {
            crawlid.respawnForRoomEntry();
        }

        if (crystalGuardian != null) {
            crystalGuardian.respawnForRoomEntry();
        }

        if (wingedSentry != null) {
            wingedSentry.respawnForRoomEntry();
        }
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

    public Array<SpikeHazard> getSpikeHazards() {
        return spikeHazards;
    }

    public TiledWorld getWorld() {
        return world;
    }

    public Rectangle getCurrentRoomBounds() {
        return world.getCrossroadsBounds();
    }

    public CrackedWall getCrackedWall() {
        return crackedWall;
    }

    public String text(String key) {
        return game
            .getLocalization()
            .get(key);
    }

    public void dispose() {
        world.dispose();
    }

    public void returnToMainMenu() {
        game.showMainMenu();
    }
}
