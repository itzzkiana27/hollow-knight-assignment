package com.hollowknight.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;
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
import com.hollowknight.model.npc.Zote;
import com.hollowknight.model.combat.Knockbackable;
import com.hollowknight.model.boss.FalseKnight;
import com.hollowknight.model.charm.CharmEffects;
import com.hollowknight.model.charm.CharmInventory;
import com.hollowknight.model.charm.CharmType;

import java.util.EnumSet;

public class GameController {

    private static final float
        POGO_SPIKE_GRACE_DURATION = 0.12f;

    private static final float
       CHARM_OBTAINED_MESSAGE_DURATION = 3f;

    private static final float
        CHECKPOINT_HAZARD_MARGIN = 110f;

    private static final float
        VOID_SHADE_SOUL_DURATION = 0.72f;

    private static final float
        VOID_SHADE_SOUL_SPEED = 720f;

    private static final float
        VOID_SHADE_SOUL_WIDTH = 190f;

    private static final float
        VOID_SHADE_SOUL_HEIGHT = 82f;

    private static final int
        SHADE_SOUL_BASE_DAMAGE = 2;

    private static final float
        VOID_ABYSS_SHRIEK_DURATION = 0.48f;

    private static final float
        VOID_ABYSS_SHRIEK_WIDTH = 250f;

    private static final float
        VOID_ABYSS_SHRIEK_HEIGHT = 285f;

    private static final int
        ABYSS_SHRIEK_BASE_DAMAGE = 3;

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

    // ===== ZOTE MAIN DIALOGUE LINES - EDIT  =====.
    private static final String[] ZOTE_MAIN_DIALOGUE = {
        "Halt, noble knight, and mark my words well.",
        "Beyond yonder pass rides a monarch with a heart of stone, cruel in judgment and colder than winter’s iron",
        "Many brave souls have bent the knee before him, yet found no mercy beneath his crown.",
        "Turn thy blade with wisdom, for valor alone shall not soften such a tyrant’s heart."
    };

    // ===== PRECEPTS OF ZOTE - DYNAMIC DIALOGUE =====
    private static final String[] ZOTE_PRECEPTS = {
        "Precept One: 'Always Win Your Battles'. Losing a battle earns you nothing and teaches you nothing. Win your battles, or don't engage in them at all!",

        "Precept Two: 'Never Let Them Laugh at You'. Fools laugh at everything, even at their superiors. But beware, laughter isn't harmless! Laughter spreads like a disease, and soon everyone is laughing at you. You need to strike at the source of this perverse merriment quickly to stop it from spreading.",

        "Precept Three: 'Always Be Rested'. Fighting and adventuring take their toll on your body. When you rest, your body strengthens and repairs itself. The longer you rest, the stronger you become.",

        "Precept Four: 'Forget Your Past'. The past is painful, and thinking about your past can only bring you misery. Think about something else instead, such as the future, or some food.",

        "Precept Five: 'Strength Beats Strength'. Is your opponent strong? No matter! Simply overcome their strength with even more strength, and they'll soon be defeated."
    };

    private final HollowKnightGame game;

    private final TiledWorld world;
    private final float spawnX;
    private final float spawnY;
    private final Vector2 returnFromCitySpawn;

    private String currentRoomId;
    private Rectangle currentRoomBounds;
    private float currentRoomSpawnX;
    private float currentRoomSpawnY;

    private final Player player;
    private final PlayerBody playerBody;
    private final PlayerMovement movement;
    private final PlayerCombat combat;

    private final PlayerHealth health;
    private final PlayerSoul soul;
    private final PlayerFocus focus;

    private final CharmInventory charmInventory;
    private final CharmEffects charmEffects;

    private final PlayerCheckpoint checkpoint;
    private final PlatformWorld platformWorld;

    private HuskHornhead huskHornhead;
    private Crawlid crawlid;
    private CrystalGuardian crystalGuardian;
    private WingedSentry wingedSentry;
    private Zote zote;
    private FalseKnight falseKnight;

    private Array<SpikeHazard> spikeHazards;

    private final CrackedWall crackedWall;
    private final Platform crackedWallPlatform;

    private final Rectangle checkpointDangerZone;
    private final Rectangle sharpShadowDashVisualBounds;
    private final Rectangle voidShadeSoulBounds;
    private final Rectangle voidAbyssShriekBounds;
    private final KeyBindings keyBindings;

    private final Sound[] zoteVoiceSounds;
    private final Sound zoteBattleAttackSound;
    private Sound falseKnightSwingSound;
    private Sound falseKnightStrikeGroundSound;
    private Sound falseKnightJumpSound;
    private Sound falseKnightLandSound;
    private Sound falseKnightDamageArmourSound;
    private Sound falseKnightDamageArmourFinalSound;
    private Sound falseKnightHeadDamageSound;
    private Sound falseKnightRollSound;

    private boolean zoteDialogueActive;
    private boolean zoteMainDialogueFinished;
    private boolean zoteUsingMainDialogue;
    private boolean falseKnightMaceHitConsumed;
    private boolean falseKnightShockwaveHitConsumed;

    private boolean charmInventoryOpen;
    private boolean charmEquipFailed;

    private boolean sharpShadowHitHuskHornhead;
    private boolean sharpShadowHitCrawlid;
    private boolean sharpShadowHitCrystalGuardian;
    private boolean sharpShadowHitWingedSentry;
    private boolean sharpShadowHitFalseKnight;

    private boolean voidShadeSoulActive;
    private boolean voidShadeSoulFacingRight;
    private float voidShadeSoulTimeRemaining;
    private boolean shadeSoulHitHuskHornhead;
    private boolean shadeSoulHitCrawlid;
    private boolean shadeSoulHitCrystalGuardian;
    private boolean shadeSoulHitWingedSentry;
    private boolean shadeSoulHitFalseKnight;

    private boolean voidAbyssShriekActive;
    private float voidAbyssShriekTimeRemaining;
    private boolean abyssShriekHitHuskHornhead;
    private boolean abyssShriekHitCrawlid;
    private boolean abyssShriekHitCrystalGuardian;
    private boolean abyssShriekHitWingedSentry;
    private boolean abyssShriekHitFalseKnight;


    private String charmInventoryMessage;
    private String charmObtainedMessage;
   private float charmObtainedMessageTimeRemaining;

    private String[] activeZoteDialogueLines;
    private int zoteDialogueLineIndex;

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

        currentRoomId = "forgotten_crossroads";
        currentRoomBounds = world.getRoomBounds(
            currentRoomId
        );
        currentRoomSpawnX = spawnX;
        currentRoomSpawnY = spawnY;

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
        } else {
            crackedWallPlatform = null;
        }

        configureCurrentRoomPhysics();

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

        charmInventory =
            new CharmInventory();

        charmEffects =
            new CharmEffects(
                charmInventory
            );

        charmInventoryOpen = false;
        charmEquipFailed = false;
        charmInventoryMessage = "";
        charmObtainedMessage = "";
        charmObtainedMessageTimeRemaining = 0f;

        resetSharpShadowDashHits();
        resetVoidShadeSoulHits();
        resetVoidAbyssShriekHits();

        voidShadeSoulActive = false;
        voidShadeSoulFacingRight = true;
        voidShadeSoulTimeRemaining = 0f;

        voidAbyssShriekActive = false;
        voidAbyssShriekTimeRemaining = 0f;

        checkpoint =
            new PlayerCheckpoint(
                spawnX,
                spawnY
            );

        checkpointDangerZone =
            new Rectangle();

        sharpShadowDashVisualBounds =
            new Rectangle();

        voidShadeSoulBounds =
            new Rectangle();

        voidAbyssShriekBounds =
            new Rectangle();

        spawnEnemiesForCurrentRoom();
        spawnZoteForCurrentRoom();
        spawnFalseKnightForCurrentRoom();

        spikeHazards =
            world.getSpikeHazardsForRoom(
                currentRoomId
            );

        GameSettings settings =
            game.getSettings();

        keyBindings = new KeyBindings(
            settings.getMoveLeftKey(),
            settings.getMoveRightKey(),
            settings.getJumpKey(),
            settings.getDashKey(),
            settings.getAttackKey()
        );

        zoteVoiceSounds = new Sound[] {
            Gdx.audio.newSound(
                Gdx.files.internal(
                    "audio/npc/zote/voice/Zote_01.wav"
                )
            ),
            Gdx.audio.newSound(
                Gdx.files.internal(
                    "audio/npc/zote/voice/Zote_02.wav"
                )
            ),
            Gdx.audio.newSound(
                Gdx.files.internal(
                    "audio/npc/zote/voice/Zote_03.wav"
                )
            ),
            Gdx.audio.newSound(
                Gdx.files.internal(
                    "audio/npc/zote/voice/Zote_04.wav"
                )
            ),
            Gdx.audio.newSound(
                Gdx.files.internal(
                    "audio/npc/zote/voice/Zote_05.wav"
                )
            )
        };

        zoteBattleAttackSound =
            Gdx.audio.newSound(
                Gdx.files.internal(
                    "audio/npc/zote/battle/Zote_battle_attack_loop.wav"
                )
            );

        activeZoteDialogueLines =
            new String[0];

        zoteDialogueLineIndex = 0;
        zoteDialogueActive = false;
        zoteMainDialogueFinished = false;
        zoteUsingMainDialogue = false;

        falseKnightSwingSound =
            Gdx.audio.newSound(
                Gdx.files.internal(
                    "audio/bosses/false_knight/swing.wav"
                )
            );

        falseKnightStrikeGroundSound =
            Gdx.audio.newSound(
                Gdx.files.internal(
                    "audio/bosses/false_knight/strike_ground.wav"
                )
            );

        falseKnightJumpSound =
            Gdx.audio.newSound(
                Gdx.files.internal(
                    "audio/bosses/false_knight/jump.wav"
                )
            );

        falseKnightLandSound =
            Gdx.audio.newSound(
                Gdx.files.internal(
                    "audio/bosses/false_knight/land.wav"
                )
            );

        falseKnightDamageArmourSound =
            Gdx.audio.newSound(
                Gdx.files.internal(
                    "audio/bosses/false_knight/damage_armour.wav"
                )
            );

        falseKnightDamageArmourFinalSound =
            Gdx.audio.newSound(
                Gdx.files.internal(
                    "audio/bosses/false_knight/damage_armour_final.wav"
                )
            );

        falseKnightHeadDamageSound =
            Gdx.audio.newSound(
                Gdx.files.internal(
                    "audio/bosses/false_knight/head_damage.wav"
                )
            );

        falseKnightRollSound =
            Gdx.audio.newSound(
                Gdx.files.internal(
                    "audio/bosses/false_knight/roll.wav"
                )
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

        handleCharmInventoryInput();

        if (charmInventoryOpen) {
            player.updateAnimationTime(delta);
            return;
        }

        updateTimers(safeDelta);

        movement.updateDashCooldown(
            getDashCooldownUpdateDelta(
                safeDelta
            )
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

        handleZoteDialogueInput();

        if (zoteDialogueActive) {
            player.updateAnimationTime(delta);
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

        if (charmObtainedMessageTimeRemaining > 0f) {
                       charmObtainedMessageTimeRemaining -= delta;
                          if (charmObtainedMessageTimeRemaining < 0f) {
                               charmObtainedMessageTimeRemaining = 0f;
                           }
                   }

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

        if (falseKnight != null) {
            FalseKnight.State oldState =
                falseKnight.getState();

            falseKnight.update(
                delta,
                playerBody.getBounds(),
                platformWorld
            );

            playFalseKnightStateSound(
                oldState,
                falseKnight.getState()
            );
        }

        updateAbilityEffects(delta);

        if (zote != null) {
            zote.update(
                delta,
                playerBody.getBounds()
            );
        }

        for (SpikeHazard hazard : spikeHazards) {
            hazard.update(delta);
        }

        if (crackedWall != null) {
            crackedWall.update(delta);
        }
    }

    private void playFalseKnightStateSound(
        FalseKnight.State oldState,
        FalseKnight.State newState
    ) {
        if (oldState == newState) {
            return;
        }

        switch (newState) {
            case MACE_SLAM_ANTIC -> {
                if (falseKnightSwingSound != null) {
                    falseKnightSwingSound.play(0.65f);
                }
            }

            case MACE_SLAM, POWER_SLAM -> {
                if (falseKnightStrikeGroundSound != null) {
                    falseKnightStrikeGroundSound.play(0.8f);
                }
            }

            case OFFENSIVE_LEAP,
                 DEFENSIVE_LEAP,
                 POWER_JUMP -> {
                if (falseKnightJumpSound != null) {
                    falseKnightJumpSound.play(0.7f);
                }
            }

            case STUNNED -> {
                if (falseKnightDamageArmourFinalSound != null) {
                    falseKnightDamageArmourFinalSound.play(0.8f);
                }
            }

            case DEAD -> {
                if (falseKnightRollSound != null) {
                    falseKnightRollSound.play(0.75f);
                }
            }

            default -> {
            }
        }
    }


    private void handleCharmInventoryInput() {
        if (
            !Gdx.input.isKeyJustPressed(
                Input.Keys.I
            )
        ) {
            return;
        }

        if (charmInventoryOpen) {
            closeCharmInventory();
            return;
        }

        if (!canOpenCharmInventory()) {
            return;
        }

        openCharmInventory();
    }

    private boolean canOpenCharmInventory() {
        return !player.isDead()
            && !zoteDialogueActive
            && !movement.isKnockbackActive()
            && !movement.isDashing()
            && !movement.isWallJumpPushActive()
            && !isMovementLocked();
    }

    private void openCharmInventory() {
        charmInventoryOpen = true;
        charmEquipFailed = false;
        charmInventoryMessage =
            "Choose charms. Notches: "
                + charmInventory.getUsedNotches()
                + "/"
                + charmInventory.getNotchCapacity();

        combat.finishAttack();
        focus.cancel();
    }

    private void closeCharmInventory() {
        charmInventoryOpen = false;
        charmEquipFailed = false;
        charmInventoryMessage = "";
    }

    public boolean toggleCharmFromInventory(
        CharmType charm
    ) {
        if (charm == null) {
            return false;
        }

        if (!charmInventoryOpen) {
            return false;
        }

        boolean wasEquipped =
            charmInventory.isEquipped(charm);

        boolean changed =
            charmInventory.toggleCharm(charm);

        if (!changed) {
            charmEquipFailed = true;
            charmInventoryMessage =
                "Not enough charm notches.";
            return false;
        }

        charmEquipFailed = false;

        if (wasEquipped) {
            charmInventoryMessage =
                "Unequipped "
                    + charm.getDisplayName()
                    + ". Notches: "
                    + charmInventory.getUsedNotches()
                    + "/"
                    + charmInventory.getNotchCapacity();
        } else {
            charmInventoryMessage =
                "Equipped "
                    + charm.getDisplayName()
                    + ". Notches: "
                    + charmInventory.getUsedNotches()
                    + "/"
                    + charmInventory.getNotchCapacity();
        }

        return true;
    }

    public boolean isCharmInventoryOpen() {
        return charmInventoryOpen;
    }

    public CharmInventory getCharmInventory() {
        return charmInventory;
    }

    public CharmEffects getCharmEffects() {
        return charmEffects;
    }

    public String getCharmInventoryMessage() {
        return charmInventoryMessage;
    }
    public boolean shouldShowCharmObtainedMessage() {
               return charmObtainedMessageTimeRemaining > 0f
                      && charmObtainedMessage != null
                      && !charmObtainedMessage.isBlank();
          }

       public String getCharmObtainedMessage() {
                return charmObtainedMessage;
           }

       public boolean shouldDrawVoidHeartRewardCharm() {
               return crackedWall != null
                       && crackedWall.isDestroyed()
                      && charmInventory.isOwned(
                           CharmType.VOID_HEART
                          );
    }
    public boolean didCharmEquipFail() {
        return charmEquipFailed;
    }

    public boolean isCharmEquipped(
        CharmType charm
    ) {
        return charmInventory.isEquipped(
            charm
        );
    }

    public int getUsedCharmNotches() {
        return charmInventory.getUsedNotches();
    }

    public int getCharmNotchCapacity() {
        return charmInventory.getNotchCapacity();
    }

    private int getNailDamage() {
        return charmEffects.getNailDamage(
            combat.getDamage()
        );
    }

    private void gainSoulFromNailHit() {
        soul.gain(
            charmEffects.getSoulGain(
                PlayerSoul.NAIL_HIT_GAIN
            )
        );
    }

    private float getDashCooldownUpdateDelta(
        float delta
    ) {
        float baseCooldown = 1f;
        float modifiedCooldown =
            charmEffects.getDashCooldown(
                baseCooldown
            );

        if (modifiedCooldown <= 0f) {
            return delta;
        }

        return delta
            * baseCooldown
            / modifiedCooldown;
    }

    private float getFocusUpdateDelta(
        float delta
    ) {
        float baseDuration = 1f;
        float modifiedDuration =
            charmEffects.getFocusDuration(
                baseDuration
            );

        if (modifiedDuration <= 0f) {
            return delta;
        }

        return delta
            * baseDuration
            / modifiedDuration;
    }

    public float getAttackCooldownMultiplier() {
        return charmEffects.getAttackCooldown(
            1f
        );
    }

    public float getEnemyKnockbackMultiplier() {
        return charmEffects
            .getKnockbackMultiplier();
    }

    public boolean hasSharpShadow() {
        return charmEffects.hasSharpShadow();
    }

    public boolean shouldUseSharpShadowAnimation() {
        return charmEffects
            .shouldUseSharpShadowAnimation();
    }

    public float getDashLengthMultiplier() {
        return charmEffects
            .getDashLengthMultiplier();
    }

    public boolean hasVoidHeart() {
        return charmEffects.hasVoidHeart();
    }

    public boolean shouldUseVoidAbilityAnimations() {
        return charmEffects
            .shouldUseVoidAbilityAnimations();
    }

    public int getAbilityDamage(
        int baseAbilityDamage
    ) {
        return charmEffects.getAbilityDamage(
            baseAbilityDamage
        );
    }

    public boolean isSharpShadowDashVisualActive() {
        return shouldUseSharpShadowAnimation()
            && movement.isDashing();
    }

    public Rectangle getSharpShadowDashVisualBounds() {
        Rectangle playerBounds =
            playerBody.getBounds();

        sharpShadowDashVisualBounds.set(
            playerBounds.x - 54f,
            playerBounds.y - 28f,
            playerBounds.width + 108f,
            playerBounds.height + 56f
        );

        return new Rectangle(
            sharpShadowDashVisualBounds
        );
    }

    public boolean isVoidShadeSoulActive() {
        return voidShadeSoulActive;
    }

    public Rectangle getVoidShadeSoulBounds() {
        return new Rectangle(
            voidShadeSoulBounds
        );
    }

    public boolean isVoidShadeSoulFacingRight() {
        return voidShadeSoulFacingRight;
    }

    public float getVoidShadeSoulProgress() {
        if (!voidShadeSoulActive) {
            return 0f;
        }

        return Math.max(
            0f,
            Math.min(
                1f,
                voidShadeSoulTimeRemaining
                    / VOID_SHADE_SOUL_DURATION
            )
        );
    }

    public boolean isVoidAbyssShriekActive() {
        return voidAbyssShriekActive;
    }

    public Rectangle getVoidAbyssShriekBounds() {
        return new Rectangle(
            voidAbyssShriekBounds
        );
    }

    public float getVoidAbyssShriekProgress() {
        if (!voidAbyssShriekActive) {
            return 0f;
        }

        return Math.max(
            0f,
            Math.min(
                1f,
                voidAbyssShriekTimeRemaining
                    / VOID_ABYSS_SHRIEK_DURATION
            )
        );
    }

    private void startShadeSoulAbility() {
        player.setAnimation(
            PlayerAnimationType.FIREBALL_CAST
        );

        if (!shouldUseVoidAbilityAnimations()) {
            return;
        }

        Rectangle playerBounds =
            playerBody.getBounds();

        voidShadeSoulFacingRight =
            player.isFacingRight();

        float startX = voidShadeSoulFacingRight
            ? playerBounds.x + playerBounds.width - 10f
            : playerBounds.x - VOID_SHADE_SOUL_WIDTH + 10f;

        float startY =
            playerBounds.y
                + playerBounds.height * 0.52f
                - VOID_SHADE_SOUL_HEIGHT / 2f;

        voidShadeSoulBounds.set(
            startX,
            startY,
            VOID_SHADE_SOUL_WIDTH,
            VOID_SHADE_SOUL_HEIGHT
        );

        voidShadeSoulActive = true;
        voidShadeSoulTimeRemaining =
            VOID_SHADE_SOUL_DURATION;

        resetVoidShadeSoulHits();
        applyVoidShadeSoulDamage();
    }

    private void startAbyssShriekAbility() {
        player.setAnimation(
            PlayerAnimationType.SCREAM
        );

        if (!shouldUseVoidAbilityAnimations()) {
            return;
        }

        positionVoidAbyssShriekBounds();

        voidAbyssShriekActive = true;
        voidAbyssShriekTimeRemaining =
            VOID_ABYSS_SHRIEK_DURATION;

        resetVoidAbyssShriekHits();
        applyVoidAbyssShriekDamage();
    }

    private void updateAbilityEffects(
        float delta
    ) {
        updateVoidShadeSoul(
            delta
        );

        updateVoidAbyssShriek(
            delta
        );
    }

    private void updateVoidShadeSoul(
        float delta
    ) {
        if (!voidShadeSoulActive) {
            return;
        }

        voidShadeSoulTimeRemaining -= delta;

        float direction = voidShadeSoulFacingRight
            ? 1f
            : -1f;

        voidShadeSoulBounds.x +=
            direction
                * VOID_SHADE_SOUL_SPEED
                * delta;

        applyVoidShadeSoulDamage();

        if (
            voidShadeSoulTimeRemaining <= 0f
                || voidShadeSoulBounds.x
                > currentRoomBounds.x
                + currentRoomBounds.width
                + VOID_SHADE_SOUL_WIDTH
                || voidShadeSoulBounds.x
                + voidShadeSoulBounds.width
                < currentRoomBounds.x
                - VOID_SHADE_SOUL_WIDTH
        ) {
            voidShadeSoulActive = false;
            voidShadeSoulTimeRemaining = 0f;
        }
    }

    private void updateVoidAbyssShriek(
        float delta
    ) {
        if (!voidAbyssShriekActive) {
            return;
        }

        voidAbyssShriekTimeRemaining -= delta;

        positionVoidAbyssShriekBounds();
        applyVoidAbyssShriekDamage();

        if (voidAbyssShriekTimeRemaining <= 0f) {
            voidAbyssShriekActive = false;
            voidAbyssShriekTimeRemaining = 0f;
        }
    }

    private void positionVoidAbyssShriekBounds() {
        Rectangle playerBounds =
            playerBody.getBounds();

        float centerX =
            playerBounds.x
                + playerBounds.width / 2f;

        voidAbyssShriekBounds.set(
            centerX - VOID_ABYSS_SHRIEK_WIDTH / 2f,
            playerBounds.y + 12f,
            VOID_ABYSS_SHRIEK_WIDTH,
            VOID_ABYSS_SHRIEK_HEIGHT
        );
    }

    private void resetVoidShadeSoulHits() {
        shadeSoulHitHuskHornhead = false;
        shadeSoulHitCrawlid = false;
        shadeSoulHitCrystalGuardian = false;
        shadeSoulHitWingedSentry = false;
        shadeSoulHitFalseKnight = false;
    }

    private void resetVoidAbyssShriekHits() {
        abyssShriekHitHuskHornhead = false;
        abyssShriekHitCrawlid = false;
        abyssShriekHitCrystalGuardian = false;
        abyssShriekHitWingedSentry = false;
        abyssShriekHitFalseKnight = false;
    }

    private void applyVoidShadeSoulDamage() {
        int damage = getAbilityDamage(
            SHADE_SOUL_BASE_DAMAGE
        );

        if (
            !shadeSoulHitHuskHornhead
                && huskHornhead != null
                && huskHornhead.isAlive()
                && voidShadeSoulBounds.overlaps(
                huskHornhead.getBounds()
            )
        ) {
            shadeSoulHitHuskHornhead = true;
            huskHornhead.takeDamage(damage);
        }

        if (
            !shadeSoulHitCrawlid
                && crawlid != null
                && crawlid.isAlive()
                && voidShadeSoulBounds.overlaps(
                crawlid.getBounds()
            )
        ) {
            shadeSoulHitCrawlid = true;
            crawlid.takeDamage(damage);
        }

        if (
            !shadeSoulHitCrystalGuardian
                && crystalGuardian != null
                && crystalGuardian.isAlive()
                && voidShadeSoulBounds.overlaps(
                crystalGuardian.getBounds()
            )
        ) {
            shadeSoulHitCrystalGuardian = true;
            crystalGuardian.takeDamage(damage);
        }

        if (
            !shadeSoulHitWingedSentry
                && wingedSentry != null
                && wingedSentry.isAlive()
                && voidShadeSoulBounds.overlaps(
                wingedSentry.getBounds()
            )
        ) {
            shadeSoulHitWingedSentry = true;
            wingedSentry.takeDamage(damage);
        }

        if (!shadeSoulHitFalseKnight) {
            shadeSoulHitFalseKnight =
                applyAbilityDamageToFalseKnight(
                    voidShadeSoulBounds,
                    damage
                );
        }
    }

    private void applyVoidAbyssShriekDamage() {
        int damage = getAbilityDamage(
            ABYSS_SHRIEK_BASE_DAMAGE
        );

        if (
            !abyssShriekHitHuskHornhead
                && huskHornhead != null
                && huskHornhead.isAlive()
                && voidAbyssShriekBounds.overlaps(
                huskHornhead.getBounds()
            )
        ) {
            abyssShriekHitHuskHornhead = true;
            huskHornhead.takeDamage(damage);
        }

        if (
            !abyssShriekHitCrawlid
                && crawlid != null
                && crawlid.isAlive()
                && voidAbyssShriekBounds.overlaps(
                crawlid.getBounds()
            )
        ) {
            abyssShriekHitCrawlid = true;
            crawlid.takeDamage(damage);
        }

        if (
            !abyssShriekHitCrystalGuardian
                && crystalGuardian != null
                && crystalGuardian.isAlive()
                && voidAbyssShriekBounds.overlaps(
                crystalGuardian.getBounds()
            )
        ) {
            abyssShriekHitCrystalGuardian = true;
            crystalGuardian.takeDamage(damage);
        }

        if (
            !abyssShriekHitWingedSentry
                && wingedSentry != null
                && wingedSentry.isAlive()
                && voidAbyssShriekBounds.overlaps(
                wingedSentry.getBounds()
            )
        ) {
            abyssShriekHitWingedSentry = true;
            wingedSentry.takeDamage(damage);
        }

        if (!abyssShriekHitFalseKnight) {
            abyssShriekHitFalseKnight =
                applyAbilityDamageToFalseKnight(
                    voidAbyssShriekBounds,
                    damage
                );
        }
    }

    private boolean applyAbilityDamageToFalseKnight(
        Rectangle abilityBounds,
        int damage
    ) {
        if (
            falseKnight == null
                || !falseKnight.isAlive()
        ) {
            return false;
        }

        boolean hitVulnerableBody =
            falseKnight.isStunned()
                && abilityBounds.overlaps(
                falseKnight.getVulnerableHitbox()
            );

        boolean hitArmour =
            !falseKnight.isStunned()
                && abilityBounds.overlaps(
                falseKnight.getBounds()
            );

        if (!hitVulnerableBody && !hitArmour) {
            return false;
        }

        falseKnight.takeDamage(
            damage,
            hitVulnerableBody
        );

        return true;
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
                getFocusUpdateDelta(
                    delta
                ),
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
            handleRoomTransition(
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
            touchedHazard =
                handleFalseKnightContact();
        }

        if (!touchedHazard) {
            updateSafeCheckpoint();
        }

        player.updateAnimationTime(delta);
    }

    private void configureCurrentRoomPhysics() {
        currentRoomBounds = world.getRoomBounds(
            currentRoomId
        );

        platformWorld.configure(
            currentRoomBounds,
            world.getCollisionPlatformsForRoom(
                currentRoomId
            )
        );

        if (
            crackedWallPlatform != null
                && crackedWall != null
                && !crackedWall.isDestroyed()
                && "forgotten_crossroads"
                .equals(currentRoomId)
        ) {
            platformWorld.addPlatform(
                crackedWallPlatform
            );
        }
    }

    private void spawnEnemiesForCurrentRoom() {
        TiledWorld.EnemySpawn huskSpawn =
            world.findEnemySpawn(
                "HUSK_HORNHEAD",
                currentRoomId
            );

        huskHornhead = huskSpawn == null
            ? null
            : new HuskHornhead(
                huskSpawn.getX(),
                huskSpawn.getY(),
                huskSpawn.isFacingRight()
            );

        TiledWorld.EnemySpawn crawlidSpawn =
            world.findEnemySpawn(
                "CRAWLID",
                currentRoomId
            );

        crawlid = crawlidSpawn == null
            ? null
            : new Crawlid(
                crawlidSpawn.getX(),
                crawlidSpawn.getY(),
                crawlidSpawn.isFacingRight()
            );

        TiledWorld.EnemySpawn crystalSpawn =
            world.findEnemySpawn(
                "CRYSTAL_GUARDIAN",
                currentRoomId
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
                "WINGED_SENTRY",
                currentRoomId
            );

        wingedSentry = wingedSpawn == null
            ? null
            : new WingedSentry(
                wingedSpawn.getX(),
                wingedSpawn.getY(),
                wingedSpawn.isFacingRight()
            );
    }

    private void spawnZoteForCurrentRoom() {
        TiledWorld.NpcSpawn zoteSpawn =
            world.findNpcSpawn(
                "ZOTE",
                currentRoomId
            );

        zote = zoteSpawn == null
            ? null
            : new Zote(
            zoteSpawn.getX(),
            zoteSpawn.getY(),
            zoteSpawn.getWidth(),
            zoteSpawn.getHeight(),
            zoteSpawn.getRoomId(),
            zoteSpawn.isFacingRight(),
            zoteSpawn.getInteractionRadius(),
            zoteSpawn.getMoveMinX(),
            zoteSpawn.getMoveMaxX()
        );
    }

    private void spawnFalseKnightForCurrentRoom() {
        TiledWorld.BossSpawn bossSpawn =
            world.findBossSpawn(
                "FALSE_KNIGHT",
                currentRoomId
            );

        if (bossSpawn == null) {
            falseKnight = null;
            return;
        }

        Rectangle arenaBounds =
            world.getRoomBounds(
                currentRoomId
            );

        float arenaMinX =
            arenaBounds.x;

        float arenaMaxX =
            arenaBounds.x
                + arenaBounds.width;

        float groundY =
            bossSpawn.getY();

        falseKnight =
            new FalseKnight(
                bossSpawn.getX(),
                bossSpawn.getY(),
                bossSpawn.getWidth(),
                bossSpawn.getHeight(),
                arenaMinX,
                arenaMaxX,
                groundY
            );
    }

    private boolean handleRoomTransition(
        float knightDrawWidth,
        float knightDrawHeight
    ) {
        for (
            TiledWorld.RoomTransition transition
            : world.getRoomTransitions()
        ) {
            if (
                !currentRoomId.equals(
                    transition.getFromRoom()
                )
            ) {
                continue;
            }

            if (
                !playerBody
                    .getBounds()
                    .overlaps(
                        transition.getBounds()
                    )
            ) {
                continue;
            }

            enterRoom(
                transition.getTargetRoom(),
                transition.getTargetSpawn(),
                knightDrawWidth,
                knightDrawHeight
            );

            return true;
        }

        return false;
    }

    private void enterRoom(
        String roomId,
        String spawnId,
        float knightDrawWidth,
        float knightDrawHeight
    ) {
        Vector2 spawn = world.getPlayerSpawn(
            spawnId
        );

        if (spawn == null) {
            throw new IllegalStateException(
                "Missing target spawn for room "
                    + roomId
                    + ": "
                    + spawnId
            );
        }

        currentRoomId = roomId;
        currentRoomSpawnX = spawn.x;
        currentRoomSpawnY = spawn.y;

        configureCurrentRoomPhysics();

        spikeHazards =
            world.getSpikeHazardsForRoom(
                currentRoomId
            );

        spawnEnemiesForCurrentRoom();
        spawnZoteForCurrentRoom();
        spawnFalseKnightForCurrentRoom();
        endZoteDialogue();

        combat.finishAttack();
        focus.cancel();

        movement.respawnAt(
            spawn.x,
            spawn.y
        );

        checkpoint.save(
            spawn.x,
            spawn.y
        );

        playerBody.update(
            player,
            knightDrawWidth,
            knightDrawHeight
        );
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
                currentRoomSpawnX,
                currentRoomSpawnY
            );

            movement.respawnAt(
                currentRoomSpawnX,
                currentRoomSpawnY
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

    private boolean isSharpShadowDashActive() {
        return movement.isDashing()
            && charmEffects
            .shouldDashIgnoreEnemyContactDamage();
    }

    private void resetSharpShadowDashHits() {
        sharpShadowHitHuskHornhead = false;
        sharpShadowHitCrawlid = false;
        sharpShadowHitCrystalGuardian = false;
        sharpShadowHitWingedSentry = false;
        sharpShadowHitFalseKnight = false;
    }

    private void applySharpShadowDamageToHuskHornhead() {
        if (
            sharpShadowHitHuskHornhead
                || huskHornhead == null
                || !huskHornhead.isAlive()
        ) {
            return;
        }

        sharpShadowHitHuskHornhead = true;

        huskHornhead.takeDamage(
            charmEffects.getSharpShadowDamage(
                combat.getDamage()
            )
        );
    }

    private void applySharpShadowDamageToCrawlid() {
        if (
            sharpShadowHitCrawlid
                || crawlid == null
                || !crawlid.isAlive()
        ) {
            return;
        }

        sharpShadowHitCrawlid = true;

        crawlid.takeDamage(
            charmEffects.getSharpShadowDamage(
                combat.getDamage()
            )
        );
    }

    private void applySharpShadowDamageToCrystalGuardian() {
        if (
            sharpShadowHitCrystalGuardian
                || crystalGuardian == null
                || !crystalGuardian.isAlive()
        ) {
            return;
        }

        sharpShadowHitCrystalGuardian = true;

        crystalGuardian.takeDamage(
            charmEffects.getSharpShadowDamage(
                combat.getDamage()
            )
        );
    }

    private void applySharpShadowDamageToWingedSentry() {
        if (
            sharpShadowHitWingedSentry
                || wingedSentry == null
                || !wingedSentry.isAlive()
        ) {
            return;
        }

        sharpShadowHitWingedSentry = true;

        wingedSentry.takeDamage(
            charmEffects.getSharpShadowDamage(
                combat.getDamage()
            )
        );
    }

    private void applySharpShadowDamageToFalseKnight(
        Rectangle playerBounds
    ) {
        if (
            sharpShadowHitFalseKnight
                || falseKnight == null
                || !falseKnight.isAlive()
                || !playerBounds.overlaps(
                falseKnight.getBounds()
            )
        ) {
            return;
        }

        sharpShadowHitFalseKnight = true;

        falseKnight.takeDamage(
            charmEffects.getSharpShadowDamage(
                combat.getDamage()
            ),
            falseKnight.isStunned()
        );
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

        if (isSharpShadowDashActive()) {
            applySharpShadowDamageToHuskHornhead();
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

        if (isSharpShadowDashActive()) {
            applySharpShadowDamageToCrawlid();
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

        if (isSharpShadowDashActive()) {
            applySharpShadowDamageToCrystalGuardian();
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

        if (isSharpShadowDashActive()) {
            applySharpShadowDamageToWingedSentry();
            return false;
        }

        return applyEnemyContact(
            wingedSentry.getBounds(),
            wingedSentry.getContactDamage()
        );
    }

    private boolean handleFalseKnightContact() {
        if (
            falseKnight == null
                || !falseKnight.isAlive()
        ) {
            return false;
        }

        Rectangle playerBounds =
            playerBody.getBounds();

        if (isSharpShadowDashActive()) {
            applySharpShadowDamageToFalseKnight(
                playerBounds
            );

            return false;
        }

        if (!falseKnight.isMaceHitActive()) {
            falseKnightMaceHitConsumed = false;
        }

        if (!falseKnight.isShockwaveActive()) {
            falseKnightShockwaveHitConsumed = false;
        }

        /*
         * Shockwave has priority because it deals more damage.
         */
        if (
            falseKnight.isShockwaveActive()
                && !falseKnightShockwaveHitConsumed
                && playerBounds.overlaps(
                falseKnight.getShockwaveHitbox()
            )
        ) {
            falseKnightShockwaveHitConsumed = true;

            return applyEnemyContact(
                falseKnight.getShockwaveHitbox(),
                falseKnight.getShockwaveDamage()
            );
        }

        /*
         * Mace damage.
         */
        if (
            falseKnight.isMaceHitActive()
                && !falseKnightMaceHitConsumed
                && playerBounds.overlaps(
                falseKnight.getMaceHitbox()
            )
        ) {
            falseKnightMaceHitConsumed = true;

            return applyEnemyContact(
                falseKnight.getMaceHitbox(),
                falseKnight.getMaceDamage()
            );
        }

        /*
         * Body contact damage.
         */
        if (
            falseKnight.canApplyBodyContactNow()
                && playerBounds.overlaps(
                falseKnight.getBounds()
            )
        ) {
            falseKnight.registerBodyContact();

            return applyEnemyContact(
                falseKnight.getBounds(),
                falseKnight.getBodyDamage()
            );
        }

        return false;
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
            currentRoomSpawnX,
            currentRoomSpawnY
        );

        movement.respawnAt(
            currentRoomSpawnX,
            currentRoomSpawnY
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

    private void handleZoteDialogueInput() {
        if (zote == null) {
            return;
        }

        if (zoteDialogueActive) {
            if (
                Gdx.input.isKeyJustPressed(
                    Input.Keys.ENTER
                )
            ) {
                advanceZoteDialogue();
            }

            return;
        }

        boolean interactPressed =
            Gdx.input.isKeyJustPressed(
                Input.Keys.E
            )
                || Gdx.input.isKeyJustPressed(
                Input.Keys.UP
            );

        if (
            interactPressed
                && shouldShowZotePrompt()
        ) {
            startZoteDialogue();
        }
    }

    private void startZoteDialogue() {
        if (zote == null || zote.isAngry()) {
            return;
        }

        zoteDialogueActive = true;
        zoteDialogueLineIndex = 0;

        if (!zoteMainDialogueFinished) {
            activeZoteDialogueLines =
                ZOTE_MAIN_DIALOGUE;

            zoteUsingMainDialogue = true;
        } else {
            activeZoteDialogueLines =
                new String[] {
                    ZOTE_PRECEPTS[
                        MathUtils.random(
                            ZOTE_PRECEPTS.length - 1
                        )
                    ]
                };

            zoteUsingMainDialogue = false;
        }

        zote.startTalking();
        playRandomZoteVoice();
    }

    private void advanceZoteDialogue() {
        zoteDialogueLineIndex++;

        if (
            zoteDialogueLineIndex
                >= activeZoteDialogueLines.length
        ) {
            endZoteDialogue();
            return;
        }

        playRandomZoteVoice();
    }

    private void endZoteDialogue() {
        zoteDialogueActive = false;
        zoteDialogueLineIndex = 0;

        if (zoteUsingMainDialogue) {
            zoteMainDialogueFinished = true;
        }

        zoteUsingMainDialogue = false;
        activeZoteDialogueLines =
            new String[0];

        if (zote != null) {
            zote.stopTalking();
        }
    }

    private void playRandomZoteVoice() {
        if (
            zoteVoiceSounds == null
                || zoteVoiceSounds.length == 0
        ) {
            return;
        }

        int index =
            MathUtils.random(
                zoteVoiceSounds.length - 1
            );

        zoteVoiceSounds[index].play(0.65f);
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

            resetSharpShadowDashHits();

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
            startShadeSoulAbility();
            return;
        }

        if (
            currentInput.isScreamPressed()
        ) {
            startAbyssShriekAbility();
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

        if (tryHitZote()) {
            return;
        }

        if (tryHitFalseKnight()) {
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

    private boolean tryHitZote() {
        if (zote == null) {
            return false;
        }

        if (
            !combat
                .getAttackHitbox()
                .overlaps(
                    zote.getBounds()
                )
        ) {
            return false;
        }

        combat.registerHit();

        if (zoteDialogueActive) {
            endZoteDialogue();
        }

        zote.hitByPlayer();

        if (zoteBattleAttackSound != null) {
            zoteBattleAttackSound.play(0.55f);
        }

        return true;
    }

    private boolean tryHitFalseKnight() {
        if (
            falseKnight == null
                || !falseKnight.isAlive()
        ) {
            return false;
        }

        Rectangle targetHitbox;

        boolean hitVulnerableBody =
            falseKnight.isStunned();

        if (hitVulnerableBody) {
            targetHitbox =
                falseKnight.getVulnerableHitbox();
        } else {
            targetHitbox =
                falseKnight.getBounds();
        }

        if (
            !combat
                .getAttackHitbox()
                .overlaps(
                    targetHitbox
                )
        ) {
            return false;
        }

        combat.registerHit();

        falseKnight.takeDamage(
            getNailDamage(),
            hitVulnerableBody
        );

        gainSoulFromNailHit();

        if (
            combat.isDownwardAttack()
                && !movement.isOnGround()
        ) {
            applySuccessfulPogo();
        }

        if (hitVulnerableBody) {
            if (falseKnightHeadDamageSound != null) {
                falseKnightHeadDamageSound.play(0.75f);
            }
        } else {
            if (falseKnightDamageArmourSound != null) {
                falseKnightDamageArmourSound.play(0.65f);
            }
        }

        return true;
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
        ) {
            if (crackedWallPlatform != null) {
                platformWorld.removePlatform(
                    crackedWallPlatform
                );
            }

            unlockVoidHeartReward();
        }

        return true;
    }
    private void unlockVoidHeartReward() {
               charmInventory.unlockCharm(
                        CharmType.VOID_HEART
                        );

               charmInventoryMessage =
                       "Void Heart obtained.";

                   charmObtainedMessage =
                       "Void Heart obtained.";

                    charmObtainedMessageTimeRemaining =
                        CHARM_OBTAINED_MESSAGE_DURATION;
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
            getNailDamage()
        );

        gainSoulFromNailHit();

        if (
            !combat.isDownwardAttack()
                && damageable instanceof Knockbackable
        ) {
            Knockbackable knockbackable =
                (Knockbackable) damageable;

            knockbackable.applyKnockback(
                getEnemyKnockbackDirection(
                    enemyBounds
                ),
                platformWorld
            );
        }

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

    private int getEnemyKnockbackDirection(
        Rectangle enemyBounds
    ) {
        Rectangle playerBounds =
            playerBody.getBounds();

        float playerCenterX =
            playerBounds.x
                + playerBounds.width / 2f;

        float enemyCenterX =
            enemyBounds.x
                + enemyBounds.width / 2f;

        return enemyCenterX >= playerCenterX
            ? 1
            : -1;
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
        return zoteDialogueActive
            || focus.isActive()
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

    public Zote getZote() {
        return zote;
    }

    public FalseKnight getFalseKnight() {
        return falseKnight;
    }

    public boolean shouldShowZotePrompt() {
        return zote != null
            && !zoteDialogueActive
            && !zote.isAngry()
            && currentRoomId.equals(
                zote.getRoomId()
            )
            && zote.isPlayerInInteractionRange(
                playerBody.getBounds()
            );
    }

    public boolean isZoteDialogueActive() {
        return zoteDialogueActive;
    }

    public String getCurrentZoteDialogueLine() {
        if (
            !zoteDialogueActive
                || activeZoteDialogueLines == null
                || activeZoteDialogueLines.length == 0
        ) {
            return "";
        }

        return activeZoteDialogueLines[
            zoteDialogueLineIndex
        ];
    }

    public void respawnEnemiesForRoomEntry() {
        spawnEnemiesForCurrentRoom();
        spawnZoteForCurrentRoom();
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
        return new Rectangle(currentRoomBounds);
    }

    public Rectangle getCurrentCameraBounds() {
        return world.getCameraBoundsForRoom(
            currentRoomId
        );
    }

    public String getCurrentRoomId() {
        return currentRoomId;
    }

    public boolean isCurrentRoom(
        String roomId
    ) {
        return currentRoomId.equals(roomId);
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
        if (zoteVoiceSounds != null) {
            for (Sound sound : zoteVoiceSounds) {
                sound.dispose();
            }
        }

        if (zoteBattleAttackSound != null) {
            zoteBattleAttackSound.dispose();
        }

        if (falseKnightSwingSound != null) {
            falseKnightSwingSound.dispose();
        }

        if (falseKnightStrikeGroundSound != null) {
            falseKnightStrikeGroundSound.dispose();
        }

        if (falseKnightJumpSound != null) {
            falseKnightJumpSound.dispose();
        }

        if (falseKnightLandSound != null) {
            falseKnightLandSound.dispose();
        }

        if (falseKnightDamageArmourSound != null) {
            falseKnightDamageArmourSound.dispose();
        }

        if (falseKnightDamageArmourFinalSound != null) {
            falseKnightDamageArmourFinalSound.dispose();
        }

        if (falseKnightHeadDamageSound != null) {
            falseKnightHeadDamageSound.dispose();
        }

        if (falseKnightRollSound != null) {
            falseKnightRollSound.dispose();
        }

        world.dispose();
    }

    public void returnToMainMenu() {
        game.showMainMenu();
    }
}
