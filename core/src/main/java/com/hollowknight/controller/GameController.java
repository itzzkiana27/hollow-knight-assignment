package com.hollowknight.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.hollowknight.HollowKnightGame;
import com.hollowknight.audio.GameSfxPlayer;
import com.hollowknight.model.GameSettings;
import com.hollowknight.model.combat.AttackDirection;
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
import com.hollowknight.model.achievement.Achievement;
import com.hollowknight.model.achievement.AchievementManager;
import com.hollowknight.model.achievement.AchievementObserver;
import com.hollowknight.model.achievement.AchievementType;
import com.hollowknight.model.save.DatabaseSaveManager;
import com.hollowknight.model.save.GameData;
import com.hollowknight.model.save.SaveManager;
import com.hollowknight.model.EndGameStats;

import java.util.EnumSet;
import java.util.List;

public class GameController {

    private enum EnemyKillType {
        HUSK_HORNHEAD,
        CRAWLID,
        CRYSTAL_GUARDIAN,
        WINGED_SENTRY
    }

    private static final float
        POGO_SPIKE_GRACE_DURATION = 0.12f;

    private static final float
       CHARM_OBTAINED_MESSAGE_DURATION = 3f;

    private static final float
        CHEAT_MESSAGE_DURATION = 2.5f;

    private static final float
        TWO_KILL_ACHIEVEMENT_WINDOW = 10f;

    private static final float
        SPEEDRUN_LIMIT_SECONDS = 600f;

    private static final float
        END_GAME_SCREEN_DELAY_SECONDS = 2.75f;

    private static final float
        NOCLIP_SPEED = 650f;

    private static final float
        CAMERA_SHAKE_PLAYER_DAMAGE = 0.36f;

    private static final float
        CAMERA_SHAKE_ABILITY_CAST = 0.32f;

    private static final float
        CAMERA_SHAKE_BOSS_MOVE = 0.24f;

    private static final float
        CAMERA_SHAKE_HEAVY_BLOW = 0.18f;

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


    private static final int
        ABILITY_SOUL_COST = PlayerSoul.FOCUS_COST;

    private static final float
        SHADE_SOUL_CAST_DURATION = 0.28f;

    private static final float
        SHADE_SOUL_CAST_WIDTH = 118f;

    private static final float
        SHADE_SOUL_CAST_HEIGHT = 88f;

    private static final float
        SHADE_SOUL_END_DURATION = 0.24f;

    private static final float
        SHADE_SOUL_END_WIDTH = 118f;

    private static final float
        SHADE_SOUL_END_HEIGHT = 88f;

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
    private boolean roomTransitionBlockedUntilExit;

    private final Player player;
    private final PlayerBody playerBody;
    private final PlayerMovement movement;
    private final PlayerCombat combat;

    private final PlayerHealth health;
    private final PlayerSoul soul;
    private final PlayerFocus focus;

    private final CharmInventory charmInventory;
    private final CharmEffects charmEffects;

    private final AchievementManager achievementManager;
    private final SaveManager saveManager;
    private final DatabaseSaveManager databaseSaveManager;
    private final int saveSlotNumber;

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
    private final Rectangle shadeSoulCastBounds;
    private final Rectangle voidShadeSoulBounds;
    private final Rectangle shadeSoulEndBounds;
    private final Rectangle voidAbyssShriekBounds;
    private KeyBindings keyBindings;
    private final GameSfxPlayer gameSfxPlayer;

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
    private boolean falseKnightDefeated;
    private boolean endGamePending;
    private boolean endGameRequested;
    private float endGameDelayRemaining;

    private int deathCount;
    private int totalEnemiesKilled;

    private boolean gameplayCameraShakeRequested;
    private float gameplayCameraShakeIntensity;

    private boolean godModeEnabled;
    private boolean noclipModeEnabled;
    private boolean emergencyHealArmed;
    private boolean respawnAfterDeathAnimation;

    private String cheatMessage;
    private float cheatMessageTimeRemaining;

    private float elapsedGameSeconds;
    private int recentEnemyKills;
    private float recentEnemyKillWindowRemaining;
    private final EnumSet<EnemyKillType> killedEnemyTypes;

    private boolean charmInventoryOpen;
    private boolean charmEquipFailed;

    private boolean sharpShadowHitHuskHornhead;
    private boolean sharpShadowHitCrawlid;
    private boolean sharpShadowHitCrystalGuardian;
    private boolean sharpShadowHitWingedSentry;
    private boolean sharpShadowHitFalseKnight;

    private boolean shadeSoulCastActive;
    private float shadeSoulCastTimeRemaining;
    private boolean shadeSoulVoidVariant;

    private boolean voidShadeSoulActive;
    private boolean voidShadeSoulFacingRight;
    private float voidShadeSoulTimeRemaining;
    private boolean shadeSoulEndActive;
    private float shadeSoulEndTimeRemaining;
    private boolean shadeSoulEndVoidVariant;
    private boolean shadeSoulHitHuskHornhead;
    private boolean shadeSoulHitCrawlid;
    private boolean shadeSoulHitCrystalGuardian;
    private boolean shadeSoulHitWingedSentry;
    private boolean shadeSoulHitFalseKnight;

    private boolean voidAbyssShriekActive;
    private float voidAbyssShriekTimeRemaining;
    private boolean abyssShriekVoidVariant;
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
        this(
            game,
            false,
            game.getActiveSaveSlot()
        );
    }

    public GameController(
        HollowKnightGame game,
        boolean loadSavedGame
    ) {
        this(
            game,
            loadSavedGame,
            game.getActiveSaveSlot()
        );
    }

    public GameController(
        HollowKnightGame game,
        boolean loadSavedGame,
        int saveSlotNumber
    ) {
        this.game = game;
        this.saveSlotNumber =
            SaveManager.normalizeSlot(
                saveSlotNumber
            );

        game.setActiveSaveSlot(
            this.saveSlotNumber
        );

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
        roomTransitionBlockedUntilExit = false;

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

        achievementManager =
            game.getAchievementManager();

        saveManager = new SaveManager();
        databaseSaveManager =
            new DatabaseSaveManager();

        killedEnemyTypes =
            EnumSet.noneOf(EnemyKillType.class);

        charmInventoryOpen = false;
        charmEquipFailed = false;
        charmInventoryMessage = "";
        charmObtainedMessage = "";
        charmObtainedMessageTimeRemaining = 0f;

        resetSharpShadowDashHits();
        resetVoidShadeSoulHits();
        resetVoidAbyssShriekHits();

        shadeSoulCastActive = false;
        shadeSoulCastTimeRemaining = 0f;
        shadeSoulVoidVariant = false;

        voidShadeSoulActive = false;
        voidShadeSoulFacingRight = true;
        voidShadeSoulTimeRemaining = 0f;

        shadeSoulEndActive = false;
        shadeSoulEndTimeRemaining = 0f;
        shadeSoulEndVoidVariant = false;

        voidAbyssShriekActive = false;
        voidAbyssShriekTimeRemaining = 0f;
        abyssShriekVoidVariant = false;

        checkpoint =
            new PlayerCheckpoint(
                spawnX,
                spawnY
            );

        checkpointDangerZone =
            new Rectangle();

        sharpShadowDashVisualBounds =
            new Rectangle();

        shadeSoulCastBounds =
            new Rectangle();

        voidShadeSoulBounds =
            new Rectangle();

        shadeSoulEndBounds =
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

        keyBindings = createKeyBindings(settings);

        gameSfxPlayer = new GameSfxPlayer(settings);

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

        falseKnightDefeated = false;
        endGamePending = false;
        endGameRequested = false;
        endGameDelayRemaining = 0f;
        deathCount = 0;
        totalEnemiesKilled = 0;
        gameplayCameraShakeRequested = false;
        gameplayCameraShakeIntensity = 0f;
        godModeEnabled = false;
        noclipModeEnabled = false;
        emergencyHealArmed = false;
        respawnAfterDeathAnimation = false;
        cheatMessage = "";
        cheatMessageTimeRemaining = 0f;
        elapsedGameSeconds = 0f;
        recentEnemyKills = 0;
        recentEnemyKillWindowRemaining = 0f;

        if (loadSavedGame) {
            loadGame();
        }

        updateBackgroundMusic();

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

        handleCheatCodes(
            knightDrawWidth,
            knightDrawHeight
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

        if (noclipModeEnabled) {
            updateNoclipMovement(
                safeDelta,
                knightDrawWidth,
                knightDrawHeight
            );

            finishNoclipFrame(
                delta,
                knightDrawWidth,
                knightDrawHeight
            );

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
        elapsedGameSeconds += delta;

        health.update(delta);

        if (cheatMessageTimeRemaining > 0f) {
            cheatMessageTimeRemaining -= delta;

            if (cheatMessageTimeRemaining <= 0f) {
                cheatMessageTimeRemaining = 0f;
                cheatMessage = "";
            }
        }

        if (recentEnemyKillWindowRemaining > 0f) {
            recentEnemyKillWindowRemaining -= delta;

            if (recentEnemyKillWindowRemaining <= 0f) {
                recentEnemyKillWindowRemaining = 0f;
                recentEnemyKills = 0;
            }
        }

        if (charmObtainedMessageTimeRemaining > 0f) {
                       charmObtainedMessageTimeRemaining -= delta;
                          if (charmObtainedMessageTimeRemaining < 0f) {
                               charmObtainedMessageTimeRemaining = 0f;
                           }
                   }

        updateEndGameDelay(delta);

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

    private void updateEndGameDelay(float delta) {
        if (!endGamePending) {
            return;
        }

        endGameDelayRemaining -= delta;

        if (endGameDelayRemaining > 0f) {
            return;
        }

        endGameDelayRemaining = 0f;
        endGamePending = false;
        endGameRequested = true;
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

            requestBossMoveShake(
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

    private void requestBossMoveShake(
        FalseKnight.State oldState,
        FalseKnight.State newState
    ) {
        if (oldState == newState) {
            return;
        }

        if (
            newState == FalseKnight.State.IDLE
                || newState == FalseKnight.State.DEAD
        ) {
            return;
        }

        requestGameplayCameraShake(
            CAMERA_SHAKE_BOSS_MOVE
        );
    }

    private void triggerHeavyBlowFeedback() {
        if (!hasHeavyBlow()) {
            return;
        }

        requestGameplayCameraShake(
            CAMERA_SHAKE_HEAVY_BLOW
        );
    }

    private void requestGameplayCameraShake(
        float intensity
    ) {
        if (intensity <= 0f) {
            return;
        }

        gameplayCameraShakeRequested = true;

        if (intensity > gameplayCameraShakeIntensity) {
            gameplayCameraShakeIntensity = intensity;
        }
    }

    public boolean consumeGameplayCameraShakeRequest() {
        if (!gameplayCameraShakeRequested) {
            return false;
        }

        gameplayCameraShakeRequested = false;
        return true;
    }

    public float getPendingGameplayCameraShakeIntensity() {
        float intensity = gameplayCameraShakeIntensity;
        gameplayCameraShakeIntensity = 0f;
        return intensity;
    }



    private void handleCheatCodes(
        float knightDrawWidth,
        float knightDrawHeight
    ) {
        if (!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
            teleportToFalseKnightArena(
                knightDrawWidth,
                knightDrawHeight
            );
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            toggleNoclipMode(
                knightDrawWidth,
                knightDrawHeight
            );
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            emergencyHeal();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            refillSoulCheat();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
            toggleGodMode();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
            instaKillCurrentEnemies();
        }
    }

    private void setCheatMessage(String message) {
        cheatMessage = message == null
            ? ""
            : message;

        cheatMessageTimeRemaining =
            CHEAT_MESSAGE_DURATION;
    }

    private void teleportToFalseKnightArena(
        float knightDrawWidth,
        float knightDrawHeight
    ) {
        TiledWorld.BossSpawn targetSpawn = null;

        for (TiledWorld.BossSpawn spawn : world.getBossSpawns()) {
            if (
                "FALSE_KNIGHT".equals(
                    spawn.getBossType()
                )
            ) {
                targetSpawn = spawn;
                break;
            }
        }

        if (targetSpawn == null) {
            setCheatMessage(
                "Boss arena spawn not found."
            );
            return;
        }

        currentRoomId = targetSpawn.getRoomId();
        currentRoomSpawnX =
            targetSpawn.getX() - 260f;
        currentRoomSpawnY =
            targetSpawn.getY();

        configureCurrentRoomPhysics();

        spikeHazards =
            world.getSpikeHazardsForRoom(
                currentRoomId
            );

        spawnEnemiesForCurrentRoom();
        spawnZoteForCurrentRoom();
        spawnFalseKnightForCurrentRoom();
        endZoteDialogue();
        updateBackgroundMusic();

        combat.finishAttack();
        cancelFocus();

        movement.respawnAt(
            currentRoomSpawnX,
            currentRoomSpawnY
        );

        checkpoint.save(
            currentRoomSpawnX,
            currentRoomSpawnY
        );

        playerBody.update(
            player,
            knightDrawWidth,
            knightDrawHeight
        );

        setCheatMessage(
            "Teleported to False Knight arena."
        );
    }

    private void toggleNoclipMode(
        float knightDrawWidth,
        float knightDrawHeight
    ) {
        noclipModeEnabled = !noclipModeEnabled;

        if (!noclipModeEnabled) {
            synchronizeRoomAfterNoclip(
                knightDrawWidth,
                knightDrawHeight
            );
        }

        combat.finishAttack();
        cancelFocus();
        movement.respawnAt(
            player.getPosition().x,
            player.getPosition().y
        );

        player.setAnimation(
            PlayerAnimationType.IDLE
        );

        setCheatMessage(
            noclipModeEnabled
                ? "Flight / noclip enabled."
                : "Flight / noclip disabled."
        );
    }

    /**
     * Noclip can move the player into another room without touching one of
     * the normal transition triggers. When noclip ends, update the active
     * room without teleporting the player so camera bounds, collisions,
     * hazards, enemies, and music all match the player's actual location.
     */
    private void synchronizeRoomAfterNoclip(
        float knightDrawWidth,
        float knightDrawHeight
    ) {
        float playerCenterX =
            player.getPosition().x
                + knightDrawWidth / 2f;

        float playerCenterY =
            player.getPosition().y
                + knightDrawHeight / 2f;

        String roomAtPlayer =
            world.findRoomIdAt(
                playerCenterX,
                playerCenterY
            );

        if (
            roomAtPlayer == null
                || roomAtPlayer.equals(currentRoomId)
        ) {
            return;
        }

        currentRoomId = roomAtPlayer;
        currentRoomSpawnX = player.getPosition().x;
        currentRoomSpawnY = player.getPosition().y;

        configureCurrentRoomPhysics();

        spikeHazards =
            world.getSpikeHazardsForRoom(
                currentRoomId
            );

        spawnEnemiesForCurrentRoom();
        spawnZoteForCurrentRoom();
        spawnFalseKnightForCurrentRoom();
        endZoteDialogue();
        updateBackgroundMusic();

        checkpoint.save(
            currentRoomSpawnX,
            currentRoomSpawnY
        );

        playerBody.update(
            player,
            knightDrawWidth,
            knightDrawHeight
        );
    }

    private void emergencyHeal() {
        emergencyHealArmed = true;

        if (
            player.isDead()
                || health.getCurrentMasks() <= 0
        ) {
            triggerEmergencyHeal();
            return;
        }

        setCheatMessage(
            "Emergency heal armed: last mask will recharge."
        );
    }

    private boolean tryTriggerEmergencyHealOnDefeat() {
        if (!emergencyHealArmed) {
            return false;
        }

        triggerEmergencyHeal();
        return true;
    }

    private void triggerEmergencyHeal() {
        emergencyHealArmed = false;
        respawnAfterDeathAnimation = false;

        if (player.isDead()) {
            player.setDead(false);
        }

        health.setCurrentMasks(1);

        movement.respawnAt(
            player.getPosition().x,
            player.getPosition().y
        );

        player.setMovementState(
            PlayerMovementState.GROUNDED
        );

        player.setAnimation(
            PlayerAnimationType.FOCUS_GET
        );

        setCheatMessage(
            "Emergency heal triggered: +1 mask."
        );
    }

    private void refillSoulCheat() {
        soul.refill();

        setCheatMessage(
            "Soul vessel refilled."
        );
    }

    private void toggleGodMode() {
        godModeEnabled = !godModeEnabled;

        setCheatMessage(
            godModeEnabled
                ? "God Mode enabled."
                : "God Mode disabled."
        );
    }

    private void instaKillCurrentEnemies() {
        int kills = 0;

        if (huskHornhead != null && huskHornhead.isAlive()) {
            huskHornhead.takeDamage(9999);
            registerEnemyKill(EnemyKillType.HUSK_HORNHEAD);
            kills++;
        }

        if (crawlid != null && crawlid.isAlive()) {
            crawlid.takeDamage(9999);
            registerEnemyKill(EnemyKillType.CRAWLID);
            kills++;
        }

        if (crystalGuardian != null && crystalGuardian.isAlive()) {
            crystalGuardian.takeDamage(9999);
            registerEnemyKill(EnemyKillType.CRYSTAL_GUARDIAN);
            kills++;
        }

        if (wingedSentry != null && wingedSentry.isAlive()) {
            wingedSentry.takeDamage(9999);
            registerEnemyKill(EnemyKillType.WINGED_SENTRY);
            kills++;
        }

        if (falseKnight != null && falseKnight.isAlive()) {
            falseKnight.takeDamage(9999, true);
            registerFalseKnightDefeatedIfNeeded();
            kills++;
        }

        setCheatMessage(
            "Insta-kill affected " + kills + " enemies."
        );
    }

    private boolean isDamageBlockedByCheat() {
        return godModeEnabled
            || noclipModeEnabled;
    }

    private void updateNoclipMovement(
        float delta,
        float knightDrawWidth,
        float knightDrawHeight
    ) {
        int horizontal =
            currentInput.getHorizontalDirection();

        int vertical = 0;

        if (currentInput.isUpHeld()) {
            vertical += 1;
        }

        if (currentInput.isDownHeld()) {
            vertical -= 1;
        }

        player.getPosition().x +=
            horizontal * NOCLIP_SPEED * delta;

        player.getPosition().y +=
            vertical * NOCLIP_SPEED * delta;

        if (horizontal != 0) {
            player.setFacingRight(
                horizontal > 0
            );
        }

        player.setMovementState(
            PlayerMovementState.GROUNDED
        );

        player.setAnimation(
            PlayerAnimationType.IDLE
        );

        playerBody.update(
            player,
            knightDrawWidth,
            knightDrawHeight
        );
    }

    private void finishNoclipFrame(
        float delta,
        float knightDrawWidth,
        float knightDrawHeight
    ) {
        playerBody.update(
            player,
            knightDrawWidth,
            knightDrawHeight
        );

        handleRoomTransition(
            knightDrawWidth,
            knightDrawHeight
        );

        updateSafeCheckpoint();
        player.updateAnimationTime(delta);
    }

    private void handleCharmInventoryInput() {
        if (
            !Gdx.input.isKeyJustPressed(
                keyBindings.getInventory()
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
        cancelFocus();
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

        gameSfxPlayer.playSoulGain();
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

    public boolean hasHeavyBlow() {
        return charmEffects.hasHeavyBlow();
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

    public boolean isShadeSoulCastActive() {
        return shadeSoulCastActive;
    }

    public Rectangle getShadeSoulCastBounds() {
        return new Rectangle(
            shadeSoulCastBounds
        );
    }

    public float getShadeSoulCastProgress() {
        if (!shadeSoulCastActive) {
            return 0f;
        }

        return Math.max(
            0f,
            Math.min(
                1f,
                shadeSoulCastTimeRemaining
                    / SHADE_SOUL_CAST_DURATION
            )
        );
    }

    public boolean isCurrentShadeSoulVoidVariant() {
        return shadeSoulVoidVariant;
    }

    public boolean isShadeSoulEndActive() {
        return shadeSoulEndActive;
    }

    public Rectangle getShadeSoulEndBounds() {
        return new Rectangle(
            shadeSoulEndBounds
        );
    }

    public float getShadeSoulEndProgress() {
        if (!shadeSoulEndActive) {
            return 0f;
        }

        return Math.max(
            0f,
            Math.min(
                1f,
                shadeSoulEndTimeRemaining
                    / SHADE_SOUL_END_DURATION
            )
        );
    }

    public boolean isCurrentShadeSoulEndVoidVariant() {
        return shadeSoulEndVoidVariant;
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

    public boolean isCurrentAbyssShriekVoidVariant() {
        return abyssShriekVoidVariant;
    }

    private void startShadeSoulAbility() {
        if (!soul.spend(ABILITY_SOUL_COST)) {
            charmInventoryMessage =
                "Not enough Soul.";
            return;
        }

        player.setAnimation(
            PlayerAnimationType.FIREBALL_CAST
        );

        Rectangle playerBounds =
            playerBody.getBounds();

        shadeSoulVoidVariant =
            shouldUseVoidAbilityAnimations();

        voidShadeSoulFacingRight =
            player.isFacingRight();

        float castX = voidShadeSoulFacingRight
            ? playerBounds.x + playerBounds.width - 12f
            : playerBounds.x - SHADE_SOUL_CAST_WIDTH + 12f;

        float castY =
            playerBounds.y
                + playerBounds.height * 0.5f
                - SHADE_SOUL_CAST_HEIGHT / 2f;

        shadeSoulCastBounds.set(
            castX,
            castY,
            SHADE_SOUL_CAST_WIDTH,
            SHADE_SOUL_CAST_HEIGHT
        );

        shadeSoulCastActive = true;
        shadeSoulCastTimeRemaining =
            SHADE_SOUL_CAST_DURATION;

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

        shadeSoulEndActive = false;
        shadeSoulEndTimeRemaining = 0f;

        requestGameplayCameraShake(
            CAMERA_SHAKE_ABILITY_CAST
        );

        resetVoidShadeSoulHits();
        applyVoidShadeSoulDamage();
    }

    private void startAbyssShriekAbility() {
        if (!soul.spend(ABILITY_SOUL_COST)) {
            charmInventoryMessage =
                "Not enough Soul.";
            return;
        }

        player.setAnimation(
            PlayerAnimationType.SCREAM
        );

        abyssShriekVoidVariant =
            shouldUseVoidAbilityAnimations();

        positionVoidAbyssShriekBounds();

        voidAbyssShriekActive = true;
        voidAbyssShriekTimeRemaining =
            VOID_ABYSS_SHRIEK_DURATION;

        requestGameplayCameraShake(
            CAMERA_SHAKE_ABILITY_CAST
        );

        resetVoidAbyssShriekHits();
        applyVoidAbyssShriekDamage();
    }

    private void updateAbilityEffects(
        float delta
    ) {
        updateShadeSoulCastEffect(
            delta
        );

        updateVoidShadeSoul(
            delta
        );

        updateShadeSoulEndEffect(
            delta
        );

        updateVoidAbyssShriek(
            delta
        );
    }

    private void updateShadeSoulCastEffect(
        float delta
    ) {
        if (!shadeSoulCastActive) {
            return;
        }

        shadeSoulCastTimeRemaining -= delta;

        if (shadeSoulCastTimeRemaining <= 0f) {
            shadeSoulCastActive = false;
            shadeSoulCastTimeRemaining = 0f;
        }
    }

    private void updateShadeSoulEndEffect(
        float delta
    ) {
        if (!shadeSoulEndActive) {
            return;
        }

        shadeSoulEndTimeRemaining -= delta;

        if (shadeSoulEndTimeRemaining <= 0f) {
            shadeSoulEndActive = false;
            shadeSoulEndTimeRemaining = 0f;
        }
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

        boolean outOfRoom =
            voidShadeSoulBounds.x
                > currentRoomBounds.x
                + currentRoomBounds.width
                + VOID_SHADE_SOUL_WIDTH
                || voidShadeSoulBounds.x
                + voidShadeSoulBounds.width
                < currentRoomBounds.x
                - VOID_SHADE_SOUL_WIDTH;

        if (
            voidShadeSoulTimeRemaining <= 0f
                || outOfRoom
                || isShadeSoulBlockedByEnvironment()
        ) {
            finishShadeSoulProjectile();
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

    private boolean isShadeSoulBlockedByEnvironment() {
        for (Platform platform : platformWorld.getPlatforms()) {
            if (
                platform != null
                    && voidShadeSoulBounds.overlaps(
                    platform.getBounds()
                )
            ) {
                return true;
            }
        }

        return false;
    }

    private void finishShadeSoulProjectile() {
        if (!voidShadeSoulActive) {
            return;
        }

        shadeSoulEndBounds.set(
            voidShadeSoulBounds.x
                + voidShadeSoulBounds.width / 2f
                - SHADE_SOUL_END_WIDTH / 2f,
            voidShadeSoulBounds.y
                + voidShadeSoulBounds.height / 2f
                - SHADE_SOUL_END_HEIGHT / 2f,
            SHADE_SOUL_END_WIDTH,
            SHADE_SOUL_END_HEIGHT
        );

        shadeSoulEndActive = true;
        shadeSoulEndTimeRemaining =
            SHADE_SOUL_END_DURATION;
        shadeSoulEndVoidVariant =
            shadeSoulVoidVariant;

        voidShadeSoulActive = false;
        voidShadeSoulTimeRemaining = 0f;
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
            gameSfxPlayer.playEnemyDamage();
            if (!huskHornhead.isAlive()) {
                registerEnemyKill(
                    EnemyKillType.HUSK_HORNHEAD
                );
            }
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
            gameSfxPlayer.playEnemyDamage();
            if (!crawlid.isAlive()) {
                registerEnemyKill(
                    EnemyKillType.CRAWLID
                );
            }
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
            gameSfxPlayer.playEnemyDamage();
            if (!crystalGuardian.isAlive()) {
                registerEnemyKill(
                    EnemyKillType.CRYSTAL_GUARDIAN
                );
            }
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
            gameSfxPlayer.playEnemyDamage();
            if (!wingedSentry.isAlive()) {
                registerEnemyKill(
                    EnemyKillType.WINGED_SENTRY
                );
            }
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
            gameSfxPlayer.playEnemyDamage();
            if (!huskHornhead.isAlive()) {
                registerEnemyKill(
                    EnemyKillType.HUSK_HORNHEAD
                );
            }
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
            gameSfxPlayer.playEnemyDamage();
            if (!crawlid.isAlive()) {
                registerEnemyKill(
                    EnemyKillType.CRAWLID
                );
            }
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
            gameSfxPlayer.playEnemyDamage();
            if (!crystalGuardian.isAlive()) {
                registerEnemyKill(
                    EnemyKillType.CRYSTAL_GUARDIAN
                );
            }
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
            gameSfxPlayer.playEnemyDamage();
            if (!wingedSentry.isAlive()) {
                registerEnemyKill(
                    EnemyKillType.WINGED_SENTRY
                );
            }
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

        boolean wasAlive = falseKnight.isAlive();

        falseKnight.takeDamage(
            damage,
            hitVulnerableBody
        );

        playFalseKnightDamageFeedback(
            hitVulnerableBody
        );

        if (wasAlive && !falseKnight.isAlive()) {
            registerFalseKnightDefeatedIfNeeded();
        }

        return true;
    }

    private void playFalseKnightDamageFeedback(
        boolean hitVulnerableBody
    ) {
        gameSfxPlayer.playEnemyDamage();

        if (hitVulnerableBody) {
            if (falseKnightHeadDamageSound != null) {
                falseKnightHeadDamageSound.play(0.75f);
            }
        } else if (falseKnightDamageArmourSound != null) {
            falseKnightDamageArmourSound.play(0.65f);
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
            gameSfxPlayer.stopFocusCharging();

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
            gameSfxPlayer.playFocusHeal();

            player.setAnimation(
                PlayerAnimationType.FOCUS_GET
            );
        }
    }

    private void cancelFocus() {
        boolean wasActive = focus.isActive();

        focus.cancel();

        if (wasActive) {
            gameSfxPlayer.stopFocusCharging();
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
        if (falseKnightDefeated) {
            falseKnight = null;
            return;
        }

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
        if (roomTransitionBlockedUntilExit) {
            if (isOverlappingCurrentRoomTransition()) {
                return false;
            }

            roomTransitionBlockedUntilExit = false;
        }

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

    private boolean isOverlappingCurrentRoomTransition() {
        for (
            TiledWorld.RoomTransition transition
            : world.getRoomTransitions()
        ) {
            if (
                currentRoomId.equals(
                    transition.getFromRoom()
                )
                    && playerBody
                    .getBounds()
                    .overlaps(
                        transition.getBounds()
                    )
            ) {
                return true;
            }
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
        updateBackgroundMusic();

        combat.finishAttack();
        cancelFocus();

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

        roomTransitionBlockedUntilExit = true;
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

        if (isDamageBlockedByCheat()) {
            return true;
        }

        PlayerHealth.DamageResult result =
            health.takeDamage(
                touchedHazard.getDamage()
            );

        if (result != PlayerHealth.DamageResult.IGNORED) {
            gameSfxPlayer.playPlayerDamage();

            requestGameplayCameraShake(
                CAMERA_SHAKE_PLAYER_DAMAGE
            );
        }

        combat.finishAttack();
        cancelFocus();

        if (
            result
                == PlayerHealth
                .DamageResult.DEFEATED
        ) {
            if (tryTriggerEmergencyHealOnDefeat()) {
                return true;
            }

            beginPlayerDeath(true);
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

        gameSfxPlayer.playEnemyDamage();

        if (!huskHornhead.isAlive()) {
            registerEnemyKill(
                EnemyKillType.HUSK_HORNHEAD
            );
        }
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

        gameSfxPlayer.playEnemyDamage();

        if (!crawlid.isAlive()) {
            registerEnemyKill(
                EnemyKillType.CRAWLID
            );
        }
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

        gameSfxPlayer.playEnemyDamage();

        if (!crystalGuardian.isAlive()) {
            registerEnemyKill(
                EnemyKillType.CRYSTAL_GUARDIAN
            );
        }
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

        gameSfxPlayer.playEnemyDamage();

        if (!wingedSentry.isAlive()) {
            registerEnemyKill(
                EnemyKillType.WINGED_SENTRY
            );
        }
    }

    private void applySharpShadowDamageToFalseKnight(
        Rectangle playerBounds
    ) {
        if (
            sharpShadowHitFalseKnight
                || falseKnight == null
                || !falseKnight.isAlive()
        ) {
            return;
        }

        boolean hitVulnerableBody =
            falseKnight.isStunned();

        Rectangle targetHitbox = hitVulnerableBody
            ? falseKnight.getVulnerableHitbox()
            : falseKnight.getBounds();

        if (!playerBounds.overlaps(targetHitbox)) {
            return;
        }

        sharpShadowHitFalseKnight = true;

        boolean wasAlive = falseKnight.isAlive();

        falseKnight.takeDamage(
            charmEffects.getSharpShadowDamage(
                getNailDamage()
            ),
            hitVulnerableBody
        );

        playFalseKnightDamageFeedback(
            hitVulnerableBody
        );

        if (wasAlive && !falseKnight.isAlive()) {
            registerFalseKnightDefeatedIfNeeded();
        }
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

        if (isDamageBlockedByCheat()) {
            return true;
        }

        PlayerHealth.DamageResult result =
            health.takeDamage(
                crystalGuardian
                    .getLaserDamage()
            );

        if (result != PlayerHealth.DamageResult.IGNORED) {
            gameSfxPlayer.playPlayerDamage();

            requestGameplayCameraShake(
                CAMERA_SHAKE_PLAYER_DAMAGE
            );
        }

        if (
            result
                == PlayerHealth
                .DamageResult.IGNORED
        ) {
            return true;
        }

        combat.finishAttack();
        cancelFocus();

        if (
            result
                == PlayerHealth
                .DamageResult.DEFEATED
        ) {
            if (tryTriggerEmergencyHealOnDefeat()) {
                return true;
            }

            beginPlayerDeath(true);
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
            /*
             * Use the complete shadow trail instead of only the Knight body.
             * This prevents a faster dash from skipping over the boss between
             * frames and also lets the trail reach the exposed creature.
             */
            applySharpShadowDamageToFalseKnight(
                getSharpShadowDashVisualBounds()
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
        if (isDamageBlockedByCheat()) {
            return true;
        }

        PlayerHealth.DamageResult result =
            health.takeDamage(damage);

        if (result != PlayerHealth.DamageResult.IGNORED) {
            gameSfxPlayer.playPlayerDamage();

            requestGameplayCameraShake(
                CAMERA_SHAKE_PLAYER_DAMAGE
            );
        }

        if (
            result
                == PlayerHealth
                .DamageResult.IGNORED
        ) {
            return true;
        }

        combat.finishAttack();
        cancelFocus();

        if (
            result
                == PlayerHealth
                .DamageResult.DEFEATED
        ) {
            if (tryTriggerEmergencyHealOnDefeat()) {
                return true;
            }

            beginPlayerDeath(true);
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

    private void beginPlayerDeath(
        boolean respawnAutomatically
    ) {
        emergencyHealArmed = false;
        respawnAfterDeathAnimation =
            respawnAutomatically;

        if (respawnAutomatically) {
            deathCount++;
        }

        combat.finishAttack();
        cancelFocus();

        player.setDead(true);
        player.setMovementState(
            PlayerMovementState.DEAD
        );
        player.setAnimation(
            PlayerAnimationType.DEATH
        );

        /*
         * Stop every movement timer but leave the
         * Knight at the death position. Moving it to
         * the start before this animation is rendered
         * can place the death sprite outside the active
         * room and camera.
         */
        movement.prepareForDeath();
    }

    private void respawnPlayerAtGameStart() {
        respawnAfterDeathAnimation = false;
        emergencyHealArmed = false;

        health.restoreFullHealth();

        currentRoomId = "forgotten_crossroads";
        currentRoomSpawnX = spawnX;
        currentRoomSpawnY = spawnY;

        configureCurrentRoomPhysics();

        spikeHazards =
            world.getSpikeHazardsForRoom(
                currentRoomId
            );

        spawnEnemiesForCurrentRoom();
        spawnZoteForCurrentRoom();
        spawnFalseKnightForCurrentRoom();
        endZoteDialogue();
        updateBackgroundMusic();

        combat.finishAttack();
        cancelFocus();

        checkpoint.save(
            currentRoomSpawnX,
            currentRoomSpawnY
        );

        movement.respawnAt(
            currentRoomSpawnX,
            currentRoomSpawnY
        );

        roomTransitionBlockedUntilExit = false;

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
                keyBindings.getFireball()
            ),
            Gdx.input.isKeyJustPressed(
                keyBindings.getScream()
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
            respawnPlayerAtGameStart();
            return;
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
                    keyBindings.getDialogueAdvance()
                )
            ) {
                advanceZoteDialogue();
            }

            return;
        }

        boolean interactPressed =
            Gdx.input.isKeyJustPressed(
                keyBindings.getInteract()
            )
                || Gdx.input.isKeyJustPressed(
                keyBindings.getUp()
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
            /*
             * The death-test cheat keeps the final
             * frame until the revive key is pressed.
             */
            beginPlayerDeath(false);
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
                currentInput,
                getDashLengthMultiplier()
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
            gameSfxPlayer.playSoulGain();

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
                gameSfxPlayer.playFocusStart();

                player.setAnimation(
                    PlayerAnimationType.FOCUS_START
                );
            }

            return;
        }

        if (
            combat.tryStartAttack(
                currentInput,
                player
            )
        ) {
            gameSfxPlayer.playNailSlash();
        }
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

        boolean wasAlive = huskHornhead.isAlive();

        boolean hit = applyNailHit(
            huskHornhead.getBounds(),
            huskHornhead,
            huskHornhead
        );

        if (hit && wasAlive && !huskHornhead.isAlive()) {
            registerEnemyKill(
                EnemyKillType.HUSK_HORNHEAD
            );
        }

        return hit;
    }

    private boolean tryHitCrawlid() {
        if (
            crawlid == null
                || !crawlid.isAlive()
        ) {
            return false;
        }

        boolean wasAlive = crawlid.isAlive();

        boolean hit = applyNailHit(
            crawlid.getBounds(),
            crawlid,
            crawlid
        );

        if (hit && wasAlive && !crawlid.isAlive()) {
            registerEnemyKill(
                EnemyKillType.CRAWLID
            );
        }

        return hit;
    }

    private boolean
    tryHitCrystalGuardian() {
        if (
            crystalGuardian == null
                || !crystalGuardian.isAlive()
        ) {
            return false;
        }

        boolean wasAlive = crystalGuardian.isAlive();

        boolean hit = applyNailHit(
            crystalGuardian.getBounds(),
            crystalGuardian,
            crystalGuardian
        );

        if (hit && wasAlive && !crystalGuardian.isAlive()) {
            registerEnemyKill(
                EnemyKillType.CRYSTAL_GUARDIAN
            );
        }

        return hit;
    }

    private boolean tryHitWingedSentry() {
        if (
            wingedSentry == null
                || !wingedSentry.isAlive()
        ) {
            return false;
        }

        boolean wasAlive = wingedSentry.isAlive();

        boolean hit = applyNailHit(
            wingedSentry.getBounds(),
            wingedSentry,
            wingedSentry
        );

        if (hit && wasAlive && !wingedSentry.isAlive()) {
            registerEnemyKill(
                EnemyKillType.WINGED_SENTRY
            );
        }

        return hit;
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

        boolean wasAlive = falseKnight.isAlive();

        falseKnight.takeDamage(
            getNailDamage(),
            hitVulnerableBody
        );

        if (wasAlive && !falseKnight.isAlive()) {
            registerFalseKnightDefeatedIfNeeded();
        }

        gainSoulFromNailHit();

        if (
            combat.isDownwardAttack()
                && !movement.isOnGround()
        ) {
            applySuccessfulPogo();
        }

        playFalseKnightDamageFeedback(
            hitVulnerableBody
        );

        triggerHeavyBlowFeedback();

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
            gameSfxPlayer.playBreakableWallDestroyed();

            if (crackedWallPlatform != null) {
                platformWorld.removePlatform(
                    crackedWallPlatform
                );
            }

            unlockVoidHeartReward();
        } else if (
            result
                == CrackedWall.HitResult.BROKEN_APPEARANCE
        ) {
            gameSfxPlayer.playBreakableWallHit2();
        } else {
            gameSfxPlayer.playBreakableWallHit1();
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
                platformWorld,
                getEnemyKnockbackMultiplier()
            );
        }

        damageable.takeDamage(
            getNailDamage()
        );

        gameSfxPlayer.playEnemyDamage();
        triggerHeavyBlowFeedback();

        gainSoulFromNailHit();

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
                if (respawnAfterDeathAnimation) {
                    respawnPlayerAtGameStart();
                }
                // Cheat deaths keep the final frame
                // until the revive key is pressed.
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


    private void registerEnemyKill(
        EnemyKillType enemyType
    ) {
        if (enemyType == null) {
            return;
        }

        killedEnemyTypes.add(enemyType);
        totalEnemiesKilled++;

        recentEnemyKills++;
        recentEnemyKillWindowRemaining =
            TWO_KILL_ACHIEVEMENT_WINDOW;

        if (recentEnemyKills >= 2) {
            achievementManager.unlock(
                AchievementType
                    .KILL_TWO_ENEMIES_10_SECONDS
            );
        }

        if (
            killedEnemyTypes.containsAll(
                EnumSet.allOf(EnemyKillType.class)
            )
        ) {
            achievementManager.unlock(
                AchievementType.TRUE_HUNTER
            );
        }
    }

    private void registerFalseKnightDefeatedIfNeeded() {
        if (
            falseKnight == null
                || falseKnight.isAlive()
                || falseKnightDefeated
        ) {
            return;
        }

        falseKnightDefeated = true;
        totalEnemiesKilled++;

        achievementManager.unlock(
            AchievementType.DEFEAT_FALSE_KNIGHT
        );

        achievementManager.unlock(
            AchievementType.COMPLETION
        );

        if (elapsedGameSeconds <= SPEEDRUN_LIMIT_SECONDS) {
            achievementManager.unlock(
                AchievementType.SPEEDRUN
            );
        }

        endGamePending = true;
        endGameRequested = false;
        endGameDelayRemaining =
            END_GAME_SCREEN_DELAY_SECONDS;
    }

    private GameData createGameData() {
        GameData data = new GameData();

        data.currentRoomId = currentRoomId;
        data.playerX = player.getPosition().x;
        data.playerY = player.getPosition().y;
        data.currentMasks = health.getCurrentMasks();
        data.currentSoul = soul.getCurrentSoul();
        data.crackedWallDestroyed =
            crackedWall != null
                && crackedWall.isDestroyed();
        data.falseKnightDefeated =
            falseKnightDefeated
                || (falseKnight != null
                && !falseKnight.isAlive());
        data.godModeEnabled = godModeEnabled;
        data.noclipModeEnabled = noclipModeEnabled;
        data.elapsedGameSeconds = elapsedGameSeconds;
        data.deathCount = deathCount;
        data.totalEnemiesKilled = totalEnemiesKilled;

        for (CharmType charm : charmInventory.getOwnedCharms()) {
            data.ownedCharms.add(charm.name());
        }

        for (CharmType charm : charmInventory.getEquippedCharms()) {
            data.equippedCharms.add(charm.name());
        }

        for (EnemyKillType enemyType : killedEnemyTypes) {
            data.killedEnemyTypes.add(enemyType.name());
        }

        data.unlockedAchievements.addAll(
            achievementManager.getUnlockedTypeNames()
        );

        return data;
    }

    private void applyGameData(
        GameData data
    ) {
        if (data == null) {
            return;
        }

        currentRoomId = data.currentRoomId == null
            ? "forgotten_crossroads"
            : data.currentRoomId;

        currentRoomBounds = world.getRoomBounds(
            currentRoomId
        );

        currentRoomSpawnX = data.playerX;
        currentRoomSpawnY = data.playerY;

        falseKnightDefeated =
            data.falseKnightDefeated;

        godModeEnabled =
            data.godModeEnabled;

        noclipModeEnabled =
            data.noclipModeEnabled;

        elapsedGameSeconds =
            Math.max(0f, data.elapsedGameSeconds);

        deathCount =
            Math.max(0, data.deathCount);

        totalEnemiesKilled =
            Math.max(0, data.totalEnemiesKilled);

        endGamePending = false;
        endGameRequested = false;
        endGameDelayRemaining = 0f;

        configureCurrentRoomPhysics();

        if (
            data.crackedWallDestroyed
                && crackedWall != null
                && !crackedWall.isDestroyed()
        ) {
            while (!crackedWall.isDestroyed()) {
                crackedWall.hit();
            }

            if (crackedWallPlatform != null) {
                platformWorld.removePlatform(
                    crackedWallPlatform
                );
            }
        }

        spikeHazards = world.getSpikeHazardsForRoom(
            currentRoomId
        );

        spawnEnemiesForCurrentRoom();
        spawnZoteForCurrentRoom();
        spawnFalseKnightForCurrentRoom();
        endZoteDialogue();
        updateBackgroundMusic();

        movement.respawnAt(
            data.playerX,
            data.playerY
        );

        checkpoint.save(
            data.playerX,
            data.playerY
        );

        health.setCurrentMasks(
            data.currentMasks
        );

        soul.setCurrentSoul(
            data.currentSoul
        );

        applySavedCharms(data);

        killedEnemyTypes.clear();

        if (data.killedEnemyTypes != null) {
            for (String enemyName : data.killedEnemyTypes) {
                try {
                    killedEnemyTypes.add(
                        EnemyKillType.valueOf(enemyName)
                    );
                } catch (IllegalArgumentException ignored) {
                    // Ignore unknown enemy types from other builds.
                }
            }
        }

        achievementManager.applyUnlockedTypeNames(
            data.unlockedAchievements
        );

        combat.finishAttack();
        cancelFocus();
    }

    private void applySavedCharms(
        GameData data
    ) {
        EnumSet<CharmType> owned =
            EnumSet.noneOf(CharmType.class);

        if (data.ownedCharms != null) {
            for (String charmName : data.ownedCharms) {
                try {
                    owned.add(
                        CharmType.valueOf(charmName)
                    );
                } catch (IllegalArgumentException ignored) {
                    // Ignore unknown charms from other builds.
                }
            }
        }

        if (owned.isEmpty()) {
            for (CharmType charm : CharmType.values()) {
                if (charm != CharmType.VOID_HEART) {
                    owned.add(charm);
                }
            }
        }

        charmInventory.setOwnedCharms(owned);

        EnumSet<CharmType> equipped =
            EnumSet.noneOf(CharmType.class);

        if (data.equippedCharms != null) {
            for (String charmName : data.equippedCharms) {
                try {
                    equipped.add(
                        CharmType.valueOf(charmName)
                    );
                } catch (IllegalArgumentException ignored) {
                    // Ignore unknown charms from other builds.
                }
            }
        }

        charmInventory.setEquippedCharms(equipped);
    }


    private void updateBackgroundMusic() {
        game.playGameplayMusic(
            currentRoomId,
            falseKnight != null
                && falseKnight.isAlive()
        );
    }

    public String saveGame() {
        GameData data = createGameData();

        saveManager.save(
            data,
            saveSlotNumber
        );

        databaseSaveManager.save(
            data,
            saveSlotNumber
        );

        setCheatMessage(
            "Game saved."
        );

        return "Game saved to slot "
            + saveSlotNumber
            + ".";
    }

    public String loadGame() {
        GameData data = saveManager.load(
            saveSlotNumber
        );

        if (data == null) {
            data = databaseSaveManager.load(
                saveSlotNumber
            );
        }

        if (data == null) {
            setCheatMessage(
                "No save file found."
            );

            return "No save file found.";
        }

        applyGameData(data);

        setCheatMessage(
            "Game loaded."
        );

        return "Game loaded.";
    }

    public boolean hasSavedGame() {
        return saveManager.hasSave(
            saveSlotNumber
        )
            || databaseSaveManager.hasSave(
            saveSlotNumber
        );
    }

    public void saveGameAndExit() {
        saveGame();
        returnToMainMenu();
    }

    private KeyBindings createKeyBindings(
        GameSettings settings
    ) {
        return new KeyBindings(
            settings.getMoveLeftKey(),
            settings.getMoveRightKey(),
            settings.getJumpKey(),
            settings.getDashKey(),
            settings.getAttackKey(),
            settings.getUpKey(),
            settings.getDownKey(),
            settings.getAlternateAttackKey(),
            settings.getFocusKey(),
            settings.getFireballKey(),
            settings.getScreamKey(),
            settings.getInteractKey(),
            settings.getDialogueAdvanceKey(),
            settings.getInventoryKey(),
            settings.getPauseKey()
        );
    }

    public int getPauseKey() {
        return keyBindings.getPause();
    }

    public void openSettingsMenu() {
        game.showSettingsMenuFromPause(this);
    }

    public void resumeAfterSettings() {
        GameSettings settings = game.getSettings();

        keyBindings = createKeyBindings(settings);

        updateBackgroundMusic();
    }

    public void openAchievementsMenu() {
        game.showAchievementsMenu();
    }

    public List<Achievement> getAchievements() {
        return achievementManager.getAchievements();
    }

    public void addAchievementObserver(
        AchievementObserver observer
    ) {
        achievementManager.addObserver(observer);
    }

    public void removeAchievementObserver(
        AchievementObserver observer
    ) {
        achievementManager.removeObserver(observer);
    }

    public boolean shouldOpenEndGameScreen() {
        return endGameRequested;
    }

    public void openEndGameScreen() {
        if (!endGameRequested) {
            return;
        }

        endGamePending = false;
        endGameRequested = false;
        endGameDelayRemaining = 0f;

        game.showEndGameScreen(
            new EndGameStats(
                deathCount,
                totalEnemiesKilled,
                elapsedGameSeconds
            )
        );
    }

    public int getDeathCount() {
        return deathCount;
    }

    public int getTotalEnemiesKilled() {
        return totalEnemiesKilled;
    }

    public boolean isGodModeEnabled() {
        return godModeEnabled;
    }

    public boolean isNoclipModeEnabled() {
        return noclipModeEnabled;
    }

    public String getCheatMessage() {
        return cheatMessage == null
            ? ""
            : cheatMessage;
    }

    public String getCheatStatusLine() {
        return "God Mode: "
            + (godModeEnabled ? "ON" : "OFF")
            + "   Flight/Noclip: "
            + (noclipModeEnabled ? "ON" : "OFF")
            + "   Emergency Heal: "
            + (emergencyHealArmed ? "ARMED" : "OFF");
    }

    public float getElapsedGameSeconds() {
        return elapsedGameSeconds;
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
        /*
         * Damage invincibility normally blinks the
         * Knight. A death animation must remain fully
         * visible even though the final hit started the
         * invincibility timer.
         */
        return player.isDead()
            || health.shouldDrawPlayer();
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

    public boolean isNailSlashActive() {
        return combat.isAttacking();
    }

    public AttackDirection getCurrentAttackDirection() {
        return combat.getAttackDirection();
    }

    public float getCurrentAttackTime() {
        return combat.getAttackTime();
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
        if (noclipModeEnabled) {
            return new Rectangle(
                0f,
                0f,
                world.getMapWidth(),
                world.getMapHeight()
            );
        }

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

        gameSfxPlayer.dispose();

        world.dispose();
    }

    public void returnToMainMenu() {
        game.showMainMenu();
    }
}
