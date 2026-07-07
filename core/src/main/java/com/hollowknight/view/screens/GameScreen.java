package com.hollowknight.view.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
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
import com.hollowknight.model.player.PlayerAnimationType;
import com.hollowknight.view.animation.CrawlidAnimationManager;
import com.hollowknight.view.animation.HuskHornheadAnimationManager;
import com.hollowknight.view.animation.KnightAnimationManager;
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

        worldCamera.reset(
            getPlayerCameraTargetX(player),
            getPlayerCameraTargetY(player)
        );
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

        drawRain();

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

        drawKnight();
        drawActiveAttackHitbox();
        drawMapForeground();

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

        drawPlayerHud();

        stage.draw();

        finishAnimationIfNecessary();
    }

    private void updateWorldCamera(
        float delta
    ) {
        Player player =
            controller.getPlayer();

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



    private void drawKnight() {
        if (
            !controller.shouldDrawPlayer()
        ) {
            return;
        }

        Player player =
            controller.getPlayer();

        TextureRegion frame =
            knightAnimationManager.getFrame(
                player.getAnimationType(),
                player.getAnimationTime()
            );

        batch.setProjectionMatrix(
            worldCamera.getCombined()
        );

        batch.begin();

        float x =
            player.getPosition().x;

        float y =
            player.getPosition().y;

        boolean shouldFlip =
            player.isFacingRight()
                != KNIGHT_SOURCE_FACES_RIGHT;

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

        batch.end();
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
        if (
            wingedAnimationManager != null
        ) {
            wingedAnimationManager.dispose();
        }

        controller.dispose();
    }
}
