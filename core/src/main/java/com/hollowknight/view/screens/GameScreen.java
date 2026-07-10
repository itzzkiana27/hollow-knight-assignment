package com.hollowknight.view.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hollowknight.controller.GameController;
import com.hollowknight.model.enemy.Crawlid;
import com.hollowknight.model.enemy.HuskHornhead;
import com.hollowknight.model.player.Player;
import com.hollowknight.model.npc.Zote;
import com.hollowknight.model.player.PlayerAnimationType;
import com.hollowknight.view.animation.CrawlidAnimationManager;
import com.hollowknight.view.animation.HuskHornheadAnimationManager;
import com.hollowknight.view.animation.KnightAnimationManager;
import com.hollowknight.view.animation.ZoteAnimationManager;
import com.hollowknight.view.camera.GameCamera;
import com.hollowknight.model.enemy.CrystalGuardian;
import com.hollowknight.view.animation.CrystalGuardianAnimationManager;
import com.hollowknight.view.effects.CrystalGuardianLaserRenderer;
import com.hollowknight.view.effects.CrackedWallRenderer;
import com.hollowknight.model.world.CrackedWall;
import com.hollowknight.model.world.TiledWorld;
import com.hollowknight.model.enemy.WingedSentry;
import com.hollowknight.view.animation.WingedSentryAnimationManager;
import com.hollowknight.view.effects.RainEffect;
import com.hollowknight.model.boss.FalseKnight;
import com.hollowknight.model.charm.CharmType;
import com.hollowknight.model.achievement.Achievement;
import com.hollowknight.model.combat.AttackDirection;
import com.hollowknight.view.actors.AchievementPopupObserver;
import com.hollowknight.view.animation.FalseKnightAnimationManager;

import java.util.EnumMap;

public class GameScreen extends ScreenAdapter {

    /*
     * Visible camera area.
     */
    private static final float CAMERA_VIEW_WIDTH =
        1280f;

    private static final float CAMERA_VIEW_HEIGHT = 720f;

    /*
     * Knight rendering values.
     */
    private static final float KNIGHT_SOURCE_FRAME_WIDTH = 349f;

    private static final float KNIGHT_SOURCE_FRAME_HEIGHT = 186f;

    private static final float KNIGHT_DRAW_HEIGHT = 150f;

    private static final float
        KNIGHT_DRAW_WIDTH =
        KNIGHT_DRAW_HEIGHT
            * KNIGHT_SOURCE_FRAME_WIDTH
            / KNIGHT_SOURCE_FRAME_HEIGHT;

    /*
     * The supplied Knight frames face left.
     */
    private static final boolean KNIGHT_SOURCE_FACES_RIGHT = false;

    /*
     * Husk Hornhead rendering values.
     *
     * Every Husk frame is 239 x 219.
     */
    private static final float HUSK_SOURCE_WIDTH = 239f;

    private static final float HUSK_SOURCE_HEIGHT = 219f;

    private static final float HUSK_DRAW_HEIGHT = 145f;

    private static final float
        HUSK_DRAW_WIDTH =
        HUSK_DRAW_HEIGHT
            * HUSK_SOURCE_WIDTH
            / HUSK_SOURCE_HEIGHT;

    /*
     * The supplied Husk frames face left.
     */
    private static final boolean
        HUSK_SOURCE_FACES_RIGHT = false;

    /*
     * Change to true to display the Husk body
     * and vision rectangles.
     */
    private static final boolean
        DRAW_HUSK_DEBUG = false;

    /*
     * Crawlid walk frames are 301 x 149.
     *
     * Death frames have a slightly different canvas
     * size. The renderer calculates their dimensions
     * dynamically while keeping the same scale.
     */
    private static final float CRAWLID_SOURCE_HEIGHT = 149f;

    private static final float CRAWLID_DRAW_HEIGHT = 105f;

    private static final float
        CRAWLID_DRAW_SCALE =
        CRAWLID_DRAW_HEIGHT
            / CRAWLID_SOURCE_HEIGHT;

    /*
     * The supplied Crawlid frames face left.
     */
    private static final boolean
        CRAWLID_SOURCE_FACES_RIGHT = false;

    /*
     * Change to true to display the Crawlid body.
     */
    private static final boolean
        DRAW_CRAWLID_DEBUG = false;

    private static final float
        CRYSTAL_SOURCE_HEIGHT = 189f;

    private static final float
        CRYSTAL_DRAW_HEIGHT = 165f;

    private static final float
        CRYSTAL_DRAW_SCALE =
        CRYSTAL_DRAW_HEIGHT
            / CRYSTAL_SOURCE_HEIGHT;

    private static final boolean CRYSTAL_SOURCE_FACES_RIGHT = false;

    private static final boolean DRAW_CRYSTAL_DEBUG = false;

    private static final float WINGED_SOURCE_HEIGHT = 398f;
    private static final float WINGED_DRAW_HEIGHT = 170f;

    private static final float
        WINGED_DRAW_SCALE =
        WINGED_DRAW_HEIGHT
            / WINGED_SOURCE_HEIGHT;

    /*
     * The supplied Winged Sentry frames face left.
     */
    private static final boolean WINGED_SOURCE_FACES_RIGHT = false;

    private static final boolean DRAW_WINGED_DEBUG = false;

    /*
     * Zote frames use the same source canvas size
     * as the Knight frames: 349 x 186.
     */
    private static final float ZOTE_SOURCE_FRAME_WIDTH = 349f;

    private static final float ZOTE_SOURCE_FRAME_HEIGHT = 186f;

    private static final float ZOTE_DRAW_HEIGHT = 130f;

    private static final float
        ZOTE_DRAW_WIDTH =
        ZOTE_DRAW_HEIGHT
            * ZOTE_SOURCE_FRAME_WIDTH
            / ZOTE_SOURCE_FRAME_HEIGHT;

    /*
     * The supplied Zote frames face left.
     */
    private static final boolean ZOTE_SOURCE_FACES_RIGHT = false;

    /*
     * False Knight rendering values.
     * Change this if the boss looks too large or too small.
     */
    private static final float FALSE_KNIGHT_DRAW_HEIGHT = 400f;

    /*
     * On-screen height of the Power Mace Slam shockwave sprite. Change this if
     * the wave looks too large or too small relative to the boss.
     */
    private static final float SHOCKWAVE_DRAW_HEIGHT = 150f;

    /*
     * If False Knight faces the wrong direction, flip this value.
     */
    private static final boolean FALSE_KNIGHT_SOURCE_FACES_RIGHT = false;

    private static final boolean DRAW_FALSE_KNIGHT_DEBUG = false;

    /*
     * Charm inventory menu values.
     */


    private static final int CHARM_MENU_COLUMNS = 4;

    private static final float CHARM_MENU_PANEL_WIDTH = 940f;

    private static final float CHARM_MENU_PANEL_HEIGHT = 590f;

    private static final float CHARM_CARD_WIDTH = 190f;

    private static final float CHARM_CARD_HEIGHT = 145f;

    private static final float CHARM_CARD_GAP_X = 22f;

    private static final float CHARM_CARD_GAP_Y = 24f;

    private static final float CHARM_ICON_SIZE = 70f;

    private static final int SHARP_SHADOW_DASH_FRAME_COUNT = 11;

    private static final float SHARP_SHADOW_DASH_FRAME_DURATION = 0.025f;

    private static final String WHITE_DASH_EFFECT_PATH =
        "sprites/effects/charms/sharp_shadow_dash.png";

    private static final float WHITE_DASH_EFFECT_DRAW_HEIGHT = 100f;

    private static final float WHITE_DASH_EFFECT_ALPHA = 0.85f;
    private static final float WHITE_DASH_EFFECT_DURATION = 0.2f;

    private static final int WHITE_DASH_EFFECT_FRAME_COUNT = 8;

    private static final int BLAST_SOUL_FRAME_COUNT = 8;

    private static final int SOUL_BALL_FRAME_COUNT = 4;

    private static final int SOUL_BALL_END_FRAME_COUNT = 3;

    private static final int SHADOW_BALL_FRAME_COUNT = 8;

    private static final int SHADOW_BALL_END_FRAME_COUNT = 3;

    private static final int SOUL_SCREAM_FRAME_COUNT = 13;

    private static final int SHADOW_SCREAM_FRAME_COUNT = 14;

    private static final float SHADE_SOUL_PROJECTILE_VISUAL_DURATION = 0.72f;

    private static final float SHADE_SOUL_PROJECTILE_FRAME_DURATION = 0.055f;

    /*
     * If the white dash effect faces the wrong way,
     * change this to false.
     */
    private static final boolean WHITE_DASH_EFFECT_SOURCE_FACES_RIGHT = true;

    private static final int SLASH_EFFECT_FRAME_COUNT = 4;

    private static final float SLASH_EFFECT_FRAME_DURATION = 0.045f;

    private static final float SLASH_EFFECT_DURATION =
        SLASH_EFFECT_FRAME_COUNT * SLASH_EFFECT_FRAME_DURATION;

    private static final String SLASH_EFFECT_HORIZONTAL_PATH_FORMAT =
        "sprites/effects/slash/slash_horizontal_%d.png";

    private static final String SLASH_EFFECT_UP_PATH_FORMAT =
        "sprites/effects/slash/slash_up_%d.png";

    private static final String SLASH_EFFECT_DOWN_PATH_FORMAT =
        "sprites/effects/slash/slash_down_%d.png";

    private static final float SLASH_EFFECT_HORIZONTAL_DRAW_HEIGHT = 105f;

    private static final float SLASH_EFFECT_VERTICAL_DRAW_HEIGHT = 145f;

    private static final float SLASH_EFFECT_ALPHA = 0.92f;

    /*
     * The horizontal slash source image is drawn facing left.
     * Keep this false so right-facing attacks are mirrored in code.
     */
    private static final boolean SLASH_EFFECT_HORIZONTAL_SOURCE_FACES_RIGHT = false;

    /*
     * Distance between the delayed shadow copies.
     * Higher = more spaced/paced trail.
     */
    private static final float SHARP_SHADOW_TRAIL_SPACING = 34f;

    /*
     * These uploaded Shadow Dash frames face left,
     * same as your normal Knight source frames.
     */
    private static final boolean SHARP_SHADOW_SOURCE_FACES_RIGHT = false;

    private static final float SHADE_SOUL_CAST_EFFECT_HEIGHT = 185f;

    private static final float SHADE_SOUL_PROJECTILE_EFFECT_HEIGHT = 115f;

    private static final float SHADE_SOUL_END_EFFECT_HEIGHT = 150f;

    private static final float ABYSS_SHRIEK_EFFECT_HEIGHT = 310f;

    private static final boolean SHADE_SOUL_SOURCE_FACES_RIGHT = true;

    private static final float VOID_HEART_REWARD_DRAW_SIZE = 54f;

    private static final float VOID_HEART_REWARD_Y_OFFSET = 58f;


    private final GameController controller;

    private Stage stage;
    private Skin skin;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthogonalTiledMapRenderer mapRenderer;
    private CrackedWallRenderer crackedWallRenderer;
    private RainEffect rainEffect;

    private KnightAnimationManager knightAnimationManager;
    private HuskHornheadAnimationManager huskAnimationManager;
    private CrawlidAnimationManager crawlidAnimationManager;
    private CrystalGuardianAnimationManager crystalAnimationManager;
    private CrystalGuardianLaserRenderer crystalLaserRenderer;
    private WingedSentryAnimationManager wingedAnimationManager;
    private FalseKnightAnimationManager falseKnightAnimationManager;
    private ZoteAnimationManager zoteAnimationManager;

    private EnumMap<CharmType, Texture> charmIconTextures;
    private Texture[] sharpShadowDashTextures;
    private TextureRegion[] sharpShadowDashFrames;
    private Texture whiteDashEffectTexture;
    private TextureRegion[] whiteDashEffectFrames;
    private Texture[] slashEffectTextures;
    private TextureRegion[] slashHorizontalEffectFrames;
    private TextureRegion[] slashUpEffectFrames;
    private TextureRegion[] slashDownEffectFrames;
    private Texture blastSoulTexture;
    private TextureRegion[] blastSoulFrames;
    private Texture soulBallTexture;
    private TextureRegion[] soulBallFrames;
    private Texture soulBallEndTexture;
    private TextureRegion[] soulBallEndFrames;
    private Texture shadowBallTexture;
    private TextureRegion[] shadowBallFrames;
    private Texture shadowBallEndTexture;
    private TextureRegion[] shadowBallEndFrames;
    private Texture soulScreamTexture;
    private TextureRegion[] soulScreamFrames;
    private Texture shadowScreamTexture;
    private TextureRegion[] shadowScreamFrames;
    private final Rectangle charmMenuPanelBounds = new Rectangle();
    private final Rectangle charmCardBounds = new Rectangle();
    private final Vector2 charmTouchPosition = new Vector2();
    private CharmType selectedCharm;

    private Table pauseOverlay;
    private Table pauseContent;
    private Label pauseMessageLabel;
    private boolean pauseMenuOpen;
    private boolean startPaused;
    private boolean preserveControllerOnDispose;
    private AchievementPopupObserver popupObserver;

    private GameCamera worldCamera;

    public GameScreen(
        GameController controller
    ) {
        this(controller, false);
    }

    public GameScreen(
        GameController controller,
        boolean startPaused
    ) {
        this.controller = controller;
        this.startPaused = startPaused;
    }

    public void preserveControllerOnDispose() {
        preserveControllerOnDispose = true;
    }

    @Override
    public void show() {
        /*
         * The Stage is used only for fixed
         * screen-space interface elements.
         */
        stage = new Stage(
            new ScreenViewport()
        );

        skin = new Skin(
            Gdx.files.internal(
                "ui/uiskin.json"
            )
        );

        batch = new SpriteBatch();

        shapeRenderer = new ShapeRenderer();

        TiledWorld world =
            controller.getWorld();

        mapRenderer =
            new OrthogonalTiledMapRenderer(
                world.getTiledMap(),
                1f
            );

        crackedWallRenderer =
            new CrackedWallRenderer(
                controller.getCrackedWall()
            );
        rainEffect = new RainEffect();

        knightAnimationManager = new KnightAnimationManager();
        huskAnimationManager = new HuskHornheadAnimationManager();
        crawlidAnimationManager = new CrawlidAnimationManager();
        crystalAnimationManager = new CrystalGuardianAnimationManager();
        crystalLaserRenderer = new CrystalGuardianLaserRenderer();
        wingedAnimationManager = new WingedSentryAnimationManager();
        falseKnightAnimationManager = new FalseKnightAnimationManager();
        zoteAnimationManager = new ZoteAnimationManager();
        charmIconTextures = loadCharmIconTextures();

        loadSharpShadowDashFrames();
        loadWhiteDashEffect();
        loadSlashEffects();
        loadAbilityEffectTextures();

        worldCamera = new GameCamera(
            CAMERA_VIEW_WIDTH,
            CAMERA_VIEW_HEIGHT,
            controller.getCurrentRoomBounds()
        );

        worldCamera.resize(
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );

        resetWorldCamera();

        createInterface();
        createPauseMenu();

        if (startPaused) {
            setPauseMenuOpen(true);
            startPaused = false;
        }

        popupObserver =
            new AchievementPopupObserver(
                stage,
                skin,
                controller::text
            );

        controller.addAchievementObserver(
            popupObserver
        );

        Gdx.input.setInputProcessor(stage);
    }

    private void resetWorldCamera() {
        Player player =
            controller.getPlayer();
//for debug
      //  System.out.println("RESET CAMERA");
      //  System.out.println("Player X: " + player.getPosition().x);
       // System.out.println("Player Y: " + player.getPosition().y);
     //   System.out.println("Camera bounds: " + controller.getCurrentCameraBounds());


        worldCamera.reset(
            getPlayerCameraTargetX(player),
            getPlayerCameraTargetY(player)
        );

        //for debug
       // System.out.println("Camera X: " + worldCamera.getCamera().position.x);
       // System.out.println("Camera Y: " + worldCamera.getCamera().position.y);

    }

    private void createInterface() {
        TextButton backButton =
            new TextButton(
                controller.text(
                    "game.returnToMainMenu"
                ),
                skin
            );

        backButton.addListener(
            new ChangeListener() {
                @Override
                public void changed(
                    ChangeEvent event,
                    Actor actor
                ) {
                    controller.returnToMainMenu();
                }
            }
        );

        Table backButtonTable =
            new Table();

        backButtonTable.setFillParent(
            true
        );

        backButtonTable
            .bottom()
            .left();

        backButtonTable.add(backButton)
            .width(240f)
            .height(50f)
            .pad(20f);

        stage.addActor(
            backButtonTable
        );
    }


    private void createPauseMenu() {
        pauseMenuOpen = false;

        pauseOverlay = new Table();
        pauseOverlay.setFillParent(true);
        pauseOverlay.center();
        pauseOverlay.setVisible(false);

        Window window = new Window(
            "PAUSE",
            skin
        );

        window.setMovable(false);
        window.setResizable(false);
        window.pad(22f);

        pauseContent = new Table();
        pauseContent.defaults().pad(6f);

        window.add(pauseContent)
            .width(560f)
            .pad(8f);

        pauseOverlay.add(window)
            .width(620f);

        stage.addActor(pauseOverlay);

        showPauseMainPanel();
    }

    private void handlePauseInput() {
        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
            )
        ) {
            setPauseMenuOpen(
                !pauseMenuOpen
            );
        }
    }

    private void setPauseMenuOpen(
        boolean open
    ) {
        pauseMenuOpen = open;

        if (pauseOverlay != null) {
            pauseOverlay.setVisible(open);
        }

        if (open) {
            showPauseMainPanel();
        }
    }

    private void showPauseMainPanel() {
        if (pauseContent == null) {
            return;
        }

        pauseContent.clear();

        Label title = new Label(
            "Game Paused",
            skin
        );

        title.setFontScale(1.45f);

        pauseContent.add(title)
            .padBottom(12f)
            .row();

        Label status = new Label(
            controller.getCheatStatusLine(),
            skin
        );

        status.setColor(
            0.76f,
            0.86f,
            1f,
            1f
        );

        pauseContent.add(status)
            .width(520f)
            .center()
            .padBottom(8f)
            .row();

        pauseMessageLabel = new Label(
            "",
            skin
        );

        pauseMessageLabel.setWrap(true);
        pauseMessageLabel.setAlignment(
            Align.center
        );

        pauseContent.add(pauseMessageLabel)
            .width(520f)
            .height(36f)
            .row();

        addPauseButton(
            "Continue",
            () -> setPauseMenuOpen(false)
        );

        addPauseButton(
            "Cheat Codes",
            this::showCheatCodesPanel
        );

        addPauseButton(
            "Achievements",
            this::showAchievementsPanel
        );

        addPauseButton(
            "Settings",
            controller::openSettingsMenu
        );

        addPauseButton(
            "Save Game",
            () -> setPauseMessage(
                controller.saveGame()
            )
        );

        addPauseButton(
            "Load Game",
            () -> {
                setPauseMessage(
                    controller.loadGame()
                );
                resetWorldCamera();
            }
        );

        addPauseButton(
            "Save and Exit",
            controller::saveGameAndExit
        );
    }

    private void addPauseButton(
        String text,
        Runnable action
    ) {
        TextButton button = new TextButton(
            text,
            skin
        );

        button.getLabel().setFontScale(1.05f);

        button.addListener(
            new ChangeListener() {
                @Override
                public void changed(
                    ChangeEvent event,
                    Actor actor
                ) {
                    action.run();
                }
            }
        );

        pauseContent.add(button)
            .width(300f)
            .height(46f)
            .row();
    }

    private void setPauseMessage(
        String message
    ) {
        if (pauseMessageLabel != null) {
            pauseMessageLabel.setText(
                message == null ? "" : message
            );
        }
    }

    private void showCheatCodesPanel() {
        pauseContent.clear();

        Label title = new Label(
            "Cheat Codes",
            skin
        );

        title.setFontScale(1.35f);

        pauseContent.add(title)
            .padBottom(12f)
            .row();

        Label cheats = new Label(
            "Left Ctrl + B  : Teleport to False Knight arena\n"
                + "Left Ctrl + F  : Toggle flight / noclip mode\n"
                + "Left Ctrl + H  : Emergency heal (+1 mask)\n"
                + "Left Ctrl + S  : Refill Soul vessel\n"
                + "Left Ctrl + G  : Toggle God Mode\n"
                + "Left Ctrl + K  : Insta-kill enemies on screen",
            skin
        );

        cheats.setWrap(true);
        cheats.setAlignment(Align.left);

        pauseContent.add(cheats)
            .width(500f)
            .padBottom(16f)
            .row();

        addPauseButton(
            "Back",
            this::showPauseMainPanel
        );
    }

    private void showAchievementsPanel() {
        pauseContent.clear();

        Label title = new Label(
            "Achievements",
            skin
        );

        title.setFontScale(1.35f);

        pauseContent.add(title)
            .padBottom(12f)
            .row();

        Table list = new Table();
        list.defaults().pad(5f);

        for (Achievement achievement
            : controller.getAchievements()) {
            boolean unlocked =
                achievement.isUnlocked();

            String line =
                (unlocked ? "✓ " : "🔒 ")
                    + controller.text(
                    achievement.getTitleKey()
                )
                    + " - "
                    + controller.text(
                    achievement.getDescriptionKey()
                );

            Label label = new Label(
                line,
                skin
            );

            label.setWrap(true);

            if (!unlocked) {
                label.setColor(Color.GRAY);
            } else {
                label.setColor(
                    0.70f,
                    1f,
                    0.80f,
                    1f
                );
            }

            list.add(label)
                .width(490f)
                .left()
                .row();
        }

        ScrollPane scrollPane = new ScrollPane(
            list,
            skin
        );

        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(
            true,
            false
        );

        pauseContent.add(scrollPane)
            .width(520f)
            .height(270f)
            .padBottom(14f)
            .row();

        addPauseButton(
            "Back",
            this::showPauseMainPanel
        );
    }

    @Override
    public void render(float delta) {
        handlePauseInput();

        if (!pauseMenuOpen) {
            controller.update(
                delta,
                KNIGHT_DRAW_WIDTH,
                KNIGHT_DRAW_HEIGHT
            );

            if (controller.shouldOpenEndGameScreen()) {
                controller.openEndGameScreen();
                return;
            }

            updateWorldCamera(delta);
        }

        Gdx.gl.glClearColor(
            0.02f,
            0.02f,
            0.05f,
            1f
        );

        Gdx.gl.glClear(
            GL20.GL_COLOR_BUFFER_BIT
        );

        /*
         * Apply the moving camera before drawing
         * world-space objects.
         */
        worldCamera.apply();

        drawMapBackground();
        drawHiddenRoomCover();
        drawCrackedWall();
        drawVoidHeartRewardCharm();

        if (controller.isCurrentRoom("city_of_tears")) {
            drawRain();
        }

        drawHuskHornhead();

        if (DRAW_HUSK_DEBUG) {
            drawHuskHornheadDebug();
        }

        drawCrawlid();

        if (DRAW_CRAWLID_DEBUG) {
            drawCrawlidDebug();
        }

        drawCrystalGuardian();
        drawCrystalGuardianLaser();

        if (DRAW_CRYSTAL_DEBUG) {
            drawCrystalGuardianDebug();
        }
        drawWingedSentry();

        if (DRAW_WINGED_DEBUG) {
            drawWingedSentryDebug();
        }

        drawFalseKnight();
        drawFalseKnightShockwave();

        if (DRAW_FALSE_KNIGHT_DEBUG) {
            drawFalseKnightDebug();
        }

        drawZote();
        drawWhiteDashEffect();
        drawKnight();
        drawShadeSoulCastEffect();
        drawShadeSoulProjectileEffect();
        drawShadeSoulEndEffect();
        drawAbyssShriekEffect();
        drawSlashEffect();
        drawActiveAttackHitbox();
        drawMapForeground();
        drawZotePrompt();

        stage.act(
            Math.min(
                delta,
                1f / 30f
            )
        );

        /*
         * Restore the fixed UI viewport before
         * drawing the HUD and Scene2D actors.
         */
        stage.getViewport().apply();

        if (!pauseMenuOpen) {
            handleCharmInventoryClick();
        }

        drawPlayerHud();
        drawCharmObtainedMessage();
        drawCheatMessage();
        drawZoteDialogueBox();
        drawCharmInventoryMenu();

        stage.draw();

        if (!pauseMenuOpen) {
            finishAnimationIfNecessary();
        }
    }

    private void updateWorldCamera(
        float delta
    ) {
        Player player =
            controller.getPlayer();

        worldCamera.setWorldBounds(
            controller.getCurrentCameraBounds()
        );

        /*
         * The False Knight queues a shake whenever a heavy attack or landing
         * hits the ground. Drain that request here so the strike actually
         * moves the camera, proportional to the requested intensity.
         */
        FalseKnight falseKnight =
            controller.getFalseKnight();

        if (
            falseKnight != null
                && falseKnight.consumeCameraShakeRequest()
        ) {
            worldCamera.shake(
                falseKnight.getPendingShakeIntensity()
            );
        }

        if (controller.consumeGameplayCameraShakeRequest()) {
            worldCamera.shake(
                controller
                    .getPendingGameplayCameraShakeIntensity()
            );
        }

        worldCamera.update(
            Math.min(
                delta,
                1f / 30f
            ),
            getPlayerCameraTargetX(player),
            getPlayerCameraTargetY(player)
        );
    }

    private float getPlayerCameraTargetX(
        Player player
    ) {
        return player.getPosition().x
            + KNIGHT_DRAW_WIDTH / 2f;
    }

    private float getPlayerCameraTargetY(
        Player player
    ) {
        return player.getPosition().y
            + KNIGHT_DRAW_HEIGHT / 2f;
    }

    private void drawMapBackground() {
        mapRenderer.setView(
            worldCamera.getCamera()
        );

        mapRenderer.render(
            controller
                .getWorld()
                .getBackgroundLayerIndices()
        );
    }

    private void drawMapForeground() {
        mapRenderer.setView(
            worldCamera.getCamera()
        );

        mapRenderer.render(
            controller
                .getWorld()
                .getForegroundLayerIndices()
        );
    }

    private void drawHiddenRoomCover() {
        CrackedWall wall =
            controller.getCrackedWall();

        Rectangle hiddenRoom =
            controller
                .getWorld()
                .getHiddenRoomBounds();

        if (
            wall == null
                || wall.isDestroyed()
                || hiddenRoom == null
        ) {
            return;
        }

        shapeRenderer.setProjectionMatrix(
            worldCamera.getCombined()
        );

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        );

        // Blend the hidden-room cover into the Crossroads
        // background instead of showing a pure-black rectangle.
        shapeRenderer.setColor(
            0.020f,
            0.024f,
            0.052f,
            1f
        );

        shapeRenderer.rect(
            hiddenRoom.x,
            hiddenRoom.y,
            hiddenRoom.width,
            hiddenRoom.height
        );

        shapeRenderer.end();
    }

    private void drawCrackedWall() {
        CrackedWall wall =
            controller.getCrackedWall();

        if (wall == null) {
            return;
        }

        batch.setProjectionMatrix(
            worldCamera.getCombined()
        );

        batch.begin();
        crackedWallRenderer.draw(batch, wall);
        batch.end();
    }

    private void drawVoidHeartRewardCharm() {
               if (!controller.shouldDrawVoidHeartRewardCharm()) {
                       return;
                    }

                  if (charmIconTextures == null) {
                        return;
                    }

                    Texture texture =
                        charmIconTextures.get(
                                CharmType.VOID_HEART
                               );

                   if (texture == null) {
                       return;
                 }

                    Rectangle hiddenRoom =
                       controller
                               .getWorld()
                          .getHiddenRoomBounds();

                   if (hiddenRoom == null) {
                       return;
                  }

                   float drawX =
                      hiddenRoom.x
                               + hiddenRoom.width / 2f
                          - VOID_HEART_REWARD_DRAW_SIZE / 2f;

                    float drawY =
                      hiddenRoom.y
                              + VOID_HEART_REWARD_Y_OFFSET;

                  batch.setProjectionMatrix(
                          worldCamera.getCombined()
                           );

                  batch.begin();

                  batch.draw(
                            texture,
                            drawX,
                            drawY,
                            VOID_HEART_REWARD_DRAW_SIZE,
                            VOID_HEART_REWARD_DRAW_SIZE
                           );

            batch.end();
    }

    private void drawRain() {

        batch.setProjectionMatrix(
            worldCamera.getCombined()
        );

        batch.begin();

        rainEffect.update(
            Gdx.graphics.getDeltaTime()
        );

        rainEffect.draw(batch);

        batch.end();
    }

    private void drawHuskHornhead() {
        HuskHornhead husk =
            controller.getHuskHornhead();

        if (husk == null) {
            return;
        }

        TextureRegion frame =
            huskAnimationManager.getFrame(
                husk.getAnimationType(),
                husk.getAnimationTime()
            );

        Rectangle body =
            husk.getBounds();

        /*
         * Center the larger source frame around the
         * smaller physical collision body.
         */
        float drawX =
            body.x
                + body.width / 2f
                - HUSK_DRAW_WIDTH / 2f;

        float drawY =
            body.y - 7f;

        batch.setProjectionMatrix(
            worldCamera.getCombined()
        );

        batch.begin();

        if (husk.isFlashing()) {
            batch.setColor(
                1f,
                0.55f,
                0.55f,
                1f
            );
        }

        boolean shouldFlip =
            husk.isFacingRight()
                != HUSK_SOURCE_FACES_RIGHT;

        if (shouldFlip) {
            batch.draw(
                frame,
                drawX + HUSK_DRAW_WIDTH,
                drawY,
                -HUSK_DRAW_WIDTH,
                HUSK_DRAW_HEIGHT
            );
        } else {
            batch.draw(
                frame,
                drawX,
                drawY,
                HUSK_DRAW_WIDTH,
                HUSK_DRAW_HEIGHT
            );
        }

        batch.setColor(
            Color.WHITE
        );

        batch.end();
    }

    private void drawHuskHornheadDebug() {
        HuskHornhead husk =
            controller.getHuskHornhead();

        if (husk == null) {
            return;
        }

        shapeRenderer.setProjectionMatrix(
            worldCamera.getCombined()
        );

        Gdx.gl.glLineWidth(2f);

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        );

        /*
         * Yellow: physical body.
         */
        shapeRenderer.setColor(
            Color.YELLOW
        );

        Rectangle body =
            husk.getBounds();

        shapeRenderer.rect(
            body.x,
            body.y,
            body.width,
            body.height
        );

        /*
         * Green: forward field of vision.
         */
        if (husk.isAlive()) {
            shapeRenderer.setColor(
                Color.GREEN
            );

            Rectangle vision =
                husk.getVisionBounds();

            shapeRenderer.rect(
                vision.x,
                vision.y,
                vision.width,
                vision.height
            );
        }

        shapeRenderer.end();

        Gdx.gl.glLineWidth(1f);
    }

    private void drawCrawlid() {
        Crawlid crawlid =
            controller.getCrawlid();

        if (crawlid == null) {
            return;
        }

        TextureRegion frame =
            crawlidAnimationManager.getFrame(
                crawlid.getAnimationType(),
                crawlid.getAnimationTime()
            );

        Rectangle body =
            crawlid.getBounds();

        /*
         * Use one scale for all Crawlid actions.
         * This prevents the differently sized death
         * canvases from being stretched.
         */
        float drawWidth =
            frame.getRegionWidth()
                * CRAWLID_DRAW_SCALE;

        float drawHeight =
            frame.getRegionHeight()
                * CRAWLID_DRAW_SCALE;

        float drawX =
            body.x
                + body.width / 2f
                - drawWidth / 2f;

        float drawY =
            body.y - 2f;

        batch.setProjectionMatrix(
            worldCamera.getCombined()
        );

        batch.begin();

        if (crawlid.isFlashing()) {
            batch.setColor(
                1f,
                0.55f,
                0.55f,
                1f
            );
        }

        boolean shouldFlip =
            crawlid.isFacingRight()
                != CRAWLID_SOURCE_FACES_RIGHT;

        if (shouldFlip) {
            batch.draw(
                frame,
                drawX + drawWidth,
                drawY,
                -drawWidth,
                drawHeight
            );
        } else {
            batch.draw(
                frame,
                drawX,
                drawY,
                drawWidth,
                drawHeight
            );
        }

        batch.setColor(
            Color.WHITE
        );

        batch.end();
    }

    private void drawCrawlidDebug() {
        Crawlid crawlid =
            controller.getCrawlid();

        if (crawlid == null) {
            return;
        }

        Rectangle body =
            crawlid.getBounds();

        shapeRenderer.setProjectionMatrix(
            worldCamera.getCombined()
        );

        Gdx.gl.glLineWidth(2f);

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        );

        shapeRenderer.setColor(
            Color.YELLOW
        );

        shapeRenderer.rect(
            body.x,
            body.y,
            body.width,
            body.height
        );

        shapeRenderer.end();

        Gdx.gl.glLineWidth(1f);
    }

    private void drawCrystalGuardianLaser() {
        CrystalGuardian guardian =
            controller.getCrystalGuardian();

        if (guardian == null) {
            return;
        }

        if (!guardian.isLaserActive()) {
            return;
        }

        crystalLaserRenderer.draw(
            batch,
            guardian.getLaserBounds(),
            guardian.getLaserDirection(),
            guardian.getLaserActiveTime(),
            worldCamera.getCombined()
        );
    }

    private void drawCrystalGuardian() {
        CrystalGuardian guardian =
            controller.getCrystalGuardian();

        if (guardian == null) {
            return;
        }

        TextureRegion frame =
            crystalAnimationManager.getFrame(
                guardian.getAnimationType(),
                guardian.getAnimationTime()
            );

        Rectangle body =
            guardian.getBounds();

        float drawWidth =
            frame.getRegionWidth()
                * CRYSTAL_DRAW_SCALE;

        float drawHeight =
            frame.getRegionHeight()
                * CRYSTAL_DRAW_SCALE;

        float drawX =
            body.x
                + body.width / 2f
                - drawWidth / 2f;

        float drawY =
            body.y - 7f;

        batch.setProjectionMatrix(
            worldCamera.getCombined()
        );

        batch.begin();

        if (guardian.isFlashing()) {
            batch.setColor(
                1f,
                0.55f,
                0.55f,
                1f
            );
        }

        boolean shouldFlip =
            guardian.isFacingRight()
                != CRYSTAL_SOURCE_FACES_RIGHT;

        if (shouldFlip) {
            batch.draw(
                frame,
                drawX + drawWidth,
                drawY,
                -drawWidth,
                drawHeight
            );
        } else {
            batch.draw(
                frame,
                drawX,
                drawY,
                drawWidth,
                drawHeight
            );
        }

        batch.setColor(Color.WHITE);

        batch.end();
    }

    private void drawCrystalGuardianDebug() {
        CrystalGuardian guardian =
            controller.getCrystalGuardian();

        if (guardian == null) {
            return;
        }

        shapeRenderer.setProjectionMatrix(
            worldCamera.getCombined()
        );

        Gdx.gl.glLineWidth(2f);

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        );

        Rectangle body =
            guardian.getBounds();

        shapeRenderer.setColor(
            Color.YELLOW
        );

        shapeRenderer.rect(
            body.x,
            body.y,
            body.width,
            body.height
        );

        if (guardian.isAlive()) {
            Rectangle vision =
                guardian.getVisionBounds();

            shapeRenderer.setColor(
                Color.GREEN
            );

            shapeRenderer.rect(
                vision.x,
                vision.y,
                vision.width,
                vision.height
            );
        }

        if (guardian.isLaserActive()) {
            Rectangle laser =
                guardian.getLaserBounds();

            shapeRenderer.setColor(
                Color.MAGENTA
            );

            shapeRenderer.rect(
                laser.x,
                laser.y,
                laser.width,
                laser.height
            );
        }

        shapeRenderer.end();

        Gdx.gl.glLineWidth(1f);
    }

    private void drawWingedSentry() {
        WingedSentry sentry =
            controller.getWingedSentry();

        if (sentry == null) {
            return;
        }

        TextureRegion frame =
            wingedAnimationManager.getFrame(
                sentry.getAnimationType(),
                sentry.getAnimationTime()
            );

        Rectangle body =
            sentry.getBounds();

        float drawWidth =
            frame.getRegionWidth()
                * WINGED_DRAW_SCALE;

        float drawHeight =
            frame.getRegionHeight()
                * WINGED_DRAW_SCALE;

        /*
         * This offset aligns the visible flying body
         * with its gameplay rectangle.
         */
        float drawX =
            body.x
                + body.width / 2f
                - drawWidth / 2f;

        float drawY =
            body.y - 72f;

        batch.setProjectionMatrix(
            worldCamera.getCombined()
        );

        batch.begin();

        if (sentry.isFlashing()) {
            batch.setColor(
                1f,
                0.55f,
                0.55f,
                1f
            );
        }

        boolean shouldFlip =
            sentry.isFacingRight()
                != WINGED_SOURCE_FACES_RIGHT;

        if (shouldFlip) {
            batch.draw(
                frame,
                drawX + drawWidth,
                drawY,
                -drawWidth,
                drawHeight
            );
        } else {
            batch.draw(
                frame,
                drawX,
                drawY,
                drawWidth,
                drawHeight
            );
        }

        batch.setColor(Color.WHITE);

        batch.end();
    }


    private void drawWingedSentryDebug() {
        WingedSentry sentry =
            controller.getWingedSentry();

        if (sentry == null) {
            return;
        }

        shapeRenderer.setProjectionMatrix(
            worldCamera.getCombined()
        );

        Gdx.gl.glLineWidth(2f);

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        );

        Rectangle body =
            sentry.getBounds();

        /*
         * Yellow: physical collision body.
         */
        shapeRenderer.setColor(
            Color.YELLOW
        );

        shapeRenderer.rect(
            body.x,
            body.y,
            body.width,
            body.height
        );

        /*
         * Green: detection region.
         */
        if (sentry.isAlive()) {
            Rectangle detection =
                sentry.getDetectionBounds();

            shapeRenderer.setColor(
                Color.GREEN
            );

            shapeRenderer.rect(
                detection.x,
                detection.y,
                detection.width,
                detection.height
            );
        }

        /*
         * Cyan: the exact height captured when the
         * Knight was first detected.
         */
        if (sentry.isAttackHeightLocked()) {
            float lockedY =
                sentry.getLockedChargeCenterY();

            shapeRenderer.setColor(
                Color.CYAN
            );

            Rectangle roomBounds =
                controller.getCurrentRoomBounds();

            shapeRenderer.line(
                roomBounds.x,
                lockedY,
                roomBounds.x + roomBounds.width,
                lockedY
            );
        }

        shapeRenderer.end();

        Gdx.gl.glLineWidth(1f);
    }

    private void drawFalseKnight() {
        FalseKnight falseKnight =
            controller.getFalseKnight();

        if (falseKnight == null) {
            return;
        }

        TextureRegion frame =
            falseKnightAnimationManager.getFrame(
                falseKnight
            );

        Rectangle body =
            falseKnight.getBounds();

        float scale =
            FALSE_KNIGHT_DRAW_HEIGHT
                / frame.getRegionHeight();

        float drawWidth =
            frame.getRegionWidth()
                * scale;

        float drawHeight =
            frame.getRegionHeight()
                * scale;

        /*
         * Center the big visual sprite around the smaller
         * physical boss body.
         */
        float drawX =
            body.x
                + body.width / 2f
                - drawWidth / 2f;

        float drawY =
            body.y - 18f;

        batch.setProjectionMatrix(
            worldCamera.getCombined()
        );

        batch.begin();

        boolean shouldFlip =
            falseKnight.isFacingRight()
                != FALSE_KNIGHT_SOURCE_FACES_RIGHT;

        if (shouldFlip) {
            batch.draw(
                frame,
                drawX + drawWidth,
                drawY,
                -drawWidth,
                drawHeight
            );
        } else {
            batch.draw(
                frame,
                drawX,
                drawY,
                drawWidth,
                drawHeight
            );
        }

        batch.end();
    }

    private void drawFalseKnightShockwave() {
        FalseKnight falseKnight =
            controller.getFalseKnight();

        if (
            falseKnight == null
                || !falseKnight.isShockwaveActive()
        ) {
            return;
        }

        Rectangle shockwave =
            falseKnight.getShockwaveHitbox();

        TextureRegion frame =
            falseKnightAnimationManager.getShockwaveFrame(
                falseKnight.getShockwaveAnimationTime()
            );

        float scale =
            SHOCKWAVE_DRAW_HEIGHT
                / frame.getRegionHeight();

        float drawWidth =
            frame.getRegionWidth() * scale;

        float drawHeight =
            frame.getRegionHeight() * scale;

        /*
         * Anchor the wave on the ground and centre it on the moving hitbox so
         * the visual travels with the collision box.
         */
        float centerX =
            shockwave.x + shockwave.width / 2f;

        float drawX =
            centerX - drawWidth / 2f;

        float drawY =
            shockwave.y - 6f;

        batch.setProjectionMatrix(
            worldCamera.getCombined()
        );

        batch.begin();

        /*
         * Source frames face right; flip horizontally when the wave travels
         * to the left so it always leads with its crest.
         */
        boolean shouldFlip =
            !falseKnight.isShockwaveMovingRight();

        if (shouldFlip) {
            batch.draw(
                frame,
                drawX + drawWidth,
                drawY,
                -drawWidth,
                drawHeight
            );
        } else {
            batch.draw(
                frame,
                drawX,
                drawY,
                drawWidth,
                drawHeight
            );
        }

        batch.end();
    }
    private void drawFalseKnightDebug() {
        FalseKnight falseKnight =
            controller.getFalseKnight();

        if (falseKnight == null) {
            return;
        }

        shapeRenderer.setProjectionMatrix(
            worldCamera.getCombined()
        );

        Gdx.gl.glLineWidth(2f);

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        );

        /*
         * Yellow: boss body.
         */
        shapeRenderer.setColor(
            Color.YELLOW
        );

        Rectangle body =
            falseKnight.getBounds();

        shapeRenderer.rect(
            body.x,
            body.y,
            body.width,
            body.height
        );

        /*
         * Red: mace hitbox.
         */
        if (falseKnight.isMaceHitActive()) {
            shapeRenderer.setColor(
                Color.RED
            );

            Rectangle mace =
                falseKnight.getMaceHitbox();

            shapeRenderer.rect(
                mace.x,
                mace.y,
                mace.width,
                mace.height
            );
        }

        /*
         * Cyan: vulnerable body during stun.
         */
        if (falseKnight.isStunned()) {
            shapeRenderer.setColor(
                Color.CYAN
            );

            Rectangle vulnerable =
                falseKnight.getVulnerableHitbox();

            shapeRenderer.rect(
                vulnerable.x,
                vulnerable.y,
                vulnerable.width,
                vulnerable.height
            );
        }

        /*
         * Blue: shockwave hitbox.
         */
        if (falseKnight.isShockwaveActive()) {
            shapeRenderer.setColor(
                Color.BLUE
            );

            Rectangle shockwave =
                falseKnight.getShockwaveHitbox();

            shapeRenderer.rect(
                shockwave.x,
                shockwave.y,
                shockwave.width,
                shockwave.height
            );
        }

        shapeRenderer.end();

        Gdx.gl.glLineWidth(1f);
    }

    private void drawZote() {
        Zote zote =
            controller.getZote();

        if (zote == null) {
            return;
        }

        TextureRegion frame =
            zoteAnimationManager.getFrame(
                zote
            );

        Rectangle body =
            zote.getBounds();

        float drawX =
            body.x
                + body.width / 2f
                - ZOTE_DRAW_WIDTH / 2f;

        float drawY =
            body.y - 18f;

        boolean shouldFlip =
            zote.isFacingRight()
                != ZOTE_SOURCE_FACES_RIGHT;

        batch.setProjectionMatrix(
            worldCamera.getCombined()
        );

        batch.begin();

        if (shouldFlip) {
            batch.draw(
                frame,
                drawX + ZOTE_DRAW_WIDTH,
                drawY,
                -ZOTE_DRAW_WIDTH,
                ZOTE_DRAW_HEIGHT
            );
        } else {
            batch.draw(
                frame,
                drawX,
                drawY,
                ZOTE_DRAW_WIDTH,
                ZOTE_DRAW_HEIGHT
            );
        }

        batch.end();
    }

    private void drawZotePrompt() {
        if (!controller.shouldShowZotePrompt()) {
            return;
        }

        Zote zote =
            controller.getZote();

        if (zote == null) {
            return;
        }

        Rectangle body =
            zote.getBounds();

        float promptWidth = 86f;
        float promptHeight = 42f;

        float promptX =
            body.x
                + body.width / 2f
                - promptWidth / 2f;

        float promptY =
            body.y
                + body.height
                + 48f;

        Gdx.gl.glEnable(
            GL20.GL_BLEND
        );

        Gdx.gl.glBlendFunc(
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE_MINUS_SRC_ALPHA
        );

        shapeRenderer.setProjectionMatrix(
            worldCamera.getCombined()
        );

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        );

        shapeRenderer.setColor(
            0f,
            0f,
            0f,
            0.72f
        );

        shapeRenderer.rect(
            promptX,
            promptY,
            promptWidth,
            promptHeight
        );

        shapeRenderer.end();

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        );

        shapeRenderer.setColor(
            0.85f,
            0.88f,
            1f,
            1f
        );

        shapeRenderer.rect(
            promptX,
            promptY,
            promptWidth,
            promptHeight
        );

        shapeRenderer.end();

        Gdx.gl.glDisable(
            GL20.GL_BLEND
        );

        batch.setProjectionMatrix(
            worldCamera.getCombined()
        );

        batch.begin();

        BitmapFont font =
            skin.getFont(
                "font"
            );

        font.setColor(
            Color.WHITE
        );

        font.draw(
            batch,
            "E / UP",
            promptX + 13f,
            promptY + 28f
        );

        batch.end();
    }

    private void drawZoteDialogueBox() {
        if (!controller.isZoteDialogueActive()) {
            return;
        }

        String dialogueLine =
            controller.getCurrentZoteDialogueLine();

        float screenWidth =
            stage
                .getViewport()
                .getWorldWidth();

        float screenHeight =
            stage
                .getViewport()
                .getWorldHeight();

        float boxX = 80f;
        float boxHeight = 166f;
        float boxY = screenHeight - boxHeight - 42f;
        float boxWidth = screenWidth - 160f;

        Gdx.gl.glEnable(
            GL20.GL_BLEND
        );

        Gdx.gl.glBlendFunc(
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE_MINUS_SRC_ALPHA
        );

        shapeRenderer.setProjectionMatrix(
            stage.getCamera().combined
        );

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        );

        shapeRenderer.setColor(
            0f,
            0f,
            0f,
            0.82f
        );

        shapeRenderer.rect(
            boxX,
            boxY,
            boxWidth,
            boxHeight
        );

        shapeRenderer.end();

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        );

        shapeRenderer.setColor(
            0.78f,
            0.80f,
            0.92f,
            1f
        );

        shapeRenderer.rect(
            boxX,
            boxY,
            boxWidth,
            boxHeight
        );

        shapeRenderer.end();

        Gdx.gl.glDisable(
            GL20.GL_BLEND
        );

        batch.setProjectionMatrix(
            stage.getCamera().combined
        );

        batch.begin();

        BitmapFont titleFont =
            skin.getFont(
                "window"
            );

        BitmapFont textFont =
            skin.getFont(
                "font"
            );

        titleFont.setColor(
            0.90f,
            0.90f,
            1f,
            1f
        );

        titleFont.draw(
            batch,
            "Zote the Mighty",
            boxX + 24f,
            boxY + boxHeight - 24f
        );

        textFont.setColor(
            Color.WHITE
        );

        textFont.draw(
            batch,
            dialogueLine,
            boxX + 24f,
            boxY + boxHeight - 62f,
            boxWidth - 48f,
            Align.left,
            true
        );

        textFont.setColor(
            0.78f,
            0.80f,
            0.92f,
            1f
        );

        textFont.draw(
            batch,
            "ENTER",
            boxX + boxWidth - 94f,
            boxY + 26f
        );

        batch.end();
    }
    private void drawWhiteDashEffect() {
        if (
            !isDashVisualActive()
                || whiteDashEffectFrames == null
        ) {
            return;
        }

        Player player =
            controller.getPlayer();

        float dashVisualTime =
            player.getAnimationTime();

        if (dashVisualTime > WHITE_DASH_EFFECT_DURATION) {
            return;
        }

        TextureRegion frame =
            getFrameFromElapsedTime(
                whiteDashEffectFrames,
                dashVisualTime,
                WHITE_DASH_EFFECT_DURATION
                    / WHITE_DASH_EFFECT_FRAME_COUNT,
                false
            );

        if (frame == null) {
            return;
        }

        float dashAlpha =
            WHITE_DASH_EFFECT_ALPHA
                * (1f - dashVisualTime / WHITE_DASH_EFFECT_DURATION);

        float scale =
            WHITE_DASH_EFFECT_DRAW_HEIGHT
                / frame.getRegionHeight();

        float drawWidth =
            frame.getRegionWidth()
                * scale;

        float drawHeight =
            frame.getRegionHeight()
                * scale;

        float drawX =
            player.getPosition().x
                + KNIGHT_DRAW_WIDTH / 2f
                - drawWidth / 2f;

        float drawY =
            player.getPosition().y
                + KNIGHT_DRAW_HEIGHT / 2f
                - drawHeight / 2f;

        boolean shouldFlip =
            player.isFacingRight()
                != WHITE_DASH_EFFECT_SOURCE_FACES_RIGHT;

        batch.setProjectionMatrix(
            worldCamera.getCombined()
        );

        batch.begin();

        batch.setColor(
            1f,
            1f,
            1f,
            dashAlpha
        );

        if (shouldFlip) {
            batch.draw(
                frame,
                drawX + drawWidth,
                drawY,
                -drawWidth,
                drawHeight
            );
        } else {
            batch.draw(
                frame,
                drawX,
                drawY,
                drawWidth,
                drawHeight
            );
        }

        batch.setColor(Color.WHITE);
        batch.end();
    }

    private void drawSlashEffect() {
        if (!controller.isNailSlashActive()) {
            return;
        }

        float attackTime =
            controller.getCurrentAttackTime();

        if (attackTime > SLASH_EFFECT_DURATION) {
            return;
        }

        AttackDirection direction =
            controller.getCurrentAttackDirection();

        TextureRegion frame =
            getSlashEffectFrame(
                attackTime,
                direction
            );

        if (frame == null) {
            return;
        }

        Rectangle hitbox =
            controller.getAttackHitbox();

        if (
            hitbox.width <= 0f
                || hitbox.height <= 0f
        ) {
            return;
        }

        boolean vertical =
            direction == AttackDirection.UP
                || direction == AttackDirection.DOWN;

        float requestedDrawHeight =
            vertical
                ? SLASH_EFFECT_VERTICAL_DRAW_HEIGHT
                : SLASH_EFFECT_HORIZONTAL_DRAW_HEIGHT;

        float scale =
            requestedDrawHeight
                / frame.getRegionHeight();

        float drawWidth =
            frame.getRegionWidth()
                * scale;

        float drawHeight =
            frame.getRegionHeight()
                * scale;

        float drawX =
            hitbox.x
                + hitbox.width / 2f
                - drawWidth / 2f;

        float drawY =
            hitbox.y
                + hitbox.height / 2f
                - drawHeight / 2f;

        if (direction == AttackDirection.RIGHT) {
            drawX += 8f;
        } else if (direction == AttackDirection.LEFT) {
            drawX -= 8f;
        } else if (direction == AttackDirection.UP) {
            drawY += 10f;
        } else if (direction == AttackDirection.DOWN) {
            drawY -= 10f;
        }

        float slashAlpha =
            SLASH_EFFECT_ALPHA
                * (1f - attackTime / SLASH_EFFECT_DURATION);

        boolean shouldFlip =
            direction == AttackDirection.LEFT
                ? SLASH_EFFECT_HORIZONTAL_SOURCE_FACES_RIGHT
                : direction == AttackDirection.RIGHT
                    && !SLASH_EFFECT_HORIZONTAL_SOURCE_FACES_RIGHT;

        batch.setProjectionMatrix(
            worldCamera.getCombined()
        );

        batch.begin();

        batch.setColor(
            1f,
            1f,
            1f,
            slashAlpha
        );

        if (shouldFlip) {
            batch.draw(
                frame,
                drawX + drawWidth,
                drawY,
                -drawWidth,
                drawHeight
            );
        } else {
            batch.draw(
                frame,
                drawX,
                drawY,
                drawWidth,
                drawHeight
            );
        }

        batch.setColor(Color.WHITE);
        batch.end();
    }

    private TextureRegion getSlashEffectFrame(
        float attackTime,
        AttackDirection direction
    ) {
        TextureRegion[] frames;

        if (direction == AttackDirection.UP) {
            frames = slashUpEffectFrames;
        } else if (direction == AttackDirection.DOWN) {
            frames = slashDownEffectFrames;
        } else {
            frames = slashHorizontalEffectFrames;
        }

        if (
            frames == null
                || frames.length == 0
        ) {
            return null;
        }

        int frameIndex = Math.min(
            frames.length - 1,
            Math.max(
                0,
                (int)(attackTime / SLASH_EFFECT_FRAME_DURATION)
            )
        );

        return frames[frameIndex];
    }


    private void drawShadeSoulCastEffect() {
        if (!controller.isShadeSoulCastActive()) {
            return;
        }

        drawCenteredAbilityFrame(
            getFrameFromProgress(
                blastSoulFrames,
                controller.getShadeSoulCastProgress()
            ),
            controller.getShadeSoulCastBounds(),
            SHADE_SOUL_CAST_EFFECT_HEIGHT,
            controller.isVoidShadeSoulFacingRight(),
            SHADE_SOUL_SOURCE_FACES_RIGHT,
            0.95f,
            controller.getShadeSoulCastProgress()
        );
    }

    private void drawShadeSoulProjectileEffect() {
        if (!controller.isVoidShadeSoulActive()) {
            return;
        }

        /*
         * Let the blast animation read clearly first.
         * The projectile still exists in the controller, but it is not drawn
         * over the blast during the short cast window.
         */
        if (controller.isShadeSoulCastActive()) {
            return;
        }

        TextureRegion[] projectileFrames =
            controller.isCurrentShadeSoulVoidVariant()
                ? shadowBallFrames
                : soulBallFrames;

        drawCenteredAbilityFrame(
            getFrameFromElapsedTime(
                projectileFrames,
                (1f - clamp01(controller.getVoidShadeSoulProgress()))
                    * SHADE_SOUL_PROJECTILE_VISUAL_DURATION,
                SHADE_SOUL_PROJECTILE_FRAME_DURATION,
                true
            ),
            controller.getVoidShadeSoulBounds(),
            SHADE_SOUL_PROJECTILE_EFFECT_HEIGHT,
            controller.isVoidShadeSoulFacingRight(),
            SHADE_SOUL_SOURCE_FACES_RIGHT,
            0.92f,
            1f
        );
    }

    private void drawShadeSoulEndEffect() {
        if (!controller.isShadeSoulEndActive()) {
            return;
        }

        TextureRegion[] endFrames =
            controller.isCurrentShadeSoulEndVoidVariant()
                ? shadowBallEndFrames
                : soulBallEndFrames;

        drawCenteredAbilityFrame(
            getFrameFromProgress(
                endFrames,
                controller.getShadeSoulEndProgress()
            ),
            controller.getShadeSoulEndBounds(),
            SHADE_SOUL_END_EFFECT_HEIGHT,
            controller.isVoidShadeSoulFacingRight(),
            SHADE_SOUL_SOURCE_FACES_RIGHT,
            0.95f,
            controller.getShadeSoulEndProgress()
        );
    }

    private void drawAbyssShriekEffect() {
        if (!controller.isVoidAbyssShriekActive()) {
            return;
        }

        TextureRegion[] screamFrames =
            controller.isCurrentAbyssShriekVoidVariant()
                ? shadowScreamFrames
                : soulScreamFrames;

        drawCenteredAbilityFrame(
            getFrameFromProgress(
                screamFrames,
                controller.getVoidAbyssShriekProgress()
            ),
            controller.getVoidAbyssShriekBounds(),
            ABYSS_SHRIEK_EFFECT_HEIGHT,
            true,
            true,
            0.94f,
            controller.getVoidAbyssShriekProgress()
        );
    }

    private void drawCenteredAbilityFrame(
        TextureRegion frame,
        Rectangle bounds,
        float targetHeight,
        boolean facingRight,
        boolean sourceFacesRight,
        float maxAlpha,
        float progress
    ) {
        if (frame == null || bounds == null) {
            return;
        }

        float scale =
            targetHeight / frame.getRegionHeight();

        float drawWidth =
            frame.getRegionWidth() * scale;

        float drawHeight =
            frame.getRegionHeight() * scale;

        float drawX =
            bounds.x
                + bounds.width / 2f
                - drawWidth / 2f;

        float drawY =
            bounds.y
                + bounds.height / 2f
                - drawHeight / 2f;

        boolean shouldFlip =
            facingRight != sourceFacesRight;

        float alpha =
            Math.max(0.2f, Math.min(1f, progress))
                * maxAlpha;

        batch.setProjectionMatrix(
            worldCamera.getCombined()
        );

        batch.begin();

        batch.setColor(
            1f,
            1f,
            1f,
            alpha
        );

        if (shouldFlip) {
            batch.draw(
                frame,
                drawX + drawWidth,
                drawY,
                -drawWidth,
                drawHeight
            );
        } else {
            batch.draw(
                frame,
                drawX,
                drawY,
                drawWidth,
                drawHeight
            );
        }

        batch.setColor(Color.WHITE);

        batch.end();
    }

    private TextureRegion getFrameFromProgress(
        TextureRegion[] frames,
        float progress
    ) {
        if (
            frames == null
                || frames.length == 0
        ) {
            return null;
        }

        float elapsedProgress =
            1f - clamp01(progress);

        int frameIndex = Math.min(
            frames.length - 1,
            Math.max(
                0,
                (int)(elapsedProgress * frames.length)
            )
        );

        return frames[frameIndex];
    }

    private TextureRegion getFrameFromElapsedTime(
        TextureRegion[] frames,
        float elapsedTime,
        float frameDuration,
        boolean loop
    ) {
        if (
            frames == null
                || frames.length == 0
        ) {
            return null;
        }

        if (frameDuration <= 0f) {
            return frames[0];
        }

        int frameIndex =
            (int)(Math.max(0f, elapsedTime) / frameDuration);

        if (loop) {
            frameIndex %= frames.length;
        } else if (frameIndex >= frames.length) {
            frameIndex = frames.length - 1;
        }

        return frames[frameIndex];
    }

    private float clamp01(
        float value
    ) {
        return Math.max(
            0f,
            Math.min(
                1f,
                value
            )
        );
    }

    private boolean isDashVisualActive() {
        Player player =
            controller.getPlayer();

        return player.getAnimationType()
            == PlayerAnimationType.DASH;
    }

    private void drawKnight() {
        if (
            !controller.shouldDrawPlayer()
        ) {
            return;
        }

        Player player =
            controller.getPlayer();

        boolean usingSharpShadow =
            controller.isSharpShadowDashVisualActive();

        TextureRegion frame;

        boolean sourceFacesRight;

        if (usingSharpShadow) {
            frame =
                getSharpShadowDashFrame(
                    player.getAnimationTime()
                );

            sourceFacesRight =
                SHARP_SHADOW_SOURCE_FACES_RIGHT;

            if (frame == null) {
                frame =
                    knightAnimationManager.getFrame(
                        player.getAnimationType(),
                        player.getAnimationTime()
                    );

                sourceFacesRight =
                    KNIGHT_SOURCE_FACES_RIGHT;
            }
        } else {
            frame =
                knightAnimationManager.getFrame(
                    player.getAnimationType(),
                    player.getAnimationTime()
                );

            sourceFacesRight =
                KNIGHT_SOURCE_FACES_RIGHT;
        }

        batch.setProjectionMatrix(
            worldCamera.getCombined()
        );

        batch.begin();

        drawPlayerSizedFrame(
            frame,
            player.getPosition().x,
            player.getPosition().y,
            player.isFacingRight(),
            sourceFacesRight
        );

        batch.setColor(Color.WHITE);

        batch.end();
    }

    private void drawPlayerSizedFrame(
        TextureRegion frame,
        float x,
        float y,
        boolean facingRight,
        boolean sourceFacesRight
    ) {
        boolean shouldFlip =
            facingRight != sourceFacesRight;

        if (shouldFlip) {
            batch.draw(
                frame,
                x + KNIGHT_DRAW_WIDTH,
                y,
                -KNIGHT_DRAW_WIDTH,
                KNIGHT_DRAW_HEIGHT
            );
        } else {
            batch.draw(
                frame,
                x,
                y,
                KNIGHT_DRAW_WIDTH,
                KNIGHT_DRAW_HEIGHT
            );
        }
    }

    private void drawActiveAttackHitbox() {
        if (
            !controller
                .isAttackHitboxActive()
        ) {
            return;
        }

        Rectangle hitbox =
            controller.getAttackHitbox();

        shapeRenderer.setProjectionMatrix(
            worldCamera.getCombined()
        );

        Gdx.gl.glLineWidth(2f);

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        );

        shapeRenderer.setColor(
            Color.RED
        );

        shapeRenderer.rect(
            hitbox.x,
            hitbox.y,
            hitbox.width,
            hitbox.height
        );

        shapeRenderer.end();

        Gdx.gl.glLineWidth(1f);
    }

    private EnumMap<CharmType, Texture> loadCharmIconTextures() {
        EnumMap<CharmType, Texture> result =
            new EnumMap<>(
                CharmType.class
            );

        for (CharmType charm : CharmType.values()) {
            if (
                !Gdx.files
                    .internal(
                        charm.getIconPath()
                    )
                    .exists()
            ) {
                continue;
            }

            Texture texture =
                new Texture(
                    Gdx.files.internal(
                        charm.getIconPath()
                    )
                );

            texture.setFilter(
                Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear
            );

            result.put(
                charm,
                texture
            );
        }

        return result;
    }

    private Texture loadEffectTexture(
        String path
    ) {
        Texture texture =
            new Texture(
                Gdx.files.internal(path)
            );

        texture.setFilter(
            Texture.TextureFilter.Linear,
            Texture.TextureFilter.Linear
        );

        return texture;
    }

    private TextureRegion[] splitHorizontalSheet(
        Texture texture,
        int frameCount
    ) {
        if (
            texture == null
                || frameCount <= 0
        ) {
            return new TextureRegion[0];
        }

        TextureRegion[] frames =
            new TextureRegion[frameCount];

        int baseFrameWidth =
            Math.max(
                1,
                texture.getWidth() / frameCount
            );

        for (
            int index = 0;
            index < frameCount;
            index++
        ) {
            int x =
                index * baseFrameWidth;

            int frameWidth =
                index == frameCount - 1
                    ? texture.getWidth() - x
                    : baseFrameWidth;

            frames[index] =
                new TextureRegion(
                    texture,
                    x,
                    0,
                    Math.max(1, frameWidth),
                    texture.getHeight()
                );
        }

        return frames;
    }

    private void loadAbilityEffectTextures() {
        blastSoulTexture = loadEffectTexture(
            "sprites/effects/abilities/blast_soul.png"
        );
        blastSoulFrames = splitHorizontalSheet(
            blastSoulTexture,
            BLAST_SOUL_FRAME_COUNT
        );

        soulBallTexture = loadEffectTexture(
            "sprites/effects/abilities/soul_ball.png"
        );
        soulBallFrames = splitHorizontalSheet(
            soulBallTexture,
            SOUL_BALL_FRAME_COUNT
        );

        soulBallEndTexture = loadEffectTexture(
            "sprites/effects/abilities/soul_ball_end.png"
        );
        soulBallEndFrames = splitHorizontalSheet(
            soulBallEndTexture,
            SOUL_BALL_END_FRAME_COUNT
        );

        shadowBallTexture = loadEffectTexture(
            "sprites/effects/abilities/shadow_ball.png"
        );
        shadowBallFrames = splitHorizontalSheet(
            shadowBallTexture,
            SHADOW_BALL_FRAME_COUNT
        );

        shadowBallEndTexture = loadEffectTexture(
            "sprites/effects/abilities/shadow_ball_end.png"
        );
        shadowBallEndFrames = splitHorizontalSheet(
            shadowBallEndTexture,
            SHADOW_BALL_END_FRAME_COUNT
        );

        soulScreamTexture = loadEffectTexture(
            "sprites/effects/abilities/soul_scream.png"
        );
        soulScreamFrames = splitHorizontalSheet(
            soulScreamTexture,
            SOUL_SCREAM_FRAME_COUNT
        );

        shadowScreamTexture = loadEffectTexture(
            "sprites/effects/abilities/shadow_scream.png"
        );
        shadowScreamFrames = splitHorizontalSheet(
            shadowScreamTexture,
            SHADOW_SCREAM_FRAME_COUNT
        );
    }

    private void loadSharpShadowDashFrames() {
        sharpShadowDashTextures =
            new Texture[
                SHARP_SHADOW_DASH_FRAME_COUNT
                ];

        sharpShadowDashFrames =
            new TextureRegion[
                SHARP_SHADOW_DASH_FRAME_COUNT
                ];

        for (
            int index = 0;
            index < SHARP_SHADOW_DASH_FRAME_COUNT;
            index++
        ) {
            String path =
                String.format(
                    "sprites/effects/charms/sharp_shadow_dash/sharp_shadow_dash_%03d.png",
                    index
                );

            Texture texture =
                new Texture(
                    Gdx.files.internal(path)
                );

            texture.setFilter(
                Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear
            );

            sharpShadowDashTextures[index] =
                texture;

            sharpShadowDashFrames[index] =
                new TextureRegion(texture);
        }
    }

    private void loadWhiteDashEffect() {
        whiteDashEffectTexture =
            new Texture(
                Gdx.files.internal(
                    WHITE_DASH_EFFECT_PATH
                )
            );

        whiteDashEffectTexture.setFilter(
            Texture.TextureFilter.Linear,
            Texture.TextureFilter.Linear
        );

        whiteDashEffectFrames = splitHorizontalSheet(
            whiteDashEffectTexture,
            WHITE_DASH_EFFECT_FRAME_COUNT
        );
    }

    private void loadSlashEffects() {
        slashEffectTextures =
            new Texture[
                SLASH_EFFECT_FRAME_COUNT * 3
            ];

        slashHorizontalEffectFrames =
            loadSlashEffectFrames(
                SLASH_EFFECT_HORIZONTAL_PATH_FORMAT,
                0
            );

        slashUpEffectFrames =
            loadSlashEffectFrames(
                SLASH_EFFECT_UP_PATH_FORMAT,
                SLASH_EFFECT_FRAME_COUNT
            );

        slashDownEffectFrames =
            loadSlashEffectFrames(
                SLASH_EFFECT_DOWN_PATH_FORMAT,
                SLASH_EFFECT_FRAME_COUNT * 2
            );
    }

    private TextureRegion[] loadSlashEffectFrames(
        String pathFormat,
        int textureOffset
    ) {
        TextureRegion[] frames =
            new TextureRegion[
                SLASH_EFFECT_FRAME_COUNT
            ];

        for (
            int index = 0;
            index < SLASH_EFFECT_FRAME_COUNT;
            index++
        ) {
            Texture texture =
                new Texture(
                    Gdx.files.internal(
                        String.format(
                            pathFormat,
                            index
                        )
                    )
                );

            texture.setFilter(
                Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear
            );

            slashEffectTextures[textureOffset + index] =
                texture;

            frames[index] =
                new TextureRegion(texture);
        }

        return frames;
    }

    private TextureRegion getSharpShadowDashFrame(
        float animationTime
    ) {
        if (
            sharpShadowDashFrames == null
                || sharpShadowDashFrames.length == 0
        ) {
            return null;
        }

        if (animationTime < 0f) {
            animationTime = 0f;
        }

        int frameIndex =
            (int) (
                animationTime
                    / SHARP_SHADOW_DASH_FRAME_DURATION
            );

        if (
            frameIndex
                >= sharpShadowDashFrames.length
        ) {
            frameIndex =
                sharpShadowDashFrames.length - 1;
        }

        return sharpShadowDashFrames[
            frameIndex
            ];
    }


    private void handleCharmInventoryClick() {
        if (!controller.isCharmInventoryOpen()) {
            return;
        }

        if (!Gdx.input.justTouched()) {
            return;
        }

        charmTouchPosition.set(
            Gdx.input.getX(),
            Gdx.input.getY()
        );

        stage.screenToStageCoordinates(
            charmTouchPosition
        );

        calculateCharmMenuPanelBounds(
            charmMenuPanelBounds
        );

        CharmType[] charms =
            CharmType.values();

        for (
            int index = 0;
            index < charms.length;
            index++
        ) {
            calculateCharmCardBounds(
                index,
                charmMenuPanelBounds,
                charmCardBounds
            );

            if (
                charmCardBounds.contains(
                    charmTouchPosition.x,
                    charmTouchPosition.y
                )
            ) {
                handleCharmClick(
                    charms[index]
                );

                return;
            }
        }
    }

    private void handleCharmClick(
        CharmType clickedCharm
    ) {
        if (clickedCharm == null) {
            return;
        }

        /*
         * First click only selects the charm and shows
         * its name and description.
         */
        if (selectedCharm != clickedCharm) {
            selectedCharm = clickedCharm;
            return;
        }

        /*
         * Second click on the same charm equips or
         * unequips it.
         */
        controller.toggleCharmFromInventory(
            clickedCharm
        );
    }

    private void drawCharmInventoryMenu() {
        if (!controller.isCharmInventoryOpen()) {
            return;
        }

        if (selectedCharm == null) {
            selectedCharm =
                CharmType.values()[0];
        }

        calculateCharmMenuPanelBounds(
            charmMenuPanelBounds
        );

        Gdx.gl.glEnable(
            GL20.GL_BLEND
        );

        Gdx.gl.glBlendFunc(
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE_MINUS_SRC_ALPHA
        );

        shapeRenderer.setProjectionMatrix(
            stage.getCamera().combined
        );

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        );

        shapeRenderer.setColor(
            0f,
            0f,
            0f,
            0.72f
        );

        shapeRenderer.rect(
            0f,
            0f,
            stage.getViewport().getWorldWidth(),
            stage.getViewport().getWorldHeight()
        );

        shapeRenderer.setColor(
            0.055f,
            0.058f,
            0.075f,
            0.96f
        );

        shapeRenderer.rect(
            charmMenuPanelBounds.x,
            charmMenuPanelBounds.y,
            charmMenuPanelBounds.width,
            charmMenuPanelBounds.height
        );

        drawCharmCardsBackground();

        shapeRenderer.end();

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        );

        shapeRenderer.setColor(
            0.70f,
            0.72f,
            0.84f,
            1f
        );

        shapeRenderer.rect(
            charmMenuPanelBounds.x,
            charmMenuPanelBounds.y,
            charmMenuPanelBounds.width,
            charmMenuPanelBounds.height
        );

        drawCharmCardsOutlines();

        shapeRenderer.end();

        Gdx.gl.glDisable(
            GL20.GL_BLEND
        );

        batch.setProjectionMatrix(
            stage.getCamera().combined
        );

        batch.begin();

        drawCharmInventoryTextAndIcons();

        batch.end();
    }

    private void drawCharmCardsBackground() {
        CharmType[] charms =
            CharmType.values();

        for (
            int index = 0;
            index < charms.length;
            index++
        ) {
            CharmType charm =
                charms[index];

            calculateCharmCardBounds(
                index,
                charmMenuPanelBounds,
                charmCardBounds
            );

            if (controller.isCharmEquipped(charm)) {
                shapeRenderer.setColor(
                    0.18f,
                    0.17f,
                    0.095f,
                    0.96f
                );
            } else {
                shapeRenderer.setColor(
                    0.09f,
                    0.095f,
                    0.125f,
                    0.92f
                );
            }

            shapeRenderer.rect(
                charmCardBounds.x,
                charmCardBounds.y,
                charmCardBounds.width,
                charmCardBounds.height
            );
        }
    }

    private void drawCharmCardsOutlines() {
        CharmType[] charms =
            CharmType.values();

        for (
            int index = 0;
            index < charms.length;
            index++
        ) {
            CharmType charm =
                charms[index];

            calculateCharmCardBounds(
                index,
                charmMenuPanelBounds,
                charmCardBounds
            );

            if (selectedCharm == charm) {
                shapeRenderer.setColor(
                    0.95f,
                    0.76f,
                    0.34f,
                    1f
                );
            } else if (controller.isCharmEquipped(charm)) {
                shapeRenderer.setColor(
                    0.74f,
                    0.86f,
                    1f,
                    1f
                );
            } else {
                shapeRenderer.setColor(
                    0.36f,
                    0.38f,
                    0.48f,
                    1f
                );
            }

            shapeRenderer.rect(
                charmCardBounds.x,
                charmCardBounds.y,
                charmCardBounds.width,
                charmCardBounds.height
            );
        }
    }

    private void drawCharmInventoryTextAndIcons() {
        BitmapFont titleFont =
            skin.getFont(
                "window"
            );

        BitmapFont textFont =
            skin.getFont(
                "font"
            );

        titleFont.setColor(
            0.92f,
            0.93f,
            1f,
            1f
        );

        titleFont.draw(
            batch,
            "CHARMS",
            charmMenuPanelBounds.x + 34f,
            charmMenuPanelBounds.y
                + charmMenuPanelBounds.height
                - 28f
        );

        textFont.setColor(
            0.72f,
            0.75f,
            0.86f,
            1f
        );

        textFont.draw(
            batch,
            "Press I to close. Click a charm to inspect it. Click it again to equip or unequip it.",
            charmMenuPanelBounds.x + 34f,
            charmMenuPanelBounds.y
                + charmMenuPanelBounds.height
                - 62f
        );

        drawCharmNotches(
            textFont
        );

        drawCharmCardsTextAndIcons(
            textFont
        );

        drawSelectedCharmDescription(
            titleFont,
            textFont
        );
    }

    private void drawCharmNotches(
        BitmapFont textFont
    ) {
        int usedNotches =
            controller.getUsedCharmNotches();

        int notchCapacity =
            controller.getCharmNotchCapacity();

        float startX =
            charmMenuPanelBounds.x
                + charmMenuPanelBounds.width
                - 230f;

        float centerY =
            charmMenuPanelBounds.y
                + charmMenuPanelBounds.height
                - 44f;

        textFont.setColor(
            0.86f,
            0.88f,
            0.96f,
            1f
        );

        textFont.draw(
            batch,
            "Notches "
                + usedNotches
                + "/"
                + notchCapacity,
            startX,
            centerY + 7f
        );

        batch.end();

        shapeRenderer.setProjectionMatrix(
            stage.getCamera().combined
        );

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        );

        for (
            int index = 0;
            index < notchCapacity;
            index++
        ) {
            if (index < usedNotches) {
                shapeRenderer.setColor(
                    0.95f,
                    0.76f,
                    0.34f,
                    1f
                );
            } else {
                shapeRenderer.setColor(
                    0.18f,
                    0.19f,
                    0.25f,
                    1f
                );
            }

            shapeRenderer.circle(
                startX + 98f + index * 28f,
                centerY,
                8f
            );
        }

        shapeRenderer.end();

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        );

        shapeRenderer.setColor(
            0.70f,
            0.72f,
            0.84f,
            1f
        );

        for (
            int index = 0;
            index < notchCapacity;
            index++
        ) {
            shapeRenderer.circle(
                startX + 98f + index * 28f,
                centerY,
                8f
            );
        }

        shapeRenderer.end();

        batch.begin();

        drawEquippedCharmIcons(
            startX,
            centerY
        );
    }

    private void drawEquippedCharmIcons(
        float startX,
        float centerY
    ) {
        if (charmIconTextures == null) {
            return;
        }

        float iconSize = 24f;

        int equippedIndex = 0;

        for (CharmType charm : CharmType.values()) {
            if (!controller.isCharmEquipped(charm)) {
                continue;
            }

            Texture texture =
                charmIconTextures.get(charm);

            if (texture != null) {
                float centerX =
                    startX
                        + 98f
                        + equippedIndex
                        * 28f;

                batch.draw(
                    texture,
                    centerX - iconSize / 2f,
                    centerY - iconSize / 2f,
                    iconSize,
                    iconSize
                );
            }

            equippedIndex++;

            if (
                equippedIndex
                    >= controller.getCharmNotchCapacity()
            ) {
                break;
            }
        }
    }

    private void drawSelectedCharmDescription(
        BitmapFont titleFont,
        BitmapFont textFont
    ) {
        CharmType charm =
            selectedCharm;

        if (charm == null) {
            charm =
                CharmType.values()[0];
        }

        titleFont.setColor(
            0.95f,
            0.76f,
            0.34f,
            1f
        );

        titleFont.draw(
            batch,
            charm.getDisplayName(),
            charmMenuPanelBounds.x + 34f,
            charmMenuPanelBounds.y + 116f,
            charmMenuPanelBounds.width - 68f,
            Align.center,
            false
        );

        textFont.setColor(
            0.88f,
            0.90f,
            1f,
            1f
        );

        textFont.draw(
            batch,
            charm.getDescription(),
            charmMenuPanelBounds.x + 80f,
            charmMenuPanelBounds.y + 84f,
            charmMenuPanelBounds.width - 160f,
            Align.center,
            true
        );

        String message =
            controller.getCharmInventoryMessage();

        if (
            message != null
                && !message.isBlank()
        ) {
            if (controller.didCharmEquipFail()) {
                textFont.setColor(
                    1f,
                    0.42f,
                    0.42f,
                    1f
                );
            } else {
                textFont.setColor(
                    0.70f,
                    0.74f,
                    0.86f,
                    1f
                );
            }

            textFont.draw(
                batch,
                message,
                charmMenuPanelBounds.x + 80f,
                charmMenuPanelBounds.y + 34f,
                charmMenuPanelBounds.width - 160f,
                Align.center,
                true
            );
        }
    }

    private void drawCharmCardsTextAndIcons(
        BitmapFont textFont
    ) {
        CharmType[] charms =
            CharmType.values();

        for (
            int index = 0;
            index < charms.length;
            index++
        ) {
            CharmType charm =
                charms[index];

            calculateCharmCardBounds(
                index,
                charmMenuPanelBounds,
                charmCardBounds
            );

            Texture texture =
                charmIconTextures == null
                    ? null
                    : charmIconTextures.get(charm);

            float iconX =
                charmCardBounds.x
                    + charmCardBounds.width / 2f
                    - CHARM_ICON_SIZE / 2f;

            float iconY =
                charmCardBounds.y
                    + charmCardBounds.height
                    - CHARM_ICON_SIZE
                    - 16f;

            if (texture != null) {
                batch.draw(
                    texture,
                    iconX,
                    iconY,
                    CHARM_ICON_SIZE,
                    CHARM_ICON_SIZE
                );
            }

            textFont.setColor(
                Color.WHITE
            );

            textFont.draw(
                batch,
                charm.getDisplayName(),
                charmCardBounds.x + 10f,
                charmCardBounds.y + 47f,
                charmCardBounds.width - 20f,
                Align.center,
                true
            );

            if (controller.isCharmEquipped(charm)) {
                textFont.setColor(
                    0.95f,
                    0.76f,
                    0.34f,
                    1f
                );

                textFont.draw(
                    batch,
                    "EQUIPPED",
                    charmCardBounds.x + 10f,
                    charmCardBounds.y + 20f,
                    charmCardBounds.width - 20f,
                    Align.center,
                    false
                );
            } else {
                textFont.setColor(
                    0.60f,
                    0.64f,
                    0.74f,
                    1f
                );

                textFont.draw(
                    batch,
                    "click to equip",
                    charmCardBounds.x + 10f,
                    charmCardBounds.y + 20f,
                    charmCardBounds.width - 20f,
                    Align.center,
                    false
                );
            }
        }
    }

    private void calculateCharmMenuPanelBounds(
        Rectangle out
    ) {
        float screenWidth =
            stage.getViewport().getWorldWidth();

        float screenHeight =
            stage.getViewport().getWorldHeight();

        float panelWidth =
            Math.min(
                CHARM_MENU_PANEL_WIDTH,
                screenWidth - 80f
            );

        float panelHeight =
            Math.min(
                CHARM_MENU_PANEL_HEIGHT,
                screenHeight - 60f
            );

        out.set(
            screenWidth / 2f - panelWidth / 2f,
            screenHeight / 2f - panelHeight / 2f,
            panelWidth,
            panelHeight
        );
    }

    private void calculateCharmCardBounds(
        int index,
        Rectangle panelBounds,
        Rectangle out
    ) {
        int column =
            index % CHARM_MENU_COLUMNS;

        int row =
            index / CHARM_MENU_COLUMNS;

        float totalCardsWidth =
            CHARM_MENU_COLUMNS
                * CHARM_CARD_WIDTH
                + (CHARM_MENU_COLUMNS - 1)
                * CHARM_CARD_GAP_X;

        float startX =
            panelBounds.x
                + panelBounds.width / 2f
                - totalCardsWidth / 2f;

        float firstRowTopY =
            panelBounds.y
                + panelBounds.height
                - 116f;

        float x =
            startX
                + column
                * (CHARM_CARD_WIDTH + CHARM_CARD_GAP_X);

        float y =
            firstRowTopY
                - CHARM_CARD_HEIGHT
                - row
                * (CHARM_CARD_HEIGHT + CHARM_CARD_GAP_Y);

        out.set(
            x,
            y,
            CHARM_CARD_WIDTH,
            CHARM_CARD_HEIGHT
        );
    }


    private void drawCharmObtainedMessage() {
        if (!controller.shouldShowCharmObtainedMessage()) {
            return;
        }

        String message =
            controller.getCharmObtainedMessage();

        if (
            message == null
                || message.isBlank()
        ) {
            return;
        }

        float screenWidth =
            stage
                .getViewport()
                .getWorldWidth();

        float screenHeight =
            stage
                .getViewport()
                .getWorldHeight();

        batch.setProjectionMatrix(
            stage.getCamera().combined
        );

        batch.begin();

        BitmapFont font =
            skin.getFont(
                "window"
            );

        font.setColor(
            0.95f,
            0.76f,
            0.34f,
            1f
        );

        font.draw(
            batch,
            message,
            0f,
            screenHeight - 92f,
            screenWidth,
            Align.center,
            false
        );

        batch.end();
    }


    private void drawCheatMessage() {
        String message =
            controller.getCheatMessage();

        if (message == null || message.isBlank()) {
            return;
        }

        float screenWidth =
            stage.getViewport().getWorldWidth();

        float screenHeight =
            stage.getViewport().getWorldHeight();

        batch.setProjectionMatrix(
            stage.getCamera().combined
        );

        batch.begin();

        BitmapFont font =
            skin.getFont(
                "window"
            );

        font.setColor(
            0.50f,
            0.90f,
            1f,
            1f
        );

        font.draw(
            batch,
            message,
            0f,
            screenHeight - 126f,
            screenWidth,
            Align.center,
            false
        );

        batch.end();
    }

    private void drawPlayerHud() {
        int currentMasks =
            controller.getCurrentMasks();

        int maximumMasks =
            controller.getMaximumMasks();

        float screenHeight =
            stage
                .getViewport()
                .getWorldHeight();

        float maskStartX = 28f;
        float maskY = screenHeight - 32f;

        float maskSpacing = 31f;
        float maskRadius = 11f;

        float vesselCenterX =
            maskStartX
                + maximumMasks
                * maskSpacing
                + 20f;

        float vesselCenterY = maskY;

        float vesselOuterRadius = 20f;
        float vesselInnerRadius = 15f;

        /*
         * The HUD uses the fixed stage camera.
         */
        shapeRenderer.setProjectionMatrix(
            stage.getCamera().combined
        );

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        );

        /*
         * Health masks.
         */
        for (
            int index = 0;
            index < maximumMasks;
            index++
        ) {
            if (index < currentMasks) {
                shapeRenderer.setColor(
                    0.92f,
                    0.94f,
                    1f,
                    1f
                );
            } else {
                shapeRenderer.setColor(
                    0.18f,
                    0.19f,
                    0.24f,
                    1f
                );
            }

            shapeRenderer.circle(
                maskStartX
                    + index * maskSpacing,
                maskY,
                maskRadius
            );
        }

        /*
         * Soul vessel background.
         */
        shapeRenderer.setColor(
            0.10f,
            0.11f,
            0.16f,
            1f
        );

        shapeRenderer.circle(
            vesselCenterX,
            vesselCenterY,
            vesselOuterRadius
        );

        shapeRenderer.setColor(
            0.20f,
            0.22f,
            0.29f,
            1f
        );

        shapeRenderer.circle(
            vesselCenterX,
            vesselCenterY,
            vesselInnerRadius
        );

        /*
         * Soul fill.
         */
        float soulRatio =
            controller.getSoulFillRatio();

        float vesselBottom =
            vesselCenterY
                - vesselInnerRadius;

        float fillTop =
            vesselBottom
                + vesselInnerRadius
                * 2f
                * soulRatio;

        shapeRenderer.setColor(
            0.93f,
            0.95f,
            1f,
            1f
        );

        int rows = 30;

        float rowHeight =
            vesselInnerRadius
                * 2f
                / rows;

        for (
            int row = 0;
            row < rows;
            row++
        ) {
            float rowY =
                vesselBottom
                    + row * rowHeight;

            if (rowY > fillTop) {
                break;
            }

            float relativeY =
                rowY
                    - vesselCenterY
                    + rowHeight / 2f;

            float halfWidth =
                (float) Math.sqrt(
                    Math.max(
                        0f,
                        vesselInnerRadius
                            * vesselInnerRadius
                            - relativeY
                            * relativeY
                    )
                );

            shapeRenderer.rect(
                vesselCenterX
                    - halfWidth,
                rowY,
                halfWidth * 2f,
                rowHeight + 0.5f
            );
        }

        /*
         * Focus progress bar.
         */
        if (controller.isFocusing()) {
            float barWidth = 80f;
            float barHeight = 6f;

            float barX =
                vesselCenterX
                    - barWidth / 2f;

            float barY =
                vesselCenterY
                    - vesselOuterRadius
                    - 13f;

            shapeRenderer.setColor(
                0.15f,
                0.16f,
                0.21f,
                1f
            );

            shapeRenderer.rect(
                barX,
                barY,
                barWidth,
                barHeight
            );

            shapeRenderer.setColor(
                0.90f,
                0.92f,
                1f,
                1f
            );

            shapeRenderer.rect(
                barX,
                barY,
                barWidth
                    * controller
                    .getFocusProgress(),
                barHeight
            );
        }

        shapeRenderer.end();

        /*
         * HUD outlines.
         */
        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        );

        shapeRenderer.setColor(
            0.55f,
            0.58f,
            0.68f,
            1f
        );

        for (
            int index = 0;
            index < maximumMasks;
            index++
        ) {
            shapeRenderer.circle(
                maskStartX
                    + index * maskSpacing,
                maskY,
                maskRadius
            );
        }

        shapeRenderer.circle(
            vesselCenterX,
            vesselCenterY,
            vesselOuterRadius
        );

        shapeRenderer.end();
    }

    private void finishAnimationIfNecessary() {
        Player player =
            controller.getPlayer();

        PlayerAnimationType animationType =
            player.getAnimationType();

        if (
            knightAnimationManager.isLooping(
                animationType
            )
        ) {
            return;
        }

        if (
            knightAnimationManager.isFinished(
                animationType,
                player.getAnimationTime()
            )
        ) {
            controller.onAnimationFinished(
                animationType
            );
        }
    }

    @Override
    public void resize(
        int width,
        int height
    ) {
        if (worldCamera != null) {
            worldCamera.resize(
                width,
                height
            );
        }

        if (stage != null) {
            stage.getViewport().update(
                width,
                height,
                true
            );
        }
    }

    @Override
    public void hide() {
        controller.removeAchievementObserver(
            popupObserver
        );

        if (
            stage != null
                && Gdx.input
                .getInputProcessor()
                == stage
        ) {
            Gdx.input.setInputProcessor(
                null
            );
        }
    }

    @Override
    public void dispose() {
        controller.removeAchievementObserver(
            popupObserver
        );

        if (
            knightAnimationManager != null
        ) {
            knightAnimationManager.dispose();
        }

        if (
            huskAnimationManager != null
        ) {
            huskAnimationManager.dispose();
        }

        if (
            crawlidAnimationManager != null
        ) {
            crawlidAnimationManager.dispose();
        }

        if (crackedWallRenderer != null) {
            crackedWallRenderer.dispose();
        }

        if (mapRenderer != null) {
            mapRenderer.dispose();
        }

        if (batch != null) {
            batch.dispose();
        }

        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }

        if (stage != null) {
            stage.dispose();
        }

        if (skin != null) {
            skin.dispose();
        }

        if (
            crystalAnimationManager != null
        ) {
            crystalAnimationManager.dispose();
        }

        if (
            crystalLaserRenderer != null
        ) {
            crystalLaserRenderer.dispose();
        }
        if (wingedAnimationManager != null) {
            wingedAnimationManager.dispose();
        }

        if (falseKnightAnimationManager != null) {
            falseKnightAnimationManager.dispose();
        }

        if (
            zoteAnimationManager != null
        ) {
            zoteAnimationManager.dispose();
        }

        if (rainEffect != null) {
            rainEffect.dispose();
        }

        if (charmIconTextures != null) {
            for (Texture texture : charmIconTextures.values()) {
                texture.dispose();
            }

            charmIconTextures.clear();
        }

        if (whiteDashEffectTexture != null) {
            whiteDashEffectTexture.dispose();
            whiteDashEffectTexture = null;
            whiteDashEffectFrames = null;
        }

        if (slashEffectTextures != null) {
            for (Texture texture : slashEffectTextures) {
                if (texture != null) {
                    texture.dispose();
                }
            }

            slashEffectTextures = null;
            slashHorizontalEffectFrames = null;
            slashUpEffectFrames = null;
            slashDownEffectFrames = null;
        }

        if (sharpShadowDashTextures != null) {
            for (Texture texture : sharpShadowDashTextures) {
                if (texture != null) {
                    texture.dispose();
                }
            }

            sharpShadowDashTextures = null;
            sharpShadowDashFrames = null;
        }

        if (blastSoulTexture != null) {
            blastSoulTexture.dispose();
            blastSoulTexture = null;
            blastSoulFrames = null;
        }

        if (soulBallTexture != null) {
            soulBallTexture.dispose();
            soulBallTexture = null;
            soulBallFrames = null;
        }

        if (soulBallEndTexture != null) {
            soulBallEndTexture.dispose();
            soulBallEndTexture = null;
            soulBallEndFrames = null;
        }

        if (shadowBallTexture != null) {
            shadowBallTexture.dispose();
            shadowBallTexture = null;
            shadowBallFrames = null;
        }

        if (shadowBallEndTexture != null) {
            shadowBallEndTexture.dispose();
            shadowBallEndTexture = null;
            shadowBallEndFrames = null;
        }

        if (soulScreamTexture != null) {
            soulScreamTexture.dispose();
            soulScreamTexture = null;
            soulScreamFrames = null;
        }

        if (shadowScreamTexture != null) {
            shadowScreamTexture.dispose();
            shadowScreamTexture = null;
            shadowScreamFrames = null;
        }

        if (!preserveControllerOnDispose) {
            controller.dispose();
        }
    }
}
