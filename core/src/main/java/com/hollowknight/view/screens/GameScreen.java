package com.hollowknight.view.screens;

import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
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

    private static final float SHARP_SHADOW_DASH_FRAME_DURATION = 0.049f;

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

    private static final float VOID_SHADE_SOUL_EFFECT_HEIGHT = 115f;

    private static final float VOID_ABYSS_SHRIEK_EFFECT_HEIGHT = 310f;

    private static final boolean VOID_SHADE_SOUL_SOURCE_FACES_RIGHT = true;

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
    private Texture voidShadeSoulTexture;
    private Texture voidAbyssShriekTexture;
    private final Rectangle charmMenuPanelBounds = new Rectangle();
    private final Rectangle charmCardBounds = new Rectangle();
    private final Vector2 charmTouchPosition = new Vector2();

    private GameCamera worldCamera;

    public GameScreen(
        GameController controller
    ) {
        this.controller = controller;
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

        voidShadeSoulTexture = loadEffectTexture(
            "sprites/effects/abilities/void_shade_soul.png"
        );

        voidAbyssShriekTexture = loadEffectTexture(
            "sprites/effects/abilities/void_abyss_shriek.png"
        );

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

    @Override
    public void render(float delta) {
        controller.update(
            delta,
            KNIGHT_DRAW_WIDTH,
            KNIGHT_DRAW_HEIGHT
        );

        updateWorldCamera(delta);

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

        drawVoidShadeSoulEffect();
        drawVoidAbyssShriekEffect();
// * Draw the delayed shadow trail behind the Knight.
// * The Knight itself is drawn black inside drawKnight().
        drawZote();
        drawSharpShadowDashEffect();
        drawKnight();
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

        handleCharmInventoryClick();
        drawPlayerHud();
        drawZoteDialogueBox();
        drawCharmInventoryMenu();

        stage.draw();

        finishAnimationIfNecessary();
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

        shapeRenderer.setColor(Color.BLACK);

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


    private void drawSharpShadowDashEffect() {
        if (
            !controller.isSharpShadowDashVisualActive()
                || sharpShadowDashFrames == null
        ) {
            return;
        }

        Player player =
            controller.getPlayer();

        float animationTime =
            player.getAnimationTime();

        float x =
            player.getPosition().x;

        float y =
            player.getPosition().y;

        int direction =
            player.isFacingRight()
                ? 1
                : -1;

        batch.setProjectionMatrix(
            worldCamera.getCombined()
        );

        batch.begin();

        /*
         * Two delayed copies behind the Knight.
         * This makes the shadow feel paced instead of
         * one static sprite glued on top of the dash.
         */
        drawSharpShadowTrailCopy(
            animationTime
                - SHARP_SHADOW_DASH_FRAME_DURATION,
            x
                - direction
                * SHARP_SHADOW_TRAIL_SPACING,
            y,
            player.isFacingRight(),
            0.32f
        );

        drawSharpShadowTrailCopy(
            animationTime
                - SHARP_SHADOW_DASH_FRAME_DURATION * 2f,
            x
                - direction
                * SHARP_SHADOW_TRAIL_SPACING * 2f,
            y,
            player.isFacingRight(),
            0.18f
        );

        batch.setColor(Color.WHITE);

        batch.end();
    }
    private void drawSharpShadowTrailCopy(
        float animationTime,
        float x,
        float y,
        boolean facingRight,
        float alpha
    ) {
        TextureRegion frame =
            getSharpShadowDashFrame(
                animationTime
            );

        if (frame == null) {
            return;
        }

        batch.setColor(
            1f,
            1f,
            1f,
            alpha
        );

        drawPlayerSizedFrame(
            frame,
            x,
            y,
            facingRight,
            SHARP_SHADOW_SOURCE_FACES_RIGHT
        );
    }

    private void drawVoidShadeSoulEffect() {
        if (
            voidShadeSoulTexture == null
                || !controller.isVoidShadeSoulActive()
        ) {
            return;
        }

        Rectangle bounds =
            controller.getVoidShadeSoulBounds();

        float scale =
            VOID_SHADE_SOUL_EFFECT_HEIGHT
                / voidShadeSoulTexture.getHeight();

        float drawWidth =
            voidShadeSoulTexture.getWidth()
                * scale;

        float drawHeight =
            voidShadeSoulTexture.getHeight()
                * scale;

        float drawX =
            bounds.x
                + bounds.width / 2f
                - drawWidth / 2f;

        float drawY =
            bounds.y
                + bounds.height / 2f
                - drawHeight / 2f;

        boolean shouldFlip =
            controller.isVoidShadeSoulFacingRight()
                != VOID_SHADE_SOUL_SOURCE_FACES_RIGHT;

        batch.setProjectionMatrix(
            worldCamera.getCombined()
        );

        batch.begin();

        batch.setColor(
            1f,
            1f,
            1f,
            0.92f
        );

        if (shouldFlip) {
            batch.draw(
                voidShadeSoulTexture,
                drawX + drawWidth,
                drawY,
                -drawWidth,
                drawHeight
            );
        } else {
            batch.draw(
                voidShadeSoulTexture,
                drawX,
                drawY,
                drawWidth,
                drawHeight
            );
        }

        batch.setColor(Color.WHITE);

        batch.end();
    }

    private void drawVoidAbyssShriekEffect() {
        if (
            voidAbyssShriekTexture == null
                || !controller
                .isVoidAbyssShriekActive()
        ) {
            return;
        }

        Rectangle bounds =
            controller.getVoidAbyssShriekBounds();

        float scale =
            VOID_ABYSS_SHRIEK_EFFECT_HEIGHT
                / voidAbyssShriekTexture.getHeight();

        float drawWidth =
            voidAbyssShriekTexture.getWidth()
                * scale;

        float drawHeight =
            voidAbyssShriekTexture.getHeight()
                * scale;

        float drawX =
            bounds.x
                + bounds.width / 2f
                - drawWidth / 2f;

        float drawY =
            bounds.y
                + bounds.height / 2f
                - drawHeight / 2f;

        batch.setProjectionMatrix(
            worldCamera.getCombined()
        );

        batch.begin();

        batch.setColor(
            1f,
            1f,
            1f,
            0.94f
        );

        batch.draw(
            voidAbyssShriekTexture,
            drawX,
            drawY,
            drawWidth,
            drawHeight
        );

        batch.setColor(Color.WHITE);

        batch.end();
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
                controller.toggleCharmFromInventory(
                    charms[index]
                );

                return;
            }
        }
    }

    private void drawCharmInventoryMenu() {
        if (!controller.isCharmInventoryOpen()) {
            return;
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

            if (controller.isCharmEquipped(charm)) {
                shapeRenderer.setColor(
                    0.95f,
                    0.76f,
                    0.34f,
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
            "Press I to close. Click a charm to equip or unequip it.",
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

        String message =
            controller.getCharmInventoryMessage();

        if (
            message == null
                || message.isBlank()
        ) {
            message =
                "Each charm uses one notch. Maximum equipped charms: 3.";
        }

        if (controller.didCharmEquipFail()) {
            textFont.setColor(
                1f,
                0.42f,
                0.42f,
                1f
            );
        } else {
            textFont.setColor(
                0.88f,
                0.90f,
                1f,
                1f
            );
        }

        textFont.draw(
            batch,
            message,
            charmMenuPanelBounds.x + 34f,
            charmMenuPanelBounds.y + 36f,
            charmMenuPanelBounds.width - 68f,
            Align.center,
            true
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

        if (sharpShadowDashTextures != null) {
            for (Texture texture : sharpShadowDashTextures) {
                if (texture != null) {
                    texture.dispose();
                }
            }

            sharpShadowDashTextures = null;
            sharpShadowDashFrames = null;
        }

        if (voidShadeSoulTexture != null) {
            voidShadeSoulTexture.dispose();
        }

        if (voidAbyssShriekTexture != null) {
            voidAbyssShriekTexture.dispose();
        }

        controller.dispose();
    }
}
